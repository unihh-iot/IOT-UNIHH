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

package com.unihh.iot.admin.service.impl;

import com.unihh.iot.admin.dao.DeviceDao;
import com.unihh.iot.admin.dao.DevicePointMappingDao;
import com.unihh.iot.admin.dao.DriverInfoDao;
import com.unihh.iot.admin.dao.ThingModelDao;
import com.unihh.iot.admin.mqtt.AdminMqttPublisher;
import com.unihh.iot.admin.service.DeviceService;
import com.unihh.iot.common.model.DeviceConfig;
import com.unihh.iot.common.model.DevicePointMapping;
import com.unihh.iot.common.model.ThingModel;
import com.unihh.iot.protocol.api.ProtocolDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DeviceServiceImpl implements DeviceService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DeviceServiceImpl.class);

    @Autowired
    private DeviceDao deviceDao;

    @Autowired
    private ThingModelDao thingModelDao;

    @Autowired
    private DevicePointMappingDao devicePointMappingDao;

    @Autowired
    private AdminMqttPublisher adminMqttPublisher;

    @Autowired(required = false)
    private com.unihh.iot.admin.dao.LogDao logDao;

    @Autowired(required = false)
    private DriverInfoDao driverInfoDao;

    private Map<String, ProtocolDriver> driverPluginCache;

    private synchronized ProtocolDriver getProtocolDriver(String driverCode) {
        if (driverCode == null) return null;
        if (driverPluginCache == null) {
            driverPluginCache = new HashMap<>();
            ServiceLoader<ProtocolDriver> loader = ServiceLoader.load(ProtocolDriver.class);
            for (ProtocolDriver driver : loader) {
                driverPluginCache.put(driver.getDriverCode(), driver);
            }
        }
        return driverPluginCache.get(driverCode);
    }

    @Override
    public List<DeviceConfig> getDeviceList(String keyword, String driverCode, Integer acqStatus) {
        return deviceDao.findAll(keyword, driverCode, acqStatus);
    }

    @Override
    public DeviceConfig getDeviceById(String deviceId) {
        return deviceDao.findById(deviceId);
    }

    @Override
    public boolean addDevice(DeviceConfig device) {
        if (device != null && (device.getDeviceId() == null || device.getDeviceId().trim().isEmpty())) {
            device.setDeviceId("DEV_" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        boolean success = deviceDao.insert(device) > 0;
        if (success && device != null) {
            List<DevicePointMapping> mappings = getPointAddressMappings(device.getDeviceId());
            adminMqttPublisher.publishDeviceConfigUpdate("UPDATE", device, convertMappingsToPropertySpecs(mappings));
        }
        return success;
    }

    @Override
    public boolean updateDevice(DeviceConfig device) {
        boolean success = deviceDao.update(device) > 0;
        if (success && device != null) {
            DeviceConfig updatedDev = deviceDao.findById(device.getDeviceId());
            List<DevicePointMapping> mappings = getPointAddressMappings(device.getDeviceId());
            adminMqttPublisher.publishDeviceConfigUpdate("UPDATE", updatedDev != null ? updatedDev : device, convertMappingsToPropertySpecs(mappings));
        }
        return success;
    }

    @Override
    public boolean updateAcqStatus(String deviceId, int acqStatus) {
        boolean success = deviceDao.updateAcqStatus(deviceId, acqStatus) > 0;
        if (success) {
            DeviceConfig dev = deviceDao.findById(deviceId);
            if (dev != null) {
                List<DevicePointMapping> mappings = getPointAddressMappings(deviceId);
                adminMqttPublisher.publishDeviceConfigUpdate("STATUS_CHANGE", dev, convertMappingsToPropertySpecs(mappings));
                adminMqttPublisher.publishDeviceControl(acqStatus == 1 ? "START" : "STOP", deviceId, dev.getDriverCode(), null, null);
            }
        }
        return success;
    }

    @Override
    public boolean deleteDevice(String deviceId) {
        DeviceConfig dev = deviceDao.findById(deviceId);
        devicePointMappingDao.deleteByDeviceId(deviceId);
        boolean success = deviceDao.deleteById(deviceId) > 0;
        if (success && dev != null) {
            adminMqttPublisher.publishDeviceConfigUpdate("DELETE", dev, null);
            adminMqttPublisher.publishDeviceControl("STOP", deviceId, dev.getDriverCode(), null, null);
        }
        return success;
    }

    @Override
    public List<DevicePointMapping> getPointAddressMappings(String deviceId) {
        if (deviceId == null || deviceId.trim().isEmpty()) {
            return new ArrayList<>();
        }
        String devId = deviceId.trim();
        DeviceConfig dev = deviceDao.findById(devId);
        if (dev == null || dev.getModelKey() == null) {
            return new ArrayList<>();
        }

        List<DevicePointMapping> existing = devicePointMappingDao.findByDeviceId(devId);
        ThingModel model = thingModelDao.findByModelKey(dev.getModelKey());

        Map<String, DevicePointMapping> existingMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        if (existing != null) {
            for (DevicePointMapping m : existing) {
                if (m != null && m.getPointCode() != null) {
                    existingMap.put(m.getPointCode(), m);
                }
            }
        }

        Set<String> addedPointCodes = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        List<DevicePointMapping> resultList = new ArrayList<>();

        if (model != null && model.getProperties() != null) {
            for (ThingModel.PropertySpec prop : model.getProperties()) {
                if (prop == null || prop.getPointCode() == null) continue;
                String pc = prop.getPointCode().trim();
                if (existingMap.containsKey(pc)) {
                    resultList.add(existingMap.get(pc));
                } else {
                    String addr = prop.getAddress() != null ? prop.getAddress().trim() : "";
                    DevicePointMapping mapping = new DevicePointMapping(
                            devId,
                            dev.getModelKey(),
                            pc,
                            prop.getPointName(),
                            addr,
                            prop.getDataType(),
                            prop.getAccessMode()
                    );
                    resultList.add(mapping);
                }
                addedPointCodes.add(pc);
            }
        }

        // 补入在设备映射表已单独配置但物模型列表中未包含的点位 (保证现场配置完整不丢失)
        if (existing != null) {
            for (DevicePointMapping m : existing) {
                if (m != null && m.getPointCode() != null && !addedPointCodes.contains(m.getPointCode().trim())) {
                    resultList.add(m);
                    addedPointCodes.add(m.getPointCode().trim());
                }
            }
        }

        return resultList;
    }

    @Override
    public List<DevicePointMapping> suggestPointAddressMappings(String deviceId) {
        DeviceConfig dev = deviceDao.findById(deviceId);
        if (dev == null || dev.getModelKey() == null) {
            return new ArrayList<>();
        }

        ThingModel model = thingModelDao.findByModelKey(dev.getModelKey());
        if (model == null || model.getProperties() == null || model.getProperties().isEmpty()) {
            return new ArrayList<>();
        }

        // 从驱动注册元数据中获取该驱动声明的 AddressRule
        com.unihh.iot.common.model.DriverInfo driverInfo = (driverInfoDao != null) ? driverInfoDao.findByCode(dev.getDriverCode()) : null;
        com.unihh.iot.protocol.model.DriverMetadata.AddressRule addressRule = (driverInfo != null) ? driverInfo.getAddressRule() : null;

        // 调用驱动地址推算引擎动态推算所有点位地址
        Map<String, String> suggestedMap = com.unihh.iot.protocol.address.DriverAddressEvaluator.evaluate(
                addressRule, dev.getDriverCode(), dev, model.getProperties()
        );

        List<DevicePointMapping> suggestedList = new ArrayList<>();
        for (ThingModel.PropertySpec prop : model.getProperties()) {
            String suggestedAddr = suggestedMap.getOrDefault(prop.getPointCode(), "");
            if (prop.getAddress() != null && !prop.getAddress().trim().isEmpty()) {
                suggestedAddr = prop.getAddress();
            }

            DevicePointMapping mapping = new DevicePointMapping(
                    deviceId,
                    dev.getModelKey(),
                    prop.getPointCode(),
                    prop.getPointName(),
                    suggestedAddr,
                    prop.getDataType(),
                    prop.getAccessMode()
            );
            if (prop.getScale() != null) {
                mapping.setScale(prop.getScale());
            }
            if (prop.getOffset() != null) {
                mapping.setOffset(prop.getOffset());
            }
            suggestedList.add(mapping);
        }

        return suggestedList;
    }

    @Override
    public boolean savePointAddressMappings(String deviceId, List<DevicePointMapping> mappings) {
        if (mappings == null) return false;
        DeviceConfig dev = deviceDao.findById(deviceId);
        if (dev == null || dev.getModelKey() == null) return false;
        boolean success = devicePointMappingDao.saveOrUpdateBatch(deviceId, dev.getModelKey(), mappings) > 0;
        if (success) {
            adminMqttPublisher.publishDeviceConfigUpdate("POINTS_UPDATE", dev, convertMappingsToPropertySpecs(mappings));
        }
        return success;
    }

    @Override
    public boolean controlDevicePoint(String deviceId, String pointCode, Object value) {
        return controlDevicePoint(deviceId, pointCode, value, null);
    }

    @Override
    public boolean controlDevicePoint(String deviceId, String pointCode, Object value, String explicitDataType) {
        DeviceConfig dev = deviceDao.findById(deviceId);
        if (dev == null) return false;

        // 查询该测点的实际物理地址与数据类型及缩放系数 (优先采用设备物理点位映射配置)
        String physicalAddress = null;
        String dataType = null;
        Double scale = 1.0;
        Double offset = 0.0;
        try {
            List<DevicePointMapping> mappings = devicePointMappingDao.findByDeviceId(deviceId);
            if (mappings != null) {
                for (DevicePointMapping m : mappings) {
                    if (pointCode.equalsIgnoreCase(m.getPointCode())) {
                        physicalAddress = m.getPhysicalAddress();
                        if (m.getDataType() != null && !m.getDataType().trim().isEmpty()) {
                            dataType = m.getDataType().trim();
                        }
                        if (m.getScale() != null) scale = m.getScale();
                        if (m.getOffset() != null) offset = m.getOffset();
                        break;
                    }
                }
            }
        } catch (Exception ignored) {}

        // 若设备物理点位映射未指定数据类型，则使用调用方传入的显式类型
        if (dataType == null || dataType.trim().isEmpty()) {
            dataType = explicitDataType;
        }

        // 如果仍未获取到，从对应物模型中提取定义
        if (dataType == null || dataType.trim().isEmpty() || physicalAddress == null || physicalAddress.trim().isEmpty()) {
            try {
                ThingModel model = thingModelDao.findByModelKey(dev.getModelKey());
                if (model != null && model.getProperties() != null) {
                    for (ThingModel.PropertySpec p : model.getProperties()) {
                        if (pointCode.equalsIgnoreCase(p.getPointCode())) {
                            if (dataType == null || dataType.trim().isEmpty()) dataType = p.getDataType();
                            if (physicalAddress == null || physicalAddress.trim().isEmpty()) physicalAddress = p.getAddress();
                            break;
                        }
                    }
                }
            } catch (Exception ignored) {}
        }

        // 自动类型推导兜底 (若传入值含小数点或为浮点数类型，标记为 FLOAT 32位单精度；布尔标记为 BOOL；整数标记为 INT 16位)
        if (dataType == null || dataType.trim().isEmpty()) {
            if (value != null && (value.toString().contains(".") || value instanceof Double || value instanceof Float)) {
                dataType = "FLOAT";
            } else if (value instanceof Boolean || "true".equalsIgnoreCase(String.valueOf(value)) || "false".equalsIgnoreCase(String.valueOf(value))) {
                dataType = "BOOL";
            } else {
                dataType = "INT";
            }
        }

        if (physicalAddress == null || physicalAddress.trim().isEmpty()) {
            log.error("[DeviceControl] 拒绝下发: 设备 [{}] 测点 [{}] 未配置物理底层寄存器地址，根据工业安全规范严禁兜底或猜测地址下发!", deviceId, pointCode);
            return false;
        }

        int slaveId = 1;
        String byteOrder = "s7-driver".equalsIgnoreCase(dev.getDriverCode()) ? "ABCD" : "CDAB";
        if (dev.getConnectionParams() != null) {
            if (dev.getConnectionParams().get("slaveId") != null) {
                try {
                    slaveId = Integer.parseInt(dev.getConnectionParams().get("slaveId").toString());
                } catch (Exception ignored) {}
            }
            if (dev.getConnectionParams().get("byteOrder") != null) {
                String bo = dev.getConnectionParams().get("byteOrder").toString().trim();
                if (!bo.isEmpty()) {
                    byteOrder = bo;
                }
            }
        }

        // 1. 记录反控指令审计日志
        try {
            if (logDao != null) {
                com.unihh.iot.common.model.IotLog cmdLog = new com.unihh.iot.common.model.IotLog();
                cmdLog.setLogId("LOG_" + System.currentTimeMillis());
                cmdLog.setLogType("COMMAND");
                cmdLog.setDeviceId(deviceId);
                cmdLog.setModelKey(dev.getModelKey());
                cmdLog.setLevel("INFO");
                cmdLog.setContent(String.format("控制指令下发: 设置点位 [%s] (物理地址: %s, 数据类型: %s, 字节序: %s, 系数: %s) 目标值: %s",
                        pointCode, physicalAddress != null ? physicalAddress : "--", dataType, byteOrder, scale, value));
                cmdLog.setOperator("admin");
                logDao.insert(cmdLog);
            }
        } catch (Exception logErr) {
            log.warn("记录点位反控审计日志失败: {}", logErr.getMessage());
        }

        // 2. 通过 MQTT 消息总线广播反控写入指令至具体驱动
        adminMqttPublisher.publishDeviceControl("WRITE_POINT", deviceId, dev.getDriverCode(), pointCode, value,
                physicalAddress, dataType, dev.getHost(), dev.getPort() > 0 ? dev.getPort() : 502, slaveId, scale, offset, byteOrder);
        return true;
    }

    @Override
    public boolean invokeDeviceService(String deviceId, String serviceCode, Map<String, Object> params) {
        DeviceConfig dev = deviceDao.findById(deviceId);
        if (dev == null) return false;

        // 1. 记录服务调用审计日志
        try {
            if (logDao != null) {
                com.unihh.iot.common.model.IotLog cmdLog = new com.unihh.iot.common.model.IotLog();
                cmdLog.setLogId("LOG_" + System.currentTimeMillis());
                cmdLog.setLogType("COMMAND");
                cmdLog.setDeviceId(deviceId);
                cmdLog.setModelKey(dev.getModelKey());
                cmdLog.setLevel("INFO");
                cmdLog.setContent(String.format("控制指令下发: 调用服务 [%s], 参数: %s",
                        serviceCode, params != null ? com.alibaba.fastjson2.JSON.toJSONString(params) : "{}"));
                cmdLog.setOperator("admin");
                logDao.insert(cmdLog);
            }
        } catch (Exception logErr) {
            log.warn("记录服务调用审计日志失败: {}", logErr.getMessage());
        }

        // 2. 通过 MQTT 消息总线广播服务指令
        adminMqttPublisher.publishDeviceServiceInvoke(deviceId, dev.getDriverCode(), serviceCode, params);
        return true;
    }

    private List<ThingModel.PropertySpec> convertMappingsToPropertySpecs(List<DevicePointMapping> mappings) {
        List<ThingModel.PropertySpec> specs = new ArrayList<>();
        if (mappings != null) {
            for (DevicePointMapping m : mappings) {
                ThingModel.PropertySpec spec = new ThingModel.PropertySpec(
                        m.getPointCode(),
                        m.getPointName(),
                        m.getDataType(),
                        m.getAccessMode(),
                        ""
                );
                spec.setAddress(m.getPhysicalAddress());
                if (m.getScale() != null) spec.setScale(m.getScale());
                if (m.getOffset() != null) spec.setOffset(m.getOffset());
                specs.add(spec);
            }
        }
        return specs;
    }
}
