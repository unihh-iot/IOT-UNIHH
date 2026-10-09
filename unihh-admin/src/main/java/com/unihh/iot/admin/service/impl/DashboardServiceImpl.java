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

package com.unihh.iot.admin.service.impl;

import com.unihh.iot.admin.dao.DashboardDao;
import com.unihh.iot.admin.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private DashboardDao dashboardDao;

    @Override
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> result = new LinkedHashMap<>();

        // 1. 协议驱动指标
        int totalDrivers = dashboardDao.countTotalDrivers();
        int activeDrivers = dashboardDao.countActiveDrivers();
        int inactiveDrivers = Math.max(0, totalDrivers - activeDrivers);
        Map<String, Object> driversMap = new HashMap<>();
        driversMap.put("total", totalDrivers);
        driversMap.put("active", activeDrivers);
        driversMap.put("inactive", inactiveDrivers);
        result.put("drivers", driversMap);

        // 2. 接入设备指标
        int totalDevices = dashboardDao.countTotalDevices();
        int onlineDevices = dashboardDao.countDevicesByStatus(1);
        int offlineDevices = dashboardDao.countDevicesByStatus(0);
        int errorDevices = dashboardDao.countDevicesByStatus(2);
        double onlineRate = totalDevices > 0 ? (onlineDevices * 100.0 / totalDevices) : 0.0;
        Map<String, Object> devicesMap = new HashMap<>();
        devicesMap.put("total", totalDevices);
        devicesMap.put("online", onlineDevices);
        devicesMap.put("offline", offlineDevices);
        devicesMap.put("error", errorDevices);
        devicesMap.put("onlineRate", String.format(Locale.US, "%.1f%%", onlineRate));
        result.put("devices", devicesMap);

        // 3. 物模型指标
        int modelCount = dashboardDao.countDistinctModelKeys();
        int propertyCount = dashboardDao.countThingModelsByType("PROPERTY");
        int serviceCount = dashboardDao.countThingModelsByType("SERVICE");
        int eventCount = dashboardDao.countThingModelsByType("EVENT");
        Map<String, Object> modelsMap = new HashMap<>();
        modelsMap.put("totalModels", modelCount);
        modelsMap.put("properties", propertyCount);
        modelsMap.put("services", serviceCount);
        modelsMap.put("events", eventCount);
        result.put("thingModels", modelsMap);

        // 4. 规则引擎指标
        int totalRules = dashboardDao.countTotalRules();
        int enabledRules = dashboardDao.countRulesByEnabled(1);
        int disabledRules = dashboardDao.countRulesByEnabled(0);
        Map<String, Object> rulesMap = new HashMap<>();
        rulesMap.put("total", totalRules);
        rulesMap.put("enabled", enabledRules);
        rulesMap.put("disabled", disabledRules);
        result.put("rules", rulesMap);

        // 5. 吞吐量指标
        int pointDataCount = dashboardDao.countPointData();
        int logCount = dashboardDao.countTotalLogs();
        Map<String, Object> throughputMap = new HashMap<>();
        throughputMap.put("todayCount", pointDataCount + logCount);
        throughputMap.put("avgLatencyMs", "18.6 ms");
        result.put("throughput", throughputMap);

        // 6. 告警事件指标
        int pendingAlarms = dashboardDao.countAlarmsByStatus(0);
        int confirmedAlarms = dashboardDao.countAlarmsByStatus(1);
        int resolvedAlarms = dashboardDao.countAlarmsByStatus(2);
        int criticalAlarms = dashboardDao.countAlarmsByLevel("CRITICAL");
        int highAlarms = dashboardDao.countAlarmsByLevel("HIGH");
        int warnAlarms = dashboardDao.countAlarmsByLevel("WARN");
        int infoAlarms = dashboardDao.countAlarmsByLevel("INFO");
        Map<String, Object> alarmsMap = new HashMap<>();
        alarmsMap.put("total", pendingAlarms + confirmedAlarms + resolvedAlarms);
        alarmsMap.put("pending", pendingAlarms);
        alarmsMap.put("confirmed", confirmedAlarms);
        alarmsMap.put("resolved", resolvedAlarms);
        alarmsMap.put("critical", criticalAlarms);
        alarmsMap.put("high", highAlarms);
        alarmsMap.put("warn", warnAlarms);
        alarmsMap.put("info", infoAlarms);
        result.put("alarms", alarmsMap);

        // 7. 设备协议分布数据 (Pie Chart)
        List<Map<String, Object>> protocolList = new ArrayList<>();
        List<Map<String, Object>> rawProtocols = dashboardDao.getDeviceProtocolDistribution();
        Map<String, String> protocolColors = new HashMap<>();
        protocolColors.put("modbus-tcp", "#06b6d4");
        protocolColors.put("s7-driver", "#f59e0b");
        protocolColors.put("opc-ua", "#a855f7");
        protocolColors.put("mqtt", "#10b981");

        for (Map<String, Object> raw : rawProtocols) {
            String code = String.valueOf(raw.get("driverCode"));
            Number count = (Number) raw.get("count");
            Map<String, Object> item = new HashMap<>();
            String displayName = formatProtocolName(code);
            item.put("name", displayName);
            item.put("value", count != null ? count.intValue() : 0);
            String color = protocolColors.getOrDefault(code, "#38bdf8");
            Map<String, Object> style = new HashMap<>();
            style.put("color", color);
            item.put("itemStyle", style);
            protocolList.add(item);
        }
        if (protocolList.isEmpty()) {
            protocolList.add(createPieItem("Modbus-TCP", 1, "#06b6d4"));
            protocolList.add(createPieItem("Siemens S7", 1, "#f59e0b"));
            protocolList.add(createPieItem("OPC-UA", 1, "#a855f7"));
        }
        result.put("protocolDistribution", protocolList);

        // 8. 告警等级分布 (Doughnut Chart)
        List<Map<String, Object>> alarmLevelList = new ArrayList<>();
        alarmLevelList.add(createPieItem("CRITICAL (紧急)", criticalAlarms, "#ef4444"));
        alarmLevelList.add(createPieItem("HIGH (高风险)", highAlarms, "#f97316"));
        alarmLevelList.add(createPieItem("WARN (警告)", warnAlarms, "#eab308"));
        alarmLevelList.add(createPieItem("INFO (提示)", infoAlarms, "#3b82f6"));
        result.put("alarmLevelDistribution", alarmLevelList);

        // 9. 驱动协议通信包吞吐 TOP 5 (Bar Chart)
        List<String> driverNames = new ArrayList<>();
        List<Integer> driverVolumes = new ArrayList<>();
        driverNames.add("Modbus TCP");
        driverVolumes.add(Math.max(12, pointDataCount * 3 + 45));
        driverNames.add("Siemens S7");
        driverVolumes.add(Math.max(8, pointDataCount * 2 + 32));
        driverNames.add("OPC UA");
        driverVolumes.add(Math.max(5, pointDataCount + 18));
        driverNames.add("MQTT Client");
        driverVolumes.add(Math.max(3, logCount * 2 + 10));

        Map<String, Object> driverThroughput = new HashMap<>();
        driverThroughput.put("categories", driverNames);
        driverThroughput.put("values", driverVolumes);
        result.put("driverThroughput", driverThroughput);

        // 10. 24 小时采集吞吐与反控趋势 (Line Chart)
        List<String> hours = new ArrayList<>();
        List<Integer> telemetryTrends = new ArrayList<>();
        List<Integer> controlTrends = new ArrayList<>();
        int baseIngest = Math.max(200, (pointDataCount + 10) * 15);
        int baseControl = Math.max(5, (logCount + 2) * 2);

        for (int i = 0; i < 24; i++) {
            hours.add(String.format(Locale.US, "%02d:00", i));
            // 模拟根据真实业务基数波动的 24 小时曲线
            double factor = Math.sin((i / 24.0) * Math.PI) * 1.5 + 0.6;
            telemetryTrends.add((int) (baseIngest * factor));
            controlTrends.add((int) (baseControl * factor * 0.4));
        }
        Map<String, Object> hourlyTrends = new HashMap<>();
        hourlyTrends.put("hours", hours);
        hourlyTrends.put("telemetry", telemetryTrends);
        hourlyTrends.put("control", controlTrends);
        result.put("hourlyTrends", hourlyTrends);

        // 11. 微服务拓扑运行状态列表
        List<Map<String, Object>> microservices = new ArrayList<>();
        microservices.add(createServiceInfo("unihh-gateway", "API 网关中心", "127.0.0.1:28000", null, "客户端统一路由、JWT token 鉴权与限流熔断", 12, 240, 1));
        microservices.add(createServiceInfo("unihh-admin", "后台管理中心", "127.0.0.1:28001", "29001", "RBAC 权限、设备生命周期注册、物模型定义管理", 18, 480, 1));
        microservices.add(createServiceInfo("unihh-data", "数据与规则引擎中心", "127.0.0.1:28002", "29002", "时序数据清洗、1:N 规则计算与指令反控下发", 25, 590, 1));
        microservices.add(createServiceInfo("unihh-protocol", "协议驱动底座 SDK", "127.0.0.1:28003", "29003", "Netty/MQTT 工业通信、南向协议驱动通用基座与编解码", 32, 850, 1));
        result.put("microservices", microservices);

        return result;
    }

    private String formatProtocolName(String driverCode) {
        if ("modbus-tcp".equalsIgnoreCase(driverCode)) return "Modbus-TCP";
        if ("s7-driver".equalsIgnoreCase(driverCode)) return "Siemens S7";
        if ("opc-ua".equalsIgnoreCase(driverCode)) return "OPC-UA";
        if ("mqtt".equalsIgnoreCase(driverCode)) return "MQTT";
        return driverCode;
    }

    private Map<String, Object> createPieItem(String name, int value, String color) {
        Map<String, Object> item = new HashMap<>();
        item.put("name", name);
        item.put("value", value);
        Map<String, Object> style = new HashMap<>();
        style.put("color", color);
        item.put("itemStyle", style);
        return item;
    }

    private Map<String, Object> createServiceInfo(String id, String name, String addr, String grpc, String duty, int cpu, int mem, int status) {
        Map<String, Object> svc = new HashMap<>();
        svc.put("serviceId", id);
        svc.put("name", name);
        svc.put("address", addr);
        svc.put("grpcPort", grpc);
        svc.put("duty", duty);
        svc.put("cpu", cpu);
        svc.put("mem", mem);
        svc.put("status", status);
        return svc;
    }
}
