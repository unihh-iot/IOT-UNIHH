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

import com.unihh.iot.common.model.DeviceConfig;
import com.unihh.iot.common.model.PointData;
import com.unihh.iot.common.model.ThingModel;
import com.unihh.iot.driver.s7.protocol.address.S7Address;
import com.unihh.iot.driver.s7.protocol.address.S7AddressParser;
import com.unihh.iot.driver.s7.protocol.frame.S7DataType;
import com.unihh.iot.driver.s7.protocol.frame.S7TransportSize;
import com.unihh.iot.protocol.model.ParsedPayload;
import com.unihh.iot.protocol.util.IndustrialEndianUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 西门子 S7 响应报文二进制解码器
 * <p>
 * 支持多测点批量解码、ReturnCode 品质校验、工业字节序 (Endianness) 转换与物理量纲换算。
 */
public class S7ResponseDecoder {
    private static final Logger log = LoggerFactory.getLogger(S7ResponseDecoder.class);

    /**
     * 校验 COTP Connection Confirm (CC) 响应
     */
    public static boolean isCotpConnectionConfirmed(byte[] response) {
        if (response == null || response.length < 7) {
            return false;
        }
        // TPKT: response[0..3], COTP: response[4] = length, response[5] = PDU Type (0xD0: CC)
        return (response[5] & 0xFF) == 0xD0;
    }

    /**
     * 校验 S7 Setup Communication 协商响应
     */
    public static boolean isSetupCommunicationAck(byte[] response) {
        if (response == null || response.length < 19) {
            return false;
        }
        // S7 Header: response[7] == 0x32 (Protocol ID), response[8] == 0x03 (Ack_Data)
        // Function: response[19] == 0xF0 (Setup Communication)
        return (response[7] & 0xFF) == 0x32
                && (response[8] & 0xFF) == 0x03
                && response.length > 19
                && (response[19] & 0xFF) == 0xF0;
    }

