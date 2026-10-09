<!--
  Author: deversun
  Website: https://unihh.com
  License: Apache License 2.0
-->
<template>
  <div class="page-container">
    <!-- Page Header -->
    <div class="page-header">
      <div class="header-title-box">
        <h2 class="iot-header-title">
          <SvgIcon name="refresh" :size="24" color="#ef4444" />
          规则管理 (1:N 规则引擎与设备控制服务联动)
        </h2>
        <span class="header-subtitle">
          聚焦规则条件判定与自动化设备控制服务下发
        </span>
      </div>
      <div v-if="hasPermission('rule:edit')" class="header-action-group">
        <el-button type="primary" size="default" class="add-btn-flex" @click="openAddRuleModal">
          <SvgIcon name="plus" :size="16" />
          新建规则
        </el-button>
      </div>
    </div>

    <!-- 4 High-Tech Linear Gradient KPI Cards -->
    <div class="metrics-grid">
      <div class="iot-card metric-card grad-cyan">
        <div class="metric-icon icon-cyan">
          <SvgIcon name="refresh" :size="22" color="#06b6d4" />
        </div>
        <div class="metric-info">
          <span class="metric-label">已启用规则数</span>
          <div class="metric-val-wrap">
            <span class="metric-value text-cyan">{{ activeRuleCount }}</span>
            <span class="metric-unit">条</span>
          </div>
        </div>
      </div>

      <div class="iot-card metric-card grad-green">
        <div class="metric-icon icon-green">
          <SvgIcon name="send" :size="22" color="#10b981" />
        </div>
        <div class="metric-info">
          <span class="metric-label">配置反控动作数</span>
          <div class="metric-val-wrap">
            <span class="metric-value text-green">{{ totalActionCount }}</span>
            <span class="metric-unit">项</span>
          </div>
        </div>
      </div>

      <div class="iot-card metric-card grad-indigo">
        <div class="metric-icon icon-indigo">
          <SvgIcon name="cpu" :size="22" color="#818cf8" />
        </div>
        <div class="metric-info">
          <span class="metric-label">受控设备覆盖数</span>
          <div class="metric-val-wrap">
            <span class="metric-value text-indigo">{{ coveredDeviceCount }}</span>
            <span class="metric-unit">台</span>
          </div>
        </div>
      </div>

      <div class="iot-card metric-card grad-orange">
        <div class="metric-icon icon-orange">
          <SvgIcon name="bell" :size="22" color="#f59e0b" />
        </div>
        <div class="metric-info">
          <span class="metric-label">停用/备用规则数</span>
          <div class="metric-val-wrap">
            <span class="metric-value text-orange">{{ disabledRuleCount }}</span>
            <span class="metric-unit">条</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Search & Filter Toolbar -->
    <div class="iot-card filter-card">
      <div class="filter-row">
        <el-input
          v-model="queryForm.keyword"
          placeholder="搜索规则ID、规则名称、监控点位或联动指令..."
          clearable
          style="width: 300px;"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <SvgIcon name="filter" :size="14" color="#94a3b8" />
          </template>
        </el-input>

        <el-select
          v-model="queryForm.deviceId"
          placeholder="按监控设备筛选"
          clearable
          style="width: 220px;"
        >
          <el-option label="全部监控设备" value="" />
          <el-option
            v-for="dev in mockDevices"
            :key="dev.id"
            :label="`${dev.name} (${dev.id})`"
            :value="dev.id"
          />
        </el-select>

        <el-select
          v-model="queryForm.enabled"
          placeholder="启停状态"
          clearable
          style="width: 140px;"
        >
          <el-option label="全部状态" value="" />
          <el-option label="已启用" value="true" />
          <el-option label="已停用" value="false" />
        </el-select>

        <el-button type="primary" @click="handleSearch">
          <SvgIcon name="filter" :size="14" style="margin-right: 4px;" />
          查询
        </el-button>

        <el-button type="default" @click="handleReset">
          重置
        </el-button>

        <div class="search-counter">
          共找到 <strong>{{ filteredRules.length }}</strong> 条规则
        </div>
      </div>
    </div>

    <!-- Rule Cards Grid with Vibrant Gradient Styling -->
    <div v-if="pagedRules.length > 0" class="rule-cards-grid">
      <div v-for="rule in pagedRules" :key="rule.ruleId" class="rule-card iot-card" :class="{ 'is-disabled': !rule.enabled }">
        <!-- Master Rule Header -->
        <div class="card-top-header">
          <div class="rule-title-group">
            <span class="rule-id-badge">{{ rule.ruleId }}</span>
            <h3 class="rule-title" :title="rule.ruleName">{{ rule.ruleName }}</h3>
          </div>
          <div class="rule-status-switch">
            <span class="switch-label">主开关:</span>
            <el-switch
              v-model="rule.enabled"
              :disabled="!hasPermission('rule:edit')"
              active-text="开启"
              inactive-text="停用"
              inline-prompt
              @change="toggleMasterRule(rule)"
            />
          </div>
        </div>

        <div class="rule-card-body">
          <!-- 1. Trigger Rule & Flexible Condition Display -->
          <div class="rule-info-row">
            <span class="row-label">
              <SvgIcon name="cpu" :size="14" color="#06b6d4" />
              监控对象与点位
            </span>
            <div class="row-value condition-value-box">
              <el-tag size="small" type="info" class="device-tag" :title="getDeviceName(rule.sourceDeviceId)">
                {{ getDeviceName(rule.sourceDeviceId) }}
              </el-tag>
              <code class="point-code" :title="rule.sourcePoint">{{ rule.sourcePoint }}</code>
              
              <!-- Flexible Condition Modes: SINGLE / RANGE_INSIDE / RANGE_OUTSIDE / EVENT -->
              <template v-if="getPointType(rule.sourceDeviceId, rule.sourcePoint) === 'PROPERTY'">
                <template v-if="rule.conditionMode === 'RANGE_INSIDE'">
                  <span class="range-text">
                    <code>{{ rule.minVal }}</code> ≤ 点位 ≤ <code>{{ rule.maxVal }}</code> {{ getPointUnit(rule.sourceDeviceId, rule.sourcePoint) }}
                    <el-tag size="small" type="success" effect="plain" class="margin-left-mini">区间内</el-tag>
                  </span>
                </template>

                <template v-else-if="rule.conditionMode === 'RANGE_OUTSIDE'">
                  <span class="range-text">
                    &lt; <code>{{ rule.minVal }}</code> 或 &gt; <code>{{ rule.maxVal }}</code> {{ getPointUnit(rule.sourceDeviceId, rule.sourcePoint) }}
                    <el-tag size="small" type="danger" effect="plain" class="margin-left-mini">超界</el-tag>
                  </span>
                </template>

                <template v-else>
                  <span class="operator-badge">{{ rule.operator }}</span>
                  <span class="threshold-val">{{ rule.thresholdValue }} {{ getPointUnit(rule.sourceDeviceId, rule.sourcePoint) }}</span>
                </template>
              </template>
              <template v-else>
                <el-tag size="small" type="warning" effect="plain">事件上报触发</el-tag>
              </template>
            </div>
          </div>

          <!-- 2. Control Service Linkage Summary Badge -->
          <div class="rule-info-row">
            <span class="row-label">
              <SvgIcon name="send" :size="14" color="#10b981" />
              服务下发联动 (共 {{ rule.actions.length }} 项反控)
            </span>
            <div class="row-value linkage-summary-row">
              <div v-if="rule.actions.length > 0" class="actions-badge-wrapper">
                <el-tag
                  v-for="act in rule.actions"
                  :key="act.actionId"
                  size="small"
                  :type="act.enabled ? 'success' : 'info'"
                  :effect="act.enabled ? 'light' : 'plain'"
                  class="action-summary-tag"
                  :title="`${act.actionName} [${act.targetServiceCode}]`"
                >
                  <span class="dot-indicator" :class="{ 'is-active': act.enabled }"></span>
                  <span class="tag-text-ellipsis">{{ act.actionName }}</span>
                </el-tag>
              </div>
              <span v-else class="text-muted">暂未配置控制联动</span>

              <el-button
                v-if="hasPermission('rule:edit')"
                size="small"
                type="primary"
                plain
                class="config-actions-btn"
                @click="openLinkagesManageModal(rule)"
              >
                配置联动 ({{ rule.actions.length }})
              </el-button>
            </div>
          </div>
        </div>

        <!-- Master Rule Footer -->
        <div v-if="hasPermission('rule:edit')" class="rule-card-footer">
          <el-button size="small" type="default" plain @click="openEditRuleModal(rule)">
            编辑规则
          </el-button>
          <el-button size="small" type="danger" plain @click="deleteRule(rule)">
            删除
          </el-button>
        </div>
      </div>
    </div>

    <!-- Empty State -->
    <div v-else class="iot-card empty-card">
      <el-empty description="暂无符合条件的规则记录" />
    </div>

    <!-- Pagination Wrapper -->
    <div class="iot-card pagination-card">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[8, 16, 24, 32]"
        :total="filteredRules.length"
        layout="total, sizes, prev, pager, next, jumper"
        background
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- Modal 1: Add/Edit Rule Main Container -->
    <el-dialog v-model="ruleModalVisible" :title="isEditRule ? '编辑规则定义' : '新建规则'" width="600px">
      <el-form :model="ruleForm" label-width="150px" class="rule-form">
        <div class="form-section-title">1. 规则基础信息</div>
        <el-form-item v-if="isEditRule" label="规则 ID">
          <span class="rule-id-badge">{{ ruleForm.ruleId }}</span>
        </el-form-item>

        <el-form-item label="规则名称">
          <el-input v-model="ruleForm.ruleName" placeholder="如 1号离心风机电机轴承超温自动风扇与降速联动" />
        </el-form-item>

        <div class="form-section-title">2. 监控对象与判定条件模式</div>
        <el-form-item label="监控对象设备">
          <el-select v-model="ruleForm.sourceDeviceId" style="width: 100%" placeholder="选择监控设备" @change="onSourceDeviceChange">
            <el-option v-for="dev in mockDevices" :key="dev.id" :label="`${dev.name} (${dev.id})`" :value="dev.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="监控点位 / 物模型事件">
          <el-select v-model="ruleForm.sourcePoint" style="width: 100%" placeholder="选择物模型点位/事件">
            <el-option
              v-for="pt in availableSourcePoints"
              :key="pt.code"
              :label="`${pt.name} [${pt.code}] (${pt.type})`"
              :value="pt.code"
            />
          </el-select>
        </el-form-item>

        <!-- Condition Evaluation Mode Selection: SINGLE / RANGE_INSIDE / RANGE_OUTSIDE -->
        <template v-if="selectedSourcePointObj && selectedSourcePointObj.dataType !== 'EVENT'">
          <el-form-item label="判定逻辑类型">
            <el-select v-model="ruleForm.conditionMode" style="width: 100%">
              <el-option label="单点阈值比较 (例如: > 80.0)" value="SINGLE" />
              <el-option label="区间内触发 (例如: 20.0 <= 点位 <= 80.0)" value="RANGE_INSIDE" />
              <el-option label="超范围/越界触发 (例如: 点位 < 20.0 或 > 80.0)" value="RANGE_OUTSIDE" />
            </el-select>
          </el-form-item>

          <!-- Single Threshold Form -->
          <template v-if="ruleForm.conditionMode === 'SINGLE'">
            <el-form-item label="判断运算符">
              <el-select v-model="ruleForm.operator" style="width: 100%">
                <el-option label="> (大于)" value=">" />
                <el-option label=">= (大于等于)" value=">=" />
                <el-option label="< (小于)" value="<" />
                <el-option label="<= (小于等于)" value="<=" />
                <el-option label="== (等于)" value="==" />
                <el-option label="!= (不等于)" value="!=" />
              </el-select>
            </el-form-item>

            <el-form-item label="判定阈值">
              <div class="threshold-input-group">
                <el-input-number v-model="ruleForm.thresholdValue" style="width: 180px" :precision="1" :step="0.5" />
                <span class="unit-text">{{ selectedSourcePointObj.unit || '' }}</span>
              </div>
            </el-form-item>
          </template>

          <!-- Range Threshold Form -->
          <template v-else>
            <el-form-item label="判定区间下限 (Min)">
              <div class="threshold-input-group">
                <el-input-number v-model="ruleForm.minVal" style="width: 180px" :precision="1" :step="0.5" />
                <span class="unit-text">{{ selectedSourcePointObj.unit || '' }}</span>
              </div>
            </el-form-item>

            <el-form-item label="判定区间上限 (Max)">
              <div class="threshold-input-group">
                <el-input-number v-model="ruleForm.maxVal" style="width: 180px" :precision="1" :step="0.5" />
                <span class="unit-text">{{ selectedSourcePointObj.unit || '' }}</span>
              </div>
            </el-form-item>
          </template>
        </template>

        <template v-else-if="selectedSourcePointObj">
          <el-form-item label="触发条件模式">
            <el-tag type="warning" effect="plain">物模型事件模式：设备端主动上报事件时触发</el-tag>
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="ruleModalVisible = false">取消</el-button>
        <el-button type="primary" @click="submitSaveRule">保存规则</el-button>
      </template>
    </el-dialog>

    <!-- Modal 2: Manage Linkage Actions for a Specific Rule -->
    <el-dialog
      v-model="linkagesModalVisible"
      :title="`配置规则联动响应动作 - [${activeRuleContext ? activeRuleContext.ruleName : ''}]`"
      width="820px"
    >
      <div v-if="activeRuleContext" class="linkages-manage-container">
        <!-- Banner Summary -->
        <div class="linkage-modal-banner">
          <div class="banner-info-item">
            <span class="label">触发源设备:</span>
            <strong>{{ getDeviceName(activeRuleContext.sourceDeviceId) }}</strong>
          </div>
          <div class="banner-info-item">
            <span class="label">判定条件:</span>
            <template v-if="activeRuleContext.conditionMode === 'RANGE_INSIDE'">
              <code class="text-cyan">{{ activeRuleContext.minVal }} ≤ {{ activeRuleContext.sourcePoint }} ≤ {{ activeRuleContext.maxVal }}</code>
            </template>
            <template v-else-if="activeRuleContext.conditionMode === 'RANGE_OUTSIDE'">
              <code class="text-cyan">{{ activeRuleContext.sourcePoint }} &lt; {{ activeRuleContext.minVal }} 或 &gt; {{ activeRuleContext.maxVal }}</code>
            </template>
            <template v-else>
              <code class="text-cyan">{{ activeRuleContext.sourcePoint }} {{ activeRuleContext.operator }} {{ activeRuleContext.thresholdValue }}</code>
            </template>
          </div>
          <el-button type="success" size="small" class="add-action-btn-top" @click="openAddActionModal">
            + 添加联动响应动作
          </el-button>
        </div>

        <!-- Actions Management Table -->
        <el-table :data="activeRuleContext.actions" style="width: 100%" stripe class="margin-top-small" border>
          <el-table-column label="独立开关" width="90" align="center">
            <template #default="scope">
              <el-switch
                v-model="scope.row.enabled"
                size="small"
                active-text="启用"
                inactive-text="禁用"
                inline-prompt
                @change="toggleActionItem(activeRuleContext, scope.row)"
              />
            </template>
          </el-table-column>

          <el-table-column label="动作类型" width="130" align="center">
            <template #default="scope">
              <el-tag v-if="scope.row.actionType === 'ALARM'" type="danger" effect="plain" size="small">触发系统告警</el-tag>
              <el-tag v-else-if="scope.row.actionType === 'LOG'" type="warning" effect="plain" size="small">发布系统日志</el-tag>
              <el-tag v-else type="success" effect="plain" size="small">设备反控服务</el-tag>
            </template>
          </el-table-column>

          <el-table-column prop="actionName" label="联动动作名称" min-width="150" show-overflow-tooltip>
            <template #default="scope">
              <span class="font-weight-600">{{ scope.row.actionName }}</span>
            </template>
          </el-table-column>

          <el-table-column label="详细配置 / 指令内容" min-width="260">
            <template #default="scope">
              <div v-if="scope.row.actionType === 'ALARM'" class="table-service-info">
                <span>告警级别: <el-tag size="small" type="danger">{{ scope.row.alarmLevel || 'CRITICAL' }}</el-tag></span>
                <span class="text-muted" style="margin-top: 2px;">{{ scope.row.alarmContent || '生成系统告警日志' }}</span>
              </div>
              <div v-else-if="scope.row.actionType === 'LOG'" class="table-service-info">
                <span>日志级别: <el-tag size="small" type="warning">{{ scope.row.logLevel || 'INFO' }}</el-tag></span>
                <span class="text-muted" style="margin-top: 2px;">{{ scope.row.logContent || '发布审计日志记录' }}</span>
              </div>
              <div v-else class="table-service-info">
                <span>目标设备: <strong>[{{ getDeviceName(scope.row.targetDeviceId) }}]</strong></span>
                <span class="margin-left-small">指令: <code>{{ scope.row.targetServiceCode }}</code></span>
                <div v-if="scope.row.serviceInputValues && Object.keys(scope.row.serviceInputValues).length > 0" class="table-params-tags">
                  <el-tag
                    v-for="(val, pCode) in scope.row.serviceInputValues"
                    :key="pCode"
                    size="small"
                    type="info"
                    effect="plain"
                    class="mini-param-tag"
                  >
                    {{ pCode }} = {{ val }}
                  </el-tag>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="操作" width="130" fixed="right" align="center">
            <template #default="scope">
              <el-button link type="primary" size="small" @click="openEditActionModal(scope.row)">编辑</el-button>
              <el-button link type="danger" size="small" @click="deleteActionItem(activeRuleContext, scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <el-button type="primary" @click="linkagesModalVisible = false">完成配置</el-button>
      </template>
    </el-dialog>

    <!-- Modal 3: Add/Edit Single Linkage Action Item -->
    <el-dialog v-model="actionEditModalVisible" :title="isEditActionItem ? '编辑联动响应动作' : '添加联动响应动作'" width="600px">
      <el-form :model="actionForm" label-width="140px" class="action-form">
        <el-form-item label="联动动作类型">
          <el-radio-group v-model="actionForm.actionType">
            <el-radio-button label="SERVICE">设备反控服务</el-radio-button>
            <el-radio-button label="LOG">发布系统日志</el-radio-button>
            <el-radio-button label="ALARM">触发系统告警</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="联动动作名称">
          <el-input v-model="actionForm.actionName" placeholder="如 发布设备离线通知 / 开启辅助冷却风扇" />
        </el-form-item>

        <el-form-item label="联动启用状态">
          <el-switch v-model="actionForm.enabled" active-text="启用本动作" inactive-text="禁用本动作" />
        </el-form-item>

        <!-- Option 1: 设备反控服务 (SERVICE) -->
        <template v-if="actionForm.actionType === 'SERVICE'">
          <el-form-item label="联动目标设备">
            <el-select v-model="actionForm.targetDeviceId" style="width: 100%" placeholder="选择联动反控设备" @change="onActionTargetDeviceChange">
              <el-option v-for="dev in mockDevices" :key="dev.id" :label="`${dev.name} (${dev.id})`" :value="dev.id" />
            </el-select>
          </el-form-item>

          <el-form-item label="自动下发控制服务">
            <el-select v-model="actionForm.targetServiceCode" style="width: 100%" placeholder="选择目标设备物模型服务" @change="onActionTargetServiceChange">
              <el-option
                v-for="svc in availableActionTargetServices"
                :key="svc.code"
                :label="`${svc.name} [${svc.code}]`"
                :value="svc.code"
              />
            </el-select>
          </el-form-item>

          <div v-if="selectedActionServiceSchema && selectedActionServiceSchema.inputParams && selectedActionServiceSchema.inputParams.length > 0" class="schema-params-box">
            <div class="schema-box-title">
              <SvgIcon name="code" :size="14" />
              <strong>物模型入参绑定 (Service Input Parameters Schema)</strong>
            </div>

            <div v-for="param in selectedActionServiceSchema.inputParams" :key="param.code" class="param-form-row">
              <span class="param-label" style="display: flex; align-items: center; gap: 4px;">
                <span>{{ param.name }}</span> (<code>{{ param.code }}</code>):
                <el-tag
                  v-if="param.defaultValue !== undefined && param.defaultValue !== null && param.defaultValue !== ''"
                  size="small"
                  type="info"
                  style="font-size: 11px; margin-left: 4px;"
                >
                  预设默认: {{ param.defaultValue }}
                </el-tag>
              </span>

              <el-select
                v-if="param.dataType === 'ENUM'"
                v-model="actionForm.serviceInputValues[param.code]"
                style="width: 220px"
              >
                <el-option
                  v-for="opt in param.enumOptions"
                  :key="opt.value"
                  :label="`${opt.value}: ${opt.label}`"
                  :value="opt.value"
                />
              </el-select>

              <div v-else-if="param.dataType === 'INT' || param.dataType === 'FLOAT' || param.dataType === 'DOUBLE'" class="threshold-input-group">
                <el-input-number
                  v-model="actionForm.serviceInputValues[param.code]"
                  style="width: 180px"
                  :precision="param.dataType === 'INT' ? 0 : 4"
                  :step="param.dataType === 'INT' ? 1 : 0.01"
                />
                <span class="unit-text">{{ param.unit || '' }}</span>
              </div>

              <el-switch
                v-else-if="param.dataType === 'BOOL'"
                v-model="actionForm.serviceInputValues[param.code]"
                active-text="是"
                inactive-text="否"
              />

              <el-input
                v-else
                v-model="actionForm.serviceInputValues[param.code]"
                style="width: 220px"
                placeholder="请输入参数值"
              />
            </div>
          </div>
        </template>

        <!-- Option 2: 发布系统日志 (LOG) -->
        <template v-else-if="actionForm.actionType === 'LOG'">
          <el-form-item label="日志级别">
            <el-select v-model="actionForm.logLevel" style="width: 100%">
              <el-option label="信息 (INFO)" value="INFO" />
              <el-option label="警告 (WARN)" value="WARN" />
              <el-option label="错误 (ERROR)" value="ERROR" />
            </el-select>
          </el-form-item>

          <el-form-item label="发布日志内容">
            <el-input
              v-model="actionForm.logContent"
              type="textarea"
              :rows="3"
              placeholder="请输入发布的系统日志内容，如: [规则引擎] 检测到1号电机温度超标，已自动记录审计日志"
            />
          </el-form-item>
        </template>

        <!-- Option 3: 触发系统告警 (ALARM) -->
        <template v-else-if="actionForm.actionType === 'ALARM'">
          <el-form-item label="告警级别">
            <el-select v-model="actionForm.alarmLevel" style="width: 100%">
              <el-option label="紧急 (CRITICAL)" value="CRITICAL" />
              <el-option label="重要 (HIGH)" value="HIGH" />
              <el-option label="警告 (WARN)" value="WARN" />
            </el-select>
          </el-form-item>

          <el-form-item label="告警消息内容">
            <el-input
              v-model="actionForm.alarmContent"
              type="textarea"
              :rows="3"
              placeholder="请输入触发的系统告警消息，如: 紧急告警: 2号储罐压力越界，请即刻派人现场检查"
            />
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="actionEditModalVisible = false">取消</el-button>
        <el-button type="primary" @click="submitSaveActionItem">保存联动动作</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/* Copyright (c) 2026 deversun / unihh.com - Apache 2.0 License */
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import SvgIcon from '../../components/common/SvgIcon.vue'
import { hasPermission } from '@/utils/permission'
import { getRuleList, addRule, updateRule, toggleRule as apiToggleRule, deleteRule as apiDeleteRule } from '../../api/rule'
import { getDeviceList } from '../../api/device'
import { getThingModel } from '../../api/thingmodel'

