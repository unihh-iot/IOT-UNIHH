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

import service from './request';

/**
 * 查询角色列表
 * @param {Object} params - { keyword, status }
 */
export function getRoleList(params) {
  return service.get('/system/role/list', { params });
}

/**
 * 查询指定角色详情
 * @param {number|string} id
 */
export function getRoleById(id) {
  return service.get(`/system/role/${id}`);
}

/**
 * 新增角色
 * @param {Object} data
 */
export function addRole(data) {
  return service.post('/system/role', data);
}

/**
 * 修改角色
 * @param {Object} data
 */
export function updateRole(data) {
  return service.put('/system/role', data);
}

/**
 * 删除角色
 * @param {number|string} id
 */
export function deleteRole(id) {
  return service.delete(`/system/role/${id}`);
}

/**
 * 修改角色状态
 * @param {number|string} id
 * @param {number} status
 */
export function changeRoleStatus(id, status) {
  return service.put(`/system/role/${id}/status`, null, { params: { status } });
}

/**
 * 获取角色已分配的权限 ID 列表
 * @param {number|string} id
 */
export function getRolePermissionIds(id) {
  return service.get(`/system/role/${id}/permissions`);
}

/**
 * 分配角色功能权限
 * @param {number|string} id
 * @param {Array<number>} permissionIds
 */
export function updateRolePermissions(id, permissionIds) {
  return service.put(`/system/role/${id}/permissions`, { permissionIds });
}

/**
 * 获取角色已分配的菜单 ID 列表
 * @param {number|string} id
 */
export function getRoleMenuIds(id) {
  return service.get(`/system/role/${id}/menus`);
}

/**
 * 分配角色菜单路由
 * @param {number|string} id
 * @param {Array<number>} menuIds
 */
export function updateRoleMenus(id, menuIds) {
  return service.put(`/system/role/${id}/menus`, { menuIds });
}

/**
 * 获取系统全部可用功能权限列表
 */
export function getAllPermissions() {
  return service.get('/system/role/permissions/all');
}
