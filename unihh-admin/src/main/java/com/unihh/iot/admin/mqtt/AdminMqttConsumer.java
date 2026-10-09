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

package com.unihh.iot.admin.mqtt;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.unihh.iot.admin.dao.DeviceDao;
import com.unihh.iot.admin.service.DeviceService;
import com.unihh.iot.admin.service.DriverService;
import com.unihh.iot.common.model.DeviceConfig;
import com.unihh.iot.common.model.DevicePointMapping;
import com.unihh.iot.common.model.DriverInfo;
import com.unihh.iot.common.model.ThingModel;
import com.unihh.iot.common.mqtt.MqttBridgeClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;

/**
 * 管理中心 EMQX MQTT 消息总线订阅消费者
 * 负责接收南向协议驱动的在线注册 (iot/driver/register)、心跳保持 (iot/driver/heartbeat) 与状态变更 (iot/driver/+/status)
 * 并在驱动注册成功后，主动将已配置的设备连接属性与点位属性全量下发同步给该驱动
 */
@Component
public class AdminMqttConsumer {
    private static final Logger log = LoggerFactory.getLogger(AdminMqttConsumer.class);

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

    @Autowired
    private DriverService driverService;

    @Autowired
    @Lazy
    private DeviceService deviceService;

    @Autowired
    private DeviceDao deviceDao;

    @Autowired
    private com.unihh.iot.admin.dao.DriverInfoDao driverInfoDao;

    @Autowired
    @Lazy
    private AdminMqttPublisher adminMqttPublisher;

    private MqttBridgeClient mqttClient;
    private final java.util.Map<String, Long> lastHeartbeatMap = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ScheduledExecutorService watchdogScheduler = java.util.concurrent.Executors.newSingleThreadScheduledExecutor();

    public boolean isDriverAlive(String driverCode) {
        if (driverCode == null) return false;
        Long last = lastHeartbeatMap.get(driverCode);
        return last != null && (System.currentTimeMillis() - last <= 15000);
    }

    public void recordHeartbeat(String driverCode) {
        if (driverCode != null) {
            lastHeartbeatMap.put(driverCode, System.currentTimeMillis());
        }
    }

    @PostConstruct
    public void init() {
        // 程序启动时首先重置所有驱动和设备在数据库中的运行状态为离线 (0)
        // 随后由 EMQX MQTT 保留消息 (Retained LWT) 或实时心跳动态对齐在线状态
        try {
            driverInfoDao.updateAllStatus(0);
            deviceDao.updateAllStatus(0);
            log.info("[AdminMqttConsumer] Initialized all driver and device statuses to OFFLINE (0) on startup.");
        } catch (Exception e) {
            log.warn("[AdminMqttConsumer] Startup status reset notice: {}", e.getMessage());
        }

        // 启动心跳超时看门狗巡检 (每 5 秒检测一次，超过 15 秒无心跳则判定离线)
        watchdogScheduler.scheduleAtFixedRate(this::checkHeartbeatTimeout, 10, 5, java.util.concurrent.TimeUnit.SECONDS);

        new Thread(() -> {
            try {
                Thread.sleep(1500);
                String clientId = "admin_consumer_" + System.currentTimeMillis();
                mqttClient = new MqttBridgeClient(emqxServerUri, clientId, username, password,
                        cleanSession, keepAliveInterval, connectionTimeout, automaticReconnect, qos);
                mqttClient.connect();

                // 1. 订阅驱动注册主题
                mqttClient.subscribe("iot/driver/register", qos, (topic, message) -> {
                    String payload = new String(message.getPayload());
                    log.info("[AdminMqttConsumer] Received driver registration via MQTT: {}", payload);
                    handleDriverRegistration(payload);
                });

                // 2. 订阅驱动心跳主题
                mqttClient.subscribe("iot/driver/heartbeat", qos, (topic, message) -> {
                    String payload = new String(message.getPayload());
                    handleDriverHeartbeat(payload);
                });

                // 3. 订阅各驱动状态主题 (包含遗嘱消息 LWT 与在线保留消息)
                mqttClient.subscribe("iot/driver/+/status", qos, (topic, message) -> {
                    String payload = new String(message.getPayload());
                    handleDriverStatusChange(topic, payload);
                });

                // 4. 订阅设备物理通信在线/离线状态变更主题 (iot/device/status, iot/device/+/status)
                mqttClient.subscribe("iot/device/status", qos, (topic, message) -> {
                    String payload = new String(message.getPayload());
                    handleDeviceStatusChange(payload);
                });
                mqttClient.subscribe("iot/device/+/status", qos, (topic, message) -> {
                    String payload = new String(message.getPayload());
                    handleDeviceStatusChange(payload);
                });

                log.info("[AdminMqttConsumer] Subscribed to EMQX MQTT topics for driver register, heartbeat, driver status and device status.");
            } catch (Exception e) {
                log.warn("[AdminMqttConsumer] EMQX MQTT connection notice: {}", e.getMessage());
            }
        }).start();
    }

