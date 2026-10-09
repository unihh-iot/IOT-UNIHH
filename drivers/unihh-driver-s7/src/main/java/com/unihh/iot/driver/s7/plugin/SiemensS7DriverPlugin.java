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

package com.unihh.iot.driver.s7.plugin;

import com.unihh.iot.common.model.DeviceConfig;
import com.unihh.iot.common.model.PointData;
import com.unihh.iot.common.model.ThingModel;
import com.unihh.iot.driver.s7.client.S7TcpClient;
import com.unihh.iot.driver.s7.protocol.address.S7Address;
import com.unihh.iot.driver.s7.protocol.address.S7AddressParser;
import com.unihh.iot.driver.s7.protocol.codec.S7RequestEncoder;
import com.unihh.iot.driver.s7.protocol.codec.S7ResponseDecoder;
import com.unihh.iot.driver.s7.protocol.frame.S7DataType;
import com.unihh.iot.driver.s7.protocol.frame.S7TransportSize;
import com.unihh.iot.protocol.annotation.IoTDriver;
import com.unihh.iot.protocol.base.AbstractDriverPlugin;
import com.unihh.iot.protocol.model.DriverMetadata;
import com.unihh.iot.protocol.model.ParsedPayload;
import com.unihh.iot.protocol.util.IndustrialEndianUtils;
import com.unihh.iot.protocol.util.PointValueConverter;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 西门子 S7 工业驱动插件核心实现类 (S7-200/300/400/1200/1500)
 * <p>
 * 继承 AbstractDriverPlugin 基座，支持 ISO-on-TCP (RFC 1006 / COTP ISO 8073) 与 S7Comm PDU 协议栈：
 * 1. 自动 COTP CR/CC 连接握手与 S7 Setup Communication 参数协商；
 * 2. 多测点自动切片批量读取 (Chunked Read Var 0x04)；
 * 3. 全数据类型安全写入反控 (Write Var 0x05, 含位/字/双字/浮点/字符串及大小端转换)；
 * 4. 联动规则引擎南向点位服务调用 (invokeService / resolvedPoints)；
 * 5. 通道物理连通性与握手协商诊断 (openConnection)。
 */
@Component
@IoTDriver(code = "s7-driver", name = "西门子 S7 纯正工业驱动插件", version = "2.0.0")
public class SiemensS7DriverPlugin extends AbstractDriverPlugin {

    private static final int MAX_READ_POINTS_PER_CHUNK = 18;

    @Override
    public String getDriverCode() {
        return "s7-driver";
    }

