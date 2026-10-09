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
import com.unihh.iot.admin.model.SysUser;
import com.unihh.iot.admin.service.SysUserService;
import com.unihh.iot.common.model.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/system/user", "/admin/system/user"})
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    @GetMapping("/list")
    @RequiresPermissions("system:user:list")
    public R<List<SysUser>> getUserList(@RequestParam(value = "keyword", required = false) String keyword,
                                        @RequestParam(value = "status", required = false) Integer status) {
        List<SysUser> list = sysUserService.getUserList(keyword, status);
        return R.ok(list);
    }

    @GetMapping("/{id}")
    @RequiresPermissions("system:user:list")
    public R<SysUser> getUserById(@PathVariable("id") Long id) {
        SysUser user = sysUserService.getUserById(id);
        if (user == null) {
            return R.fail(404, "未找到指定系统用户");
        }
        return R.ok(user);
    }

    @PostMapping
    @RequiresPermissions("system:user:add")
    @AuditLog(module = "用户管理", action = "新增系统用户")
    public R<String> createUser(@RequestBody SysUser user) {
        try {
            boolean success = sysUserService.createUser(user);
            return success ? R.ok("新增系统用户成功") : R.fail(500, "新增用户失败");
        } catch (IllegalArgumentException e) {
            return R.fail(400, e.getMessage());
        }
    }

    @PutMapping
    @RequiresPermissions("system:user:edit")
    @AuditLog(module = "用户管理", action = "修改系统用户")
    public R<String> updateUser(@RequestBody SysUser user) {
        try {
            boolean success = sysUserService.updateUser(user);
            return success ? R.ok("修改系统用户成功") : R.fail(500, "修改用户失败");
        } catch (IllegalArgumentException e) {
            return R.fail(400, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @RequiresPermissions("system:user:edit")
    @AuditLog(module = "用户管理", action = "删除系统用户")
    public R<String> deleteUser(@PathVariable("id") Long id) {
        try {
            boolean success = sysUserService.deleteUser(id);
            return success ? R.ok("删除系统用户成功") : R.fail(500, "删除用户失败");
        } catch (IllegalArgumentException e) {
            return R.fail(400, e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    @RequiresPermissions("system:user:edit")
    @AuditLog(module = "用户管理", action = "更新用户状态")
    public R<String> updateStatus(@PathVariable("id") Long id, @RequestParam("status") Integer status) {
        try {
            boolean success = sysUserService.updateStatus(id, status);
            return success ? R.ok("更新用户状态成功") : R.fail(500, "更新状态失败");
        } catch (IllegalArgumentException e) {
            return R.fail(400, e.getMessage());
        }
    }

    @PutMapping("/{id}/password")
    @RequiresPermissions("system:user:edit")
    @AuditLog(module = "用户管理", action = "重置用户密码")
    public R<String> resetPassword(@PathVariable("id") Long id, @RequestBody Map<String, String> body) {
        String newPassword = body != null ? body.get("password") : null;
        try {
            boolean success = sysUserService.resetPassword(id, newPassword);
            return success ? R.ok("重置密码成功") : R.fail(500, "重置密码失败");
        } catch (IllegalArgumentException e) {
            return R.fail(400, e.getMessage());
        }
    }
}
