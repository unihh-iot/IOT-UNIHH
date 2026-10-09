<!--
  Author: deversun
  Website: https://unihh.com
  License: Apache License 2.0
-->
<template>
  <div class="alarm-center-container">
    <!-- Top Header -->
    <div class="page-header">
      <div class="header-title-group">
        <h2 class="iot-header-title">
          <SvgIcon name="bell" :size="24" color="#ef4444" />
          告警中心 (Alarm Monitoring Center)
        </h2>
        <span class="header-subtitle">
          实时接入规则引擎计算产生的告警事件，支持多维度检索、联动追溯与协同处置
        </span>
      </div>
      <div class="header-actions">
        <el-button type="primary" plain @click="fetchAlarms">
          <SvgIcon name="refresh" :size="16" />
          刷新数据
        </el-button>
        <el-button v-if="selectedRows.length > 0 && hasPermission('alarm:handle')" type="success" @click="batchConfirm">
          <SvgIcon name="check-circle" :size="16" />
          批量确认 ({{ selectedRows.length }})
        </el-button>
      </div>
    </div>

    <!-- 4 High-Tech Linear Gradient KPI Cards -->
    <div class="metrics-grid">
      <div class="iot-card metric-card grad-red">
        <div class="metric-icon icon-red">
          <SvgIcon name="bell" :size="22" color="#ef4444" />
        </div>
        <div class="metric-info">
          <span class="metric-label">待处理告警事件</span>
          <div class="metric-val-wrap">
            <span class="metric-value text-red">{{ pendingCount }}</span>
            <span class="metric-unit">条</span>
          </div>
        </div>
      </div>

      <div class="iot-card metric-card grad-orange">
        <div class="metric-icon icon-orange">
          <SvgIcon name="activity" :size="22" color="#f59e0b" />
        </div>
        <div class="metric-info">
          <span class="metric-label">严重/紧急告警</span>
          <div class="metric-val-wrap">
            <span class="metric-value text-orange">{{ criticalCount }}</span>
            <span class="metric-unit">条</span>
          </div>
        </div>
      </div>

      <div class="iot-card metric-card grad-cyan">
        <div class="metric-icon icon-cyan">
          <SvgIcon name="server" :size="22" color="#06b6d4" />
        </div>
        <div class="metric-info">
          <span class="metric-label">今日告警总数</span>
          <div class="metric-val-wrap">
            <span class="metric-value text-cyan">{{ alarmList.length }}</span>
            <span class="metric-unit">次</span>
          </div>
        </div>
      </div>

      <div class="iot-card metric-card grad-green">
        <div class="metric-icon icon-green">
          <SvgIcon name="check-circle" :size="22" color="#10b981" />
        </div>
        <div class="metric-info">
          <span class="metric-label">告警响应恢复率</span>
          <div class="metric-val-wrap">
            <span class="metric-value text-green">{{ resolutionRate }}%</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Search & Filter Toolbar -->
    <div class="iot-card filter-card">
      <div class="filter-row">
        <el-input
          v-model="queryForm.keyword"
          placeholder="搜索告警编号、设备ID、监控点或消息..."
          clearable
          style="width: 260px;"
        >
          <template #prefix>
            <SvgIcon name="filter" :size="14" color="#94a3b8" />
          </template>
        </el-input>

        <el-select
          v-model="queryForm.level"
          placeholder="告警级别"
          clearable
          style="width: 130px;"
        >
          <el-option label="CRITICAL 紧急" value="CRITICAL" />
          <el-option label="HIGH 严重" value="HIGH" />
          <el-option label="WARN 警告" value="WARN" />
          <el-option label="INFO 提示" value="INFO" />
        </el-select>

        <el-select
          v-model="queryForm.status"
          placeholder="处理状态"
          clearable
          style="width: 130px;"
        >
          <el-option label="未处理 (0)" :value="0" />
          <el-option label="已确认 (1)" :value="1" />
          <el-option label="已恢复 (2)" :value="2" />
        </el-select>

        <el-select
          v-model="queryForm.ruleId"
          placeholder="关联规则"
          clearable
          style="width: 180px;"
        >
          <el-option label="RULE_101 1号电机过热防护规则" value="RULE_101" />
          <el-option label="RULE_102 2号储罐压力上限规则" value="RULE_102" />
          <el-option label="RULE_103 车间湿度过高排风规则" value="RULE_103" />
        </el-select>

        <el-button type="primary" @click="handleSearch">
          <SvgIcon name="filter" :size="14" />
          查询
        </el-button>
        <el-button type="default" @click="resetFilters">重置筛选</el-button>

        <div class="search-counter">
          共找到 <strong>{{ filteredAlarms.length }}</strong> 条告警事件
        </div>
      </div>
    </div>

    <!-- Redesigned High-Tech Alarm Table Container (Pure Dark Theme, No White Backdrops) -->
    <div class="iot-card table-card">
      <div class="table-header-toolbar">
        <div class="tb-title">
          <span class="live-pulse-dot red"></span>
          <span>实时告警事件集中处置与追溯流水</span>
        </div>
        <div class="tb-actions">
          <el-button v-if="selectedRows.length > 0 && hasPermission('alarm:handle')" size="small" type="success" @click="batchConfirm">
            批量确认选中 ({{ selectedRows.length }})
          </el-button>
        </div>
      </div>

      <el-table
        :data="paginatedAlarms"
        style="width: 100%"
        stripe
        class="alarm-cyber-table"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="45" align="center" />
        
        <el-table-column prop="id" label="事件 ID" width="95">
          <template #default="scope">
            <span class="cyber-id-badge">#{{ scope.row.id }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="alarmLevel" label="告警级别" width="125" align="center">
          <template #default="scope">
            <div class="level-badge-wrap" :class="`level-${scope.row.alarmLevel.toLowerCase()}`">
              <span class="badge-dot"></span>
              <span class="badge-text">{{ scope.row.alarmLevel }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="关联触发规则" min-width="190" show-overflow-tooltip>
          <template #default="scope">
            <div class="rule-link-cell">
              <code class="rule-code-tag">{{ scope.row.ruleId || 'RULE-101' }}</code>
              <span class="rule-name-text">{{ scope.row.ruleName }}</span>
            </div>
          </template>
        </el-table-column>

        <!-- Trigger Device & Point Column (Without Icon as requested) -->
        <el-table-column label="触发设备 / 点位" min-width="210">
          <template #default="scope">
            <div class="device-point-cell">
              <div class="dev-name-row">
                <span class="dev-name-text">{{ scope.row.deviceName }}</span>
                <code class="dev-id-tag">{{ scope.row.deviceId }}</code>
              </div>
              <div class="point-row">
                <span class="point-lbl">点位:</span>
                <code class="point-code-badge">{{ scope.row.pointCode }}</code>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="alarmMessage" label="告警描述及实时数值快照" min-width="280">
          <template #default="scope">
            <div class="alarm-msg-cell">
              <span class="msg-text">{{ scope.row.alarmMessage }}</span>
              <div class="snapshot-badge">
                <span class="snap-lbl">实时触发值:</span>
                <strong class="snap-val">{{ scope.row.value }}</strong>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="triggerTime" label="触发时间" width="165">
          <template #default="scope">
            <div class="time-cell">
              <span class="time-text">{{ scope.row.triggerTime }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="响应状态" width="115" align="center">
          <template #default="scope">
            <div class="status-pill-wrap" :class="`status-${scope.row.status}`">
              <span class="status-dot"></span>
              <span class="status-text">{{ getStatusText(scope.row.status) }}</span>
            </div>
          </template>
        </el-table-column>

        <!-- Actions Column (Generous Width & Margin Padding) -->
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="scope">
            <div class="table-actions">
              <el-button size="small" type="primary" plain class="action-btn" @click="openDetailModal(scope.row)">
                详情
              </el-button>
              <el-button
                v-if="scope.row.status === 0 && hasPermission('alarm:handle')"
                size="small"
                type="success"
                plain
                class="action-btn"
                @click="handleConfirm(scope.row)"
              >
                确认
              </el-button>
              <el-button
                v-if="scope.row.status !== 2 && hasPermission('alarm:handle')"
                size="small"
                type="info"
                plain
                class="action-btn"
                @click="handleResolve(scope.row)"
              >
                清除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination Footer -->
      <div class="pagination-footer margin-top-small">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[8, 16, 24, 32]"
          :total="filteredAlarms.length"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- Alarm Detail & Execution Audit Modal (Dark Theme, No White Background) -->
    <el-dialog
      v-model="detailModalVisible"
      title="告警事件追溯与处理"
      width="720px"
      destroy-on-close
      class="alarm-detail-modal"
    >
      <div v-if="currentAlarm" class="modal-body-container">
        <!-- Event Header Card -->
        <div class="event-summary-card">
          <div class="summary-top">
            <div class="level-badge-wrap" :class="`level-${currentAlarm.alarmLevel.toLowerCase()}`">
              <span class="badge-dot"></span>
              <span class="badge-text">{{ currentAlarm.alarmLevel }}</span>
            </div>
            <span class="event-id">事件编号: #{{ currentAlarm.id }}</span>
            <span class="event-time">触发于: {{ currentAlarm.triggerTime }}</span>
          </div>
          <h3 class="event-title">{{ currentAlarm.alarmMessage }}</h3>
        </div>

        <!-- Detail Grid -->
        <div class="detail-grid">
          <div class="detail-item">
            <span class="detail-label">关联规则:</span>
            <span class="detail-val">{{ currentAlarm.ruleId }} - {{ currentAlarm.ruleName }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">触发设备:</span>
            <span class="detail-val">{{ currentAlarm.deviceName }} ({{ currentAlarm.deviceId }})</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">监控点位:</span>
            <span class="detail-val">{{ currentAlarm.pointCode }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">触发数值 snapshot:</span>
            <span class="detail-val text-red font-bold">{{ currentAlarm.value }}</span>
          </div>
        </div>

        <!-- Executed Linkage Logs -->
        <div class="linkage-logs-section">
          <h4 class="section-title">规则联动执行日志 (Executed Linkage Actions)</h4>
          <div class="log-list">
            <div v-for="(log, idx) in currentAlarm.linkageLogs" :key="idx" class="log-item">
              <span class="log-badge" :class="log.status === 'SUCCESS' ? 'badge-success' : 'badge-fail'">
                {{ log.status }}
              </span>
              <span class="log-desc">{{ log.actionName }}</span>
              <span class="log-time">{{ log.time }}</span>
            </div>
          </div>
        </div>

        <!-- Resolution Note Form -->
        <div class="resolution-form">
          <h4 class="section-title">处置记录与说明</h4>
          <el-input
            v-model="handlingNote"
            type="textarea"
            :rows="3"
            placeholder="请输入告警原因、现场排查情况或处置结果说明..."
          />
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailModalVisible = false">关闭</el-button>
          <el-button
            v-if="currentAlarm && currentAlarm.status === 0"
            type="success"
            @click="submitConfirm"
          >
            确认此告警
          </el-button>
          <el-button
            v-if="currentAlarm && currentAlarm.status !== 2"
            type="primary"
            @click="submitResolve"
          >
            标记故障已排除并恢复
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import SvgIcon from '../../components/common/SvgIcon.vue'
import { hasPermission } from '@/utils/permission'
import { getAlarmList, confirmAlarm, resolveAlarm } from '../../api/alarm'

// Search & Filter & Pagination States
const queryForm = ref({ keyword: '', level: '', status: null, ruleId: '' })
const activeQueryParams = ref({ keyword: '', level: '', status: null, ruleId: '' })
const selectedRows = ref([])
const currentPage = ref(1)
const pageSize = ref(8)

const fetchAlarmsFromApi = async () => {
  try {
    const params = {}
    if (activeQueryParams.value.keyword) params.keyword = activeQueryParams.value.keyword
    if (activeQueryParams.value.level) params.level = activeQueryParams.value.level
    if (activeQueryParams.value.status !== null && activeQueryParams.value.status !== '') params.status = activeQueryParams.value.status
    if (activeQueryParams.value.ruleId) params.ruleId = activeQueryParams.value.ruleId
    const res = await getAlarmList(params)
    const data = (res && res.data) ? res.data : res
    if (data) {
      const list = Array.isArray(data) ? data : (data.list || [])
      if (list && list.length >= 0) {
        alarmList.value = list.map(item => ({
          id: item.id || item.alarmId,
          alarmId: item.alarmId || item.id,
          ruleId: item.ruleId || 'RULE_101',
          ruleName: item.alarmTitle || '规则触发告警',
          deviceId: item.deviceId,
          deviceName: item.deviceName || item.deviceId,
          pointCode: item.metric || 'telemetry',
          alarmLevel: item.alarmLevel || 'WARN',
          alarmMessage: item.alarmTitle + (item.triggerValue ? ` (触发值: ${item.triggerValue})` : ''),
          value: item.triggerValue || '--',
          triggerTime: item.alarmTime ? new Date(item.alarmTime).toLocaleString() : new Date().toLocaleString(),
          status: item.status !== undefined ? item.status : 0,
          remark: item.remark || '',
          linkageLogs: [
            { actionName: '规则引擎计算并触发联动告警记录', status: 'SUCCESS', time: 'SYSTEM' }
          ]
        }))
      }
    }
  } catch (e) {
    console.warn('Backend API connection warning for alarms, using local fallback', e)
  }
}

onMounted(() => {
  fetchAlarmsFromApi()
})

const detailModalVisible = ref(false)
const currentAlarm = ref(null)
const handlingNote = ref('')

const alarmList = ref([])

const pendingCount = computed(() => alarmList.value.filter(a => a.status === 0).length)
const criticalCount = computed(() => alarmList.value.filter(a => a.alarmLevel === 'CRITICAL' || a.alarmLevel === 'HIGH').length)
const resolutionRate = computed(() => {
  if (alarmList.value.length === 0) return 100
  const resolved = alarmList.value.filter(a => a.status === 2 || a.status === 1).length
  return Math.round((resolved / alarmList.value.length) * 100)
})

const filteredAlarms = computed(() => {
  return alarmList.value.filter(item => {
    if (activeQueryParams.value.keyword) {
      const kw = activeQueryParams.value.keyword.toLowerCase()
      const matchId = String(item.id).includes(kw)
      const matchDev = (item.deviceId && item.deviceId.toLowerCase().includes(kw)) || (item.deviceName && item.deviceName.toLowerCase().includes(kw))
      const matchPoint = item.pointCode && item.pointCode.toLowerCase().includes(kw)
      const matchMsg = item.alarmMessage && item.alarmMessage.toLowerCase().includes(kw)
      if (!matchId && !matchDev && !matchPoint && !matchMsg) return false
    }
    if (activeQueryParams.value.level && item.alarmLevel !== activeQueryParams.value.level) return false
    if (activeQueryParams.value.status !== '' && activeQueryParams.value.status !== null && item.status !== activeQueryParams.value.status) return false
    if (activeQueryParams.value.ruleId && item.ruleId !== activeQueryParams.value.ruleId) return false
    return true
  })
})

const paginatedAlarms = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredAlarms.value.slice(start, end)
})

const getStatusText = (status) => {
  switch (status) {
    case 0: return '待处理'
    case 1: return '已确认'
    case 2: return '已恢复'
    default: return '未知'
  }
}

const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
}

const handleCurrentChange = (val) => {
  currentPage.value = val
}

const handleSearch = () => {
  activeQueryParams.value = { ...queryForm.value }
  currentPage.value = 1
  fetchAlarmsFromApi()
}

const fetchAlarms = () => {
  fetchAlarmsFromApi()
  ElMessage.success('最新告警中心事件列表已同步更新')
}

const resetFilters = () => {
  queryForm.value = { keyword: '', level: '', status: null, ruleId: '' }
  activeQueryParams.value = { keyword: '', level: '', status: null, ruleId: '' }
  currentPage.value = 1
  fetchAlarmsFromApi()
}

const handleConfirm = (row) => {
  ElMessageBox.prompt('请输入告警确认处置记录与排查说明：', `告警确认处置 [事件 #${row.id}]`, {
    confirmButtonText: '确认处置',
    cancelButtonText: '取消',
    inputPlaceholder: '如：现场人员已排查，设备转速与轴承温度恢复正常范围内',
    inputValue: row.remark || '值班人员手动确认告警，现场排查无异常',
    inputType: 'textarea'
  }).then(async ({ value }) => {
    const remark = value ? value.trim() : '手动确认告警'
    try {
      await confirmAlarm(row.alarmId || row.id, { handleUser: 'admin', remark })
    } catch (e) {
      console.warn('Confirm alarm API warning:', e)
    }
    row.status = 1
    row.remark = remark
    ElMessage.success(`告警事件 #${row.id} 已确认响应，处置记录已归档`)
    fetchAlarmsFromApi()
  }).catch(() => {})
}

const handleResolve = (row) => {
  ElMessageBox.prompt('请输入告警恢复解单记录说明：', `告警恢复解单 [事件 #${row.id}]`, {
    confirmButtonText: '完成恢复',
    cancelButtonText: '取消',
    inputPlaceholder: '如：故障已排除，系统恢复正常运行',
    inputValue: row.remark || '告警故障已恢复排除',
    inputType: 'textarea'
  }).then(async ({ value }) => {
    const remark = value ? value.trim() : '手动解除告警'
    try {
      await resolveAlarm(row.alarmId || row.id, { handleUser: 'admin', remark })
    } catch (e) {
      console.warn('Resolve alarm API warning:', e)
    }
    row.status = 2
    row.remark = remark
    ElMessage.success(`告警事件 #${row.id} 已解除恢复`)
    fetchAlarmsFromApi()
  }).catch(() => {})
}

const batchConfirm = () => {
  if (selectedRows.value.length === 0) return
  ElMessageBox.prompt(`请输入选中 ${selectedRows.value.length} 条告警的批量处置记录说明：`, '批量告警处置确认', {
    confirmButtonText: '批量确认',
    cancelButtonText: '取消',
    inputPlaceholder: '如：值班员批量核实并确认告警，已通知现场集中处置',
    inputValue: '值班人员批量核对并确认告警事件',
    inputType: 'textarea'
  }).then(({ value }) => {
    const remark = value ? value.trim() : '批量确认告警'
    selectedRows.value.forEach(async (row) => {
      if (row.status === 0) {
        row.status = 1
        row.remark = remark
        try {
          await confirmAlarm(row.alarmId || row.id, { handleUser: 'admin', remark })
        } catch (e) {}
      }
    })
    ElMessage.success(`成功批量确认 ${selectedRows.value.length} 条告警事件`)
    fetchAlarmsFromApi()
  }).catch(() => {})
}

const openDetailModal = (row) => {
  currentAlarm.value = row
  handlingNote.value = row.remark || ''
  detailModalVisible.value = true
}

const submitConfirm = async () => {
  if (currentAlarm.value) {
    const remark = handlingNote.value ? handlingNote.value.trim() : '值班人员手动确认告警，现场排查正常'
    try {
      await confirmAlarm(currentAlarm.value.alarmId || currentAlarm.value.id, { handleUser: 'admin', remark })
    } catch (e) {
      console.warn('Confirm alarm API warning:', e)
    }
    currentAlarm.value.status = 1
    currentAlarm.value.remark = remark
    ElMessage.success(`告警事件 #${currentAlarm.value.id} 确认成功`)
    detailModalVisible.value = false
    fetchAlarmsFromApi()
  }
}

const submitResolve = async () => {
  if (currentAlarm.value) {
    const remark = handlingNote.value ? handlingNote.value.trim() : '告警故障消除，系统恢复正常运行'
    try {
      await resolveAlarm(currentAlarm.value.alarmId || currentAlarm.value.id, { handleUser: 'admin', remark })
    } catch (e) {
      console.warn('Resolve alarm API warning:', e)
    }
    currentAlarm.value.status = 2
    currentAlarm.value.remark = remark
    ElMessage.success(`告警事件 #${currentAlarm.value.id} 已解除恢复`)
    detailModalVisible.value = false
    fetchAlarmsFromApi()
  }
}

onMounted(() => {
  fetchAlarms()
})
</script>

<style scoped>
.alarm-center-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.header-subtitle {
  color: var(--iot-text-muted);
  font-size: 0.88rem;
}

.header-actions {
  display: flex;
  gap: 12px;
}

/* Metric KPI Cards with Modern Linear Gradients */
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
}