// Query Form & Pagination States
const queryForm = ref({
  keyword: '',
  deviceId: '',
  enabled: ''
})
const currentPage = ref(1)
const pageSize = ref(8)

// Dynamic Rules & Devices Data State
const rules = ref([])
const mockDevices = ref([])

const fetchRulesFromApi = async () => {
  try {
    const params = {}
    if (queryForm.value.keyword) params.keyword = queryForm.value.keyword.trim()
    if (queryForm.value.deviceId) params.deviceId = queryForm.value.deviceId
    if (queryForm.value.enabled !== '' && queryForm.value.enabled !== null) {
      params.enabled = queryForm.value.enabled === 'true'
    }

    const res = await getRuleList(params)
    const data = (res && res.data) ? res.data : res
    if (data) {
      const list = Array.isArray(data) ? data : (data.list || [])
      rules.value = list.map(r => {
        let acts = Array.isArray(r.actions) ? r.actions : []
        const formattedActs = acts.map(a => {
          let inputs = a.serviceInputValues || {}
          if (typeof inputs === 'string') {
            try { inputs = JSON.parse(inputs) } catch (e) { inputs = {} }
          } else if (!a.serviceInputValues && a.paramsSnapshot) {
            try { inputs = JSON.parse(a.paramsSnapshot) } catch (e) {}
          }
          const detectedType = a.actionType || (a.targetServiceCode || a.serviceName ? 'SERVICE' : (a.alarmLevel ? 'ALARM' : 'LOG'))
          return {
            actionId: a.actionId || ('act_' + Math.random().toString(36).substring(2, 8)),
            actionType: detectedType,
            actionName: a.actionName || (detectedType === 'ALARM' ? `告警[${a.alarmLevel || '紧急'}]` : (detectedType === 'LOG' ? `发布[${a.logLevel || 'INFO'}]日志` : (a.serviceName || '设备反控服务'))),
            enabled: a.enabled !== false,
            targetDeviceId: a.targetDeviceId || r.sourceDeviceId || '',
            targetServiceCode: a.targetServiceCode || a.serviceName || '',
            serviceInputValues: inputs,
            alarmLevel: a.alarmLevel || 'CRITICAL',
            alarmContent: a.alarmContent || a.content || '',
            logLevel: a.logLevel || 'INFO',
            logContent: a.logContent || a.content || ''
          }
        })
        return {
          ...r,
          ruleId: r.ruleId,
          ruleName: r.ruleName,
          sourceDeviceId: r.sourceDeviceId,
          sourcePoint: r.sourcePoint,
          conditionMode: r.conditionMode || 'SINGLE',
          operator: r.operator || '>',
          thresholdValue: r.thresholdValue !== undefined && r.thresholdValue !== null ? r.thresholdValue : 0,
          minVal: r.minVal !== undefined && r.minVal !== null ? r.minVal : 0,
          maxVal: r.maxVal !== undefined && r.maxVal !== null ? r.maxVal : 0,
          enabled: r.enabled === true || r.enabled === 1,
          actions: formattedActs
        }
      })
    }
  } catch (e) {
    console.error('Fetch rules failed', e)
    ElMessage.error('获取规则数据失败: ' + (e.message || e))
  }
}

