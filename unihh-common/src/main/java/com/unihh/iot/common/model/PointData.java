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

/**
 * 物模型标准点位数据模型
 */
public class PointData implements Serializable {
    private static final long serialVersionUID = 1L;

    private String modelKey;
    private String deviceId;
    private String pointCode;
    private double value;
    private String rawValue;
    private int quality;
    private long timestamp;

    public PointData() {}

    public PointData(String modelKey, String deviceId, String pointCode, double value, long timestamp) {
        this.modelKey = modelKey;
        this.deviceId = deviceId;
        this.pointCode = pointCode;
        this.value = value;
        this.quality = 1;
        this.timestamp = timestamp;
    }

    public String getModelKey() { return modelKey; }
    public void setModelKey(String modelKey) { this.modelKey = modelKey; }
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public String getPointCode() { return pointCode; }
    public void setPointCode(String pointCode) { this.pointCode = pointCode; }
    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }
    public String getRawValue() { return rawValue; }
    public void setRawValue(String rawValue) { this.rawValue = rawValue; }
    public int getQuality() { return quality; }
    public void setQuality(int quality) { this.quality = quality; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
