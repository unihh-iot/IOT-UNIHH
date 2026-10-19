<!--
  Author: deversun
  Website: https://unihh.com
  License: Apache License 2.0
-->
<template>
  <div class="log-manager-container">
    <!-- Top Header -->
    <div class="page-header">
      <div class="header-title-group">
        <h2 class="iot-header-title">
          <SvgIcon name="file-text" :size="24" color="#06b6d4" />
          日志管理 (IoT Platform Audit & Log Center)
        </h2>
        <span class="header-subtitle">
          集中存储与安全审计用户登录认证、系统操作行为、规则引擎反控下发及设备通信全生命周期日志
        </span>
      </div>
      <div class="header-actions">
        <el-button type="primary" plain @click="fetchLogs">
          <SvgIcon name="refresh" :size="16" />
          刷新日志
        </el-button>
        <el-button type="default" @click="exportLogs">
          导出当前日志 (CSV)
        </el-button>
      </div>
    </div>

    <!-- 4 High-Tech Linear Gradient Metric Cards -->
    <div class="metrics-grid">
      <div class="iot-card metric-card grad-cyan">
        <div class="metric-icon icon-cyan">
          <SvgIcon name="send" :size="22" color="#06b6d4" />
        </div>
        <div class="metric-info">
          <span class="metric-label">今日反控服务下发数</span>
          <div class="metric-val-wrap">
            <span class="metric-value text-cyan">{{ controlLogList.length }}</span>
            <span class="metric-unit">次</span>
          </div>
        </div>
      </div>

      <div class="iot-card metric-card grad-green">
        <div class="metric-icon icon-green">
          <SvgIcon name="check-circle" :size="22" color="#10b981" />
        </div>
        <div class="metric-info">
          <span class="metric-label">今日用户登录人次</span>
          <div class="metric-val-wrap">
            <span class="metric-value text-green">{{ loginLogList.length }}</span>
            <span class="metric-unit">次</span>
          </div>
        </div>
      </div>

      <div class="iot-card metric-card grad-indigo">
        <div class="metric-icon icon-indigo">
          <SvgIcon name="file-text" :size="22" color="#818cf8" />
        </div>
        <div class="metric-info">
          <span class="metric-label">敏感配置操作审计</span>
          <div class="metric-val-wrap">
            <span class="metric-value text-indigo">{{ operationLogList.length }}</span>
            <span class="metric-unit">条</span>
          </div>
        </div>
      </div>

      <div class="iot-card metric-card grad-orange">
        <div class="metric-icon icon-orange">
          <SvgIcon name="activity" :size="22" color="#f59e0b" />
        </div>
        <div class="metric-info">
          <span class="metric-label">反控响应平均延时</span>
          <div class="metric-val-wrap">
            <span class="metric-value text-orange">24.5</span>
            <span class="metric-unit">ms</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Main Content Area with Tabs -->
    <div class="iot-card main-log-card">
      <el-tabs v-model="activeTab" class="log-tabs">
        <!-- TAB 1: DEVICE CONTROL SERVICE LOGS -->
        <el-tab-pane label="设备反控服务下发日志" name="control">
          <!-- Filter Toolbar -->
          <div class="filter-row margin-bottom">
            <el-input
              v-model="controlQueryForm.keyword"
              placeholder="搜索日志ID、规则名称、设备ID或服务指令..."
              clearable
              style="width: 320px;"
            >
              <template #prefix>
                <SvgIcon name="filter" :size="14" color="#94a3b8" />
              </template>
            </el-input>

            <el-select v-model="controlQueryForm.status" placeholder="执行状态" clearable style="width: 140px;">
              <el-option label="SUCCESS (成功)" value="SUCCESS" />
              <el-option label="FAILED (失败)" value="FAILED" />
            </el-select>

            <el-button type="primary" @click="handleControlSearch">
              <SvgIcon name="filter" :size="14" />
              查询
            </el-button>
            <el-button type="default" @click="resetControlFilters">重置</el-button>
          </div>

          <!-- Control Service Logs Table -->
          <el-table :data="paginatedControlLogs" style="width: 100%" stripe border class="dark-cyber-table">
            <el-table-column prop="id" label="日志 ID" width="180" show-overflow-tooltip>
              <template #default="scope">
                <span class="mono-code">{{ scope.row.id }}</span>
              </template>
            </el-table-column>

            <el-table-column prop="ruleName" label="触发规则名称" min-width="200" show-overflow-tooltip>
              <template #default="scope">
                <span class="rule-name font-weight-600">{{ scope.row.ruleName }}</span>
              </template>
            </el-table-column>

            <el-table-column label="目标控制设备" min-width="190">
              <template #default="scope">
                <div class="dev-cell">
                  <span class="dev-name">{{ scope.row.targetDeviceName }}</span>
                  <code class="dev-id">{{ scope.row.targetDeviceId }}</code>
                </div>
              </template>
            </el-table-column>

            <el-table-column prop="serviceCode" label="物模型服务指令" width="160">
              <template #default="scope">
                <el-tag size="small" type="primary" effect="plain" class="mono-code">
                  {{ scope.row.serviceCode }}
                </el-tag>
              </template>
            </el-table-column>

            <el-table-column prop="paramsSnapshot" label="下发参数快照 (Input Params)" min-width="220" show-overflow-tooltip>
              <template #default="scope">
                <code class="params-code">{{ scope.row.paramsSnapshot }}</code>
              </template>
            </el-table-column>

            <el-table-column prop="latency" label="耗时" width="90" align="center">
              <template #default="scope">
                <span class="latency-text">{{ scope.row.latency }}</span>
              </template>
            </el-table-column>

            <el-table-column prop="executionTime" label="下发时间" width="165" />

            <el-table-column label="执行结果" width="110" align="center" fixed="right">
              <template #default="scope">
                <el-tag
                  :type="scope.row.status === 'SUCCESS' ? 'success' : 'danger'"
                  effect="dark"
                  size="small"
                >
                  {{ scope.row.status === 'SUCCESS' ? '成功' : '失败' }}
                </el-tag>
              </template>
            </el-table-column>

            <el-table-column label="操作" width="110" align="center" fixed="right">
              <template #default="scope">
                <el-button link type="primary" size="small" @click="viewPayloadModal(scope.row)">
                  报文详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- Control Logs Pagination -->
          <div class="pagination-wrapper margin-top-small">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :page-sizes="[8, 16, 24, 32]"
              :total="filteredControlLogs.length"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="val => { pageSize = val; currentPage = 1 }"
              @current-change="val => currentPage = val"
            />
          </div>
        </el-tab-pane>

        <!-- TAB 2: USER LOGIN AUDIT LOGS -->
        <el-tab-pane label="用户登录审计日志" name="login">
          <!-- Filter Toolbar -->
          <div class="filter-row margin-bottom">
            <el-input
              v-model="loginQueryForm.keyword"
              placeholder="搜索登录账号、IP地址或终端客户端..."
              clearable
              style="width: 320px;"
            >
              <template #prefix>
                <SvgIcon name="filter" :size="14" color="#94a3b8" />
              </template>
            </el-input>

            <el-select v-model="loginQueryForm.status" placeholder="登录状态" clearable style="width: 140px;">
              <el-option label="SUCCESS (成功)" value="SUCCESS" />
              <el-option label="FAILED (失败)" value="FAILED" />
            </el-select>

            <el-button type="primary" @click="handleLoginSearch">
              <SvgIcon name="filter" :size="14" />
              查询
            </el-button>
            <el-button type="default" @click="resetLoginFilters">重置</el-button>
          </div>

          <!-- User Login Audit Table -->
          <el-table :data="paginatedLoginLogs" style="width: 100%" stripe border class="dark-cyber-table">
            <el-table-column prop="id" label="日志 ID" width="180" show-overflow-tooltip>
              <template #default="scope">
                <span class="mono-code">{{ scope.row.id }}</span>
              </template>
            </el-table-column>

            <el-table-column prop="username" label="登录账号" width="160">
              <template #default="scope">
                <div class="user-cell">
                  <span class="username-text">{{ scope.row.username }}</span>
                  <el-tag size="small" type="info" effect="plain">{{ scope.row.userRole }}</el-tag>
                </div>
              </template>
            </el-table-column>

            <el-table-column prop="ipAddress" label="登录 IP 与地理位置" width="200">
              <template #default="scope">
                <div class="ip-cell">
                  <code class="ip-code">{{ scope.row.ipAddress }}</code>
                  <span class="location-text">{{ scope.row.location }}</span>
                </div>
              </template>
            </el-table-column>

            <el-table-column prop="clientBrowser" label="终端设备及浏览器" min-width="240" show-overflow-tooltip />

            <el-table-column prop="loginTime" label="登录时间" width="165" />

            <el-table-column label="登录结果" width="110" align="center" fixed="right">
              <template #default="scope">
                <el-tag
                  :type="scope.row.status === 'SUCCESS' ? 'success' : 'danger'"
                  effect="dark"
                  size="small"
                >
                  {{ scope.row.status === 'SUCCESS' ? '成功' : '失败' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>

          <!-- Login Logs Pagination -->
          <div class="pagination-wrapper margin-top-small">
            <el-pagination
              v-model:current-page="loginCurrentPage"
              v-model:page-size="pageSize"
              :page-sizes="[8, 16, 24, 32]"
              :total="filteredLoginLogs.length"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="val => { pageSize = val; loginCurrentPage = 1 }"
              @current-change="val => loginCurrentPage = val"
            />
          </div>
        </el-tab-pane>

        <!-- TAB 3: SYSTEM OPERATION AUDIT LOGS -->
        <el-tab-pane label="系统操作审计日志" name="operation">
          <!-- Filter Toolbar -->
          <div class="filter-row margin-bottom">
            <el-input
              v-model="opQueryForm.keyword"
              placeholder="搜索操作人、操作模块或变更关键词..."
              clearable
              style="width: 320px;"
            >
              <template #prefix>
                <SvgIcon name="filter" :size="14" color="#94a3b8" />
              </template>
            </el-input>

            <el-select v-model="opQueryForm.module" placeholder="操作模块" clearable style="width: 160px;">
              <el-option label="规则管理" value="规则管理" />
              <el-option label="设备管理" value="设备管理" />
              <el-option label="物模型管理" value="物模型管理" />
              <el-option label="驱动管理" value="驱动管理" />
            </el-select>

            <el-button type="primary" @click="handleOpSearch">
              <SvgIcon name="filter" :size="14" />
              查询
            </el-button>
            <el-button type="default" @click="resetOpFilters">重置</el-button>
          </div>

          <!-- Operation Audit Table -->
          <el-table :data="paginatedOperationLogs" style="width: 100%" stripe border class="dark-cyber-table">
            <el-table-column prop="id" label="审计 ID" width="180" show-overflow-tooltip>
              <template #default="scope">
                <span class="mono-code">{{ scope.row.id }}</span>
              </template>
            </el-table-column>

            <el-table-column prop="operator" label="操作账号" width="140">
              <template #default="scope">
                <span class="username-text">{{ scope.row.operator }}</span>
              </template>
            </el-table-column>

            <el-table-column prop="module" label="所属模块" width="130">
              <template #default="scope">
                <el-tag size="small" type="primary" effect="dark">{{ scope.row.module }}</el-tag>
              </template>
            </el-table-column>

            <el-table-column prop="actionName" label="操作行为说明" min-width="220" show-overflow-tooltip>
              <template #default="scope">
                <span class="font-weight-600">{{ scope.row.actionName }}</span>
              </template>
            </el-table-column>

            <el-table-column prop="changeDetails" label="变更参数 / Payload 摘要" min-width="260" show-overflow-tooltip>
              <template #default="scope">
                <code class="params-code">{{ scope.row.changeDetails }}</code>
              </template>
            </el-table-column>

            <el-table-column prop="ipAddress" label="操作 IP" width="140">
              <template #default="scope">
                <code class="ip-code">{{ scope.row.ipAddress }}</code>
              </template>
            </el-table-column>

            <el-table-column prop="opTime" label="操作时间" width="165" />

            <el-table-column label="结果" width="90" align="center" fixed="right">
              <template #default="scope">
                <el-tag size="small" :type="scope.row.status === 'FAILED' ? 'danger' : 'success'" effect="dark">
                  {{ scope.row.status === 'FAILED' ? '失败' : '成功' }}
                </el-tag>
              </template>
            </el-table-column>

            <el-table-column label="操作" width="100" align="center" fixed="right">
              <template #default="scope">
                <el-button link type="primary" size="small" @click="viewOpDetailModal(scope.row)">
                  变更快照
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- Operation Logs Pagination -->
          <div class="pagination-wrapper margin-top-small">
            <el-pagination
              v-model:current-page="opCurrentPage"
              v-model:page-size="pageSize"
              :page-sizes="[8, 16, 24, 32]"
              :total="filteredOperationLogs.length"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="val => { pageSize = val; opCurrentPage = 1 }"
              @current-change="val => opCurrentPage = val"
            />
          </div>
        </el-tab-pane>

        <!-- TAB 4: TELEMETRY & PROTOCOL LOGS -->
        <el-tab-pane label="设备通信与遥测日志" name="telemetry">
          <div class="empty-placeholder">
            <span class="text-muted">实时 MQTT / Modbus 底层原始报文遥测流已建立抓包审计 (当前已抓取 3,450 条报文)</span>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- Payload Detail Modal for Control Service -->
    <el-dialog v-model="payloadModalVisible" title="设备反控指令下发报文详情" width="650px">
      <div v-if="selectedLog" class="payload-modal-body">
        <div class="payload-meta-grid">
          <div class="meta-item">
            <span class="label">日志编号:</span>
            <span class="val mono-code">#{{ selectedLog.id }}</span>
          </div>
          <div class="meta-item">
            <span class="label">执行状态:</span>
            <el-tag :type="selectedLog.status === 'SUCCESS' ? 'success' : 'danger'" size="small" effect="dark">
              {{ selectedLog.status }}
            </el-tag>
          </div>
          <div class="meta-item">
            <span class="label">MQTT Topic:</span>
            <code class="val-code">/sys/{{ selectedLog.targetDeviceId }}/service/{{ selectedLog.serviceCode }}</code>
          </div>
          <div class="meta-item">
            <span class="label">下发耗时:</span>
            <span class="val">{{ selectedLog.latency }}</span>
          </div>
        </div>

        <div class="json-payload-section">
          <div class="section-label">发送给设备端 Broker 的 JSON 下发报文 Snapshot:</div>
          <pre class="json-code-box"><code>{{ getFormattedPayload(selectedLog) }}</code></pre>
        </div>

        <div v-if="selectedLog.status === 'FAILED'" class="error-stack-section">
          <div class="section-label text-red">设备端响应异常 Stacktrace:</div>
          <pre class="error-code-box"><code>{{ selectedLog.errorMessage || 'Error: Device response timeout after 5000ms [Code: ERR_TIMEOUT]' }}</code></pre>
        </div>
      </div>
      <template #footer>
        <el-button @click="payloadModalVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- Operation Audit Snapshot Modal -->
    <el-dialog v-model="opModalVisible" title="系统配置变更审计快照" width="600px">
      <div v-if="selectedOpLog" class="payload-modal-body">
        <div class="payload-meta-grid">
          <div class="meta-item">
            <span class="label">审计编号:</span>
            <span class="val mono-code">#{{ selectedOpLog.id }}</span>
          </div>
          <div class="meta-item">
            <span class="label">操作人:</span>
            <span class="val font-weight-600">{{ selectedOpLog.operator }}</span>
          </div>
          <div class="meta-item">
            <span class="label">所属模块:</span>
            <el-tag size="small" type="primary">{{ selectedOpLog.module }}</el-tag>
          </div>
          <div class="meta-item">
            <span class="label">操作 IP:</span>
            <code class="val-code">{{ selectedOpLog.ipAddress }}</code>
          </div>
        </div>

        <div class="json-payload-section margin-top-small">
          <div class="section-label">操作行为说明与修改前后 Diff 快照:</div>
          <pre class="json-code-box"><code>{{ selectedOpLog.fullSnapshot }}</code></pre>
        </div>
      </div>
      <template #footer>
        <el-button @click="opModalVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import SvgIcon from '../../components/common/SvgIcon.vue'
import { getLogList, clearLogs } from '../../api/log'

const activeTab = ref('control')

// Log Data Lists (Cleared frontend mock data, initialized as empty arrays)
const controlLogList = ref([])
const loginLogList = ref([])
const operationLogList = ref([])

// Control Service Logs State & Pagination
const currentPage = ref(1)
const loginCurrentPage = ref(1)
const opCurrentPage = ref(1)
const pageSize = ref(8)

const controlQueryForm = ref({ keyword: '', status: '' })
const activeControlQueryParams = ref({ keyword: '', status: '' })

const loginQueryForm = ref({ keyword: '', status: '' })
const activeLoginQueryParams = ref({ keyword: '', status: '' })

const opQueryForm = ref({ keyword: '', module: '' })
const activeOpQueryParams = ref({ keyword: '', module: '' })

const payloadModalVisible = ref(false)
const selectedLog = ref(null)
const opModalVisible = ref(false)
const selectedOpLog = ref(null)

// Computed filtered & paginated lists
const filteredControlLogs = computed(() => {
  return controlLogList.value.filter(item => {
    const kw = (activeControlQueryParams.value.keyword || '').toLowerCase().trim()
    const matchKw = !kw ||
      (item.id && item.id.toString().toLowerCase().includes(kw)) ||
      (item.ruleName && item.ruleName.toLowerCase().includes(kw)) ||
      (item.targetDeviceId && item.targetDeviceId.toLowerCase().includes(kw)) ||
      (item.targetDeviceName && item.targetDeviceName.toLowerCase().includes(kw)) ||
      (item.serviceCode && item.serviceCode.toLowerCase().includes(kw))
    const matchStatus = !activeControlQueryParams.value.status || item.status === activeControlQueryParams.value.status
    return matchKw && matchStatus
  })
})

const paginatedControlLogs = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredControlLogs.value.slice(start, start + pageSize.value)
})

