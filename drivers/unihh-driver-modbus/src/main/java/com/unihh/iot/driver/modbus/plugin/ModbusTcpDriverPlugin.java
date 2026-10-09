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

package com.unihh.iot.driver.modbus.plugin;

import com.unihh.iot.common.model.DeviceConfig;
import com.unihh.iot.common.model.PointData;
import com.unihh.iot.common.model.ThingModel;
import com.unihh.iot.driver.modbus.client.ModbusTcpClient;
import com.unihh.iot.driver.modbus.protocol.address.ModbusAddressParser;
import com.unihh.iot.driver.modbus.protocol.codec.ModbusRequestEncoder;
import com.unihh.iot.driver.modbus.protocol.codec.ModbusResponseDecoder;
import com.unihh.iot.driver.modbus.protocol.frame.ModbusDataType;
import com.unihh.iot.driver.modbus.protocol.frame.ModbusFunctionCode;
import com.unihh.iot.protocol.annotation.IoTDriver;
import com.unihh.iot.protocol.base.AbstractDriverPlugin;
import com.unihh.iot.protocol.model.DriverMetadata;
import com.unihh.iot.protocol.model.ParsedPayload;
import com.unihh.iot.protocol.util.IndustrialEndianUtils;
import com.unihh.iot.protocol.util.PointValueConverter;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Modbus TCP 工业驱动插件核心实现类
 * 继承 AbstractDriverPlugin 基座，专注 Modbus TCP 报文解析、寄存器批量读取与反控写入
 */
@Component
@IoTDriver(code = "modbus-tcp", name = "Modbus TCP 工业驱动插件", version = "2.0.0")
public class ModbusTcpDriverPlugin extends AbstractDriverPlugin {

    @Override
    public String getDriverCode() {
        return "modbus-tcp";
    }

    @Override
    public DriverMetadata getMetadata() {
        DriverMetadata metadata = new DriverMetadata("modbus-tcp", "Modbus TCP 工业驱动插件");
        metadata.getParams().add(new DriverMetadata.ParamSchema("host", "IP地址", "STRING", true, "192.168.1.100"));
        metadata.getParams().add(new DriverMetadata.ParamSchema("port", "端口号", "INT", true, "502"));
        metadata.getParams().add(new DriverMetadata.ParamSchema("slaveId", "从站 ID", "INT", true, "1"));

        DriverMetadata.ParamSchema byteOrderSchema = new DriverMetadata.ParamSchema(
                "byteOrder", "寄存器字节序", "SELECT", false, "CDAB",
                "32位/64位整型与浮点数在Modbus寄存器中的存储排列顺序 (推荐 CDAB)");
        byteOrderSchema.getOptions().add(new DriverMetadata.ParamOption("CDAB (工业推荐 / 32位小端字大端字节)", "CDAB"));
        byteOrderSchema.getOptions().add(new DriverMetadata.ParamOption("ABCD (标准大端模式 / Big-Endian)", "ABCD"));
        byteOrderSchema.getOptions().add(new DriverMetadata.ParamOption("DCBA (完全小端模式 / Little-Endian)", "DCBA"));
        byteOrderSchema.getOptions().add(new DriverMetadata.ParamOption("BADC (字节交换模式 / Byte Swap)", "BADC"));
        byteOrderSchema.getOptions().add(new DriverMetadata.ParamOption("AB (16位标准大端)", "AB"));
        byteOrderSchema.getOptions().add(new DriverMetadata.ParamOption("BA (16位字节交换)", "BA"));
        byteOrderSchema.getOptions().add(new DriverMetadata.ParamOption("ABCDEFGH (64位标准大端)", "ABCDEFGH"));
        byteOrderSchema.getOptions().add(new DriverMetadata.ParamOption("GHEFCDAB (64位双字交换)", "GHEFCDAB"));
        byteOrderSchema.getOptions().add(new DriverMetadata.ParamOption("HGFEDCBA (64位完全小端)", "HGFEDCBA"));
        byteOrderSchema.getOptions().add(new DriverMetadata.ParamOption("BADCFEHG (64位字节交换)", "BADCFEHG"));
        metadata.getParams().add(byteOrderSchema);

        DriverMetadata.AddressRule addressRule = new DriverMetadata.AddressRule("MODBUS", "例: 40001 (保持寄存器) 或 00001 (线圈)");
        addressRule.setStartOffset(40001);
        addressRule.setCoilStartOffset(1);
        Map<String, Integer> steps = new HashMap<>();
        steps.put("INT", 1);
        steps.put("ENUM", 1);
        steps.put("INT32", 2);
        steps.put("FLOAT", 2);
        steps.put("DOUBLE", 4);
        steps.put("INT64", 4);
        steps.put("BOOL", 1);
        addressRule.setTypeSteps(steps);
        metadata.setAddressRule(addressRule);

        return metadata;
    }

