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

package com.unihh.iot.protocol.model;

import com.unihh.iot.common.model.PointData;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ParsedPayload implements Serializable {
    private static final long serialVersionUID = 1L;

    private String deviceId;
    private String modelKey;
    private List<PointData> points = new ArrayList<>();
    private long timestamp;

    public ParsedPayload() {
        this.timestamp = System.currentTimeMillis();
    }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public String getModelKey() { return modelKey; }
    public void setModelKey(String modelKey) { this.modelKey = modelKey; }
    public List<PointData> getPoints() { return points; }
    public void setPoints(List<PointData> points) { this.points = points; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
