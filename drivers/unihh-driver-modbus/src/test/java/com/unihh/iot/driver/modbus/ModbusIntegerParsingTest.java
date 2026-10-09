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

package com.unihh.iot.driver.modbus;

import com.unihh.iot.common.model.DeviceConfig;
import com.unihh.iot.common.model.PointData;
import com.unihh.iot.common.model.ThingModel;
import com.unihh.iot.driver.modbus.protocol.address.ModbusAddressParser;
import com.unihh.iot.driver.modbus.protocol.codec.ModbusRequestEncoder;
import com.unihh.iot.driver.modbus.protocol.codec.ModbusResponseDecoder;
import com.unihh.iot.driver.modbus.protocol.frame.ModbusDataType;
import com.unihh.iot.protocol.model.ParsedPayload;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Modbus 整型数据及各种字节序/缩放换算解码测试
 */
public class ModbusIntegerParsingTest {

    private byte[] buildModbusResponseFrame(int fc, byte[] pduData) {
        // MBAP: TransId(2B) + ProtoId(2B) + Length(2B) + UnitId(1B) = 7B
        // FC(1B) + ByteCount(1B) + Data(NB)
        int byteCount = pduData.length;
        byte[] frame = new byte[7 + 2 + byteCount];
        frame[0] = 0x00; frame[1] = 0x01; // Transaction ID
        frame[2] = 0x00; frame[3] = 0x00; // Protocol ID (Modbus TCP = 0)
        frame[4] = (byte) (((2 + byteCount) >> 8) & 0xFF);
        frame[5] = (byte) ((2 + byteCount) & 0xFF); // Length
        frame[6] = 0x01; // Unit ID (Slave ID)
        frame[7] = (byte) (fc & 0xFF); // Function Code
        frame[8] = (byte) (byteCount & 0xFF); // Byte Count
        System.arraycopy(pduData, 0, frame, 9, byteCount);
        return frame;
    }

    @Test
    public void testDataTypeAliases() {
        Assert.assertEquals(ModbusDataType.INT16, ModbusDataType.fromString("INT"));
        Assert.assertEquals(ModbusDataType.INT16, ModbusDataType.fromString("INTEGER"));
        Assert.assertEquals(ModbusDataType.INT16, ModbusDataType.fromString("int16"));
        Assert.assertEquals(ModbusDataType.INT16, ModbusDataType.fromString("SHORT"));
        Assert.assertEquals(ModbusDataType.INT16, ModbusDataType.fromString("WORD"));

        Assert.assertEquals(ModbusDataType.UINT16, ModbusDataType.fromString("UINT"));
        Assert.assertEquals(ModbusDataType.UINT16, ModbusDataType.fromString("UINT16"));
        Assert.assertEquals(ModbusDataType.UINT16, ModbusDataType.fromString("UWORD"));

        Assert.assertEquals(ModbusDataType.INT32, ModbusDataType.fromString("INT32"));
        Assert.assertEquals(ModbusDataType.INT32, ModbusDataType.fromString("DINT"));
        Assert.assertEquals(ModbusDataType.INT32, ModbusDataType.fromString("DWORD"));
        Assert.assertEquals(ModbusDataType.INT32, ModbusDataType.fromString("LONG"));

        Assert.assertEquals(ModbusDataType.UINT32, ModbusDataType.fromString("UINT32"));
        Assert.assertEquals(ModbusDataType.UINT32, ModbusDataType.fromString("UDINT"));
        Assert.assertEquals(ModbusDataType.UINT32, ModbusDataType.fromString("UDWORD"));

        Assert.assertEquals(ModbusDataType.INT64, ModbusDataType.fromString("INT64"));
        Assert.assertEquals(ModbusDataType.INT64, ModbusDataType.fromString("BIGINT"));
        Assert.assertEquals(ModbusDataType.UINT64, ModbusDataType.fromString("UINT64"));
    }

