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

import java.io.Serializable;

/**
 * EMQX MQTT 综合配置参数模型 (支持连接地址、认证鉴权、超时重连、QoS 及 Session 策略)
 */
public class EmqxProperties implements Serializable {
    private static final long serialVersionUID = 1L;

    private String serverUri = "tcp://127.0.0.1:1883";
    private String username = "";
    private String password = "";
    private String clientIdPrefix = "unihh";
    private boolean cleanSession = true;
    private int keepAliveInterval = 30;
    private int connectionTimeout = 10;
    private boolean automaticReconnect = true;
    private int qos = 1;

    public String getServerUri() { return serverUri; }
    public void setServerUri(String serverUri) { this.serverUri = serverUri; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getClientIdPrefix() { return clientIdPrefix; }
    public void setClientIdPrefix(String clientIdPrefix) { this.clientIdPrefix = clientIdPrefix; }

    public boolean isCleanSession() { return cleanSession; }
    public void setCleanSession(boolean cleanSession) { this.cleanSession = cleanSession; }

    public int getKeepAliveInterval() { return keepAliveInterval; }
    public void setKeepAliveInterval(int keepAliveInterval) { this.keepAliveInterval = keepAliveInterval; }

    public int getConnectionTimeout() { return connectionTimeout; }
    public void setConnectionTimeout(int connectionTimeout) { this.connectionTimeout = connectionTimeout; }

    public boolean isAutomaticReconnect() { return automaticReconnect; }
    public void setAutomaticReconnect(boolean automaticReconnect) { this.automaticReconnect = automaticReconnect; }

    public int getQos() { return qos; }
    public void setQos(int qos) { this.qos = qos; }
}
