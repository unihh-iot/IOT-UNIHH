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

import com.alibaba.fastjson2.JSON;
import com.unihh.iot.admin.annotation.Logical;
import com.unihh.iot.admin.annotation.RequiresPermissions;
import com.unihh.iot.admin.annotation.RequiresRoles;
import com.unihh.iot.common.model.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

/**
 * 系统安全认证与细粒度权限动态拦截器
 */
@Component
public class SecurityInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(SecurityInterceptor.class);

    @Autowired
    private TokenSessionManager tokenSessionManager;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        // 提取认证 Token
        String token = extractToken(request);
        LoginUser loginUser = null;
        if (token != null && !token.isEmpty()) {
            loginUser = tokenSessionManager.getLoginUser(token);
        }

        // 存入当前请求线程上下文
        if (loginUser != null) {
            SecurityContextHolder.set(loginUser);
        }

        // 检查角色权限注解
        RequiresRoles rolesAnno = handlerMethod.getMethodAnnotation(RequiresRoles.class);
        if (rolesAnno == null) {
            rolesAnno = handlerMethod.getBeanType().getAnnotation(RequiresRoles.class);
        }
        if (rolesAnno != null) {
            if (loginUser == null) {
                renderJson(response, 401, "请先登录平台后再访问受限资源");
                return false;
            }
            if (!checkRoles(loginUser, rolesAnno)) {
                log.warn("[权限拦截] 用户 {} 无权访问 {}: 缺少角色 {}", loginUser.getUsername(), request.getRequestURI(), Arrays.toString(rolesAnno.value()));
                renderJson(response, 403, "权限不足，需要角色: " + Arrays.toString(rolesAnno.value()));
                return false;
            }
        }

        // 检查功能权限注解
        RequiresPermissions permsAnno = handlerMethod.getMethodAnnotation(RequiresPermissions.class);
        if (permsAnno == null) {
            permsAnno = handlerMethod.getBeanType().getAnnotation(RequiresPermissions.class);
        }
        if (permsAnno != null) {
            if (loginUser == null) {
                renderJson(response, 401, "请先登录平台后再访问受限资源");
                return false;
            }
            if (!checkPermissions(loginUser, permsAnno)) {
                log.warn("[权限拦截] 用户 {} 无权访问 {}: 缺少权限标识 {}", loginUser.getUsername(), request.getRequestURI(), Arrays.toString(permsAnno.value()));
                renderJson(response, 403, "权限不足，缺少权限: " + Arrays.toString(permsAnno.value()));
                return false;
            }
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        SecurityContextHolder.clear();
    }

    private String extractToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || token.trim().isEmpty()) {
            token = request.getHeader("X-Token");
        }
        if (token == null || token.trim().isEmpty()) {
            token = request.getParameter("token");
        }
        if (token != null && !token.trim().isEmpty()) {
            String trimmed = token.trim();
            if (trimmed.startsWith("Bearer ") || trimmed.startsWith("bearer ")) {
                return trimmed.substring(7).trim();
            }
            return trimmed;
        }
        return null;
    }

    private boolean checkRoles(LoginUser user, RequiresRoles anno) {
        if (user == null) return false;
        if (user.isAdmin()) return true;
        Set<String> userRoles = user.getRoles();
        String[] requiredRoles = anno.value();
        if (requiredRoles == null || requiredRoles.length == 0) return true;
        if (userRoles == null || userRoles.isEmpty()) return false;

        if (anno.logical() == Logical.AND) {
            for (String r : requiredRoles) {
                if (!userRoles.contains(r)) return false;
            }
            return true;
        } else {
            for (String r : requiredRoles) {
                if (userRoles.contains(r)) return true;
            }
            return false;
        }
    }

    private boolean checkPermissions(LoginUser user, RequiresPermissions anno) {
        if (user == null) return false;
        if (user.isAdmin()) return true;
        Set<String> userPerms = user.getPermissions();
        if (userPerms != null && (userPerms.contains("*:*:*") || userPerms.contains("*"))) return true;
        String[] requiredPerms = anno.value();
        if (requiredPerms == null || requiredPerms.length == 0) return true;
        if (userPerms == null || userPerms.isEmpty()) return false;

        if (anno.logical() == Logical.AND) {
            for (String p : requiredPerms) {
                if (!userPerms.contains(p)) return false;
            }
            return true;
        } else {
            for (String p : requiredPerms) {
                if (userPerms.contains(p)) return true;
            }
            return false;
        }
    }

    private void renderJson(HttpServletResponse response, int code, String msg) throws IOException {
        response.setStatus(code == 401 ? 401 : (code == 403 ? 403 : 200));
        response.setContentType("application/json;charset=UTF-8");
        R<?> r = R.fail(code, msg);
        response.getWriter().write(JSON.toJSONString(r));
        response.getWriter().flush();
    }
}