    @Test
    public void testInt16AndUint16Parsing() {
        DeviceConfig config = new DeviceConfig("DEV_TEST", "测试设备", "MODEL_1", "modbus-tcp", "127.0.0.1", 502);

        List<ThingModel.PropertySpec> points = new ArrayList<>();
        ThingModel.PropertySpec p1 = new ThingModel.PropertySpec("pos_int", "正整数", "INTEGER", "R", "");
        p1.setAddress("40001");
        ThingModel.PropertySpec p2 = new ThingModel.PropertySpec("neg_int", "负整数", "INT16", "R", "");
        p2.setAddress("40002");
        ThingModel.PropertySpec p3 = new ThingModel.PropertySpec("u_int", "无符号整型", "UINT16", "R", "");
        p3.setAddress("40003");
        points.add(p1);
        points.add(p2);
        points.add(p3);

        // 寄存器 40001: 1500 (0x05DC)
        // 寄存器 40002: -50 (0xFFCE)
        // 寄存器 40003: 45000 (0xAFC8)
        byte[] pdu = new byte[]{
                0x05, (byte) 0xDC,
                (byte) 0xFF, (byte) 0xCE,
                (byte) 0xAF, (byte) 0xC8
        };
        byte[] frame = buildModbusResponseFrame(3, pdu);

        ParsedPayload payload = ModbusResponseDecoder.decode(frame, config, points);
        Assert.assertEquals(3, payload.getPoints().size());

        PointData pd1 = payload.getPoints().get(0);
        Assert.assertEquals(1500.0, pd1.getValue(), 0.001);
        Assert.assertEquals("1500", pd1.getRawValue());
        Assert.assertEquals(1, pd1.getQuality());

        PointData pd2 = payload.getPoints().get(1);
        Assert.assertEquals(-50.0, pd2.getValue(), 0.001);
        Assert.assertEquals("-50", pd2.getRawValue());
        Assert.assertEquals(1, pd2.getQuality());

        PointData pd3 = payload.getPoints().get(2);
        Assert.assertEquals(45000.0, pd3.getValue(), 0.001);
        Assert.assertEquals("45000", pd3.getRawValue());
        Assert.assertEquals(1, pd3.getQuality());
    }

    @Test
    public void testInt32ByteOrders() {
        // 数值 123456 (0x0001E240)
        // ABCD: 00 01 E2 40
        // CDAB: E2 40 00 01 (工业常用PLC Word Swap)
        // BADC: 01 00 40 E2 (Byte Swap)
        // DCBA: 40 E2 01 00 (Little Endian)

        String[] orders = new String[]{"ABCD", "CDAB", "BADC", "DCBA"};
        byte[][] data = new byte[][]{
                {0x00, 0x01, (byte) 0xE2, 0x40},
                {(byte) 0xE2, 0x40, 0x00, 0x01},
                {0x01, 0x00, 0x40, (byte) 0xE2},
                {0x40, (byte) 0xE2, 0x01, 0x00}
        };

        for (int i = 0; i < orders.length; i++) {
            String order = orders[i];
            DeviceConfig config = new DeviceConfig("DEV_TEST", "测试设备", "MODEL_1", "modbus-tcp", "127.0.0.1", 502);
            Map<String, Object> params = new HashMap<>();
            params.put("byteOrder", order);
            config.setConnectionParams(params);

            List<ThingModel.PropertySpec> points = new ArrayList<>();
            ThingModel.PropertySpec p = new ThingModel.PropertySpec("flow", "流量计累计量", "INT32", "R", "L");
            p.setAddress("40001");
            points.add(p);

            byte[] frame = buildModbusResponseFrame(3, data[i]);
            ParsedPayload payload = ModbusResponseDecoder.decode(frame, config, points);
            Assert.assertEquals(1, payload.getPoints().size());
            PointData pd = payload.getPoints().get(0);
            Assert.assertEquals("字节序 " + order + " 解析失败", 123456.0, pd.getValue(), 0.001);
            Assert.assertEquals("123456", pd.getRawValue());
            Assert.assertEquals(1, pd.getQuality());
        }
    }

