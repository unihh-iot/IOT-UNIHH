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

import com.unihh.iot.common.model.DeviceConfig;
import com.unihh.iot.common.model.PointData;
import com.unihh.iot.common.model.ThingModel;
import com.unihh.iot.driver.modbus.protocol.address.ModbusAddressParser;
import com.unihh.iot.driver.modbus.protocol.frame.ModbusDataType;
import com.unihh.iot.driver.modbus.protocol.frame.ModbusFunctionCode;
import com.unihh.iot.driver.modbus.protocol.frame.ModbusHeader;
import com.unihh.iot.protocol.model.ParsedPayload;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;

/**
 * Modbus 响应报文解码器
 * 负责解析从网关/设备收到的二进制 Response 字节数组，并映射为结构化的 ParsedPayload / PointData
 */
public class ModbusResponseDecoder {

    /**
     * 将 Modbus 响应报文解码为物模型点位数据
     */
    public static ParsedPayload decode(byte[] rawData, DeviceConfig config, List<ThingModel.PropertySpec> points) {
        ParsedPayload payload = new ParsedPayload();
        payload.setDeviceId(config.getDeviceId());
        payload.setModelKey(config.getModelKey());
        long now = System.currentTimeMillis();

        if (rawData == null || rawData.length < 9) {
            return payload;
        }

        // 解析 MBAP Header (0..6 字节)
        ModbusHeader header = ModbusHeader.decode(rawData);
        int fcCode = rawData[7] & 0xFF;
        ModbusFunctionCode fc = ModbusFunctionCode.fromCode(fcCode);
        int byteCount = rawData[8] & 0xFF;

        byte[] pduData = Arrays.copyOfRange(rawData, 9, Math.min(rawData.length, 9 + byteCount));

        if (points == null || points.isEmpty()) {
            return payload;
        }

        int minRegAddr = Integer.MAX_VALUE;
        for (ThingModel.PropertySpec p : points) {
            int addr = ModbusAddressParser.parseRegisterAddress(p.getAddress());
            if (addr < minRegAddr) {
                minRegAddr = addr;
            }
        }
        int startRegAddr = (minRegAddr == Integer.MAX_VALUE) ? 0 : minRegAddr;

        String byteOrder = "ABCD";
        if (config != null && config.getConnectionParams() != null && config.getConnectionParams().containsKey("byteOrder")) {
            Object bo = config.getConnectionParams().get("byteOrder");
            if (bo != null) {
                byteOrder = bo.toString().trim().toUpperCase();
            }
        }

        for (ThingModel.PropertySpec spec : points) {
            int regAddress = ModbusAddressParser.parseRegisterAddress(spec.getAddress());
            ModbusDataType dataType = ModbusDataType.fromString(spec.getDataType());

            double value = 0.0;
            int quality = 1;

            try {
                if (fc == ModbusFunctionCode.READ_COILS || fc == ModbusFunctionCode.READ_DISCRETE_INPUTS) {
                    int bitAddress = regAddress - startRegAddr;
                    if (bitAddress >= 0) {
                        int byteOffset = bitAddress / 8;
                        int bitOffset = bitAddress % 8;
                        if (byteOffset < pduData.length) {
                            value = ((pduData[byteOffset] >> bitOffset) & 0x01);
                        } else {
                            quality = 0;
                        }
                    } else {
                        quality = 0;
                    }
                } else { // 保持寄存器 / 输入寄存器
                    int regOffset = regAddress - startRegAddr;
                    int byteOffset = regOffset * 2;
                    if (byteOffset >= 0 && byteOffset + 1 < pduData.length) {
                        if (dataType == ModbusDataType.BOOL) {
                            value = (pduData[byteOffset + 1] & 0x01);
                        } else if (dataType == ModbusDataType.INT16 || dataType == ModbusDataType.UINT16 || dataType == ModbusDataType.ENUM) {
                            byte[] int16Bytes = extractBytes(pduData, byteOffset, 2, byteOrder);
                            short rawShort = (short) (((int16Bytes[0] & 0xFF) << 8) | (int16Bytes[1] & 0xFF));
                            value = (dataType == ModbusDataType.UINT16 || dataType == ModbusDataType.ENUM) ? (rawShort & 0xFFFF) : rawShort;
                        } else if (dataType == ModbusDataType.INT32 || dataType == ModbusDataType.UINT32) {
                            if (byteOffset + 3 < pduData.length) {
                                byte[] int32Bytes = extractBytes(pduData, byteOffset, 4, byteOrder);
                                ByteBuffer buf = ByteBuffer.wrap(int32Bytes);
                                buf.order(ByteOrder.BIG_ENDIAN);
                                int rawInt = buf.getInt();
                                value = (dataType == ModbusDataType.UINT32) ? Integer.toUnsignedLong(rawInt) : rawInt;
                            } else {
                                quality = 0;
                            }
                        } else if (dataType == ModbusDataType.INT64 || dataType == ModbusDataType.UINT64) {
                            if (byteOffset + 7 < pduData.length) {
                                byte[] int64Bytes = extractBytes(pduData, byteOffset, 8, byteOrder);
                                ByteBuffer buf = ByteBuffer.wrap(int64Bytes);
                                buf.order(ByteOrder.BIG_ENDIAN);
                                long rawLong = buf.getLong();
                                if (dataType == ModbusDataType.UINT64) {
                                    value = (rawLong < 0) ? new java.math.BigInteger(1, int64Bytes).doubleValue() : rawLong;
                                } else {
                                    value = rawLong;
                                }
                            } else {
                                quality = 0;
                            }
                        } else if (dataType == ModbusDataType.FLOAT) {
                            if (byteOffset + 3 < pduData.length) {
                                byte[] floatBytes = extractBytes(pduData, byteOffset, 4, byteOrder);
                                ByteBuffer buf = ByteBuffer.wrap(floatBytes);
                                buf.order(ByteOrder.BIG_ENDIAN);
                                float floatVal = buf.getFloat();
                                if (Float.isNaN(floatVal) || Float.isInfinite(floatVal)) {
                                    quality = 0;
                                } else {
                                    value = Double.parseDouble(Float.toString(floatVal));
                                }
                            } else {
                                quality = 0;
                            }
                        } else if (dataType == ModbusDataType.DOUBLE) {
                            if (byteOffset + 7 < pduData.length) {
                                byte[] doubleBytes = extractBytes(pduData, byteOffset, 8, byteOrder);
                                ByteBuffer buf = ByteBuffer.wrap(doubleBytes);
                                buf.order(ByteOrder.BIG_ENDIAN);
                                double doubleVal = buf.getDouble();
                                if (Double.isNaN(doubleVal) || Double.isInfinite(doubleVal)) {
                                    quality = 0;
                                } else {
                                    value = stripDoublePrecisionNoise(doubleVal);
                                }
                            } else if (byteOffset + 3 < pduData.length) {
                                // 容错：若从站仅返回 4 字节，兼容按 32 位单精度浮点解析
                                byte[] floatBytes = extractBytes(pduData, byteOffset, 4, byteOrder);
                                ByteBuffer buf = ByteBuffer.wrap(floatBytes);
                                buf.order(ByteOrder.BIG_ENDIAN);
                                float floatVal = buf.getFloat();
                                if (Float.isNaN(floatVal) || Float.isInfinite(floatVal)) {
                                    quality = 0;
                                } else {
                                    value = Double.parseDouble(Float.toString(floatVal));
                                }
                            } else {
                                quality = 0;
                            }
                        } else {
                            // 默认按 16 位整数处理
                            byte[] int16Bytes = extractBytes(pduData, byteOffset, 2, byteOrder);
                            short rawShort = (short) (((int16Bytes[0] & 0xFF) << 8) | (int16Bytes[1] & 0xFF));
                            value = rawShort;
                        }
                    } else {
                        quality = 0;
                    }
                }
            } catch (Exception e) {
                quality = 0;
            }

            // 保存原始寄存器解析值文本 (整数输出规整)
            String rawValStr = (quality == 1)
                    ? ((value % 1 == 0 && Math.abs(value) < 1e15) ? String.valueOf((long) value) : String.valueOf(value))
                    : "0";

            // 执行工程换算: actualValue = rawValue * scale + offset
            double finalValue = value;
            if (quality == 1) {
                double scale = (spec.getScale() != null) ? spec.getScale() : 1.0;
                double offset = (spec.getOffset() != null) ? spec.getOffset() : 0.0;
                if (scale != 1.0 || offset != 0.0) {
                    finalValue = finalValue * scale + offset;
                    finalValue = stripDoublePrecisionNoise(finalValue);
                }
            }

            PointData pd = new PointData(config.getModelKey(), config.getDeviceId(), spec.getPointCode(), finalValue, now);
            pd.setQuality(quality);
            pd.setRawValue(rawValStr);
            payload.getPoints().add(pd);
        }

        return payload;
    }