    @Override
    public DriverMetadata getMetadata() {
        DriverMetadata metadata = new DriverMetadata("s7-driver", "西门子 S7 纯正工业驱动插件");
        metadata.getParams().add(new DriverMetadata.ParamSchema("host", "PLC IP地址", "STRING", true, "192.168.1.200"));
        metadata.getParams().add(new DriverMetadata.ParamSchema("port", "端口号", "INT", true, "102"));
        metadata.getParams().add(new DriverMetadata.ParamSchema("rack", "机架号 (Rack)", "INT", true, "0"));
        metadata.getParams().add(new DriverMetadata.ParamSchema("slot", "插槽号 (Slot)", "INT", true, "1"));

        DriverMetadata.ParamSchema plcTypeSchema = new DriverMetadata.ParamSchema(
                "plcType", "PLC 型号", "SELECT", false, "S7-1200",
                "西门子 S7 系列控制器型号 (自动适配机架与插槽)");
        plcTypeSchema.getOptions().add(new DriverMetadata.ParamOption("S7-1200 (默认 Rack 0, Slot 1)", "S7-1200"));
        plcTypeSchema.getOptions().add(new DriverMetadata.ParamOption("S7-1500 (默认 Rack 0, Slot 1)", "S7-1500"));
        plcTypeSchema.getOptions().add(new DriverMetadata.ParamOption("S7-300 (默认 Rack 0, Slot 2)", "S7-300"));
        plcTypeSchema.getOptions().add(new DriverMetadata.ParamOption("S7-400 (默认 Rack 0, Slot 2)", "S7-400"));
        plcTypeSchema.getOptions().add(new DriverMetadata.ParamOption("S7-200 SMART (默认 Rack 0, Slot 1)", "S7-200-SMART"));
        metadata.getParams().add(plcTypeSchema);

        DriverMetadata.ParamSchema byteOrderSchema = new DriverMetadata.ParamSchema(
                "byteOrder", "寄存器字节序", "SELECT", false, "ABCD",
                "32位/64位整型与浮点数在S7数据块中的存储排列顺序 (西门子原生推荐 ABCD)");
        byteOrderSchema.getOptions().add(new DriverMetadata.ParamOption("ABCD (西门子原生标准大端模式 / Big-Endian)", "ABCD"));
        byteOrderSchema.getOptions().add(new DriverMetadata.ParamOption("CDAB (32位小端字大端字节)", "CDAB"));
        byteOrderSchema.getOptions().add(new DriverMetadata.ParamOption("DCBA (完全小端模式 / Little-Endian)", "DCBA"));
        byteOrderSchema.getOptions().add(new DriverMetadata.ParamOption("BADC (字节交换模式 / Byte Swap)", "BADC"));
        byteOrderSchema.getOptions().add(new DriverMetadata.ParamOption("AB (16位标准大端)", "AB"));
        byteOrderSchema.getOptions().add(new DriverMetadata.ParamOption("BA (16位字节交换)", "BA"));
        byteOrderSchema.getOptions().add(new DriverMetadata.ParamOption("ABCDEFGH (64位标准大端)", "ABCDEFGH"));
        byteOrderSchema.getOptions().add(new DriverMetadata.ParamOption("GHEFCDAB (64位双字交换)", "GHEFCDAB"));
        byteOrderSchema.getOptions().add(new DriverMetadata.ParamOption("HGFEDCBA (64位完全小端)", "HGFEDCBA"));
        byteOrderSchema.getOptions().add(new DriverMetadata.ParamOption("BADCFEHG (64位字节交换)", "BADCFEHG"));
        metadata.getParams().add(byteOrderSchema);

        DriverMetadata.AddressRule addressRule = new DriverMetadata.AddressRule("S7", "例: DB1.DBD0 (Float/DInt) 或 DB1.DBW2 (Int)");
        addressRule.setDefaultArea("DB1");
        addressRule.setStartOffset(0);
        Map<String, Integer> steps = new HashMap<>();
        steps.put("BOOL", 1);
        steps.put("INT", 2);
        steps.put("INT32", 4);
        steps.put("FLOAT", 4);
        steps.put("DOUBLE", 8);
        steps.put("INT64", 8);
        addressRule.setTypeSteps(steps);
        metadata.setAddressRule(addressRule);

        return metadata;
    }

    @Override
    public boolean openConnection(DeviceConfig config) throws Exception {
        if (config == null) return false;
        String host = (config.getHost() != null && !config.getHost().trim().isEmpty()) ? config.getHost() : "127.0.0.1";
        int port = (config.getPort() > 0) ? config.getPort() : 102;
        int rack = parseRack(config.getConnectionParams());
        int slot = parseSlot(config.getConnectionParams());

        S7TcpClient client = new S7TcpClient(host, port, rack, slot);
        return client.testConnection();
    }

    @Override
    public String suggestAddress(int pointIndex, String pointCode, DeviceConfig config) {
        return "DB1.DBD" + (pointIndex * 4);
    }

