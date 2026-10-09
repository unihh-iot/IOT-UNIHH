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
 * 设备实体与通信配置模型
 */
public class DeviceConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String deviceId;
    private String deviceName;
    private String modelKey;
    private String driverCode;
    private String host;
    private int port;
    private Map<String, Object> connectionParams = new HashMap<>();
    private Integer status = 1; // 1: 在线, 0: 离线, 2: 异常
    private Integer acqStatus = 1; // 1: 数采运行, 0: 暂停数采
    private Integer baseIntervalMs = 1000; // 设备基础采集周期(毫秒), 默认1000ms
    private String lastOnlineTime;
    private String createTime;
    private String updateTime;

    public DeviceConfig() {}

    public DeviceConfig(String deviceId, String modelKey, String driverCode, String host, int port) {
        this.deviceId = deviceId;
        this.deviceName = deviceId;
        this.modelKey = modelKey;
        this.driverCode = driverCode;
        this.host = host;
        this.port = port;
    }

    public DeviceConfig(String deviceId, String deviceName, String modelKey, String driverCode, String host, int port) {
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.modelKey = modelKey;
        this.driverCode = driverCode;
        this.host = host;
        this.port = port;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
    public String getModelKey() { return modelKey; }
    public void setModelKey(String modelKey) { this.modelKey = modelKey; }
    public String getDriverCode() { return driverCode; }
    public void setDriverCode(String driverCode) { this.driverCode = driverCode; }
    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }
    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }
    public Map<String, Object> getConnectionParams() { return connectionParams; }
    public void setConnectionParams(Map<String, Object> connectionParams) { this.connectionParams = connectionParams; }

    /**
     * 通用驱动连接拓展参数获取（支持任意私有/标准协议驱动自由扩展，如 Modbus 的 slaveId、S7 的 rack/slot、OPC UA 的 endpointUrl 等）
     */
    public Object getParam(String key) {
        return connectionParams != null ? connectionParams.get(key) : null;
    }

    public Object getParamOrDefault(String key, Object defaultValue) {
        if (connectionParams != null && connectionParams.containsKey(key)) {
            Object val = connectionParams.get(key);
            return val != null ? val : defaultValue;
        }
        return defaultValue;
    }

    public void setParam(String key, Object value) {
        if (this.connectionParams == null) {
            this.connectionParams = new HashMap<>();
        }
        this.connectionParams.put(key, value);
    }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Integer getAcqStatus() { return acqStatus; }
    public void setAcqStatus(Integer acqStatus) { this.acqStatus = acqStatus; }
    public Integer getBaseIntervalMs() { return baseIntervalMs; }
    public void setBaseIntervalMs(Integer baseIntervalMs) { this.baseIntervalMs = baseIntervalMs; }
    public String getLastOnlineTime() { return lastOnlineTime; }
    public void setLastOnlineTime(String lastOnlineTime) { this.lastOnlineTime = lastOnlineTime; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
    public String getUpdateTime() { return updateTime; }
    public void setUpdateTime(String updateTime) { this.updateTime = updateTime; }
}
