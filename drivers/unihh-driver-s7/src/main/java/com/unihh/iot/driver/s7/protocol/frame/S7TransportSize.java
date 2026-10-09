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
 * 西门子 S7 协议传输尺寸编码 (Transport Size)
 */
public enum S7TransportSize {
    BIT((byte) 0x01, 1),
    BYTE((byte) 0x02, 1),
    CHAR((byte) 0x03, 1),
    WORD((byte) 0x04, 2),
    INT((byte) 0x05, 2),
    DWORD((byte) 0x06, 4),
    DINT((byte) 0x07, 4),
    REAL((byte) 0x08, 4),
    OCTET_STRING((byte) 0x09, 1);

    private final byte code;
    private final int byteLength;

    S7TransportSize(byte code, int byteLength) {
        this.code = code;
        this.byteLength = byteLength;
    }

    public byte getCode() {
        return code;
    }

    public int getByteLength() {
        return byteLength;
    }
}
