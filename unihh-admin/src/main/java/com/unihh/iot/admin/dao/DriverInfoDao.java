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

import com.unihh.iot.common.model.DriverInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class DriverInfoDao {

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<DriverInfo> driverRowMapper = new RowMapper<DriverInfo>() {
        @Override
        public DriverInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
            DriverInfo info = new DriverInfo();
            info.setId(rs.getLong("id"));
            info.setDriverCode(rs.getString("driver_code"));
            info.setDriverName(rs.getString("driver_name"));
            info.setVersion(rs.getString("version"));
            info.setJarFileName(rs.getString("jar_file_name"));
            info.setStatus(rs.getInt("status"));
            try { info.setDescription(rs.getString("description")); } catch (Exception ignored) {}
            info.setManifestYaml(rs.getString("manifest_yaml"));
            try { info.setServiceUrl(rs.getString("service_url")); } catch (Exception ignored) {}
            info.setCreateTime(rs.getString("create_time"));
            return info;
        }
    };

    public List<DriverInfo> findAll() {
        if (jdbcTemplate == null) return new ArrayList<>();
        String sql = "SELECT * FROM iot_driver_info ORDER BY id ASC";
        return jdbcTemplate.query(sql, driverRowMapper);
    }

    public List<DriverInfo> findPage(String keyword, Integer status, int pageNum, int pageSize) {
        if (jdbcTemplate == null) return new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM iot_driver_info WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (driver_code LIKE ? OR driver_name LIKE ? OR description LIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }
        if (status != null) {
            sql.append("AND status = ? ");
            params.add(status);
        }

        sql.append("ORDER BY id ASC LIMIT ? OFFSET ?");
        int offset = (pageNum - 1) * pageSize;
        params.add(pageSize);
        params.add(offset);
        return jdbcTemplate.query(sql.toString(), params.toArray(), driverRowMapper);
    }

    public int count(String keyword, Integer status) {
        if (jdbcTemplate == null) return 0;
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM iot_driver_info WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (driver_code LIKE ? OR driver_name LIKE ? OR description LIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }
        if (status != null) {
            sql.append("AND status = ? ");
            params.add(status);
        }

        Integer total = jdbcTemplate.queryForObject(sql.toString(), params.toArray(), Integer.class);
        return total != null ? total : 0;
    }

    public DriverInfo findByCode(String driverCode) {
        if (jdbcTemplate == null) return null;
        String sql = "SELECT * FROM iot_driver_info WHERE driver_code = ?";
        List<DriverInfo> list = jdbcTemplate.query(sql, new Object[]{driverCode}, driverRowMapper);
        return list.isEmpty() ? null : list.get(0);
    }

    public int insert(DriverInfo info) {
        if (jdbcTemplate == null) return 0;
        String sql = "INSERT INTO iot_driver_info (driver_code, driver_name, version, jar_file_name, status, description, manifest_yaml, service_url) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql,
                info.getDriverCode(),
                info.getDriverName(),
                info.getVersion() != null ? info.getVersion() : "2.0.0",
                info.getJarFileName(),
                info.getStatus() != null ? info.getStatus() : 1,
                info.getDescription(),
                info.getManifestYaml(),
                info.getServiceUrl());
    }

    public int update(DriverInfo info) {
        if (jdbcTemplate == null) return 0;
        String yaml = info.getManifestYaml();
        if (yaml == null || yaml.trim().isEmpty()) {
            String sql = "UPDATE iot_driver_info SET driver_name = ?, version = ?, description = ?, service_url = ?, status = ? WHERE driver_code = ?";
            return jdbcTemplate.update(sql,
                    info.getDriverName(),
                    info.getVersion() != null ? info.getVersion() : "2.0.0",
                    info.getDescription(),
                    info.getServiceUrl(),
                    info.getStatus() != null ? info.getStatus() : 1,
                    info.getDriverCode());
        }
        String sql = "UPDATE iot_driver_info SET driver_name = ?, version = ?, description = ?, manifest_yaml = ?, service_url = ?, status = ? WHERE driver_code = ?";
        return jdbcTemplate.update(sql,
                info.getDriverName(),
                info.getVersion() != null ? info.getVersion() : "2.0.0",
                info.getDescription(),
                yaml,
                info.getServiceUrl(),
                info.getStatus() != null ? info.getStatus() : 1,
                info.getDriverCode());
    }

    public int updateStatus(String driverCode, int status) {
        if (jdbcTemplate == null) return 0;
        String sql = "UPDATE iot_driver_info SET status = ? WHERE driver_code = ?";
        return jdbcTemplate.update(sql, status, driverCode);
    }

    public int updateAllStatus(int status) {
        if (jdbcTemplate == null) return 0;
        String sql = "UPDATE iot_driver_info SET status = ?";
        return jdbcTemplate.update(sql, status);
    }

    public int deleteByCode(String driverCode) {
        if (jdbcTemplate == null) return 0;
        String sql = "DELETE FROM iot_driver_info WHERE driver_code = ?";
        return jdbcTemplate.update(sql, driverCode);
    }
}
