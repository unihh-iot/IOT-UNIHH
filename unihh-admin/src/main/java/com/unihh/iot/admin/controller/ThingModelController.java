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
import com.unihh.iot.admin.service.ThingModelService;
import com.unihh.iot.common.model.R;
import com.unihh.iot.common.model.ThingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/thingmodels", "/admin/thingmodels"})
public class ThingModelController {

    @Autowired
    private ThingModelService thingModelService;

    @GetMapping
    @RequiresPermissions("thingmodel:list")
    public R<List<ThingModel>> getAllThingModels() {
        List<ThingModel> models = thingModelService.getAllThingModels();
        return R.ok(models);
    }

    @GetMapping("/{modelKey}")
    @RequiresPermissions("thingmodel:list")
    public R<ThingModel> getThingModel(@PathVariable("modelKey") String modelKey) {
        ThingModel model = thingModelService.getThingModel(modelKey);
        return R.ok(model);
    }

    @PostMapping
    @RequiresPermissions(value = {"thingmodel:add", "thingmodel:edit"}, logical = Logical.OR)
    @AuditLog(module = "物模型管理", action = "新建物模型模板")
    public R<String> createModel(@RequestBody ThingModel model) {
        boolean success = thingModelService.saveOrUpdateModel(model);
        return success ? R.ok("新增物模型模板成功") : R.fail(500, "新增物模型模板失败");
    }

    @PutMapping("/{modelKey}")
    @RequiresPermissions(value = {"thingmodel:edit", "thingmodel:add"}, logical = Logical.OR)
    @AuditLog(module = "物模型管理", action = "更新物模型基础信息")
    public R<String> updateModel(@PathVariable("modelKey") String modelKey, @RequestBody ThingModel model) {
        model.setModelKey(modelKey);
        boolean success = thingModelService.saveOrUpdateModel(model);
        return success ? R.ok("更新物模型模板成功") : R.fail(500, "更新物模型模板失败");
    }

    @DeleteMapping("/{modelKey}")
    @RequiresPermissions(value = {"thingmodel:delete", "thingmodel:edit"}, logical = Logical.OR)
    @AuditLog(module = "物模型管理", action = "删除物模型模板")
    public R<String> deleteModel(@PathVariable("modelKey") String modelKey) {
        try {
            boolean success = thingModelService.deleteModel(modelKey);
            return success ? R.ok("删除物模型模板成功") : R.fail(500, "删除物模型模板失败");
        } catch (IllegalStateException e) {
            return R.fail(400, e.getMessage());
        } catch (Exception e) {
            return R.fail(500, e.getMessage() != null ? e.getMessage() : "删除物模型模板失败");
        }
    }

    @PostMapping("/{modelKey}/properties")
    @RequiresPermissions(value = {"thingmodel:design", "thingmodel:edit"}, logical = Logical.OR)
    @AuditLog(module = "物模型管理", action = "新增属性点位")
    public R<String> addProperty(@PathVariable("modelKey") String modelKey, @RequestBody ThingModel.PropertySpec prop) {
        boolean success = thingModelService.addProperty(modelKey, prop);
        return success ? R.ok("新增属性点位成功") : R.fail(500, "新增属性点位失败");
    }

    @PutMapping("/{modelKey}/properties/{pointCode}")
    @RequiresPermissions(value = {"thingmodel:design", "thingmodel:edit"}, logical = Logical.OR)
    @AuditLog(module = "物模型管理", action = "更新属性点位")
    public R<String> updateProperty(@PathVariable("modelKey") String modelKey,
                                    @PathVariable("pointCode") String pointCode,
                                    @RequestBody ThingModel.PropertySpec prop) {
        boolean success = thingModelService.updateProperty(modelKey, pointCode, prop);
        return success ? R.ok("更新属性点位成功") : R.fail(500, "更新属性点位失败");
    }

    @DeleteMapping("/{modelKey}/properties/{pointCode}")
    @RequiresPermissions(value = {"thingmodel:design", "thingmodel:edit"}, logical = Logical.OR)
    @AuditLog(module = "物模型管理", action = "删除属性点位")
    public R<String> deleteProperty(@PathVariable("modelKey") String modelKey, @PathVariable("pointCode") String pointCode) {
        boolean success = thingModelService.deleteProperty(modelKey, pointCode);
        return success ? R.ok("删除属性点位成功") : R.fail(500, "删除属性点位失败");
    }

