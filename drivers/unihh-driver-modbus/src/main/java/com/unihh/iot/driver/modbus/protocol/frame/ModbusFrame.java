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
 * 完整 Modbus TCP 帧实体 (MBAP Header + PDU)
 */
public class ModbusFrame {
    private ModbusHeader header;
    private ModbusFunctionCode functionCode;
    private byte[] pduData;

    public ModbusFrame() {}

    public ModbusFrame(ModbusHeader header, ModbusFunctionCode functionCode, byte[] pduData) {
        this.header = header;
        this.functionCode = functionCode;
        this.pduData = pduData;
    }

    public byte[] encode() {
        byte[] headerBytes = header.encode();
        int pduLen = 1 + (pduData != null ? pduData.length : 0);
        byte[] fullFrame = new byte[7 + pduLen];

        System.arraycopy(headerBytes, 0, fullFrame, 0, 7);
        fullFrame[7] = (byte) functionCode.getCode();

        if (pduData != null && pduData.length > 0) {
            System.arraycopy(pduData, 0, fullFrame, 8, pduData.length);
        }
        return fullFrame;
    }

    public ModbusHeader getHeader() { return header; }
    public void setHeader(ModbusHeader header) { this.header = header; }

    public ModbusFunctionCode getFunctionCode() { return functionCode; }
    public void setFunctionCode(ModbusFunctionCode functionCode) { this.functionCode = functionCode; }

    public byte[] getPduData() { return pduData; }
    public void setPduData(byte[] pduData) { this.pduData = pduData; }
}