const fetchDevicesAndThingModels = async () => {
  try {
    const devRes = await getDeviceList()
    const devList = (devRes && devRes.data) ? devRes.data : (Array.isArray(devRes) ? devRes : [])
    if (Array.isArray(devList) && devList.length > 0) {
      const loadedDevices = []
      for (const dev of devList) {
        let points = []
        let services = []
        if (dev.modelKey) {
          try {
            const tmRes = await getThingModel(dev.modelKey)
            const tm = (tmRes && tmRes.data) ? tmRes.data : tmRes
            if (tm) {
              if (Array.isArray(tm.properties)) {
                points.push(...tm.properties.map(p => ({
                  code: p.pointCode || p.identifier,
                  name: p.pointName || p.name || p.pointCode,
                  type: '属性点位',
                  unit: p.unit || '',
                  dataType: p.dataType || 'FLOAT'
                })))
              }
              if (Array.isArray(tm.events)) {
                points.push(...tm.events.map(e => ({
                  code: e.eventCode || e.identifier,
                  name: e.eventName || e.name || e.eventCode,
                  type: '物模型告警事件',
                  dataType: 'EVENT'
                })))
              }
              if (Array.isArray(tm.services)) {
                services.push(...tm.services.map(s => ({
                  code: s.serviceCode || s.identifier,
                  name: s.serviceName || s.name || s.serviceCode,
                  inputParams: (s.inputParams || []).map(ip => ({
                    code: ip.pointCode || ip.identifier,
                    name: ip.pointName || ip.name || ip.pointCode,
                    dataType: (ip.dataType || 'INT').toUpperCase(),
                    unit: ip.unit || '',
                    defaultValue: (ip.defaultValue !== undefined && ip.defaultValue !== null && ip.defaultValue !== '')
                      ? ip.defaultValue
                      : (ip.default !== undefined ? ip.default : '')
                  }))
                })))
              }
            }
          } catch (e) {
            console.warn('Load thing model for device failed:', dev.deviceId, e)
          }
        }
        loadedDevices.push({
          id: dev.deviceId,
          name: dev.deviceName || dev.deviceId,
          modelName: dev.modelKey || '未定义物模型',
          points,
          services
        })
      }
      mockDevices.value = loadedDevices
    }
  } catch (e) {
    console.warn('Fetch devices for rules failed', e)
  }
}

