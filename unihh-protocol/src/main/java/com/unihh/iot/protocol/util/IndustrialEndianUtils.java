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

package com.unihh.iot.protocol.util;

import com.unihh.iot.protocol.constants.ByteOrderConstants;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * 工业级字节序变换与原始报文数据解析工具类
 * 覆盖所有常用 PLC（西门子、Modbus、欧姆龙、三菱）的 ABCD / CDAB / BADC / DCBA 字节排列
 */
public class IndustrialEndianUtils {

    /**
     * 读取 16 位有符号整数
     */
    public static short readInt16(byte[] data, int offset, String byteOrder) {
        byte[] b = reorder2Bytes(data, offset, byteOrder);
        return (short) (((b[0] & 0xFF) << 8) | (b[1] & 0xFF));
    }

    /**
     * 读取 16 位无符号整数
     */
    public static int readUInt16(byte[] data, int offset, String byteOrder) {
        return readInt16(data, offset, byteOrder) & 0xFFFF;
    }

    /**
     * 读取 32 位有符号整数
     */
    public static int readInt32(byte[] data, int offset, String byteOrder) {
        byte[] b = reorder4Bytes(data, offset, byteOrder);
        return ((b[0] & 0xFF) << 24)
                | ((b[1] & 0xFF) << 16)
                | ((b[2] & 0xFF) << 8)
                | (b[3] & 0xFF);
    }

    /**
     * 读取 32 位无符号整数
     */
    public static long readUInt32(byte[] data, int offset, String byteOrder) {
        return readInt32(data, offset, byteOrder) & 0xFFFFFFFFL;
    }

    /**
     * 读取 32 位单精度浮点数 (IEEE 754)
     */
    public static float readFloat32(byte[] data, int offset, String byteOrder) {
        int bits = readInt32(data, offset, byteOrder);
        return Float.intBitsToFloat(bits);
    }

    /**
     * 读取 64 位双精度浮点数 (IEEE 754)
     */
    public static double readFloat64(byte[] data, int offset, String byteOrder) {
        byte[] b = reorder8Bytes(data, offset, byteOrder);
        ByteBuffer buffer = ByteBuffer.wrap(b).order(ByteOrder.BIG_ENDIAN);
        return buffer.getDouble();
    }

    /**
     * 读取单个比特位 (Boolean)
     * @param bitIndex 0-7
     */
    public static boolean readBit(byte b, int bitIndex) {
        return ((b >> (bitIndex & 7)) & 1) == 1;
    }


    /**
     * 根据字节序将 16 位整数转为 2 字节数组
     */
    public static byte[] toBytes16(int value, String byteOrder) {
        int reg = toRegister16(value, byteOrder);
        return new byte[]{
                (byte) ((reg >> 8) & 0xFF),
                (byte) (reg & 0xFF)
        };
    }

    /**
     * 根据字节序将 32 位整数转为 4 字节数组
     */
    public static byte[] toBytes32(int value, String byteOrder) {
        int[] regs = toRegisters32(value, byteOrder);
        return new byte[]{
                (byte) ((regs[0] >> 8) & 0xFF),
                (byte) (regs[0] & 0xFF),
                (byte) ((regs[1] >> 8) & 0xFF),
                (byte) (regs[1] & 0xFF)
        };
    }

    /**
     * 根据字节序将 32 位浮点数转为 4 字节数组
     */
    public static byte[] toBytesFloat32(float value, String byteOrder) {
        return toBytes32(Float.floatToIntBits(value), byteOrder);
    }

    /**
     * 根据字节序将 64 位整数转为 8 字节数组
     */
    public static byte[] toBytes64(long value, String byteOrder) {
        int[] regs = toRegisters64(value, byteOrder);
        return new byte[]{
                (byte) ((regs[0] >> 8) & 0xFF),
                (byte) (regs[0] & 0xFF),
                (byte) ((regs[1] >> 8) & 0xFF),
                (byte) (regs[1] & 0xFF),
                (byte) ((regs[2] >> 8) & 0xFF),
                (byte) (regs[2] & 0xFF),
                (byte) ((regs[3] >> 8) & 0xFF),
                (byte) (regs[3] & 0xFF)
        };
    }

