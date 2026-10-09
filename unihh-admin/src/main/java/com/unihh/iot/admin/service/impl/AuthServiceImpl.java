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

import com.unihh.iot.admin.dao.SysMenuDao;
import com.unihh.iot.admin.dao.SysUserDao;
import com.unihh.iot.admin.model.LoginRequest;
import com.unihh.iot.admin.model.LoginResponse;
import com.unihh.iot.admin.model.SysMenu;
import com.unihh.iot.admin.model.SysRole;
import com.unihh.iot.admin.model.SysUser;
import com.unihh.iot.admin.service.AuthService;
import com.unihh.iot.admin.util.PasswordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    @Autowired
    private SysUserDao sysUserDao;

    @Autowired
    private SysMenuDao sysMenuDao;

    @Autowired
    private com.unihh.iot.admin.security.TokenSessionManager tokenSessionManager;

    @Override
    public LoginResponse login(LoginRequest request, String clientIp) {
        if (request == null || request.getUsername() == null || request.getPassword() == null) {
            throw new IllegalArgumentException("登录用户名或密码不能为空");
        }

        String username = request.getUsername().trim();
        SysUser user = sysUserDao.findByUsername(username);

        if (user == null) {
            log.warn("[用户登录失败] 用户不存在: username={}, IP={}", username, clientIp);
            throw new RuntimeException("用户名或密码错误");
        }

        if (user.getStatus() != null && user.getStatus() == 0) {
            log.warn("[用户登录失败] 账号已被停用: username={}, IP={}", username, clientIp);
            throw new RuntimeException("该账号已被冻结或停用，请联系管理员");
        }

        // 加盐比对密码哈希
        boolean match = PasswordUtils.matches(request.getPassword(), user.getSalt(), user.getPassword());
        if (!match) {
            log.warn("[用户登录失败] 密码错误: username={}, IP={}", username, clientIp);
            throw new RuntimeException("用户名或密码错误");
        }

        // 更新最后登录记录
        sysUserDao.updateLastLogin(user.getId(), clientIp);

        // 获取用户角色
        List<SysRole> roles = sysUserDao.findRolesByUserId(user.getId());
        user.setRoles(roles);
        List<String> roleKeys = roles.stream().map(SysRole::getRoleKey).collect(Collectors.toList());

        // 获取用户权限
        List<String> permissions = sysUserDao.findPermissionsByUserId(user.getId());
        user.setPermissions(permissions);

        // 获取用户关联菜单列表
        List<SysMenu> menus = sysMenuDao.findMenusByUserId(user.getId());

        // 生成访问 Token (包含用户名标识，以便安全恢复会话)
        String token = "unihh_" + user.getUsername() + "_" + UUID.randomUUID().toString().replace("-", "");

        // 注册在线安全会话
        com.unihh.iot.admin.security.LoginUser loginUser = new com.unihh.iot.admin.security.LoginUser();
        loginUser.setUserId(user.getId());
        loginUser.setUsername(user.getUsername());
        loginUser.setNickname(user.getNickname());
        loginUser.setStatus(user.getStatus());
        loginUser.setToken(token);
        loginUser.setLoginIp(clientIp);
        loginUser.setRoles(new HashSet<>(roleKeys));
        loginUser.setPermissions(new HashSet<>(permissions));
        if (tokenSessionManager != null) {
            tokenSessionManager.registerSession(token, loginUser);
        }

        log.info("[用户登录成功] username={}, 角色={}, IP={}", username, roleKeys, clientIp);

        return new LoginResponse(token, user, roleKeys, permissions, menus);
    }

    @Override
    public SysUser getUserInfo(String username) {
        if (username == null) return null;
        SysUser user = sysUserDao.findByUsername(username);
        if (user != null) {
            List<SysRole> roles = sysUserDao.findRolesByUserId(user.getId());
            user.setRoles(roles);
            List<String> permissions = sysUserDao.findPermissionsByUserId(user.getId());
            user.setPermissions(permissions);
        }
        return user;
    }

    @Override
    public List<SysMenu> getRoutes(String username) {
        if (username == null) return Collections.emptyList();
        SysUser user = sysUserDao.findByUsername(username);
        if (user == null) {
            return Collections.emptyList();
        }
        return sysMenuDao.findMenusByUserId(user.getId());
    }

    @Override
    public void logout(String token) {
        if (token != null && tokenSessionManager != null) {
            tokenSessionManager.removeSession(token);
        }
    }
}