onMounted(() => {
  fetchRulesFromApi()
  fetchDevicesAndThingModels()
})

const handleSearch = () => {
  currentPage.value = 1
  fetchRulesFromApi()
}

const handleReset = () => {
  queryForm.value = {
    keyword: '',
    deviceId: '',
    enabled: ''
  }
  currentPage.value = 1
  fetchRulesFromApi()
}

// KPI Card Aggregations
const activeRuleCount = computed(() => rules.value.filter(r => r.enabled).length)
const totalActionCount = computed(() => rules.value.reduce((acc, r) => acc + (r.actions ? r.actions.length : 0), 0))
const coveredDeviceCount = computed(() => {
  const devs = new Set()
  rules.value.forEach(r => {
    if (r.sourceDeviceId) devs.add(r.sourceDeviceId)
    if (Array.isArray(r.actions)) {
      r.actions.forEach(a => { if (a.targetDeviceId) devs.add(a.targetDeviceId) })
    }
  })
  return devs.size
})
const disabledRuleCount = computed(() => rules.value.filter(r => !r.enabled).length)

const filteredRules = computed(() => {
  return rules.value
})

const pagedRules = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredRules.value.slice(start, end)
})

const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
}

const handleCurrentChange = (val) => {
  currentPage.value = val
}

const getDeviceName = (deviceId) => {
  const dev = mockDevices.value.find(d => d.id === deviceId)
  return dev ? dev.name : (deviceId || '未配置')
}