    @Override
    public boolean openConnection(DeviceConfig config) throws Exception {
        if (config == null) return false;
        String host = (config.getHost() != null && !config.getHost().trim().isEmpty()) ? config.getHost() : "127.0.0.1";
        int port = (config.getPort() > 0) ? config.getPort() : 502;
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 1000);
            return socket.isConnected();
        } catch (Exception e) {
            log.warn("[ModbusTcp] Connection test failed [{}:{}]: {}", host, port, e.getMessage());
            return false;
        }
    }

    @Override
    public String suggestAddress(int pointIndex, String pointCode, DeviceConfig config) {
        return String.valueOf(40001 + (pointIndex * 2));
    }

    @Override
    public ParsedPayload readPoints(DeviceConfig config, List<ThingModel.PropertySpec> points) throws Exception {
        ParsedPayload payload = new ParsedPayload();
        payload.setDeviceId(config.getDeviceId());
        payload.setModelKey(config.getModelKey());

        if (points == null || points.isEmpty()) {
            return payload;
        }

        String host = (config.getHost() != null && !config.getHost().trim().isEmpty()) ? config.getHost() : "127.0.0.1";
        int port = (config.getPort() > 0) ? config.getPort() : 502;
        int slaveId = parseSlaveId(config.getConnectionParams());

        // 解析功能码与首个寄存器地址
        String firstAddr = points.get(0).getAddress();
        ModbusFunctionCode fc = ModbusAddressParser.inferFunctionCode(firstAddr);

        int minRegAddr = Integer.MAX_VALUE;
        int maxRegAddr = 0;
        for (ThingModel.PropertySpec p : points) {
            int addr = ModbusAddressParser.parseRegisterAddress(p.getAddress());
            ModbusDataType dt = ModbusDataType.fromString(p.getDataType());
            int regs = Math.max(1, dt.getByteLength() / 2);
            if (addr < minRegAddr) {
                minRegAddr = addr;
            }
            if (addr + regs > maxRegAddr) {
                maxRegAddr = addr + regs;
            }
        }
        int startRegAddr = (minRegAddr == Integer.MAX_VALUE) ? 0 : minRegAddr;
        int quantity = Math.max(1, maxRegAddr - startRegAddr);

        byte[] requestFrame = ModbusRequestEncoder.encodeReadRequest(1, slaveId, fc, startRegAddr, quantity);
        ModbusTcpClient client = new ModbusTcpClient(host, port);

        try {
            byte[] responseBytes = client.executeFrame(requestFrame);
            return ModbusResponseDecoder.decode(responseBytes, config, points);
        } catch (Exception e) {
            log.warn("Modbus Socket 采集通信失败 [{}:{}]: {}", host, port, e.getMessage());
            long now = System.currentTimeMillis();
            for (ThingModel.PropertySpec spec : points) {
                PointData pd = new PointData(config.getModelKey(), config.getDeviceId(), spec.getPointCode(), 0.0, now);
                pd.setQuality(0);
                pd.setRawValue("COMM_FAILURE: " + e.getMessage());
                payload.getPoints().add(pd);
            }
            return payload;
        }
    }

    @Override
    public boolean invokeService(DeviceConfig config, String serviceCode, Map<String, Object> params) throws Exception {
        log.info("[modbus-tcp] Executing service [{}] on device [{}] with params: {}",
                serviceCode, config != null ? config.getDeviceId() : null, params);

        if (params == null || params.isEmpty()) {
            // 如果无参数，且 serviceCode 为纯数字物理地址，可作为点位触发；否则拒绝盲目触发
            if (serviceCode != null && (serviceCode.matches("\\d+") || serviceCode.contains("."))) {
                try {
                    return writePoint(config, serviceCode, serviceCode, 1, "BOOL");
                } catch (Exception e) {
                    log.error("[modbus-tcp] Failed to execute trigger service [{}]: {}", serviceCode, e.getMessage());
                    return false;
                }
            }
            log.warn("[modbus-tcp] 服务 [{}] 入参为空且未指定物理触发地址，拒绝执行", serviceCode);
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
                            log.error("[modbus-tcp] Failed to write resolved point [{}] (addr={}): {}", pCode, pAddr, e.getMessage());
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
                log.error("[modbus-tcp] Failed to write point with explicit address [{}]: {}", addr, e.getMessage());
                return false;
            }
        }

        // 3. 严格安全约束：若既无 resolvedPoints 又无明确底层物理地址，严禁盲目猜测寄存器写入，杜绝安全事故
        log.error("[modbus-tcp] 拒绝执行服务 [{}]: 参数中未包含已翻译的物理点位地址 (缺少 resolvedPoints 或 address)，工业安全规范严禁兜底猜测下发!",
                serviceCode);
        return false;
    }

    @Override
    public boolean writePoint(DeviceConfig config, String pointCode, String address, Object value, String dataType) throws Exception {
        String host = (config != null && config.getHost() != null && !config.getHost().trim().isEmpty()) ? config.getHost() : "127.0.0.1";
        int port = (config != null && config.getPort() > 0) ? config.getPort() : 502;
        int slaveId = parseSlaveId(config != null ? config.getConnectionParams() : null);

        String addressStr = address;
        if (addressStr == null || addressStr.trim().isEmpty()) {
            if (pointCode != null && (pointCode.matches("\\d+") || pointCode.contains("."))) {
                addressStr = pointCode;
            } else {
                log.error("[modbus-tcp] 严禁下发: 设备 [{}] 测点 [{}] 未指定物理寄存器地址，工业规范严禁盲目猜测写入!",
                        config != null ? config.getDeviceId() : "null", pointCode);
                return false;
            }
        }

        if (dataType == null || dataType.trim().isEmpty()) {
            if (value instanceof Boolean) {
                dataType = "BOOL";
            } else if (value instanceof Float || value instanceof Double) {
                dataType = "FLOAT";
            } else {
                dataType = "INT16";
            }
        }

        int regAddr = ModbusAddressParser.parseRegisterAddress(addressStr);
        double valDouble = PointValueConverter.parseToDouble(value, 0.0);
        ModbusDataType mdt = ModbusDataType.fromString(dataType);

        String byteOrder = "CDAB";
        if (config != null && config.getConnectionParams() != null && config.getConnectionParams().get("byteOrder") != null) {
            byteOrder = config.getConnectionParams().get("byteOrder").toString().trim().toUpperCase();
        }

        // 地址安全检查 (3xxxx 为只读输入寄存器 FC04)
        if (addressStr.trim().startsWith("3")) {
            log.warn("Modbus 目标地址 [{}] 为只读输入寄存器(FC04), 不支持写入", addressStr);
            return false;
        }

        boolean isCoil = addressStr.trim().startsWith("0") || mdt == ModbusDataType.BOOL;
        ModbusFunctionCode fc;
        if (isCoil) {
            fc = ModbusFunctionCode.WRITE_SINGLE_COIL;
        } else if (mdt.getByteLength() > 2) {
            fc = ModbusFunctionCode.WRITE_MULTIPLE_REGISTERS;
        } else {
            fc = ModbusFunctionCode.WRITE_SINGLE_REGISTER;
        }

        byte[] writeFrame;
        if (fc == ModbusFunctionCode.WRITE_SINGLE_COIL) {
            boolean coilVal = (valDouble > 0 || "true".equalsIgnoreCase(String.valueOf(value)));
            writeFrame = ModbusRequestEncoder.encodeWriteSingleCoil(1, slaveId, regAddr, coilVal);
        } else if (fc == ModbusFunctionCode.WRITE_MULTIPLE_REGISTERS) {
            int[] regValues;
            if (mdt.getByteLength() == 8) {
                // 64 位双精度浮点数 (DOUBLE / FLOAT64) 或 64 位大整型 (INT64 / UINT64)
                if (mdt == ModbusDataType.DOUBLE) {
                    regValues = IndustrialEndianUtils.toRegistersFloat64(valDouble, byteOrder);
                } else {
                    long longVal = (value instanceof Number) ? ((Number) value).longValue() : Math.round(valDouble);
                    regValues = IndustrialEndianUtils.toRegisters64(longVal, byteOrder);
                }
            } else {
                // 32 位单精度浮点数 (FLOAT / FLOAT32) 或 32 位整型 (INT32 / UINT32)
                if (mdt == ModbusDataType.FLOAT) {
                    regValues = IndustrialEndianUtils.toRegistersFloat32((float) valDouble, byteOrder);
                } else {
                    long longVal = (value instanceof Number) ? ((Number) value).longValue() : Math.round(valDouble);
                    regValues = IndustrialEndianUtils.toRegisters32((int) longVal, byteOrder);
                }
            }
            writeFrame = ModbusRequestEncoder.encodeWriteMultipleRegisters(1, slaveId, regAddr, regValues);
        } else {
            // 16 位整型 (INT16 / UINT16 / ENUM 等)
            int rawInt = (int) Math.round(valDouble);
            int regVal = IndustrialEndianUtils.toRegister16(rawInt, byteOrder);
            writeFrame = ModbusRequestEncoder.encodeWriteSingleRegister(1, slaveId, regAddr, regVal);
        }

        ModbusTcpClient client = new ModbusTcpClient(host, port);
        try {
            byte[] responseBytes = client.executeFrame(writeFrame);
            if (responseBytes != null && responseBytes.length >= 8 && ((responseBytes[7] & 0x80) != 0)) {
                log.warn("Modbus 从站拒绝写入 [{}:{}] addr={}", host, port, addressStr);
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("Modbus 写入异常 [{}:{}]: {}", host, port, e.getMessage());
            return false;
        }
    }

    private int parseSlaveId(Map<String, Object> params) {
        if (params != null && params.get("slaveId") != null) {
            try { return Integer.parseInt(params.get("slaveId").toString()); } catch (Exception ignored) {}
        }
        return 1;
    }
}
