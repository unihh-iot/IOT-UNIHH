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

/**
 * 细粒度功能权限与角色判断工具函数
 * 支持超级管理员 admin 自动放行及通配符 *:*:*
 */
export function hasPermission(perm) {
  if (!perm) return true;

  try {
    // 1. 检查角色列表，admin 角色拥有系统最高全权
    const rolesStr = localStorage.getItem('unihh_roles');
    if (rolesStr) {
      const roles = JSON.parse(rolesStr);
      if (Array.isArray(roles) && roles.includes('admin')) {
        return true;
      }
    }

    // 2. 检查权限标识列表
    const permsStr = localStorage.getItem('unihh_perms');
    if (!permsStr) return false;
    const perms = JSON.parse(permsStr);
    if (!Array.isArray(perms) || perms.length === 0) return false;

    // 3. 通配符放行
    if (perms.includes('*:*:*') || perms.includes('*')) {
      return true;
    }

    // 4. 数组多权限匹配 (任意满足即可)
    if (Array.isArray(perm)) {
      return perm.some(p => perms.includes(p));
    }

    return perms.includes(perm);
  } catch (e) {
    console.error('[权限校验异常]', e);
    return false;
  }
}

/**
 * 检查当前登录用户是否具备某角色
 */
export function hasRole(role) {
  if (!role) return true;
  try {
    const rolesStr = localStorage.getItem('unihh_roles');
    if (!rolesStr) return false;
    const roles = JSON.parse(rolesStr);
    if (!Array.isArray(roles)) return false;
    return roles.includes(role);
  } catch (e) {
    return false;
  }
}

export default {
  hasPermission,
  hasRole
};
