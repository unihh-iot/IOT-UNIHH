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

import com.unihh.iot.common.model.DriverInfo;
import java.util.List;
import java.util.Map;

public interface DriverService {
    List<DriverInfo> getDriverList();
    Map<String, Object> getDriverPage(String keyword, Integer status, Integer pageNum, Integer pageSize);
    DriverInfo getDriverByCode(String driverCode);
    boolean addDriver(DriverInfo driverInfo);
    boolean updateStatus(String driverCode, int status);
    boolean deleteDriver(String driverCode);
    Map<String, Object> pingDriver(String driverCode);
}
