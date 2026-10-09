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

package com.unihh.iot.protocol.api;

import com.unihh.iot.common.model.CommandMessage;
import com.unihh.iot.common.model.DeviceConfig;
import com.unihh.iot.protocol.model.ChannelContext;
import com.unihh.iot.protocol.model.DriverMetadata;
import com.unihh.iot.protocol.model.ParsedPayload;
import java.util.List;

public interface ProtocolDriver {
    String getDriverCode();
    DriverMetadata getMetadata();
    
    /**
     * 根据测点索引、测点标识及设备配置生成建议物理地址
     */
    default String suggestAddress(int pointIndex, String pointCode, DeviceConfig config) {
        return String.valueOf(40001 + (pointIndex * 2));
    }

    void init() throws Exception;
    ChannelContext connect(DeviceConfig config) throws Exception;
    byte[] encode(CommandMessage command) throws Exception;
    ParsedPayload decode(byte[] rawData, DeviceConfig config) throws Exception;

    /**
     * 主动/周期采集指定测点列表的数据
     * @param config 设备配置
     * @param points 待采样的物模型测点定义列表
     * @return 解析完成的测点数据载荷
     */
    default ParsedPayload readPoints(DeviceConfig config, List<com.unihh.iot.common.model.ThingModel.PropertySpec> points) throws Exception {
        return decode(new byte[0], config);
    }

    boolean ping(ChannelContext context);
    void disconnect(ChannelContext context);
    void destroy();
}
