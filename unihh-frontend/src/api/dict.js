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
 * 查询字典类型列表
 * @param {Object} params - { keyword, status }
 */
export function getDictTypes(params) {
  return service.get('/system/dict/types', { params });
}

/**
 * 获取字典类型详情
 * @param {number|string} id
 */
export function getDictTypeById(id) {
  return service.get(`/system/dict/type/${id}`);
}

/**
 * 新增字典类型
 * @param {Object} data
 */
export function addDictType(data) {
  return service.post('/system/dict/type', data);
}

/**
 * 修改字典类型
 * @param {Object} data
 */
export function updateDictType(data) {
  return service.put('/system/dict/type', data);
}

/**
 * 删除字典类型
 * @param {number|string} id
 */
export function deleteDictType(id) {
  return service.delete(`/system/dict/type/${id}`);
}

/**
 * 根据字典类型获取字典数据列表
 * @param {string} dictType
 */
export function getDictDataByType(dictType) {
  return service.get(`/system/dict/data/type/${dictType}`);
}

/**
 * 获取字典键值明细
 * @param {number|string} id
 */
export function getDictDataById(id) {
  return service.get(`/system/dict/data/${id}`);
}

/**
 * 新增字典键值
 * @param {Object} data
 */
export function addDictData(data) {
  return service.post('/system/dict/data', data);
}

/**
 * 修改字典键值
 * @param {Object} data
 */
export function updateDictData(data) {
  return service.put('/system/dict/data', data);
}

/**
 * 删除字典键值
 * @param {number|string} id
 */
export function deleteDictData(id) {
  return service.delete(`/system/dict/data/${id}`);
}
