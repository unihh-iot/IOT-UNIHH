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
import com.unihh.iot.admin.service.LogService;
import com.unihh.iot.common.model.IotLog;
import com.unihh.iot.common.model.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/logs", "/admin/logs"})
public class LogController {

    @Autowired
    private LogService logService;

    @GetMapping
    @RequiresPermissions("log:list")
    public R<List<IotLog>> getLogs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String logType,
            @RequestParam(required = false) String level
    ) {
        return R.ok(logService.getLogList(keyword, logType, level));
    }

    @PostMapping
    public R<String> recordLog(@RequestBody IotLog log) {
        boolean success = logService.recordLog(log);
        return success ? R.ok("日志记录成功") : R.fail("日志记录失败");
    }

    @DeleteMapping
    @RequiresPermissions(value = {"log:edit", "log:list"}, logical = Logical.OR)
    @AuditLog(module = "日志管理", action = "清空系统运行与审计日志")
    public R<String> clearLogs() {
        boolean success = logService.clearLogs();
        return success ? R.ok("日志清空成功") : R.fail("清空失败");
    }
}
