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

package com.unihh.iot.storage.router;

import com.unihh.iot.common.model.PointData;
import com.unihh.iot.storage.engine.DataStorageEngine;
import com.unihh.iot.storage.engine.MemoryStorageEngine;
import com.unihh.iot.storage.engine.RedisStorageEngine;
import com.unihh.iot.storage.engine.MySQLStorageEngine;
import com.unihh.iot.storage.engine.TDengineStorageEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 存储引擎路由与工厂适配器
 */
public class StorageEngineRouter {
    private static final Logger log = LoggerFactory.getLogger(StorageEngineRouter.class);
    private final Map<String, DataStorageEngine> engineRegistry = new ConcurrentHashMap<>();
    private final MemoryStorageEngine memoryEngine = new MemoryStorageEngine();
    private final RedisStorageEngine redisEngine = new RedisStorageEngine();
    private final MySQLStorageEngine mysqlEngine = new MySQLStorageEngine();
    private final TDengineStorageEngine tdengineEngine = new TDengineStorageEngine();
    private String activeEngineType = "memory";

    public StorageEngineRouter() {
        registerEngine(memoryEngine);
        registerEngine(redisEngine);
        registerEngine(mysqlEngine);
        registerEngine(tdengineEngine);
    }

    public void registerEngine(DataStorageEngine engine) {
        engineRegistry.put(engine.getEngineType().toLowerCase(), engine);
        log.info("注册持久化存储引擎: {}", engine.getEngineType());
    }

    public boolean setActiveEngine(String engineType) {
        if (engineType != null && engineRegistry.containsKey(engineType.toLowerCase())) {
            this.activeEngineType = engineType.toLowerCase();
            log.info("切换当前生效存储引擎为: {}", activeEngineType);
            return true;
        } else {
            log.warn("存储引擎未注册: {}，保持使用 default [{}]", engineType, activeEngineType);
            return false;
        }
    }

    public String getActiveEngineType() {
        return activeEngineType;
    }

    public List<String> getAvailableEngines() {
        return new ArrayList<>(engineRegistry.keySet());
    }

    public void save(PointData data) throws Exception {
        if (data == null) return;
        DataStorageEngine engine = engineRegistry.get(activeEngineType);
        if (engine != null) {
            engine.save(data);
        }
        // 始终同步最新快照至内存缓冲区与 Redis 缓存
        if (engine != memoryEngine) {
            memoryEngine.save(data);
        }
        redisEngine.save(data);
    }

    public void saveBatch(List<PointData> dataList) throws Exception {
        if (dataList == null || dataList.isEmpty()) return;
        DataStorageEngine engine = engineRegistry.get(activeEngineType);
        if (engine != null) {
            engine.saveBatch(dataList);
        }
        if (engine != memoryEngine) {
            memoryEngine.saveBatch(dataList);
        }
        redisEngine.saveBatch(dataList);
    }

    /**
     * 获取指定设备的最新点位数据快照
     */
    public Map<String, PointData> getLatestSnapshot(String deviceId) {
        Map<String, PointData> map = memoryEngine.getLatestSnapshot(deviceId);
        if (map == null || map.isEmpty()) {
            map = redisEngine.getLatestSnapshot(deviceId);
        }
        return map;
    }

    /**
     * 查询历史点位数据
     */
    public List<PointData> queryHistory(String deviceId, String pointCode, long startTime, long endTime, int limit) {
        return memoryEngine.queryHistory(deviceId, pointCode, startTime, endTime, limit);
    }
}
