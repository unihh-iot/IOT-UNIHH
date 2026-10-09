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

package com.unihh.iot.admin.controller;

import com.unihh.iot.admin.annotation.AuditLog;
import com.unihh.iot.admin.model.LoginRequest;
import com.unihh.iot.admin.model.LoginResponse;
import com.unihh.iot.admin.model.SysMenu;
import com.unihh.iot.admin.model.SysUser;
import com.unihh.iot.admin.service.AuthService;
import com.unihh.iot.common.model.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping({"/api/v1/auth", "/admin/auth"})
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    @AuditLog(module = "用户认证", action = "用户登录")
    public R<LoginResponse> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String clientIp = httpRequest.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = httpRequest.getRemoteAddr();
        }
        try {
            LoginResponse response = authService.login(request, clientIp);
            return R.ok(response);
        } catch (IllegalArgumentException e) {
            return R.fail(400, e.getMessage());
        } catch (RuntimeException e) {
            return R.fail(401, e.getMessage());
        }
    }

    @GetMapping("/info")
    public R<SysUser> getUserInfo(@RequestParam(value = "username", required = false) String username,
                                 HttpServletRequest httpRequest) {
        String user = (username != null && !username.trim().isEmpty()) ? username.trim() : "admin";
        SysUser userInfo = authService.getUserInfo(user);
        if (userInfo == null) {
            return R.fail(404, "用户不存在: " + user);
        }
        return R.ok(userInfo);
    }

    @GetMapping("/routes")
    public R<List<SysMenu>> getRoutes(@RequestParam(value = "username", required = false) String username) {
        String user = (username != null && !username.trim().isEmpty()) ? username.trim() : "admin";
        List<SysMenu> routes = authService.getRoutes(user);
        return R.ok(routes);
    }

    @PostMapping("/logout")
    @AuditLog(module = "用户认证", action = "注销登录")
    public R<String> logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && !token.trim().isEmpty()) {
            if (token.startsWith("Bearer ") || token.startsWith("bearer ")) {
                token = token.substring(7).trim();
            }
            authService.logout(token);
        }
        return R.ok("注销成功");
    }
}
