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

import com.unihh.iot.protocol.constants.DataTypeConstants;

/**
 * 测点工程值缩放换算与数据类型智能适配工具类
 */
public class PointValueConverter {

    /**
     * 应用物模型比例系数与偏移量计算工程值: result = rawValue * scale + offset
     */
    public static double applyScaleAndOffset(double rawValue, Double scale, Double offset) {
        double s = (scale != null && scale != 0.0) ? scale : 1.0;
        double o = (offset != null) ? offset : 0.0;
        double res = rawValue * s + o;
        return Math.round(res * 10000.0) / 10000.0;
    }

    /**
     * 逆运算: 从工程值还原原始设定值: rawValue = (engValue - offset) / scale
     */
    public static double reverseScaleAndOffset(double engValue, Double scale, Double offset) {
        double s = (scale != null && scale != 0.0) ? scale : 1.0;
        double o = (offset != null) ? offset : 0.0;
        return (engValue - o) / s;
    }

    /**
     * 将 Object 转换为 double 数值
     */
    public static double parseToDouble(Object value, double defaultValue) {
        if (value == null) return defaultValue;
        if (value instanceof Number) return ((Number) value).doubleValue();
        if (value instanceof Boolean) return ((Boolean) value) ? 1.0 : 0.0;
        try {
            return Double.parseDouble(value.toString().trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * 根据目标物模型类型转换值
     */
    public static Object convertToTargetType(Object val, String targetType) {
        if (val == null) return null;
        if (targetType == null) return val;

        if (DataTypeConstants.isBooleanType(targetType)) {
            if (val instanceof Boolean) return val;
            double num = parseToDouble(val, 0.0);
            return num > 0 || "true".equalsIgnoreCase(val.toString());
        }

        if (DataTypeConstants.isIntegerType(targetType)) {
            double num = parseToDouble(val, 0.0);
            return (long) Math.round(num);
        }

        if (DataTypeConstants.isFloatingPoint(targetType)) {
            return parseToDouble(val, 0.0);
        }

        return val.toString();
    }
}