    /**
     * 根据字节序将 64 位双精度浮点数转为 8 字节数组
     */
    public static byte[] toBytesFloat64(double value, String byteOrder) {
        return toBytes64(Double.doubleToLongBits(value), byteOrder);
    }

    // -------------------------------------------------------------
    // Modbus 16位保持寄存器级重排 (供驱动写入 FC06 / FC16)
    // -------------------------------------------------------------

    /**
     * 针对 16 位整型按字节序编码单个 Modbus 寄存器
     */
    public static int toRegister16(int value, String byteOrder) {
        if (isByteSwap16(byteOrder)) {
            return Short.reverseBytes((short) value) & 0xFFFF;
        }
        return value & 0xFFFF;
    }

    /**
     * 针对 32 位整型 (2个 Modbus 寄存器) 按字节序重排
     */
    public static int[] toRegisters32(int bits, String byteOrder) {
        int w0 = (bits >>> 16) & 0xFFFF; // AB
        int w1 = bits & 0xFFFF;          // CD
        String order = normalizeByteOrder(byteOrder);
        switch (order) {
            case ByteOrderConstants.CDAB: // 工业 Modbus 最常用小端字大端字节
                return new int[]{w1, w0};
            case ByteOrderConstants.DCBA: // 完全小端模式 (字逆序且字节交换)
                return new int[]{
                        Short.reverseBytes((short) w1) & 0xFFFF,
                        Short.reverseBytes((short) w0) & 0xFFFF
                };
            case ByteOrderConstants.BADC: // 大端字小端字节 (字节交换)
                return new int[]{
                        Short.reverseBytes((short) w0) & 0xFFFF,
                        Short.reverseBytes((short) w1) & 0xFFFF
                };
            case ByteOrderConstants.ABCD: // 标准大端
            default:
                return new int[]{w0, w1};
        }
    }

    /**
     * 针对 32 位浮点数 (2个 Modbus 寄存器) 按字节序重排
     */
    public static int[] toRegistersFloat32(float value, String byteOrder) {
        return toRegisters32(Float.floatToIntBits(value), byteOrder);
    }

    /**
     * 针对 64 位长整型 (4个 Modbus 寄存器) 按字节序重排
     */
    public static int[] toRegisters64(long bits, String byteOrder) {
        int w0 = (int) ((bits >>> 48) & 0xFFFF); // AB
        int w1 = (int) ((bits >>> 32) & 0xFFFF); // CD
        int w2 = (int) ((bits >>> 16) & 0xFFFF); // EF
        int w3 = (int) (bits & 0xFFFF);          // GH
        String order = normalizeByteOrder(byteOrder);
        switch (order) {
            case ByteOrderConstants.CDAB: // 64位双字交换 / 4字逆序 (GHEFCDAB)
                return new int[]{w3, w2, w1, w0};
            case ByteOrderConstants.DCBA: // 64位完全小端 (HGFEDCBA)
                return new int[]{
                        Short.reverseBytes((short) w3) & 0xFFFF,
                        Short.reverseBytes((short) w2) & 0xFFFF,
                        Short.reverseBytes((short) w1) & 0xFFFF,
                        Short.reverseBytes((short) w0) & 0xFFFF
                };
            case ByteOrderConstants.BADC: // 64位字节交换 (BADCFEHG)
                return new int[]{
                        Short.reverseBytes((short) w0) & 0xFFFF,
                        Short.reverseBytes((short) w1) & 0xFFFF,
                        Short.reverseBytes((short) w2) & 0xFFFF,
                        Short.reverseBytes((short) w3) & 0xFFFF
                };
            case ByteOrderConstants.ABCD: // 64位标准大端
            default:
                return new int[]{w0, w1, w2, w3};
        }
    }

    /**
     * 针对 64 位双精度浮点数 (4个 Modbus 寄存器) 按字节序重排
     */
    public static int[] toRegistersFloat64(double value, String byteOrder) {
        return toRegisters64(Double.doubleToLongBits(value), byteOrder);
    }

