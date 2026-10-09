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

package com.unihh.iot.common.mqtt;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

/**
 * EMQX MQTT 消息总线客户端封装 (支持账号密码认证、自定义 QoS、Session 管理与高可用自动断线重连)
 */
public class MqttBridgeClient {
    private static final Logger log = LoggerFactory.getLogger(MqttBridgeClient.class);

    private final String serverUri;
    private final String clientId;
    private final String username;
    private final String password;
    private final int defaultQos;
    private MqttClient mqttClient;
    private final MqttConnectOptions connectOptions;

    public MqttBridgeClient(String serverUri, String clientId) {
        this(serverUri, clientId, null, null);
    }

    public MqttBridgeClient(String serverUri, String clientId, String username, String password) {
        this(serverUri, clientId, username, password, true, 30, 10, true, 1);
    }

    public MqttBridgeClient(EmqxProperties properties, String clientId) {
        this(properties != null ? properties.getServerUri() : "tcp://127.0.0.1:1883",
             clientId,
             properties != null ? properties.getUsername() : null,
             properties != null ? properties.getPassword() : null,
             properties == null || properties.isCleanSession(),
             properties != null ? properties.getKeepAliveInterval() : 30,
             properties != null ? properties.getConnectionTimeout() : 10,
             properties == null || properties.isAutomaticReconnect(),
             properties != null ? properties.getQos() : 1);
    }

    public MqttBridgeClient(String serverUri, String clientId, String username, String password,
                            boolean cleanSession, int keepAliveInterval, int connectionTimeout,
                            boolean automaticReconnect, int qos) {
        this.serverUri = (serverUri != null && !serverUri.trim().isEmpty()) ? serverUri : "tcp://127.0.0.1:1883";
        this.clientId = clientId;
        this.username = username;
        this.password = password;
        this.defaultQos = (qos >= 0 && qos <= 2) ? qos : 1;

        this.connectOptions = new MqttConnectOptions();
        this.connectOptions.setAutomaticReconnect(automaticReconnect);
        this.connectOptions.setCleanSession(cleanSession);
        this.connectOptions.setConnectionTimeout(connectionTimeout > 0 ? connectionTimeout : 10);
        this.connectOptions.setKeepAliveInterval(keepAliveInterval > 0 ? keepAliveInterval : 30);

        if (username != null && !username.trim().isEmpty()) {
            this.connectOptions.setUserName(username.trim());
            if (password != null) {
                this.connectOptions.setPassword(password.toCharArray());
            }
        }
    }

    /**
     * 配置 MQTT LWT (Last Will and Testament / 遗嘱消息)
     * 当客户端与 EMQX 异常断开 (进程强杀/崩溃/网络闪断) 时，由 EMQX Broker 自动向该主题发布离线遗嘱报文
     */
    public void setWill(String topic, String payload, int qos, boolean retained) {
        if (connectOptions != null && topic != null && payload != null) {
            connectOptions.setWill(topic, payload.getBytes(StandardCharsets.UTF_8), qos, retained);
            log.info("[EMQX-MQTT] Configured LWT (Last Will and Testament) on topic [{}], QoS [{}], retained [{}]",
                    topic, qos, retained);
        }
    }

    public MqttConnectOptions getConnectOptions() {
        return connectOptions;
    }

    public synchronized void connect() throws MqttException {
        if (mqttClient != null && mqttClient.isConnected()) {
            return;
        }
        log.info("[EMQX-MQTT] Connecting to EMQX Broker [{}] with clientId [{}], username [{}]...",
                serverUri, clientId, (username != null && !username.trim().isEmpty()) ? username : "anonymous");
        mqttClient = new MqttClient(serverUri, clientId, new MemoryPersistence());
        mqttClient.connect(connectOptions);
        log.info("[EMQX-MQTT] Connected to EMQX Broker successfully.");
    }

    public void subscribe(String topic, IMqttMessageListener messageListener) throws MqttException {
        subscribe(topic, defaultQos, messageListener);
    }

    public void subscribe(String topic, int qos, IMqttMessageListener messageListener) throws MqttException {
        if (mqttClient == null || !mqttClient.isConnected()) {
            connect();
        }
        log.info("[EMQX-MQTT] Subscribing to topic [{}] with QoS [{}]...", topic, qos);
        mqttClient.subscribe(topic, qos, messageListener);
    }

    public void publish(String topic, String payloadJson) {
        publish(topic, payloadJson, defaultQos);
    }

    public void publish(String topic, String payloadJson, int qos) {
        publish(topic, payloadJson, qos, false);
    }

    public void publish(String topic, String payloadJson, int qos, boolean retained) {
        try {
            if (mqttClient == null || !mqttClient.isConnected()) {
                connect();
            }
            MqttMessage message = new MqttMessage(payloadJson.getBytes(StandardCharsets.UTF_8));
            message.setQos(qos);
            message.setRetained(retained);
            mqttClient.publish(topic, message);
            log.debug("[EMQX-MQTT] Published message to topic [{}], QoS [{}], retained [{}]: {}", topic, qos, retained, payloadJson);
        } catch (Exception e) {
            log.error("[EMQX-MQTT] Failed to publish message to topic [{}]: {}", topic, e.getMessage());
        }
    }

    public void disconnect() {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
                log.info("[EMQX-MQTT] Disconnected from EMQX Broker.");
            }
        } catch (Exception ignored) {}
    }

    public boolean isConnected() {
        return mqttClient != null && mqttClient.isConnected();
    }
}
