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

import com.unihh.iot.common.model.DeviceConfig;
import com.unihh.iot.common.model.ThingModel;
import com.unihh.iot.driver.s7.protocol.address.S7Address;
import com.unihh.iot.driver.s7.protocol.address.S7AddressParser;
import com.unihh.iot.driver.s7.protocol.codec.S7RequestEncoder;
import com.unihh.iot.driver.s7.protocol.codec.S7ResponseDecoder;
import com.unihh.iot.protocol.model.ParsedPayload;
import com.unihh.iot.protocol.util.IndustrialEndianUtils;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

/**
 * 西门子 S7 编解码器单元测试 (COTP / Setup Comm / Read Var / Write Var / 工业字节序)
 */
public class SiemensS7CodecTest {

    @Test
    public void testCotpConnectRequestEncoding() {
        // Rack 0, Slot 1 (S7-1200 / S7-1500)
        byte[] frame = S7RequestEncoder.encodeCotpConnectRequest(0, 1);
        assertNotNull(frame);
        assertEquals(22, frame.length);
        assertEquals(0x03, frame[0] & 0xFF); // TPKT magic
        assertEquals(0x00, frame[1] & 0xFF);
        assertEquals(22, frame[3] & 0xFF);   // TPKT length
        assertEquals((byte) 0xE0, frame[5]); // COTP CR

        // Called TSAP check: rack 0 slot 1 -> 0x0101 (high byte at 17, low byte at 18)
        assertEquals(0x01, frame[17] & 0xFF);
        assertEquals(0x01, frame[18] & 0xFF);

        // Rack 0, Slot 2 (S7-300 / S7-400) -> TSAP 0x0102
        byte[] frame300 = S7RequestEncoder.encodeCotpConnectRequest(0, 2);
        assertEquals(0x01, frame300[17] & 0xFF);
        assertEquals(0x02, frame300[18] & 0xFF);
    }

    @Test
    public void testCotpConnectConfirmVerification() {
        byte[] ccFrame = new byte[]{
                0x03, 0x00, 0x00, 0x16, // TPKT
                0x11,                   // COTP Len
                (byte) 0xD0,            // COTP CC (Connection Confirm)
                0x00, 0x01, 0x00, 0x01, 0x00
        };
        assertTrue(S7ResponseDecoder.isCotpConnectionConfirmed(ccFrame));

        byte[] invalidFrame = new byte[]{0x03, 0x00, 0x00, 0x16, 0x11, (byte) 0xE0};
        assertFalse(S7ResponseDecoder.isCotpConnectionConfirmed(invalidFrame));
    }

    @Test
    public void testSetupCommunicationEncodingAndAck() {
        byte[] req = S7RequestEncoder.encodeSetupCommunication(480);
        assertEquals(25, req.length);
        assertEquals(0x32, req[7] & 0xFF); // S7 Protocol
        assertEquals(0x01, req[8] & 0xFF); // Job
        assertEquals((byte) 0xF0, req[17]); // Function 0xF0 (Setup Communication)

        // Mock PLC Setup Comm Ack response
        byte[] ack = new byte[27];
        ack[0] = 0x03; ack[1] = 0x00; ack[2] = 0x00; ack[3] = 27; // TPKT
        ack[4] = 0x02; ack[5] = (byte) 0xF0; ack[6] = (byte) 0x80; // COTP DT
        ack[7] = 0x32; // Protocol ID
        ack[8] = 0x03; // Ack_Data
        ack[19] = (byte) 0xF0; // Function 0xF0
        assertTrue(S7ResponseDecoder.isSetupCommunicationAck(ack));
    }

    @Test
    public void testReadRequestEncoding() {
        List<S7Address> items = new ArrayList<>();
        items.add(S7AddressParser.parse("DB1.DBD0", "FLOAT"));
        items.add(S7AddressParser.parse("DB1.DBD4", "FLOAT"));

        byte[] req = S7RequestEncoder.encodeReadRequest(1, items);
        assertNotNull(req);
        // TPKT(4) + COTP(3) + S7Header(10) + Param(2 + 2*12 = 26) = 43 bytes
        assertEquals(43, req.length);
        assertEquals(0x04, req[17] & 0xFF); // Read Var function code
        assertEquals(2, req[18] & 0xFF);    // Item count: 2
    }

