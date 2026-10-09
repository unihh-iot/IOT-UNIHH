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
 * Modbus 点位数据类型枚举
 */
public enum ModbusDataType {
    BOOL(1, "单比特位"),
    ENUM(2, "枚举状态量"),
    INT16(2, "16位有符号整数"),
    UINT16(2, "16位无符号整数"),
    INT32(4, "32位有符号整数"),
    UINT32(4, "32位无符号整数"),
    INT64(8, "64位有符号整数"),
    UINT64(8, "64位无符号整数"),
    FLOAT(4, "32位单精度浮点数"),
    DOUBLE(8, "64位双精度浮点数");

    private final int byteLength;
    private final String description;

    ModbusDataType(int byteLength, String description) {
        this.byteLength = byteLength;
        this.description = description;
    }

    public int getByteLength() {
        return byteLength;
    }

    public String getDescription() {
        return description;
    }

    public static ModbusDataType fromString(String typeStr) {
        if (typeStr == null) return DOUBLE;
        String upper = typeStr.trim().toUpperCase();
        switch (upper) {
            case "BOOL":
            case "BOOLEAN":
            case "BIT":
                return BOOL;
            case "ENUM":
                return ENUM;
            case "INT":
            case "INTEGER":
            case "INT16":
            case "SHORT":
            case "WORD":
                return INT16;
            case "UINT":
            case "UINT16":
            case "USHORT":
            case "UWORD":
                return UINT16;
            case "INT32":
            case "DINT":
            case "DWORD":
            case "LONG":
                return INT32;
            case "UINT32":
            case "UDINT":
            case "UDWORD":
            case "ULONG":
                return UINT32;
            case "INT64":
            case "LONG64":
            case "BIGINT":
                return INT64;
            case "UINT64":
            case "UBIGINT":
                return UINT64;
            case "FLOAT":
            case "FLOAT32":
            case "SINGLE":
            case "REAL":
                return FLOAT;
            case "DOUBLE":
            case "FLOAT64":
            case "LREAL":
            default:
                return DOUBLE;
        }
    }
}
