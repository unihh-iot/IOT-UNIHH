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
 * 工业物联网设备与驱动动作命令常量规范接口
 */
public interface DriverActionConstants {

    /** 设备创建 */
    String ACTION_CREATE = "CREATE";

    /** 设备修改 */
    String ACTION_UPDATE = "UPDATE";

    /** 设备删除 */
    String ACTION_DELETE = "DELETE";

    /** 启动设备数据采集 */
    String ACTION_START = "START";

    /** 停止设备数据采集 */
    String ACTION_STOP = "STOP";

    /** 点位反控写入指令 */
    String ACTION_WRITE_POINT = "WRITE_POINT";

    /** 服务调用下发指令 */
    String ACTION_INVOKE_SERVICE = "INVOKE_SERVICE";

    /** 服务指令类型简写 */
    String ACTION_SERVICE = "SERVICE";

    /** 点位单次即时读取指令 */
    String ACTION_READ_POINT = "READ_POINT";

    /** 驱动与设备配置全量同步 */
    String ACTION_SYNC = "SYNC";
}