    @Test
    public void testUint32LargeValue() {
        // 无符号 32 位整数 3000000000 (0xB2D05E00)
        // 符号位为 1，在未做无符号处理时会被错误解析为 -1294967296
        DeviceConfig config = new DeviceConfig("DEV_TEST", "测试设备", "MODEL_1", "modbus-tcp", "127.0.0.1", 502);
        Map<String, Object> params = new HashMap<>();
        params.put("byteOrder", "CDAB");
        config.setConnectionParams(params);

        List<ThingModel.PropertySpec> points = new ArrayList<>();
        ThingModel.PropertySpec p = new ThingModel.PropertySpec("energy_kwh", "总电能计数", "UINT32", "R", "kWh");
        p.setAddress("40001");
        points.add(p);

        // CDAB: 5E 00 B2 D0
        byte[] pdu = new byte[]{0x5E, 0x00, (byte) 0xB2, (byte) 0xD0};
        byte[] frame = buildModbusResponseFrame(3, pdu);

        ParsedPayload payload = ModbusResponseDecoder.decode(frame, config, points);
        Assert.assertEquals(1, payload.getPoints().size());
        PointData pd = payload.getPoints().get(0);
        Assert.assertEquals(3000000000.0, pd.getValue(), 0.001);
        Assert.assertEquals("3000000000", pd.getRawValue());
        Assert.assertEquals(1, pd.getQuality());
    }

    @Test
    public void testScaleAndOffsetConversion() {
        // 工业传感器常见温度量程:
        // 寄存器读取原始整型值 255, scale=0.1, offset=0.0 -> 实际工程值 25.5 ℃
        DeviceConfig config = new DeviceConfig("DEV_TEST", "测试设备", "MODEL_1", "modbus-tcp", "127.0.0.1", 502);

        List<ThingModel.PropertySpec> points = new ArrayList<>();
        ThingModel.PropertySpec p = new ThingModel.PropertySpec("temp", "室内温度", "INTEGER", "R", "℃");
        p.setAddress("40001");
        p.setScale(0.1);
        p.setOffset(0.0);
        points.add(p);

        byte[] pdu = new byte[]{0x00, (byte) 0xFF}; // 255
        byte[] frame = buildModbusResponseFrame(3, pdu);

        ParsedPayload payload = ModbusResponseDecoder.decode(frame, config, points);
        Assert.assertEquals(1, payload.getPoints().size());
        PointData pd = payload.getPoints().get(0);
        Assert.assertEquals(25.5, pd.getValue(), 0.001);
        Assert.assertEquals("255", pd.getRawValue()); // 原始值保留整型 255
        Assert.assertEquals(1, pd.getQuality());
    }

    @Test
    public void testSixDigitModbusAddress() {
        Assert.assertEquals(0, ModbusAddressParser.parseRegisterAddress("400001"));
        Assert.assertEquals(4, ModbusAddressParser.parseRegisterAddress("400005"));
        Assert.assertEquals(0, ModbusAddressParser.parseRegisterAddress("300001"));
        Assert.assertEquals(0, ModbusAddressParser.parseRegisterAddress("100001"));
        Assert.assertEquals(0, ModbusAddressParser.parseRegisterAddress("40001"));
        Assert.assertEquals(2, ModbusAddressParser.parseRegisterAddress("40003"));
    }

