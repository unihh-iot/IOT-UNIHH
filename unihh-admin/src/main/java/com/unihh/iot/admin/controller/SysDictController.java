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

package com.unihh.iot.admin.controller;

import com.unihh.iot.admin.annotation.AuditLog;
import com.unihh.iot.admin.annotation.RequiresPermissions;
import com.unihh.iot.admin.model.SysDictData;
import com.unihh.iot.admin.model.SysDictType;
import com.unihh.iot.admin.service.SysDictService;
import com.unihh.iot.common.model.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/system/dict", "/admin/system/dict"})
public class SysDictController {

    @Autowired
    private SysDictService sysDictService;

    // ---------------- 字典类型接口 ----------------

    @GetMapping("/types")
    @RequiresPermissions("system:dict:list")
    public R<List<SysDictType>> getAllTypes(@RequestParam(value = "keyword", required = false) String keyword,
                                            @RequestParam(value = "status", required = false) Integer status) {
        if (keyword != null || status != null) {
            return R.ok(sysDictService.getAllTypes(keyword, status));
        }
        return R.ok(sysDictService.getAllTypes());
    }

    @GetMapping("/type/{id}")
    @RequiresPermissions("system:dict:list")
    public R<SysDictType> getTypeById(@PathVariable("id") Long id) {
        SysDictType type = sysDictService.getTypeById(id);
        if (type == null) {
            return R.fail(404, "未找到指定字典类型");
        }
        return R.ok(type);
    }

    @PostMapping("/type")
    @RequiresPermissions("system:dict:add")
    @AuditLog(module = "字典管理", action = "新增字典类型")
    public R<String> createType(@RequestBody SysDictType type) {
        try {
            boolean success = sysDictService.createType(type);
            return success ? R.ok("新增字典类型成功") : R.fail(500, "新增字典类型失败");
        } catch (IllegalArgumentException e) {
            return R.fail(400, e.getMessage());
        }
    }

    @PutMapping("/type")
    @RequiresPermissions("system:dict:edit")
    @AuditLog(module = "字典管理", action = "修改字典类型")
    public R<String> updateType(@RequestBody SysDictType type) {
        try {
            boolean success = sysDictService.updateType(type);
            return success ? R.ok("修改字典类型成功") : R.fail(500, "修改字典类型失败");
        } catch (IllegalArgumentException e) {
            return R.fail(400, e.getMessage());
        }
    }

    @DeleteMapping("/type/{id}")
    @RequiresPermissions("system:dict:edit")
    @AuditLog(module = "字典管理", action = "删除字典类型")
    public R<String> deleteType(@PathVariable("id") Long id) {
        try {
            boolean success = sysDictService.deleteType(id);
            return success ? R.ok("删除字典类型成功") : R.fail(500, "删除字典类型失败");
        } catch (IllegalArgumentException e) {
            return R.fail(400, e.getMessage());
        }
    }

    // ---------------- 字典数据键值接口 ----------------

    @GetMapping("/data/type/{dictType}")
    public R<List<SysDictData>> getDataByType(@PathVariable("dictType") String dictType) {
        return R.ok(sysDictService.getDataByType(dictType));
    }

    @GetMapping("/data/{id}")
    @RequiresPermissions("system:dict:list")
    public R<SysDictData> getDataById(@PathVariable("id") Long id) {
        SysDictData data = sysDictService.getDataById(id);
        if (data == null) {
            return R.fail(404, "未找到指定字典数据");
        }
        return R.ok(data);
    }

    @PostMapping("/data")
    @RequiresPermissions("system:dict:add")
    @AuditLog(module = "字典管理", action = "新增字典键值")
    public R<String> createData(@RequestBody SysDictData data) {
        try {
            boolean success = sysDictService.createData(data);
            return success ? R.ok("新增字典键值成功") : R.fail(500, "新增字典键值失败");
        } catch (IllegalArgumentException e) {
            return R.fail(400, e.getMessage());
        }
    }

    @PutMapping("/data")
    @RequiresPermissions("system:dict:edit")
    @AuditLog(module = "字典管理", action = "修改字典键值")
    public R<String> updateData(@RequestBody SysDictData data) {
        try {
            boolean success = sysDictService.updateData(data);
            return success ? R.ok("修改字典键值成功") : R.fail(500, "修改字典键值失败");
        } catch (IllegalArgumentException e) {
            return R.fail(400, e.getMessage());
        }
    }

    @DeleteMapping("/data/{id}")
    @RequiresPermissions("system:dict:edit")
    @AuditLog(module = "字典管理", action = "删除字典键值")
    public R<String> deleteData(@PathVariable("id") Long id) {
        try {
            boolean success = sysDictService.deleteData(id);
            return success ? R.ok("删除字典键值成功") : R.fail(500, "删除字典键值失败");
        } catch (IllegalArgumentException e) {
            return R.fail(400, e.getMessage());
        }
    }
}
