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
import java.util.List;

/**
 * 存储引擎标准适配接口
 */
public interface DataStorageEngine {

    /** 存储引擎类型标识 */
    String getEngineType();

    /** 单条点位数据实时保存 (同步写盘/高优先级告警) */
    void save(PointData data) throws Exception;

    /** 批量点位数据异步保存 (高吞吐批处理削峰) */
    void saveBatch(List<PointData> dataList) throws Exception;

    /** 释放连接池与句柄资源 */
    void close();
}
