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

import com.unihh.iot.common.model.DevicePointMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class DevicePointMappingDao {

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    public List<DevicePointMapping> findByDeviceId(String deviceId) {
        if (jdbcTemplate == null) return new ArrayList<>();
        String sql = "SELECT * FROM iot_device_point_mapping WHERE device_id = ? ORDER BY id ASC";
        return jdbcTemplate.query(sql, new Object[]{deviceId}, new BeanPropertyRowMapper<>(DevicePointMapping.class));
    }

    public int saveOrUpdateBatch(String deviceId, String modelKey, List<DevicePointMapping> mappings) {
        if (jdbcTemplate == null || mappings == null) return 0;
        int count = 0;
        for (DevicePointMapping m : mappings) {
            String sql = "INSERT INTO iot_device_point_mapping (device_id, model_key, point_code, point_name, physical_address, data_type, access_mode, scale, offset, remark) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
                    + "ON DUPLICATE KEY UPDATE point_name = VALUES(point_name), physical_address = VALUES(physical_address), data_type = VALUES(data_type), access_mode = VALUES(access_mode), scale = VALUES(scale), offset = VALUES(offset), remark = VALUES(remark)";
            count += jdbcTemplate.update(sql,
                    deviceId,
                    modelKey != null ? modelKey : m.getModelKey(),
                    m.getPointCode(),
                    m.getPointName(),
                    m.getPhysicalAddress(),
                    m.getDataType() != null ? m.getDataType() : "DOUBLE",
                    m.getAccessMode() != null ? m.getAccessMode() : "R",
                    m.getScale() != null ? m.getScale() : 1.0,
                    m.getOffset() != null ? m.getOffset() : 0.0,
                    m.getRemark()
            );
        }
        return count;
    }

    public int deleteByDeviceId(String deviceId) {
        if (jdbcTemplate == null) return 0;
        String sql = "DELETE FROM iot_device_point_mapping WHERE device_id = ?";
        return jdbcTemplate.update(sql, deviceId);
    }

    public int updatePropertySpecByModelKey(String modelKey, String pointCode, String dataType, String pointName, String accessMode) {
        if (jdbcTemplate == null) return 0;
        String sql = "UPDATE iot_device_point_mapping SET data_type = ?, point_name = ?, access_mode = ? WHERE model_key = ? AND point_code = ?";
        return jdbcTemplate.update(sql, dataType != null ? dataType : "DOUBLE", pointName, accessMode != null ? accessMode : "R", modelKey, pointCode);
    }
}
