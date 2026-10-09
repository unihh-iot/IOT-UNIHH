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

import com.unihh.iot.admin.model.SysMenu;
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
public class SysMenuDao {

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    public List<SysMenu> findAll() {
        return findAll(null, null);
    }

    public List<SysMenu> findAll(String keyword, Integer status) {
        if (jdbcTemplate == null) return new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM sys_menu WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (menu_name LIKE ? OR path LIKE ? OR perms LIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }
        if (status != null) {
            sql.append("AND status = ? ");
            params.add(status);
        }
        sql.append("ORDER BY sort_order ASC, id ASC");
        return jdbcTemplate.query(sql.toString(), params.toArray(), new BeanPropertyRowMapper<>(SysMenu.class));
    }

    public SysMenu findById(Long id) {
        if (jdbcTemplate == null || id == null) return null;
        String sql = "SELECT * FROM sys_menu WHERE id = ? LIMIT 1";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{id}, new BeanPropertyRowMapper<>(SysMenu.class));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public int countChildren(Long parentId) {
        if (jdbcTemplate == null || parentId == null) return 0;
        String sql = "SELECT COUNT(*) FROM sys_menu WHERE parent_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, new Object[]{parentId}, Integer.class);
        return count != null ? count : 0;
    }

    public List<SysMenu> findMenusByUserId(Long userId) {
        if (jdbcTemplate == null || userId == null) return new ArrayList<>();
        // 管理员(role_key='admin')返回全部启用菜单
        String checkAdminSql = "SELECT COUNT(*) FROM sys_user_role ur "
                + "INNER JOIN sys_role r ON ur.role_id = r.id "
                + "WHERE ur.user_id = ? AND r.role_key = 'admin'";
        Integer adminCount = jdbcTemplate.queryForObject(checkAdminSql, new Object[]{userId}, Integer.class);
        if (adminCount != null && adminCount > 0) {
            return jdbcTemplate.query("SELECT * FROM sys_menu WHERE status = 1 ORDER BY sort_order ASC, id ASC", new BeanPropertyRowMapper<>(SysMenu.class));
        }

        // 普通用户根据角色关联菜单返回 (并联其父级目录以防断链)
        String sql = "SELECT DISTINCT m.* FROM sys_menu m "
                + "INNER JOIN sys_role_menu rm ON m.id = rm.menu_id "
                + "INNER JOIN sys_user_role ur ON rm.role_id = ur.role_id "
                + "WHERE ur.user_id = ? AND m.status = 1 "
                + "UNION "
                + "SELECT DISTINCT p.* FROM sys_menu p "
                + "INNER JOIN sys_menu c ON p.id = c.parent_id "
                + "INNER JOIN sys_role_menu rm ON c.id = rm.menu_id "
                + "INNER JOIN sys_user_role ur ON rm.role_id = ur.role_id "
                + "WHERE ur.user_id = ? AND p.status = 1 "
                + "ORDER BY sort_order ASC, id ASC";
        return jdbcTemplate.query(sql, new Object[]{userId, userId}, new BeanPropertyRowMapper<>(SysMenu.class));
    }

    public Long insert(SysMenu menu) {
        if (jdbcTemplate == null) return null;
        String sql = "INSERT INTO sys_menu (parent_id, menu_name, path, component, icon, sort_order, menu_type, perms, visible, status, remark) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, menu.getParentId() != null ? menu.getParentId() : 0L);
            ps.setString(2, menu.getMenuName());
            ps.setString(3, menu.getPath());
            ps.setString(4, menu.getComponent());
            ps.setString(5, menu.getIcon());
            ps.setInt(6, menu.getSortOrder() != null ? menu.getSortOrder() : 0);
            ps.setString(7, menu.getMenuType() != null ? menu.getMenuType() : "C");
            ps.setString(8, menu.getPerms());
            ps.setInt(9, menu.getVisible() != null ? menu.getVisible() : 1);
            ps.setInt(10, menu.getStatus() != null ? menu.getStatus() : 1);
            ps.setString(11, menu.getRemark());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            return keyHolder.getKey().longValue();
        }
        return null;
    }

    public int update(SysMenu menu) {
        if (jdbcTemplate == null || menu.getId() == null) return 0;
        String sql = "UPDATE sys_menu SET parent_id = ?, menu_name = ?, path = ?, component = ?, icon = ?, sort_order = ?, menu_type = ?, perms = ?, visible = ?, status = ?, remark = ? WHERE id = ?";
        return jdbcTemplate.update(sql,
                menu.getParentId() != null ? menu.getParentId() : 0L,
                menu.getMenuName(),
                menu.getPath(),
                menu.getComponent(),
                menu.getIcon(),
                menu.getSortOrder() != null ? menu.getSortOrder() : 0,
                menu.getMenuType() != null ? menu.getMenuType() : "C",
                menu.getPerms(),
                menu.getVisible() != null ? menu.getVisible() : 1,
                menu.getStatus() != null ? menu.getStatus() : 1,
                menu.getRemark(),
                menu.getId()
        );
    }

    public int deleteById(Long id) {
        if (jdbcTemplate == null || id == null) return 0;
        jdbcTemplate.update("DELETE FROM sys_role_menu WHERE menu_id = ?", id);
        return jdbcTemplate.update("DELETE FROM sys_menu WHERE id = ?", id);
    }
}
