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

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.unihh.iot.admin.dao.DeviceDao;
import com.unihh.iot.admin.dao.DriverInfoDao;
import com.unihh.iot.admin.mqtt.AdminMqttConsumer;
import com.unihh.iot.admin.service.DriverService;
import com.unihh.iot.common.model.DriverInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DriverServiceImpl implements DriverService {
    private static final Logger log = LoggerFactory.getLogger(DriverServiceImpl.class);

    @Autowired
    private DriverInfoDao driverInfoDao;

    @Autowired
    private DeviceDao deviceDao;

    @Autowired
    @Lazy
    private AdminMqttConsumer adminMqttConsumer;

    @Override
    public List<DriverInfo> getDriverList() {
        List<DriverInfo> list = driverInfoDao.findAll();
        populateDriverMetrics(list);
        return list;
    }

    @Override
    public Map<String, Object> getDriverPage(String keyword, Integer status, Integer pageNum, Integer pageSize) {
        int page = (pageNum != null && pageNum > 0) ? pageNum : 1;
        int size = (pageSize != null && pageSize > 0) ? pageSize : 10;

        List<DriverInfo> list = driverInfoDao.findPage(keyword, status, page, size);
        int total = driverInfoDao.count(keyword, status);
        populateDriverMetrics(list);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("list", list);
        resultMap.put("total", total);
        resultMap.put("pageNum", page);
        resultMap.put("pageSize", size);
        return resultMap;
    }

    private void populateDriverMetrics(List<DriverInfo> list) {
        if (list == null || list.isEmpty()) return;
        for (DriverInfo info : list) {
            int devCount = deviceDao.countByDriverCode(info.getDriverCode());
            info.setDeviceCount(devCount);
            info.setChannelCount(info.getStatus() != null && info.getStatus() == 1 ? devCount : 0);
        }
    }

    @Override
    public DriverInfo getDriverByCode(String driverCode) {
        DriverInfo info = driverInfoDao.findByCode(driverCode);
        if (info != null) {
            int devCount = deviceDao.countByDriverCode(info.getDriverCode());
            info.setDeviceCount(devCount);
            info.setChannelCount(info.getStatus() != null && info.getStatus() == 1 ? devCount : 0);
        }
        return info;
    }

    @Override
    public boolean addDriver(DriverInfo driverInfo) {
        DriverInfo existing = driverInfoDao.findByCode(driverInfo.getDriverCode());
        if (existing != null) {
            return driverInfoDao.update(driverInfo) > 0;
        }
        return driverInfoDao.insert(driverInfo) > 0;
    }

    @Override
    public boolean updateStatus(String driverCode, int status) {
        return driverInfoDao.updateStatus(driverCode, status) > 0;
    }

    @Override
    public boolean deleteDriver(String driverCode) {
        return driverInfoDao.deleteByCode(driverCode) > 0;
    }

    @Override
    public Map<String, Object> pingDriver(String driverCode) {
        Map<String, Object> result = new HashMap<>();
        DriverInfo driver = driverInfoDao.findByCode(driverCode);
        if (driver == null) {
            result.put("online", false);
            result.put("latencyMs", -1);
            result.put("message", "未找到驱动标识 [" + driverCode + "] 的注册信息");
            return result;
        }

        long startTime = System.currentTimeMillis();
        boolean isAlive = false;
        int activeDeviceCount = 0;
        int channelCount = 0;
        String probeDetail = "";

        // 1. 若配置了 serviceUrl，优先通过 HTTP 健康端点探测
        String serviceUrl = driver.getServiceUrl();
        if (serviceUrl != null && !serviceUrl.trim().isEmpty()) {
            HttpURLConnection conn = null;
            try {
                String checkUrl = serviceUrl.trim();
                if (checkUrl.endsWith("/")) {
                    checkUrl = checkUrl.substring(0, checkUrl.length() - 1);
                }
                checkUrl += "/api/v1/driver/status";

                conn = (HttpURLConnection) new URL(checkUrl).openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(2000);
                conn.setReadTimeout(2000);
                int respCode = conn.getResponseCode();
                if (respCode == 200) {
                    isAlive = true;
                    try (InputStream in = conn.getInputStream();
                         ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                        byte[] buf = new byte[1024];
                        int len;
                        while ((len = in.read(buf)) != -1) {
                            out.write(buf, 0, len);
                        }
                        String body = out.toString(StandardCharsets.UTF_8.name());
                        JSONObject json = JSON.parseObject(body);
                        if (json != null && json.getJSONObject("data") != null) {
                            JSONObject data = json.getJSONObject("data");
                            activeDeviceCount = data.getIntValue("activeDeviceCount");
                            if (data.containsKey("devices") && data.get("devices") instanceof Map) {
                                channelCount = data.getJSONObject("devices").size();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                probeDetail = "HTTP 探测无响应 (" + e.getMessage() + ")";
            } finally {
                if (conn != null) {
                    try { conn.disconnect(); } catch (Exception ignored) {}
                }
            }
        }

        // 2. 结合 MQTT 实时心跳进行探测与校验
        if (!isAlive && adminMqttConsumer != null && adminMqttConsumer.isDriverAlive(driverCode)) {
            isAlive = true;
            probeDetail = "MQTT 链路心跳活跃";
        }

        long latency = System.currentTimeMillis() - startTime;
        int dbDevCount = deviceDao.countByDriverCode(driverCode);
        if (activeDeviceCount == 0 && dbDevCount > 0 && isAlive) {
            activeDeviceCount = dbDevCount;
            channelCount = dbDevCount;
        }

        if (isAlive) {
            driverInfoDao.updateStatus(driverCode, 1);
            if (adminMqttConsumer != null) {
                adminMqttConsumer.recordHeartbeat(driverCode);
            }
            result.put("online", true);
            result.put("latencyMs", latency);
            result.put("activeDeviceCount", activeDeviceCount);
            result.put("channelCount", channelCount > 0 ? channelCount : activeDeviceCount);
            result.put("message", "通道心跳自检正常，驱动运行良好" + (probeDetail.isEmpty() ? "" : " (" + probeDetail + ")"));
            log.info("[DriverService] Self-check ping passed for driver [{}]: latency={}ms, devices={}",
                    driverCode, latency, activeDeviceCount);
        } else {
            driverInfoDao.updateStatus(driverCode, 0);
            deviceDao.updateStatusByDriverCode(driverCode, 0);
            result.put("online", false);
            result.put("latencyMs", latency);
            result.put("activeDeviceCount", 0);
            result.put("channelCount", 0);
            result.put("message", "通道心跳自检失败：驱动服务未响应或处于离线状态" + (probeDetail.isEmpty() ? "" : " (" + probeDetail + ")"));
            log.warn("[DriverService] Self-check ping failed for driver [{}]. Marked OFFLINE in database.", driverCode);
        }

        return result;
    }
}