    @Override
    public ParsedPayload readPoints(DeviceConfig config, List<ThingModel.PropertySpec> points) throws Exception {
        ParsedPayload payload = new ParsedPayload();
        payload.setDeviceId(config != null ? config.getDeviceId() : null);
        payload.setModelKey(config != null ? config.getModelKey() : null);

        if (points == null || points.isEmpty()) {
            return payload;
        }

        String host = (config != null && config.getHost() != null && !config.getHost().trim().isEmpty())
                ? config.getHost() : "127.0.0.1";
        int port = (config != null && config.getPort() > 0) ? config.getPort() : 102;
        int rack = parseRack(config != null ? config.getConnectionParams() : null);
        int slot = parseSlot(config != null ? config.getConnectionParams() : null);

        S7TcpClient client = new S7TcpClient(host, port, rack, slot);

        try {
            // 对测点进行分片批量采集，防止单个 S7 PDU 溢出
            int totalSize = points.size();
            for (int i = 0; i < totalSize; i += MAX_READ_POINTS_PER_CHUNK) {
                int end = Math.min(i + MAX_READ_POINTS_PER_CHUNK, totalSize);
                List<ThingModel.PropertySpec> chunk = points.subList(i, end);

                List<S7Address> addressList = new ArrayList<>(chunk.size());
                for (ThingModel.PropertySpec spec : chunk) {
                    addressList.add(S7AddressParser.parse(spec.getAddress(), spec.getDataType()));
                }

                int pduRef = (i / MAX_READ_POINTS_PER_CHUNK) + 1;
                byte[] requestFrame = S7RequestEncoder.encodeReadRequest(pduRef, addressList);
                byte[] responseBytes = client.executeJob(requestFrame);

                ParsedPayload chunkPayload = S7ResponseDecoder.decodeReadResponse(responseBytes, config, chunk);
                if (chunkPayload != null && chunkPayload.getPoints() != null) {
                    payload.getPoints().addAll(chunkPayload.getPoints());
                }
            }
            return payload;
        } catch (Exception e) {
            log.warn("S7 PLC 通信采集失败 [{}:{}]: {}", host, port, e.getMessage());
            long now = System.currentTimeMillis();
            for (ThingModel.PropertySpec spec : points) {
                PointData pd = new PointData(
                        config != null ? config.getModelKey() : null,
                        config != null ? config.getDeviceId() : null,
                        spec.getPointCode(),
                        0.0,
                        now);
                pd.setQuality(0);
                pd.setRawValue("S7_COMM_FAILURE: " + e.getMessage());
                payload.getPoints().add(pd);
            }
            return payload;
        }
    }

    @Override
    public boolean invokeService(DeviceConfig config, String serviceCode, Map<String, Object> params) throws Exception {
        log.info("[s7-driver] Executing service [{}] on device [{}] with params: {}",
                serviceCode, config != null ? config.getDeviceId() : null, params);

        if (params == null || params.isEmpty()) {
            // 如果无参数，且 serviceCode 为合法的 S7 寄存器地址格式 (如 DB1.DBX0.0, M0.0, Q0.0)，可作为脉冲/触发执行
            if (serviceCode != null && (serviceCode.toUpperCase().startsWith("DB") || serviceCode.matches("^[MIQV].*"))) {
                try {
                    return writePoint(config, serviceCode, serviceCode, 1, "BOOL");
                } catch (Exception e) {
                    log.error("[s7-driver] Failed to execute trigger service [{}]: {}", serviceCode, e.getMessage());
                    return false;
                }
            }
            log.warn("[s7-driver] 服务 [{}] 入参为空且未指定有效物理触发地址，拒绝执行", serviceCode);
            return false;
        }

        // 1. 若 params 携带了南向已翻译解析的物理点位集合 (resolvedPoints)
        if (params.containsKey("resolvedPoints") && params.get("resolvedPoints") != null) {
            Object rList = params.get("resolvedPoints");
            if (rList instanceof List) {
                List<?> list = (List<?>) rList;
                boolean allSuccess = true;
                for (Object item : list) {
                    if (item instanceof Map) {
                        Map<?, ?> ptMap = (Map<?, ?>) item;
                        String pCode = ptMap.containsKey("pointCode") ? String.valueOf(ptMap.get("pointCode")) : null;
                        String pAddr = ptMap.containsKey("address") ? String.valueOf(ptMap.get("address")) : null;
                        Object pVal = ptMap.get("value");
                        String pDt = ptMap.containsKey("dataType") ? String.valueOf(ptMap.get("dataType")) : null;
                        String pBo = ptMap.containsKey("byteOrder") ? String.valueOf(ptMap.get("byteOrder")) : null;

                        if (config != null) {
                            if (config.getConnectionParams() == null) {
                                config.setConnectionParams(new HashMap<>());
                            }
                            if (pBo != null && !pBo.trim().isEmpty()) {
                                config.getConnectionParams().put("byteOrder", pBo.trim());
                            } else if (params.containsKey("byteOrder") && params.get("byteOrder") != null) {
                                config.getConnectionParams().put("byteOrder", params.get("byteOrder").toString().trim());
                            }
                        }

                        try {
                            boolean ok = writePoint(config, pCode, pAddr, pVal, pDt);
                            if (!ok) allSuccess = false;
                        } catch (Exception e) {
                            log.error("[s7-driver] Failed to write resolved point [{}] (addr={}): {}", pCode, pAddr, e.getMessage());
                            allSuccess = false;
                        }
                    }
                }
                return allSuccess;
            }
        }

        // 2. 若 params 直接包含 address 和 value (标准单点反控包装)
        if (params.containsKey("address") && params.containsKey("value")) {
            String addr = params.get("address").toString();
            Object val = params.get("value");
            String dt = params.containsKey("dataType") ? params.get("dataType").toString() : null;
            if (params.containsKey("byteOrder") && params.get("byteOrder") != null && config != null) {
                if (config.getConnectionParams() == null) {
                    config.setConnectionParams(new HashMap<>());
                }
                config.getConnectionParams().put("byteOrder", params.get("byteOrder").toString().trim());
            }
            try {
                return writePoint(config, serviceCode, addr, val, dt);
            } catch (Exception e) {
                log.error("[s7-driver] Failed to write point with explicit address [{}]: {}", addr, e.getMessage());
                return false;
            }
        }

        // 3. 严格安全约束：若既无 resolvedPoints 又无明确底层物理地址，严禁盲目猜测寄存器写入
        log.error("[s7-driver] 拒绝执行服务 [{}]: 参数中未包含已翻译的物理点位地址 (缺少 resolvedPoints 或 address)，工业安全规范严禁兜底猜测下发!",
                serviceCode);
        return false;
    }