    /**
     * 规格化字节序枚举字符串，兼容工业各品牌常见别名
     */
    public static String normalizeByteOrder(String byteOrder) {
        if (byteOrder == null) return ByteOrderConstants.ABCD;
        String bo = byteOrder.trim().toUpperCase();
        switch (bo) {
            case "CDAB":
            case "GHEFCDAB":
            case "SWAP_WORDS":
            case "MID_LITTLE":
                return ByteOrderConstants.CDAB;
            case "DCBA":
            case "HGFEDCBA":
            case "LITTLE_ENDIAN":
                return ByteOrderConstants.DCBA;
            case "BADC":
            case "BADCFEHG":
            case "BYTE_SWAP":
                return ByteOrderConstants.BADC;
            case "ABCD":
            case "ABCDEFGH":
            case "BIG_ENDIAN":
            default:
                return ByteOrderConstants.ABCD;
        }
    }

    /**
     * 判定 16 位寄存器是否需要执行字节翻转 (BA)
     */
    public static boolean isByteSwap16(String byteOrder) {
        if (byteOrder == null) return false;
        String bo = byteOrder.trim().toUpperCase();
        return "BA".equals(bo) || "BADC".equals(bo) || "DCBA".equals(bo)
                || "LITTLE_ENDIAN".equals(bo) || "BADCFEHG".equals(bo)
                || "HGFEDCBA".equals(bo) || "BYTE_SWAP".equals(bo);
    }

    // -------------------------------------------------------------
    // 内部字节重排辅助方法 (规范为标准大端序 ABCD)
    // -------------------------------------------------------------

    private static byte[] reorder2Bytes(byte[] data, int offset, String byteOrder) {
        if (data == null || offset + 1 >= data.length) return new byte[2];
        byte b0 = data[offset];
        byte b1 = data[offset + 1];
        if (isByteSwap16(byteOrder)) {
            return new byte[]{b1, b0};
        }
        return new byte[]{b0, b1};
    }

    private static byte[] reorder4Bytes(byte[] data, int offset, String byteOrder) {
        if (data == null || offset + 3 >= data.length) return new byte[4];
        byte b0 = data[offset];
        byte b1 = data[offset + 1];
        byte b2 = data[offset + 2];
        byte b3 = data[offset + 3];

        String order = normalizeByteOrder(byteOrder);
        switch (order) {
            case ByteOrderConstants.CDAB: // 工业常用小端字大端字节 (例如 Modbus)
                return new byte[]{b2, b3, b0, b1};
            case ByteOrderConstants.BADC: // 大端字小端字节 (字节交换)
                return new byte[]{b1, b0, b3, b2};
            case ByteOrderConstants.DCBA: // 完全小端 (Little-Endian)
                return new byte[]{b3, b2, b1, b0};
            case ByteOrderConstants.ABCD: // 标准大端
            default:
                return new byte[]{b0, b1, b2, b3};
        }
    }

    private static byte[] reorder8Bytes(byte[] data, int offset, String byteOrder) {
        if (data == null || offset + 7 >= data.length) return new byte[8];
        byte[] raw = new byte[8];
        System.arraycopy(data, offset, raw, 0, 8);
        String order = normalizeByteOrder(byteOrder);
        switch (order) {
            case ByteOrderConstants.CDAB: // 64位双字交换 / 4字逆序
                return new byte[]{raw[6], raw[7], raw[4], raw[5], raw[2], raw[3], raw[0], raw[1]};
            case ByteOrderConstants.DCBA: // 64位完全小端
                return new byte[]{raw[7], raw[6], raw[5], raw[4], raw[3], raw[2], raw[1], raw[0]};
            case ByteOrderConstants.BADC: // 64位字节交换
                return new byte[]{raw[1], raw[0], raw[3], raw[2], raw[5], raw[4], raw[7], raw[6]};
            case ByteOrderConstants.ABCD: // 64位标准大端
            default:
                return raw;
        }
    }
}
