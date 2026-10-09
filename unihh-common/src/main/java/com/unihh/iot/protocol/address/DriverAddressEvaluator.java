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

package com.unihh.iot.protocol.address;

import com.unihh.iot.common.model.DeviceConfig;
import com.unihh.iot.common.model.ThingModel;
import com.unihh.iot.protocol.model.DriverMetadata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 驱动协议物理点位地址动态推算引擎
 * 根据驱动注册元数据中定义的 AddressRule，针对物模型的各点位类型计算合法连续的硬件地址
 */
public class DriverAddressEvaluator {

    public static Map<String, String> evaluate(
            DriverMetadata.AddressRule rule,
            String driverCode,
            DeviceConfig device,
            List<ThingModel.PropertySpec> properties
    ) {
        Map<String, String> result = new HashMap<>();
        if (properties == null || properties.isEmpty()) {
            return result;
        }

        // 识别规则类型：若元数据未指定，自动由常用驱动Code推导默认规则类型
        String ruleType = (rule != null && rule.getRuleType() != null && !rule.getRuleType().trim().isEmpty())
                ? rule.getRuleType().toUpperCase()
                : detectDefaultRuleType(driverCode);

        if ("MODBUS".equals(ruleType)) {
            evaluateModbus(rule, properties, result);
        } else if ("S7".equals(ruleType)) {
            evaluateS7(rule, device, properties, result);
        } else if ("TEMPLATE".equals(ruleType)) {
            evaluateTemplate(rule, device, properties, result);
        } else if ("OFFSET_BASED".equals(ruleType)) {
            evaluateOffsetBased(rule, properties, result);
        } else {
            // 通用兜底策略
            for (ThingModel.PropertySpec prop : properties) {
                result.put(prop.getPointCode(), prop.getPointCode());
            }
        }

        return result;
    }

    private static String detectDefaultRuleType(String driverCode) {
        if (driverCode == null) return "DEFAULT";
        String lower = driverCode.toLowerCase();
        if (lower.contains("modbus")) return "MODBUS";
        if (lower.contains("s7") || lower.contains("siemens")) return "S7";
        if (lower.contains("opc")) return "TEMPLATE";
        return "DEFAULT";
    }

    private static void evaluateModbus(
            DriverMetadata.AddressRule rule,
            List<ThingModel.PropertySpec> properties,
            Map<String, String> result
    ) {
        int holdingOffset = (rule != null && rule.getStartOffset() != null) ? rule.getStartOffset() : 40001;
        int coilOffset = (rule != null && rule.getCoilStartOffset() != null) ? rule.getCoilStartOffset() : 1;

        Map<String, Integer> steps = (rule != null && rule.getTypeSteps() != null) ? rule.getTypeSteps() : new HashMap<>();

        for (ThingModel.PropertySpec prop : properties) {
            String dt = prop.getDataType() != null ? prop.getDataType().toUpperCase() : "FLOAT";
            if ("BOOL".equals(dt)) {
                result.put(prop.getPointCode(), String.format("%05d", coilOffset));
                coilOffset++;
            } else {
                result.put(prop.getPointCode(), String.valueOf(holdingOffset));
                int step = getModbusStep(dt, steps);
                holdingOffset += step;
            }
        }
    }

    private static int getModbusStep(String dataType, Map<String, Integer> steps) {
        if (steps != null && steps.containsKey(dataType)) {
            return steps.get(dataType);
        }
        switch (dataType) {
            case "INT":
            case "SHORT":
            case "WORD":
            case "ENUM":
                return 1;
            case "DOUBLE":
            case "INT64":
            case "LREAL":
            case "LWORD":
                return 4;
            case "FLOAT":
            case "INT32":
            case "DWORD":
            default:
                return 2;
        }
    }

