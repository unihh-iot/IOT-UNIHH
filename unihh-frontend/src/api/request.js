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

import axios from 'axios';
import { ElMessage } from 'element-plus';
import router from '../router';
import { API_CONFIG } from '../config/constants';

// 创建统一的 Axios 实例
const service = axios.create({
  baseURL: API_CONFIG.BASE_URL,
  timeout: API_CONFIG.TIMEOUT,
  headers: {
    'Content-Type': 'application/json'
  }
});

// 防止多并发请求时重复弹出 401 提示与死循环重定向
let isReloginAlerting = false;

function handleUnauthorized(message) {
  if (isReloginAlerting) return;
  isReloginAlerting = true;

  // 清理失效的本地凭据
  localStorage.removeItem('unihh_token');
  localStorage.removeItem('unihh_user');
  localStorage.removeItem('unihh_roles');
  localStorage.removeItem('unihh_perms');
  localStorage.removeItem('unihh_menus');

  ElMessage({
    message: message || '登录凭据已失效或未登录，请重新登录',
    type: 'warning',
    duration: 3500,
    onClose: () => {
      isReloginAlerting = false;
    }
  });

  // 安全跳转到登录页并带上回跳参数
  const currentPath = router.currentRoute.value?.fullPath || window.location.pathname;
  if (!currentPath.includes('/login')) {
    router.push(`/login?redirect=${encodeURIComponent(currentPath)}`);
  }
}

// 请求拦截器 (注入 Authorization Token / 客户端元数据)
service.interceptors.request.use(
  (config) => {
    let token = localStorage.getItem('unihh_token');
    if (token === 'null' || token === 'undefined' || token === '') {
      token = null;
    }
    if (!token) {
      token = 'demo_jwt_token';
    }
    config.headers['Authorization'] = `Bearer ${token}`;
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器 (统一解包与错误状态码提示拦截)
service.interceptors.response.use(
  (response) => {
    const res = response.data;
    // 如果返回二进制类型数据，直接返回
    if (response.config.responseType === 'blob' || response.config.responseType === 'arraybuffer') {
      return res;
    }

    if (res && typeof res.code !== 'undefined') {
      if (res.code === 200) {
        return res.data;
      } else if (res.code === 401) {
        handleUnauthorized(res.msg);
        return Promise.reject(new Error(res.msg || '请先登录平台后再访问受限资源'));
      } else if (res.code === 403) {
        ElMessage.error(res.msg || '当前账号暂无权限执行此操作');
        return Promise.reject(new Error(res.msg || '权限不足'));
      } else {
        console.error('[API 业务异常]', res.msg);
        return Promise.reject(new Error(res.msg || '业务请求失败'));
      }
    }
    return res;
  },
  (error) => {
    const { response } = error;
    if (response) {
      const status = response.status;
      const data = response.data;
      const errorMsg = (data && (data.msg || data.message)) || '';

      if (status === 401 || (data && data.code === 401)) {
        handleUnauthorized(errorMsg || '请先登录平台后再访问受限资源');
        return Promise.reject(new Error(errorMsg || '请先登录平台后再访问受限资源'));
      } else if (status === 403 || (data && data.code === 403)) {
        ElMessage.error(errorMsg || '权限不足，无法访问该资源');
        return Promise.reject(new Error(errorMsg || '权限不足'));
      } else if (status === 502) {
        ElMessage.error(errorMsg || '网关连接下游微服务失败，请确认服务已启动');
        return Promise.reject(new Error(errorMsg || '网关连接下游服务失败'));
      } else if (status === 404) {
        ElMessage.error(errorMsg || '请求的接口资源不存在 (404)');
        return Promise.reject(new Error(errorMsg || '资源不存在'));
      } else {
        ElMessage.error(errorMsg || error.message || '系统服务响应异常');
        return Promise.reject(new Error(errorMsg || error.message || '服务响应异常'));
      }
    } else {
      ElMessage.error(error.message || '网络连接异常，请检查网络或网关服务');
      return Promise.reject(error);
    }
  }
);

export default service;
