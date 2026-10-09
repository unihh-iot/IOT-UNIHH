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

package com.unihh.iot.admin.model;

import java.util.List;

/**
 * 登录成功响应体
 */
public class LoginResponse {
    private String token;
    private SysUser user;
    private List<String> roles;
    private List<String> permissions;
    private List<SysMenu> menus;

    public LoginResponse() {}

    public LoginResponse(String token, SysUser user, List<String> roles, List<String> permissions, List<SysMenu> menus) {
        this.token = token;
        this.user = user;
        this.roles = roles;
        this.permissions = permissions;
        this.menus = menus;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public SysUser getUser() { return user; }
    public void setUser(SysUser user) { this.user = user; }

    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }

    public List<String> getPermissions() { return permissions; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }

    public List<SysMenu> getMenus() { return menus; }
    public void setMenus(List<SysMenu> menus) { this.menus = menus; }
}