    @Override
    public boolean writePoint(DeviceConfig config, String pointCode, String address, Object value, String dataType) throws Exception {
        String host = (config != null && config.getHost() != null && !config.getHost().trim().isEmpty())
                ? config.getHost() : "127.0.0.1";
        int port = (config != null && config.getPort() > 0) ? config.getPort() : 102;
        int rack = parseRack(config != null ? config.getConnectionParams() : null);
        int slot = parseSlot(config != null ? config.getConnectionParams() : null);
        String byteOrder = parseByteOrder(config != null ? config.getConnectionParams() : null);

        String addressStr = address;
        if (addressStr == null || addressStr.trim().isEmpty()) {
            if (pointCode != null && (pointCode.toUpperCase().startsWith("DB") || pointCode.matches("^[MIQV].*"))) {
                addressStr = pointCode;
            } else {
                log.error("[s7-driver] 严禁下发: 设备 [{}] 测点 [{}] 未指定西门子物理地址，工业规范严禁盲目猜测写入!",
                        config != null ? config.getDeviceId() : "null", pointCode);
                return false;
            }
        }

        S7Address s7Addr = S7AddressParser.parse(addressStr, dataType);
        byte[] dataBytes = encodeDataBytes(s7Addr, value, byteOrder);

        byte[] writeFrame = S7RequestEncoder.encodeWriteRequest(1, s7Addr, dataBytes);
        S7TcpClient client = new S7TcpClient(host, port, rack, slot);

        try {
            byte[] responseBytes = client.executeJob(writeFrame);
            boolean ok = S7ResponseDecoder.isWriteResponseSuccess(responseBytes);
            if (!ok) {
                log.warn("[s7-driver] S7 PLC 写入被拒绝或返回错误 [{}:{}] addr={}", host, port, addressStr);
            }
            return ok;
        } catch (Exception e) {
            log.error("[s7-driver] S7 写入异常 [{}:{}]: {}", host, port, e.getMessage());
            return false;
        }
    }

