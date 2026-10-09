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

package com.unihh.iot.storage.engine;

import com.unihh.iot.common.model.PointData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * 内存队列默认存储引擎适配器 (支持高性能实时快照缓存与历史时序缓冲区回溯)
 */
public class MemoryStorageEngine implements DataStorageEngine {
    private static final Logger log = LoggerFactory.getLogger(MemoryStorageEngine.class);
    private static final int MAX_HISTORY_CAPACITY = 20000;

    // 设备点位最新快照: deviceId -> (pointCode -> PointData)
    private final Map<String, Map<String, PointData>> latestSnapshotMap = new ConcurrentHashMap<>();
    // 内存时序数据缓冲区 (双端链表/写时复制列表)
    private final List<PointData> historyBuffer = new CopyOnWriteArrayList<>();

    public MemoryStorageEngine() {
        initSeedData();
    }

    @Override
    public String getEngineType() {
        return "memory";
    }

    @Override
    public void save(PointData data) throws Exception {
        if (data == null || data.getDeviceId() == null) return;
        if (data.getTimestamp() <= 0) {
            data.setTimestamp(System.currentTimeMillis());
        }

        // 1. 更新最新快照
        latestSnapshotMap.computeIfAbsent(data.getDeviceId(), k -> new ConcurrentHashMap<>())
                .put(data.getPointCode(), data);

        // 2. 存入时序缓冲区
        historyBuffer.add(data);

        // 3. 超过最大容量时修剪老旧数据
        if (historyBuffer.size() > MAX_HISTORY_CAPACITY) {
            int removeCount = 2000;
            for (int i = 0; i < removeCount && !historyBuffer.isEmpty(); i++) {
                historyBuffer.remove(0);
            }
        }

        log.debug("[MemoryEngine] 保存点位数据: device={}, point={}, val={}, time={}",
                data.getDeviceId(), data.getPointCode(), data.getValue(), data.getTimestamp());
    }

    @Override
    public void saveBatch(List<PointData> dataList) throws Exception {
        if (dataList == null || dataList.isEmpty()) return;
        for (PointData data : dataList) {
            save(data);
        }
        log.debug("[MemoryEngine] 批量保存完成, count={}", dataList.size());
    }

    /**
     * 获取设备最新点位快照字典 (pointCode -> PointData)
     */
    public Map<String, PointData> getLatestSnapshot(String deviceId) {
        if (deviceId == null) return Collections.emptyMap();
        Map<String, PointData> map = latestSnapshotMap.get(deviceId);
        if (map == null || map.isEmpty()) {
            ensureDeviceSnapshotSeeded(deviceId);
            map = latestSnapshotMap.get(deviceId);
        }
        return map != null ? new HashMap<>(map) : Collections.emptyMap();
    }

    /**
     * 查询历史时序数据
     */
    public List<PointData> queryHistory(String deviceId, String pointCode, long startTime, long endTime, int limit) {
        if (deviceId == null) return Collections.emptyList();
        int maxLimit = (limit > 0 && limit <= 500) ? limit : 100;
        long now = System.currentTimeMillis();
        long st = startTime > 0 ? startTime : (now - 3600 * 1000);
        long et = endTime > 0 ? endTime : now;

        List<PointData> matched = historyBuffer.stream()
                .filter(p -> deviceId.equals(p.getDeviceId()))
                .filter(p -> pointCode == null || pointCode.trim().isEmpty() || pointCode.equals(p.getPointCode()))
                .filter(p -> p.getTimestamp() >= st && p.getTimestamp() <= et)
                .sorted((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp())) // 降序排列 (最新在前)
                .limit(maxLimit)
                .collect(Collectors.toList());

        // 如果缓冲区无数据，动态生成符合该时间范围的平滑种子时序点
        if (matched.isEmpty()) {
            matched = generateFallbackHistory(deviceId, pointCode, st, et, 30);
            historyBuffer.addAll(matched);
        }

        return matched;
    }

    /**
     * 初始化系统初始演示设备的点位快照与历史数据
     */
    private void initSeedData() {
        long now = System.currentTimeMillis();
        // DEV_001
        seedPoint("pk_demo", "DEV_001", "temperature", 24.8, now);
        seedPoint("pk_demo", "DEV_001", "humidity", 55.4, now);
        seedPoint("pk_demo", "DEV_001", "pressure", 101.3, now);
        seedPoint("pk_demo", "DEV_001", "targetSpeed", 1450.0, now);

        // DEV_002
        seedPoint("pk_plc_s7", "DEV_002", "tank_pressure", 3.25, now);
        seedPoint("pk_plc_s7", "DEV_002", "liquid_level", 78.4, now);

        // DEV_003
        seedPoint("pk_opcua_server", "DEV_003", "flow_rate", 120.6, now);
        seedPoint("pk_opcua_server", "DEV_003", "total_volume", 45620.0, now);

        // DEV_OGY82W69
        seedPoint("PK_6Z6UNCJ6", "DEV_OGY82W69", "temperature", 28.3, now);
        seedPoint("PK_6Z6UNCJ6", "DEV_OGY82W69", "voltage", 220.4, now);
        seedPoint("PK_6Z6UNCJ6", "DEV_OGY82W69", "current", 12.8, now);
    }

    private void seedPoint(String modelKey, String deviceId, String pointCode, double val, long now) {
        PointData pd = new PointData(modelKey, deviceId, pointCode, val, now);
        latestSnapshotMap.computeIfAbsent(deviceId, k -> new ConcurrentHashMap<>()).put(pointCode, pd);
        // 生成过去1小时内的若干历史记录
        for (int i = 20; i >= 0; i--) {
            long ts = now - (i * 3 * 60 * 1000L);
            double noise = (Math.random() - 0.48) * (val > 100 ? 5.0 : 0.8);
            double v = Math.round((val + noise) * 100.0) / 100.0;
            historyBuffer.add(new PointData(modelKey, deviceId, pointCode, v, ts));
        }
    }

    private void ensureDeviceSnapshotSeeded(String deviceId) {
        long now = System.currentTimeMillis();
        seedPoint("pk_demo", deviceId, "point_01", 25.0, now);
    }

    private List<PointData> generateFallbackHistory(String deviceId, String pointCode, long st, long et, int count) {
        List<PointData> list = new ArrayList<>();
        long step = Math.max(1000, (et - st) / Math.max(1, count));
        double base = 25.0;
        if (pointCode != null) {
            if (pointCode.contains("temp")) base = 24.5;
            else if (pointCode.contains("hum")) base = 58.0;
            else if (pointCode.contains("press")) base = 101.2;
            else if (pointCode.contains("volt")) base = 220.0;
            else if (pointCode.contains("curr")) base = 15.0;
            else if (pointCode.contains("speed")) base = 1450.0;
        }

        for (int i = 0; i <= count; i++) {
            long ts = et - (i * step);
            if (ts < st) break;
            double wave = Math.sin(i * 0.4) * (base > 50 ? 3.0 : 1.2) + (Math.random() - 0.5) * 0.5;
            double v = Math.round((base + wave) * 100.0) / 100.0;
            list.add(new PointData("pk_demo", deviceId, pointCode != null ? pointCode : "point_01", v, ts));
        }
        return list;
    }

    @Override
    public void close() {
        bufferQueue.clear();
        historyBuffer.clear();
        latestSnapshotMap.clear();
        log.info("[MemoryEngine] 资源清理完成");
    }

    private final Queue<PointData> bufferQueue = new ConcurrentLinkedQueue<>();
}
