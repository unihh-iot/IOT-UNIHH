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

import com.unihh.iot.admin.dao.AlarmDao;
import com.unihh.iot.admin.service.AlarmService;
import com.unihh.iot.common.model.AlarmRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlarmServiceImpl implements AlarmService {

    @Autowired
    private AlarmDao alarmDao;

    @Override
    public List<AlarmRecord> getAlarmList(String keyword, String alarmLevel, Integer status, String ruleId) {
        return alarmDao.findAll(keyword, alarmLevel, status, ruleId);
    }

    @Override
    public AlarmRecord getAlarmById(String alarmId) {
        return alarmDao.findByAlarmId(alarmId);
    }

    @Override
    public boolean createAlarm(AlarmRecord alarm) {
        if (alarm == null) return false;
        if (alarm.getAlarmId() == null || alarm.getAlarmId().trim().isEmpty()) {
            alarm.setAlarmId("ALM_" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        if (alarm.getAlarmTime() == null) {
            alarm.setAlarmTime(new java.util.Date());
        }
        if (alarm.getStatus() == null) {
            alarm.setStatus(0);
        }
        return alarmDao.insert(alarm) > 0;
    }

    @Override
    public boolean acknowledgeAlarm(String alarmId, String handleUser, String remark) {
        return alarmDao.updateStatus(alarmId, 1, handleUser, remark) > 0;
    }

    @Override
    public boolean resolveAlarm(String alarmId, String handleUser, String remark) {
        return alarmDao.updateStatus(alarmId, 2, handleUser, remark) > 0;
    }

    @Override
    public boolean deleteAlarm(String alarmId) {
        return alarmDao.delete(alarmId) > 0;
    }
}
