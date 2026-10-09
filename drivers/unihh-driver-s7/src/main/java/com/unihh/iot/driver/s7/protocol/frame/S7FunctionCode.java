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

package com.unihh.iot.driver.s7.protocol.frame;

/**
 * 西门子 S7 协议功能码定义
 */
public enum S7FunctionCode {
    /** 建立通信连接 / 协商参数 */
    SETUP_COMM((byte) 0xF0),

    /** 批量读取变量 (Read Var) */
    READ_VAR((byte) 0x04),

    /** 批量写入变量 (Write Var) */
    WRITE_VAR((byte) 0x05),

    /** PLC 启动控制 */
    PLC_START((byte) 0x28),

    /** PLC 停止控制 */
    PLC_STOP((byte) 0x29);

    private final byte code;

    S7FunctionCode(byte code) {
        this.code = code;
    }

    public byte getCode() {
        return code;
    }
}