const getPointType = (deviceId, pointCode) => {
  const dev = mockDevices.value.find(d => d.id === deviceId)
  if (!dev) return 'PROPERTY'
  const pt = dev.points.find(p => p.code === pointCode)
  return pt ? (pt.dataType === 'EVENT' ? 'EVENT' : 'PROPERTY') : 'PROPERTY'
}

const getPointUnit = (deviceId, pointCode) => {
  const dev = mockDevices.value.find(d => d.id === deviceId)
  if (!dev) return ''
  const pt = dev.points.find(p => p.code === pointCode)
  return pt ? (pt.unit || '') : ''
}

// Modal 1: Rule Form State
const ruleModalVisible = ref(false)
const isEditRule = ref(false)
const ruleForm = ref({
  ruleId: '',
  ruleName: '',
  sourceDeviceId: '',
  sourcePoint: '',
  conditionMode: 'SINGLE',
  operator: '>=',
  thresholdValue: 80.0,
  minVal: 20.0,
  maxVal: 80.0,
  enabled: true,
  actions: []
})

const availableSourcePoints = computed(() => {
  const dev = mockDevices.value.find(d => d.id === ruleForm.value.sourceDeviceId)
  return dev ? dev.points : []
})

const selectedSourcePointObj = computed(() => {
  return availableSourcePoints.value.find(p => p.code === ruleForm.value.sourcePoint)
})

