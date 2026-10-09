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
import java.util.Date;

/**
 * 设备物模型点位物理地址映射模型
 */
public class DevicePointMapping implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String deviceId;
    private String modelKey;
    private String pointCode;
    private String pointName;
    private String physicalAddress;
    private String dataType;   // INT, DOUBLE, BOOL, STRING, ENUM
    private String accessMode; // R, W, RW
    private Double scale = 1.0;
    private Double offset = 0.0;
    private String remark;
    private Date createTime;
    private Date updateTime;

    public DevicePointMapping() {}

    public DevicePointMapping(String deviceId, String modelKey, String pointCode, String pointName, String physicalAddress, String dataType, String accessMode) {
        this.deviceId = deviceId;
        this.modelKey = modelKey;
        this.pointCode = pointCode;
        this.pointName = pointName;
        this.physicalAddress = physicalAddress;
        this.dataType = dataType;
        this.accessMode = accessMode;
        this.scale = 1.0;
        this.offset = 0.0;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public String getModelKey() { return modelKey; }
    public void setModelKey(String modelKey) { this.modelKey = modelKey; }
    public String getPointCode() { return pointCode; }
    public void setPointCode(String pointCode) { this.pointCode = pointCode; }
    public String getPointName() { return pointName; }
    public void setPointName(String pointName) { this.pointName = pointName; }
    public String getPhysicalAddress() { return physicalAddress; }
    public void setPhysicalAddress(String physicalAddress) { this.physicalAddress = physicalAddress; }
    public String getDataType() { return dataType; }
    public void setDataType(String dataType) { this.dataType = dataType; }
    public String getAccessMode() { return accessMode; }
    public void setAccessMode(String accessMode) { this.accessMode = accessMode; }
    public Double getScale() { return scale != null ? scale : 1.0; }
    public void setScale(Double scale) { this.scale = scale; }
    public Double getOffset() { return offset != null ? offset : 0.0; }
    public void setOffset(Double offset) { this.offset = offset; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
}