    private static void evaluateS7(
            DriverMetadata.AddressRule rule,
            DeviceConfig device,
            List<ThingModel.PropertySpec> properties,
            Map<String, String> result
    ) {
        String area = "DB1";
        if (device != null && device.getConnectionParams() != null) {
            Object dbObj = device.getConnectionParams().get("dbNumber");
            if (dbObj == null) dbObj = device.getConnectionParams().get("dbBlock");
            if (dbObj == null) dbObj = device.getConnectionParams().get("db");
            if (dbObj != null && !dbObj.toString().trim().isEmpty()) {
                String dbStr = dbObj.toString().trim().toUpperCase();
                area = dbStr.startsWith("DB") ? dbStr : "DB" + dbStr;
            }
        }
        if ("DB1".equals(area) && rule != null && rule.getDefaultArea() != null && !rule.getDefaultArea().trim().isEmpty()) {
            area = rule.getDefaultArea();
        }

        int byteOffset = (rule != null && rule.getStartOffset() != null) ? rule.getStartOffset() : 0;
        int bitOffset = 0;
        Map<String, Integer> steps = (rule != null && rule.getTypeSteps() != null) ? rule.getTypeSteps() : new HashMap<>();

        for (ThingModel.PropertySpec prop : properties) {
            String dt = prop.getDataType() != null ? prop.getDataType().toUpperCase() : "FLOAT";
            if ("BOOL".equals(dt)) {
                result.put(prop.getPointCode(), String.format("%s.DBX%d.%d", area, byteOffset, bitOffset));
                bitOffset++;
                if (bitOffset >= 8) {
                    bitOffset = 0;
                    byteOffset++;
                }
            } else {
                if (bitOffset > 0) {
                    byteOffset++;
                    bitOffset = 0;
                }
                // 西门子 S7 要求字/双字地址偶数对齐
                if (byteOffset % 2 != 0) {
                    byteOffset++;
                }

                int step = getS7Step(dt, steps);
                if (step == 2) {
                    result.put(prop.getPointCode(), String.format("%s.DBW%d", area, byteOffset));
                } else {
                    result.put(prop.getPointCode(), String.format("%s.DBD%d", area, byteOffset));
                }
                byteOffset += step;
            }
        }
    }

    private static int getS7Step(String dataType, Map<String, Integer> steps) {
        if (steps != null && steps.containsKey(dataType)) {
            return steps.get(dataType);
        }
        switch (dataType) {
            case "INT":
            case "SHORT":
            case "WORD":
            case "ENUM":
                return 2;
            case "DOUBLE":
            case "INT64":
            case "LREAL":
                return 8;
            case "FLOAT":
            case "INT32":
            case "DWORD":
            default:
                return 4;
        }
    }

    private static void evaluateTemplate(
            DriverMetadata.AddressRule rule,
            DeviceConfig device,
            List<ThingModel.PropertySpec> properties,
            Map<String, String> result
    ) {
        String tmpl = (rule != null && rule.getDefaultTemplate() != null && !rule.getDefaultTemplate().trim().isEmpty())
                ? rule.getDefaultTemplate()
                : "ns=2;s={deviceId}.{pointCode}";

        String devId = (device != null && device.getDeviceId() != null) ? device.getDeviceId() : "device";
        String modelKey = (device != null && device.getModelKey() != null) ? device.getModelKey() : "model";

        int idx = 0;
        for (ThingModel.PropertySpec prop : properties) {
            String addr = tmpl.replace("{deviceId}", devId)
                    .replace("{pointCode}", prop.getPointCode() != null ? prop.getPointCode() : "")
                    .replace("{modelKey}", modelKey)
                    .replace("{pointName}", prop.getPointName() != null ? prop.getPointName() : "")
                    .replace("{index}", String.valueOf(idx++));
            result.put(prop.getPointCode(), addr);
        }
    }

    private static void evaluateOffsetBased(
            DriverMetadata.AddressRule rule,
            List<ThingModel.PropertySpec> properties,
            Map<String, String> result
    ) {
        int offset = (rule != null && rule.getStartOffset() != null) ? rule.getStartOffset() : 0;
        Map<String, Integer> steps = (rule != null && rule.getTypeSteps() != null) ? rule.getTypeSteps() : new HashMap<>();

        for (ThingModel.PropertySpec prop : properties) {
            String dt = prop.getDataType() != null ? prop.getDataType().toUpperCase() : "FLOAT";
            int step = steps.getOrDefault(dt, 1);
            result.put(prop.getPointCode(), String.valueOf(offset));
            offset += step;
        }
    }
}