const onSourceDeviceChange = (devId) => {
  const dev = mockDevices.value.find(d => d.id === devId)
  if (dev && dev.points.length > 0) {
    ruleForm.value.sourcePoint = dev.points[0].code
  } else {
    ruleForm.value.sourcePoint = ''
  }
}

const openAddRuleModal = () => {
  isEditRule.value = false
  const defaultDevId = mockDevices.value.length > 0 ? mockDevices.value[0].id : ''
  const defaultDev = mockDevices.value.find(d => d.id === defaultDevId)
  const defaultPoint = (defaultDev && defaultDev.points.length > 0) ? defaultDev.points[0].code : ''
  ruleForm.value = {
    ruleId: 'RULE_' + Date.now().toString().slice(-6),
    ruleName: '',
    sourceDeviceId: defaultDevId,
    sourcePoint: defaultPoint,
    conditionMode: 'SINGLE',
    operator: '>=',
    thresholdValue: 80.0,
    minVal: 20.0,
    maxVal: 80.0,
    enabled: true,
    actions: []
  }
  ruleModalVisible.value = true
}

const openEditRuleModal = (rule) => {
  isEditRule.value = true
  ruleForm.value = {
    ...rule,
    actions: [...(rule.actions || [])]
  }
  ruleModalVisible.value = true
}

const submitSaveRule = async () => {
  if (!ruleForm.value.ruleName) {
    ElMessage.warning('请输入规则名称')
    return
  }
  if (!ruleForm.value.sourceDeviceId) {
    ElMessage.warning('请选择监控设备')
    return
  }
  if (!ruleForm.value.sourcePoint) {
    ElMessage.warning('请选择监控点位/事件')
    return
  }

  const payload = {
    ...ruleForm.value,
    createTime: undefined,
    updateTime: undefined,
    actions: (ruleForm.value.actions || []).map(a => ({
      ...a,
      serviceInputValues: typeof a.serviceInputValues === 'object' ? JSON.stringify(a.serviceInputValues) : a.serviceInputValues
    }))
  }

  try {
    if (isEditRule.value) {
      await updateRule(ruleForm.value.ruleId, payload)
      ElMessage.success('规则定义更新成功')
    } else {
      await addRule(payload)
      ElMessage.success('新建规则成功')
    }
    ruleModalVisible.value = false
    await fetchRulesFromApi()
  } catch (err) {
    console.error('Save rule failed:', err)
    ElMessage.error('保存规则失败: ' + (err.message || err))
  }
}

const toggleMasterRule = async (rule) => {
  try {
    await apiToggleRule(rule.ruleId, rule.enabled)
    ElMessage.success(`规则 [${rule.ruleName}] 已${rule.enabled ? '开启' : '停用'}`)
  } catch (err) {
    console.error('Toggle rule failed:', err)
    rule.enabled = !rule.enabled
    ElMessage.error('切换规则状态失败: ' + (err.message || err))
  }
}

const deleteRule = (rule) => {
  ElMessageBox.confirm(`确定删除规则 [${rule.ruleName}] 吗？`, '提示', { type: 'warning' }).then(async () => {
    try {
      await apiDeleteRule(rule.ruleId)
      ElMessage.success('删除规则成功')
      await fetchRulesFromApi()
    } catch (err) {
      console.error('Delete rule failed:', err)
      ElMessage.error('删除规则失败: ' + (err.message || err))
    }
  }).catch(() => {})
}

// Modal 2: Dedicated Linkages Management Modal for a Rule
const linkagesModalVisible = ref(false)
const activeRuleContext = ref(null)

const openLinkagesManageModal = (rule) => {
  activeRuleContext.value = rule
  linkagesModalVisible.value = true
}

const saveCurrentRuleActionsToBackend = async (rule) => {
  try {
    const payload = {
      ...rule,
      createTime: undefined,
      updateTime: undefined,
      actions: (rule.actions || []).map(a => ({
        ...a,
        serviceInputValues: typeof a.serviceInputValues === 'object' ? JSON.stringify(a.serviceInputValues) : a.serviceInputValues
      }))
    }
    await updateRule(rule.ruleId, payload)
    await fetchRulesFromApi()
    const updated = rules.value.find(r => r.ruleId === rule.ruleId)
    if (updated) {
      activeRuleContext.value = updated
    }
  } catch (err) {
    console.error('Save rule actions failed:', err)
    ElMessage.error('保存联动动作失败: ' + (err.message || err))
  }
}

