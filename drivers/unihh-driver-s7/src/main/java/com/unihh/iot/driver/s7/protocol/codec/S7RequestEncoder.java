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

package com.unihh.iot.driver.s7.protocol.codec;

import com.unihh.iot.driver.s7.protocol.address.S7Address;
import com.unihh.iot.driver.s7.protocol.frame.S7FunctionCode;
import com.unihh.iot.driver.s7.protocol.frame.S7TransportSize;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * 西门子 S7 工业协议二进制请求帧编码器
 * <p>
 * 遵循 ISO-on-TCP (RFC 1006) / COTP (ISO 8073) 与 S7Comm PDU 协议规范。
 */
public class S7RequestEncoder {

    /**
     * 构造 COTP Connection Request (CR) 连接握手帧
     *
     * @param rack 机架号 (默认 0)
     * @param slot 插槽号 (S7-1200/1500 默认为 1, S7-300/400 默认为 2)
     */
    public static byte[] encodeCotpConnectRequest(int rack, int slot) {
        int calledTsap = (0x01 << 8) | (rack * 32 + slot);
        return new byte[]{
                0x03, 0x00, 0x00, 0x16,                       // TPKT: Version 3, Length 22
                0x11,                                         // COTP: Length 17
                (byte) 0xE0,                                  // PDU Type: CR (Connection Request)
                0x00, 0x00,                                   // DST Ref
                0x00, 0x01,                                   // SRC Ref
                0x00,                                         // Class & Option
                (byte) 0xC1, 0x02, 0x01, 0x00,                 // Calling TSAP (0x0100)
                (byte) 0xC2, 0x02, (byte) ((calledTsap >> 8) & 0xFF), (byte) (calledTsap & 0xFF), // Called TSAP
                (byte) 0xC0, 0x01, 0x0A                       // TPDU Size: 1024 bytes (0x0A)
        };
    }

    /**
     * 构造 S7 Setup Communication 协商参数帧
     *
     * @param pduLength 期望的 PDU 缓冲区尺寸 (通常为 480 或 960 字节)
     */
    public static byte[] encodeSetupCommunication(int pduLength) {
        int length = pduLength > 0 ? pduLength : 480;
        return new byte[]{
                0x03, 0x00, 0x00, 0x19,                       // TPKT: Length 25
                0x02, (byte) 0xF0, (byte) 0x80,               // COTP: DT (Data)
                0x32,                                         // S7 Protocol ID
                0x01,                                         // Message Type: Job
                0x00, 0x00,                                   // Redundancy ID
                0x00, 0x01,                                   // PDU Reference
                0x00, 0x08,                                   // Parameter Length: 8
                0x00, 0x00,                                   // Data Length: 0
                // Parameter
                S7FunctionCode.SETUP_COMM.getCode(),          // Function: Setup Communication (0xF0)
                0x00,                                         // Reserved
                0x00, 0x01,                                   // Max AMQ Caller
                0x00, 0x01,                                   // Max AMQ Callee
                (byte) ((length >> 8) & 0xFF), (byte) (length & 0xFF) // PDU Length
        };
    }

    /**
     * 构造 S7 批量点位读取帧 (Read Var, Function 0x04)
     *
     * @param pduRef 帧序号/流水号
     * @param items  待读取测点地址列表 (单包建议不超过 20 个)
     */
    public static byte[] encodeReadRequest(int pduRef, List<S7Address> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Read items cannot be empty");
        }

        int itemCount = items.size();
        int paramLength = 2 + (itemCount * 12);
        int totalLength = 4 + 3 + 10 + paramLength; // TPKT(4) + COTP(3) + S7Header(10) + Param

        byte[] pdu = new byte[totalLength];

        // 1. TPKT Header
        pdu[0] = 0x03;
        pdu[1] = 0x00;
        pdu[2] = (byte) ((totalLength >> 8) & 0xFF);
        pdu[3] = (byte) (totalLength & 0xFF);

        // 2. COTP Header (DT Data)
        pdu[4] = 0x02;
        pdu[5] = (byte) 0xF0;
        pdu[6] = (byte) 0x80;

        // 3. S7 Header
        pdu[7] = 0x32;                                         // Protocol ID
        pdu[8] = 0x01;                                         // Message Type: Job (0x01)
        pdu[9] = 0x00; pdu[10] = 0x00;                         // Redundancy ID
        pdu[11] = (byte) ((pduRef >> 8) & 0xFF);              // PDU Reference
        pdu[12] = (byte) (pduRef & 0xFF);
        pdu[13] = (byte) ((paramLength >> 8) & 0xFF);         // Parameter Length
        pdu[14] = (byte) (paramLength & 0xFF);
        pdu[15] = 0x00; pdu[16] = 0x00;                        // Data Length: 0

        // 4. Parameter: Read Var
        pdu[17] = S7FunctionCode.READ_VAR.getCode();          // Function: Read Var (0x04)
        pdu[18] = (byte) itemCount;                            // Item Count

