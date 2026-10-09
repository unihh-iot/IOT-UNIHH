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
 * 获取完整菜单树 (用于菜单管理及树选择)
 */
export function getMenuTree() {
  return service.get('/system/menu/tree');
}

/**
 * 获取菜单列表 (平面列表)
 * @param {Object} params - { keyword, status }
 */
export function getMenuList(params) {
  return service.get('/system/menu/list', { params });
}

/**
 * 获取菜单明细
 * @param {number|string} id
 */
export function getMenuById(id) {
  return service.get(`/system/menu/${id}`);
}

/**
 * 新增菜单
 * @param {Object} data
 */
export function addMenu(data) {
  return service.post('/system/menu', data);
}

/**
 * 修改菜单
 * @param {Object} data
 */
export function updateMenu(data) {
  return service.put('/system/menu', data);
}

/**
 * 删除菜单
 * @param {number|string} id
 */
export function deleteMenu(id) {
  return service.delete(`/system/menu/${id}`);
}

/**
 * 获取当前用户分配的动态路由菜单树
 * @param {number|string} [userId]
 */
export function getUserRoutes(userId) {
  return service.get('/system/menu/routes', { params: { userId } });
}
