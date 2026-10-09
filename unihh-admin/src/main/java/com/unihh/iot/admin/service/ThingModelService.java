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

import com.unihh.iot.common.model.ThingModel;
import java.util.List;

public interface ThingModelService {
    ThingModel getThingModel(String modelKey);
    List<ThingModel> getAllThingModels();
    boolean saveOrUpdateModel(ThingModel model);
    boolean addProperty(String modelKey, ThingModel.PropertySpec prop);
    boolean updateProperty(String modelKey, String pointCode, ThingModel.PropertySpec prop);
    boolean deleteProperty(String modelKey, String pointCode);
    boolean addService(String modelKey, ThingModel.ServiceSpec service);
    boolean updateService(String modelKey, String serviceCode, ThingModel.ServiceSpec service);
    boolean deleteService(String modelKey, String serviceCode);
    boolean addEvent(String modelKey, ThingModel.EventSpec event);
    boolean updateEvent(String modelKey, String eventCode, ThingModel.EventSpec event);
    boolean deleteEvent(String modelKey, String eventCode);
    boolean deleteModel(String modelKey);
}
