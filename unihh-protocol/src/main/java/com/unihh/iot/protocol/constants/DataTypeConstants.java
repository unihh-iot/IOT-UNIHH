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
 * 工业物联网物模型与南向驱动数据类型标准常量接口
 */
public interface DataTypeConstants {

    // 16 位整型
    String INT16 = "INT16";
    String UINT16 = "UINT16";
    String SHORT = "SHORT";

    // 32 位整型
    String INT32 = "INT32";
    String UINT32 = "UINT32";
    String INT = "INT";
    String INTEGER = "INTEGER";

    // 64 位整型
    String INT64 = "INT64";
    String UINT64 = "UINT64";
    String LONG = "LONG";

    // 浮点型
    String FLOAT = "FLOAT";
    String FLOAT32 = "FLOAT32";
    String DOUBLE = "DOUBLE";
    String FLOAT64 = "FLOAT64";

    // 布尔/线圈状态
    String BOOLEAN = "BOOLEAN";
    String BOOL = "BOOL";
    String BIT = "BIT";

    // 文本/字符串
    String STRING = "STRING";
    String TEXT = "TEXT";

    // 二进制与特殊工业编码
    String BYTE = "BYTE";
    String BYTES = "BYTES";
    String BCD = "BCD";
    String HEX = "HEX";

    /**
     * 判断是否属于浮点类型
     */
    static boolean isFloatingPoint(String dataType) {
        if (dataType == null) return false;
        String upper = dataType.trim().toUpperCase();
        return FLOAT.equals(upper) || FLOAT32.equals(upper) || DOUBLE.equals(upper) || FLOAT64.equals(upper);
    }

    /**
     * 判断是否属于整型类型
     */
    static boolean isIntegerType(String dataType) {
        if (dataType == null) return false;
        String upper = dataType.trim().toUpperCase();
        return INT16.equals(upper) || UINT16.equals(upper) || SHORT.equals(upper)
                || INT32.equals(upper) || UINT32.equals(upper) || INT.equals(upper) || INTEGER.equals(upper)
                || INT64.equals(upper) || UINT64.equals(upper) || LONG.equals(upper);
    }

    /**
     * 判断是否属于布尔类型
     */
    static boolean isBooleanType(String dataType) {
        if (dataType == null) return false;
        String upper = dataType.trim().toUpperCase();
        return BOOLEAN.equals(upper) || BOOL.equals(upper) || BIT.equals(upper);
    }
}
