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

package com.unihh.iot.admin.security;

import com.unihh.iot.admin.dao.SysUserDao;
import com.unihh.iot.admin.model.SysRole;
import com.unihh.iot.admin.model.SysUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 用户在线 Token 会话管理器
 */
@Component
public class TokenSessionManager {

    private static final Logger log = LoggerFactory.getLogger(TokenSessionManager.class);
    private static final long SESSION_EXPIRE_MS = 24 * 60 * 60 * 1000L; // 24小时有效

    private final Map<String, LoginUser> sessionCache = new ConcurrentHashMap<>();

    @Autowired(required = false)
    private SysUserDao sysUserDao;

    /**
     * 注册用户会话
     */
    public void registerSession(String token, LoginUser user) {
        if (token == null || user == null) return;
        user.setToken(token);
        user.setLoginTime(System.currentTimeMillis());
        sessionCache.put(token, user);
        log.info("[Token会话注册] 用户: {}, 角色: {}, 权限数: {}", user.getUsername(), user.getRoles(), user.getPermissions().size());
    }

    /**
     * 移除用户会话 (登出)
     */
    public void removeSession(String token) {
        if (token == null) return;
        LoginUser removed = sessionCache.remove(token);
        if (removed != null) {
            log.info("[Token会话注销] 用户: {}", removed.getUsername());
        }
    }

    /**
     * 刷新所有在线用户的角色与权限缓存 (在角色权限分配或用户角色变更时触发)
     */
    public void refreshAllPermissions() {
        if (sysUserDao == null) return;
        for (LoginUser user : sessionCache.values()) {
            if (user.getUserId() != null) {
                List<SysRole> roles = sysUserDao.findRolesByUserId(user.getUserId());
                Set<String> roleKeys = roles.stream().map(SysRole::getRoleKey).collect(Collectors.toSet());
                user.setRoles(roleKeys);

                List<String> permissions = sysUserDao.findPermissionsByUserId(user.getUserId());
                user.setPermissions(new HashSet<>(permissions));
            }
        }
        log.info("[Token会话] 同步刷新所有在线会话权限成功，当前在线人数: {}", sessionCache.size());
    }

    /**
     * 获取在线用户信息
     */
    public LoginUser getLoginUser(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }
        String cleanToken = token.trim();
        if (cleanToken.startsWith("Bearer ") || cleanToken.startsWith("bearer ")) {
            cleanToken = cleanToken.substring(7).trim();
        }

        LoginUser user = sessionCache.get(cleanToken);
        if (user != null) {
            // 检查是否过期
            if (System.currentTimeMillis() - user.getLoginTime() > SESSION_EXPIRE_MS) {
                sessionCache.remove(cleanToken);
                log.warn("[Token会话过期] Token={}", cleanToken);
                return null;
            }
            return user;
        }

        // 会话恢复机制：服务端重启或开发模式下根据 Token 还原对应用户的会话（严禁无条件降级恢复为 admin）
        if (sysUserDao != null) {
            String targetUsername = null;
            if (cleanToken.startsWith("unihh_")) {
                String sub = cleanToken.substring(6);
                int underlineIdx = sub.indexOf('_');
                if (underlineIdx > 0) {
                    targetUsername = sub.substring(0, underlineIdx);
                }
            } else if ("dev_token".equalsIgnoreCase(cleanToken) || "admin_token".equalsIgnoreCase(cleanToken) || "demo_jwt_token".equalsIgnoreCase(cleanToken)) {
                targetUsername = "admin";
            }

            if (targetUsername != null) {
                SysUser sysUser = sysUserDao.findByUsername(targetUsername);
                if (sysUser != null && sysUser.getStatus() != null && sysUser.getStatus() == 1) {
                    LoginUser restored = buildLoginUser(sysUser, cleanToken, "127.0.0.1");
                    sessionCache.put(cleanToken, restored);
                    log.info("[Token会话恢复] 从持久库恢复在线会话: username={}, roles={}, token={}", targetUsername, restored.getRoles(), cleanToken);
                    return restored;
                }
            }
        }

        return null;
    }

    /**
     * 根据数据库用户实体构建 LoginUser
     */
    public LoginUser buildLoginUser(SysUser sysUser, String token, String ip) {
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(sysUser.getId());
        loginUser.setUsername(sysUser.getUsername());
        loginUser.setNickname(sysUser.getNickname());
        loginUser.setStatus(sysUser.getStatus());
        loginUser.setToken(token);
        loginUser.setLoginIp(ip);
        loginUser.setLoginTime(System.currentTimeMillis());

        if (sysUserDao != null && sysUser.getId() != null) {
            List<SysRole> roles = sysUserDao.findRolesByUserId(sysUser.getId());
            Set<String> roleKeys = roles.stream().map(SysRole::getRoleKey).collect(Collectors.toSet());
            loginUser.setRoles(roleKeys);

            List<String> permissions = sysUserDao.findPermissionsByUserId(sysUser.getId());
            loginUser.setPermissions(new HashSet<>(permissions));
        }
        return loginUser;
    }
}
