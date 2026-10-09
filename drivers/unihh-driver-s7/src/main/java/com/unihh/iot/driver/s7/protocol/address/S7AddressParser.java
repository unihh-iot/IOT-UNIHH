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

package com.unihh.iot.driver.s7.protocol.address;

import com.unihh.iot.driver.s7.protocol.frame.S7Area;
import com.unihh.iot.driver.s7.protocol.frame.S7DataType;
import com.unihh.iot.driver.s7.protocol.frame.S7TransportSize;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 西门子 S7 全系列工业测点地址解析器
 * <p>
 * 支持 DB区、M区(标志位)、I区(输入)、Q区(输出)、V区(S7-200/Smart变量) 等标准工业表示法。
 */
public class S7AddressParser {

    // 1. DB 区正则: DB1.DBX0.0, DB1.DBB10, DB1.DBW2, DB1.DBD4, DB1.DBL8
    private static final Pattern PATTERN_DB = Pattern.compile(
            "^DB(\\d+)\\.DB([XBWDL])(\\d+)(?:\\.(\\d+))?$", Pattern.CASE_INSENSITIVE);

    // 2. M / I / Q / V 区域多字节/字/双字正则: MB10, MW20, MD30, IB0, IW2, ID4, QB0, QW2, QD4, VB10, VW20, VD30
    private static final Pattern PATTERN_AREA_WORD = Pattern.compile(
            "^([MIQV])([BWDL])(\\d+)$", Pattern.CASE_INSENSITIVE);

    // 3. M / I / Q / V 区域位正则: M0.0, MX0.0, I0.1, IX0.1, Q0.0, QX0.0, V100.0, VX100.0
    private static final Pattern PATTERN_AREA_BIT = Pattern.compile(
            "^([MIQV])(?:X)?(\\d+)\\.(\\d+)$", Pattern.CASE_INSENSITIVE);

    /**
     * 解析 S7 地址字符串
     *
     * @param addressStr  地址表达式 (如 DB1.DBD0, M0.0, IW2)
     * @param explicitType 物模型定义的数据类型 (可选，如 FLOAT, INT, BOOL)
     * @return 解析后的 S7Address 模型
     */
    public static S7Address parse(String addressStr, String explicitType) {
        if (addressStr == null || addressStr.trim().isEmpty()) {
            return new S7Address(S7Area.DATA_BLOCKS, 1, 0, 0, S7TransportSize.REAL, 1, S7DataType.REAL);
        }

        String raw = addressStr.trim().replaceAll("\\s+", "");
        S7DataType dt = (explicitType != null && !explicitType.trim().isEmpty())
                ? S7DataType.fromString(explicitType) : null;

        // 1. 匹配 DB 区
        Matcher mDb = PATTERN_DB.matcher(raw);
        if (mDb.matches()) {
            int dbNum = Integer.parseInt(mDb.group(1));
            String sizeTag = mDb.group(2).toUpperCase();
            int byteOffset = Integer.parseInt(mDb.group(3));
            int bitOffset = mDb.group(4) != null ? Integer.parseInt(mDb.group(4)) : 0;

            return buildAddress(S7Area.DATA_BLOCKS, dbNum, sizeTag, byteOffset, bitOffset, dt);
        }

        // 2. 匹配 M / I / Q / V 区字节/字/双字/四字
        Matcher mWord = PATTERN_AREA_WORD.matcher(raw);
        if (mWord.matches()) {
            String areaTag = mWord.group(1).toUpperCase();
            String sizeTag = mWord.group(2).toUpperCase();
            int byteOffset = Integer.parseInt(mWord.group(3));

            S7Area area = resolveArea(areaTag);
            int dbNum = "V".equals(areaTag) ? 1 : 0;
            return buildAddress(area, dbNum, sizeTag, byteOffset, 0, dt);
        }

        // 3. 匹配 M / I / Q / V 区位寻址
        Matcher mBit = PATTERN_AREA_BIT.matcher(raw);
        if (mBit.matches()) {
            String areaTag = mBit.group(1).toUpperCase();
            int byteOffset = Integer.parseInt(mBit.group(2));
            int bitOffset = Integer.parseInt(mBit.group(3));

            S7Area area = resolveArea(areaTag);
            int dbNum = "V".equals(areaTag) ? 1 : 0;
            return buildAddress(area, dbNum, "X", byteOffset, bitOffset, dt != null ? dt : S7DataType.BOOL);
        }

        // 4. 容错解析: 纯数字 (视为 DB1.DBD 偏移)
        try {
            int offset = Integer.parseInt(raw);
            return new S7Address(S7Area.DATA_BLOCKS, 1, offset, 0, S7TransportSize.REAL, 1, S7DataType.REAL);
        } catch (NumberFormatException ignored) {}

        // 默认兜底
        return new S7Address(S7Area.DATA_BLOCKS, 1, 0, 0, S7TransportSize.REAL, 1, S7DataType.REAL);
    }

    private static S7Area resolveArea(String areaTag) {
        switch (areaTag) {
            case "M": return S7Area.FLAGS;
            case "I": return S7Area.INPUTS;
            case "Q": return S7Area.OUTPUTS;
            case "V": return S7Area.DATA_BLOCKS; // S7-200 V 存储区属于 DB1
            default: return S7Area.DATA_BLOCKS;
        }
    }

    private static S7Address buildAddress(S7Area area, int dbNum, String sizeTag, int byteOffset, int bitOffset, S7DataType explicitDt) {
        S7TransportSize ts;
        S7DataType dt;
        int count = 1;

        switch (sizeTag) {
            case "X": // 位 (1 bit)
                ts = S7TransportSize.BIT;
                dt = (explicitDt != null) ? explicitDt : S7DataType.BOOL;
                count = 1;
                break;
            case "B": // 字节 (1 byte)
                ts = S7TransportSize.BYTE;
                dt = (explicitDt != null && explicitDt.getByteLength() == 1) ? explicitDt : S7DataType.BYTE;
                count = 1;
                break;
            case "W": // 16位字 (2 bytes)
                // 西门子 S7 通信规范: 多字节寻址在 S7Any 中统一使用 BYTE(0x02) 并以字节数作为 count，确保各型号 PLC 兼容性
                ts = S7TransportSize.BYTE;
                count = 2;
                if (explicitDt == S7DataType.WORD) {
                    dt = S7DataType.WORD;
                } else {
                    dt = S7DataType.INT;
                }
                break;
            case "D": // 32位双字 (若物模型显式定义为 64位 DOUBLE/LREAL 则读取 8 字节 LREAL)
                ts = S7TransportSize.BYTE;
                if (explicitDt == S7DataType.LREAL) {
                    count = 8;
                    dt = S7DataType.LREAL;
                } else if (explicitDt == S7DataType.DINT || explicitDt == S7DataType.DWORD || explicitDt == S7DataType.INT) {
                    count = 4;
                    dt = (explicitDt == S7DataType.DWORD) ? S7DataType.DWORD : S7DataType.DINT;
                } else {
                    count = 4;
                    dt = S7DataType.REAL;
                }
                break;
            case "L": // 64位 (8 bytes)
                ts = S7TransportSize.BYTE;
                dt = (explicitDt != null) ? explicitDt : S7DataType.LREAL;
                count = 8;
                break;
            default:
                ts = S7TransportSize.BYTE;
                dt = (explicitDt != null) ? explicitDt : S7DataType.REAL;
                count = 4;
                break;
        }

        return new S7Address(area, dbNum, byteOffset, bitOffset, ts, count, dt);
    }
}
