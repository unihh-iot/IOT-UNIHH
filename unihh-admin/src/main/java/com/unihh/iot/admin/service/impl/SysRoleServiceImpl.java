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

package com.unihh.iot.admin.service.impl;

import com.unihh.iot.admin.dao.SysRoleDao;
import com.unihh.iot.admin.model.SysPermission;
import com.unihh.iot.admin.model.SysRole;
import com.unihh.iot.admin.service.SysRoleService;
import com.unihh.iot.admin.security.TokenSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SysRoleServiceImpl implements SysRoleService {

    private static final Logger log = LoggerFactory.getLogger(SysRoleServiceImpl.class);

    @Autowired
    private SysRoleDao sysRoleDao;

    @Autowired(required = false)
    private TokenSessionManager tokenSessionManager;

    @Override
    public List<SysRole> getRoleList(String keyword, Integer status) {
        List<SysRole> list = sysRoleDao.findAll(keyword, status);
        for (SysRole role : list) {
            int count = sysRoleDao.countPermissionsByRoleId(role.getId());
            role.setPermsCount(count);
            role.setPermissionIds(sysRoleDao.findPermissionIdsByRoleId(role.getId()));
            role.setMenuIds(sysRoleDao.findMenuIdsByRoleId(role.getId()));
        }
        return list;
    }

    @Override
    public SysRole getRoleById(Long id) {
        if (id == null) return null;
        SysRole role = sysRoleDao.findById(id);
        if (role != null) {
            role.setPermsCount(sysRoleDao.countPermissionsByRoleId(role.getId()));
            role.setPermissionIds(sysRoleDao.findPermissionIdsByRoleId(role.getId()));
            role.setMenuIds(sysRoleDao.findMenuIdsByRoleId(role.getId()));
        }
        return role;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createRole(SysRole role) {
        if (role == null || role.getRoleKey() == null || role.getRoleKey().trim().isEmpty()) {
            throw new IllegalArgumentException("角色权限标识字符不能为空 (如: operator, viewer)");
        }
        if (role.getRoleName() == null || role.getRoleName().trim().isEmpty()) {
            throw new IllegalArgumentException("角色名称不能为空");
        }

        String roleKey = role.getRoleKey().trim();
        SysRole exist = sysRoleDao.findByRoleKey(roleKey);
        if (exist != null) {
            throw new IllegalArgumentException("角色权限标识已存在: " + roleKey);
        }

        role.setRoleKey(roleKey);
        role.setRoleName(role.getRoleName().trim());
        if (role.getStatus() == null) {
            role.setStatus(1);
        }
        if (role.getSortOrder() == null) {
            role.setSortOrder(0);
        }

        Long roleId = sysRoleDao.insert(role);
        if (roleId != null) {
            if (role.getPermissionIds() != null && !role.getPermissionIds().isEmpty()) {
                sysRoleDao.assignPermissions(roleId, role.getPermissionIds());
            }
            if (role.getMenuIds() != null && !role.getMenuIds().isEmpty()) {
                sysRoleDao.assignMenus(roleId, role.getMenuIds());
            }
        }

        log.info("[角色管理] 成功创建系统角色: roleKey={}, id={}", roleKey, roleId);
        return roleId != null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRole(SysRole role) {
        if (role == null || role.getId() == null) {
            throw new IllegalArgumentException("角色 ID 不能为空");
        }

        SysRole exist = sysRoleDao.findById(role.getId());
        if (exist == null) {
            throw new IllegalArgumentException("角色不存在");
        }

        int rows = sysRoleDao.update(role);
        if (role.getPermissionIds() != null) {
            sysRoleDao.assignPermissions(role.getId(), role.getPermissionIds());
        }
        if (role.getMenuIds() != null) {
            sysRoleDao.assignMenus(role.getId(), role.getMenuIds());
        }
        if (tokenSessionManager != null) {
            tokenSessionManager.refreshAllPermissions();
        }

        log.info("[角色管理] 成功更新系统角色: id={}, roleKey={}", role.getId(), exist.getRoleKey());
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRole(Long id) {
        if (id == null) return false;
        SysRole role = sysRoleDao.findById(id);
        if (role == null) return false;

        if ("admin".equalsIgnoreCase(role.getRoleKey()) || id == 1L) {
            throw new IllegalArgumentException("系统内置最高超级管理员角色不可删除");
        }

        int userCount = sysRoleDao.countUsersByRoleId(id);
        if (userCount > 0) {
            throw new IllegalArgumentException("该角色当前已分配给 " + userCount + " 个用户，不可直接删除");
        }

        int rows = sysRoleDao.deleteById(id);
        log.info("[角色管理] 删除系统角色: id={}, roleKey={}", id, role.getRoleKey());
        return rows > 0;
    }

    @Override
    public boolean updateStatus(Long id, Integer status) {
        if (id == null || status == null) return false;
        SysRole role = sysRoleDao.findById(id);
        if (role != null && ("admin".equalsIgnoreCase(role.getRoleKey()) || id == 1L) && status == 0) {
            throw new IllegalArgumentException("超级管理员角色不可停用");
        }
        int rows = sysRoleDao.updateStatus(id, status);
        log.info("[角色管理] 修改角色状态: id={}, status={}", id, status);
        return rows > 0;
    }

    @Override
    public List<Long> getRolePermissionIds(Long roleId) {
        return sysRoleDao.findPermissionIdsByRoleId(roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignPermissions(Long roleId, List<Long> permissionIds) {
        if (roleId == null) {
            throw new IllegalArgumentException("角色 ID 不能为空");
        }
        SysRole role = sysRoleDao.findById(roleId);
        if (role == null) {
            throw new IllegalArgumentException("角色不存在");
        }
        sysRoleDao.assignPermissions(roleId, permissionIds);
        if (tokenSessionManager != null) {
            tokenSessionManager.refreshAllPermissions();
        }
        log.info("[角色管理] 为角色 [{}] 重新分配了 {} 项功能权限", role.getRoleName(), permissionIds != null ? permissionIds.size() : 0);
        return true;
    }

    @Override
    public List<Long> getRoleMenuIds(Long roleId) {
        return sysRoleDao.findMenuIdsByRoleId(roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignMenus(Long roleId, List<Long> menuIds) {
        if (roleId == null) {
            throw new IllegalArgumentException("角色 ID 不能为空");
        }
        SysRole role = sysRoleDao.findById(roleId);
        if (role == null) {
            throw new IllegalArgumentException("角色不存在");
        }
        sysRoleDao.assignMenus(roleId, menuIds);
        if (tokenSessionManager != null) {
            tokenSessionManager.refreshAllPermissions();
        }
        log.info("[角色管理] 为角色 [{}] 重新分配了 {} 项菜单路由", role.getRoleName(), menuIds != null ? menuIds.size() : 0);
        return true;
    }

    @Override
    public List<SysPermission> getAllPermissions() {
        return sysRoleDao.findAllPermissions();
    }
}
