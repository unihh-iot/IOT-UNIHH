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

import com.unihh.iot.admin.model.SysRole;
import com.unihh.iot.admin.model.SysUser;
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
public class SysUserDao {

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    public SysUser findByUsername(String username) {
        if (jdbcTemplate == null || username == null) return null;
        String sql = "SELECT * FROM sys_user WHERE username = ? LIMIT 1";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{username.trim()}, new BeanPropertyRowMapper<>(SysUser.class));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public SysUser findById(Long id) {
        if (jdbcTemplate == null || id == null) return null;
        String sql = "SELECT * FROM sys_user WHERE id = ? LIMIT 1";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{id}, new BeanPropertyRowMapper<>(SysUser.class));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<SysRole> findRolesByUserId(Long userId) {
        if (jdbcTemplate == null || userId == null) return new ArrayList<>();
        String sql = "SELECT r.* FROM sys_role r "
                + "INNER JOIN sys_user_role ur ON r.id = ur.role_id "
                + "WHERE ur.user_id = ? AND r.status = 1 "
                + "ORDER BY r.sort_order ASC";
        return jdbcTemplate.query(sql, new Object[]{userId}, new BeanPropertyRowMapper<>(SysRole.class));
    }

    public List<Long> findRoleIdsByUserId(Long userId) {
        if (jdbcTemplate == null || userId == null) return new ArrayList<>();
        String sql = "SELECT role_id FROM sys_user_role WHERE user_id = ?";
        return jdbcTemplate.queryForList(sql, new Object[]{userId}, Long.class);
    }

    public List<String> findPermissionsByUserId(Long userId) {
        if (jdbcTemplate == null || userId == null) return new ArrayList<>();
        String sql = "SELECT DISTINCT p.perm_key FROM sys_permission p "
                + "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id "
                + "INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id "
                + "WHERE ur.user_id = ?";
        return jdbcTemplate.queryForList(sql, new Object[]{userId}, String.class);
    }

    public void updateLastLogin(Long userId, String ip) {
        if (jdbcTemplate == null || userId == null) return;
        String sql = "UPDATE sys_user SET last_login_ip = ?, last_login_time = NOW() WHERE id = ?";
        jdbcTemplate.update(sql, ip, userId);
    }

    public List<SysUser> findAll(String keyword, Integer status) {
        if (jdbcTemplate == null) return new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT id, username, nickname, email, phone, avatar, status, last_login_ip, last_login_time, remark, create_time, update_time FROM sys_user WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (username LIKE ? OR nickname LIKE ? OR phone LIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }
        if (status != null) {
            sql.append("AND status = ? ");
            params.add(status);
        }
        sql.append("ORDER BY id ASC");
        return jdbcTemplate.query(sql.toString(), params.toArray(), new BeanPropertyRowMapper<>(SysUser.class));
    }

    public Long insert(SysUser user) {
        if (jdbcTemplate == null) return null;
        String sql = "INSERT INTO sys_user (username, nickname, password, salt, email, phone, avatar, status, remark) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getNickname());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getSalt());
            ps.setString(5, user.getEmail());
            ps.setString(6, user.getPhone());
            ps.setString(7, user.getAvatar());
            ps.setInt(8, user.getStatus() != null ? user.getStatus() : 1);
            ps.setString(9, user.getRemark());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            return keyHolder.getKey().longValue();
        }
        return null;
    }

    public int update(SysUser user) {
        if (jdbcTemplate == null || user.getId() == null) return 0;
        String sql = "UPDATE sys_user SET nickname = ?, email = ?, phone = ?, avatar = ?, status = ?, remark = ? WHERE id = ?";
        return jdbcTemplate.update(sql,
                user.getNickname(),
                user.getEmail(),
                user.getPhone(),
                user.getAvatar(),
                user.getStatus() != null ? user.getStatus() : 1,
                user.getRemark(),
                user.getId()
        );
    }

    public int updateStatus(Long id, Integer status) {
        if (jdbcTemplate == null || id == null) return 0;
        String sql = "UPDATE sys_user SET status = ? WHERE id = ?";
        return jdbcTemplate.update(sql, status, id);
    }

    public int updatePassword(Long id, String password, String salt) {
        if (jdbcTemplate == null || id == null) return 0;
        String sql = "UPDATE sys_user SET password = ?, salt = ? WHERE id = ?";
        return jdbcTemplate.update(sql, password, salt, id);
    }

    public int deleteById(Long id) {
        if (jdbcTemplate == null || id == null) return 0;
        clearRolesByUserId(id);
        String sql = "DELETE FROM sys_user WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }

    public int clearRolesByUserId(Long userId) {
        if (jdbcTemplate == null || userId == null) return 0;
        String sql = "DELETE FROM sys_user_role WHERE user_id = ?";
        return jdbcTemplate.update(sql, userId);
    }

    public int assignRole(Long userId, Long roleId) {
        if (jdbcTemplate == null || userId == null || roleId == null) return 0;
        String sql = "INSERT IGNORE INTO sys_user_role (user_id, role_id) VALUES (?, ?)";
        return jdbcTemplate.update(sql, userId, roleId);
    }

    public void assignRoles(Long userId, List<Long> roleIds) {
        if (jdbcTemplate == null || userId == null) return;
        clearRolesByUserId(userId);
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                assignRole(userId, roleId);
            }
        }
    }
}
