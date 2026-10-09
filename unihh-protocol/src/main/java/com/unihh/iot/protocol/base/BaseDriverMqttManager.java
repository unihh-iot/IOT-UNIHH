/*
 * Copyright (c) 2026 unihh.com All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Website: https://unihh.com
 * Author: deversun
 */

package com.unihh.iot.protocol.base;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.unihh.iot.common.model.DeviceConfig;
import com.unihh.iot.common.model.DriverInfo;
import com.unihh.iot.common.model.ThingModel;
import com.unihh.iot.common.mqtt.MqttBridgeClient;
import com.unihh.iot.protocol.constants.DriverActionConstants;
import com.unihh.iot.protocol.constants.MqttTopicConstants;
import com.unihh.iot.protocol.model.DriverMetadata;
import com.unihh.iot.protocol.model.ParsedPayload;
import com.unihh.iot.protocol.util.NetworkUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 通用南向驱动 EMQX MQTT 消息总线生命周期底座 (Base Driver MQTT Manager)
 * <p>
 * 自动化托管：
 * 1. MQTT 自动连接与断线重连；
 * 2. 启动自动向 iot/driver/register 广播注册驱动元数据与本地物理 IP 服务地址；
 * 3. 5s 定时向 iot/driver/heartbeat 上报驱动健康心跳；
 * 4. 配置 MQTT LWT 遗嘱消息 (iot/driver/{code}/status)，异常崩溃自动广播离线；
 * 5. 自动订阅 iot/device/config/update 全局配置与专属配置主题，驱动零代码热同步设备与测点；
 * 6. 自动订阅 iot/device/control 反控写入指令并回调驱动 writePoint 方法；
 * 7. 自动将周期采样数据标准化推送至 iot/driver/{code}/data 供数据中心消费。
 */
