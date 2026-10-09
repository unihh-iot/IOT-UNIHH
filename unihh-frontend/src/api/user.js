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
 * 查询系统用户列表
 * @param {Object} params - { keyword, status }
 */
export function getUserList(params) {
  return service.get('/system/user/list', { params });
}

/**
 * 查询指定用户详情
 * @param {number|string} id
 */
export function getUserById(id) {
  return service.get(`/system/user/${id}`);
}

/**
 * 新增系统用户
 * @param {Object} data
 */
export function addUser(data) {
  return service.post('/system/user', data);
}

/**
 * 修改系统用户
 * @param {Object} data
 */
export function updateUser(data) {
  return service.put('/system/user', data);
}

/**
 * 删除系统用户
 * @param {number|string} id
 */
export function deleteUser(id) {
  return service.delete(`/system/user/${id}`);
}

/**
 * 修改用户账号状态 (启用/停用)
 * @param {number|string} id
 * @param {number} status
 */
export function changeUserStatus(id, status) {
  return service.put(`/system/user/${id}/status`, null, { params: { status } });
}

/**
 * 重置用户密码
 * @param {number|string} id
 * @param {string} password
 */
export function resetUserPassword(id, password) {
  return service.put(`/system/user/${id}/password`, { password });
}
