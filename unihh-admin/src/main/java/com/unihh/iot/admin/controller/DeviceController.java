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
import com.unihh.iot.admin.service.DeviceService;
import com.unihh.iot.common.model.DeviceConfig;
import com.unihh.iot.common.model.DevicePointMapping;
import com.unihh.iot.common.model.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/devices", "/admin/devices"})
public class DeviceController {

    @Autowired
    private DeviceService deviceService;

    @GetMapping
    @RequiresPermissions("device:list")
    public R<List<DeviceConfig>> getDeviceList(@RequestParam(value = "keyword", required = false) String keyword,
                                               @RequestParam(value = "driverCode", required = false) String driverCode,
                                               @RequestParam(value = "acqStatus", required = false) Integer acqStatus) {
        List<DeviceConfig> devices = deviceService.getDeviceList(keyword, driverCode, acqStatus);
        return R.ok(devices);
    }

    @GetMapping("/{deviceId}")
    @RequiresPermissions("device:list")
    public R<DeviceConfig> getDeviceConfig(@PathVariable("deviceId") String deviceId) {
        DeviceConfig config = deviceService.getDeviceById(deviceId);
        if (config == null) {
            return R.fail(404, "设备未在 MySQL 数据库中找到: " + deviceId);
        }
        return R.ok(config);
    }

    @PostMapping
    @RequiresPermissions("device:add")
    @AuditLog(module = "设备管理", action = "新增接入物理设备")
    public R<String> addDevice(@RequestBody DeviceConfig device) {
        if (device.getDeviceId() == null || device.getDeviceId().trim().isEmpty()) {
            return R.fail(400, "设备唯一ID不能为空");
        }
        boolean success = deviceService.addDevice(device);
        return success ? R.ok("新增设备接入成功") : R.fail(500, "新增设备失败");
    }

    @PutMapping("/{deviceId}")
    @RequiresPermissions(value = {"device:add", "device:edit"}, logical = Logical.OR)
    @AuditLog(module = "设备管理", action = "更新物理设备配置")
    public R<String> updateDevice(@PathVariable("deviceId") String deviceId, @RequestBody DeviceConfig device) {
        device.setDeviceId(deviceId);
        boolean success = deviceService.updateDevice(device);
        return success ? R.ok("更新设备成功") : R.fail(500, "更新设备失败");
    }

    @PutMapping("/{deviceId}/acq-status")
    @RequiresPermissions("device:control")
    @AuditLog(module = "设备管理", action = "切换设备数采状态")
    public R<String> updateAcqStatus(@PathVariable("deviceId") String deviceId, @RequestParam("acqStatus") int acqStatus) {
        boolean success = deviceService.updateAcqStatus(deviceId, acqStatus);
        return success ? R.ok("更新设备数采状态成功") : R.fail(500, "更新设备数采状态失败");
    }

    @DeleteMapping("/{deviceId}")
    @RequiresPermissions(value = {"device:add", "device:edit"}, logical = Logical.OR)
    @AuditLog(module = "设备管理", action = "删除物理设备")
    public R<String> deleteDevice(@PathVariable("deviceId") String deviceId) {
        boolean success = deviceService.deleteDevice(deviceId);
        return success ? R.ok("删除设备成功") : R.fail(500, "删除设备失败");
    }

    @GetMapping("/{deviceId}/points")
    @RequiresPermissions("device:list")
    public R<List<DevicePointMapping>> getPointAddressMappings(@PathVariable("deviceId") String deviceId) {
        List<DevicePointMapping> list = deviceService.getPointAddressMappings(deviceId);
        return R.ok(list);
    }

    @GetMapping("/{deviceId}/points/suggested")
    @RequiresPermissions("device:list")
    public R<List<DevicePointMapping>> suggestPointAddressMappings(@PathVariable("deviceId") String deviceId) {
        List<DevicePointMapping> list = deviceService.suggestPointAddressMappings(deviceId);
        return R.ok(list);
    }

