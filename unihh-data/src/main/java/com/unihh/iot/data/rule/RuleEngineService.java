package com.unihh.iot.data.rule;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.unihh.iot.common.grpc.GrpcClient;
import com.unihh.iot.common.grpc.GrpcModels;
import com.unihh.iot.common.model.AlarmRecord;
import com.unihh.iot.common.model.DeviceConfig;
import com.unihh.iot.common.model.DevicePointMapping;
import com.unihh.iot.common.model.PointData;
import com.unihh.iot.common.model.Rule;
import com.unihh.iot.common.model.RuleAction;
import com.unihh.iot.common.mqtt.MqttBridgeClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 高性能实时数据规则引擎服务 (High-Performance Rule Engine Service)
 * 
 * 核心设计亮点：
 * 1. O(1) 哈希倒排索引 (Inverted Hash Index)：避免对全量规则线性扫描，上报点位微秒级命中绑定规则。
 * 2. 异步执行解耦 (Decoupled Async Action Executor)：纯内存阈值判断，重 I/O 动作隔离在独立线程池，绝不阻塞数采主链路。
 * 3. 动态热同步机制 (Dynamic Sync via gRPC & MQTT)：订阅 Admin 规则与设备变更，同时后台周期保活刷新。
 * 4. 工业状态机防抖与抑制窗口 (Debounce & Suppression Window)：防止现场高频噪声引发控制与告警风暴。
 * 5. 物模型服务参数默认值合并与下发 (Downlink Service Execution)：自动合并物模型默认入参并真实发布至 MQTT 反控总线。
 */
@Service
public class RuleEngineService {
    private static final Logger log = LoggerFactory.getLogger(RuleEngineService.class);

    @Value("${unihh.services.admin-service.host:127.0.0.1}")
    private String adminGrpcHost;

    @Value("${unihh.services.admin-service.grpc-port:29001}")
    private int adminGrpcPort;

    // 1. O(1) 内存倒排索引: Key 格式为 "DEV:{deviceId}:{pointCode}" 或 "POINT:{pointCode}"
    private final Map<String, List<Rule>> ruleIndex = new ConcurrentHashMap<>();

    // 2. 设备驱动映射缓存: deviceId -> driverCode
    private final Map<String, String> deviceDriverMap = new ConcurrentHashMap<>();

    // 2.1 设备基础配置与连接参数缓存: deviceId -> DeviceConfig
    private final Map<String, DeviceConfig> deviceConfigCache = new ConcurrentHashMap<>();

    // 2.2 设备物理测点地址映射缓存: deviceId -> List<DevicePointMapping>
    private final Map<String, List<DevicePointMapping>> deviceMappingCache = new ConcurrentHashMap<>();

    // 3. 防抖与告警抑制窗口缓存: Key 为 "ruleId:deviceId"，Value 为上次触发时间戳 (毫秒)
    private final Map<String, Long> lastTriggerTimeMap = new ConcurrentHashMap<>();

    // 默认防抖冷却窗口 (毫秒, 默认 5 秒内不重复触发同一控制动作)
    private static final long DEFAULT_COOLDOWN_MS = 5_000L;

    // 4. MQTT 客户端引用 (由 DataMqttConsumer 注入，用于反控指令发布)
    private volatile MqttBridgeClient mqttClient;

