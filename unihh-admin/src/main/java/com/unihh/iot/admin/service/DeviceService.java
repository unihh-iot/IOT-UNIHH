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

package com.unihh.iot.admin.service;

import com.unihh.iot.common.model.DeviceConfig;
import com.unihh.iot.common.model.DevicePointMapping;
import java.util.List;

public interface DeviceService {
    List<DeviceConfig> getDeviceList(String keyword, String driverCode, Integer acqStatus);
    DeviceConfig getDeviceById(String deviceId);
    boolean addDevice(DeviceConfig device);
    boolean updateDevice(DeviceConfig device);
    boolean updateAcqStatus(String deviceId, int acqStatus);
    boolean deleteDevice(String deviceId);
    List<DevicePointMapping> getPointAddressMappings(String deviceId);
    List<DevicePointMapping> suggestPointAddressMappings(String deviceId);
    boolean savePointAddressMappings(String deviceId, List<DevicePointMapping> mappings);
    boolean controlDevicePoint(String deviceId, String pointCode, Object value);
    boolean controlDevicePoint(String deviceId, String pointCode, Object value, String explicitDataType);
    boolean invokeDeviceService(String deviceId, String serviceCode, java.util.Map<String, Object> params);
}