// Modal 3: Add/Edit Single Control Service Linkage Action Item Form
const actionEditModalVisible = ref(false)
const isEditActionItem = ref(false)
const actionForm = ref({
  actionId: '',
  actionType: 'SERVICE',
  actionName: '',
  enabled: true,
  targetDeviceId: '',
  targetServiceCode: '',
  serviceInputValues: {},
  alarmLevel: 'CRITICAL',
  alarmContent: '',
  logLevel: 'INFO',
  logContent: ''
})

const availableActionTargetServices = computed(() => {
  const dev = mockDevices.value.find(d => d.id === actionForm.value.targetDeviceId)
  return dev ? dev.services : []
})

const selectedActionServiceSchema = computed(() => {
  return availableActionTargetServices.value.find(s => s.code === actionForm.value.targetServiceCode)
})

const onActionTargetDeviceChange = (devId) => {
  const dev = mockDevices.value.find(d => d.id === devId)
  if (dev && dev.services.length > 0) {
    actionForm.value.targetServiceCode = dev.services[0].code
    populateDefaultActionServiceInputs()
  } else {
    actionForm.value.targetServiceCode = ''
    actionForm.value.serviceInputValues = {}
  }
}

const onActionTargetServiceChange = () => {
  populateDefaultActionServiceInputs()
}

const populateDefaultActionServiceInputs = () => {
  const schema = selectedActionServiceSchema.value
  const newValues = {}
  if (schema && schema.inputParams) {
    schema.inputParams.forEach(p => {
      newValues[p.code] = p.defaultValue !== undefined ? p.defaultValue : ''
    })
  }
  actionForm.value.serviceInputValues = newValues
}

const openAddActionModal = () => {
  if (!activeRuleContext.value) return
  isEditActionItem.value = false
  const defaultDevId = activeRuleContext.value.sourceDeviceId || (mockDevices.value.length > 0 ? mockDevices.value[0].id : '')
  const defaultDev = mockDevices.value.find(d => d.id === defaultDevId)
  const defaultSvc = (defaultDev && defaultDev.services.length > 0) ? defaultDev.services[0].code : ''
  actionForm.value = {
    actionId: 'act_' + Date.now().toString().slice(-6),
    actionType: 'SERVICE',
    actionName: '',
    enabled: true,
    targetDeviceId: defaultDevId,
    targetServiceCode: defaultSvc,
    serviceInputValues: {},
    alarmLevel: 'CRITICAL',
    alarmContent: '',
    logLevel: 'INFO',
    logContent: ''
  }
  populateDefaultActionServiceInputs()
  actionEditModalVisible.value = true
}

const openEditActionModal = (act) => {
  isEditActionItem.value = true
  actionForm.value = {
    actionType: 'SERVICE',
    alarmLevel: 'CRITICAL',
    logLevel: 'INFO',
    alarmContent: '',
    logContent: '',
    ...act,
    serviceInputValues: { ...(act.serviceInputValues || {}) }
  }
  actionEditModalVisible.value = true
}

const submitSaveActionItem = async () => {
  if (!actionForm.value.actionName) {
    ElMessage.warning('请输入联动动作名称')
    return
  }
  const rule = activeRuleContext.value
  if (!rule) return

  if (isEditActionItem.value) {
    const idx = rule.actions.findIndex(a => a.actionId === actionForm.value.actionId)
    if (idx !== -1) {
      rule.actions[idx] = { ...actionForm.value }
    }
  } else {
    rule.actions.push({ ...actionForm.value })
  }
  actionEditModalVisible.value = false
  await saveCurrentRuleActionsToBackend(rule)
  ElMessage.success('控制服务反控动作配置成功')
}

const toggleActionItem = async (rule, act) => {
  await saveCurrentRuleActionsToBackend(rule)
  ElMessage.info(`动作 [${act.actionName}] 已${act.enabled ? '启用' : '禁用'}`)
}

const deleteActionItem = async (rule, act) => {
  rule.actions = rule.actions.filter(a => a.actionId !== act.actionId)
  await saveCurrentRuleActionsToBackend(rule)
  ElMessage.success(`动作 [${act.actionName}] 已移除`)
}
</script>

<style scoped>
.page-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title-box {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.header-subtitle {
  font-size: 0.85rem;
  color: var(--iot-text-secondary);
}

.header-action-group {
  display: flex;
  gap: 10px;
  align-items: center;
}

.add-btn-flex {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

/* Metric KPI Cards with Rich Modern Linear Gradients */
.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
}

.metric-card {
  padding: 18px 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  border-radius: 10px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.metric-card:hover {
  transform: translateY(-2px);
}

.grad-cyan {
  background: linear-gradient(135deg, rgba(6, 182, 212, 0.16) 0%, rgba(15, 23, 42, 0.88) 100%);
  border: 1px solid rgba(6, 182, 212, 0.4);
  border-left: 4px solid #06b6d4;
  box-shadow: 0 4px 20px rgba(6, 182, 212, 0.14);
}

.grad-green {
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.16) 0%, rgba(15, 23, 42, 0.88) 100%);
  border: 1px solid rgba(16, 185, 129, 0.4);
  border-left: 4px solid #10b981;
  box-shadow: 0 4px 20px rgba(16, 185, 129, 0.14);
}

.grad-indigo {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.16) 0%, rgba(15, 23, 42, 0.88) 100%);
  border: 1px solid rgba(99, 102, 241, 0.4);
  border-left: 4px solid #818cf8;
  box-shadow: 0 4px 20px rgba(99, 102, 241, 0.14);
}

.grad-orange {
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.16) 0%, rgba(15, 23, 42, 0.88) 100%);
  border: 1px solid rgba(245, 158, 11, 0.4);
  border-left: 4px solid #f59e0b;
  box-shadow: 0 4px 20px rgba(245, 158, 11, 0.14);
}

