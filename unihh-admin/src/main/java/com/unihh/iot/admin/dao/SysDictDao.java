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

import com.unihh.iot.admin.model.SysDictData;
import com.unihh.iot.admin.model.SysDictType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Repository
public class SysDictDao {

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    // ---------------- 字典类型 DAO ----------------

    public List<SysDictType> findAllTypes() {
        return findAllTypes(null, null);
    }

    public List<SysDictType> findAllTypes(String keyword, Integer status) {
        if (jdbcTemplate == null) return new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM sys_dict_type WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (dict_name LIKE ? OR dict_type LIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
        }
        if (status != null) {
            sql.append("AND status = ? ");
            params.add(status);
        }
        sql.append("ORDER BY id ASC");
        return jdbcTemplate.query(sql.toString(), params.toArray(), new BeanPropertyRowMapper<>(SysDictType.class));
    }

    public SysDictType findTypeById(Long id) {
        if (jdbcTemplate == null || id == null) return null;
        String sql = "SELECT * FROM sys_dict_type WHERE id = ? LIMIT 1";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{id}, new BeanPropertyRowMapper<>(SysDictType.class));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public SysDictType findTypeByCode(String dictType) {
        if (jdbcTemplate == null || dictType == null) return null;
        String sql = "SELECT * FROM sys_dict_type WHERE dict_type = ? LIMIT 1";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{dictType.trim()}, new BeanPropertyRowMapper<>(SysDictType.class));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public Long insertType(SysDictType type) {
        if (jdbcTemplate == null) return null;
        String sql = "INSERT INTO sys_dict_type (dict_name, dict_type, status, remark) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, type.getDictName());
            ps.setString(2, type.getDictType());
            ps.setInt(3, type.getStatus() != null ? type.getStatus() : 1);
            ps.setString(4, type.getRemark());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            return keyHolder.getKey().longValue();
        }
        return null;
    }

    public int updateType(SysDictType type) {
        if (jdbcTemplate == null || type.getId() == null) return 0;
        String sql = "UPDATE sys_dict_type SET dict_name = ?, status = ?, remark = ? WHERE id = ?";
        return jdbcTemplate.update(sql,
                type.getDictName(),
                type.getStatus() != null ? type.getStatus() : 1,
                type.getRemark(),
                type.getId()
        );
    }

    public int deleteTypeById(Long id) {
        if (jdbcTemplate == null || id == null) return 0;
        SysDictType type = findTypeById(id);
        if (type != null) {
            deleteDataByType(type.getDictType());
        }
        String sql = "DELETE FROM sys_dict_type WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }

    // ---------------- 字典数据 DAO ----------------

    public List<SysDictData> findDataByType(String dictType) {
        if (jdbcTemplate == null || dictType == null) return new ArrayList<>();
        String sql = "SELECT * FROM sys_dict_data WHERE dict_type = ? ORDER BY sort_order ASC, id ASC";
        return jdbcTemplate.query(sql, new Object[]{dictType.trim()}, new BeanPropertyRowMapper<>(SysDictData.class));
    }

    public SysDictData findDataById(Long id) {
        if (jdbcTemplate == null || id == null) return null;
        String sql = "SELECT * FROM sys_dict_data WHERE id = ? LIMIT 1";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{id}, new BeanPropertyRowMapper<>(SysDictData.class));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public Long insertData(SysDictData data) {
        if (jdbcTemplate == null) return null;
        String sql = "INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, sort_order, css_class, list_class, is_default, status, remark) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, data.getDictType());
            ps.setString(2, data.getDictLabel());
            ps.setString(3, data.getDictValue());
            ps.setInt(4, data.getSortOrder() != null ? data.getSortOrder() : 0);
            ps.setString(5, data.getCssClass());
            ps.setString(6, data.getListClass());
            ps.setInt(7, data.getIsDefault() != null ? data.getIsDefault() : 0);
            ps.setInt(8, data.getStatus() != null ? data.getStatus() : 1);
            ps.setString(9, data.getRemark());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            return keyHolder.getKey().longValue();
        }
        return null;
    }

    public int updateData(SysDictData data) {
        if (jdbcTemplate == null || data.getId() == null) return 0;
        String sql = "UPDATE sys_dict_data SET dict_label = ?, dict_value = ?, sort_order = ?, css_class = ?, list_class = ?, is_default = ?, status = ?, remark = ? WHERE id = ?";
        return jdbcTemplate.update(sql,
                data.getDictLabel(),
                data.getDictValue(),
                data.getSortOrder() != null ? data.getSortOrder() : 0,
                data.getCssClass(),
                data.getListClass(),
                data.getIsDefault() != null ? data.getIsDefault() : 0,
                data.getStatus() != null ? data.getStatus() : 1,
                data.getRemark(),
                data.getId()
        );
    }

    public int deleteDataById(Long id) {
        if (jdbcTemplate == null || id == null) return 0;
        String sql = "DELETE FROM sys_dict_data WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }

    public int deleteDataByType(String dictType) {
        if (jdbcTemplate == null || dictType == null) return 0;
        String sql = "DELETE FROM sys_dict_data WHERE dict_type = ?";
        return jdbcTemplate.update(sql, dictType.trim());
    }
}