    @Test
    public void testReadResponseDecodingWithEndianness() throws Exception {
        DeviceConfig config = new DeviceConfig();
        config.setDeviceId("plc_s7_1200");
        config.setModelKey("siemens_s7");
        config.setConnectionParams(new HashMap<>());
        config.getConnectionParams().put("byteOrder", "ABCD");

        List<ThingModel.PropertySpec> specs = new ArrayList<>();
        ThingModel.PropertySpec p1 = new ThingModel.PropertySpec();
        p1.setPointCode("temperature");
        p1.setAddress("DB1.DBD0");
        p1.setDataType("FLOAT");
        specs.add(p1);

        ThingModel.PropertySpec p2 = new ThingModel.PropertySpec();
        p2.setPointCode("pressure");
        p2.setAddress("DB1.DBW4");
        p2.setDataType("INT");
        specs.add(p2);

        // Build mock response bytes
        // Value 1: float 25.5f in ABCD
        byte[] floatBytes = IndustrialEndianUtils.toBytesFloat32(25.5f, "ABCD");
        // Value 2: int 1024 in ABCD
        byte[] intBytes = IndustrialEndianUtils.toBytes16(1024, "ABCD");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // TPKT
        out.write(new byte[]{0x03, 0x00, 0x00, 0x00}); // length will be patched
        // COTP
        out.write(new byte[]{0x02, (byte) 0xF0, (byte) 0x80});
        // S7 Header (10 bytes: 0x32, 0x03(Ack), 0x00, 0x00, 0x00, 0x01, paramLen=2, dataLen=..., errClass=0, errCode=0)
        out.write(new byte[]{
                0x32, 0x03, 0x00, 0x00, 0x00, 0x01,
                0x00, 0x02, 0x00, 0x00,
                0x00, 0x00 // errClass=0, errCode=0 (indices 17, 18 in full packet)
        });
        // S7 Param: Function 0x04, Item Count 2
        out.write(new byte[]{0x04, 0x02}); // index 19, 20

        // Data item 1: ReturnCode=0xFF, TransportSize=0x04(BYTE/WORD/DWORD), LengthInBits=32(4 bytes)
        out.write(new byte[]{(byte) 0xFF, 0x04, 0x00, 0x20});
        out.write(floatBytes);

        // Data item 2: ReturnCode=0xFF, TransportSize=0x04, LengthInBits=16(2 bytes)
        out.write(new byte[]{(byte) 0xFF, 0x04, 0x00, 0x10});
        out.write(intBytes);

        byte[] rawResponse = out.toByteArray();
        rawResponse[2] = (byte) ((rawResponse.length >> 8) & 0xFF);
        rawResponse[3] = (byte) (rawResponse.length & 0xFF);

        ParsedPayload payload = S7ResponseDecoder.decodeReadResponse(rawResponse, config, specs);
        assertNotNull(payload);
        assertEquals(2, payload.getPoints().size());
        assertEquals("temperature", payload.getPoints().get(0).getPointCode());
        assertEquals(25.5, (Double) payload.getPoints().get(0).getValue(), 0.001);
        assertEquals(1, payload.getPoints().get(0).getQuality());

        assertEquals("pressure", payload.getPoints().get(1).getPointCode());
        assertEquals(1024.0, (Double) payload.getPoints().get(1).getValue(), 0.001);
        assertEquals(1, payload.getPoints().get(1).getQuality());
    }

    @Test
    public void testWriteRequestEncoding() throws Exception {
        S7Address addr = S7AddressParser.parse("DB1.DBD0", "FLOAT");
        byte[] dataBytes = IndustrialEndianUtils.toBytesFloat32(36.6f, "ABCD");
        byte[] writeFrame = S7RequestEncoder.encodeWriteRequest(1, addr, dataBytes);

        assertNotNull(writeFrame);
        assertEquals(0x03, writeFrame[0] & 0xFF); // TPKT
        assertEquals(0x32, writeFrame[7] & 0xFF); // S7 Protocol
        assertEquals(0x05, writeFrame[17] & 0xFF); // Write Var Function (0x05)
    }

    @Test
    public void testWriteResponseVerification() {
        byte[] writeAckSuccess = new byte[23];
        writeAckSuccess[0] = 0x03; writeAckSuccess[1] = 0x00; writeAckSuccess[2] = 0x00; writeAckSuccess[3] = 23;
        writeAckSuccess[4] = 0x02; writeAckSuccess[5] = (byte) 0xF0; writeAckSuccess[6] = (byte) 0x80;
        writeAckSuccess[7] = 0x32; // Protocol ID
        writeAckSuccess[8] = 0x03; // Ack_Data
        writeAckSuccess[17] = 0x00; // errClass
        writeAckSuccess[18] = 0x00; // errCode
        writeAckSuccess[19] = 0x05; // Function Write Var
        writeAckSuccess[20] = 0x01; // Item count
        writeAckSuccess[21] = (byte) 0xFF; // ReturnCode 0xFF (Success)

        assertTrue(S7ResponseDecoder.isWriteResponseSuccess(writeAckSuccess));

        // Failure case (ReturnCode 0x81 or other)
        writeAckSuccess[21] = (byte) 0x81;
        assertFalse(S7ResponseDecoder.isWriteResponseSuccess(writeAckSuccess));
    }

