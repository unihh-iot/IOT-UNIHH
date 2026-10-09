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

import com.alibaba.fastjson2.JSON;
import com.unihh.iot.admin.dao.DeviceDao;
import com.unihh.iot.admin.dao.RuleActionDao;
import com.unihh.iot.admin.dao.RuleDao;
import com.unihh.iot.admin.service.RuleService;
import com.unihh.iot.common.model.DeviceConfig;
import com.unihh.iot.common.model.Rule;
import com.unihh.iot.common.model.RuleAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class RuleServiceImpl implements RuleService {

    @Autowired
    private RuleDao ruleDao;

    @Autowired
    private RuleActionDao ruleActionDao;

    @Autowired
    private DeviceDao deviceDao;

    @Autowired(required = false)
    @org.springframework.context.annotation.Lazy
    private com.unihh.iot.admin.mqtt.AdminMqttPublisher adminMqttPublisher;

    private void notifyRuleSync() {
        if (adminMqttPublisher != null) {
            try {
                adminMqttPublisher.publishRuleSync();
            } catch (Exception ignored) {}
        }
    }

    @Override
    public List<Rule> getRuleList(String keyword, String deviceId, Boolean enabled) {
        List<Rule> list = ruleDao.findAll(keyword, deviceId, enabled);
        if (list != null) {
            for (Rule r : list) {
                populateRuleActions(r);
            }
        }
        return list;
    }

    @Override
    public Rule getRuleById(String ruleId) {
        Rule rule = ruleDao.findByRuleId(ruleId);
        if (rule != null) {
            populateRuleActions(rule);
        }
        return rule;
    }

    private void populateRuleActions(Rule rule) {
        if (rule == null) return;
        List<RuleAction> actions = ruleActionDao.findByRuleId(rule.getRuleId());
        rule.setActions(actions != null ? actions : new ArrayList<>());
    }

    private void autoFillRuleMeta(Rule rule) {
        if (rule == null) return;
        if (rule.getRuleId() == null || rule.getRuleId().trim().isEmpty()) {
            rule.setRuleId("RULE_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        if ((rule.getModelKey() == null || rule.getModelKey().trim().isEmpty()) && rule.getSourceDeviceId() != null) {
            try {
                DeviceConfig dev = deviceDao.findById(rule.getSourceDeviceId());
                if (dev != null && dev.getModelKey() != null && !dev.getModelKey().trim().isEmpty()) {
                    rule.setModelKey(dev.getModelKey());
                }
            } catch (Exception ignored) {
            }
        }
        if (rule.getActions() != null) {
            for (RuleAction act : rule.getActions()) {
                if (act.getActionId() == null || act.getActionId().trim().isEmpty()) {
                    act.setActionId("ACT_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                }
            }
        }
    }

    @Override
    @Transactional
    public boolean addRule(Rule rule) {
        autoFillRuleMeta(rule);
        boolean ok = ruleDao.insert(rule) > 0;
        if (ok && rule.getActions() != null && !rule.getActions().isEmpty()) {
            ruleActionDao.batchInsert(rule.getRuleId(), rule.getActions());
        }
        if (ok) notifyRuleSync();
        return ok;
    }

    @Override
    @Transactional
    public boolean updateRule(Rule rule) {
        autoFillRuleMeta(rule);
        boolean ok = ruleDao.update(rule) > 0;
        if (ok) {
            ruleActionDao.deleteByRuleId(rule.getRuleId());
            if (rule.getActions() != null && !rule.getActions().isEmpty()) {
                ruleActionDao.batchInsert(rule.getRuleId(), rule.getActions());
            }
            notifyRuleSync();
        }
        return ok;
    }

    @Override
    public boolean toggleRule(String ruleId, boolean enabled) {
        boolean ok = ruleDao.updateEnabled(ruleId, enabled) > 0;
        if (ok) notifyRuleSync();
        return ok;
    }

    @Override
    @Transactional
    public boolean deleteRule(String ruleId) {
        ruleActionDao.deleteByRuleId(ruleId);
        boolean ok = ruleDao.delete(ruleId) > 0;
        if (ok) notifyRuleSync();
        return ok;
    }
}
