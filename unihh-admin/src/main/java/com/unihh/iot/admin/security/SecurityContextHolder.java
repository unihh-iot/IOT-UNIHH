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

/**
 * 线程上下文用户安全上下文持有者 (基于 ThreadLocal)
 */
public final class SecurityContextHolder {

    private static final ThreadLocal<LoginUser> CONTEXT_HOLDER = new ThreadLocal<>();

    private SecurityContextHolder() {}

    public static void set(LoginUser user) {
        CONTEXT_HOLDER.set(user);
    }

    public static LoginUser get() {
        return CONTEXT_HOLDER.get();
    }

    public static Long getUserId() {
        LoginUser user = get();
        return user != null ? user.getUserId() : null;
    }

    public static String getUsername() {
        LoginUser user = get();
        return user != null ? user.getUsername() : "SYSTEM";
    }

    public static void clear() {
        CONTEXT_HOLDER.remove();
    }
}