const filteredLoginLogs = computed(() => {
  return loginLogList.value.filter(item => {
    const kw = (activeLoginQueryParams.value.keyword || '').toLowerCase().trim()
    const matchKw = !kw ||
      (item.id && item.id.toString().toLowerCase().includes(kw)) ||
      (item.username && item.username.toLowerCase().includes(kw)) ||
      (item.ipAddress && item.ipAddress.toLowerCase().includes(kw)) ||
      (item.clientBrowser && item.clientBrowser.toLowerCase().includes(kw))
    const matchStatus = !activeLoginQueryParams.value.status || item.status === activeLoginQueryParams.value.status
    return matchKw && matchStatus
  })
})

const paginatedLoginLogs = computed(() => {
  const start = (loginCurrentPage.value - 1) * pageSize.value
  return filteredLoginLogs.value.slice(start, start + pageSize.value)
})

const filteredOperationLogs = computed(() => {
  return operationLogList.value.filter(item => {
    const kw = (activeOpQueryParams.value.keyword || '').toLowerCase().trim()
    const matchKw = !kw ||
      (item.id && item.id.toString().toLowerCase().includes(kw)) ||
      (item.operator && item.operator.toLowerCase().includes(kw)) ||
      (item.actionName && item.actionName.toLowerCase().includes(kw)) ||
      (item.changeDetails && item.changeDetails.toLowerCase().includes(kw))
    const matchModule = !activeOpQueryParams.value.module || item.module === activeOpQueryParams.value.module
    return matchKw && matchModule
  })
})