    /**
     * 解析 S7 批量点位读取响应 (Read Var Response, Function 0x04)
     *
     * @param responseBytes S7 返回的完整二进制报文
     * @param config        设备通信配置 (包含字节序 byteOrder、缩放系数等)
     * @param points        本次请求读取的测点规格列表
     * @return 标准化 ParsedPayload
     */
    public static ParsedPayload decodeReadResponse(byte[] responseBytes, DeviceConfig config, List<ThingModel.PropertySpec> points) {
        ParsedPayload payload = new ParsedPayload();
        payload.setDeviceId(config != null ? config.getDeviceId() : null);
        payload.setModelKey(config != null ? config.getModelKey() : null);
        long now = System.currentTimeMillis();

        if (responseBytes == null || responseBytes.length < 21) {
            log.warn("S7 响应报文过短 (长度: {}), 无法解析", responseBytes != null ? responseBytes.length : 0);
            return fillErrorPayload(payload, config, points, now, "S7_RESPONSE_TOO_SHORT");
        }

        // 校验 S7 Header: response[7] 必须为 0x32, response[8] 必须为 0x03 (Ack_Data)
        if ((responseBytes[7] & 0xFF) != 0x32 || (responseBytes[8] & 0xFF) != 0x03) {
            log.warn("S7 响应头非有效 Ack_Data: ID=0x{}, Type=0x{}",
                    Integer.toHexString(responseBytes[7] & 0xFF),
                    Integer.toHexString(responseBytes[8] & 0xFF));
            return fillErrorPayload(payload, config, points, now, "S7_INVALID_HEADER");
        }

        // 提取全局 Error Class 与 Error Code (位于 S7 Header 末尾 bytes 17, 18)
        int errClass = responseBytes[17] & 0xFF;
        int errCode = responseBytes[18] & 0xFF;
        if (errClass != 0 || errCode != 0) {
            log.warn("S7 PLC 返回全局错误码: Class=0x{}, Code=0x{}",
                    Integer.toHexString(errClass), Integer.toHexString(errCode));
            return fillErrorPayload(payload, config, points, now, "S7_GLOBAL_ERROR_" + errClass + "_" + errCode);
        }

        // 提取返回的项目数量
        int returnedItemCount = responseBytes[20] & 0xFF;
        int expectedCount = points.size();
        int parseCount = Math.min(returnedItemCount, expectedCount);

        // 字节序设置 (西门子原生默认 ABCD 大端模式)
        String byteOrder = "ABCD";
        if (config != null && config.getConnectionParams() != null && config.getConnectionParams().get("byteOrder") != null) {
            byteOrder = config.getConnectionParams().get("byteOrder").toString().trim();
        }

        int offset = 21; // 数据起始位置

        for (int i = 0; i < parseCount; i++) {
            ThingModel.PropertySpec spec = points.get(i);
            S7Address addr = S7AddressParser.parse(spec.getAddress(), spec.getDataType());

            if (offset + 4 > responseBytes.length) {
                log.warn("S7 响应数据包提前截断, index={}", i);
                PointData pd = new PointData(config.getModelKey(), config.getDeviceId(), spec.getPointCode(), 0.0, now);
                pd.setQuality(0);
                pd.setRawValue("S7_TRUNCATED_PACKET");
                payload.getPoints().add(pd);
                break;
            }

            int returnCode = responseBytes[offset] & 0xFF;

            // ReturnCode == 0xFF 代表 S7 硬件读取成功
            if (returnCode != 0xFF) {
                log.warn("S7 测点 [{}] 地址 [{}] 读取失败, ReturnCode=0x{}",
                        spec.getPointCode(), spec.getAddress(), Integer.toHexString(returnCode));
                PointData pd = new PointData(config.getModelKey(), config.getDeviceId(), spec.getPointCode(), 0.0, now);
                pd.setQuality(0);
                pd.setRawValue("S7_ERROR_RETURN_CODE_0x" + Integer.toHexString(returnCode));
                payload.getPoints().add(pd);

                // 西门子 S7 规范: 当 ReturnCode != 0xFF 时，该项无数据负载。
                // 某些 PLC 会填充 4 字节的错误头(RC, 0x00, 0x00, 0x00)，有的仅占 1 字节。
                if (offset + 3 < responseBytes.length
                        && responseBytes[offset + 1] == 0x00
                        && responseBytes[offset + 2] == 0x00
                        && responseBytes[offset + 3] == 0x00) {
                    offset += 4;
                } else {
                    offset += 1;
                }

                // 检查是否有 0x00 填充对齐字节
                if (i < parseCount - 1 && offset < responseBytes.length && responseBytes[offset] == 0x00) {
                    offset++;
                }
                continue;
            }

            int transportSize = responseBytes[offset + 1] & 0xFF;
            int lengthInBits = ((responseBytes[offset + 2] & 0xFF) << 8) | (responseBytes[offset + 3] & 0xFF);

            int byteLen;
            if (transportSize == 0x03) {
                // BIT
                byteLen = (lengthInBits + 7) / 8;
            } else if (transportSize == 0x09) {
                // OCTET_STRING (长度直接以字节为单位)
                byteLen = lengthInBits;
            } else {
                // BYTE / WORD / DWORD (长度以位为单位)
                byteLen = lengthInBits / 8;
            }

            int dataStart = offset + 4;
            offset += 4 + byteLen;

            // 检查西门子 S7 Comm 协议填充字节 (Padding):
            // 只有当紧随其后的字节为 0x00 填充字节且非最后一项时才跳过，严禁依据 byteLen 奇数盲目跳过
            if (i < parseCount - 1 && offset < responseBytes.length && responseBytes[offset] == 0x00) {
                offset++;
            }

            if (dataStart + byteLen > responseBytes.length) {
                PointData pd = new PointData(config.getModelKey(), config.getDeviceId(), spec.getPointCode(), 0.0, now);
                pd.setQuality(0);
                pd.setRawValue("S7_DATA_LENGTH_OVERFLOW");
                payload.getPoints().add(pd);
                continue;
            }

            // 解析数值
            PointData pd = parsePointValue(responseBytes, dataStart, byteLen, addr, spec, config, byteOrder, now);
            payload.getPoints().add(pd);
        }

        return payload;
    }

