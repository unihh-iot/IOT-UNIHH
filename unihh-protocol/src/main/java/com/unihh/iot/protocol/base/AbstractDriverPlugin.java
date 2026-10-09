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

package com.unihh.iot.protocol.base;

import com.unihh.iot.common.model.CommandMessage;
import com.unihh.iot.common.model.DeviceConfig;
import com.unihh.iot.common.model.ThingModel;
import com.unihh.iot.protocol.api.ProtocolDriver;
import com.unihh.iot.protocol.model.ChannelContext;
import com.unihh.iot.protocol.model.DriverMetadata;
import com.unihh.iot.protocol.model.ParsedPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 工业物联网协议驱动抽象基类 (Driver SDK Base)
 * <p>
 * 所有具体协议驱动 (Modbus, OPC UA, S7, BACnet, Mitsubishi 等) 仅需继承此类并打上 @Component 注解。
 * 驱动只需专注于实现硬件握手建连、点位批量采集与反控写入，无需编写任何 MQTT 连接、自注册、心跳、调度等样板代码。
 */
public abstract class AbstractDriverPlugin implements ProtocolDriver {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * 目标物理设备通道级公平互斥锁 (按 host:port 维度隔离)
     * 确保同一物理链路读写严格排队串行化，彻底防止半双工冲突、数据粘包与错帧
     */
    private final ConcurrentHashMap<String, ReentrantLock> channelLocks = new ConcurrentHashMap<>();

    /**
     * 获取目标设备物理通信通道的独占公平互斥锁
     */
    public ReentrantLock getChannelLock(DeviceConfig config) {
        if (config == null) {
            return new ReentrantLock(true);
        }
        String key;
        if (config.getHost() != null && !config.getHost().trim().isEmpty() && config.getPort() > 0) {
            key = config.getHost().trim() + ":" + config.getPort();
        } else {
            key = config.getDeviceId() != null ? config.getDeviceId() : "default_channel";
        }
        return channelLocks.computeIfAbsent(key, k -> new ReentrantLock(true));
    }

    @Override
    public abstract String getDriverCode();

    @Override
    public abstract DriverMetadata getMetadata();

    /**
     * 与目标物理设备建立通信连接 (TCP/串口/会话等)
     *
     * @param config 设备通信配置 (IP、端口、从站号、自定义参数等)
     * @return 是否成功建立物理链路
     */
    public boolean openConnection(DeviceConfig config) throws Exception {
        return true;
    }

    /**
     * 释放与目标物理设备的连接
     *
     * @param deviceId 设备唯一标识
     */
    public void closeConnection(String deviceId) {
        log.info("[{}] Closing connection for device [{}]", getDriverCode(), deviceId);
    }

    /**
     * 批量读取物模型测点数据
     *
     * @param config 设备通信参数
     * @param points 本次待采样的物模型点位列表
     * @return 包含测点采集值、品质戳、时间戳的解析载荷
     */
    @Override
    public abstract ParsedPayload readPoints(DeviceConfig config, List<ThingModel.PropertySpec> points) throws Exception;

    /**
     * 反控写入指定点位值
     *
     * @param config    设备通信参数
     * @param pointCode 点位物模型标识 (如 "temperature_target")
     * @param address   点位物理底层地址 (如 "40001" 或 "DB1.DBD4")
     * @param value     写入的目标数值
     * @param dataType  目标数据类型
     * @return 写入是否成功
     */
    public boolean writePoint(DeviceConfig config, String pointCode, String address, Object value, String dataType) throws Exception {
        log.warn("[{}] Write operation not implemented for device [{}] point [{}] address [{}]",
                getDriverCode(), config.getDeviceId(), pointCode, address);
        return false;
    }

    /**
     * 响应服务调用下发指令 (如设备启停、参数整定、规则联动下发服务等)
     *
     * @param config      目标设备通信参数
     * @param serviceCode 服务物模型标识 (例如 "startCoolingFan", "setMode" 或直接为点位编码)
     * @param params      输入参数键值对 (例如 {"speed": 2800, "mode": "AUTO"})
     * @return 执行是否成功
     */
    public boolean invokeService(DeviceConfig config, String serviceCode, Map<String, Object> params) throws Exception {
        log.info("[{}] Executing service [{}] on device [{}] with params: {}",
                getDriverCode(), serviceCode, config != null ? config.getDeviceId() : null, params);
        return true;
    }

    /**
     * 设备连通性链路探测
     */
    public boolean ping(DeviceConfig config) {
        return true;
    }

    // -------------------------------------------------------------
    // ProtocolDriver 接口默认适配实现
    // -------------------------------------------------------------

    @Override
    public void init() throws Exception {
        log.info("[{}] Driver plugin initialized.", getDriverCode());
    }

    @Override
    public ChannelContext connect(DeviceConfig config) throws Exception {
        boolean ok = openConnection(config);
        ChannelContext ctx = new ChannelContext("ch_" + getDriverCode() + "_" + config.getDeviceId(), config.getDeviceId(), getDriverCode());
        ctx.setActive(ok);
        return ctx;
    }

    @Override
    public byte[] encode(CommandMessage command) throws Exception {
        return new byte[0];
    }

    @Override
    public ParsedPayload decode(byte[] rawData, DeviceConfig config) throws Exception {
        return new ParsedPayload();
    }

    @Override
    public boolean ping(ChannelContext context) {
        return context != null && context.isActive();
    }

    @Override
    public void disconnect(ChannelContext context) {
        if (context != null) {
            closeConnection(context.getDeviceId());
            context.setActive(false);
        }
    }

    @Override
    public void destroy() {
        log.info("[{}] Driver plugin destroyed.", getDriverCode());
    }

    @Override
    public String suggestAddress(int pointIndex, String pointCode, DeviceConfig config) {
        return String.valueOf(40001 + (pointIndex * 2));
    }
}
