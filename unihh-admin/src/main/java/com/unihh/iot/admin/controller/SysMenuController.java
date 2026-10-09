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
import com.unihh.iot.admin.model.SysMenu;
import com.unihh.iot.admin.service.SysMenuService;
import com.unihh.iot.common.model.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/system/menu", "/admin/system/menu"})
public class SysMenuController {

    @Autowired
    private SysMenuService sysMenuService;

    @GetMapping("/tree")
    @RequiresPermissions("system:menu:list")
    public R<List<SysMenu>> getMenuTree() {
        return R.ok(sysMenuService.getMenuTree());
    }

    @GetMapping("/routes")
    public R<List<SysMenu>> getRoutes(@RequestParam(value = "userId", required = false) Long userId) {
        if (userId != null) {
            return R.ok(sysMenuService.getRoutesByUserId(userId));
        }
        return R.ok(sysMenuService.getCurrentUserRoutes());
    }

    @GetMapping("/list")
    @RequiresPermissions("system:menu:list")
    public R<List<SysMenu>> getAllMenus(@RequestParam(value = "keyword", required = false) String keyword,
                                       @RequestParam(value = "status", required = false) Integer status) {
        return R.ok(sysMenuService.getMenuList(keyword, status));
    }

    @GetMapping("/{id}")
    @RequiresPermissions("system:menu:list")
    public R<SysMenu> getMenuById(@PathVariable("id") Long id) {
        SysMenu menu = sysMenuService.getMenuById(id);
        if (menu == null) {
            return R.fail(404, "未找到指定菜单");
        }
        return R.ok(menu);
    }

    @PostMapping
    @RequiresPermissions("system:menu:add")
    @AuditLog(module = "菜单路由", action = "新增菜单路由")
    public R<String> createMenu(@RequestBody SysMenu menu) {
        try {
            boolean success = sysMenuService.createMenu(menu);
            return success ? R.ok("新增菜单成功") : R.fail(500, "新增菜单失败");
        } catch (IllegalArgumentException e) {
            return R.fail(400, e.getMessage());
        }
    }

    @PutMapping
    @RequiresPermissions("system:menu:edit")
    @AuditLog(module = "菜单路由", action = "修改菜单路由")
    public R<String> updateMenu(@RequestBody SysMenu menu) {
        try {
            boolean success = sysMenuService.updateMenu(menu);
            return success ? R.ok("修改菜单成功") : R.fail(500, "修改菜单失败");
        } catch (IllegalArgumentException e) {
            return R.fail(400, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @RequiresPermissions("system:menu:edit")
    @AuditLog(module = "菜单路由", action = "删除菜单路由")
    public R<String> deleteMenu(@PathVariable("id") Long id) {
        try {
            boolean success = sysMenuService.deleteMenu(id);
            return success ? R.ok("删除菜单成功") : R.fail(500, "删除菜单失败");
        } catch (IllegalArgumentException e) {
            return R.fail(400, e.getMessage());
        }
    }
}