    /**
     * 根据配置的字节序/字序重排原始寄存器字节 (支持 16位的 AB/BA, 32位的 ABCD/CDAB/BADC/DCBA, 以及 64位的 ABCDEFGH/GHEFCDAB/HGFEDCBA/BADCFEHG)
     */
    private static byte[] extractBytes(byte[] src, int offset, int length, String byteOrder) {
        byte[] bytes = new byte[length];
        if (length == 2) {
            if ("BADC".equalsIgnoreCase(byteOrder) || "BA".equalsIgnoreCase(byteOrder) || "DCBA".equalsIgnoreCase(byteOrder) || "LITTLE_ENDIAN".equalsIgnoreCase(byteOrder) || "BADCFEHG".equalsIgnoreCase(byteOrder) || "HGFEDCBA".equalsIgnoreCase(byteOrder)) {
                // Byte swap within 16-bit register: BA
                bytes[0] = src[offset + 1];
                bytes[1] = src[offset];
            } else {
                // Standard big endian: AB
                bytes[0] = src[offset];
                bytes[1] = src[offset + 1];
            }
        } else if (length == 4) {
            if ("CDAB".equalsIgnoreCase(byteOrder) || "SWAP_WORDS".equalsIgnoreCase(byteOrder) || "MID_LITTLE".equalsIgnoreCase(byteOrder)) {
                // Word swap: CD AB
                bytes[0] = src[offset + 2];
                bytes[1] = src[offset + 3];
                bytes[2] = src[offset];
                bytes[3] = src[offset + 1];
            } else if ("BADC".equalsIgnoreCase(byteOrder)) {
                // Byte swap: BA DC
                bytes[0] = src[offset + 1];
                bytes[1] = src[offset];
                bytes[2] = src[offset + 3];
                bytes[3] = src[offset + 2];
            } else if ("DCBA".equalsIgnoreCase(byteOrder) || "LITTLE_ENDIAN".equalsIgnoreCase(byteOrder)) {
                // True little endian: DC BA
                bytes[0] = src[offset + 3];
                bytes[1] = src[offset + 2];
                bytes[2] = src[offset + 1];
                bytes[3] = src[offset];
            } else {
                // Standard big endian: AB CD
                System.arraycopy(src, offset, bytes, 0, 4);
            }
        } else if (length == 8) {
            if ("GHEFCDAB".equalsIgnoreCase(byteOrder) || "SWAP_WORDS".equalsIgnoreCase(byteOrder) || "CDAB".equalsIgnoreCase(byteOrder) || "MID_LITTLE".equalsIgnoreCase(byteOrder)) {
                // Word swap for 64-bit: GH EF CD AB (4 words reversed)
                bytes[0] = src[offset + 6];
                bytes[1] = src[offset + 7];
                bytes[2] = src[offset + 4];
                bytes[3] = src[offset + 5];
                bytes[4] = src[offset + 2];
                bytes[5] = src[offset + 3];
                bytes[6] = src[offset];
                bytes[7] = src[offset + 1];
            } else if ("HGFEDCBA".equalsIgnoreCase(byteOrder) || "LITTLE_ENDIAN".equalsIgnoreCase(byteOrder) || "DCBA".equalsIgnoreCase(byteOrder)) {
                // True little endian: HG FE DC BA
                for (int i = 0; i < 8; i++) {
                    bytes[i] = src[offset + 7 - i];
                }
            } else if ("BADCFEHG".equalsIgnoreCase(byteOrder) || "BADC".equalsIgnoreCase(byteOrder)) {
                // Byte swap for 64-bit: BA DC FE HG
                for (int i = 0; i < 8; i += 2) {
                    bytes[i] = src[offset + i + 1];
                    bytes[i + 1] = src[offset + i];
                }
            } else {
                // Standard big endian: AB CD EF GH
                System.arraycopy(src, offset, bytes, 0, 8);
            }
        } else {
            System.arraycopy(src, offset, bytes, 0, Math.min(length, src.length - offset));
        }
        return bytes;
    }

    /**
     * 消除 IEEE-754 浮点数二进制近似表示产生的细微精度尾数噪点 (如 1325.6599999999999 -> 1325.66)
     */
    private static double stripDoublePrecisionNoise(double val) {
        if (Double.isNaN(val) || Double.isInfinite(val)) return val;
        try {
            // 对常见工业测点精度 (6位小数) 进行四舍五入规整
            BigDecimal bd = new BigDecimal(Double.toString(val));
            return bd.setScale(6, RoundingMode.HALF_UP).stripTrailingZeros().doubleValue();
        } catch (Exception e) {
            return val;
        }
    }
}
