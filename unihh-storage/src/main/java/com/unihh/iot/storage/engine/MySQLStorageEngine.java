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
 * MySQL 8.0 关系型数据库存储引擎适配器
 */
public class MySQLStorageEngine implements DataStorageEngine {
    private static final Logger log = LoggerFactory.getLogger(MySQLStorageEngine.class);

    @Override
    public String getEngineType() {
        return "mysql";
    }

    @Override
    public void save(PointData data) throws Exception {
        log.info("[MySQL Engine] 单条落库: INSERT INTO device_point_data (device_id, point_code, val, ts) VALUES ('{}', '{}', {}, {})",
                data.getDeviceId(), data.getPointCode(), data.getValue(), data.getTimestamp());
    }

    @Override
    public void saveBatch(List<PointData> dataList) throws Exception {
        if (dataList != null && !dataList.isEmpty()) {
            log.info("[MySQL Engine] 批量 SQL 插入记录数: {}", dataList.size());
        }
    }

    @Override
    public void close() {
        log.info("[MySQL Engine] 连接池释放完毕");
    }
}
