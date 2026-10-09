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
 * Modbus TCP MBAP 报文头实体 (7 字节)
 * 结构: Transaction ID (2B) + Protocol ID (2B) + Length (2B) + Unit ID (1B)
 */
public class ModbusHeader {
    private int transactionId = 0x0001; // 事务标识符
    private int protocolId = 0x0000;    // 协议标识符 (0x0000 = Modbus TCP)
    private int length = 0x0006;        // 后续字节长度
    private int unitId = 0x01;          // 单元标识符 (Slave ID)

    public ModbusHeader() {}

    public ModbusHeader(int transactionId, int unitId, int pduLength) {
        this.transactionId = transactionId;
        this.unitId = unitId;
        this.length = pduLength + 1; // PDU 长度 + UnitID(1B)
    }

    public byte[] encode() {
        byte[] bytes = new byte[7];
        bytes[0] = (byte) ((transactionId >> 8) & 0xFF);
        bytes[1] = (byte) (transactionId & 0xFF);
        bytes[2] = (byte) ((protocolId >> 8) & 0xFF);
        bytes[3] = (byte) (protocolId & 0xFF);
        bytes[4] = (byte) ((length >> 8) & 0xFF);
        bytes[5] = (byte) (length & 0xFF);
        bytes[6] = (byte) (unitId & 0xFF);
        return bytes;
    }

    public static ModbusHeader decode(byte[] bytes) {
        if (bytes == null || bytes.length < 7) {
            throw new IllegalArgumentException("MBAP Header 至少需要 7 字节");
        }
        ModbusHeader header = new ModbusHeader();
        header.transactionId = ((bytes[0] & 0xFF) << 8) | (bytes[1] & 0xFF);
        header.protocolId = ((bytes[2] & 0xFF) << 8) | (bytes[3] & 0xFF);
        header.length = ((bytes[4] & 0xFF) << 8) | (bytes[5] & 0xFF);
        header.unitId = bytes[6] & 0xFF;
        return header;
    }

    public int getTransactionId() { return transactionId; }
    public void setTransactionId(int transactionId) { this.transactionId = transactionId; }

    public int getProtocolId() { return protocolId; }
    public void setProtocolId(int protocolId) { this.protocolId = protocolId; }

    public int getLength() { return length; }
    public void setLength(int length) { this.length = length; }

    public int getUnitId() { return unitId; }
    public void setUnitId(int unitId) { this.unitId = unitId; }
}
