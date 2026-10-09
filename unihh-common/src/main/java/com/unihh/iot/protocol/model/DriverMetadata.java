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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DriverMetadata implements Serializable {
    private static final long serialVersionUID = 1L;

    private String driverCode;
    private String driverName;
    private List<ParamSchema> params = new ArrayList<>();

    public DriverMetadata() {}

    public DriverMetadata(String driverCode, String driverName) {
        this.driverCode = driverCode;
        this.driverName = driverName;
    }

    public static class ParamOption implements Serializable {
        private static final long serialVersionUID = 1L;
        private String label;
        private String value;

        public ParamOption() {}

        public ParamOption(String label, String value) {
            this.label = label;
            this.value = value;
        }

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }

    public static class ParamSchema implements Serializable {
        private static final long serialVersionUID = 1L;

        private String key;
        private String name;
        private String dataType;
        private boolean required;
        private String defaultValue;
        private String description;
        private List<ParamOption> options = new ArrayList<>();

        public ParamSchema() {}

        public ParamSchema(String key, String name, String dataType, boolean required, String defaultValue) {
            this.key = key;
            this.name = name;
            this.dataType = dataType;
            this.required = required;
            this.defaultValue = defaultValue;
        }

        public ParamSchema(String key, String name, String dataType, boolean required, String defaultValue, String description) {
            this.key = key;
            this.name = name;
            this.dataType = dataType;
            this.required = required;
            this.defaultValue = defaultValue;
            this.description = description;
        }

        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDataType() { return dataType; }
        public void setDataType(String dataType) { this.dataType = dataType; }
        public boolean isRequired() { return required; }
        public void setRequired(boolean required) { this.required = required; }
        public String getDefaultValue() { return defaultValue; }
        public void setDefaultValue(String defaultValue) { this.defaultValue = defaultValue; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public List<ParamOption> getOptions() { return options; }
        public void setOptions(List<ParamOption> options) { this.options = options; }
    }

    private AddressRule addressRule;

    public AddressRule getAddressRule() { return addressRule; }
    public void setAddressRule(AddressRule addressRule) { this.addressRule = addressRule; }

    public static class AddressRule implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 规则类型:
         * "MODBUS" - Modbus 寄存器模型 (按数据类型计算步长，离散/线圈独立)
         * "S7" - 西门子 S7 内存模型 (DB 块，DBX/DBW/DBD 偶数对齐及步长)
         * "TEMPLATE" - 模板替换模型 (支持 {deviceId}, {pointCode}, {pointName} 等)
         * "OFFSET_BASED" - 通用数字偏移模型
         */
        private String ruleType;
        private String placeholder;
        private String defaultArea;
        private Integer startOffset;
        private Integer coilStartOffset;
        private String defaultTemplate;
        private Map<String, Integer> typeSteps = new HashMap<>();
        private Map<String, String> typeTemplates = new HashMap<>();

        public AddressRule() {}

        public AddressRule(String ruleType, String placeholder) {
            this.ruleType = ruleType;
            this.placeholder = placeholder;
        }

        public String getRuleType() { return ruleType; }
        public void setRuleType(String ruleType) { this.ruleType = ruleType; }

        public String getPlaceholder() { return placeholder; }
        public void setPlaceholder(String placeholder) { this.placeholder = placeholder; }

        public String getDefaultArea() { return defaultArea; }
        public void setDefaultArea(String defaultArea) { this.defaultArea = defaultArea; }

        public Integer getStartOffset() { return startOffset; }
        public void setStartOffset(Integer startOffset) { this.startOffset = startOffset; }

        public Integer getCoilStartOffset() { return coilStartOffset; }
        public void setCoilStartOffset(Integer coilStartOffset) { this.coilStartOffset = coilStartOffset; }

        public String getDefaultTemplate() { return defaultTemplate; }
        public void setDefaultTemplate(String defaultTemplate) { this.defaultTemplate = defaultTemplate; }

        public Map<String, Integer> getTypeSteps() { return typeSteps; }
        public void setTypeSteps(Map<String, Integer> typeSteps) { this.typeSteps = typeSteps; }

        public Map<String, String> getTypeTemplates() { return typeTemplates; }
        public void setTypeTemplates(Map<String, String> typeTemplates) { this.typeTemplates = typeTemplates; }
    }

    public String getDriverCode() { return driverCode; }
    public void setDriverCode(String driverCode) { this.driverCode = driverCode; }
    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }
    public List<ParamSchema> getParams() { return params; }
    public void setParams(List<ParamSchema> params) { this.params = params; }
}