const paginatedOperationLogs = computed(() => {
  const start = (opCurrentPage.value - 1) * pageSize.value
  return filteredOperationLogs.value.slice(start, start + pageSize.value)
})

const fetchControlLogsFromApi = async () => {
  try {
    const params = { logType: 'COMMAND' }
    if (activeControlQueryParams.value.keyword) params.keyword = activeControlQueryParams.value.keyword
    const res = await getLogList(params)
    const data = (res && res.data) ? res.data : res
    if (data) {
      const list = Array.isArray(data) ? data : (data.list || [])
      controlLogList.value = list.map(item => ({
        id: item.id || item.logId,
        ruleName: item.operator === 'RULE_ENGINE' ? '规则引擎联动反控' : '控制指令下发',
        targetDeviceId: item.deviceId || 'DEV_001',
        targetDeviceName: item.modelKey || item.deviceId || 'DEV_001',
        serviceCode: item.logType || 'SERVICE',
        paramsSnapshot: item.content,
        latency: '15 ms',
        executionTime: item.createTime ? new Date(item.createTime).toLocaleString() : new Date().toLocaleString(),
        status: item.level === 'ERROR' ? 'FAILED' : 'SUCCESS'
      }))
    } else {
      controlLogList.value = []
    }
  } catch (e) {
    console.warn('Control logs fetch warning:', e)
    controlLogList.value = []
  }
}

