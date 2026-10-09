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

package com.unihh.iot.data.mqtt;

import com.alibaba.fastjson2.JSON;
import com.unihh.iot.common.model.PointData;
import com.unihh.iot.common.mqtt.MqttBridgeClient;
import com.unihh.iot.data.rule.RuleEngineService;
import com.unihh.iot.data.service.DataIngestionService;
import com.unihh.iot.protocol.model.ParsedPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

/**
 * 数据中心 EMQX MQTT 点位数据上报消费者 (DataMqttConsumer)
 * 订阅所有驱动点位上报主题 (iot/driver/+/data)，通过 DataIngestionService 路由落库，并触发规则引擎处理
 */
@Component
public class DataMqttConsumer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DataMqttConsumer.class);

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

    private final DataIngestionService ingestionService;
    private final RuleEngineService ruleEngineService;
    private MqttBridgeClient mqttClient;

    public DataMqttConsumer(DataIngestionService ingestionService, RuleEngineService ruleEngineService) {
        this.ingestionService = ingestionService;
        this.ruleEngineService = ruleEngineService;
    }

    @Override
    public void run(String... args) {
        new Thread(() -> {
            try {
                Thread.sleep(1500);
                String clientId = "data_service_" + System.currentTimeMillis();
                mqttClient = new MqttBridgeClient(emqxServerUri, clientId, username, password,
                        cleanSession, keepAliveInterval, connectionTimeout, automaticReconnect, qos);
                mqttClient.connect();

                // 注入 MQTT 客户端供规则引擎下发联动控制
                ruleEngineService.setMqttClient(mqttClient);

                // 连接成功后立即主动同步一次 Admin 规则
                ruleEngineService.syncRulesFromAdmin();

                // 订阅规则热更新通知 (iot/rule/sync)
                mqttClient.subscribe("iot/rule/sync", qos, (syncTopic, message) -> {
                    log.info("[DataMqttConsumer] Received rule sync notification from MQTT topic [{}], refreshing rules...", syncTopic);
                    ruleEngineService.syncRulesFromAdmin();
                });

                // 订阅全局设备配置与测点映射变更通知 (iot/device/config/update)，动态失效缓存
                mqttClient.subscribe("iot/device/config/update", qos, (updateTopic, message) -> {
                    try {
                        String payloadStr = new String(message.getPayload());
                        com.alibaba.fastjson2.JSONObject json = JSON.parseObject(payloadStr);
                        if (json != null && json.containsKey("deviceId")) {
                            String devId = json.getString("deviceId");
                            ruleEngineService.invalidateDeviceCache(devId);
                            log.info("[DataMqttConsumer] 监听到设备配置/测点更新事件通知，已同步清空设备 [{}] 规则引擎元数据与测点映射缓存", devId);
                        }
                    } catch (Exception e) {
                        log.warn("[DataMqttConsumer] 处理设备配置更新事件通知异常: {}", e.getMessage());
                    }
                });

                // 订阅所有驱动的点位数据上报主题 (iot/driver/+/data)
                mqttClient.subscribe("iot/driver/+/data", qos, (topic, message) -> {
                    String payloadStr = new String(message.getPayload());
                    handleDriverData(topic, payloadStr);
                });

                log.info("[DataMqttConsumer] Subscribed to EMQX topics [iot/driver/+/data], [iot/rule/sync], [iot/device/config/update] for point data, rules & device updates.");
            } catch (Exception e) {
                log.warn("[DataMqttConsumer] EMQX MQTT connection notice: {}", e.getMessage());
            }
        }).start();
    }

    private void handleDriverData(String topic, String payloadStr) {
        try {
            ParsedPayload payload = JSON.parseObject(payloadStr, ParsedPayload.class);
            if (payload != null && payload.getPoints() != null && !payload.getPoints().isEmpty()) {
                String devId = payload.getDeviceId();
                // 确保每个点位携带正确的 deviceId，以精准匹配倒排规则索引 DEV:{deviceId}:{pointCode}
                for (PointData point : payload.getPoints()) {
                    if (point != null && (point.getDeviceId() == null || point.getDeviceId().trim().isEmpty())) {
                        point.setDeviceId(devId);
                    }
                }

                // 1. 采用存储引擎策略批量落库
                ingestionService.processPointDataBatch(payload.getPoints());

                // 2. 规则引擎计算与阈值告警检测
                for (PointData point : payload.getPoints()) {
                    if (point != null) {
                        ruleEngineService.evaluateRule(point);
                    }
                }

                log.debug("[DataMqttConsumer] Processed point data via MQTT from topic [{}]: deviceId={}, count={}",
                        topic, payload.getDeviceId(), payload.getPoints().size());
            }
        } catch (Exception e) {
            log.error("[DataMqttConsumer] Error parsing point data from MQTT topic [{}]: {}", topic, e.getMessage());
        }
    }

    @PreDestroy
    public void destroy() {
        if (mqttClient != null) {
            mqttClient.disconnect();
        }
    }
}