    @PostMapping("/{modelKey}/services")
    @RequiresPermissions(value = {"thingmodel:design", "thingmodel:edit"}, logical = Logical.OR)
    @AuditLog(module = "物模型管理", action = "新增控制服务")
    public R<String> addService(@PathVariable("modelKey") String modelKey, @RequestBody ThingModel.ServiceSpec service) {
        boolean success = thingModelService.addService(modelKey, service);
        return success ? R.ok("新增控制服务成功") : R.fail(500, "新增控制服务失败");
    }

    @PutMapping("/{modelKey}/services/{serviceCode}")
    @RequiresPermissions(value = {"thingmodel:design", "thingmodel:edit"}, logical = Logical.OR)
    @AuditLog(module = "物模型管理", action = "更新控制服务")
    public R<String> updateService(@PathVariable("modelKey") String modelKey,
                                   @PathVariable("serviceCode") String serviceCode,
                                   @RequestBody ThingModel.ServiceSpec service) {
        boolean success = thingModelService.updateService(modelKey, serviceCode, service);
        return success ? R.ok("更新控制服务成功") : R.fail(500, "更新控制服务失败");
    }

    @DeleteMapping("/{modelKey}/services/{serviceCode}")
    @RequiresPermissions(value = {"thingmodel:design", "thingmodel:edit"}, logical = Logical.OR)
    @AuditLog(module = "物模型管理", action = "删除控制服务")
    public R<String> deleteService(@PathVariable("modelKey") String modelKey, @PathVariable("serviceCode") String serviceCode) {
        boolean success = thingModelService.deleteService(modelKey, serviceCode);
        return success ? R.ok("删除控制服务成功") : R.fail(500, "删除控制服务失败");
    }

    @PostMapping("/{modelKey}/events")
    @RequiresPermissions(value = {"thingmodel:design", "thingmodel:edit"}, logical = Logical.OR)
    @AuditLog(module = "物模型管理", action = "新增告警事件")
    public R<String> addEvent(@PathVariable("modelKey") String modelKey, @RequestBody ThingModel.EventSpec event) {
        boolean success = thingModelService.addEvent(modelKey, event);
        return success ? R.ok("新增告警事件成功") : R.fail(500, "新增告警事件失败");
    }

    @PutMapping("/{modelKey}/events/{eventCode}")
    @RequiresPermissions(value = {"thingmodel:design", "thingmodel:edit"}, logical = Logical.OR)
    @AuditLog(module = "物模型管理", action = "更新告警事件")
    public R<String> updateEvent(@PathVariable("modelKey") String modelKey,
                                 @PathVariable("eventCode") String eventCode,
                                 @RequestBody ThingModel.EventSpec event) {
        boolean success = thingModelService.updateEvent(modelKey, eventCode, event);
        return success ? R.ok("更新告警事件成功") : R.fail(500, "更新告警事件失败");
    }

    @DeleteMapping("/{modelKey}/events/{eventCode}")
    @RequiresPermissions(value = {"thingmodel:design", "thingmodel:edit"}, logical = Logical.OR)
    @AuditLog(module = "物模型管理", action = "删除告警事件")
    public R<String> deleteEvent(@PathVariable("modelKey") String modelKey, @PathVariable("eventCode") String eventCode) {
        boolean success = thingModelService.deleteEvent(modelKey, eventCode);
        return success ? R.ok("删除告警事件成功") : R.fail(500, "删除告警事件失败");
    }

    @DeleteMapping("/template/{modelKey}")
    @RequiresPermissions(value = {"thingmodel:delete", "thingmodel:edit"}, logical = Logical.OR)
    @AuditLog(module = "物模型管理", action = "删除物模型模板")
    public R<String> deleteModelTemplate(@PathVariable("modelKey") String modelKey) {
        try {
            boolean success = thingModelService.deleteModel(modelKey);
            return success ? R.ok("删除物模型模板成功") : R.fail(500, "删除物模型模板失败");
        } catch (IllegalStateException e) {
            return R.fail(400, e.getMessage());
        } catch (Exception e) {
            return R.fail(500, e.getMessage() != null ? e.getMessage() : "删除物模型模板失败");
        }
    }
}