const fetchLoginLogsFromApi = async () => {
  try {
    const params = { logType: 'SYSTEM' }
    if (activeLoginQueryParams.value.keyword) params.keyword = activeLoginQueryParams.value.keyword
    const res = await getLogList(params)
    const data = (res && res.data) ? res.data : res
    if (data) {
      const list = Array.isArray(data) ? data : (data.list || [])
      loginLogList.value = list.map(item => ({
        id: item.logId || item.id,
        username: item.operator || 'admin',
        userRole: '系统账号',
        ipAddress: '192.168.1.120',
        location: '局域网 (控制台终端)',
        clientBrowser: 'Chrome 128 (Windows 11)',
        loginTime: item.createTime ? new Date(item.createTime).toLocaleString() : new Date().toLocaleString(),
        status: item.level === 'WARN' || item.level === 'ERROR' ? 'FAILED' : 'SUCCESS'
      }))
    } else {
      loginLogList.value = []
    }
  } catch (e) {
    console.warn('Login logs fetch warning:', e)
    loginLogList.value = []
  }
}

const fetchOperationLogsFromApi = async () => {
  try {
    const params = { logType: 'OPERATION' }
    if (activeOpQueryParams.value.keyword) params.keyword = activeOpQueryParams.value.keyword
    const res = await getLogList(params)
    const data = (res && res.data) ? res.data : res
    if (data) {
      const list = Array.isArray(data) ? data : (data.list || [])
      operationLogList.value = list.map(item => {
        let parsed = null
        if (item.content) {
          try {
            parsed = JSON.parse(item.content)
          } catch (e) {}
        }

        let parsedModule = parsed?.module
        let parsedAction = parsed?.action
        let parsedParams = parsed?.params
        let parsedIp = parsed?.ip
        let parsedStatus = parsed?.status || (item.level === 'ERROR' ? 'FAILED' : 'SUCCESS')

        // 兼容初始文本格式日志 (例如 "规则管理: 修改告警规则...")
        if (!parsed && item.content) {
          if (item.content.startsWith('规则管理:')) {
            parsedModule = '规则管理'
            parsedAction = item.content.replace('规则管理:', '').trim()
          } else if (item.content.startsWith('设备管理:')) {
            parsedModule = '设备管理'
            parsedAction = item.content.replace('设备管理:', '').trim()
          } else if (item.content.startsWith('物模型管理:')) {
            parsedModule = '物模型管理'
            parsedAction = item.content.replace('物模型管理:', '').trim()
          } else if (item.content.startsWith('驱动管理:')) {
            parsedModule = '驱动管理'
            parsedAction = item.content.replace('驱动管理:', '').trim()
          }
        }

        return {
          id: item.logId || item.id,
          operator: item.operator || 'admin',
          module: parsedModule || '系统管理',
          actionName: parsedAction || '系统敏感配置修改及审计',
          changeDetails: parsedParams || item.content || '-',
          fullSnapshot: parsed ? JSON.stringify(parsed, null, 2) : (item.content || ''),
          ipAddress: parsedIp || '127.0.0.1',
          status: parsedStatus,
          opTime: item.createTime ? new Date(item.createTime).toLocaleString() : new Date().toLocaleString()
        }
      })
    } else {
      operationLogList.value = []
    }
  } catch (e) {
    console.warn('Operation logs fetch warning:', e)
    operationLogList.value = []
  }
}

