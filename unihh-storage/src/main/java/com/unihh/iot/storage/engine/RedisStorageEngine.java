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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Redis 实时数据快照引擎 (Hash: iot:realtime:{deviceId})
 */
public class RedisStorageEngine implements DataStorageEngine {
    private static final Logger log = LoggerFactory.getLogger(RedisStorageEngine.class);
    // 模拟 Redis Key-Value / Hash 结构: Key -> (pointCode -> PointData)
    private final Map<String, Map<String, PointData>> redisCache = new ConcurrentHashMap<>();

    @Override
    public String getEngineType() {
        return "redis";
    }

    @Override
    public void save(PointData data) throws Exception {
        if (data == null || data.getDeviceId() == null) return;
        String redisKey = "iot:realtime:" + data.getDeviceId();
        redisCache.computeIfAbsent(redisKey, k -> new ConcurrentHashMap<>())
                  .put(data.getPointCode(), data);
        log.debug("[RedisEngine] HSET {} {} -> val={}", redisKey, data.getPointCode(), data.getValue());
    }

    @Override
    public void saveBatch(List<PointData> dataList) throws Exception {
        if (dataList != null) {
            for (PointData data : dataList) {
                save(data);
            }
        }
    }

    /**
     * 从 Redis 高速缓存读取特定设备的最新点位快照 (HGETALL iot:realtime:{deviceId})
     */
    public Map<String, PointData> getLatestSnapshot(String deviceId) {
        String redisKey = "iot:realtime:" + deviceId;
        return redisCache.getOrDefault(redisKey, new ConcurrentHashMap<>());
    }

    @Override
    public void close() {
        redisCache.clear();
        log.info("[RedisEngine] 缓存释放完成");
    }
}
