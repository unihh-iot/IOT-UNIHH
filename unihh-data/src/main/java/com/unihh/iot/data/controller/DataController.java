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

package com.unihh.iot.data.controller;

import com.unihh.iot.common.model.HistoryQuery;
import com.unihh.iot.common.model.PointData;
import com.unihh.iot.common.model.R;
import com.unihh.iot.data.service.DataIngestionService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/v1/data")
public class DataController {

    private final DataIngestionService ingestionService;

    public DataController(DataIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping("/ingest")
    public R<String> ingestPoint(@RequestBody PointData data) {
        if (data != null && data.getTimestamp() <= 0) {
            data.setTimestamp(System.currentTimeMillis());
        }
        ingestionService.processPointData(data);
        return R.ok("点位数据接入清洗完成");
    }

    @PostMapping("/ingest/batch")
    public R<String> ingestBatch(@RequestBody List<PointData> dataList) {
        if (dataList != null) {
            long now = System.currentTimeMillis();
            for (PointData d : dataList) {
                if (d.getTimestamp() <= 0) {
                    d.setTimestamp(now);
                }
            }
            ingestionService.processPointDataBatch(dataList);
        }
        return R.ok("批量点位数据接入完成, 共 " + (dataList != null ? dataList.size() : 0) + " 条");
    }

    @PostMapping("/history")
    public R<List<PointData>> queryHistory(@RequestBody HistoryQuery query) {
        List<PointData> list = ingestionService.queryHistory(query);
        return R.ok(list);
    }

    @GetMapping("/latest/{deviceId}")
    public R<Map<String, PointData>> getLatestSnapshot(@PathVariable("deviceId") String deviceId) {
        Map<String, PointData> snapshot = ingestionService.getLatestSnapshot(deviceId);
        return R.ok(snapshot);
    }

    @GetMapping(value = "/sse/{deviceId}", produces = org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE)
    public org.springframework.web.servlet.mvc.method.annotation.SseEmitter subscribeDeviceData(@PathVariable("deviceId") String deviceId) {
        return ingestionService.createSseSubscription(deviceId);
    }

    @GetMapping("/engine")
    public R<Map<String, Object>> getEngineInfo() {
        Map<String, Object> map = new HashMap<>();
        map.put("activeEngine", ingestionService.getActiveEngine());
        map.put("availableEngines", ingestionService.getAvailableEngines());
        return R.ok(map);
    }

    @PutMapping("/engine")
    public R<String> switchEngine(@RequestParam("engineType") String engineType) {
        boolean ok = ingestionService.setActiveEngine(engineType);
        return ok ? R.ok("存储引擎已切换为: " + engineType) : R.fail(400, "不支持的存储引擎: " + engineType);
    }
}
