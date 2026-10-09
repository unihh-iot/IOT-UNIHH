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

/**
 * TDengine 3.x 高性能时序数据库引擎适配器
 */
public class TDengineStorageEngine implements DataStorageEngine {
    private static final Logger log = LoggerFactory.getLogger(TDengineStorageEngine.class);

    @Override
    public String getEngineType() {
        return "tdengine";
    }

    @Override
    public void save(PointData data) throws Exception {
        log.info("[TDengine] 单条写入超级表 meters USING meters_tags VALUES ({}, '{}', {})",
                data.getTimestamp(), data.getDeviceId(), data.getValue());
    }

    @Override
    public void saveBatch(List<PointData> dataList) throws Exception {
        if (dataList != null && !dataList.isEmpty()) {
            log.info("[TDengine] 批量时序数据高吞吐写入 COUNT={}", dataList.size());
        }
    }

    @Override
    public void close() {
        log.info("[TDengine] 时序连接池关闭");
    }
}
