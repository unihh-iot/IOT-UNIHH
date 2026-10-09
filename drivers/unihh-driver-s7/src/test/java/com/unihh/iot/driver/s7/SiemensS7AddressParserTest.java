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

package com.unihh.iot.driver.s7;

import com.unihh.iot.driver.s7.protocol.address.S7Address;
import com.unihh.iot.driver.s7.protocol.address.S7AddressParser;
import com.unihh.iot.driver.s7.protocol.frame.S7Area;
import com.unihh.iot.driver.s7.protocol.frame.S7DataType;
import com.unihh.iot.driver.s7.protocol.frame.S7TransportSize;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * 西门子 S7 测点地址解析器全覆盖单元测试
 */
public class SiemensS7AddressParserTest {

    @Test
    public void testDbBlockParsing() {
        // DB1.DBD0 -> DB=1, byteOffset=0, count=4 bytes, REAL
        S7Address addr1 = S7AddressParser.parse("DB1.DBD0", "FLOAT");
        assertEquals(S7Area.DATA_BLOCKS, addr1.getArea());
        assertEquals(1, addr1.getDbNumber());
        assertEquals(0, addr1.getByteOffset());
        assertEquals(0, addr1.getBitOffset());
        assertEquals(S7TransportSize.BYTE, addr1.getTransportSize());
        assertEquals(4, addr1.getCount());
        assertEquals(S7DataType.REAL, addr1.getDataType());
        assertEquals(0, addr1.getBitAddress());

        // DB2.DBW10 -> DB=2, byteOffset=10, count=2 bytes, INT
        S7Address addr2 = S7AddressParser.parse("DB2.DBW10", "INT");
        assertEquals(S7Area.DATA_BLOCKS, addr2.getArea());
        assertEquals(2, addr2.getDbNumber());
        assertEquals(10, addr2.getByteOffset());
        assertEquals(0, addr2.getBitOffset());
        assertEquals(S7TransportSize.BYTE, addr2.getTransportSize());
        assertEquals(2, addr2.getCount());
        assertEquals(S7DataType.INT, addr2.getDataType());
        assertEquals(80, addr2.getBitAddress());

        // DB5.DBX4.5 -> DB=5, byteOffset=4, bitOffset=5, BIT/BOOL
        S7Address addr3 = S7AddressParser.parse("DB5.DBX4.5", "BOOL");
        assertEquals(S7Area.DATA_BLOCKS, addr3.getArea());
        assertEquals(5, addr3.getDbNumber());
        assertEquals(4, addr3.getByteOffset());
        assertEquals(5, addr3.getBitOffset());
        assertEquals(S7TransportSize.BIT, addr3.getTransportSize());
        assertEquals(1, addr3.getCount());
        assertEquals(S7DataType.BOOL, addr3.getDataType());
        assertEquals(37, addr3.getBitAddress()); // (4 * 8) + 5 = 37

        // DB10.DBB2 -> DB=10, byteOffset=2, BYTE
        S7Address addr4 = S7AddressParser.parse("DB10.DBB2", "BYTE");
        assertEquals(S7Area.DATA_BLOCKS, addr4.getArea());
        assertEquals(10, addr4.getDbNumber());
        assertEquals(2, addr4.getByteOffset());
        assertEquals(S7TransportSize.BYTE, addr4.getTransportSize());
        assertEquals(1, addr4.getCount());
        assertEquals(S7DataType.BYTE, addr4.getDataType());
        assertEquals(16, addr4.getBitAddress());

        // DB1.DBL8 -> DB=1, byteOffset=8, LREAL (64位)
        S7Address addr5 = S7AddressParser.parse("DB1.DBL8", "DOUBLE");
        assertEquals(S7Area.DATA_BLOCKS, addr5.getArea());
        assertEquals(1, addr5.getDbNumber());
        assertEquals(8, addr5.getByteOffset());
        assertEquals(S7TransportSize.BYTE, addr5.getTransportSize());
        assertEquals(8, addr5.getCount());
        assertEquals(S7DataType.LREAL, addr5.getDataType());
    }

