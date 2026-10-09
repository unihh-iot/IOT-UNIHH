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

import com.unihh.iot.admin.dao.ThingModelDao;
import com.unihh.iot.admin.service.ThingModelService;
import com.unihh.iot.common.model.ThingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ThingModelServiceImpl implements ThingModelService {

    @Autowired
    private ThingModelDao thingModelDao;

    @Autowired(required = false)
    private com.unihh.iot.admin.dao.DeviceDao deviceDao;

    @Autowired(required = false)
    private com.unihh.iot.admin.dao.DevicePointMappingDao devicePointMappingDao;

    @Autowired(required = false)
    private com.unihh.iot.admin.mqtt.AdminMqttPublisher adminMqttPublisher;

    @Override
    public ThingModel getThingModel(String modelKey) {
        ThingModel model = thingModelDao.findByModelKey(modelKey);
        if (model != null && deviceDao != null && model.getModelKey() != null) {
            model.setDeviceCount(deviceDao.countByModelKey(model.getModelKey()));
        }
        return model;
    }

    @Override
    public List<ThingModel> getAllThingModels() {
        List<ThingModel> list = thingModelDao.findAllModels();
        if (list != null && deviceDao != null) {
            java.util.Map<String, Integer> countMap = deviceDao.countDevicesGroupedByModel();
            for (ThingModel m : list) {
                if (m.getModelKey() != null) {
                    m.setDeviceCount(countMap.getOrDefault(m.getModelKey(), 0));
                }
            }
        }
        return list;
    }

    @Override
    public boolean saveOrUpdateModel(ThingModel model) {
        if (model != null && (model.getModelKey() == null || model.getModelKey().trim().isEmpty())) {
            model.setModelKey("PK_" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        return thingModelDao.saveOrUpdateModel(model) > 0;
    }

    @Override
    public boolean addProperty(String modelKey, ThingModel.PropertySpec prop) {
        return thingModelDao.insertProperty(modelKey, prop) > 0;
    }

    @Override
    public boolean updateProperty(String modelKey, String pointCode, ThingModel.PropertySpec prop) {
        boolean ok = thingModelDao.updateProperty(modelKey, pointCode, prop) > 0;
        if (ok) {
            // 同步级联更新已关联物理设备映射表中的属性定义 (dataType, pointName, accessMode)
            if (devicePointMappingDao != null) {
                devicePointMappingDao.updatePropertySpecByModelKey(modelKey, pointCode, prop.getDataType(), prop.getPointName(), prop.getAccessMode());
            }
            // 广播通知正在运行的协议驱动更新点位配置与数据类型
            if (deviceDao != null && adminMqttPublisher != null && devicePointMappingDao != null) {
                try {
                    List<com.unihh.iot.common.model.DeviceConfig> devices = deviceDao.findAll(null, null, null);
                    for (com.unihh.iot.common.model.DeviceConfig dev : devices) {
                        if (modelKey.equalsIgnoreCase(dev.getModelKey())) {
                            List<com.unihh.iot.common.model.DevicePointMapping> mappings = devicePointMappingDao.findByDeviceId(dev.getDeviceId());
                            List<ThingModel.PropertySpec> specs = new java.util.ArrayList<>();
                            for (com.unihh.iot.common.model.DevicePointMapping m : mappings) {
                                ThingModel.PropertySpec s = new ThingModel.PropertySpec(
                                        m.getPointCode(),
                                        m.getPointName(),
                                        m.getDataType(),
                                        m.getAccessMode(),
                                        ""
                                );
                                s.setAddress(m.getPhysicalAddress());
                                specs.add(s);
                            }
                            adminMqttPublisher.publishDeviceConfigUpdate("POINTS_UPDATE", dev, specs);
                        }
                    }
                } catch (Exception ignored) {}
            }
        }
        return ok;
    }

    @Override
    public boolean deleteProperty(String modelKey, String pointCode) {
        return thingModelDao.deleteProperty(modelKey, pointCode) > 0;
    }

    @Override
    public boolean addService(String modelKey, ThingModel.ServiceSpec service) {
        return thingModelDao.insertService(modelKey, service) > 0;
    }

    @Override
    public boolean updateService(String modelKey, String serviceCode, ThingModel.ServiceSpec service) {
        return thingModelDao.updateService(modelKey, serviceCode, service) > 0;
    }

    @Override
    public boolean deleteService(String modelKey, String serviceCode) {
        return thingModelDao.deleteService(modelKey, serviceCode) > 0;
    }

    @Override
    public boolean addEvent(String modelKey, ThingModel.EventSpec event) {
        return thingModelDao.insertEvent(modelKey, event) > 0;
    }

    @Override
    public boolean updateEvent(String modelKey, String eventCode, ThingModel.EventSpec event) {
        return thingModelDao.updateEvent(modelKey, eventCode, event) > 0;
    }

    @Override
    public boolean deleteEvent(String modelKey, String eventCode) {
        return thingModelDao.deleteEvent(modelKey, eventCode) > 0;
    }

    @Override
    public boolean deleteModel(String modelKey) {
        if (deviceDao != null) {
            int refCount = deviceDao.countByModelKey(modelKey);
            if (refCount > 0) {
                throw new IllegalStateException("当前物模型被 " + refCount + " 台设备引用，禁止删除！请先解绑或删除关联设备。");
            }
        }
        return thingModelDao.deleteModel(modelKey) > 0;
    }
}
