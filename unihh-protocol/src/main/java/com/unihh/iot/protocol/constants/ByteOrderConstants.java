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

package com.unihh.iot.protocol.constants;

/**
 * 工业 PLC 与通讯总线字节序排列常量规范接口
 */
public interface ByteOrderConstants {

    /** 32 位大端模式 (Big-Endian) ABCD: 高字节高字在前 (标准网络序) */
    String ABCD = "ABCD";

    /** 32 位小端字大端字节 (Little-Endian Word Swap) CDAB: 工业 Modbus 最常用推荐 */
    String CDAB = "CDAB";

    /** 32 位大端字小端字节 (Big-Endian Byte Swap) BADC */
    String BADC = "BADC";

    /** 32 位完全小端模式 (Little-Endian) DCBA: 低字节低字在前 (x86 CPU 架构) */
    String DCBA = "DCBA";

    /** 16 位大端模式 AB: 高字节在前 */
    String AB = "AB";

    /** 16 位小端模式 BA: 低字节在前 (字节交换) */
    String BA = "BA";

    /** 64 位标准大端 ABCDEFGH */
    String ABCDEFGH = "ABCDEFGH";

    /** 64 位双字交换 GHEFCDAB */
    String GHEFCDAB = "GHEFCDAB";

    /** 64 位完全小端 HGFEDCBA */
    String HGFEDCBA = "HGFEDCBA";

    /** 64 位字节交换 BADCFEHG */
    String BADCFEHG = "BADCFEHG";
}
