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

import com.unihh.iot.common.model.RuleAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class RuleActionDao {

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    public List<RuleAction> findByRuleId(String ruleId) {
        if (jdbcTemplate == null || ruleId == null) {
            return new ArrayList<>();
        }
        String sql = "SELECT * FROM iot_rule_action WHERE rule_id = ? ORDER BY sort_order ASC, id ASC";
        return jdbcTemplate.query(sql, new Object[]{ruleId}, new BeanPropertyRowMapper<>(RuleAction.class));
    }

    public List<RuleAction> findByTargetDeviceId(String targetDeviceId) {
        if (jdbcTemplate == null || targetDeviceId == null) {
            return new ArrayList<>();
        }
        String sql = "SELECT * FROM iot_rule_action WHERE target_device_id = ? ORDER BY id ASC";
        return jdbcTemplate.query(sql, new Object[]{targetDeviceId}, new BeanPropertyRowMapper<>(RuleAction.class));
    }

    public int insert(RuleAction action) {
        if (jdbcTemplate == null || action == null) return 0;
        String sql = "INSERT INTO iot_rule_action (rule_id, action_id, action_type, action_name, target_device_id, target_service_code, service_input_values, alarm_level, alarm_content, log_level, log_content, enabled, sort_order) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql,
                action.getRuleId(),
                action.getActionId(),
                action.getActionType() != null ? action.getActionType() : "SERVICE",
                action.getActionName(),
                action.getTargetDeviceId(),
                action.getTargetServiceCode(),
                action.getServiceInputValues(),
                action.getAlarmLevel(),
                action.getAlarmContent(),
                action.getLogLevel(),
                action.getLogContent(),
                Boolean.FALSE.equals(action.getEnabled()) ? 0 : 1,
                action.getSortOrder() != null ? action.getSortOrder() : 0
        );
    }

    public int batchInsert(String ruleId, List<RuleAction> actions) {
        if (jdbcTemplate == null || actions == null || actions.isEmpty()) return 0;
        int count = 0;
        int index = 0;
        for (RuleAction act : actions) {
            act.setRuleId(ruleId);
            if (act.getSortOrder() == null) {
                act.setSortOrder(index++);
            }
            count += insert(act);
        }
        return count;
    }

    public int deleteByRuleId(String ruleId) {
        if (jdbcTemplate == null || ruleId == null) return 0;
        String sql = "DELETE FROM iot_rule_action WHERE rule_id = ?";
        return jdbcTemplate.update(sql, ruleId);
    }
}