    @Test
    public void testWriteAndReadRoundtripAllByteOrders() {
        String[] orders = new String[]{"ABCD", "CDAB", "BADC", "DCBA"};

        // 1. INT32 回环测试
        long testInt32 = 123456L;
        for (String bo : orders) {
            int w0 = (int) ((testInt32 >> 16) & 0xFFFF);
            int w1 = (int) (testInt32 & 0xFFFF);
            int[] regs;
            if ("CDAB".equalsIgnoreCase(bo)) regs = new int[]{w1, w0};
            else if ("BADC".equalsIgnoreCase(bo)) regs = new int[]{(int)(Short.reverseBytes((short) w0) & 0xFFFF), (int)(Short.reverseBytes((short) w1) & 0xFFFF)};
            else if ("DCBA".equalsIgnoreCase(bo)) regs = new int[]{(int)(Short.reverseBytes((short) w1) & 0xFFFF), (int)(Short.reverseBytes((short) w0) & 0xFFFF)};
            else regs = new int[]{w0, w1};

            byte[] pdu = new byte[]{
                    (byte) ((regs[0] >> 8) & 0xFF), (byte) (regs[0] & 0xFF),
                    (byte) ((regs[1] >> 8) & 0xFF), (byte) (regs[1] & 0xFF)
            };
            DeviceConfig config = new DeviceConfig("DEV_TEST", "测试设备", "MODEL_1", "modbus-tcp", "127.0.0.1", 502);
            Map<String, Object> params = new HashMap<>();
            params.put("byteOrder", bo);
            config.setConnectionParams(params);

            List<ThingModel.PropertySpec> points = new ArrayList<>();
            ThingModel.PropertySpec p = new ThingModel.PropertySpec("p_int32", "测试点位", "INT32", "RW", "");
            p.setAddress("40001");
            points.add(p);

            ParsedPayload res = ModbusResponseDecoder.decode(buildModbusResponseFrame(3, pdu), config, points);
            Assert.assertEquals("INT32 回环失败 byteOrder=" + bo, 123456.0, res.getPoints().get(0).getValue(), 0.001);
        }

        // 2. FLOAT 回环测试
        float testFloat = 123.45f;
        for (String bo : orders) {
            int intBits = Float.floatToIntBits(testFloat);
            int w0 = (intBits >> 16) & 0xFFFF;
            int w1 = intBits & 0xFFFF;
            int[] regs;
            if ("CDAB".equalsIgnoreCase(bo)) regs = new int[]{w1, w0};
            else if ("BADC".equalsIgnoreCase(bo)) regs = new int[]{(int)(Short.reverseBytes((short) w0) & 0xFFFF), (int)(Short.reverseBytes((short) w1) & 0xFFFF)};
            else if ("DCBA".equalsIgnoreCase(bo)) regs = new int[]{(int)(Short.reverseBytes((short) w1) & 0xFFFF), (int)(Short.reverseBytes((short) w0) & 0xFFFF)};
            else regs = new int[]{w0, w1};

            byte[] pdu = new byte[]{
                    (byte) ((regs[0] >> 8) & 0xFF), (byte) (regs[0] & 0xFF),
                    (byte) ((regs[1] >> 8) & 0xFF), (byte) (regs[1] & 0xFF)
            };
            DeviceConfig config = new DeviceConfig("DEV_TEST", "测试设备", "MODEL_1", "modbus-tcp", "127.0.0.1", 502);
            Map<String, Object> params = new HashMap<>();
            params.put("byteOrder", bo);
            config.setConnectionParams(params);

            List<ThingModel.PropertySpec> points = new ArrayList<>();
            ThingModel.PropertySpec p = new ThingModel.PropertySpec("p_float", "测试点位", "FLOAT", "RW", "");
            p.setAddress("40001");
            points.add(p);

            ParsedPayload res = ModbusResponseDecoder.decode(buildModbusResponseFrame(3, pdu), config, points);
            Assert.assertEquals("FLOAT 回环失败 byteOrder=" + bo, 123.45, res.getPoints().get(0).getValue(), 0.01);
        }

        // 3. INT16 单寄存器字节交换回环测试
        int testInt16 = 12345;
        for (String bo : new String[]{"AB", "BA"}) {
            int reg = testInt16;
            if ("BA".equalsIgnoreCase(bo)) {
                reg = (int) (Short.reverseBytes((short) reg) & 0xFFFF);
            }
            byte[] pdu = new byte[]{(byte) ((reg >> 8) & 0xFF), (byte) (reg & 0xFF)};
            DeviceConfig config = new DeviceConfig("DEV_TEST", "测试设备", "MODEL_1", "modbus-tcp", "127.0.0.1", 502);
            Map<String, Object> params = new HashMap<>();
            params.put("byteOrder", bo);
            config.setConnectionParams(params);

            List<ThingModel.PropertySpec> points = new ArrayList<>();
            ThingModel.PropertySpec p = new ThingModel.PropertySpec("p_int16", "测试点位", "INT", "RW", "");
            p.setAddress("40001");
            points.add(p);

            ParsedPayload res = ModbusResponseDecoder.decode(buildModbusResponseFrame(3, pdu), config, points);
            Assert.assertEquals("INT16 回环失败 byteOrder=" + bo, 12345.0, res.getPoints().get(0).getValue(), 0.001);
        }

        // 4. DOUBLE (64位双精度浮点数) 全字节序回环测试
        double testDouble = 123456.789;
        for (String bo : orders) {
            long longBits = Double.doubleToLongBits(testDouble);
            int w0 = (int) ((longBits >> 48) & 0xFFFF);
            int w1 = (int) ((longBits >> 32) & 0xFFFF);
            int w2 = (int) ((longBits >> 16) & 0xFFFF);
            int w3 = (int) (longBits & 0xFFFF);
            int[] regs;
            if ("CDAB".equalsIgnoreCase(bo)) regs = new int[]{w3, w2, w1, w0};
            else if ("BADC".equalsIgnoreCase(bo)) regs = new int[]{
                    (int)(Short.reverseBytes((short) w0) & 0xFFFF),
                    (int)(Short.reverseBytes((short) w1) & 0xFFFF),
                    (int)(Short.reverseBytes((short) w2) & 0xFFFF),
                    (int)(Short.reverseBytes((short) w3) & 0xFFFF)
            };
            else if ("DCBA".equalsIgnoreCase(bo)) regs = new int[]{
                    (int)(Short.reverseBytes((short) w3) & 0xFFFF),
                    (int)(Short.reverseBytes((short) w2) & 0xFFFF),
                    (int)(Short.reverseBytes((short) w1) & 0xFFFF),
                    (int)(Short.reverseBytes((short) w0) & 0xFFFF)
            };
            else regs = new int[]{w0, w1, w2, w3};

            byte[] pdu = new byte[8];
            for (int r = 0; r < 4; r++) {
                pdu[r * 2] = (byte) ((regs[r] >> 8) & 0xFF);
                pdu[r * 2 + 1] = (byte) (regs[r] & 0xFF);
            }
            DeviceConfig config = new DeviceConfig("DEV_TEST", "测试设备", "MODEL_1", "modbus-tcp", "127.0.0.1", 502);
            Map<String, Object> params = new HashMap<>();
            params.put("byteOrder", bo);
            config.setConnectionParams(params);

            List<ThingModel.PropertySpec> points = new ArrayList<>();
            ThingModel.PropertySpec p = new ThingModel.PropertySpec("p_double", "测试双精度点位", "DOUBLE", "RW", "");
            p.setAddress("40001");
            points.add(p);

            ParsedPayload res = ModbusResponseDecoder.decode(buildModbusResponseFrame(3, pdu), config, points);
            Assert.assertEquals("DOUBLE 回环失败 byteOrder=" + bo, 123456.789, res.getPoints().get(0).getValue(), 0.001);
        }
    }

