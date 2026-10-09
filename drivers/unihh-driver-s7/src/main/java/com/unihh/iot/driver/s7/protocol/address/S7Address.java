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

/**
 * 西门子 S7 结构化解析地址模型
 */
public class S7Address {
    private final S7Area area;
    private final int dbNumber;
    private final int byteOffset;
    private final int bitOffset;
    private final S7TransportSize transportSize;
    private final int count;
    private final S7DataType dataType;

    public S7Address(S7Area area, int dbNumber, int byteOffset, int bitOffset,
                     S7TransportSize transportSize, int count, S7DataType dataType) {
        this.area = area;
        this.dbNumber = dbNumber;
        this.byteOffset = byteOffset;
        this.bitOffset = bitOffset;
        this.transportSize = transportSize;
        this.count = count > 0 ? count : 1;
        this.dataType = dataType;
    }

    public S7Area getArea() {
        return area;
    }

    public int getDbNumber() {
        return dbNumber;
    }

    public int getByteOffset() {
        return byteOffset;
    }

    public int getBitOffset() {
        return bitOffset;
    }

    public S7TransportSize getTransportSize() {
        return transportSize;
    }

    public int getCount() {
        return count;
    }

    public S7DataType getDataType() {
        return dataType;
    }

    /**
     * 计算 S7 PDU 寻址所需的 3-byte 偏移值: (byteOffset * 8) + bitOffset
     */
    public int getBitAddress() {
        return (byteOffset * 8) + bitOffset;
    }

    @Override
    public String toString() {
        if (area == S7Area.DATA_BLOCKS) {
            return String.format("DB%d.DB%s%d.%d", dbNumber, transportSize, byteOffset, bitOffset);
        }
        return String.format("%s%d.%d", area.getPrefix(), byteOffset, bitOffset);
    }
}
