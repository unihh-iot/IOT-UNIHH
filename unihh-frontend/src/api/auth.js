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
 * 用户登录认证
 * @param {Object} data - { username, password, rememberMe }
 */
export function login(data) {
  return service.post('/auth/login', data);
}

/**
 * 获取当前登录用户信息与权限
 * @param {string} username
 */
export function getUserInfo(username) {
  return service.get('/auth/info', { params: { username } });
}

/**
 * 获取用户动态路由菜单
 * @param {string} username
 */
export function getDynamicRoutes(username) {
  return service.get('/auth/routes', { params: { username } });
}

/**
 * 注销退出登录
 */
export function logout() {
  return service.post('/auth/logout');
}

/**
 * 获取字典数据列表
 * @param {string} dictType
 */
export function getDictData(dictType) {
  return service.get(`/system/dict/data/type/${dictType}`);
}
