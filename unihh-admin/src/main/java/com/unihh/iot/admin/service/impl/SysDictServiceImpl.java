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

package com.unihh.iot.admin.service.impl;

import com.unihh.iot.admin.dao.SysDictDao;
import com.unihh.iot.admin.model.SysDictData;
import com.unihh.iot.admin.model.SysDictType;
import com.unihh.iot.admin.service.SysDictService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
public class SysDictServiceImpl implements SysDictService {

    private static final Logger log = LoggerFactory.getLogger(SysDictServiceImpl.class);

    @Autowired
    private SysDictDao sysDictDao;

    @Override
    public List<SysDictType> getAllTypes() {
        return sysDictDao.findAllTypes();
    }

    @Override
    public List<SysDictType> getAllTypes(String keyword, Integer status) {
        return sysDictDao.findAllTypes(keyword, status);
    }

    @Override
    public SysDictType getTypeById(Long id) {
        return sysDictDao.findTypeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createType(SysDictType type) {
        if (type == null || type.getDictType() == null || type.getDictType().trim().isEmpty()) {
            throw new IllegalArgumentException("字典类型标识编码不能为空");
        }
        if (type.getDictName() == null || type.getDictName().trim().isEmpty()) {
            throw new IllegalArgumentException("字典名称不能为空");
        }

        String dictType = type.getDictType().trim();
        SysDictType exist = sysDictDao.findTypeByCode(dictType);
        if (exist != null) {
            throw new IllegalArgumentException("字典类型编码已存在: " + dictType);
        }

        type.setDictType(dictType);
        type.setDictName(type.getDictName().trim());
        if (type.getStatus() == null) {
            type.setStatus(1);
        }

        Long typeId = sysDictDao.insertType(type);
        log.info("[字典管理] 成功创建字典类型: dictType={}, id={}", dictType, typeId);
        return typeId != null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateType(SysDictType type) {
        if (type == null || type.getId() == null) {
            throw new IllegalArgumentException("字典类型 ID 不能为空");
        }
        SysDictType exist = sysDictDao.findTypeById(type.getId());
        if (exist == null) {
            throw new IllegalArgumentException("字典类型不存在");
        }
        int rows = sysDictDao.updateType(type);
        log.info("[字典管理] 成功更新字典类型: id={}, dictType={}", type.getId(), exist.getDictType());
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteType(Long id) {
        if (id == null) return false;
        int rows = sysDictDao.deleteTypeById(id);
        log.info("[字典管理] 删除字典类型及其所有关联数据: id={}", id);
        return rows > 0;
    }

    @Override
    public List<SysDictData> getDataByType(String dictType) {
        if (dictType == null || dictType.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return sysDictDao.findDataByType(dictType.trim());
    }

    @Override
    public SysDictData getDataById(Long id) {
        return sysDictDao.findDataById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createData(SysDictData data) {
        if (data == null || data.getDictType() == null || data.getDictType().trim().isEmpty()) {
            throw new IllegalArgumentException("字典类型编码不能为空");
        }
        if (data.getDictLabel() == null || data.getDictLabel().trim().isEmpty()) {
            throw new IllegalArgumentException("字典标签名称不能为空");
        }
        if (data.getDictValue() == null || data.getDictValue().trim().isEmpty()) {
            throw new IllegalArgumentException("字典键值不能为空");
        }

        if (data.getSortOrder() == null) {
            data.setSortOrder(0);
        }
        if (data.getStatus() == null) {
            data.setStatus(1);
        }
        if (data.getIsDefault() == null) {
            data.setIsDefault(0);
        }

        Long dataId = sysDictDao.insertData(data);
        log.info("[字典管理] 成功新增字典键值: type={}, label={}, value={}, id={}", data.getDictType(), data.getDictLabel(), data.getDictValue(), dataId);
        return dataId != null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateData(SysDictData data) {
        if (data == null || data.getId() == null) {
            throw new IllegalArgumentException("字典数据 ID 不能为空");
        }
        SysDictData exist = sysDictDao.findDataById(data.getId());
        if (exist == null) {
            throw new IllegalArgumentException("字典数据不存在");
        }

        int rows = sysDictDao.updateData(data);
        log.info("[字典管理] 成功更新字典键值: id={}, type={}, label={}", data.getId(), exist.getDictType(), data.getDictLabel());
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteData(Long id) {
        if (id == null) return false;
        int rows = sysDictDao.deleteDataById(id);
        log.info("[字典管理] 删除字典键值: id={}", id);
        return rows > 0;
    }
}
