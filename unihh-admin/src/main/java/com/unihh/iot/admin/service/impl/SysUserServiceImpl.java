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

import com.unihh.iot.admin.dao.SysUserDao;
import com.unihh.iot.admin.model.SysRole;
import com.unihh.iot.admin.model.SysUser;
import com.unihh.iot.admin.security.SecurityContextHolder;
import com.unihh.iot.admin.service.SysUserService;
import com.unihh.iot.admin.util.PasswordUtils;
import com.unihh.iot.admin.security.TokenSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SysUserServiceImpl implements SysUserService {

    private static final Logger log = LoggerFactory.getLogger(SysUserServiceImpl.class);

    @Autowired
    private SysUserDao sysUserDao;

    @Autowired(required = false)
    private TokenSessionManager tokenSessionManager;

    @Override
    public List<SysUser> getUserList(String keyword, Integer status) {
        List<SysUser> users = sysUserDao.findAll(keyword, status);
        for (SysUser user : users) {
            List<SysRole> roles = sysUserDao.findRolesByUserId(user.getId());
            user.setRoles(roles);
            List<Long> roleIds = sysUserDao.findRoleIdsByUserId(user.getId());
            user.setRoleIds(roleIds);
        }
        return users;
    }

    @Override
    public SysUser getUserById(Long id) {
        if (id == null) return null;
        SysUser user = sysUserDao.findById(id);
        if (user != null) {
            List<SysRole> roles = sysUserDao.findRolesByUserId(user.getId());
            user.setRoles(roles);
            List<Long> roleIds = sysUserDao.findRoleIdsByUserId(user.getId());
            user.setRoleIds(roleIds);
            List<String> perms = sysUserDao.findPermissionsByUserId(user.getId());
            user.setPermissions(perms);
        }
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createUser(SysUser user) {
        if (user == null || user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("登录用户名不能为空");
        }

        String username = user.getUsername().trim();
        SysUser exist = sysUserDao.findByUsername(username);
        if (exist != null) {
            throw new IllegalArgumentException("用户名已存在: " + username);
        }

        String rawPassword = user.getPassword();
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            rawPassword = "admin" + "123";
        }

        String salt = PasswordUtils.generateSalt();
        String encryptedPassword = PasswordUtils.encrypt(rawPassword.trim(), salt);

        user.setUsername(username);
        user.setPassword(encryptedPassword);
        user.setSalt(salt);
        if (user.getStatus() == null) {
            user.setStatus(1);
        }

        Long userId = sysUserDao.insert(user);
        if (userId != null && user.getRoleIds() != null && !user.getRoleIds().isEmpty()) {
            sysUserDao.assignRoles(userId, user.getRoleIds());
        }

        log.info("[用户管理] 成功创建新系统用户: username={}, id={}", username, userId);
        return userId != null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(SysUser user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("用户 ID 不能为空");
        }

        SysUser exist = sysUserDao.findById(user.getId());
        if (exist == null) {
            throw new IllegalArgumentException("用户不存在或已被删除");
        }

        int rows = sysUserDao.update(user);
        if (user.getRoleIds() != null) {
            sysUserDao.assignRoles(user.getId(), user.getRoleIds());
        }
        if (tokenSessionManager != null) {
            tokenSessionManager.refreshAllPermissions();
        }

        log.info("[用户管理] 成功更新系统用户信息: id={}, username={}", user.getId(), exist.getUsername());
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(Long id) {
        if (id == null) return false;
        if (id == 1L) {
            throw new IllegalArgumentException("系统内置最高超级管理员账号不可删除");
        }

        Long currentUserId = SecurityContextHolder.getUserId();
        if (id.equals(currentUserId)) {
            throw new IllegalArgumentException("不能删除当前正在登录的账号");
        }

        int rows = sysUserDao.deleteById(id);
        log.info("[用户管理] 删除系统用户: id={}", id);
        return rows > 0;
    }

    @Override
    public boolean updateStatus(Long id, Integer status) {
        if (id == null || status == null) return false;
        if (id == 1L && status == 0) {
            throw new IllegalArgumentException("系统内置最高超级管理员账号不可停用");
        }

        Long currentUserId = SecurityContextHolder.getUserId();
        if (id.equals(currentUserId) && status == 0) {
            throw new IllegalArgumentException("不能冻结或停用当前正在登录的账号");
        }

        int rows = sysUserDao.updateStatus(id, status);
        if (tokenSessionManager != null) {
            tokenSessionManager.refreshAllPermissions();
        }
        log.info("[用户管理] 修改用户状态: id={}, status={}", id, status);
        return rows > 0;
    }

    @Override
    public boolean resetPassword(Long id, String newPassword) {
        if (id == null) {
            throw new IllegalArgumentException("用户 ID 不能为空");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("重置密码不能为空");
        }

        SysUser exist = sysUserDao.findById(id);
        if (exist == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        String salt = PasswordUtils.generateSalt();
        String encrypted = PasswordUtils.encrypt(newPassword.trim(), salt);
        int rows = sysUserDao.updatePassword(id, encrypted, salt);

        log.info("[用户管理] 用户密码已成功重置: username={}, id={}", exist.getUsername(), id);
        return rows > 0;
    }
}
