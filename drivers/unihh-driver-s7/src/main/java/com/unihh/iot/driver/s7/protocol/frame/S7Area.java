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

package com.unihh.iot.driver.s7.protocol.frame;

/**
 * 西门子 S7 PLC 存储区常量定义
 */
public enum S7Area {
    /** 系统信息 (System Info) */
    SYS_INFO((byte) 0x03, "SYS"),

    /** 系统标志 (System Flags) */
    SYS_FLAGS((byte) 0x05, "FLAGS"),

    /** 模拟量输入 (Analog Inputs) */
    ANA_INPUT((byte) 0x06, "AI"),

    /** 模拟量输出 (Analog Outputs) */
    ANA_OUTPUT((byte) 0x07, "AQ"),

    /** 数字量输入区 (Process Inputs / I 区) */
    INPUTS((byte) 0x81, "I"),

    /** 数字量输出区 (Process Outputs / Q 区) */
    OUTPUTS((byte) 0x82, "Q"),

    /** 内部标志位区 (Flags / Merker / M 区) */
    FLAGS((byte) 0x83, "M"),

    /** 数据块存储区 (Data Blocks / DB 区 / S7-200 V区) */
    DATA_BLOCKS((byte) 0x84, "DB"),

    /** 背景数据块 (Instance Data Blocks / DI 区) */
    INSTANCE_DATA_BLOCKS((byte) 0x85, "DI"),

    /** 定时器 (Timers / T 区) */
    TIMERS((byte) 0x1D, "T"),

    /** 计数器 (Counters / C 区) */
    COUNTERS((byte) 0x1C, "C");

    private final byte code;
    private final String prefix;

    S7Area(byte code, String prefix) {
        this.code = code;
        this.prefix = prefix;
    }

    public byte getCode() {
        return code;
    }

    public String getPrefix() {
        return prefix;
    }

    public static S7Area fromPrefix(String prefix) {
        if (prefix == null) return DATA_BLOCKS;
        String p = prefix.trim().toUpperCase();
        for (S7Area area : values()) {
            if (area.prefix.equalsIgnoreCase(p)) {
                return area;
            }
        }
        if ("V".equalsIgnoreCase(p)) {
            return DATA_BLOCKS; // 西门子 S7-200 / SMART V 区映射至 DB1
        }
        return DATA_BLOCKS;
    }
}
