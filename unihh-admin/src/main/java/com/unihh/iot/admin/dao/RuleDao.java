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

import com.unihh.iot.common.model.Rule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class RuleDao {

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    public List<Rule> findAll(String keyword, String deviceId, Boolean enabled) {
        if (jdbcTemplate == null) {
            return new ArrayList<>();
        }
        StringBuilder sql = new StringBuilder("SELECT * FROM iot_rule WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (rule_id LIKE ? OR rule_name LIKE ? OR source_point LIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }
        if (deviceId != null && !deviceId.trim().isEmpty()) {
            sql.append("AND source_device_id = ? ");
            params.add(deviceId.trim());
        }
        if (enabled != null) {
            sql.append("AND enabled = ? ");
            params.add(enabled ? 1 : 0);
        }

        sql.append("ORDER BY id DESC");
        return jdbcTemplate.query(sql.toString(), params.toArray(), new BeanPropertyRowMapper<>(Rule.class));
    }

    public Rule findByRuleId(String ruleId) {
        if (jdbcTemplate == null) return null;
        String sql = "SELECT * FROM iot_rule WHERE rule_id = ?";
        List<Rule> list = jdbcTemplate.query(sql, new Object[]{ruleId}, new BeanPropertyRowMapper<>(Rule.class));
        return list.isEmpty() ? null : list.get(0);
    }

    public int insert(Rule rule) {
        if (jdbcTemplate == null) return 0;
        String sql = "INSERT INTO iot_rule (rule_id, rule_name, model_key, source_device_id, source_point, condition_mode, operator, threshold_value, min_val, max_val, enabled, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql,
                rule.getRuleId(),
                rule.getRuleName(),
                rule.getModelKey(),
                rule.getSourceDeviceId(),
                rule.getSourcePoint(),
                rule.getConditionMode() != null ? rule.getConditionMode() : "SINGLE",
                rule.getOperator() != null ? rule.getOperator() : ">",
                rule.getThresholdValue() != null ? rule.getThresholdValue() : 0.0,
                rule.getMinVal() != null ? rule.getMinVal() : 0.0,
                rule.getMaxVal() != null ? rule.getMaxVal() : 0.0,
                Boolean.TRUE.equals(rule.getEnabled()) ? 1 : 0,
                rule.getDescription()
        );
    }

    public int update(Rule rule) {
        if (jdbcTemplate == null) return 0;
        String sql = "UPDATE iot_rule SET rule_name = ?, model_key = ?, source_device_id = ?, source_point = ?, condition_mode = ?, operator = ?, threshold_value = ?, min_val = ?, max_val = ?, enabled = ?, description = ? WHERE rule_id = ?";
        return jdbcTemplate.update(sql,
                rule.getRuleName(),
                rule.getModelKey(),
                rule.getSourceDeviceId(),
                rule.getSourcePoint(),
                rule.getConditionMode(),
                rule.getOperator(),
                rule.getThresholdValue(),
                rule.getMinVal(),
                rule.getMaxVal(),
                Boolean.TRUE.equals(rule.getEnabled()) ? 1 : 0,
                rule.getDescription(),
                rule.getRuleId()
        );
    }

    public int updateEnabled(String ruleId, boolean enabled) {
        if (jdbcTemplate == null) return 0;
        String sql = "UPDATE iot_rule SET enabled = ? WHERE rule_id = ?";
        return jdbcTemplate.update(sql, enabled ? 1 : 0, ruleId);
    }

    public int delete(String ruleId) {
        if (jdbcTemplate == null) return 0;
        String sql = "DELETE FROM iot_rule WHERE rule_id = ?";
        return jdbcTemplate.update(sql, ruleId);
    }
}
