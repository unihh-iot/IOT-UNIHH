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
import com.alibaba.fastjson2.TypeReference;
import com.unihh.iot.common.model.DeviceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class DeviceDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<DeviceConfig> deviceRowMapper = new RowMapper<DeviceConfig>() {
        @Override
        public DeviceConfig mapRow(ResultSet rs, int rowNum) throws SQLException {
            DeviceConfig device = new DeviceConfig();
            device.setId(rs.getLong("id"));
            device.setDeviceId(rs.getString("device_id"));
            device.setDeviceName(rs.getString("device_name"));
            device.setModelKey(rs.getString("model_key"));
            device.setDriverCode(rs.getString("driver_code"));
            device.setHost(rs.getString("host"));
            device.setPort(rs.getInt("port"));
            device.setStatus(rs.getInt("status"));
            device.setAcqStatus(rs.getInt("acq_status"));
            try {
                int baseInt = rs.getInt("base_interval_ms");
                if (!rs.wasNull() && baseInt > 0) {
                    device.setBaseIntervalMs(baseInt);
                }
            } catch (Exception ignored) {}
            device.setLastOnlineTime(rs.getString("last_online_time"));
            device.setCreateTime(rs.getString("create_time"));
            device.setUpdateTime(rs.getString("update_time"));

            String connParamsJson = rs.getString("connection_params");
            if (connParamsJson != null && !connParamsJson.trim().isEmpty()) {
                try {
                    Map<String, Object> map = JSON.parseObject(connParamsJson, new TypeReference<Map<String, Object>>() {});
                    if (map != null) {
                        device.setConnectionParams(map);
                    }
                } catch (Exception ignored) {}
            }
            return device;
        }
    };

    public List<DeviceConfig> findAll(String keyword, String driverCode, Integer acqStatus) {
        StringBuilder sql = new StringBuilder("SELECT * FROM iot_device WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (device_id LIKE ? OR device_name LIKE ?) ");
            params.add("%" + keyword.trim() + "%");
            params.add("%" + keyword.trim() + "%");
        }
        if (driverCode != null && !driverCode.trim().isEmpty()) {
            sql.append("AND driver_code = ? ");
            params.add(driverCode.trim());
        }
        if (acqStatus != null) {
            sql.append("AND acq_status = ? ");
            params.add(acqStatus);
        }
        sql.append("ORDER BY id DESC");

        return jdbcTemplate.query(sql.toString(), params.toArray(), deviceRowMapper);
    }

    public DeviceConfig findById(String deviceId) {
        String sql = "SELECT * FROM iot_device WHERE device_id = ?";
        List<DeviceConfig> list = jdbcTemplate.query(sql, new Object[]{deviceId}, deviceRowMapper);
        return list.isEmpty() ? null : list.get(0);
    }

    public int insert(DeviceConfig device) {
        String sql = "INSERT INTO iot_device (device_id, device_name, model_key, driver_code, host, port, connection_params, status, acq_status, base_interval_ms) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String connJson = device.getConnectionParams() != null ? JSON.toJSONString(device.getConnectionParams()) : "{}";
        try {
            return jdbcTemplate.update(sql,
                    device.getDeviceId(),
                    device.getDeviceName(),
                    device.getModelKey(),
                    device.getDriverCode(),
                    device.getHost() != null ? device.getHost() : "192.168.1.100",
                    device.getPort() > 0 ? device.getPort() : 502,
                    connJson,
                    device.getStatus() != null ? device.getStatus() : 1,
                    device.getAcqStatus() != null ? device.getAcqStatus() : 1,
                    device.getBaseIntervalMs() != null ? device.getBaseIntervalMs() : 1000);
        } catch (Exception e) {
            String fallbackSql = "INSERT INTO iot_device (device_id, device_name, model_key, driver_code, host, port, connection_params, status, acq_status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            return jdbcTemplate.update(fallbackSql, device.getDeviceId(), device.getDeviceName(), device.getModelKey(), device.getDriverCode(), device.getHost(), device.getPort(), connJson, device.getStatus(), device.getAcqStatus());
        }
    }

    public int update(DeviceConfig device) {
        String sql = "UPDATE iot_device SET device_name = ?, model_key = ?, driver_code = ?, host = ?, port = ?, connection_params = ?, status = ?, acq_status = ?, base_interval_ms = ? WHERE device_id = ?";
        String connJson = device.getConnectionParams() != null ? JSON.toJSONString(device.getConnectionParams()) : "{}";
        try {
            return jdbcTemplate.update(sql,
                    device.getDeviceName(),
                    device.getModelKey(),
                    device.getDriverCode(),
                    device.getHost(),
                    device.getPort(),
                    connJson,
                    device.getStatus(),
                    device.getAcqStatus(),
                    device.getBaseIntervalMs() != null ? device.getBaseIntervalMs() : 1000,
                    device.getDeviceId());
        } catch (Exception e) {
            String fallbackSql = "UPDATE iot_device SET device_name = ?, model_key = ?, driver_code = ?, host = ?, port = ?, connection_params = ?, status = ?, acq_status = ? WHERE device_id = ?";
            return jdbcTemplate.update(fallbackSql, device.getDeviceName(), device.getModelKey(), device.getDriverCode(), device.getHost(), device.getPort(), connJson, device.getStatus(), device.getAcqStatus(), device.getDeviceId());
        }
    }

    public int updateAcqStatus(String deviceId, int acqStatus) {
        String sql = "UPDATE iot_device SET acq_status = ? WHERE device_id = ?";
        return jdbcTemplate.update(sql, acqStatus, deviceId);
    }

    public int updateStatus(String deviceId, int status) {
        if (status == 1) {
            String sql = "UPDATE iot_device SET status = ?, last_online_time = NOW() WHERE device_id = ?";
            return jdbcTemplate.update(sql, status, deviceId);
        } else {
            String sql = "UPDATE iot_device SET status = ? WHERE device_id = ?";
            return jdbcTemplate.update(sql, status, deviceId);
        }
    }

    public int updateStatusByDriverCode(String driverCode, int status) {
        if (driverCode == null || driverCode.trim().isEmpty()) return 0;
        String sql = "UPDATE iot_device SET status = ? WHERE driver_code = ?";
        return jdbcTemplate.update(sql, status, driverCode.trim());
    }

    public int updateAllStatus(int status) {
        if (jdbcTemplate == null) return 0;
        String sql = "UPDATE iot_device SET status = ?";
        return jdbcTemplate.update(sql, status);
    }

    public int countByDriverCode(String driverCode) {
        if (driverCode == null || driverCode.trim().isEmpty() || jdbcTemplate == null) return 0;
        String sql = "SELECT COUNT(*) FROM iot_device WHERE driver_code = ?";
        try {
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, driverCode.trim());
            return count != null ? count : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    public int deleteById(String deviceId) {
        String sql = "DELETE FROM iot_device WHERE device_id = ?";
        return jdbcTemplate.update(sql, deviceId);
    }

    public Map<String, Integer> countDevicesGroupedByModel() {
        String sql = "SELECT model_key, COUNT(*) as cnt FROM iot_device GROUP BY model_key";
        Map<String, Integer> map = new HashMap<>();
        try {
            jdbcTemplate.query(sql, rs -> {
                String mk = rs.getString("model_key");
                if (mk != null) {
                    map.put(mk, rs.getInt("cnt"));
                }
            });
        } catch (Exception ignored) {}
        return map;
    }

    public int countByModelKey(String modelKey) {
        if (modelKey == null || modelKey.trim().isEmpty()) {
            return 0;
        }
        String sql = "SELECT COUNT(*) FROM iot_device WHERE model_key = ?";
        try {
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, modelKey.trim());
            return count != null ? count : 0;
        } catch (Exception e) {
            return 0;
        }
    }
}