    @Test
    public void testMixedBitAndWordReadResponseDecoding() throws Exception {
        DeviceConfig config = new DeviceConfig();
        config.setDeviceId("plc_s7_1200");
        config.setModelKey("siemens_s7");
        config.setConnectionParams(new HashMap<>());
        config.getConnectionParams().put("byteOrder", "ABCD");

        List<ThingModel.PropertySpec> specs = new ArrayList<>();
        // Point 0: DB10.DBX0.0 (BOOL)
        ThingModel.PropertySpec p0 = new ThingModel.PropertySpec();
        p0.setPointCode("switchStatus");
        p0.setAddress("DB10.DBX0.0");
        p0.setDataType("BOOL");
        specs.add(p0);

        // Point 1: DB10.DBW10 (INT)
        ThingModel.PropertySpec p1 = new ThingModel.PropertySpec();
        p1.setPointCode("errorCode");
        p1.setAddress("DB10.DBW10");
        p1.setDataType("INT");
        specs.add(p1);

        // Point 2: DB10.DBD20 (FLOAT)
        ThingModel.PropertySpec p2 = new ThingModel.PropertySpec();
        p2.setPointCode("speed");
        p2.setAddress("DB10.DBD20");
        p2.setDataType("FLOAT");
        specs.add(p2);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // TPKT
        out.write(new byte[]{0x03, 0x00, 0x00, 0x00}); // length will be patched
        // COTP
        out.write(new byte[]{0x02, (byte) 0xF0, (byte) 0x80});
        // S7 Header (10 bytes: 0x32, 0x03(Ack), 0x00, 0x00, 0x00, 0x01, paramLen=2, dataLen=..., errClass=0, errCode=0)
        out.write(new byte[]{
                0x32, 0x03, 0x00, 0x00, 0x00, 0x01,
                0x00, 0x02, 0x00, 0x00,
                0x00, 0x00 // errClass=0, errCode=0
        });
        // S7 Param: Function 0x04, Item Count 3
        out.write(new byte[]{0x04, 0x03});

        // Item 0: BOOL (DB10.DBX0.0). RC=0xFF, TS=0x03(BIT), BitLen=1, Value=0x01 (true)
        out.write(new byte[]{(byte) 0xFF, 0x03, 0x00, 0x01, 0x01});
        // Notice: S7Comm does NOT insert padding byte after BIT. Item 1 directly follows!

        // Item 1: INT (DB10.DBW10). RC=0xFF, TS=0x04(WORD), BitLen=16(2 bytes), Value=42
        byte[] intBytes = IndustrialEndianUtils.toBytes16(42, "ABCD");
        out.write(new byte[]{(byte) 0xFF, 0x04, 0x00, 0x10});
        out.write(intBytes);

        // Item 2: FLOAT (DB10.DBD20). RC=0xFF, TS=0x04(DWORD), BitLen=32(4 bytes), Value=123.45f
        byte[] floatBytes = IndustrialEndianUtils.toBytesFloat32(123.45f, "ABCD");
        out.write(new byte[]{(byte) 0xFF, 0x04, 0x00, 0x20});
        out.write(floatBytes);

        byte[] rawResponse = out.toByteArray();
        rawResponse[2] = (byte) ((rawResponse.length >> 8) & 0xFF);
        rawResponse[3] = (byte) (rawResponse.length & 0xFF);

        ParsedPayload payload = S7ResponseDecoder.decodeReadResponse(rawResponse, config, specs);
        assertNotNull(payload);
        assertEquals(3, payload.getPoints().size());

        // Verify Point 0
        assertEquals("switchStatus", payload.getPoints().get(0).getPointCode());
        assertEquals(1.0, (Double) payload.getPoints().get(0).getValue(), 0.001);
        assertEquals(1, payload.getPoints().get(0).getQuality());

        // Verify Point 1
        assertEquals("errorCode", payload.getPoints().get(1).getPointCode());
        assertEquals(42.0, (Double) payload.getPoints().get(1).getValue(), 0.001);
        assertEquals(1, payload.getPoints().get(1).getQuality());

        // Verify Point 2
        assertEquals("speed", payload.getPoints().get(2).getPointCode());
        assertEquals(123.45, (Double) payload.getPoints().get(2).getValue(), 0.01);
        assertEquals(1, payload.getPoints().get(2).getQuality());
    }
}