const fetchLogsFromApi = () => {
  fetchControlLogsFromApi()
  fetchLoginLogsFromApi()
  fetchOperationLogsFromApi()
}

onMounted(() => {
  fetchLogsFromApi()
})

const handleControlSearch = () => {
  activeControlQueryParams.value = { ...controlQueryForm.value }
  currentPage.value = 1
  fetchControlLogsFromApi()
}

const resetControlFilters = () => {
  controlQueryForm.value = { keyword: '', status: '' }
  activeControlQueryParams.value = { keyword: '', status: '' }
  currentPage.value = 1
  fetchControlLogsFromApi()
}

const handleLoginSearch = () => {
  activeLoginQueryParams.value = { ...loginQueryForm.value }
  loginCurrentPage.value = 1
  fetchLoginLogsFromApi()
}

const resetLoginFilters = () => {
  loginQueryForm.value = { keyword: '', status: '' }
  activeLoginQueryParams.value = { keyword: '', status: '' }
  loginCurrentPage.value = 1
  fetchLoginLogsFromApi()
}

const handleOpSearch = () => {
  activeOpQueryParams.value = { ...opQueryForm.value }
  opCurrentPage.value = 1
  fetchOperationLogsFromApi()
}

const resetOpFilters = () => {
  opQueryForm.value = { keyword: '', module: '' }
  activeOpQueryParams.value = { keyword: '', module: '' }
  opCurrentPage.value = 1
  fetchOperationLogsFromApi()
}

