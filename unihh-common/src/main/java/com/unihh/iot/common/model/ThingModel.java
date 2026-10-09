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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 物模型定义 (Property, Service, Event)
 */
public class ThingModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private String modelKey;
    private String modelName;
    private String category;
    private String description;
    private List<PropertySpec> properties = new ArrayList<>();
    private List<ServiceSpec> services = new ArrayList<>();
    private List<EventSpec> events = new ArrayList<>();
    private Integer deviceCount = 0;

    public ThingModel() {}

    public ThingModel(String modelKey, String modelName) {
        this.modelKey = modelKey;
        this.modelName = modelName;
    }

    public static class PropertySpec implements Serializable {
        private String pointCode;
        private String pointName;
        private String dataType; // INT, DOUBLE, STRING, BOOL, ENUM
        private String accessMode; // R, W, RW
        private String unit;
        private String address;
        private String description;
        private Integer sampleFactor = 1; // 采样倍数因子, 默认1x
        private String triggerMode = "PERIODIC"; // PERIODIC: 周期采集, COV: 变化触发
        private Double scale = 1.0;
        private Double offset = 0.0;
        private Double min;
        private Double max;
        private Object defaultValue;
        private List<Map<String, Object>> enumOptions = new ArrayList<>();

        public PropertySpec() {}

        public PropertySpec(String pointCode, String pointName, String dataType, String accessMode, String unit) {
            this.pointCode = pointCode;
            this.pointName = pointName;
            this.dataType = dataType;
            this.accessMode = accessMode;
            this.unit = unit;
        }

        public String getPointCode() { return pointCode; }
        public void setPointCode(String pointCode) { this.pointCode = pointCode; }
        public String getPointName() { return pointName; }
        public void setPointName(String pointName) { this.pointName = pointName; }
        public String getDataType() { return dataType; }
        public void setDataType(String dataType) { this.dataType = dataType; }
        public String getAccessMode() { return accessMode; }
        public void setAccessMode(String accessMode) { this.accessMode = accessMode; }
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Integer getSampleFactor() { return sampleFactor; }
        public void setSampleFactor(Integer sampleFactor) { this.sampleFactor = sampleFactor; }
        public String getTriggerMode() { return triggerMode; }
        public void setTriggerMode(String triggerMode) { this.triggerMode = triggerMode; }
        public Double getScale() { return scale != null ? scale : 1.0; }
        public void setScale(Double scale) { this.scale = scale; }
        public Double getOffset() { return offset != null ? offset : 0.0; }
        public void setOffset(Double offset) { this.offset = offset; }
        public Double getMin() { return min; }
        public void setMin(Double min) { this.min = min; }
        public Double getMax() { return max; }
        public void setMax(Double max) { this.max = max; }
        public Object getDefaultValue() { return defaultValue; }
        public void setDefaultValue(Object defaultValue) { this.defaultValue = defaultValue; }
        public List<Map<String, Object>> getEnumOptions() { return enumOptions; }
        public void setEnumOptions(List<Map<String, Object>> enumOptions) { this.enumOptions = enumOptions; }
    }

    public static class ServiceSpec implements Serializable {
        private String serviceCode;
        private String serviceName;
        private String description;
        private List<PropertySpec> inputParams = new ArrayList<>();

        public ServiceSpec() {}

        public ServiceSpec(String serviceCode, String serviceName) {
            this.serviceCode = serviceCode;
            this.serviceName = serviceName;
        }

        public String getServiceCode() { return serviceCode; }
        public void setServiceCode(String serviceCode) { this.serviceCode = serviceCode; }
        public String getServiceName() { return serviceName; }
        public void setServiceName(String serviceName) { this.serviceName = serviceName; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public List<PropertySpec> getInputParams() { return inputParams; }
        public void setInputParams(List<PropertySpec> inputParams) { this.inputParams = inputParams; }
    }

    public static class EventSpec implements Serializable {
        private String eventCode;
        private String eventName;
        private String eventType; // ALERT, FAULT, INFO
        private String eventLevel; // INFO, WARN, CRITICAL
        private String description;

        public EventSpec() {}

        public EventSpec(String eventCode, String eventName, String eventType, String eventLevel, String description) {
            this.eventCode = eventCode;
            this.eventName = eventName;
            this.eventType = eventType;
            this.eventLevel = eventLevel;
            this.description = description;
        }

        public String getEventCode() { return eventCode; }
        public void setEventCode(String eventCode) { this.eventCode = eventCode; }
        public String getEventName() { return eventName; }
        public void setEventName(String eventName) { this.eventName = eventName; }
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
        public String getEventLevel() { return eventLevel; }
        public void setEventLevel(String eventLevel) { this.eventLevel = eventLevel; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public String getModelKey() { return modelKey; }
    public void setModelKey(String modelKey) { this.modelKey = modelKey; }
    public String getModelCode() { return modelKey; }
    public void setModelCode(String modelCode) { this.modelKey = modelCode; }
    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<PropertySpec> getProperties() { return properties; }
    public void setProperties(List<PropertySpec> properties) { this.properties = properties; }
    public List<ServiceSpec> getServices() { return services; }
    public void setServices(List<ServiceSpec> services) { this.services = services; }
    public List<EventSpec> getEvents() { return events; }
    public void setEvents(List<EventSpec> events) { this.events = events; }
    public Integer getDeviceCount() { return deviceCount != null ? deviceCount : 0; }
    public void setDeviceCount(Integer deviceCount) { this.deviceCount = deviceCount; }
}