@Component
public class BaseDriverMqttManager implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(BaseDriverMqttManager.class);

    @Value("${emqx.server-uri:${EMQX_SERVER_URI:tcp://127.0.0.1:1883}}")
    private String emqxServerUri;

    @Value("${emqx.username:${EMQX_USERNAME:}}")
    private String username;

    @Value("${emqx.password:${EMQX_PASSWORD:}}")
    private String password;

    @Value("${emqx.clean-session:${EMQX_CLEAN_SESSION:true}}")
    private boolean cleanSession;

    @Value("${emqx.keep-alive-interval:${EMQX_KEEP_ALIVE:30}}")
    private int keepAliveInterval;

    @Value("${emqx.connection-timeout:${EMQX_CONNECT_TIMEOUT:10}}")
    private int connectionTimeout;

    @Value("${emqx.automatic-reconnect:${EMQX_AUTO_RECONNECT:true}}")
    private boolean automaticReconnect;

    @Value("${emqx.qos:${EMQX_QOS:1}}")
    private int qos;

    @Value("${server.port:28011}")
    private int serverPort;

    @Value("${driver.code:}")
    private String configuredDriverCode;

    @Value("${driver.name:}")
    private String configuredDriverName;

    @Autowired(required = false)
    private AbstractDriverPlugin driverPlugin;

    @Autowired
    private DriverPollingEngine pollingEngine;

    private MqttBridgeClient mqttClient;
    private final ScheduledExecutorService heartbeatScheduler = Executors.newSingleThreadScheduledExecutor();
    private final Map<String, Long> processedMessageIds = new ConcurrentHashMap<>();

    @Override
    public void run(String... args) {
        new Thread(this::initMqttLifecycle, "driver-mqtt-init-thread").start();
    }

    private void initMqttLifecycle() {
        try {
            Thread.sleep(1000);
            String driverCode = resolveDriverCode();
            String clientId = "drv_" + driverCode + "_" + System.currentTimeMillis();

            mqttClient = new MqttBridgeClient(emqxServerUri, clientId, username, password,
                    cleanSession, keepAliveInterval, connectionTimeout, automaticReconnect, qos);

            // 1. 设置 LWT 掉线遗嘱消息 (异常崩溃时 EMQX 自动广播，retained=true 保证新订阅者启动即感知)
            JSONObject lwt = new JSONObject();
            lwt.put("driverCode", driverCode);
            lwt.put("status", 0);
            lwt.put("reason", "LWT_ABNORMAL_DISCONNECT");
            lwt.put("timestamp", System.currentTimeMillis());
            String willTopic = MqttTopicConstants.buildDriverStatusTopic(driverCode);
            mqttClient.setWill(willTopic, lwt.toJSONString(), qos, true);

            mqttClient.connect();
            log.info("[BaseDriverMqttManager] Connected to EMQX MQTT bus for driver [{}] at {}", driverCode, emqxServerUri);

            // 发布驱动上线保留状态 (retained=true，供 Admin 启动或重连时即时对齐状态)
            JSONObject onlineStatus = new JSONObject();
            onlineStatus.put("driverCode", driverCode);
            onlineStatus.put("status", 1);
            onlineStatus.put("reason", "ONLINE");
            onlineStatus.put("timestamp", System.currentTimeMillis());
            mqttClient.publish(willTopic, onlineStatus.toJSONString(), qos, true);

            // 2. 向总线广播驱动上线注册包 (iot/driver/register)
            broadcastDriverRegistration(driverCode);

            // 3. 启动定时上报驱动心跳包 (每 5 秒一次)
            heartbeatScheduler.scheduleAtFixedRate(() -> sendHeartbeat(driverCode), 2000, 5000, TimeUnit.MILLISECONDS);

            // 4. 订阅设备配置变更及点位更新同步主题
            subscribeConfigTopics(driverCode);

            // 5. 订阅设备控制与下发反控指令主题
            subscribeControlTopics(driverCode);

        } catch (Exception e) {
            log.warn("[BaseDriverMqttManager] MQTT lifecycle initialization notice: {}", e.getMessage());
        }
    }

    /**
     * 广播驱动上线注册包
     */
    private void broadcastDriverRegistration(String driverCode) {
        try {
            String localIp = NetworkUtils.getLocalIpAddress();
            String serviceUrl = "http://" + localIp + ":" + serverPort;
            DriverMetadata metadata = (driverPlugin != null) ? driverPlugin.getMetadata() : null;

            DriverInfo info = new DriverInfo();
            info.setDriverCode(driverCode);
            info.setDriverName((metadata != null && metadata.getDriverName() != null)
                    ? metadata.getDriverName()
                    : (!configuredDriverName.isEmpty() ? configuredDriverName : driverCode));
            info.setServiceUrl(serviceUrl);
            info.setStatus(1);
            info.setDescription(!configuredDriverName.isEmpty() ? configuredDriverName : driverCode);
            if (metadata != null && metadata.getParams() != null) {
                info.setParams(metadata.getParams());
            }

            String regPayload = JSON.toJSONString(info);
            mqttClient.publish(MqttTopicConstants.TOPIC_DRIVER_REGISTER, regPayload, qos);
            log.info("[BaseDriverMqttManager] Published driver registration to [{}]: {}",
                    MqttTopicConstants.TOPIC_DRIVER_REGISTER, regPayload);
        } catch (Exception e) {
            log.error("[BaseDriverMqttManager] Failed to publish driver registration: {}", e.getMessage());
        }
    }

    /**
     * 发送定时健康心跳
     */
    private void sendHeartbeat(String driverCode) {
        try {
            if (mqttClient == null || !mqttClient.isConnected()) return;
            JSONObject hb = new JSONObject();
            hb.put("driverCode", driverCode);
            hb.put("status", 1);
            hb.put("activeDevices", pollingEngine.getActiveDeviceCount());
            hb.put("timestamp", System.currentTimeMillis());
            mqttClient.publish(MqttTopicConstants.TOPIC_DRIVER_HEARTBEAT, hb.toJSONString(), qos);
        } catch (Exception ignored) {}
    }

    /**
     * 订阅设备配置同步主题
     */
    private void subscribeConfigTopics(String driverCode) {
        try {
            // 全局设备配置主题
            mqttClient.subscribe(MqttTopicConstants.TOPIC_DEVICE_CONFIG_UPDATE, qos, (topic, message) -> {
                handleConfigMessage(new String(message.getPayload()), driverCode);
            });

            // 驱动专属配置主题
            String specificConfigTopic = MqttTopicConstants.buildDriverConfigTopic(driverCode);
            String specificDeviceTopic = MqttTopicConstants.buildDriverDeviceConfigTopic(driverCode);

            mqttClient.subscribe(specificConfigTopic, qos, (topic, message) -> {
                handleConfigMessage(new String(message.getPayload()), driverCode);
            });
            mqttClient.subscribe(specificDeviceTopic, qos, (topic, message) -> {
                handleConfigMessage(new String(message.getPayload()), driverCode);
            });

            log.info("[BaseDriverMqttManager] Subscribed to device config update topics: [{}], [{}], [{}]",
                    MqttTopicConstants.TOPIC_DEVICE_CONFIG_UPDATE, specificConfigTopic, specificDeviceTopic);
        } catch (Exception e) {
            log.error("[BaseDriverMqttManager] Failed to subscribe to config topics: {}", e.getMessage());
        }
    }

    /**
     * 解析处理设备配置同步消息
     */
    private void handleConfigMessage(String payloadStr, String myDriverCode) {
        try {
            JSONObject json = JSON.parseObject(payloadStr);
            if (json == null) return;

            String targetDriverCode = json.getString("driverCode");
            String deviceId = json.getString("deviceId");
            String action = json.getString("action");

            // 若显式指定了驱动代码，且非本驱动，则过滤忽略
            if (targetDriverCode != null && !targetDriverCode.trim().isEmpty()
                    && !targetDriverCode.equalsIgnoreCase(myDriverCode)) {
                return;
            }

            if (deviceId == null) return;

            log.info("[BaseDriverMqttManager] Handling device config: action={}, deviceId={}, driver={}",
                    action, deviceId, myDriverCode);

            if (DriverActionConstants.ACTION_DELETE.equalsIgnoreCase(action)) {
                pollingEngine.removeDevice(deviceId);
                return;
            }

            if (DriverActionConstants.ACTION_STOP.equalsIgnoreCase(action)) {
                pollingEngine.removeDevice(deviceId);
                return;
            }

            JSONObject configObj = json.getJSONObject("deviceConfig");
            DeviceConfig config = (configObj != null)
                    ? JSON.parseObject(configObj.toJSONString(), DeviceConfig.class)
                    : new DeviceConfig();

            if (config.getDeviceId() == null) config.setDeviceId(deviceId);
            if (config.getDriverCode() == null) config.setDriverCode(myDriverCode);
            if (json.containsKey("acqStatus")) config.setAcqStatus(json.getInteger("acqStatus"));
            if (json.containsKey("host")) config.setHost(json.getString("host"));
            if (json.containsKey("port")) config.setPort(json.getIntValue("port"));
            if (json.containsKey("modelKey")) config.setModelKey(json.getString("modelKey"));
            if (json.containsKey("baseIntervalMs")) config.setBaseIntervalMs(json.getInteger("baseIntervalMs"));

            if (config.getConnectionParams() == null) {
                config.setConnectionParams(new HashMap<>());
            }
            if (json.containsKey("connectionParams") && json.get("connectionParams") instanceof Map) {
                config.getConnectionParams().putAll((Map<String, Object>) json.get("connectionParams"));
            }
            if (json.containsKey("slaveId") && json.get("slaveId") != null) {
                config.getConnectionParams().put("slaveId", json.get("slaveId"));
            }
            if (json.containsKey("byteOrder") && json.get("byteOrder") != null) {
                config.getConnectionParams().put("byteOrder", json.get("byteOrder"));
            }

            List<ThingModel.PropertySpec> points = new ArrayList<>();
            if (json.containsKey("points") && json.get("points") != null) {
                points = JSON.parseArray(json.getString("points"), ThingModel.PropertySpec.class);
            }

            pollingEngine.addOrUpdateDevice(config, points);

        } catch (Exception e) {
            log.error("[BaseDriverMqttManager] Error handling device config update: {}", e.getMessage(), e);
        }
    }

    /**
     * 订阅反控指令与调试下发主题
     */
    private void subscribeControlTopics(String driverCode) {
        try {
            // 全局控制主题
            mqttClient.subscribe(MqttTopicConstants.TOPIC_DEVICE_CONTROL, qos, (topic, message) -> {
                handleControlMessage(new String(message.getPayload()), driverCode);
            });

            // 驱动专属指令主题 (iot/driver/{driverCode}/cmd)
            String cmdTopic = MqttTopicConstants.buildDriverCmdTopic(driverCode);
            mqttClient.subscribe(cmdTopic, qos, (topic, message) -> {
                handleControlMessage(new String(message.getPayload()), driverCode);
            });

            log.info("[BaseDriverMqttManager] Subscribed to device control topics: [{}], [{}]",
                    MqttTopicConstants.TOPIC_DEVICE_CONTROL, cmdTopic);
        } catch (Exception e) {
            log.error("[BaseDriverMqttManager] Failed to subscribe to control topics: {}", e.getMessage());
        }
    }

    /**
     * 解析处理反控写入与服务调用指令
     */
    private void handleControlMessage(String payloadStr, String myDriverCode) {
        try {
            JSONObject json = JSON.parseObject(payloadStr);
            if (json == null) return;

            String targetDriverCode = json.getString("driverCode");
            if (targetDriverCode != null && !targetDriverCode.trim().isEmpty()
                    && !targetDriverCode.equalsIgnoreCase(myDriverCode)) {
                return;
            }

            // 消息幂等去重 (同一条指令同时推送至全局与专属主题时仅执行一次)
            String messageId = json.getString("messageId");
            if (messageId != null && !messageId.trim().isEmpty()) {
                long now = System.currentTimeMillis();
                Long prev = processedMessageIds.put(messageId, now);
                if (prev != null && (now - prev < 5000)) {
                    return;
                }
                if (processedMessageIds.size() > 1000) {
                    processedMessageIds.entrySet().removeIf(entry -> now - entry.getValue() > 30000);
                }
            }

            String action = json.getString("action");
            String cmdType = json.getString("cmdType");
            String deviceId = json.getString("deviceId");

            if (deviceId == null || deviceId.trim().isEmpty()) {
                return;
            }

            // 提取目标设备配置
            DeviceConfig config = pollingEngine.getDeviceConfig(deviceId);
            if (config == null) {
                config = new DeviceConfig();
                config.setDeviceId(deviceId);
                config.setDriverCode(myDriverCode);
                config.setHost(json.getString("host"));
                config.setPort(json.getIntValue("port"));
                if (json.containsKey("connectionParams")) {
                    config.setConnectionParams(json.getJSONObject("connectionParams"));
                }
            }
            if (config.getHost() == null || config.getHost().trim().isEmpty()) {
                if (json.containsKey("host")) {
                    config.setHost(json.getString("host"));
                } else {
                    config.setHost("127.0.0.1");
                }
            }
            if (config.getPort() <= 0) {
                if (json.containsKey("port") && json.getIntValue("port") > 0) {
                    config.setPort(json.getIntValue("port"));
                } else {
                    config.setPort(502);
                }
            }
            if (config.getConnectionParams() == null) {
                config.setConnectionParams(new HashMap<>());
            }
            if (json.containsKey("connectionParams") && json.get("connectionParams") instanceof Map) {
                config.getConnectionParams().putAll((Map<String, Object>) json.get("connectionParams"));
            }
            if (json.containsKey("slaveId") && json.get("slaveId") != null) {
                config.getConnectionParams().put("slaveId", json.get("slaveId"));
            }
            if (json.containsKey("byteOrder") && json.get("byteOrder") != null) {
                config.getConnectionParams().put("byteOrder", json.get("byteOrder"));
            }

            // 1. 点位写入指令
            if (DriverActionConstants.ACTION_WRITE_POINT.equalsIgnoreCase(action)
                    || "WRITE".equalsIgnoreCase(cmdType)
                    || "WRITE_POINT".equalsIgnoreCase(cmdType)) {

                String pointCode = json.getString("pointCode");
                String address = json.getString("address");
                Object value = json.get("value");
                String dataType = json.getString("dataType");

                // 自动从物模型点位缓存补全物理地址和数据类型
                ThingModel.PropertySpec spec = pollingEngine.findPoint(deviceId, pointCode);
                if (spec != null) {
                    if (address == null || address.trim().isEmpty()) {
                        address = spec.getAddress();
                    }
                    if (dataType == null || dataType.trim().isEmpty()) {
                        dataType = spec.getDataType();
                    }
                }

                log.info("[BaseDriverMqttManager] Executing writePoint: deviceId={}, pointCode={}, address={}, val={}, type={}",
                        deviceId, pointCode, address, value, dataType);

                if (driverPlugin != null) {
                    ReentrantLock lock = driverPlugin.getChannelLock(config);
                    lock.lock();
                    try {
                        boolean ok = driverPlugin.writePoint(config, pointCode, address, value, dataType);
                        log.info("[BaseDriverMqttManager] Write result for device [{}]: {}", deviceId, ok ? "SUCCESS" : "FAILED");
                        if (ok) {
                            publishDeviceStatus(deviceId, 1, "Control write success");
                        }
                    } finally {
                        lock.unlock();
                    }
                }
            }
            // 2. 服务调用指令 (规则联动 / 平台服务下发)
            else if (DriverActionConstants.ACTION_INVOKE_SERVICE.equalsIgnoreCase(action)
                    || "INVOKE_SERVICE".equalsIgnoreCase(action)
                    || "SERVICE".equalsIgnoreCase(action)
                    || "SERVICE".equalsIgnoreCase(cmdType)
                    || "INVOKE_SERVICE".equalsIgnoreCase(cmdType)) {

                String serviceCode = json.getString("serviceCode");
                Map<String, Object> params = new HashMap<>();
                if (json.containsKey("params") && json.get("params") != null) {
                    Object pObj = json.get("params");
                    if (pObj instanceof Map) {
                        params = (Map<String, Object>) pObj;
                    } else {
                        try {
                            params = JSON.parseObject(json.getString("params"));
                        } catch (Exception ignored) {}
                    }
                }
                if (json.containsKey("resolvedPoints") && json.get("resolvedPoints") != null) {
                    params.put("resolvedPoints", json.get("resolvedPoints"));
                }

                log.info("[BaseDriverMqttManager] Executing invokeService: deviceId={}, serviceCode={}, params={}",
                        deviceId, serviceCode, params);

                if (driverPlugin != null) {
                    ReentrantLock lock = driverPlugin.getChannelLock(config);
                    lock.lock();
                    try {
                        boolean ok = driverPlugin.invokeService(config, serviceCode, params);
                        log.info("[BaseDriverMqttManager] InvokeService result for device [{}]: {}", deviceId, ok ? "SUCCESS" : "FAILED");
                        if (ok) {
                            publishDeviceStatus(deviceId, 1, "Invoke service success");
                        }
                    } finally {
                        lock.unlock();
                    }
                }
            } else {
                log.info("[BaseDriverMqttManager] Ignored unhandled control action: action={}, cmdType={}", action, cmdType);
            }
        } catch (Exception e) {
            log.error("[BaseDriverMqttManager] Error handling control message: {}", e.getMessage(), e);
        }
    }

    /**
     * 向 MQTT 状态总线发布设备物理通道在线/离线状态变更 (iot/device/status 及 iot/device/{deviceId}/status)
     */
    public void publishDeviceStatus(String deviceId, int status, String reason) {
        if (mqttClient == null || !mqttClient.isConnected() || deviceId == null || deviceId.trim().isEmpty()) {
            return;
        }
        try {
            JSONObject json = new JSONObject();
            json.put("deviceId", deviceId.trim());
            json.put("status", status);
            json.put("reason", reason != null ? reason : "Normal");
            json.put("timestamp", System.currentTimeMillis());
            String payload = json.toJSONString();
            mqttClient.publish(MqttTopicConstants.TOPIC_DEVICE_STATUS, payload, qos);
            mqttClient.publish("iot/device/" + deviceId.trim() + "/status", payload, qos);
            log.info("[BaseDriverMqttManager] Published device [{}] status: {} ({}), reason: {}",
                    deviceId, status == 1 ? "ONLINE" : "OFFLINE", status, reason != null ? reason : "Normal");
        } catch (Exception e) {
            log.warn("[BaseDriverMqttManager] Failed to publish device status for [{}]: {}", deviceId, e.getMessage());
        }
    }

    /**
     * 将采集数据标准化推送至 MQTT 消息总线 (iot/driver/{driverCode}/data)
     */
    public void publishDataPayload(String deviceId, ParsedPayload payload) {
        if (mqttClient == null || !mqttClient.isConnected() || payload == null) return;
        try {
            String driverCode = resolveDriverCode();
            String topic = MqttTopicConstants.buildDriverDataTopic(driverCode);
            String jsonStr = JSON.toJSONString(payload);
            mqttClient.publish(topic, jsonStr, qos);
        } catch (Exception e) {
            log.warn("[BaseDriverMqttManager] Failed to publish data payload: {}", e.getMessage());
        }
    }

    public String resolveDriverCode() {
        if (driverPlugin != null && driverPlugin.getDriverCode() != null && !driverPlugin.getDriverCode().isEmpty()) {
            return driverPlugin.getDriverCode();
        }
        if (configuredDriverCode != null && !configuredDriverCode.trim().isEmpty()) {
            return configuredDriverCode.trim();
        }
        return "generic-driver";
    }

    @PreDestroy
    public void destroy() {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                String driverCode = resolveDriverCode();
                JSONObject offMsg = new JSONObject();
                offMsg.put("driverCode", driverCode);
                offMsg.put("status", 0);
                offMsg.put("reason", "NORMAL_SHUTDOWN");
                offMsg.put("timestamp", System.currentTimeMillis());
                mqttClient.publish(MqttTopicConstants.buildDriverStatusTopic(driverCode), offMsg.toJSONString(), qos, true);
                mqttClient.disconnect();
            }
            heartbeatScheduler.shutdownNow();
        } catch (Exception ignored) {}
    }
}