    /**
     * 将上层应用下发的值根据数据类型与字节序编码为 S7 二进制字节序列
     */
    private byte[] encodeDataBytes(S7Address s7Addr, Object value, String byteOrder) {
        S7DataType dt = s7Addr.getDataType();
        boolean isBit = (s7Addr.getTransportSize() == S7TransportSize.BIT);

        if (isBit || dt == S7DataType.BOOL) {
            boolean boolVal = "true".equalsIgnoreCase(String.valueOf(value))
                    || PointValueConverter.parseToDouble(value, 0.0) > 0;
            return new byte[]{(byte) (boolVal ? 0x01 : 0x00)};
        }

        double valDouble = PointValueConverter.parseToDouble(value, 0.0);

        // 1. 若为 16位字 (2字节): INT / WORD
        if (dt == S7DataType.INT || dt == S7DataType.WORD || s7Addr.getCount() == 2) {
            int intVal = (int) Math.round(valDouble);
            return IndustrialEndianUtils.toBytes16(intVal, byteOrder);
        }

        // 2. 若为 32位单精度浮点: REAL (4字节)
        if (dt == S7DataType.REAL || (s7Addr.getCount() == 4 && (dt == S7DataType.LREAL || dt == null))) {
            float floatVal = (float) valDouble;
            return IndustrialEndianUtils.toBytesFloat32(floatVal, byteOrder);
        }

        // 3. 若为 32位整型: DINT / DWORD (4字节)
        if (dt == S7DataType.DINT || dt == S7DataType.DWORD) {
            long longVal = (value instanceof Number) ? ((Number) value).longValue() : Math.round(valDouble);
            return IndustrialEndianUtils.toBytes32((int) longVal, byteOrder);
        }

        // 4. 若为 64位双精度浮点: LREAL (8字节)
        if (dt == S7DataType.LREAL || s7Addr.getCount() == 8) {
            return IndustrialEndianUtils.toBytesFloat64(valDouble, byteOrder);
        }

        // 5. 若为单字节: BYTE / CHAR (1字节)
        if (dt == S7DataType.BYTE || dt == S7DataType.CHAR || s7Addr.getCount() == 1) {
            int intVal = (int) Math.round(valDouble);
            return new byte[]{(byte) (intVal & 0xFF)};
        }

        switch (dt) {
            case LREAL:
                return IndustrialEndianUtils.toBytesFloat64(valDouble, byteOrder);
            case STRING: {
                String str = String.valueOf(value);
                byte[] asciiBytes = str.getBytes(StandardCharsets.US_ASCII);
                int maxLen = 254;
                int actLen = Math.min(asciiBytes.length, maxLen);
                byte[] strBuf = new byte[2 + actLen];
                strBuf[0] = (byte) maxLen;
                strBuf[1] = (byte) actLen;
                System.arraycopy(asciiBytes, 0, strBuf, 2, actLen);
                return strBuf;
            }
            default: {
                if (s7Addr.getTransportSize() == S7TransportSize.DWORD) {
                    long longVal = (value instanceof Number) ? ((Number) value).longValue() : Math.round(valDouble);
                    return IndustrialEndianUtils.toBytes32((int) longVal, byteOrder);
                } else if (s7Addr.getTransportSize() == S7TransportSize.WORD) {
                    int intVal = (int) Math.round(valDouble);
                    return IndustrialEndianUtils.toBytes16(intVal, byteOrder);
                } else {
                    float floatVal = (float) valDouble;
                    return IndustrialEndianUtils.toBytesFloat32(floatVal, byteOrder);
                }
            }
        }
    }

    private int parseRack(Map<String, Object> params) {
        if (params != null && params.get("rack") != null) {
            try {
                return Integer.parseInt(params.get("rack").toString().trim());
            } catch (Exception ignored) {}
        }
        return 0;
    }

    private int parseSlot(Map<String, Object> params) {
        if (params != null && params.get("slot") != null) {
            try {
                return Integer.parseInt(params.get("slot").toString().trim());
            } catch (Exception ignored) {}
        }
        if (params != null && params.get("plcType") != null) {
            String type = params.get("plcType").toString().trim().toUpperCase();
            if (type.contains("300") || type.contains("400")) {
                return 2;
            }
        }
        return 1;
    }

    private String parseByteOrder(Map<String, Object> params) {
        if (params != null && params.get("byteOrder") != null) {
            return params.get("byteOrder").toString().trim().toUpperCase();
        }
        return "ABCD";
    }
}