    private void checkHeartbeatTimeout() {
        try {
            List<DriverInfo> drivers = driverInfoDao.findAll();
            if (drivers == null || drivers.isEmpty()) return;
            long now = System.currentTimeMillis();
            for (DriverInfo driver : drivers) {
                if (driver.getStatus() != null && driver.getStatus() == 1) {
                    Long last = lastHeartbeatMap.get(driver.getDriverCode());
                    if (last == null || (now - last > 15000)) {
                        log.warn("[AdminMqttConsumer] Driver [{}] heartbeat timeout (>15s, last={}). Automatically transitioned to OFFLINE.",
                                driver.getDriverCode(), last);
                        driverService.updateStatus(driver.getDriverCode(), 0);
                        int cascaded = deviceDao.updateStatusByDriverCode(driver.getDriverCode(), 0);
                        lastHeartbeatMap.remove(driver.getDriverCode());
                        log.info("[AdminMqttConsumer] Cascaded [{}] device(s) to OFFLINE due to driver [{}] heartbeat timeout.",
                                cascaded, driver.getDriverCode());
                    }
                }
            }
        } catch (Exception e) {
            log.error("[AdminMqttConsumer] Error in heartbeat watchdog: {}", e.getMessage());
        }
    }

    private void handleDriverRegistration(String payload) {
        try {
            DriverInfo driverInfo = JSON.parseObject(payload, DriverInfo.class);
            if (driverInfo == null || driverInfo.getDriverCode() == null || driverInfo.getDriverCode().trim().isEmpty()) {
                log.warn("[AdminMqttConsumer] Discard invalid driver register payload: {}", payload);
                return;
            }
            if (driverInfo.getStatus() == null) {
                driverInfo.setStatus(1);
            }

            boolean ok = driverService.addDriver(driverInfo);
            driverService.updateStatus(driverInfo.getDriverCode(), 1);
            recordHeartbeat(driverInfo.getDriverCode());
            log.info("[AdminMqttConsumer] Successfully processed MQTT registration for driver [{}] (ONLINE): saved={}",
                    driverInfo.getDriverCode(), ok);

            // 驱动注册上线后，将平台上属于该驱动的所有已配置设备连接属性与点位属性同步发布给该驱动
            syncConfiguredDevicesToDriver(driverInfo.getDriverCode());
        } catch (Exception e) {
            log.error("[AdminMqttConsumer] Error handling driver registration from MQTT: {}", e.getMessage(), e);
        }
    }

