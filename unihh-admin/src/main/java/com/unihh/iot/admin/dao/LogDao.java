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

import com.unihh.iot.common.model.IotLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class LogDao {

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    public List<IotLog> findAll(String keyword, String logType, String level) {
        if (jdbcTemplate == null) return new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM iot_log WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (log_id LIKE ? OR device_id LIKE ? OR content LIKE ? OR operator LIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }
        if (logType != null && !logType.trim().isEmpty()) {
            sql.append("AND log_type = ? ");
            params.add(logType.trim());
        }
        if (level != null && !level.trim().isEmpty()) {
            sql.append("AND level = ? ");
            params.add(level.trim());
        }

        sql.append("ORDER BY id DESC LIMIT 500");
        return jdbcTemplate.query(sql.toString(), params.toArray(), new BeanPropertyRowMapper<>(IotLog.class));
    }

    public int insert(IotLog log) {
        if (jdbcTemplate == null) return 0;
        String sql = "INSERT INTO iot_log (log_id, log_type, device_id, model_key, level, content, operator) VALUES (?, ?, ?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql,
                log.getLogId(),
                log.getLogType() != null ? log.getLogType() : "DEVICE",
                log.getDeviceId(),
                log.getModelKey(),
                log.getLevel() != null ? log.getLevel() : "INFO",
                log.getContent(),
                log.getOperator() != null ? log.getOperator() : "SYSTEM"
        );
    }

    public int clearAll() {
        if (jdbcTemplate == null) return 0;
        String sql = "DELETE FROM iot_log";
        return jdbcTemplate.update(sql);
    }
}
