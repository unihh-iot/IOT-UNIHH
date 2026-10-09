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

package com.unihh.iot.admin.dao;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.unihh.iot.common.model.ThingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class ThingModelDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public ThingModel findByModelKey(String modelKey) {
        String modelName = "物模型模板 (" + modelKey + ")";
        String category = "ROTATING_EQUIP";
        String description = "工业硬件物模型定义";

        if ("pk_demo".equals(modelKey)) {
            modelName = "标准工业变频电机数采模型";
            category = "ROTATING_EQUIP";
            description = "适用于高压风机、水泵、电机设备之温度、湿度、压力与反控服务定义。";
        } else if ("pk_plc_s7".equals(modelKey)) {
            modelName = "西门子 S7 全系列 PLC 点位模型";
            category = "PLC_AUTOMATION";
            description = "适用于西门子 S7-200/300/1200/1500 PLC DB 块变量读写与脉冲控制。";
        } else if ("pk_opcua_server".equals(modelKey)) {
            modelName = "OPC UA 工业网关节点模型";
            category = "GATEWAY";
            description = "适用于工业以太网 OPC UA 规范节点订阅与控制服务。";
        }

        ThingModel model = new ThingModel(modelKey, modelName);
        model.setCategory(category);
        model.setDescription(description);

        String sql = "SELECT * FROM iot_thing_model WHERE model_key = ? ORDER BY id ASC";
        jdbcTemplate.query(sql, new Object[]{modelKey}, rs -> {
            String modelType = rs.getString("model_type");
            String identifier = rs.getString("identifier");
            String name = rs.getString("name");
            String dataType = rs.getString("data_type");
            String accessMode = rs.getString("access_mode");
            String unit = rs.getString("unit");
            String schemaJsonStr = rs.getString("schema_json");

            JSONObject schemaObj = null;
            if (schemaJsonStr != null && !schemaJsonStr.trim().isEmpty()) {
                try {
                    schemaObj = JSON.parseObject(schemaJsonStr);
                } catch (Exception ignored) {}
            }

            if ("META".equalsIgnoreCase(modelType)) {
                if (name != null && !name.trim().isEmpty()) {
                    model.setModelName(name);
                }
                if (schemaObj != null) {
                    if (schemaObj.containsKey("category")) model.setCategory(schemaObj.getString("category"));
                    if (schemaObj.containsKey("description")) model.setDescription(schemaObj.getString("description"));
                }
            } else if ("PROPERTY".equalsIgnoreCase(modelType)) {
                ThingModel.PropertySpec prop = new ThingModel.PropertySpec(identifier, name, dataType, accessMode, unit);
                if (schemaObj != null) {
                    prop.setDescription(schemaObj.getString("description"));
                    prop.setAddress(schemaObj.getString("address"));
                    if (schemaObj.containsKey("sampleFactor")) prop.setSampleFactor(schemaObj.getInteger("sampleFactor"));
                    if (schemaObj.containsKey("triggerMode")) prop.setTriggerMode(schemaObj.getString("triggerMode"));
                    if (schemaObj.containsKey("min")) prop.setMin(schemaObj.getDouble("min"));
                    if (schemaObj.containsKey("max")) prop.setMax(schemaObj.getDouble("max"));
                    if (schemaObj.containsKey("enumOptions")) {
                        List<Map> opts = schemaObj.getList("enumOptions", Map.class);
                        if (opts != null) {
                            List<Map<String, Object>> typedOpts = new ArrayList<>();
                            for (Object o : opts) {
                                if (o instanceof Map) typedOpts.add((Map<String, Object>) o);
                            }
                            prop.setEnumOptions(typedOpts);
                        }
                    }
                }
                model.getProperties().add(prop);

            } else if ("SERVICE".equalsIgnoreCase(modelType)) {
                ThingModel.ServiceSpec srv = new ThingModel.ServiceSpec(identifier, name);
                if (schemaObj != null) {
                    srv.setDescription(schemaObj.getString("description"));
                    if (schemaObj.containsKey("inputParams")) {
                        List<ThingModel.PropertySpec> params = schemaObj.getList("inputParams", ThingModel.PropertySpec.class);
                        if (params != null) srv.setInputParams(params);
                    }
                }
                model.getServices().add(srv);

            } else if ("EVENT".equalsIgnoreCase(modelType)) {
                String eventType = schemaObj != null ? schemaObj.getString("eventType") : "ALERT";
                String eventLevel = schemaObj != null ? schemaObj.getString("eventLevel") : "WARN";
                String desc = schemaObj != null ? schemaObj.getString("description") : "";
                ThingModel.EventSpec evt = new ThingModel.EventSpec(identifier, name, eventType, eventLevel, desc);
                model.getEvents().add(evt);
            }
        });

        return model;
    }

    public List<ThingModel> findAllModels() {
        String sql = "SELECT DISTINCT model_key FROM iot_thing_model";
        List<String> modelKeys = jdbcTemplate.queryForList(sql, String.class);

        List<ThingModel> result = new ArrayList<>();
        Set<String> processed = new HashSet<>();
        for (String pk : modelKeys) {
            if (pk != null && !pk.trim().isEmpty() && processed.add(pk)) {
                result.add(findByModelKey(pk));
            }
        }
        return result;
    }

    public int saveOrUpdateModel(ThingModel model) {
        String countSql = "SELECT COUNT(*) FROM iot_thing_model WHERE model_key = ? AND model_type = 'META'";
        Integer count = jdbcTemplate.queryForObject(countSql, Integer.class, model.getModelKey());

        Map<String, Object> schemaMap = new HashMap<>();
        if (model.getCategory() != null) schemaMap.put("category", model.getCategory());
        if (model.getDescription() != null) schemaMap.put("description", model.getDescription());

        if (count != null && count > 0) {
            String updateSql = "UPDATE iot_thing_model SET name = ?, schema_json = ? WHERE model_key = ? AND model_type = 'META'";
            return jdbcTemplate.update(updateSql, model.getModelName(), JSON.toJSONString(schemaMap), model.getModelKey());
        } else {
            String insertSql = "INSERT INTO iot_thing_model (model_key, identifier, name, model_type, data_type, access_mode, unit, schema_json) "
                    + "VALUES (?, '_META_', ?, 'META', 'NONE', 'NONE', NULL, ?)";
            return jdbcTemplate.update(insertSql, model.getModelKey(), model.getModelName(), JSON.toJSONString(schemaMap));
        }
    }

    public int insertProperty(String modelKey, ThingModel.PropertySpec prop) {
        String sql = "INSERT INTO iot_thing_model (model_key, identifier, name, model_type, data_type, access_mode, unit, schema_json) "
                + "VALUES (?, ?, ?, 'PROPERTY', ?, ?, ?, ?)";
        Map<String, Object> schemaMap = new HashMap<>();
        if (prop.getDescription() != null) schemaMap.put("description", prop.getDescription());
        if (prop.getAddress() != null) schemaMap.put("address", prop.getAddress());
        if (prop.getSampleFactor() != null) schemaMap.put("sampleFactor", prop.getSampleFactor());
        if (prop.getTriggerMode() != null) schemaMap.put("triggerMode", prop.getTriggerMode());
        if (prop.getMin() != null) schemaMap.put("min", prop.getMin());
        if (prop.getMax() != null) schemaMap.put("max", prop.getMax());
        if (prop.getEnumOptions() != null && !prop.getEnumOptions().isEmpty()) schemaMap.put("enumOptions", prop.getEnumOptions());

        return jdbcTemplate.update(sql,
                modelKey,
                prop.getPointCode(),
                prop.getPointName(),
                prop.getDataType() != null ? prop.getDataType() : "DOUBLE",
                prop.getAccessMode() != null ? prop.getAccessMode() : "R",
                prop.getUnit(),
                JSON.toJSONString(schemaMap));
    }

    public int updateProperty(String modelKey, String pointCode, ThingModel.PropertySpec prop) {
        String sql = "UPDATE iot_thing_model SET name = ?, data_type = ?, access_mode = ?, unit = ?, schema_json = ? "
                + "WHERE model_key = ? AND identifier = ? AND model_type = 'PROPERTY'";
        Map<String, Object> schemaMap = new HashMap<>();
        if (prop.getDescription() != null) schemaMap.put("description", prop.getDescription());
        if (prop.getAddress() != null) schemaMap.put("address", prop.getAddress());
        if (prop.getSampleFactor() != null) schemaMap.put("sampleFactor", prop.getSampleFactor());
        if (prop.getTriggerMode() != null) schemaMap.put("triggerMode", prop.getTriggerMode());
        if (prop.getMin() != null) schemaMap.put("min", prop.getMin());
        if (prop.getMax() != null) schemaMap.put("max", prop.getMax());
        if (prop.getEnumOptions() != null && !prop.getEnumOptions().isEmpty()) schemaMap.put("enumOptions", prop.getEnumOptions());

        return jdbcTemplate.update(sql,
                prop.getPointName(),
                prop.getDataType(),
                prop.getAccessMode(),
                prop.getUnit(),
                JSON.toJSONString(schemaMap),
                modelKey,
                pointCode);
    }

    public int deleteProperty(String modelKey, String pointCode) {
        String sql = "DELETE FROM iot_thing_model WHERE model_key = ? AND identifier = ? AND model_type = 'PROPERTY'";
        return jdbcTemplate.update(sql, modelKey, pointCode);
    }

    public int insertService(String modelKey, ThingModel.ServiceSpec service) {
        String sql = "INSERT INTO iot_thing_model (model_key, identifier, name, model_type, data_type, access_mode, unit, schema_json) "
                + "VALUES (?, ?, ?, 'SERVICE', 'VOID', 'W', NULL, ?)";
        Map<String, Object> schemaMap = new HashMap<>();
        if (service.getDescription() != null) schemaMap.put("description", service.getDescription());
        if (service.getInputParams() != null) schemaMap.put("inputParams", service.getInputParams());

        return jdbcTemplate.update(sql,
                modelKey,
                service.getServiceCode(),
                service.getServiceName(),
                JSON.toJSONString(schemaMap));
    }

    public int updateService(String modelKey, String serviceCode, ThingModel.ServiceSpec service) {
        String sql = "UPDATE iot_thing_model SET name = ?, schema_json = ? WHERE model_key = ? AND identifier = ? AND model_type = 'SERVICE'";
        Map<String, Object> schemaMap = new HashMap<>();
        if (service.getDescription() != null) schemaMap.put("description", service.getDescription());
        if (service.getInputParams() != null) schemaMap.put("inputParams", service.getInputParams());

        return jdbcTemplate.update(sql,
                service.getServiceName(),
                JSON.toJSONString(schemaMap),
                modelKey,
                serviceCode);
    }

    public int deleteService(String modelKey, String serviceCode) {
        String sql = "DELETE FROM iot_thing_model WHERE model_key = ? AND identifier = ? AND model_type = 'SERVICE'";
        return jdbcTemplate.update(sql, modelKey, serviceCode);
    }

    public int insertEvent(String modelKey, ThingModel.EventSpec event) {
        String sql = "INSERT INTO iot_thing_model (model_key, identifier, name, model_type, data_type, access_mode, unit, schema_json) "
                + "VALUES (?, ?, ?, 'EVENT', 'VOID', 'R', NULL, ?)";
        Map<String, Object> schemaMap = new HashMap<>();
        schemaMap.put("eventType", event.getEventType() != null ? event.getEventType() : "ALERT");
        schemaMap.put("eventLevel", event.getEventLevel() != null ? event.getEventLevel() : "WARN");
        if (event.getDescription() != null) schemaMap.put("description", event.getDescription());

        return jdbcTemplate.update(sql,
                modelKey,
                event.getEventCode(),
                event.getEventName(),
                JSON.toJSONString(schemaMap));
    }

    public int updateEvent(String modelKey, String eventCode, ThingModel.EventSpec event) {
        String sql = "UPDATE iot_thing_model SET name = ?, schema_json = ? WHERE model_key = ? AND identifier = ? AND model_type = 'EVENT'";
        Map<String, Object> schemaMap = new HashMap<>();
        schemaMap.put("eventType", event.getEventType() != null ? event.getEventType() : "ALERT");
        schemaMap.put("eventLevel", event.getEventLevel() != null ? event.getEventLevel() : "WARN");
        if (event.getDescription() != null) schemaMap.put("description", event.getDescription());

        return jdbcTemplate.update(sql,
                event.getEventName(),
                JSON.toJSONString(schemaMap),
                modelKey,
                eventCode);
    }

    public int deleteEvent(String modelKey, String eventCode) {
        String sql = "DELETE FROM iot_thing_model WHERE model_key = ? AND identifier = ? AND model_type = 'EVENT'";
        return jdbcTemplate.update(sql, modelKey, eventCode);
    }

    public int deleteModel(String modelKey) {
        String sql = "DELETE FROM iot_thing_model WHERE model_key = ?";
        return jdbcTemplate.update(sql, modelKey);
    }
}
