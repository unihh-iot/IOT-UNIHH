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
import com.unihh.iot.admin.model.SysPermission;
import com.unihh.iot.admin.model.SysRole;
import com.unihh.iot.admin.service.SysRoleService;
import com.unihh.iot.common.model.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/system/role", "/admin/system/role"})
public class SysRoleController {

    @Autowired
    private SysRoleService sysRoleService;

    @GetMapping("/list")
    @RequiresPermissions("system:role:list")
    public R<List<SysRole>> getRoleList(@RequestParam(value = "keyword", required = false) String keyword,
                                        @RequestParam(value = "status", required = false) Integer status) {
        List<SysRole> list = sysRoleService.getRoleList(keyword, status);
        return R.ok(list);
    }

    @GetMapping("/{id}")
    @RequiresPermissions("system:role:list")
    public R<SysRole> getRoleById(@PathVariable("id") Long id) {
        SysRole role = sysRoleService.getRoleById(id);
        if (role == null) {
            return R.fail(404, "未找到指定角色");
        }
        return R.ok(role);
    }

    @PostMapping
    @RequiresPermissions("system:role:add")
    @AuditLog(module = "角色权限", action = "新增系统角色")
    public R<String> createRole(@RequestBody SysRole role) {
        try {
            boolean success = sysRoleService.createRole(role);
            return success ? R.ok("新增系统角色成功") : R.fail(500, "新增角色失败");
        } catch (IllegalArgumentException e) {
            return R.fail(400, e.getMessage());
        }
    }

    @PutMapping
    @RequiresPermissions("system:role:edit")
    @AuditLog(module = "角色权限", action = "修改系统角色")
    public R<String> updateRole(@RequestBody SysRole role) {
        try {
            boolean success = sysRoleService.updateRole(role);
            return success ? R.ok("修改系统角色成功") : R.fail(500, "修改角色失败");
        } catch (IllegalArgumentException e) {
            return R.fail(400, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @RequiresPermissions("system:role:edit")
    @AuditLog(module = "角色权限", action = "删除系统角色")
    public R<String> deleteRole(@PathVariable("id") Long id) {
        try {
            boolean success = sysRoleService.deleteRole(id);
            return success ? R.ok("删除系统角色成功") : R.fail(500, "删除角色失败");
        } catch (IllegalArgumentException e) {
            return R.fail(400, e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    @RequiresPermissions("system:role:edit")
    @AuditLog(module = "角色权限", action = "更新角色状态")
    public R<String> updateStatus(@PathVariable("id") Long id, @RequestParam("status") Integer status) {
        try {
            boolean success = sysRoleService.updateStatus(id, status);
            return success ? R.ok("更新角色状态成功") : R.fail(500, "更新状态失败");
        } catch (IllegalArgumentException e) {
            return R.fail(400, e.getMessage());
        }
    }

    @GetMapping("/{id}/permissions")
    @RequiresPermissions("system:role:list")
    public R<List<Long>> getRolePermissionIds(@PathVariable("id") Long id) {
        List<Long> permIds = sysRoleService.getRolePermissionIds(id);
        return R.ok(permIds);
    }

    @PutMapping("/{id}/permissions")
    @RequiresPermissions("system:role:edit")
    @AuditLog(module = "角色权限", action = "分配角色功能权限")
    public R<String> assignPermissions(@PathVariable("id") Long id, @RequestBody Map<String, List<Long>> body) {
        List<Long> permissionIds = body != null ? body.get("permissionIds") : null;
        try {
            boolean success = sysRoleService.assignPermissions(id, permissionIds);
            return success ? R.ok("分配功能权限成功") : R.fail(500, "分配功能权限失败");
        } catch (IllegalArgumentException e) {
            return R.fail(400, e.getMessage());
        }
    }

    @GetMapping("/{id}/menus")
    @RequiresPermissions("system:role:list")
    public R<List<Long>> getRoleMenuIds(@PathVariable("id") Long id) {
        List<Long> menuIds = sysRoleService.getRoleMenuIds(id);
        return R.ok(menuIds);
    }

    @PutMapping("/{id}/menus")
    @RequiresPermissions("system:role:edit")
    @AuditLog(module = "角色权限", action = "分配角色菜单路由")
    public R<String> assignMenus(@PathVariable("id") Long id, @RequestBody Map<String, List<Long>> body) {
        List<Long> menuIds = body != null ? body.get("menuIds") : null;
        try {
            boolean success = sysRoleService.assignMenus(id, menuIds);
            return success ? R.ok("分配菜单路由成功") : R.fail(500, "分配菜单路由失败");
        } catch (IllegalArgumentException e) {
            return R.fail(400, e.getMessage());
        }
    }

    @GetMapping("/permissions/all")
    public R<List<SysPermission>> getAllPermissions() {
        List<SysPermission> list = sysRoleService.getAllPermissions();
        return R.ok(list);
    }
}
