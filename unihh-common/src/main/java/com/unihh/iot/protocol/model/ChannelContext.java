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

import java.io.Serializable;

public class ChannelContext implements Serializable {
    private static final long serialVersionUID = 1L;

    private String channelId;
    private String deviceId;
    private String driverCode;
    private boolean active;
    private long lastActiveTime;

    public ChannelContext() {}

    public ChannelContext(String channelId, String deviceId, String driverCode) {
        this.channelId = channelId;
        this.deviceId = deviceId;
        this.driverCode = driverCode;
        this.active = true;
        this.lastActiveTime = System.currentTimeMillis();
    }

    public String getChannelId() { return channelId; }
    public void setChannelId(String channelId) { this.channelId = channelId; }
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public String getDriverCode() { return driverCode; }
    public void setDriverCode(String driverCode) { this.driverCode = driverCode; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public long getLastActiveTime() { return lastActiveTime; }
    public void setLastActiveTime(long lastActiveTime) { this.lastActiveTime = lastActiveTime; }
}
