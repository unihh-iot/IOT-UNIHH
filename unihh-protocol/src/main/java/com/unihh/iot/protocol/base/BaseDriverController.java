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

package com.unihh.iot.protocol.base;

import com.unihh.iot.common.model.DeviceConfig;
import com.unihh.iot.common.model.R;
import com.unihh.iot.common.model.ThingModel;
import com.unihh.iot.protocol.model.ParsedPayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 驱动开箱即用通用在线调试与健康状态 REST 控制器
 * 所有继承了 AbstractDriverPlugin 的驱动子服务天然自带此接口，无需驱动手写任何 Controller。
 */
@RestController
@RequestMapping("/api/v1/driver")
public class BaseDriverController {

    @Autowired(required = false)
    private AbstractDriverPlugin driverPlugin;

    @Autowired
    private DriverPollingEngine pollingEngine;

    @Autowired
    private BaseDriverMqttManager mqttManager;

    @GetMapping("/status")
    public R<Map<String, Object>> getStatus() {
        Map<String, Object> data = new HashMap<>();
        String code = mqttManager.resolveDriverCode();
        data.put("driverCode", code);
        data.put("driverMetadata", (driverPlugin != null) ? driverPlugin.getMetadata() : null);
        data.put("activeDeviceCount", pollingEngine.getActiveDeviceCount());
        data.put("devices", pollingEngine.getAllDeviceStatuses());
        return R.ok(data);
    }

    @PostMapping("/test-connection")
    public R<String> testConnection(@RequestBody DeviceConfig config) {
        if (driverPlugin == null) {
            return R.fail(500, "驱动插件实例未装配");
        }
        try {
            boolean ok = driverPlugin.openConnection(config);
            return ok ? R.ok("链路连接测试成功") : R.fail("链路连接测试失败");
        } catch (Exception e) {
            return R.fail("连接测试异常: " + e.getMessage());
        }
    }

    @PostMapping("/read-points")
    public R<ParsedPayload> readPoints(@RequestBody ReadPointsRequest request) {
        if (driverPlugin == null) {
            return R.fail(500, "驱动插件实例未装配");
        }
        try {
            ParsedPayload payload = driverPlugin.readPoints(request.getConfig(), request.getPoints());
            return R.ok(payload);
        } catch (Exception e) {
            return R.fail("点位读取失败: " + e.getMessage());
        }
    }

    @PostMapping("/write-point")
    public R<String> writePoint(@RequestBody WritePointRequest request) {
        if (driverPlugin == null) {
            return R.fail(500, "驱动插件实例未装配");
        }
        try {
            boolean ok = driverPlugin.writePoint(
                    request.getConfig(),
                    request.getPointCode(),
                    request.getAddress(),
                    request.getValue(),
                    request.getDataType()
            );
            return ok ? R.ok("点位写入成功") : R.fail("点位写入失败");
        } catch (Exception e) {
            return R.fail("点位写入异常: " + e.getMessage());
        }
    }

    public static class ReadPointsRequest {
        private DeviceConfig config;
        private List<ThingModel.PropertySpec> points;

        public DeviceConfig getConfig() { return config; }
        public void setConfig(DeviceConfig config) { this.config = config; }
        public List<ThingModel.PropertySpec> getPoints() { return points; }
        public void setPoints(List<ThingModel.PropertySpec> points) { this.points = points; }
    }

    public static class WritePointRequest {
        private DeviceConfig config;
        private String pointCode;
        private String address;
        private Object value;
        private String dataType;

        public DeviceConfig getConfig() { return config; }
        public void setConfig(DeviceConfig config) { this.config = config; }
        public String getPointCode() { return pointCode; }
        public void setPointCode(String pointCode) { this.pointCode = pointCode; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public Object getValue() { return value; }
        public void setValue(Object value) { this.value = value; }
        public String getDataType() { return dataType; }
        public void setDataType(String dataType) { this.dataType = dataType; }
    }
}