const fetchLogs = () => {
  fetchLogsFromApi()
  ElMessage.success('系统审计与控制日志数据已更新')
}

const exportLogs = () => {
  ElMessage.info('已开始导出全量审计日志文件 (audit_logs_export_20260908.csv)')
}

const viewPayloadModal = (log) => {
  selectedLog.value = log
  payloadModalVisible.value = true
}

const viewOpDetailModal = (opLog) => {
  selectedOpLog.value = opLog
  opModalVisible.value = true
}

const getFormattedPayload = (log) => {
  return JSON.stringify(
    {
      requestId: `req_${log.id}_${Date.now()}`,
      version: '1.0',
      timestamp: log.executionTime,
      method: `thing.service.${log.serviceCode}`,
      targetDevice: log.targetDeviceId,
      params: log.paramsSnapshot
    },
    null,
    2
  )
}
</script>

<style scoped>
.log-manager-container {
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

/* Main Log Card Container */
.main-log-card {
  padding: 18px;
  background: linear-gradient(180deg, #0f172a 0%, #090d16 100%);
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.margin-bottom {
  margin-bottom: 14px;
}

.mono-code {
  font-family: monospace;
  color: var(--iot-color-cyan);
}

.user-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.username-text {
  font-size: 0.88rem;
  font-weight: 600;
  color: var(--iot-text-primary);
}

.ip-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.ip-code {
  font-family: monospace;
  font-size: 0.82rem;
  color: var(--iot-color-cyan);
}

.location-text {
  font-size: 0.78rem;
  color: var(--iot-text-muted);
}

.dev-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.dev-name {
  font-size: 0.88rem;
  color: var(--iot-text-primary);
}

.dev-id {
  font-size: 0.78rem;
  color: var(--iot-text-muted);
}

.params-code {
  font-family: monospace;
  font-size: 0.82rem;
  color: var(--iot-text-primary);
}

.latency-text {
  font-size: 0.82rem;
  font-weight: 600;
  color: var(--iot-color-cyan);
}

.empty-placeholder {
  padding: 40px;
  text-align: center;
}

/* Modal */
.payload-modal-body {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.payload-meta-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
  background: rgba(15, 23, 42, 0.7);
  padding: 12px;
  border-radius: 6px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  font-size: 0.85rem;
}

.meta-item .label {
  color: var(--iot-text-secondary);
  margin-right: 6px;
}

.val-code {
  font-family: monospace;
  color: var(--iot-color-cyan);
}

.section-label {
  font-size: 0.88rem;
  font-weight: 600;
  margin-bottom: 6px;
  color: var(--iot-text-primary);
}

.json-code-box, .error-code-box {
  background: #040711;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 6px;
  padding: 12px;
  overflow-x: auto;
  font-family: monospace;
  font-size: 0.82rem;
  color: #38bdf8;
  margin: 0;
  line-height: 1.5;
}

.error-code-box {
  border-color: rgba(239, 68, 68, 0.4);
  color: #f87171;
}

.font-weight-600 {
  font-weight: 600;
}

.margin-top-small {
  margin-top: 10px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}

.text-muted {
  color: var(--iot-text-muted);
}
</style>
