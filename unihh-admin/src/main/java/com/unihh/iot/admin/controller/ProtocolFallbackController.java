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
import com.unihh.iot.common.model.R;
import com.unihh.iot.admin.service.DeviceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

/**
 * 工业物联网控制与服务指令统一执行接口
 * 接收来自前端 /api/v1/protocol/command/send 的控制与服务下发请求并分发执行
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/protocol")
public class ProtocolFallbackController {
    private static final Logger log = LoggerFactory.getLogger(ProtocolFallbackController.class);

    @Autowired
    private DeviceService deviceService;

    @PostMapping("/command/send")
    @RequiresPermissions("device:control")
    @AuditLog(module = "驱动服务", action = "执行协议控制指令")
    public R<String> sendControlCommand(@RequestBody Map<String, Object> command) {
        if (command == null || !command.containsKey("deviceId")) {
            return R.fail(400, "缺少目标设备标识 (deviceId)");
        }

        String deviceId = (String) command.get("deviceId");
        String serviceCode = (String) command.getOrDefault("serviceCode", "writeProperty");
        Map<String, Object> params = (command.get("params") instanceof Map)
                ? (Map<String, Object>) command.get("params") : Collections.emptyMap();

        log.info("[Command Dispatcher] Received command for device [{}]: serviceCode={}, params={}",
                deviceId, serviceCode, params);

        // 1. 点位属性反写逻辑 (writeProperty 或携带具体点位键值)
        if ("writeProperty".equalsIgnoreCase(serviceCode) || command.containsKey("pointCode")) {
            String pointCode = (String) command.get("pointCode");
            Object value = command.get("value");

            if (pointCode == null && !params.isEmpty()) {
                Map.Entry<String, Object> entry = params.entrySet().iterator().next();
                pointCode = entry.getKey();
                value = entry.getValue();
            }

            if (pointCode == null || pointCode.trim().isEmpty()) {
                return R.fail(400, "未指定要写入的点位标识 (pointCode)");
            }

            String explicitDataType = command.get("dataType") != null ? command.get("dataType").toString() : null;
            boolean ok = deviceService.controlDevicePoint(deviceId, pointCode, value, explicitDataType);
            return ok ? R.ok(String.format("点位 [%s] 写入指令已成功下发至设备 [%s]: 目标值=%s", pointCode, deviceId, value))
                    : R.fail(500, "点位反控指令下发失败");
        }

        // 2. 物模型服务方法执行逻辑 (如 startCoolingFan, setRelay, resetFault 等)
        boolean ok = deviceService.invokeDeviceService(deviceId, serviceCode, params);
        return ok ? R.ok(String.format("控制服务 [%s] 指令已成功下发至设备 [%s]", serviceCode, deviceId))
                : R.fail(500, "控制服务下发失败");
    }
}