    @Test
    public void testMerkerFlagAreaParsing() {
        // M0.0 -> area=FLAGS, byte=0, bit=0
        S7Address addr1 = S7AddressParser.parse("M0.0", "BOOL");
        assertEquals(S7Area.FLAGS, addr1.getArea());
        assertEquals(0, addr1.getByteOffset());
        assertEquals(0, addr1.getBitOffset());
        assertEquals(S7TransportSize.BIT, addr1.getTransportSize());

        // MX1.7 -> area=FLAGS, byte=1, bit=7
        S7Address addr2 = S7AddressParser.parse("MX1.7", "BOOL");
        assertEquals(S7Area.FLAGS, addr2.getArea());
        assertEquals(1, addr2.getByteOffset());
        assertEquals(7, addr2.getBitOffset());
        assertEquals(15, addr2.getBitAddress());

        // MW10 -> area=FLAGS, byte=10, WORD/INT (2 bytes)
        S7Address addr3 = S7AddressParser.parse("MW10", "INT");
        assertEquals(S7Area.FLAGS, addr3.getArea());
        assertEquals(10, addr3.getByteOffset());
        assertEquals(S7TransportSize.BYTE, addr3.getTransportSize());
        assertEquals(2, addr3.getCount());
        assertEquals(S7DataType.INT, addr3.getDataType());

        // MD20 -> area=FLAGS, byte=20, REAL/DWORD (4 bytes)
        S7Address addr4 = S7AddressParser.parse("MD20", "FLOAT");
        assertEquals(S7Area.FLAGS, addr4.getArea());
        assertEquals(20, addr4.getByteOffset());
        assertEquals(S7TransportSize.BYTE, addr4.getTransportSize());
        assertEquals(4, addr4.getCount());
        assertEquals(S7DataType.REAL, addr4.getDataType());
    }

    @Test
    public void testInputOutputAreaParsing() {
        // I0.0
        S7Address addrIn = S7AddressParser.parse("I0.0", "BOOL");
        assertEquals(S7Area.INPUTS, addrIn.getArea());
        assertEquals(0, addrIn.getByteOffset());
        assertEquals(0, addrIn.getBitOffset());

        // IW4
        S7Address addrInW = S7AddressParser.parse("IW4", "INT");
        assertEquals(S7Area.INPUTS, addrInW.getArea());
        assertEquals(4, addrInW.getByteOffset());
        assertEquals(S7TransportSize.BYTE, addrInW.getTransportSize());
        assertEquals(2, addrInW.getCount());

        // Q0.1
        S7Address addrOut = S7AddressParser.parse("Q0.1", "BOOL");
        assertEquals(S7Area.OUTPUTS, addrOut.getArea());
        assertEquals(0, addrOut.getByteOffset());
        assertEquals(1, addrOut.getBitOffset());

        // QD8
        S7Address addrOutD = S7AddressParser.parse("QD8", "FLOAT");
        assertEquals(S7Area.OUTPUTS, addrOutD.getArea());
        assertEquals(8, addrOutD.getByteOffset());
        assertEquals(S7TransportSize.BYTE, addrOutD.getTransportSize());
        assertEquals(4, addrOutD.getCount());
    }

    @Test
    public void testS7200SmartVAreaParsing() {
        // 西门子 S7-200 / S7-200 SMART V区映射为 DB1
        S7Address addrVBit = S7AddressParser.parse("V100.3", "BOOL");
        assertEquals(S7Area.DATA_BLOCKS, addrVBit.getArea());
        assertEquals(1, addrVBit.getDbNumber());
        assertEquals(100, addrVBit.getByteOffset());
        assertEquals(3, addrVBit.getBitOffset());

        S7Address addrVD = S7AddressParser.parse("VD20", "FLOAT");
        assertEquals(S7Area.DATA_BLOCKS, addrVD.getArea());
        assertEquals(1, addrVD.getDbNumber());
        assertEquals(20, addrVD.getByteOffset());
        assertEquals(S7TransportSize.BYTE, addrVD.getTransportSize());
        assertEquals(4, addrVD.getCount());
        assertEquals(S7DataType.REAL, addrVD.getDataType());
    }
}
