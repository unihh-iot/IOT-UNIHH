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

import com.alibaba.fastjson2.JSONObject;
import com.unihh.iot.common.model.DeviceConfig;
import com.unihh.iot.common.model.ThingModel;
import com.unihh.iot.common.mqtt.MqttBridgeClient;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;

/**
 * 管理中心 EMQX MQTT 消息总线同步发布组件
 * 负责将设备创建、编辑修改、启停控制、删除以及物理点位映射更新同步发布至 EMQX 消息总线
 */
@Component
public class AdminMqttPublisher {
    private static final Logger log = LoggerFactory.getLogger(AdminMqttPublisher.class);

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

    private MqttBridgeClient mqttClient;

    @PostConstruct
    public void init() {
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                String clientId = "admin_publisher_" + System.currentTimeMillis();
                mqttClient = new MqttBridgeClient(emqxServerUri, clientId, username, password,
                        cleanSession, keepAliveInterval, connectionTimeout, automaticReconnect, qos);
                mqttClient.connect();
                log.info("[AdminMqttPublisher] Connected to EMQX MQTT message bus for device config and control sync.");
            } catch (Exception e) {
                log.warn("[AdminMqttPublisher] EMQX MQTT connection notice: {}", e.getMessage());
            }
        }).start();
    }

    public void publishDeviceConfigUpdate(String action, DeviceConfig device, List<ThingModel.PropertySpec> points) {
        if (mqttClient == null || !mqttClient.isConnected()) {
            log.warn("[AdminMqttPublisher] MQTT client not connected yet, skipping publish device config: deviceId={}",
                    device != null ? device.getDeviceId() : null);
            return;
        }
        try {
            JSONObject msg = new JSONObject();
            msg.put("action", action);
            msg.put("deviceId", device != null ? device.getDeviceId() : null);
            msg.put("driverCode", device != null ? device.getDriverCode() : null);
            msg.put("status", device != null ? device.getStatus() : 0);
            msg.put("acqStatus", device != null ? device.getAcqStatus() : null);
            msg.put("deviceConfig", device);
            msg.put("points", points);
            msg.put("timestamp", System.currentTimeMillis());

            // 显式提取南向驱动所需核心连接属性与点位属性
            if (device != null) {
                msg.put("host", device.getHost() != null ? device.getHost() : "127.0.0.1");
                msg.put("port", device.getPort() > 0 ? device.getPort() : 502);
                msg.put("connectionParams", device.getConnectionParams());
                if (device.getConnectionParams() != null) {
                    for (Map.Entry<String, Object> entry : device.getConnectionParams().entrySet()) {
                        if (!msg.containsKey(entry.getKey())) {
                            msg.put(entry.getKey(), entry.getValue());
                        }
                    }
                }
                msg.put("baseIntervalMs", device.getBaseIntervalMs() != null ? device.getBaseIntervalMs() : 1000);
                msg.put("modelKey", device.getModelKey());
                msg.put("deviceName", device.getDeviceName());
            }

            String payload = msg.toJSONString();

            // 1. 发布到全局设备配置更新主题
            mqttClient.publish("iot/device/config/update", payload, qos);

            // 2. 发布给具体绑定的协议驱动专属配置主题 (如 iot/driver/modbus-tcp/config)
            String driverCode = device != null ? device.getDriverCode() : null;
            if (driverCode != null && !driverCode.trim().isEmpty()) {
                String driverConfigTopic = "iot/driver/" + driverCode.trim() + "/config";
                String driverDeviceTopic = "iot/driver/" + driverCode.trim() + "/device/config";
                mqttClient.publish(driverConfigTopic, payload, qos);
                mqttClient.publish(driverDeviceTopic, payload, qos);
                log.info("[AdminMqttPublisher] Successfully published device [{}] connection attributes [{}:{}] (params={}) and {} points to driver MQTT topics [{}] & [{}]",
                        device.getDeviceId(), device.getHost(), device.getPort(), device.getConnectionParams(),
                        points != null ? points.size() : 0, driverConfigTopic, driverDeviceTopic);
            } else {
                log.info("[AdminMqttPublisher] Published device config update to MQTT topic [iot/device/config/update]: action={}, deviceId={}",
                        action, device != null ? device.getDeviceId() : null);
            }
        } catch (Exception e) {
            log.error("[AdminMqttPublisher] Failed to publish device config update via MQTT: {}", e.getMessage(), e);
        }
    }

    public void publishDeviceControl(String action, String deviceId, String driverCode, String pointCode, Object value) {
        publishDeviceControl(action, deviceId, driverCode, pointCode, value, null, null, null, 0, 1);
    }

    public void publishDeviceControl(String action, String deviceId, String driverCode, String pointCode, Object value, String address) {
        publishDeviceControl(action, deviceId, driverCode, pointCode, value, address, null, null, 0, 1, 1.0, 0.0);
    }

    public void publishDeviceControl(String action, String deviceId, String driverCode, String pointCode, Object value,
                                     String address, String dataType, String host, int port, int slaveId) {
        publishDeviceControl(action, deviceId, driverCode, pointCode, value, address, dataType, host, port, slaveId, 1.0, 0.0);
    }

    public void publishDeviceControl(String action, String deviceId, String driverCode, String pointCode, Object value,
                                     String address, String dataType, String host, int port, int slaveId, Double scale, Double offset) {
        publishDeviceControl(action, deviceId, driverCode, pointCode, value, address, dataType, host, port, slaveId, scale, offset, "CDAB");
    }

    public void publishDeviceControl(String action, String deviceId, String driverCode, String pointCode, Object value,
                                     String address, String dataType, String host, int port, int slaveId, Double scale, Double offset, String byteOrder) {
        if (mqttClient == null || !mqttClient.isConnected()) {
            return;
        }
        try {
            String msgId = "cmd_write_" + System.currentTimeMillis();
            JSONObject msg = new JSONObject();
            msg.put("messageId", msgId);
            msg.put("action", action);
            msg.put("deviceId", deviceId);
            msg.put("driverCode", driverCode);
            msg.put("pointCode", pointCode);
            msg.put("value", value);
            if (address != null && !address.trim().isEmpty()) {
                msg.put("address", address.trim());
            }
            if (dataType != null && !dataType.trim().isEmpty()) {
                msg.put("dataType", dataType.trim());
            }
            if (byteOrder != null && !byteOrder.trim().isEmpty()) {
                msg.put("byteOrder", byteOrder.trim());
            } else {
                msg.put("byteOrder", "CDAB");
            }
            if (host != null && !host.trim().isEmpty()) {
                msg.put("host", host.trim());
            }
            if (port > 0) {
                msg.put("port", port);
            }
            if (slaveId > 0) {
                msg.put("slaveId", slaveId);
            }
            if (scale != null) {
                msg.put("scale", scale);
            }
            if (offset != null) {
                msg.put("offset", offset);
            }
            msg.put("timestamp", System.currentTimeMillis());

            String payload = msg.toJSONString();
            mqttClient.publish("iot/device/control", payload, qos);

            // 同时推送到具体驱动指令主题 (使用相同的 messageId 供驱动端毫秒级幂等去重)
            if (driverCode != null && !driverCode.trim().isEmpty()) {
                JSONObject cmdMsg = new JSONObject();
                cmdMsg.put("messageId", msgId);
                cmdMsg.put("cmdType", "WRITE");
                cmdMsg.put("deviceId", deviceId);
                cmdMsg.put("pointCode", pointCode);
                cmdMsg.put("value", value);
                if (address != null && !address.trim().isEmpty()) cmdMsg.put("address", address.trim());
                if (dataType != null && !dataType.trim().isEmpty()) cmdMsg.put("dataType", dataType.trim());
                if (byteOrder != null && !byteOrder.trim().isEmpty()) {
                    cmdMsg.put("byteOrder", byteOrder.trim());
                } else {
                    cmdMsg.put("byteOrder", "CDAB");
                }
                if (host != null && !host.trim().isEmpty()) cmdMsg.put("host", host.trim());
                if (port > 0) cmdMsg.put("port", port);
                if (slaveId > 0) cmdMsg.put("slaveId", slaveId);
                if (scale != null) cmdMsg.put("scale", scale);
                if (offset != null) cmdMsg.put("offset", offset);
                mqttClient.publish("iot/driver/" + driverCode.trim() + "/cmd", cmdMsg.toJSONString(), qos);
            }

            log.info("[AdminMqttPublisher] Published device control to MQTT: action={}, deviceId={}, pointCode={}, val={}, addr={}, dataType={}, byteOrder={}, host={}:{}",
                    action, deviceId, pointCode, value, address, dataType, byteOrder, host, port);
        } catch (Exception e) {
            log.error("[AdminMqttPublisher] Failed to publish device control via MQTT: {}", e.getMessage());
        }
    }

    public void publishDeviceServiceInvoke(String deviceId, String driverCode, String serviceCode, Map<String, Object> params) {
        if (mqttClient == null || !mqttClient.isConnected()) {
            return;
        }
        try {
            String msgId = "cmd_srv_" + System.currentTimeMillis();
            JSONObject msg = new JSONObject();
            msg.put("messageId", msgId);
            msg.put("action", "INVOKE_SERVICE");
            msg.put("deviceId", deviceId);
            msg.put("driverCode", driverCode);
            msg.put("serviceCode", serviceCode);
            msg.put("params", params != null ? params : new JSONObject());
            msg.put("timestamp", System.currentTimeMillis());

            String payload = msg.toJSONString();
            mqttClient.publish("iot/device/control", payload, qos);

            // 同时推送到具体驱动指令主题 (使用相同的 messageId 供驱动端幂等去重)
            if (driverCode != null && !driverCode.trim().isEmpty()) {
                JSONObject cmdMsg = new JSONObject();
                cmdMsg.put("messageId", msgId);
                cmdMsg.put("cmdType", "SERVICE");
                cmdMsg.put("deviceId", deviceId);
                cmdMsg.put("serviceCode", serviceCode);
                cmdMsg.put("params", params != null ? params : new JSONObject());
                mqttClient.publish("iot/driver/" + driverCode.trim() + "/cmd", cmdMsg.toJSONString(), qos);
            }

            log.info("[AdminMqttPublisher] Published service invocation to MQTT: deviceId={}, driverCode={}, serviceCode={}, params={}",
                    deviceId, driverCode, serviceCode, params);
        } catch (Exception e) {
            log.error("[AdminMqttPublisher] Failed to publish service invocation via MQTT: {}", e.getMessage());
        }
    }

    public void publishRuleSync() {
        if (mqttClient == null || !mqttClient.isConnected()) {
            return;
        }
        try {
            mqttClient.publish("iot/rule/sync", "{\"action\":\"SYNC\",\"timestamp\":" + System.currentTimeMillis() + "}", qos);
            log.info("[AdminMqttPublisher] Published rule sync event to MQTT (iot/rule/sync)");
        } catch (Exception e) {
            log.warn("[AdminMqttPublisher] Failed to publish rule sync event: {}", e.getMessage());
        }
    }

    @PreDestroy
    public void destroy() {
        if (mqttClient != null) {
            mqttClient.disconnect();
        }
    }
}
