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

package com.unihh.iot.driver.modbus.protocol.codec;

import com.unihh.iot.driver.modbus.protocol.frame.ModbusFrame;
import com.unihh.iot.driver.modbus.protocol.frame.ModbusFunctionCode;
import com.unihh.iot.driver.modbus.protocol.frame.ModbusHeader;

/**
 * Modbus 请求报文编码器
 * 负责组装 FC01, FC02, FC03, FC04, FC05, FC06, FC15, FC16 二进制请求帧
 */
public class ModbusRequestEncoder {

    /**
     * 编码读取请求报文 (FC01 - FC04)
     */
    public static byte[] encodeReadRequest(int transactionId, int slaveId, ModbusFunctionCode fc, int startAddress, int quantity) {
        ModbusHeader header = new ModbusHeader(transactionId, slaveId, 5); // PDU: FC(1B) + Addr(2B) + Qty(2B) = 5B
        byte[] pduData = new byte[4];
        pduData[0] = (byte) ((startAddress >> 8) & 0xFF);
        pduData[1] = (byte) (startAddress & 0xFF);
        pduData[2] = (byte) ((quantity >> 8) & 0xFF);
        pduData[3] = (byte) (quantity & 0xFF);

        ModbusFrame frame = new ModbusFrame(header, fc, pduData);
        return frame.encode();
    }

    /**
     * 编码写单个线圈报文 (FC05)
     */
    public static byte[] encodeWriteSingleCoil(int transactionId, int slaveId, int address, boolean value) {
        ModbusHeader header = new ModbusHeader(transactionId, slaveId, 5);
        byte[] pduData = new byte[4];
        pduData[0] = (byte) ((address >> 8) & 0xFF);
        pduData[1] = (byte) (address & 0xFF);
        pduData[2] = (byte) (value ? 0xFF : 0x00);
        pduData[3] = 0x00;

        ModbusFrame frame = new ModbusFrame(header, ModbusFunctionCode.WRITE_SINGLE_COIL, pduData);
        return frame.encode();
    }

    /**
     * 编码写单个寄存器报文 (FC06)
     */
    public static byte[] encodeWriteSingleRegister(int transactionId, int slaveId, int address, int value) {
        ModbusHeader header = new ModbusHeader(transactionId, slaveId, 5);
        byte[] pduData = new byte[4];
        pduData[0] = (byte) ((address >> 8) & 0xFF);
        pduData[1] = (byte) (address & 0xFF);
        pduData[2] = (byte) ((value >> 8) & 0xFF);
        pduData[3] = (byte) (value & 0xFF);

        ModbusFrame frame = new ModbusFrame(header, ModbusFunctionCode.WRITE_SINGLE_REGISTER, pduData);
        return frame.encode();
    }

    /**
     * 编码写多个保持寄存器报文 (FC16)
     */
    public static byte[] encodeWriteMultipleRegisters(int transactionId, int slaveId, int startAddress, int[] registerValues) {
        int regCount = registerValues.length;
        int byteCount = regCount * 2;
        int pduLen = 1 + 5 + byteCount; // FC(1B) + Addr(2B) + Qty(2B) + ByteCount(1B) + Values(N*2B)

        ModbusHeader header = new ModbusHeader(transactionId, slaveId, pduLen);
        byte[] pduData = new byte[4 + 1 + byteCount];
        pduData[0] = (byte) ((startAddress >> 8) & 0xFF);
        pduData[1] = (byte) (startAddress & 0xFF);
        pduData[2] = (byte) ((regCount >> 8) & 0xFF);
        pduData[3] = (byte) (regCount & 0xFF);
        pduData[4] = (byte) (byteCount & 0xFF);

        for (int i = 0; i < regCount; i++) {
            pduData[5 + i * 2] = (byte) ((registerValues[i] >> 8) & 0xFF);
            pduData[6 + i * 2] = (byte) (registerValues[i] & 0xFF);
        }

        ModbusFrame frame = new ModbusFrame(header, ModbusFunctionCode.WRITE_MULTIPLE_REGISTERS, pduData);
        return frame.encode();
    }
}
