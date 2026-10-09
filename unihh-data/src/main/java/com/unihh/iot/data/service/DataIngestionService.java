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

package com.unihh.iot.data.service;

import com.unihh.iot.common.model.HistoryQuery;
import com.unihh.iot.common.model.PointData;
import com.unihh.iot.storage.router.StorageEngineRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class DataIngestionService {
    private static final Logger log = LoggerFactory.getLogger(DataIngestionService.class);
    private final StorageEngineRouter router = new StorageEngineRouter();
    private final Map<String, List<SseEmitter>> sseSubscribers = new ConcurrentHashMap<>();

    public SseEmitter createSseSubscription(String deviceId) {
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
        List<SseEmitter> list = sseSubscribers.computeIfAbsent(deviceId, k -> new CopyOnWriteArrayList<>());
        list.add(emitter);

        emitter.onCompletion(() -> list.remove(emitter));
        emitter.onTimeout(() -> {
            list.remove(emitter);
            emitter.complete();
        });
        emitter.onError(e -> list.remove(emitter));

        // 建立连接后立即下发最新快照作为初始基线
        try {
            Map<String, PointData> snapshot = getLatestSnapshot(deviceId);
            if (snapshot != null && !snapshot.isEmpty()) {
                emitter.send(SseEmitter.event().name("snapshot").data(snapshot));
            }
        } catch (Exception e) {
            log.warn("SSE 初始化快照推送失败: {}", e.getMessage());
        }

        return emitter;
    }

    private void broadcastData(PointData data) {
        if (data == null || data.getDeviceId() == null) return;
        List<SseEmitter> list = sseSubscribers.get(data.getDeviceId());
        if (list == null || list.isEmpty()) return;

        for (SseEmitter emitter : list) {
            try {
                emitter.send(SseEmitter.event().name("point-data").data(data));
            } catch (Exception e) {
                list.remove(emitter);
            }
        }
    }

    private void broadcastDataBatch(List<PointData> dataList) {
        if (dataList == null || dataList.isEmpty()) return;
        Map<String, List<PointData>> grouped = dataList.stream()
                .filter(d -> d != null && d.getDeviceId() != null)
                .collect(java.util.stream.Collectors.groupingBy(PointData::getDeviceId));

        grouped.forEach((devId, points) -> {
            List<SseEmitter> list = sseSubscribers.get(devId);
            if (list == null || list.isEmpty()) return;
            for (SseEmitter emitter : list) {
                try {
                    emitter.send(SseEmitter.event().name("batch-data").data(points));
                } catch (Exception e) {
                    list.remove(emitter);
                }
            }
        });
    }

    public void processPointData(PointData data) {
        try {
            router.save(data);
            broadcastData(data);
        } catch (Exception e) {
            log.error("点位数据保存异常: deviceId={}", data.getDeviceId(), e);
        }
    }

    public void processPointDataBatch(List<PointData> dataList) {
        try {
            router.saveBatch(dataList);
            broadcastDataBatch(dataList);
        } catch (Exception e) {
            log.error("批量点位数据保存异常 count={}", dataList.size(), e);
        }
    }

    public List<PointData> queryHistory(HistoryQuery query) {
        if (query == null) return java.util.Collections.emptyList();
        return router.queryHistory(query.getDeviceId(), query.getPointCode(),
                query.getStartTime(), query.getEndTime(), query.getLimit());
    }

    public Map<String, PointData> getLatestSnapshot(String deviceId) {
        return router.getLatestSnapshot(deviceId);
    }

    public String getActiveEngine() {
        return router.getActiveEngineType();
    }

    public boolean setActiveEngine(String engineType) {
        return router.setActiveEngine(engineType);
    }

    public List<String> getAvailableEngines() {
        return router.getAvailableEngines();
    }
}
