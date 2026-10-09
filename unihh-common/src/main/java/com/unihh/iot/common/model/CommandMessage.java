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

package com.unihh.iot.common.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 北向控制与数据写入指令模型
 */
public class CommandMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private String messageId;
    private String modelKey;
    private String deviceId;
    private String driverCode;
    private String serviceCode;
    private Map<String, Object> params = new HashMap<>();
    private long timestamp;

    public CommandMessage() {
        this.timestamp = System.currentTimeMillis();
    }

    public CommandMessage(String messageId, String modelKey, String deviceId, String serviceCode) {
        this.messageId = messageId;
        this.modelKey = modelKey;
        this.deviceId = deviceId;
        this.serviceCode = serviceCode;
        this.timestamp = System.currentTimeMillis();
    }

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }
    public String getModelKey() { return modelKey; }
    public void setModelKey(String modelKey) { this.modelKey = modelKey; }
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public String getDriverCode() { return driverCode; }
    public void setDriverCode(String driverCode) { this.driverCode = driverCode; }
    public String getServiceCode() { return serviceCode; }
    public void setServiceCode(String serviceCode) { this.serviceCode = serviceCode; }
    public Map<String, Object> getParams() { return params; }
    public void setParams(Map<String, Object> params) { this.params = params; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
