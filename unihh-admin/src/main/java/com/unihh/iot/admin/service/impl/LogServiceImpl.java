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

import com.unihh.iot.admin.dao.LogDao;
import com.unihh.iot.admin.service.LogService;
import com.unihh.iot.common.model.IotLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class LogServiceImpl implements LogService {

    @Autowired
    private LogDao logDao;

    @Override
    public List<IotLog> getLogList(String keyword, String logType, String level) {
        return logDao.findAll(keyword, logType, level);
    }

    @Override
    public boolean recordLog(IotLog log) {
        if (log.getLogId() == null || log.getLogId().trim().isEmpty()) {
            log.setLogId("LOG-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        }
        return logDao.insert(log) > 0;
    }

    @Override
    public boolean clearLogs() {
        return logDao.clearAll() >= 0;
    }
}
