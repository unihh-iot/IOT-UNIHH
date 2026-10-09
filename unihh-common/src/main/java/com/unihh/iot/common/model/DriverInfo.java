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
 * 协议驱动注册模型
 */
public class DriverInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String driverCode;
    private String driverName;
    private String version;
    private String jarFileName;
    private Integer status; // 1: 启用, 0: 停用
    private String description;
    private String manifestYaml;
    private String serviceUrl;
    private String createTime;
    private Integer deviceCount = 0;
    private Integer channelCount = 0;
    private java.util.List<com.unihh.iot.protocol.model.DriverMetadata.ParamSchema> params = new java.util.ArrayList<>();
    private com.unihh.iot.protocol.model.DriverMetadata.AddressRule addressRule;

    public DriverInfo() {}

    public DriverInfo(String driverCode, String driverName, String version, String jarFileName, String description) {
        this.driverCode = driverCode;
        this.driverName = driverName;
        this.version = version != null ? version : "1.0.0";
        this.jarFileName = jarFileName;
        this.description = description;
        this.status = 1;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDriverCode() { return driverCode; }
    public void setDriverCode(String driverCode) { this.driverCode = driverCode; }
    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public String getJarFileName() { return jarFileName; }
    public void setJarFileName(String jarFileName) { this.jarFileName = jarFileName; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getManifestYaml() {
        if ((manifestYaml == null || manifestYaml.trim().isEmpty()) && params != null && !params.isEmpty()) {
            this.manifestYaml = generateYamlFromParams();
        }
        return manifestYaml;
    }
    public void setManifestYaml(String manifestYaml) {
        this.manifestYaml = manifestYaml;
        parseManifestYamlToParams();
    }

    public String generateYamlFromParams() {
        if (params == null || params.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("driverCode: ").append(driverCode != null ? driverCode : "").append("\n");
        sb.append("driverName: ").append(driverName != null ? driverName : "").append("\n");
        sb.append("version: ").append(version != null ? version : "2.0.0").append("\n");
        sb.append("params:\n");
        for (com.unihh.iot.protocol.model.DriverMetadata.ParamSchema p : params) {
            sb.append("  - key: ").append(p.getKey()).append("\n");
            sb.append("    name: ").append(p.getName()).append("\n");
            sb.append("    dataType: ").append(p.getDataType() != null ? p.getDataType() : "STRING").append("\n");
            sb.append("    required: ").append(p.isRequired()).append("\n");
            if (p.getDefaultValue() != null) {
                sb.append("    defaultValue: '").append(p.getDefaultValue()).append("'\n");
            }
            if (p.getDescription() != null && !p.getDescription().isEmpty()) {
                sb.append("    description: '").append(p.getDescription()).append("'\n");
            }
            if (p.getOptions() != null && !p.getOptions().isEmpty()) {
                sb.append("    options:\n");
                for (com.unihh.iot.protocol.model.DriverMetadata.ParamOption opt : p.getOptions()) {
                    sb.append("      - label: '").append(opt.getLabel()).append("'\n");
                    sb.append("        value: '").append(opt.getValue()).append("'\n");
                }
            }
        }
        if (addressRule != null) {
            sb.append("addressRule:\n");
            if (addressRule.getRuleType() != null) {
                sb.append("  ruleType: '").append(addressRule.getRuleType()).append("'\n");
            }
            if (addressRule.getPlaceholder() != null) {
                sb.append("  placeholder: '").append(addressRule.getPlaceholder()).append("'\n");
            }
            if (addressRule.getDefaultArea() != null) {
                sb.append("  defaultArea: '").append(addressRule.getDefaultArea()).append("'\n");
            }
            if (addressRule.getStartOffset() != null) {
                sb.append("  startOffset: ").append(addressRule.getStartOffset()).append("\n");
            }
            if (addressRule.getCoilStartOffset() != null) {
                sb.append("  coilStartOffset: ").append(addressRule.getCoilStartOffset()).append("\n");
            }
            if (addressRule.getDefaultTemplate() != null) {
                sb.append("  defaultTemplate: '").append(addressRule.getDefaultTemplate()).append("'\n");
            }
            if (addressRule.getTypeSteps() != null && !addressRule.getTypeSteps().isEmpty()) {
                sb.append("  typeSteps:\n");
                for (java.util.Map.Entry<String, Integer> entry : addressRule.getTypeSteps().entrySet()) {
                    sb.append("    ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
                }
            }
        }
        return sb.toString();
    }
    public String getServiceUrl() { return serviceUrl; }
    public void setServiceUrl(String serviceUrl) { this.serviceUrl = serviceUrl; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
    public Integer getDeviceCount() { return deviceCount != null ? deviceCount : 0; }
    public void setDeviceCount(Integer deviceCount) { this.deviceCount = deviceCount; }
    public Integer getChannelCount() { return channelCount != null ? channelCount : 0; }
    public void setChannelCount(Integer channelCount) { this.channelCount = channelCount; }
    public java.util.List<com.unihh.iot.protocol.model.DriverMetadata.ParamSchema> getParams() {
        if ((params == null || params.isEmpty()) && manifestYaml != null && !manifestYaml.trim().isEmpty()) {
            parseManifestYamlToParams();
        }
        return params;
    }
    public void setParams(java.util.List<com.unihh.iot.protocol.model.DriverMetadata.ParamSchema> params) {
        this.params = params;
    }

    public com.unihh.iot.protocol.model.DriverMetadata.AddressRule getAddressRule() {
        if (addressRule == null && manifestYaml != null && !manifestYaml.trim().isEmpty()) {
            parseManifestYamlToParams();
        }
        return addressRule;
    }

    public void setAddressRule(com.unihh.iot.protocol.model.DriverMetadata.AddressRule addressRule) {
        this.addressRule = addressRule;
    }

    @SuppressWarnings("unchecked")
    public void parseManifestYamlToParams() {
        if (this.manifestYaml == null || this.manifestYaml.trim().isEmpty()) {
            return;
        }
        try {
            org.yaml.snakeyaml.Yaml yaml = new org.yaml.snakeyaml.Yaml();
            java.util.Map<String, Object> map = yaml.load(this.manifestYaml);
            if (map != null && map.containsKey("params")) {
                Object pListObj = map.get("params");
                if (pListObj instanceof java.util.List) {
                    java.util.List<com.unihh.iot.protocol.model.DriverMetadata.ParamSchema> list = new java.util.ArrayList<>();
                    for (Object item : (java.util.List<?>) pListObj) {
                        if (item instanceof java.util.Map) {
                            java.util.Map<?, ?> pMap = (java.util.Map<?, ?>) item;
                            com.unihh.iot.protocol.model.DriverMetadata.ParamSchema schema = new com.unihh.iot.protocol.model.DriverMetadata.ParamSchema();
                            schema.setKey(pMap.get("key") != null ? pMap.get("key").toString() : "");
                            schema.setName(pMap.get("name") != null ? pMap.get("name").toString() : "");
                            schema.setDataType(pMap.get("dataType") != null ? pMap.get("dataType").toString() : "STRING");
                            schema.setRequired(Boolean.TRUE.equals(pMap.get("required")) || "true".equalsIgnoreCase(String.valueOf(pMap.get("required"))));
                            schema.setDefaultValue(pMap.get("defaultValue") != null ? pMap.get("defaultValue").toString() : "");
                            if (pMap.get("description") != null) {
                                schema.setDescription(pMap.get("description").toString());
                            }
                            if (pMap.containsKey("options") && pMap.get("options") instanceof java.util.List) {
                                java.util.List<com.unihh.iot.protocol.model.DriverMetadata.ParamOption> optList = new java.util.ArrayList<>();
                                for (Object optObj : (java.util.List<?>) pMap.get("options")) {
                                    if (optObj instanceof java.util.Map) {
                                        java.util.Map<?, ?> optMap = (java.util.Map<?, ?>) optObj;
                                        String label = optMap.get("label") != null ? optMap.get("label").toString() : "";
                                        String val = optMap.get("value") != null ? optMap.get("value").toString() : "";
                                        optList.add(new com.unihh.iot.protocol.model.DriverMetadata.ParamOption(label, val));
                                    }
                                }
                                schema.setOptions(optList);
                            }
                            list.add(schema);
                        }
                    }
                    this.params = list;
                }
            }

            if (map != null) {
                Object ruleObj = map.get("addressRule");
                if (ruleObj == null) ruleObj = map.get("address_rule");
                if (ruleObj instanceof java.util.Map) {
                    java.util.Map<?, ?> rMap = (java.util.Map<?, ?>) ruleObj;
                    com.unihh.iot.protocol.model.DriverMetadata.AddressRule rule = new com.unihh.iot.protocol.model.DriverMetadata.AddressRule();
                    if (rMap.get("ruleType") != null) rule.setRuleType(rMap.get("ruleType").toString());
                    if (rMap.get("placeholder") != null) rule.setPlaceholder(rMap.get("placeholder").toString());
                    if (rMap.get("defaultArea") != null) rule.setDefaultArea(rMap.get("defaultArea").toString());
                    if (rMap.get("defaultTemplate") != null) rule.setDefaultTemplate(rMap.get("defaultTemplate").toString());
                    if (rMap.get("startOffset") != null) {
                        try { rule.setStartOffset(Integer.parseInt(rMap.get("startOffset").toString())); } catch (Exception ignored) {}
                    }
                    if (rMap.get("coilStartOffset") != null) {
                        try { rule.setCoilStartOffset(Integer.parseInt(rMap.get("coilStartOffset").toString())); } catch (Exception ignored) {}
                    }
                    if (rMap.get("typeSteps") instanceof java.util.Map) {
                        java.util.Map<?, ?> stepMap = (java.util.Map<?, ?>) rMap.get("typeSteps");
                        java.util.Map<String, Integer> steps = new java.util.HashMap<>();
                        for (java.util.Map.Entry<?, ?> entry : stepMap.entrySet()) {
                            try {
                                steps.put(entry.getKey().toString(), Integer.parseInt(entry.getValue().toString()));
                            } catch (Exception ignored) {}
                        }
                        rule.setTypeSteps(steps);
                    }
                    this.addressRule = rule;
                }
            }
        } catch (Exception ignored) {}
    }
}