    @Test
    public void testFc16MbapFrameLength() {
        // FC16 写 2 个寄存器 (FLOAT): MBAP 长度应为 7 + 4 = 11 (0x000B), 总帧长 17 字节
        byte[] frameFloat = ModbusRequestEncoder.encodeWriteMultipleRegisters(1, 1, 0, new int[]{0x1234, 0x5678});
        Assert.assertEquals(17, frameFloat.length);
        int mbapLenFloat = ((frameFloat[4] & 0xFF) << 8) | (frameFloat[5] & 0xFF);
        Assert.assertEquals(11, mbapLenFloat);
        Assert.assertEquals(frameFloat.length - 6, mbapLenFloat);

        // FC16 写 4 个寄存器 (DOUBLE): MBAP 长度应为 7 + 8 = 15 (0x000F), 总帧长 21 字节
        byte[] frameDouble = ModbusRequestEncoder.encodeWriteMultipleRegisters(1, 1, 0, new int[]{0x1111, 0x2222, 0x3333, 0x4444});
        Assert.assertEquals(21, frameDouble.length);
        int mbapLenDouble = ((frameDouble[4] & 0xFF) << 8) | (frameDouble[5] & 0xFF);
        Assert.assertEquals(15, mbapLenDouble);
        Assert.assertEquals(frameDouble.length - 6, mbapLenDouble);
    }
}