    // 5. 异步动作执行线程池 (与数据采集消费主线程彻底隔离)
    private final ThreadPoolExecutor actionExecutor = new ThreadPoolExecutor(
            4, 16, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(10000),
            new ThreadFactory() {
                private final AtomicInteger seq = new AtomicInteger(1);
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "rule-action-exec-" + seq.getAndIncrement());
                    t.setDaemon(true);
                    return t;
                }
            },
            new ThreadPoolExecutor.CallerRunsPolicy()
    );

    // 6. 后台规则与元数据定时同步调度器
    private final ScheduledExecutorService syncScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "rule-sync-scheduler");
        t.setDaemon(true);
        return t;
    });

    public void setMqttClient(MqttBridgeClient mqttClient) {
        this.mqttClient = mqttClient;
        log.info("[RuleEngineService] MQTT bridge client injected for rule actions downlink.");
    }

    @PostConstruct
    public void init() {
        log.info("[RuleEngineService] High-performance rule engine initialized with inverted hash index and async worker pool.");
        // 加载默认内置规则种子，确保即开即用
        loadBuiltinSeedRules();

        // 启动后台定时同步任务 (启动 1 秒后首次拉取，之后每 10 秒定期全量同步兜底)
        syncScheduler.scheduleWithFixedDelay(this::syncRulesFromAdmin, 1, 10, TimeUnit.SECONDS);
    }

    /**
     * 从 Admin 微服务全量同步规则与设备元数据
     */
    public synchronized void syncRulesFromAdmin() {
        try {
            // 1. 同步规则列表
            GrpcModels.RpcResponse resp = GrpcClient.call(adminGrpcHost, adminGrpcPort, "AdminGrpcService", "getRules", null);
            if (resp != null && resp.isSuccess() && resp.getDataJson() != null) {
                List<Rule> rules = JSON.parseArray(resp.getDataJson(), Rule.class);
                if (rules != null) {
                    refreshRules(rules);
                    log.info("[RuleEngine] Successfully synchronized {} rules from Admin gRPC.", rules.size());
                }
            } else {
                log.debug("[RuleEngine] Sync rules response from Admin gRPC: {}", resp != null ? resp.getMessage() : "null");
            }

            // 2. 同步设备与驱动映射 (用于下发服务指令时指定正确的 driverCode 与通信参数)
            GrpcModels.RpcResponse devResp = GrpcClient.call(adminGrpcHost, adminGrpcPort, "AdminGrpcService", "getDevices", null);
            if (devResp != null && devResp.isSuccess() && devResp.getDataJson() != null) {
                List<DeviceConfig> devices = JSON.parseArray(devResp.getDataJson(), DeviceConfig.class);
                if (devices != null) {
                    for (DeviceConfig dev : devices) {
                        if (dev.getDeviceId() != null) {
                            String devId = dev.getDeviceId().trim();
                            deviceConfigCache.put(devId, dev);
                            if (dev.getDriverCode() != null) {
                                deviceDriverMap.put(devId, dev.getDriverCode().trim());
                            }
                        }
                    }
                }
            }

            // 同步时清空测点地址映射缓存，确保下次调用获取最新配置
            deviceMappingCache.clear();
        } catch (Exception e) {
            log.warn("[RuleEngine] Notice while syncing rules/devices from Admin gRPC: {}", e.getMessage());
        }
    }

    /**
     * 清理指定设备的缓存 (当接收到 Admin 配置或测点更新通知时调用)
     */
    public void invalidateDeviceCache(String deviceId) {
        if (deviceId == null || deviceId.trim().isEmpty() || "ALL".equalsIgnoreCase(deviceId)) {
            deviceConfigCache.clear();
            deviceMappingCache.clear();
            deviceDriverMap.clear();
            log.info("[RuleEngine] 已清空所有设备的元数据与测点映射缓存");
        } else {
            String devId = deviceId.trim();
            deviceConfigCache.remove(devId);
            deviceMappingCache.remove(devId);
            log.info("[RuleEngine] 已清空设备 [{}] 的元数据与测点映射缓存", devId);
        }
    }

    private DeviceConfig getOrFetchDevice(String deviceId) {
        if (deviceId == null || deviceId.trim().isEmpty()) return null;
        String devId = deviceId.trim();
        DeviceConfig cfg = deviceConfigCache.get(devId);
        if (cfg != null) return cfg;
        try {
            GrpcModels.RpcResponse resp = GrpcClient.call(adminGrpcHost, adminGrpcPort, "AdminGrpcService", "getDevice", devId);
            if (resp != null && resp.isSuccess() && resp.getDataJson() != null) {
                cfg = JSON.parseObject(resp.getDataJson(), DeviceConfig.class);
                if (cfg != null) {
                    deviceConfigCache.put(devId, cfg);
                    if (cfg.getDriverCode() != null) {
                        deviceDriverMap.put(devId, cfg.getDriverCode().trim());
                    }
                    return cfg;
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    public List<DevicePointMapping> getOrFetchMappings(String deviceId) {
        if (deviceId == null || deviceId.trim().isEmpty()) return Collections.emptyList();
        String devId = deviceId.trim();
        List<DevicePointMapping> list = deviceMappingCache.get(devId);
        if (list != null && !list.isEmpty()) return list;

        try {
            log.info("[RuleEngine] 发起 gRPC 请求获取设备 [{}] 测点物理映射表: {}:{} -> AdminGrpcService.getPointAddressMappings",
                    devId, adminGrpcHost, adminGrpcPort);
            GrpcModels.RpcResponse resp = GrpcClient.call(adminGrpcHost, adminGrpcPort, "AdminGrpcService", "getPointAddressMappings", devId);
            if (resp != null && resp.isSuccess() && resp.getDataJson() != null) {
                list = JSON.parseArray(resp.getDataJson(), DevicePointMapping.class);
                if (list != null && !list.isEmpty()) {
                    deviceMappingCache.put(devId, list);
                    log.info("[RuleEngine] 成功通过 gRPC 获取并缓存设备 [{}] 测点物理映射表: 测点数={}", devId, list.size());
                    return list;
                } else {
                    log.warn("[RuleEngine] 设备 [{}] 在 Admin 端的物理测点映射表为空，严禁缓存空列表", devId);
                }
            } else {
                log.warn("[RuleEngine] gRPC 获取设备 [{}] 测点物理映射表失败: resp={}", devId, resp != null ? resp.getMessage() : "null");
            }
        } catch (Exception e) {
            log.error("[RuleEngine] gRPC 通道调用 AdminGrpcService.getPointAddressMappings 异常: {}", e.getMessage(), e);
        }

        return list != null ? list : Collections.emptyList();
    }

    /**
     * 规则核心评估入口 (由 DataMqttConsumer 在数据消费时同步调用，耗时微秒级)
     */
    public boolean evaluateRule(PointData data) {
        if (data == null || data.getPointCode() == null) {
            return false;
        }

        String pointCode = data.getPointCode().trim().toLowerCase();
        String deviceId = data.getDeviceId() != null ? data.getDeviceId().trim() : "";

        // 1. O(1) 倒排索引快速检索目标规则列表
        List<Rule> matchedRules = new ArrayList<>();

        // A. 优先检索精确匹配设备+测点的规则 "DEV:{deviceId}:{pointCode}"
        if (!deviceId.isEmpty()) {
            String devKey = "DEV:" + deviceId.toLowerCase() + ":" + pointCode;
            List<Rule> devRules = ruleIndex.get(devKey);
            if (devRules != null && !devRules.isEmpty()) {
                matchedRules.addAll(devRules);
            }
        }

        // B. 检索通用测点规则 "POINT:{pointCode}"
        String pointKey = "POINT:" + pointCode;
        List<Rule> pointRules = ruleIndex.get(pointKey);
        if (pointRules != null && !pointRules.isEmpty()) {
            matchedRules.addAll(pointRules);
        }

        // 若当前测点未绑定任何规则，O(1) 立即返回，完全零开销
        if (matchedRules.isEmpty()) {
            return false;
        }

        boolean anyTriggered = false;
        long now = System.currentTimeMillis();

        for (Rule rule : matchedRules) {
            if (Boolean.FALSE.equals(rule.getEnabled())) {
                continue;
            }

            // 2. 内存计算判断是否满足阈值条件
            if (isConditionMatched(rule, data.getValue())) {
                anyTriggered = true;

                // 3. 工业防抖抑制判断 (避免现场高频振荡引发指令与告警风暴)
                String triggerKey = rule.getRuleId() + ":" + deviceId;
                Long lastTrigger = lastTriggerTimeMap.get(triggerKey);
                if (lastTrigger != null && (now - lastTrigger) < DEFAULT_COOLDOWN_MS) {
                    log.info("[RuleEngine] Rule [{}] triggered for device [{}] but suppressed by cooldown window ({}ms remaining)",
                            rule.getRuleId(), deviceId, DEFAULT_COOLDOWN_MS - (now - lastTrigger));
                    continue;
                }

                // 更新触发时间
                lastTriggerTimeMap.put(triggerKey, now);

                // 4. 异步提交动作执行池，彻底解耦数采主线程
                actionExecutor.submit(() -> executeRuleActions(rule, data));
            }
        }

        return anyTriggered;
    }

    /**
     * 规则条件匹配判断 (支持数值比较、区间内外判定)
     */
    private boolean isConditionMatched(Rule rule, Double val) {
        if (val == null) return false;

        String mode = rule.getConditionMode() != null ? rule.getConditionMode().toUpperCase() : "SINGLE";
        String op = rule.getOperator() != null ? rule.getOperator().trim() : ">";
        Double threshold = rule.getThresholdValue();

        switch (mode) {
            case "RANGE_OUTSIDE": // 超出安全区间报警 (如 val < min || val > max)
                Double minOut = rule.getMinVal();
                Double maxOut = rule.getMaxVal();
                if (minOut != null && val < minOut) return true;
                if (maxOut != null && val > maxOut) return true;
                return false;

            case "RANGE_INSIDE": // 处于告警区间内 (如 val >= min && val <= max)
                Double minIn = rule.getMinVal();
                Double maxIn = rule.getMaxVal();
                if (minIn != null && maxIn != null) {
                    return val >= minIn && val <= maxIn;
                }
                return false;

            case "SINGLE":
            default:
                if (threshold == null) return false;
                switch (op) {
                    case ">": return val > threshold;
                    case ">=": return val >= threshold;
                    case "<": return val < threshold;
                    case "<=": return val <= threshold;
                    case "==":
                    case "=": return Math.abs(val - threshold) < 0.0001;
                    case "!=": return Math.abs(val - threshold) >= 0.0001;
                    default: return val > threshold;
                }
        }
    }

    /**
     * 异步分发执行规则关联的联动动作 (包含告警、服务反控、系统日志)
     */
    private void executeRuleActions(Rule rule, PointData data) {
        try {
            log.warn("[RuleEngine HIT] 规则触发! 规则: [{} - {}], 设备: {}, 测点: {}, 当前数值: {}",
                    rule.getRuleId(), rule.getRuleName(), data.getDeviceId(), data.getPointCode(), data.getValue());

            List<RuleAction> actions = rule.getActions();
            if (actions == null || actions.isEmpty()) {
                log.warn("[RuleEngine] Rule [{}] has no actions configured.", rule.getRuleId());
                return;
            }

            for (RuleAction act : actions) {
                if (Boolean.FALSE.equals(act.getEnabled())) continue;

                String type = act.getActionType() != null ? act.getActionType().toUpperCase() : "ALARM";

                if ("ALARM".equals(type)) {
                    String level = act.getAlarmLevel() != null ? act.getAlarmLevel() : "WARN";
                    String title = (act.getAlarmContent() != null && !act.getAlarmContent().trim().isEmpty())
                            ? act.getAlarmContent().trim()
                            : rule.getRuleName();

                    AlarmRecord alarm = new AlarmRecord();
                    alarm.setAlarmId("ALM_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
                    alarm.setRuleId(rule.getRuleId());
                    alarm.setDeviceId(data.getDeviceId());
                    alarm.setMetric(data.getPointCode());
                    alarm.setAlarmLevel(level);
                    alarm.setAlarmTitle(title);
                    alarm.setTriggerValue(String.valueOf(data.getValue()));
                    alarm.setThresholdDesc((rule.getOperator() != null ? rule.getOperator() : "") + " " + rule.getThresholdValue());
                    alarm.setStatus(0);
                    alarm.setAlarmTime(new Date());
                    alarm.setRemark("自动化规则引擎触发: " + rule.getRuleName());

                    try {
                        GrpcModels.RpcResponse resp = GrpcClient.call(adminGrpcHost, adminGrpcPort, "AdminGrpcService", "createAlarm", alarm);
                        if (resp != null && resp.isSuccess()) {
                            log.info("[RuleAction:ALARM] 告警已生成并成功落库: alarmId={}, level={}, title='{}'",
                                    alarm.getAlarmId(), level, title);
                        } else {
                            log.warn("[RuleAction:ALARM] 告警生成落库失败: {}", resp != null ? resp.getMessage() : "null");
                        }
                    } catch (Exception err) {
                        log.error("[RuleAction:ALARM] 告警通过 gRPC 写入异常: {}", err.getMessage());
                    }

                } else if ("SERVICE".equals(type)) {
                    // 核心亮点：反控服务指令下发前物理转换 (由数据中心统一翻译测点物理地址与通信要素，驱动无脑执行)
                    String targetDev = (act.getTargetDeviceId() != null && !act.getTargetDeviceId().trim().isEmpty())
                            ? act.getTargetDeviceId().trim() : data.getDeviceId();
                    String svcCode = act.getTargetServiceCode();
                    Map<String, Object> finalParams = parseAndMergeServiceArgs(act.getServiceInputValues());

                    DeviceConfig devConfig = getOrFetchDevice(targetDev);
                    String driverCode = (devConfig != null && devConfig.getDriverCode() != null)
                            ? devConfig.getDriverCode().trim() : deviceDriverMap.getOrDefault(targetDev, "modbus-tcp");
                    String host = (devConfig != null && devConfig.getHost() != null) ? devConfig.getHost().trim() : "127.0.0.1";
                    int port = (devConfig != null && devConfig.getPort() > 0) ? devConfig.getPort() : 502;
                    Map<String, Object> connParams = (devConfig != null && devConfig.getConnectionParams() != null)
                            ? devConfig.getConnectionParams() : new HashMap<>();
                    Object slaveId = connParams.getOrDefault("slaveId", 1);
                    Object byteOrder = connParams.getOrDefault("byteOrder", "CDAB");

                    List<DevicePointMapping> mappings = getOrFetchMappings(targetDev);

                    log.info("[RuleAction:SERVICE] 联动反控服务触发: 目标设备={}, 服务方法={}, 最终下发入参={}, 物理目标={}:{}, driverCode={}, 点位映射表数量={}",
                            targetDev, svcCode, JSON.toJSONString(finalParams), host, port, driverCode, mappings.size());

                    if (finalParams.isEmpty()) {
                        log.warn("[RuleAction:SERVICE] 设备 [{}] 服务 [{}] 入参为空，忽略下发", targetDev, svcCode);
                        continue;
                    }

                    // 严禁盲目兜底或猜测寄存器地址，工业现场安全第一
                    // 逐项转换物理测点反控指令
                    List<JSONObject> resolvedPoints = new ArrayList<>();
                    boolean hasUnmappedParam = false;
                    String unmappedKey = null;

                    for (Map.Entry<String, Object> entry : finalParams.entrySet()) {
                        String pKey = entry.getKey();
                        Object pVal = entry.getValue();
                        if (pVal == null) continue;

                        String matchedPointCode = null;
                        String physicalAddress = null;
                        String dataType = "INT";

                        // 严格根据入参键名与物理测点映射表进行匹配 (忽略英文大小写，例如 temperture、model、p3、p4/P4)
                        for (DevicePointMapping m : mappings) {
                            if (m != null && m.getPointCode() != null && pKey.equalsIgnoreCase(m.getPointCode().trim())) {
                                matchedPointCode = m.getPointCode().trim();
                                if (m.getPhysicalAddress() != null && !m.getPhysicalAddress().trim().isEmpty()) {
                                    physicalAddress = m.getPhysicalAddress().trim();
                                }
                                if (m.getDataType() != null && !m.getDataType().trim().isEmpty()) {
                                    dataType = m.getDataType().trim();
                                }
                                break;
                            }
                        }

                        // 工业安全底线：若未配置物理寄存器地址，严禁猜测，严禁兜底默认地址！
                        if (physicalAddress == null || physicalAddress.isEmpty()) {
                            hasUnmappedParam = true;
                            unmappedKey = pKey;
                            break;
                        }

                        JSONObject rp = new JSONObject();
                        rp.put("pointCode", matchedPointCode);
                        rp.put("address", physicalAddress);
                        rp.put("value", pVal);
                        rp.put("dataType", dataType);
                        rp.put("byteOrder", byteOrder != null ? byteOrder.toString() : "CDAB");
                        rp.put("slaveId", slaveId);
                        rp.put("host", host);
                        rp.put("port", port);
                        resolvedPoints.add(rp);
                    }

                    // 工业安全防线：若有任何参数未在数据库配置物理寄存器地址，直接阻断拦截，绝对不向 MQTT 发送未翻译的盲目指令
                    if (hasUnmappedParam || resolvedPoints.isEmpty() || resolvedPoints.size() < finalParams.size()) {
                        String blockReason = String.format("设备 [%s] 服务入参 [%s] 未配置底层物理寄存器地址！根据工业安全规范严禁兜底下发，本次反控联动已阻断拦截！",
                                targetDev, unmappedKey != null ? unmappedKey : "未知参数");
                        log.error("[RuleAction:SERVICE] 拒绝下发: {}", blockReason);

                        // 记录联动反控拦截安全审计日志至系统日志表 (iot_log)
                        try {
                            com.unihh.iot.common.model.IotLog rejectLog = new com.unihh.iot.common.model.IotLog();
                            rejectLog.setLogId("LOG_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
                            rejectLog.setLogType("COMMAND");
                            rejectLog.setDeviceId(targetDev);
                            rejectLog.setModelKey(devConfig != null ? devConfig.getModelKey() : rule.getModelKey());
                            rejectLog.setLevel("ERROR");
                            rejectLog.setContent(String.format("规则联动反控拒绝下发: 规则 [%s - %s], 动作 [%s], 目标设备 [%s], 服务 [%s], 阻断原因: %s",
                                    rule.getRuleId(), rule.getRuleName(), act.getActionName() != null ? act.getActionName() : "反控服务",
                                    targetDev, svcCode, blockReason));
                            rejectLog.setOperator("RULE_ENGINE");
                            rejectLog.setCreateTime(new Date());
                            GrpcClient.call(adminGrpcHost, adminGrpcPort, "AdminGrpcService", "recordLog", rejectLog);
                        } catch (Exception logErr) {
                            log.warn("[RuleAction:SERVICE] 记录反控拦截审计日志异常: {}", logErr.getMessage());
                        }

                        continue;
                    }

                    if (mqttClient != null && mqttClient.isConnected()) {
                        String baseMsgId = "rule_srv_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 6);

                        // 1. 逐项转换物理测点反控指令 (WRITE_POINT) 并下发至驱动指令主题
                        for (JSONObject rp : resolvedPoints) {
                            String ptCode = rp.getString("pointCode");
                            String ptAddr = rp.getString("address");
                            Object ptVal = rp.get("value");
                            String ptType = rp.getString("dataType");
                            String ptBo = rp.getString("byteOrder");
                            if (ptBo == null || ptBo.trim().isEmpty()) {
                                ptBo = byteOrder != null ? byteOrder.toString() : "CDAB";
                            }

                            JSONObject pointCmd = new JSONObject();
                            pointCmd.put("messageId", baseMsgId + "_" + ptCode);
                            pointCmd.put("action", "WRITE_POINT");
                            pointCmd.put("cmdType", "WRITE");
                            pointCmd.put("deviceId", targetDev);
                            pointCmd.put("driverCode", driverCode);
                            pointCmd.put("pointCode", ptCode);
                            pointCmd.put("address", ptAddr);
                            pointCmd.put("value", ptVal);
                            pointCmd.put("dataType", ptType);
                            pointCmd.put("host", host);
                            pointCmd.put("port", port);
                            pointCmd.put("slaveId", slaveId);
                            pointCmd.put("byteOrder", ptBo);
                            pointCmd.put("connectionParams", connParams);
                            pointCmd.put("timestamp", System.currentTimeMillis());

                            mqttClient.publish("iot/device/control", pointCmd.toJSONString(), 1);
                            mqttClient.publish("iot/driver/" + driverCode + "/cmd", pointCmd.toJSONString(), 1);

                            log.info("[RuleAction:SERVICE] 成功完成南向转换并下发物理写入指令: pointCode={}, address={}, val={}, type={}, byteOrder={}, host={}:{}",
                                    ptCode, ptAddr, ptVal, ptType, ptBo, host, port);
                        }

                        // 2. 同时发布复合服务调用消息至控制总线与驱动指令专属主题 (附带已解析物理点位 resolvedPoints 及字节序)
                        JSONObject serviceMsg = new JSONObject();
                        serviceMsg.put("messageId", baseMsgId);
                        serviceMsg.put("action", "INVOKE_SERVICE");
                        serviceMsg.put("cmdType", "SERVICE");
                        serviceMsg.put("deviceId", targetDev);
                        serviceMsg.put("driverCode", driverCode);
                        serviceMsg.put("serviceCode", svcCode);
                        serviceMsg.put("params", finalParams);
                        serviceMsg.put("resolvedPoints", resolvedPoints);
                        serviceMsg.put("host", host);
                        serviceMsg.put("port", port);
                        serviceMsg.put("slaveId", slaveId);
                        serviceMsg.put("byteOrder", byteOrder != null ? byteOrder.toString() : "CDAB");
                        serviceMsg.put("connectionParams", connParams);
                        serviceMsg.put("timestamp", System.currentTimeMillis());

                        mqttClient.publish("iot/device/control", serviceMsg.toJSONString(), 1);
                        mqttClient.publish("iot/driver/" + driverCode + "/cmd", serviceMsg.toJSONString(), 1);

                        log.info("[RuleAction:SERVICE] 成功完成南向转换并发布反控指令: targetDev={}, driverCode={}, byteOrder={}, 成功转换并下发物理点位数={}",
                                targetDev, driverCode, byteOrder, resolvedPoints.size());

                        // 3. 记录联动反控指令审计日志至系统日志表 (iot_log)
                        try {
                            StringBuilder ptsDesc = new StringBuilder();
                            for (int i = 0; i < resolvedPoints.size(); i++) {
                                JSONObject rp = resolvedPoints.get(i);
                                if (i > 0) ptsDesc.append(", ");
                                ptsDesc.append(rp.getString("pointCode"))
                                       .append("=").append(rp.get("value"))
                                       .append(" (").append(rp.getString("address"))
                                       .append(", ").append(rp.getString("dataType"))
                                       .append(", ").append(rp.getString("byteOrder")).append(")");
                            }

                            com.unihh.iot.common.model.IotLog cmdLog = new com.unihh.iot.common.model.IotLog();
                            cmdLog.setLogId("LOG_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
                            cmdLog.setLogType("COMMAND");
                            cmdLog.setDeviceId(targetDev);
                            cmdLog.setModelKey(devConfig != null ? devConfig.getModelKey() : rule.getModelKey());
                            cmdLog.setLevel("INFO");
                            cmdLog.setContent(String.format("规则联动控制指令下发成功: 规则 [%s - %s], 动作 [%s], 目标设备 [%s], 驱动 [%s (%s:%s)], 服务 [%s], 物理下发点位: [%s]",
                                    rule.getRuleId(), rule.getRuleName(),
                                    act.getActionName() != null ? act.getActionName() : "反控服务",
                                    targetDev, driverCode, host, port, svcCode, ptsDesc.toString()));
                            cmdLog.setOperator("RULE_ENGINE");
                            cmdLog.setCreateTime(new Date());
                            GrpcClient.call(adminGrpcHost, adminGrpcPort, "AdminGrpcService", "recordLog", cmdLog);
                            log.info("[RuleAction:SERVICE] 成功记录反控指令审计日志至系统日志表 (iot_log): logId={}", cmdLog.getLogId());
                        } catch (Exception logErr) {
                            log.warn("[RuleAction:SERVICE] 记录反控指令审计日志异常: {}", logErr.getMessage());
                        }
                    } else {
                        String errMsg = String.format("规则联动反控发布失败: 规则 [%s - %s], 目标设备 [%s], 服务 [%s], 原因: MQTT 客户端尚未就绪或未连接",
                                rule.getRuleId(), rule.getRuleName(), targetDev, svcCode);
                        log.warn("[RuleAction:SERVICE] {}", errMsg);

                        try {
                            com.unihh.iot.common.model.IotLog failLog = new com.unihh.iot.common.model.IotLog();
                            failLog.setLogId("LOG_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
                            failLog.setLogType("COMMAND");
                            failLog.setDeviceId(targetDev);
                            failLog.setModelKey(devConfig != null ? devConfig.getModelKey() : rule.getModelKey());
                            failLog.setLevel("ERROR");
                            failLog.setContent(errMsg);
                            failLog.setOperator("RULE_ENGINE");
                            failLog.setCreateTime(new Date());
                            GrpcClient.call(adminGrpcHost, adminGrpcPort, "AdminGrpcService", "recordLog", failLog);
                        } catch (Exception ignored) {}
                    }

                } else if ("LOG".equals(type)) {
                    String logContent = act.getLogContent() != null ? act.getLogContent() : rule.getDescription();
                    String logLevel = act.getLogLevel() != null ? act.getLogLevel() : "INFO";
                    log.info("[RuleAction:LOG] 自动化日志记录: 等级={}, 详情='{}'", logLevel, logContent);

                    try {
                        com.unihh.iot.common.model.IotLog iotLog = new com.unihh.iot.common.model.IotLog();
                        iotLog.setLogId("LOG_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
                        iotLog.setLogType("RULE");
                        iotLog.setDeviceId(data.getDeviceId());
                        iotLog.setModelKey(rule.getModelKey());
                        iotLog.setLevel(logLevel);
                        iotLog.setContent(logContent);
                        iotLog.setOperator("RULE_ENGINE");
                        iotLog.setCreateTime(new Date());
                        GrpcClient.call(adminGrpcHost, adminGrpcPort, "AdminGrpcService", "recordLog", iotLog);
                    } catch (Exception err) {
                        log.warn("[RuleAction:LOG] 记录规则日志失败: {}", err.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("[RuleEngine] Error executing actions for rule [{}]: {}", rule.getRuleId(), e.getMessage(), e);
        }
    }

    /**
     * 解析并合并服务输入参数
     */
    private Map<String, Object> parseAndMergeServiceArgs(String inputValuesStr) {
        Map<String, Object> params = new HashMap<>();
        if (inputValuesStr != null && !inputValuesStr.trim().isEmpty()) {
            try {
                JSONObject json = JSON.parseObject(inputValuesStr);
                if (json != null) {
                    params.putAll(json);
                }
            } catch (Exception ignored) {
            }
        }
        return params;
    }

    /**
     * 动态注册或更新单条规则至哈希索引 (支持管理端动态热更新)
     */
    public void registerRule(Rule rule) {
        if (rule == null || rule.getSourcePoint() == null) return;

        String point = rule.getSourcePoint().trim().toLowerCase();
        String devId = rule.getSourceDeviceId() != null ? rule.getSourceDeviceId().trim().toLowerCase() : "";

        String key = devId.isEmpty() ? ("POINT:" + point) : ("DEV:" + devId + ":" + point);

        ruleIndex.compute(key, (k, list) -> {
            if (list == null) list = new CopyOnWriteArrayList<>();
            // 避免重复添加同一 ruleId
            list.removeIf(r -> r.getRuleId() != null && r.getRuleId().equals(rule.getRuleId()));
            list.add(rule);
            return list;
        });

        log.info("[RuleEngine] Rule registered into index key [{}]: {} ({})", key, rule.getRuleId(), rule.getRuleName());
    }

    /**
     * 全量刷新规则缓存索引
     */
    public void refreshRules(List<Rule> rules) {
        ruleIndex.clear();
        if (rules != null) {
            for (Rule r : rules) {
                registerRule(r);
            }
        }
        log.info("[RuleEngine] Reloaded all rules into memory. Total index slots: {}", ruleIndex.size());
    }

    /**
     * 初始化加载种子规则
     */
    private void loadBuiltinSeedRules() {
        Rule r1 = new Rule();
        r1.setRuleId("RULE_101");
        r1.setRuleName("电机主轴承过热防护规则");
        r1.setSourceDeviceId("DEV_001");
        r1.setSourcePoint("temperature");
        r1.setConditionMode("SINGLE");
        r1.setOperator(">");
        r1.setThresholdValue(75.0);
        r1.setEnabled(true);

        RuleAction a1 = new RuleAction();
        a1.setActionId("ACT_101");
        a1.setActionType("ALARM");
        a1.setAlarmLevel("CRITICAL");
        a1.setAlarmContent("1号电机主轴承温度超过75℃安全警戒线，已启动自动防护！");
        a1.setEnabled(true);

        RuleAction a2 = new RuleAction();
        a2.setActionId("ACT_102");
        a2.setActionType("SERVICE");
        a2.setTargetDeviceId("DEV_001");
        a2.setTargetServiceCode("startCoolingFan");
        a2.setServiceInputValues("{\"speed\":2800,\"mode\":\"AUTO\"}");
        a2.setEnabled(true);

        r1.setActions(Arrays.asList(a1, a2));
        registerRule(r1);

        // 种子规则 2: 储罐压力范围异常规则
        Rule r2 = new Rule();
        r2.setRuleId("RULE_102");
        r2.setRuleName("2号储罐压力安全区间防护");
        r2.setSourceDeviceId("DEV_002");
        r2.setSourcePoint("pressure");
        r2.setConditionMode("RANGE_OUTSIDE");
        r2.setMinVal(10.0);
        r2.setMaxVal(100.0);
        r2.setEnabled(true);

        RuleAction a3 = new RuleAction();
        a3.setActionId("ACT_103");
        a3.setActionType("ALARM");
        a3.setAlarmLevel("WARN");
        a3.setAlarmContent("2号储罐压力偏离安全作业区间(10~100kPa)，请立即核查进出阀状态！");
        a3.setEnabled(true);

        r2.setActions(Collections.singletonList(a3));
        registerRule(r2);
    }

    @PreDestroy
    public void shutdown() {
        syncScheduler.shutdown();
        actionExecutor.shutdown();
        try {
            if (!actionExecutor.awaitTermination(3, TimeUnit.SECONDS)) {
                actionExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            actionExecutor.shutdownNow();
        }
    }
}
