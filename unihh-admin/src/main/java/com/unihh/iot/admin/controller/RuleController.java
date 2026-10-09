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
import com.unihh.iot.admin.service.RuleService;
import com.unihh.iot.common.model.Rule;
import com.unihh.iot.common.model.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/rules", "/admin/rules"})
public class RuleController {

    @Autowired
    private RuleService ruleService;

    @GetMapping
    @RequiresPermissions("rule:list")
    public R<List<Rule>> getRules(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String deviceId,
            @RequestParam(required = false) Boolean enabled
    ) {
        return R.ok(ruleService.getRuleList(keyword, deviceId, enabled));
    }

    @GetMapping("/{ruleId}")
    @RequiresPermissions("rule:list")
    public R<Rule> getRuleDetail(@PathVariable String ruleId) {
        Rule rule = ruleService.getRuleById(ruleId);
        if (rule == null) return R.fail("规则不存在: " + ruleId);
        return R.ok(rule);
    }

    @PostMapping
    @RequiresPermissions("rule:edit")
    @AuditLog(module = "规则管理", action = "新建告警联动规则")
    public R<String> addRule(@RequestBody Rule rule) {
        boolean success = ruleService.addRule(rule);
        return success ? R.ok("新建规则成功") : R.fail("新建规则失败");
    }

    @PutMapping("/{ruleId}")
    @RequiresPermissions("rule:edit")
    @AuditLog(module = "规则管理", action = "修改告警联动规则")
    public R<String> updateRule(@PathVariable String ruleId, @RequestBody Rule rule) {
        rule.setRuleId(ruleId);
        boolean success = ruleService.updateRule(rule);
        return success ? R.ok("更新规则成功") : R.fail("更新规则失败");
    }

    @PutMapping("/{ruleId}/toggle")
    @RequiresPermissions("rule:edit")
    @AuditLog(module = "规则管理", action = "切换规则使能开关")
    public R<String> toggleRule(@PathVariable String ruleId, @RequestBody Map<String, Object> body) {
        Boolean enabled = body.get("enabled") != null ? (Boolean) body.get("enabled") : true;
        boolean success = ruleService.toggleRule(ruleId, enabled);
        return success ? R.ok("规则状态更新成功") : R.fail("更新失败");
    }

    @DeleteMapping("/{ruleId}")
    @RequiresPermissions("rule:edit")
    @AuditLog(module = "规则管理", action = "删除告警联动规则")
    public R<String> deleteRule(@PathVariable String ruleId) {
        boolean success = ruleService.deleteRule(ruleId);
        return success ? R.ok("删除规则成功") : R.fail("删除规则失败");
    }
}