    private void syncConfiguredDevicesToDriver(String driverCode) {
        try {
            List<DeviceConfig> devices = deviceDao.findAll(null, driverCode, null);
            if (devices == null || devices.isEmpty()) {
                log.info("[AdminMqttConsumer] No pre-configured devices found for driver [{}] to sync.", driverCode);
                return;
            }

            log.info("[AdminMqttConsumer] Found {} configured device(s) for driver [{}], syncing connection & points via MQTT...",
                    devices.size(), driverCode);

            for (DeviceConfig dev : devices) {
                List<DevicePointMapping> mappings = deviceService.getPointAddressMappings(dev.getDeviceId());
                List<ThingModel.PropertySpec> specs = convertMappingsToPropertySpecs(mappings);
                adminMqttPublisher.publishDeviceConfigUpdate("SYNC", dev, specs);
            }
        } catch (Exception e) {
            log.error("[AdminMqttConsumer] Error syncing devices to driver [{}]: {}", driverCode, e.getMessage(), e);
        }
    }

    private void handleDriverHeartbeat(String payload) {
        try {
            JSONObject json = JSON.parseObject(payload);
            String driverCode = json.getString("driverCode");
            Integer status = json.getInteger("status");
            if (driverCode != null) {
                recordHeartbeat(driverCode);
                driverService.updateStatus(driverCode, status != null ? status : 1);
            }
        } catch (Exception ignored) {}
    }

    private void handleDriverStatusChange(String topic, String payload) {
        try {
            JSONObject json = JSON.parseObject(payload);
            String driverCode = json.getString("driverCode");
            if (driverCode == null && topic.contains("/")) {
                String[] parts = topic.split("/");
                if (parts.length >= 3) {
                    driverCode = parts[2];
                }
            }
            Integer status = json.getInteger("status");
            String reason = json.getString("reason");
            if (driverCode != null && status != null) {
                if (status == 1) {
                    recordHeartbeat(driverCode);
                } else {
                    lastHeartbeatMap.remove(driverCode);
                }
                driverService.updateStatus(driverCode, status);
                log.info("[AdminMqttConsumer] Driver [{}] status updated to: {} ({}), reason: {}",
                        driverCode, status == 1 ? "ONLINE" : "OFFLINE", status, reason != null ? reason : "Event");

                // 驱动级联：当驱动变为离线 (无论正常退出还是 LWT 遗嘱触发)，其下挂载的所有设备批量联动标记为离线
                if (status == 0) {
                    int cascadedCount = deviceDao.updateStatusByDriverCode(driverCode, 0);
                    log.warn("[AdminMqttConsumer] Driver [{}] went OFFLINE. Cascaded [{}] device(s) to OFFLINE in database.",
                            driverCode, cascadedCount);
                }
            }
        } catch (Exception ignored) {}
    }

    private List<ThingModel.PropertySpec> convertMappingsToPropertySpecs(List<DevicePointMapping> mappings) {
        List<ThingModel.PropertySpec> specs = new ArrayList<>();
        if (mappings != null) {
            for (DevicePointMapping m : mappings) {
                ThingModel.PropertySpec spec = new ThingModel.PropertySpec(
                        m.getPointCode(),
                        m.getPointName(),
                        m.getDataType(),
                        m.getAccessMode(),
                        ""
                );
                spec.setAddress(m.getPhysicalAddress());
                specs.add(spec);
            }
        }
        return specs;
    }

    private void handleDeviceStatusChange(String payload) {
        try {
            JSONObject json = JSON.parseObject(payload);
            if (json == null) return;
            String deviceId = json.getString("deviceId");
            Integer status = json.getInteger("status");
            String reason = json.getString("reason");
            if (deviceId != null && !deviceId.trim().isEmpty() && status != null) {
                deviceDao.updateStatus(deviceId.trim(), status);
                log.info("[AdminMqttConsumer] Device [{}] physical channel status updated to [{}] ({}), reason: {}",
                        deviceId, status == 1 ? "ONLINE" : "OFFLINE", status, reason != null ? reason : "Normal");
            }
        } catch (Exception e) {
            log.error("[AdminMqttConsumer] Error handling device status change: {}", e.getMessage());
        }
    }

    @PreDestroy
    public void destroy() {
        try {
            watchdogScheduler.shutdownNow();
        } catch (Exception ignored) {}
        if (mqttClient != null) {
            mqttClient.disconnect();
        }
    }
}
