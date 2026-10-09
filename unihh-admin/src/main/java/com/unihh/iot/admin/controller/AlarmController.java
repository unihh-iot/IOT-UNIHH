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
import com.unihh.iot.admin.service.AlarmService;
import com.unihh.iot.common.model.AlarmRecord;
import com.unihh.iot.common.model.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/alarms", "/admin/alarms"})
public class AlarmController {

    @Autowired
    private AlarmService alarmService;

    @GetMapping
    @RequiresPermissions("alarm:list")
    public R<List<AlarmRecord>> getAlarms(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String ruleId
    ) {
        return R.ok(alarmService.getAlarmList(keyword, level, status, ruleId));
    }

    @GetMapping("/{alarmId}")
    @RequiresPermissions("alarm:list")
    public R<AlarmRecord> getAlarmDetail(@PathVariable String alarmId) {
        AlarmRecord alarm = alarmService.getAlarmById(alarmId);
        if (alarm == null) return R.fail("告警记录不存在: " + alarmId);
        return R.ok(alarm);
    }

    @PostMapping("/{alarmId}/confirm")
    @RequiresPermissions("alarm:handle")
    @AuditLog(module = "告警中心", action = "确认告警信息")
    public R<String> confirmAlarm(@PathVariable String alarmId, @RequestBody(required = false) Map<String, String> body) {
        String handleUser = (body != null && body.containsKey("handleUser")) ? body.get("handleUser") : "admin";
        String remark = (body != null && body.containsKey("remark")) ? body.get("remark") : "值班人员手动确认告警信息";
        boolean success = alarmService.acknowledgeAlarm(alarmId, handleUser, remark);
        return success ? R.ok("告警确认成功") : R.fail("确认失败");
    }

    @PostMapping("/{alarmId}/resolve")
    @RequiresPermissions("alarm:handle")
    @AuditLog(module = "告警中心", action = "闭环恢复告警")
    public R<String> resolveAlarm(@PathVariable String alarmId, @RequestBody(required = false) Map<String, String> body) {
        String handleUser = (body != null && body.containsKey("handleUser")) ? body.get("handleUser") : "system";
        String remark = (body != null && body.containsKey("remark")) ? body.get("remark") : "告警故障恢复结单";
        boolean success = alarmService.resolveAlarm(alarmId, handleUser, remark);
        return success ? R.ok("告警解单成功") : R.fail("操作失败");
    }

    @DeleteMapping("/{alarmId}")
    @RequiresPermissions("alarm:handle")
    @AuditLog(module = "告警中心", action = "删除告警记录")
    public R<String> deleteAlarm(@PathVariable String alarmId) {
        boolean success = alarmService.deleteAlarm(alarmId);
        return success ? R.ok("删除告警记录成功") : R.fail("删除失败");
    }
}
