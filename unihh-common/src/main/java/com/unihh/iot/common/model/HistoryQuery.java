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
 * 历史点位数据查询参数
 */
public class HistoryQuery implements Serializable {
    private static final long serialVersionUID = 1L;

    private String deviceId;
    private String pointCode;
    private long startTime;
    private long endTime;
    private int limit = 100;

    public HistoryQuery() {}

    public HistoryQuery(String deviceId, String pointCode, long startTime, long endTime) {
        this.deviceId = deviceId;
        this.pointCode = pointCode;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public String getPointCode() { return pointCode; }
    public void setPointCode(String pointCode) { this.pointCode = pointCode; }
    public long getStartTime() { return startTime; }
    public void setStartTime(long startTime) { this.startTime = startTime; }
    public long getEndTime() { return endTime; }
    public void setEndTime(long endTime) { this.endTime = endTime; }
    public int getLimit() { return limit; }
    public void setLimit(int limit) { this.limit = limit; }
}