    /**
     * 校验 S7 Write Var 写入响应
     */
    public static boolean isWriteResponseSuccess(byte[] responseBytes) {
        if (responseBytes == null || responseBytes.length < 22) {
            return false;
        }
        // S7 Header: response[7] == 0x32, response[8] == 0x03 (Ack_Data)
        if ((responseBytes[7] & 0xFF) != 0x32 || (responseBytes[8] & 0xFF) != 0x03) {
            return false;
        }
        int errClass = responseBytes[17] & 0xFF;
        int errCode = responseBytes[18] & 0xFF;
        if (errClass != 0 || errCode != 0) {
            return false;
        }
        // Function 0x05, item count response[20]
        // 数据部分的首个 returnCode (response[21]) 必须为 0xFF
        return (responseBytes[21] & 0xFF) == 0xFF;
    }

    private static PointData parsePointValue(byte[] raw, int start, int length, S7Address addr,
                                             ThingModel.PropertySpec spec, DeviceConfig config,
                                             String byteOrder, long now) {
        PointData pd = new PointData(config.getModelKey(), config.getDeviceId(), spec.getPointCode(), 0.0, now);
        pd.setQuality(1);

        S7DataType dt = addr.getDataType();
        double scale = (spec != null && spec.getScale() != null) ? spec.getScale() : 1.0;
        double offset = (spec != null && spec.getOffset() != null) ? spec.getOffset() : 0.0;

        try {
            switch (dt) {
                case BOOL: {
                    int b = raw[start] & 0xFF;
                    boolean val;
                    // 西门子 S7 在按位读取时 (TransportSize 为 BIT)，PLC 返回单字节负载且最低位(bit 0)即代表该位值
                    if (addr.getTransportSize() == S7TransportSize.BIT || length == 1) {
                        val = (b & 0x01) == 1;
                    } else {
                        int bit = addr.getBitOffset();
                        val = ((b >> bit) & 0x01) == 1;
                    }
                    pd.setValue(val ? 1.0 : 0.0);
                    pd.setRawValue(String.valueOf(val));
                    break;
                }
                case BYTE:
                case CHAR: {
                    int val = raw[start] & 0xFF;
                    double scaled = val * scale + offset;
                    pd.setValue(scaled);
                    pd.setRawValue(String.valueOf(val));
                    break;
                }
                case INT: {
                    if (length >= 2) {
                        int rawInt = IndustrialEndianUtils.readInt16(raw, start, byteOrder);
                        double scaled = rawInt * scale + offset;
                        pd.setValue(scaled);
                        pd.setRawValue(String.valueOf(rawInt));
                    }
                    break;
                }
                case WORD: {
                    if (length >= 2) {
                        int rawInt = IndustrialEndianUtils.readInt16(raw, start, byteOrder) & 0xFFFF;
                        double scaled = rawInt * scale + offset;
                        pd.setValue(scaled);
                        pd.setRawValue(String.valueOf(rawInt));
                    }
                    break;
                }
                case DINT: {
                    if (length >= 4) {
                        int rawInt = IndustrialEndianUtils.readInt32(raw, start, byteOrder);
                        double scaled = rawInt * scale + offset;
                        pd.setValue(scaled);
                        pd.setRawValue(String.valueOf(rawInt));
                    } else if (length >= 2) {
                        int rawInt = IndustrialEndianUtils.readInt16(raw, start, byteOrder);
                        double scaled = rawInt * scale + offset;
                        pd.setValue(scaled);
                        pd.setRawValue(String.valueOf(rawInt));
                    }
                    break;
                }
                case DWORD: {
                    if (length >= 4) {
                        long rawUInt = IndustrialEndianUtils.readInt32(raw, start, byteOrder) & 0xFFFFFFFFL;
                        double scaled = rawUInt * scale + offset;
                        pd.setValue(scaled);
                        pd.setRawValue(String.valueOf(rawUInt));
                    } else if (length >= 2) {
                        int rawInt = IndustrialEndianUtils.readInt16(raw, start, byteOrder) & 0xFFFF;
                        double scaled = rawInt * scale + offset;
                        pd.setValue(scaled);
                        pd.setRawValue(String.valueOf(rawInt));
                    }
                    break;
                }
                case REAL: {
                    if (length >= 4) {
                        float floatVal = IndustrialEndianUtils.readFloat32(raw, start, byteOrder);
                        double scaled = floatVal * scale + offset;
                        double rounded = Math.round(scaled * 10000.0) / 10000.0;
                        pd.setValue(rounded);
                        pd.setRawValue(String.valueOf(floatVal));
                    } else if (length >= 2) {
                        int rawInt = IndustrialEndianUtils.readInt16(raw, start, byteOrder);
                        double scaled = rawInt * scale + offset;
                        pd.setValue(scaled);
                        pd.setRawValue(String.valueOf(rawInt));
                    }
                    break;
                }
                case LREAL: {
                    if (length >= 8) {
                        double dVal = IndustrialEndianUtils.readFloat64(raw, start, byteOrder);
                        double scaled = dVal * scale + offset;
                        pd.setValue(scaled);
                        pd.setRawValue(String.valueOf(dVal));
                    } else if (length >= 4) {
                        float floatVal = IndustrialEndianUtils.readFloat32(raw, start, byteOrder);
                        double scaled = floatVal * scale + offset;
                        double rounded = Math.round(scaled * 10000.0) / 10000.0;
                        pd.setValue(rounded);
                        pd.setRawValue(String.valueOf(floatVal));
                    } else if (length >= 2) {
                        int rawInt = IndustrialEndianUtils.readInt16(raw, start, byteOrder);
                        double scaled = rawInt * scale + offset;
                        pd.setValue(scaled);
                        pd.setRawValue(String.valueOf(rawInt));
                    }
                    break;
                }
                case STRING: {
                    if (length >= 2) {
                        int maxLen = raw[start] & 0xFF;
                        int actLen = raw[start + 1] & 0xFF;
                        int actualCount = Math.min(actLen, length - 2);
                        String str = new String(raw, start + 2, actualCount, StandardCharsets.US_ASCII);
                        pd.setValue(0.0);
                        pd.setRawValue(str);
                    }
                    break;
                }
                default: {
                    if (length >= 4) {
                        float floatVal = IndustrialEndianUtils.readFloat32(raw, start, byteOrder);
                        pd.setValue((double) floatVal);
                        pd.setRawValue(String.valueOf(floatVal));
                    } else if (length >= 2) {
                        int rawInt = IndustrialEndianUtils.readInt16(raw, start, byteOrder);
                        pd.setValue((double) rawInt);
                        pd.setRawValue(String.valueOf(rawInt));
                    }
                    break;
                }
            }
        } catch (Exception e) {
            log.warn("解析 S7 测点 [{}] 数值异常: {}", spec.getPointCode(), e.getMessage());
            pd.setQuality(0);
            pd.setRawValue("DECODE_ERROR: " + e.getMessage());
        }

        return pd;
    }

    private static ParsedPayload fillErrorPayload(ParsedPayload payload, DeviceConfig config,
                                                 List<ThingModel.PropertySpec> points, long now, String reason) {
        if (points != null) {
            for (ThingModel.PropertySpec spec : points) {
                PointData pd = new PointData(config.getModelKey(), config.getDeviceId(), spec.getPointCode(), 0.0, now);
                pd.setQuality(0);
                pd.setRawValue("S7_COMM_FAILURE: " + reason);
                payload.getPoints().add(pd);
            }
        }
        return payload;
    }
}
