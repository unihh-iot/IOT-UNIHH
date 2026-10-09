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

import com.unihh.iot.admin.model.SysPermission;
import com.unihh.iot.admin.model.SysRole;
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
public class SysRoleDao {

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    public List<SysRole> findAll() {
        return findAll(null, null);
    }

    public List<SysRole> findAll(String keyword, Integer status) {
        if (jdbcTemplate == null) return new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM sys_role WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (role_name LIKE ? OR role_key LIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
        }
        if (status != null) {
            sql.append("AND status = ? ");
            params.add(status);
        }
        sql.append("ORDER BY sort_order ASC, id ASC");
        return jdbcTemplate.query(sql.toString(), params.toArray(), new BeanPropertyRowMapper<>(SysRole.class));
    }

    public SysRole findById(Long id) {
        if (jdbcTemplate == null || id == null) return null;
        String sql = "SELECT * FROM sys_role WHERE id = ? LIMIT 1";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{id}, new BeanPropertyRowMapper<>(SysRole.class));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public SysRole findByRoleKey(String roleKey) {
        if (jdbcTemplate == null || roleKey == null) return null;
        String sql = "SELECT * FROM sys_role WHERE role_key = ? LIMIT 1";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{roleKey.trim()}, new BeanPropertyRowMapper<>(SysRole.class));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public Long insert(SysRole role) {
        if (jdbcTemplate == null) return null;
        String sql = "INSERT INTO sys_role (role_key, role_name, sort_order, status, remark) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, role.getRoleKey());
            ps.setString(2, role.getRoleName());
            ps.setInt(3, role.getSortOrder() != null ? role.getSortOrder() : 0);
            ps.setInt(4, role.getStatus() != null ? role.getStatus() : 1);
            ps.setString(5, role.getRemark());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            return keyHolder.getKey().longValue();
        }
        return null;
    }

    public int update(SysRole role) {
        if (jdbcTemplate == null || role.getId() == null) return 0;
        String sql = "UPDATE sys_role SET role_name = ?, sort_order = ?, status = ?, remark = ? WHERE id = ?";
        return jdbcTemplate.update(sql,
                role.getRoleName(),
                role.getSortOrder() != null ? role.getSortOrder() : 0,
                role.getStatus() != null ? role.getStatus() : 1,
                role.getRemark(),
                role.getId()
        );
    }

    public int updateStatus(Long id, Integer status) {
        if (jdbcTemplate == null || id == null) return 0;
        String sql = "UPDATE sys_role SET status = ? WHERE id = ?";
        return jdbcTemplate.update(sql, status, id);
    }

    public int deleteById(Long id) {
        if (jdbcTemplate == null || id == null) return 0;
        clearPermissionsByRoleId(id);
        clearMenusByRoleId(id);
        String sql = "DELETE FROM sys_role WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }

    public int countUsersByRoleId(Long roleId) {
        if (jdbcTemplate == null || roleId == null) return 0;
        String sql = "SELECT COUNT(*) FROM sys_user_role WHERE role_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, new Object[]{roleId}, Integer.class);
        return count != null ? count : 0;
    }

    // ---------------- 角色权限关联操作 ----------------

    public List<Long> findPermissionIdsByRoleId(Long roleId) {
        if (jdbcTemplate == null || roleId == null) return new ArrayList<>();
        String sql = "SELECT permission_id FROM sys_role_permission WHERE role_id = ?";
        return jdbcTemplate.queryForList(sql, new Object[]{roleId}, Long.class);
    }

    public int countPermissionsByRoleId(Long roleId) {
        if (jdbcTemplate == null || roleId == null) return 0;
        String sql = "SELECT COUNT(*) FROM sys_role_permission WHERE role_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, new Object[]{roleId}, Integer.class);
        return count != null ? count : 0;
    }

    public int clearPermissionsByRoleId(Long roleId) {
        if (jdbcTemplate == null || roleId == null) return 0;
        String sql = "DELETE FROM sys_role_permission WHERE role_id = ?";
        return jdbcTemplate.update(sql, roleId);
    }

    public int assignPermission(Long roleId, Long permissionId) {
        if (jdbcTemplate == null || roleId == null || permissionId == null) return 0;
        String sql = "INSERT IGNORE INTO sys_role_permission (role_id, permission_id) VALUES (?, ?)";
        return jdbcTemplate.update(sql, roleId, permissionId);
    }

    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        if (jdbcTemplate == null || roleId == null) return;
        clearPermissionsByRoleId(roleId);
        if (permissionIds != null && !permissionIds.isEmpty()) {
            for (Long permId : permissionIds) {
                assignPermission(roleId, permId);
            }
        }
    }

    public List<SysPermission> findAllPermissions() {
        if (jdbcTemplate == null) return new ArrayList<>();
        String sql = "SELECT * FROM sys_permission ORDER BY id ASC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(SysPermission.class));
    }

    // ---------------- 角色菜单关联操作 ----------------

    public List<Long> findMenuIdsByRoleId(Long roleId) {
        if (jdbcTemplate == null || roleId == null) return new ArrayList<>();
        String sql = "SELECT menu_id FROM sys_role_menu WHERE role_id = ?";
        return jdbcTemplate.queryForList(sql, new Object[]{roleId}, Long.class);
    }

    public int clearMenusByRoleId(Long roleId) {
        if (jdbcTemplate == null || roleId == null) return 0;
        String sql = "DELETE FROM sys_role_menu WHERE role_id = ?";
        return jdbcTemplate.update(sql, roleId);
    }

    public int assignMenu(Long roleId, Long menuId) {
        if (jdbcTemplate == null || roleId == null || menuId == null) return 0;
        String sql = "INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (?, ?)";
        return jdbcTemplate.update(sql, roleId, menuId);
    }

    public void assignMenus(Long roleId, List<Long> menuIds) {
        if (jdbcTemplate == null || roleId == null) return;
        clearMenusByRoleId(roleId);
        if (menuIds != null && !menuIds.isEmpty()) {
            for (Long menuId : menuIds) {
                assignMenu(roleId, menuId);
            }
        }
    }
}
