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
 * 西门子 S7 工业数据类型定义与映射
 */
public enum S7DataType {
    BOOL(1, S7TransportSize.BIT),
    BYTE(1, S7TransportSize.BYTE),
    CHAR(1, S7TransportSize.CHAR),
    INT(2, S7TransportSize.WORD),
    WORD(2, S7TransportSize.WORD),
    DINT(4, S7TransportSize.DWORD),
    DWORD(4, S7TransportSize.DWORD),
    REAL(4, S7TransportSize.REAL),
    LREAL(8, S7TransportSize.OCTET_STRING),
    LINT(8, S7TransportSize.OCTET_STRING),
    ULINT(8, S7TransportSize.OCTET_STRING),
    STRING(256, S7TransportSize.BYTE);

    private final int byteLength;
    private final S7TransportSize defaultTransportSize;

    S7DataType(int byteLength, S7TransportSize defaultTransportSize) {
        this.byteLength = byteLength;
        this.defaultTransportSize = defaultTransportSize;
    }

    public int getByteLength() {
        return byteLength;
    }

    public S7TransportSize getDefaultTransportSize() {
        return defaultTransportSize;
    }

    public static S7DataType fromString(String typeStr) {
        if (typeStr == null || typeStr.trim().isEmpty()) {
            return REAL;
        }
        String t = typeStr.trim().toUpperCase();
        switch (t) {
            case "BOOL":
            case "BOOLEAN":
            case "BIT":
                return BOOL;
            case "BYTE":
            case "UINT8":
            case "INT8":
                return BYTE;
            case "CHAR":
                return CHAR;
            case "SHORT":
            case "INT":
            case "INT16":
                return INT;
            case "WORD":
            case "UINT16":
                return WORD;
            case "DINT":
            case "INT32":
            case "INTEGER":
            case "LONG":
                return DINT;
            case "DWORD":
            case "UINT32":
                return DWORD;
            case "FLOAT":
            case "FLOAT32":
            case "REAL":
                return REAL;
            case "DOUBLE":
            case "FLOAT64":
            case "LREAL":
                return LREAL;
            case "LINT":
            case "INT64":
                return LINT;
            case "ULINT":
            case "UINT64":
                return ULINT;
            case "STRING":
            case "TEXT":
                return STRING;
            default:
                return REAL;
        }
    }
}