.metric-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.icon-cyan { background: rgba(6, 182, 212, 0.22); }
.icon-green { background: rgba(16, 185, 129, 0.22); }
.icon-indigo { background: rgba(99, 102, 241, 0.22); }
.icon-orange { background: rgba(245, 158, 11, 0.22); }

.metric-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.metric-label {
  font-size: 0.85rem;
  color: #94a3b8;
  font-weight: 500;
}

.metric-val-wrap {
  display: flex;
  align-items: baseline;
  gap: 6px;
}

.metric-value {
  font-size: 1.7rem;
  font-weight: 700;
  line-height: 1;
}

.metric-unit {
  font-size: 0.82rem;
  color: #64748b;
}

.text-cyan { color: #38bdf8; }
.text-green { color: #34d399; }
.text-indigo { color: #a5b4fc; }
.text-orange { color: #fbbf24; }

/* Filter Card */
.filter-card {
  padding: 14px 20px;
}

.filter-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
}

.search-counter {
  margin-left: auto;
  font-size: 0.88rem;
  color: var(--iot-text-secondary);
}

.search-counter strong {
  color: var(--iot-color-cyan);
}

/* Robust Responsive Rule Cards Grid with Vibrant Linear Gradients */
.rule-cards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
  gap: 18px;
}

.rule-card {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 18px 20px;
  background: linear-gradient(145deg, rgba(30, 41, 59, 0.75) 0%, rgba(15, 23, 42, 0.9) 100%);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-top: 3px solid var(--iot-color-cyan);
  border-radius: 10px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  min-width: 0;
  backdrop-filter: blur(8px);
}

.rule-card:hover {
  border-color: var(--iot-color-cyan);
  border-top-color: #38bdf8;
  background: linear-gradient(145deg, rgba(30, 41, 59, 0.95) 0%, rgba(15, 23, 42, 0.98) 100%);
  box-shadow: 0 8px 28px rgba(6, 182, 212, 0.22);
  transform: translateY(-3px);
}

.rule-card.is-disabled {
  opacity: 0.65;
  filter: grayscale(0.2);
  border-top-color: #64748b;
}

.card-top-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 12px;
  padding-bottom: 10px;
  border-bottom: 1px dashed rgba(255, 255, 255, 0.1);
}

.rule-title-group {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.rule-id-badge {
  font-size: 0.75rem;
  font-family: monospace;
  color: var(--iot-color-cyan);
  background: rgba(6, 182, 212, 0.12);
  padding: 1px 6px;
  border-radius: 4px;
  border: 1px solid rgba(6, 182, 212, 0.25);
  width: fit-content;
}

.rule-title {
  font-size: 1rem;
  font-weight: 600;
  color: var(--iot-text-primary);
  margin: 0;
  line-height: 1.45;
  word-break: break-word;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.rule-status-switch {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
}

.switch-label {
  font-size: 0.8rem;
  color: var(--iot-text-secondary);
  white-space: nowrap;
}

.rule-card-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 14px;
}

.rule-info-row {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.row-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.8rem;
  color: var(--iot-text-secondary);
}

.row-value {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  font-size: 0.88rem;
  background: rgba(15, 23, 42, 0.6);
  padding: 8px 12px;
  border-radius: 6px;
  border: 1px solid rgba(255, 255, 255, 0.06);
  min-width: 0;
}

.device-tag {
  max-width: 150px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.point-code {
  color: var(--iot-color-cyan);
  font-family: monospace;
  max-width: 120px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.operator-badge {
  font-weight: 700;
  color: var(--iot-color-red);
}

.threshold-val {
  font-weight: 600;
  color: var(--iot-text-primary);
}

.range-text code {
  color: var(--iot-color-cyan);
  font-weight: 600;
}

.margin-left-mini {
  margin-left: 4px;
}

.linkage-summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.actions-badge-wrapper {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  max-height: 72px;
  overflow-y: auto;
  flex: 1;
}

.action-summary-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  max-width: 160px;
}

.dot-indicator {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: var(--iot-text-muted);
}

.dot-indicator.is-active {
  background-color: var(--iot-color-green);
  box-shadow: 0 0 6px var(--iot-color-green);
}

.tag-text-ellipsis {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.config-actions-btn {
  white-space: nowrap;
  flex-shrink: 0;
}

.rule-card-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding-top: 10px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}

.empty-card {
  padding: 30px;
  text-align: center;
}

/* Pagination Card */
.pagination-card {
  padding: 12px 20px;
  display: flex;
  justify-content: flex-end;
}

/* Linkages Modal */
.linkages-manage-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.linkage-modal-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--iot-bg-tertiary);
  padding: 10px 14px;
  border-radius: 6px;
  border: 1px solid var(--iot-border-color);
  font-size: 0.85rem;
}

.banner-info-item .label {
  color: var(--iot-text-secondary);
  margin-right: 4px;
}

.text-cyan {
  color: var(--iot-color-cyan);
}

.table-service-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  font-size: 0.82rem;
}

.margin-left-small {
  margin-left: 8px;
}

.table-params-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-top: 2px;
}

.mini-param-tag {
  font-size: 0.72rem;
}

/* Modal Form Styles */
.form-section-title {
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--iot-color-cyan);
  background: var(--iot-bg-tertiary);
  padding: 6px 12px;
  border-radius: 4px;
  margin: 12px 0 8px 0;
  border-left: 3px solid var(--iot-color-cyan);
}

.threshold-input-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.unit-text {
  font-size: 0.85rem;
  color: var(--iot-text-secondary);
}

.schema-params-box {
  background: var(--iot-bg-tertiary);
  border: 1px solid var(--iot-border-color);
  border-radius: 6px;
  padding: 12px;
  margin-top: 8px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.schema-box-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.85rem;
  color: var(--iot-text-primary);
  padding-bottom: 6px;
  border-bottom: 1px solid var(--iot-border-color);
}

.param-form-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 0.85rem;
}

.param-label {
  color: var(--iot-text-secondary);
}

.param-label code {
  color: var(--iot-color-cyan);
}

.font-weight-600 {
  font-weight: 600;
}

.text-muted {
  color: var(--iot-text-muted);
}
</style>