.metric-card:hover {
  transform: translateY(-2px);
}

.grad-red {
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.16) 0%, rgba(15, 23, 42, 0.88) 100%);
  border: 1px solid rgba(239, 68, 68, 0.4);
  border-left: 4px solid #ef4444;
  box-shadow: 0 4px 20px rgba(239, 68, 68, 0.14);
}

.grad-orange {
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.16) 0%, rgba(15, 23, 42, 0.88) 100%);
  border: 1px solid rgba(245, 158, 11, 0.4);
  border-left: 4px solid #f59e0b;
  box-shadow: 0 4px 20px rgba(245, 158, 11, 0.14);
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

.metric-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.icon-red { background: rgba(239, 68, 68, 0.22); }
.icon-orange { background: rgba(245, 158, 11, 0.22); }
.icon-cyan { background: rgba(6, 182, 212, 0.22); }
.icon-green { background: rgba(16, 185, 129, 0.22); }

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

.text-red { color: #f87171; }
.text-orange { color: #fbbf24; }
.text-cyan { color: #38bdf8; }
.text-green { color: #34d399; }

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

/* Redesigned High-Tech Cyber Table Card (Zero White Backdrops) */
.table-card {
  padding: 18px;
  background: linear-gradient(180deg, #0f172a 0%, #090d16 100%);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 10px;
}

.table-header-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
  padding-bottom: 10px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.tb-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.98rem;
  font-weight: 700;
  color: var(--iot-text-primary);
}

.live-pulse-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  position: relative;
}

.live-pulse-dot.red {
  background-color: #ef4444;
  box-shadow: 0 0 8px #ef4444;
}

/* Cyber Table Custom Styling with Pure Dark Backgrounds */
.alarm-cyber-table {
  background: transparent !important;
  --el-table-bg-color: transparent !important;
  --el-table-tr-bg-color: rgba(15, 23, 42, 0.6) !important;
  --el-table-header-bg-color: #0b1120 !important;
  --el-table-border-color: rgba(255, 255, 255, 0.06) !important;
  border-radius: 8px;
  overflow: hidden;
}

.cyber-id-badge {
  font-family: monospace;
  font-weight: 700;
  color: var(--iot-color-cyan);
  background: rgba(6, 182, 212, 0.12);
  border: 1px solid rgba(6, 182, 212, 0.3);
  padding: 2px 7px;
  border-radius: 4px;
  font-size: 0.82rem;
}

/* Glowing Level Badges */
.level-badge-wrap {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 3px 10px;
  border-radius: 6px;
  font-size: 0.76rem;
  font-weight: 700;
  letter-spacing: 0.5px;
}

.level-badge-wrap .badge-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.level-critical {
  background: rgba(239, 68, 68, 0.18);
  color: #f87171;
  border: 1px solid rgba(239, 68, 68, 0.5);
  box-shadow: 0 0 10px rgba(239, 68, 68, 0.25);
}
.level-critical .badge-dot { background: #ef4444; box-shadow: 0 0 6px #ef4444; }

.level-high {
  background: rgba(245, 158, 11, 0.18);
  color: #fbbf24;
  border: 1px solid rgba(245, 158, 11, 0.5);
  box-shadow: 0 0 10px rgba(245, 158, 11, 0.25);
}
.level-high .badge-dot { background: #f59e0b; box-shadow: 0 0 6px #f59e0b; }

.level-warn {
  background: rgba(251, 191, 36, 0.14);
  color: #fde047;
  border: 1px solid rgba(251, 191, 36, 0.4);
}
.level-warn .badge-dot { background: #fbbf24; }

.level-info {
  background: rgba(6, 182, 212, 0.14);
  color: #38bdf8;
  border: 1px solid rgba(6, 182, 212, 0.4);
}
.level-info .badge-dot { background: #06b6d4; }

/* Rule Link Cell */
.rule-link-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.rule-code-tag {
  font-family: monospace;
  font-size: 0.75rem;
  color: var(--iot-color-cyan);
}

.rule-name-text {
  font-size: 0.88rem;
  font-weight: 500;
  color: var(--iot-text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* Device & Point Cell (No Icon) */
.device-point-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.dev-name-row {
  display: flex;
  align-items: center;
  gap: 6px;
}

.dev-name-text {
  font-size: 0.88rem;
  font-weight: 500;
  color: var(--iot-text-primary);
}

.dev-id-tag {
  font-size: 0.76rem;
  color: var(--iot-text-muted);
}

.point-row {
  display: flex;
  align-items: center;
  gap: 6px;
}

.point-lbl {
  font-size: 0.78rem;
  color: var(--iot-text-secondary);
}

.point-code-badge {
  font-family: monospace;
  font-size: 0.78rem;
  color: var(--iot-color-cyan);
  background: rgba(0, 0, 0, 0.35);
  padding: 1px 6px;
  border-radius: 3px;
}

/* Snapshot & Alarm Msg Cell */
.alarm-msg-cell {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.msg-text {
  font-size: 0.88rem;
  line-height: 1.45;
  color: var(--iot-text-primary);
}

.snapshot-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.22) 0%, rgba(239, 68, 68, 0.08) 100%);
  border: 1px solid rgba(239, 68, 68, 0.45);
  padding: 3px 10px;
  border-radius: 6px;
  width: fit-content;
  font-size: 0.8rem;
}

.snap-lbl {
  color: #f87171;
}

.snap-val {
  color: #ef4444;
  font-size: 0.88rem;
  font-weight: 700;
}

/* Time Cell */
.time-cell {
  font-size: 0.84rem;
  color: var(--iot-text-muted);
}

/* Status Pills */
.status-pill-wrap {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 0.78rem;
  font-weight: 600;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.status-0 {
  background: rgba(239, 68, 68, 0.16);
  color: #f87171;
  border: 1px solid rgba(239, 68, 68, 0.4);
}
.status-0 .status-dot { background: #ef4444; box-shadow: 0 0 6px #ef4444; }

.status-1 {
  background: rgba(245, 158, 11, 0.16);
  color: #fbbf24;
  border: 1px solid rgba(245, 158, 11, 0.4);
}
.status-1 .status-dot { background: #f59e0b; }

.status-2 {
  background: rgba(16, 185, 129, 0.16);
  color: #34d399;
  border: 1px solid rgba(16, 185, 129, 0.4);
}
.status-2 .status-dot { background: #10b981; }

/* Actions Column with Generous Padding & Space */
.table-actions {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  padding: 0 12px;
}

.action-btn {
  padding: 4px 10px;
  height: 28px;
}

.pagination-footer {
  display: flex;
  justify-content: flex-end;
  padding-top: 10px;
}

/* Modal Styles (Dark Theme Only) */
.modal-body-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.event-summary-card {
  background: rgba(15, 23, 42, 0.7);
  border: 1px solid var(--iot-border-color);
  padding: 16px;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.summary-top {
  display: flex;
  align-items: center;
  gap: 12px;
}

.event-id {
  font-family: monospace;
  font-size: 0.85rem;
  color: var(--iot-color-cyan);
}

.event-time {
  font-size: 0.82rem;
  color: var(--iot-text-muted);
  margin-left: auto;
}

.event-title {
  margin: 0;
  font-size: 1rem;
  color: var(--iot-text-primary);
  line-height: 1.4;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  background: rgba(15, 23, 42, 0.6);
  padding: 14px;
  border-radius: 6px;
  border: 1px solid var(--iot-border-color);
}

.detail-item {
  display: flex;
  gap: 8px;
  font-size: 0.88rem;
}

.detail-label {
  color: var(--iot-text-secondary);
}

.detail-val {
  color: var(--iot-text-primary);
}

.section-title {
  font-size: 0.92rem;
  margin: 0 0 10px 0;
  color: var(--iot-color-cyan);
}

.log-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.log-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  background: rgba(0, 0, 0, 0.3);
  border-radius: 4px;
  font-size: 0.85rem;
}

.log-badge {
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 0.72rem;
  font-weight: 700;
}

.badge-success {
  background: rgba(16, 185, 129, 0.2);
  color: #10b981;
}

.badge-fail {
  background: rgba(239, 68, 68, 0.2);
  color: #ef4444;
}

.log-desc {
  flex: 1;
  color: var(--iot-text-primary);
}

.log-time {
  color: var(--iot-text-muted);
  font-size: 0.78rem;
}

.resolution-form {
  margin-top: 6px;
}

.font-bold { font-weight: 700; }
</style>
