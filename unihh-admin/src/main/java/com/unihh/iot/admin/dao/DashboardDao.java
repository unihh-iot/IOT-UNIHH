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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class DashboardDao {

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    public int countTotalDrivers() {
        if (jdbcTemplate == null) return 0;
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM iot_driver_info", Integer.class);
            return count != null ? count : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    public int countActiveDrivers() {
        if (jdbcTemplate == null) return 0;
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM iot_driver_info WHERE status = 1", Integer.class);
            return count != null ? count : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    public int countTotalDevices() {
        if (jdbcTemplate == null) return 0;
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM iot_device", Integer.class);
            return count != null ? count : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    public int countDevicesByStatus(int status) {
        if (jdbcTemplate == null) return 0;
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM iot_device WHERE status = ?", Integer.class, status);
            return count != null ? count : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    public int countDistinctModelKeys() {
        if (jdbcTemplate == null) return 0;
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(DISTINCT model_key) FROM iot_thing_model", Integer.class);
            return count != null ? count : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    public int countThingModelsByType(String modelType) {
        if (jdbcTemplate == null) return 0;
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM iot_thing_model WHERE model_type = ?", Integer.class, modelType);
            return count != null ? count : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    public int countTotalRules() {
        if (jdbcTemplate == null) return 0;
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM iot_rule", Integer.class);
            return count != null ? count : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    public int countRulesByEnabled(int enabled) {
        if (jdbcTemplate == null) return 0;
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM iot_rule WHERE enabled = ?", Integer.class, enabled);
            return count != null ? count : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    public int countTotalAlarms() {
        if (jdbcTemplate == null) return 0;
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM iot_alarm", Integer.class);
            return count != null ? count : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    public int countAlarmsByStatus(int status) {
        if (jdbcTemplate == null) return 0;
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM iot_alarm WHERE status = ?", Integer.class, status);
            return count != null ? count : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    public int countAlarmsByLevel(String level) {
        if (jdbcTemplate == null) return 0;
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM iot_alarm WHERE alarm_level = ?", Integer.class, level);
            return count != null ? count : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    public int countTotalLogs() {
        if (jdbcTemplate == null) return 0;
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM iot_log", Integer.class);
            return count != null ? count : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    public int countPointData() {
        if (jdbcTemplate == null) return 0;
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM iot_device_point_data", Integer.class);
            return count != null ? count : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    public List<Map<String, Object>> getDeviceProtocolDistribution() {
        if (jdbcTemplate == null) return Collections.emptyList();
        try {
            String sql = "SELECT COALESCE(driver_code, 'other') as driverCode, COUNT(*) as count FROM iot_device GROUP BY driver_code";
            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> getAlarmLevelDistribution() {
        if (jdbcTemplate == null) return Collections.emptyList();
        try {
            String sql = "SELECT alarm_level as level, COUNT(*) as count FROM iot_alarm GROUP BY alarm_level";
            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> getLogTypeDistribution() {
        if (jdbcTemplate == null) return Collections.emptyList();
        try {
            String sql = "SELECT log_type as logType, COUNT(*) as count FROM iot_log GROUP BY log_type";
            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
