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

package com.unihh.iot.admin.grpc;

import com.unihh.iot.admin.service.*;
import com.unihh.iot.common.grpc.GrpcModels;
import com.unihh.iot.common.grpc.GrpcServerRunner;
import com.unihh.iot.common.model.AlarmRecord;
import com.unihh.iot.common.model.DeviceConfig;
import com.unihh.iot.common.model.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * unihh-admin 内部微服务 gRPC 服务端监听组件 (端口 :29001)
 */
@Component
public class AdminGrpcServer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminGrpcServer.class);

    @Value("${unihh.grpc.port:29001}")
    private int grpcPort;

    @Autowired
    private DeviceService deviceService;
    @Autowired
    private ThingModelService thingModelService;
    @Autowired
    private DriverService driverService;
    @Autowired
    private RuleService ruleService;
    @Autowired
    private AlarmService alarmService;
    @Autowired
    private LogService logService;

    @Override
    public void run(String... args) throws Exception {
        log.info("[Admin gRPC] 正在启动后台管理微服务内部 gRPC 服务端 (端口: {})...", grpcPort);

        GrpcServerRunner.start(grpcPort, "AdminGrpcService", request -> {
            String method = request.getMethod();
            log.info("[Admin gRPC 服务端接收 RPC 调取] method={}", method);

            try {
                switch (method) {
                    case "getDevices":
                        return GrpcModels.RpcResponse.ok(deviceService.getDeviceList(null, null, null));
                    case "getDevice":
                        String devId = request.getParamJson().replace("\"", "");
                        return GrpcModels.RpcResponse.ok(deviceService.getDeviceById(devId));
                    case "getPointAddressMappings":
                        String pDevId = request.getParamJson() != null ? request.getParamJson().replace("\"", "").trim() : "";
                        log.info("[Admin gRPC] 接收获取测点物理映射表请求: deviceId=[{}]", pDevId);
                        return GrpcModels.RpcResponse.ok(deviceService.getPointAddressMappings(pDevId));
                    case "addDevice":
                        DeviceConfig dev = request.parseParam(DeviceConfig.class);
                        return GrpcModels.RpcResponse.ok(deviceService.addDevice(dev));
                    case "getThingModel":
                        String tmpPk = request.getParamJson().replace("\"", "");
                        return GrpcModels.RpcResponse.ok(thingModelService.getThingModel(tmpPk));
                    case "getAllThingModels":
                        return GrpcModels.RpcResponse.ok(thingModelService.getAllThingModels());
                    case "getDrivers":
                        return GrpcModels.RpcResponse.ok(driverService.getDriverPage(null, null, 1, 10));
                    case "getRules":
                        return GrpcModels.RpcResponse.ok(ruleService.getRuleList(null, null, null));
                    case "addRule":
                        Rule rule = request.parseParam(Rule.class);
                        return GrpcModels.RpcResponse.ok(ruleService.addRule(rule));
                    case "deleteRule":
                        String deleteRuleId = request.getParamJson().replace("\"", "");
                        return GrpcModels.RpcResponse.ok(ruleService.deleteRule(deleteRuleId));
                    case "getAlarms":
                        return GrpcModels.RpcResponse.ok(alarmService.getAlarmList(null, null, null, null));
                    case "createAlarm":
                        AlarmRecord newAlarm = request.parseParam(AlarmRecord.class);
                        return GrpcModels.RpcResponse.ok(alarmService.createAlarm(newAlarm));
                    case "confirmAlarm":
                        String alarmId = request.getParamJson().replace("\"", "");
                        return GrpcModels.RpcResponse.ok(alarmService.acknowledgeAlarm(alarmId, "admin", "确认告警"));
                    case "resolveAlarm":
                        String resAlarmId = request.getParamJson().replace("\"", "");
                        return GrpcModels.RpcResponse.ok(alarmService.resolveAlarm(resAlarmId, "system", "恢复结单"));
                    case "getLogs":
                        return GrpcModels.RpcResponse.ok(logService.getLogList(null, null, null));
                    case "recordLog":
                        com.unihh.iot.common.model.IotLog newLog = request.parseParam(com.unihh.iot.common.model.IotLog.class);
                        return GrpcModels.RpcResponse.ok(logService.recordLog(newLog));
                    case "clearLogs":
                        return GrpcModels.RpcResponse.ok(logService.clearLogs());
                    default:
                        return GrpcModels.RpcResponse.fail("未知 Admin gRPC 方法: " + method);
                }
            } catch (Exception e) {
                log.error("[Admin gRPC 执行异常] method={}", method, e);
                return GrpcModels.RpcResponse.fail("Admin gRPC 执行失败: " + e.getMessage());
            }
        });
    }
}
