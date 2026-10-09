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

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 登录用户会话安全上下文信息
 */
public class LoginUser implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long userId;
    private String username;
    private String nickname;
    private Integer status;
    private String token;
    private String loginIp;
    private long loginTime;
    private Set<String> roles = new HashSet<>();
    private Set<String> permissions = new HashSet<>();

    public LoginUser() {}

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getLoginIp() { return loginIp; }
    public void setLoginIp(String loginIp) { this.loginIp = loginIp; }

    public long getLoginTime() { return loginTime; }
    public void setLoginTime(long loginTime) { this.loginTime = loginTime; }

    public Set<String> getRoles() { return roles != null ? roles : Collections.emptySet(); }
    public void setRoles(Set<String> roles) { this.roles = roles; }

    public Set<String> getPermissions() { return permissions != null ? permissions : Collections.emptySet(); }
    public void setPermissions(Set<String> permissions) { this.permissions = permissions; }

    public boolean isAdmin() {
        return roles != null && roles.contains("admin");
    }

    public boolean hasPermission(String perm) {
        if (isAdmin()) return true;
        return permissions != null && permissions.contains(perm);
    }

    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }
}
