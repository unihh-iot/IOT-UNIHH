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

import com.unihh.iot.common.model.AlarmRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Repository
public class AlarmDao {

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    public List<AlarmRecord> findAll(String keyword, String alarmLevel, Integer status, String ruleId) {
        if (jdbcTemplate == null) return new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM iot_alarm WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (alarm_id LIKE ? OR device_id LIKE ? OR alarm_title LIKE ? OR metric LIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }
        if (alarmLevel != null && !alarmLevel.trim().isEmpty()) {
            sql.append("AND alarm_level = ? ");
            params.add(alarmLevel.trim());
        }
        if (status != null) {
            sql.append("AND status = ? ");
            params.add(status);
        }
        if (ruleId != null && !ruleId.trim().isEmpty()) {
            sql.append("AND rule_id = ? ");
            params.add(ruleId.trim());
        }

        sql.append("ORDER BY id DESC");
        return jdbcTemplate.query(sql.toString(), params.toArray(), new BeanPropertyRowMapper<>(AlarmRecord.class));
    }

    public AlarmRecord findByAlarmId(String alarmId) {
        if (jdbcTemplate == null) return null;
        String sql = "SELECT * FROM iot_alarm WHERE alarm_id = ?";
        List<AlarmRecord> list = jdbcTemplate.query(sql, new Object[]{alarmId}, new BeanPropertyRowMapper<>(AlarmRecord.class));
        return list.isEmpty() ? null : list.get(0);
    }

    public int updateStatus(String alarmId, int status, String handleUser, String remark) {
        if (jdbcTemplate == null) return 0;
        String sql = "UPDATE iot_alarm SET status = ?, handle_time = ?, handle_user = ?, remark = ? WHERE alarm_id = ?";
        return jdbcTemplate.update(sql, status, new Date(), handleUser != null ? handleUser : "admin", remark, alarmId);
    }

    public int insert(AlarmRecord alarm) {
        if (jdbcTemplate == null || alarm == null) return 0;
        String sql = "INSERT INTO iot_alarm (alarm_id, rule_id, device_id, device_name, model_key, metric, alarm_level, alarm_title, trigger_value, threshold_desc, status, alarm_time) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql,
                alarm.getAlarmId(),
                alarm.getRuleId(),
                alarm.getDeviceId(),
                alarm.getDeviceName(),
                alarm.getModelKey(),
                alarm.getMetric(),
                alarm.getAlarmLevel() != null ? alarm.getAlarmLevel() : "WARN",
                alarm.getAlarmTitle(),
                alarm.getTriggerValue(),
                alarm.getThresholdDesc(),
                alarm.getStatus() != null ? alarm.getStatus() : 0,
                alarm.getAlarmTime() != null ? alarm.getAlarmTime() : new Date());
    }

    public int delete(String alarmId) {
        if (jdbcTemplate == null) return 0;
        String sql = "DELETE FROM iot_alarm WHERE alarm_id = ?";
        return jdbcTemplate.update(sql, alarmId);
    }
}