    @RequestMapping(value = "/{deviceId}/points", method = {RequestMethod.POST, RequestMethod.PUT})
    @RequiresPermissions(value = {"device:add", "device:edit"}, logical = Logical.OR)
    @AuditLog(module = "设备管理", action = "保存物理点位地址映射")
    public R<String> savePointAddressMappings(@PathVariable("deviceId") String deviceId, @RequestBody List<DevicePointMapping> mappings) {
        boolean success = deviceService.savePointAddressMappings(deviceId, mappings);
        return success ? R.ok("保存物理点位地址映射成功") : R.fail(500, "保存地址映射失败");
    }

    @PostMapping("/{deviceId}/control")
    @RequiresPermissions("device:control")
    @AuditLog(module = "设备管理", action = "下发设备点位写入控制")
    public R<String> controlDevicePoint(@PathVariable("deviceId") String deviceId,
                                        @RequestParam(value = "pointCode", required = false) String pointCode,
                                        @RequestParam(value = "value", required = false) Object value,
                                        @RequestParam(value = "dataType", required = false) String dataType,
                                        @RequestBody(required = false) Map<String, Object> body) {
        String finalPointCode = pointCode;
        Object finalValue = value;
        String finalDataType = dataType;

        if (body != null) {
            // 如果 body 携带了 serviceCode 并且不是 writeProperty，自动路由至服务调用
            if (body.containsKey("serviceCode") && !"writeProperty".equalsIgnoreCase((String) body.get("serviceCode"))) {
                String serviceCode = (String) body.get("serviceCode");
                Map<String, Object> params = (body.get("params") instanceof Map) ? (Map<String, Object>) body.get("params") : null;
                boolean success = deviceService.invokeDeviceService(deviceId, serviceCode, params);
                return success ? R.ok("设备服务指令 [" + serviceCode + "] 下发成功") : R.fail(500, "设备服务指令下发失败");
            }

            if (finalPointCode == null || finalPointCode.trim().isEmpty()) {
                if (body.containsKey("pointCode")) {
                    finalPointCode = (String) body.get("pointCode");
                } else if (body.containsKey("identifier")) {
                    finalPointCode = (String) body.get("identifier");
                } else if (body.containsKey("params") && body.get("params") instanceof Map) {
                    Map<String, Object> pMap = (Map<String, Object>) body.get("params");
                    if (!pMap.isEmpty()) {
                        Map.Entry<String, Object> entry = pMap.entrySet().iterator().next();
                        finalPointCode = entry.getKey();
                        finalValue = entry.getValue();
                    }
                }
            }
            if (finalValue == null) {
                if (body.containsKey("value")) finalValue = body.get("value");
                else if (body.containsKey("val")) finalValue = body.get("val");
            }
            if (finalDataType == null || finalDataType.trim().isEmpty()) {
                if (body.containsKey("dataType") && body.get("dataType") != null) {
                    finalDataType = String.valueOf(body.get("dataType"));
                }
            }
        }

        if (finalPointCode == null || finalPointCode.trim().isEmpty()) {
            return R.fail(400, "缺少控制点位标识 (pointCode)");
        }

        boolean success = deviceService.controlDevicePoint(deviceId, finalPointCode, finalValue, finalDataType);
        return success ? R.ok("点位 [" + finalPointCode + "] 反控指令下发成功: 目标值=" + finalValue) : R.fail(500, "设备反控指令下发失败");
    }

    @PostMapping("/{deviceId}/services/{serviceCode}")
    @RequiresPermissions("device:control")
    @AuditLog(module = "设备管理", action = "下发设备服务指令")
    public R<String> invokeDeviceService(@PathVariable("deviceId") String deviceId,
                                         @PathVariable("serviceCode") String serviceCode,
                                         @RequestBody(required = false) Map<String, Object> params) {
        boolean success = deviceService.invokeDeviceService(deviceId, serviceCode, params);
        return success ? R.ok("服务指令 [" + serviceCode + "] 下发成功") : R.fail(500, "服务指令下发失败");
    }
}
