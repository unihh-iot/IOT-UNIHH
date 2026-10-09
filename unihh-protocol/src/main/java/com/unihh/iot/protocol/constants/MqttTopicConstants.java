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

package com.unihh.iot.protocol.constants;

/**
 * 工业物联网 EMQX MQTT 消息总线全域主题常量规范接口
 */
public interface MqttTopicConstants {

    // -------------------------------------------------------------
    // 驱动生命周期与状态主题 (Driver Lifecycle)
    // -------------------------------------------------------------

    /** 驱动上线自动注册主题 (Driver -> Admin) */
    String TOPIC_DRIVER_REGISTER = "iot/driver/register";

    /** 驱动周期性心跳上报主题 (Driver -> Admin) */
    String TOPIC_DRIVER_HEARTBEAT = "iot/driver/heartbeat";

    /** 驱动状态变更/LWT遗嘱消息主题通配符 (iot/driver/+/status) */
    String TOPIC_DRIVER_STATUS_WILDCARD = "iot/driver/+/status";

    // -------------------------------------------------------------
    // 设备生命周期与配置同步主题 (Device Config & Lifecycle)
    // -------------------------------------------------------------

    /** 全局设备配置变更/点位更新同步主题 (Admin -> Engine / Drivers) */
    String TOPIC_DEVICE_CONFIG_UPDATE = "iot/device/config/update";

    /** 全局设备控制指令下发/反控主题 (Admin / Gateway -> Drivers) */
    String TOPIC_DEVICE_CONTROL = "iot/device/control";

    /** 设备物理通信在线/离线状态变更主题 (Driver -> Admin / Gateway) */
    String TOPIC_DEVICE_STATUS = "iot/device/status";

    /** 设备状态主题通配符 (iot/device/+/status) */
    String TOPIC_DEVICE_STATUS_WILDCARD = "iot/device/+/status";

    // -------------------------------------------------------------
    // 驱动专属主题动态生成辅助方法 (Driver-specific Topic Helpers)
    // -------------------------------------------------------------

    /**
     * 获取指定驱动的状态/遗嘱主题: iot/driver/{driverCode}/status
     */
    static String buildDriverStatusTopic(String driverCode) {
        return "iot/driver/" + driverCode + "/status";
    }

    /**
     * 获取指定驱动的配置接收主题: iot/driver/{driverCode}/config
     */
    static String buildDriverConfigTopic(String driverCode) {
        return "iot/driver/" + driverCode + "/config";
    }

    /**
     * 获取指定驱动的设备配置接收主题: iot/driver/{driverCode}/device/config
     */
    static String buildDriverDeviceConfigTopic(String driverCode) {
        return "iot/driver/" + driverCode + "/device/config";
    }

    /**
     * 获取指定驱动的即时指令下发主题: iot/driver/{driverCode}/cmd
     */
    static String buildDriverCmdTopic(String driverCode) {
        return "iot/driver/" + driverCode + "/cmd";
    }

    /**
     * 获取指定驱动采集数据上报发布主题: iot/driver/{driverCode}/data
     */
    static String buildDriverDataTopic(String driverCode) {
        return "iot/driver/" + driverCode + "/data";
    }
}