        // 5. Item Specifications (12 bytes each)
        for (int i = 0; i < itemCount; i++) {
            int offset = 19 + (i * 12);
            S7Address addr = items.get(i);

            pdu[offset] = 0x12;                                // Var Spec (0x12)
            pdu[offset + 1] = 0x0A;                            // Length of address (10 bytes)
            pdu[offset + 2] = 0x10;                            // Syntax ID: S7Any (0x10)
            pdu[offset + 3] = addr.getTransportSize().getCode(); // Transport size
            pdu[offset + 4] = (byte) ((addr.getCount() >> 8) & 0xFF); // Length High
            pdu[offset + 5] = (byte) (addr.getCount() & 0xFF);        // Length Low
            pdu[offset + 6] = (byte) ((addr.getDbNumber() >> 8) & 0xFF); // DB Number High
            pdu[offset + 7] = (byte) (addr.getDbNumber() & 0xFF);        // DB Number Low
            pdu[offset + 8] = addr.getArea().getCode();                  // Area Code (0x84, 0x83, etc.)

            int bitAddress = addr.getBitAddress();
            pdu[offset + 9] = (byte) ((bitAddress >> 16) & 0xFF);
            pdu[offset + 10] = (byte) ((bitAddress >> 8) & 0xFF);
            pdu[offset + 11] = (byte) (bitAddress & 0xFF);
        }

        return pdu;
    }

    /**
     * 构造 S7 变量写入帧 (Write Var, Function 0x05)
     *
     * @param pduRef 帧序号
     * @param addr   目标测点地址
     * @param dataBytes 写入的二进制字节数据
     */
    public static byte[] encodeWriteRequest(int pduRef, S7Address addr, byte[] dataBytes) throws IOException {
        int itemCount = 1;
        int paramLength = 2 + (itemCount * 12);

        boolean isBit = (addr.getTransportSize() == S7TransportSize.BIT);
        int dataItemHeaderLen = 4;
        int dataPayloadLen = dataBytes != null ? dataBytes.length : 0;
        int totalDataLen = dataItemHeaderLen + dataPayloadLen;

        int totalLength = 4 + 3 + 10 + paramLength + totalDataLen;

        ByteArrayOutputStream out = new ByteArrayOutputStream(totalLength);

        // 1. TPKT
        out.write(new byte[]{
                0x03, 0x00,
                (byte) ((totalLength >> 8) & 0xFF),
                (byte) (totalLength & 0xFF)
        });

        // 2. COTP
        out.write(new byte[]{0x02, (byte) 0xF0, (byte) 0x80});

        // 3. S7 Header
        out.write(new byte[]{
                0x32,                                         // Protocol ID
                0x01,                                         // Message Type: Job
                0x00, 0x00,                                   // Redundancy ID
                (byte) ((pduRef >> 8) & 0xFF), (byte) (pduRef & 0xFF), // PDU Ref
                (byte) ((paramLength >> 8) & 0xFF), (byte) (paramLength & 0xFF), // Param Length
                (byte) ((totalDataLen >> 8) & 0xFF), (byte) (totalDataLen & 0xFF) // Data Length
        });

        // 4. Parameter: Write Var
        out.write(S7FunctionCode.WRITE_VAR.getCode());        // Function: 0x05
        out.write((byte) itemCount);                           // Count: 1

        // Item Spec (12 bytes)
        out.write(0x12);                                       // Var Spec
        out.write(0x0A);                                       // Length
        out.write(0x10);                                       // Syntax ID S7Any
        out.write(addr.getTransportSize().getCode());          // Transport Size
        out.write((byte) ((addr.getCount() >> 8) & 0xFF));     // Count High
        out.write((byte) (addr.getCount() & 0xFF));            // Count Low
        out.write((byte) ((addr.getDbNumber() >> 8) & 0xFF));  // DB High
        out.write((byte) (addr.getDbNumber() & 0xFF));         // DB Low
        out.write(addr.getArea().getCode());                   // Area Code
        int bitAddress = addr.getBitAddress();
        out.write((byte) ((bitAddress >> 16) & 0xFF));
        out.write((byte) ((bitAddress >> 8) & 0xFF));
        out.write((byte) (bitAddress & 0xFF));

        // 5. Data Part
        out.write(0x00);                                       // Reserved
        out.write(isBit ? 0x03 : 0x04);                        // Data Transport Size (0x03: BIT, 0x04: BYTE/WORD/DWORD)
        int lengthInBits = isBit ? 1 : (dataPayloadLen * 8);
        out.write((byte) ((lengthInBits >> 8) & 0xFF));        // Length in bits High
        out.write((byte) (lengthInBits & 0xFF));               // Length in bits Low
        if (dataBytes != null && dataBytes.length > 0) {
            out.write(dataBytes);
        }

        return out.toByteArray();
    }
}
