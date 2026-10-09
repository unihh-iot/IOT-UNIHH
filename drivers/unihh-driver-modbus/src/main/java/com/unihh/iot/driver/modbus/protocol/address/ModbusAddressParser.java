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

package com.unihh.iot.driver.modbus.protocol.address;

import com.unihh.iot.driver.modbus.protocol.frame.ModbusFunctionCode;

/**
 * Modbus 工业点位地址解析器
 * 解析 40001, 30001, 10001, 00001 标准地址表达式为 0-based 寄存器偏移量与默认功能码
 */
public class ModbusAddressParser {

    /**
     * 解析 PLC 寄存器地址为 0-based 物理偏移量
     * 例: 40001 -> 0, 40003 -> 2, 30001 -> 0, 00001 -> 0
     */
    public static int parseRegisterAddress(String addressStr) {
        if (addressStr == null || addressStr.trim().isEmpty()) {
            return 0;
        }
        try {
            String clean = addressStr.trim().replaceAll("[^0-9]", "");
            int rawAddr = Integer.parseInt(clean);

            if (rawAddr >= 400001) {
                return rawAddr - 400001;
            } else if (rawAddr >= 300001) {
                return rawAddr - 300001;
            } else if (rawAddr >= 100001) {
                return rawAddr - 100001;
            } else if (rawAddr >= 40001) {
                return rawAddr - 40001;
            } else if (rawAddr >= 30001) {
                return rawAddr - 30001;
            } else if (rawAddr >= 10001) {
                return rawAddr - 10001;
            } else if (rawAddr >= 1) {
                return rawAddr - 1;
            }
            return rawAddr;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 根据点位地址前缀推导默认 Modbus 功能码
     */
    public static ModbusFunctionCode inferFunctionCode(String addressStr) {
        if (addressStr == null || addressStr.trim().isEmpty()) {
            return ModbusFunctionCode.READ_HOLDING_REGISTERS;
        }
        String clean = addressStr.trim();
        if (clean.startsWith("0")) {
            return ModbusFunctionCode.READ_COILS;
        } else if (clean.startsWith("1")) {
            return ModbusFunctionCode.READ_DISCRETE_INPUTS;
        } else if (clean.startsWith("3")) {
            return ModbusFunctionCode.READ_INPUT_REGISTERS;
        } else if (clean.startsWith("4")) {
            return ModbusFunctionCode.READ_HOLDING_REGISTERS;
        }
        return ModbusFunctionCode.READ_HOLDING_REGISTERS;
    }
}
