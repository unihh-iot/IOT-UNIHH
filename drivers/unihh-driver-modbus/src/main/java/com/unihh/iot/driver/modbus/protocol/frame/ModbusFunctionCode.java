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

package com.unihh.iot.driver.modbus.protocol.frame;

/**
 * Modbus 常用功能码枚举
 */
public enum ModbusFunctionCode {
    READ_COILS(0x01, "读线圈状态"),
    READ_DISCRETE_INPUTS(0x02, "读离散输入状态"),
    READ_HOLDING_REGISTERS(0x03, "读保持寄存器"),
    READ_INPUT_REGISTERS(0x04, "读输入寄存器"),
    WRITE_SINGLE_COIL(0x05, "写单个线圈"),
    WRITE_SINGLE_REGISTER(0x06, "写单个寄存器"),
    WRITE_MULTIPLE_COILS(0x0F, "写多个线圈 (FC15)"),
    WRITE_MULTIPLE_REGISTERS(0x10, "写多个寄存器 (FC16)");

    private final int code;
    private final String description;

    ModbusFunctionCode(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static ModbusFunctionCode fromCode(int code) {
        for (ModbusFunctionCode fc : values()) {
            if (fc.getCode() == code) {
                return fc;
            }
        }
        return READ_HOLDING_REGISTERS;
    }
}
