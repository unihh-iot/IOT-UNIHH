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
import com.unihh.iot.admin.annotation.Logical;
import com.unihh.iot.admin.annotation.RequiresPermissions;
import com.unihh.iot.admin.service.DriverService;
import com.unihh.iot.common.model.DriverInfo;
import com.unihh.iot.common.model.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class DriverController {

    @Autowired
    private DriverService driverService;

    @GetMapping("/api/v1/drivers")
    @RequiresPermissions("driver:list")
    public R<Map<String, Object>> getLoadedDrivers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize
    ) {
        Map<String, Object> pageResult = driverService.getDriverPage(keyword, status, pageNum, pageSize);
        return R.ok(pageResult);
    }

    @PostMapping("/api/v1/drivers")
    @RequiresPermissions(value = {"driver:edit", "driver:list"}, logical = Logical.OR)
    @AuditLog(module = "驱动管理", action = "注册协议驱动")
    public R<String> addDriver(@RequestBody DriverInfo driverInfo) {
        if (driverInfo.getDriverCode() == null || driverInfo.getDriverCode().trim().isEmpty()) {
            return R.fail(400, "驱动标识Code不能为空");
        }
        boolean success = driverService.addDriver(driverInfo);
        return success ? R.ok("注册协议驱动成功") : R.fail(500, "注册协议驱动失败");
    }

    @PutMapping("/api/v1/drivers/{driverCode}/status")
    @RequiresPermissions(value = {"driver:edit", "driver:list"}, logical = Logical.OR)
    @AuditLog(module = "驱动管理", action = "切换协议驱动启停状态")
    public R<String> updateDriverStatus(@PathVariable("driverCode") String driverCode, @RequestParam("status") int status) {
        boolean success = driverService.updateStatus(driverCode, status);
        return success ? R.ok("更新驱动状态成功") : R.fail(500, "更新驱动状态失败");
    }

    @DeleteMapping("/api/v1/drivers/{driverCode}")
    @RequiresPermissions(value = {"driver:edit", "driver:list"}, logical = Logical.OR)
    @AuditLog(module = "驱动管理", action = "注销删除协议驱动")
    public R<String> deleteDriver(@PathVariable("driverCode") String driverCode) {
        boolean success = driverService.deleteDriver(driverCode);
        return success ? R.ok("注销协议驱动成功") : R.fail(500, "注销协议驱动失败");
    }

    @RequestMapping(value = "/api/v1/drivers/{driverCode}/ping", method = {RequestMethod.GET, RequestMethod.POST})
    @RequiresPermissions(value = {"driver:edit", "driver:list"}, logical = Logical.OR)
    @AuditLog(module = "驱动管理", action = "通道心跳自检")
    public R<Map<String, Object>> pingDriver(@PathVariable("driverCode") String driverCode) {
        Map<String, Object> result = driverService.pingDriver(driverCode);
        return R.ok(result);
    }
}
