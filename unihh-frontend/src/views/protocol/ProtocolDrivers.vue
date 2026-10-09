<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="iot-header-title">
        <SvgIcon name="database" :size="24" color="#f59e0b" />
        协议驱动管理 (Registered IoT Drivers)
      </h2>
      <el-button type="primary" @click="fetchDriverStatus">
        <SvgIcon name="refresh" :size="16" />
        刷新驱动在线状态
      </el-button>
    </div>

    <!-- Search & Filter Bar -->
    <div class="iot-card filter-card">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="驱动 Code / 名称">
          <el-input v-model="queryForm.keyword" placeholder="搜索驱动标识或中文名称" clearable />
        </el-form-item>
        <el-form-item label="运行状态">
          <el-select v-model="queryForm.status" placeholder="全部状态" clearable style="width: 140px">
            <el-option label="在线 (Running)" :value="1" />
            <el-option label="离线 (Offline)" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- Drivers IoT Card Grid Layout -->
    <div v-if="driverCards.length > 0" class="driver-card-grid margin-top">
      <div v-for="driver in driverCards" :key="driver.driverCode" class="iot-card driver-card" :class="{ 'is-offline': !driver.online }">
        <!-- Card Header -->
        <div class="card-top">
          <div class="driver-icon-box">
            <SvgIcon name="cpu" :size="24" color="#06b6d4" />
          </div>
          <div class="driver-main-info">
            <div class="driver-title-row">
              <span class="driver-name">{{ driver.driverName }}</span>
              <span class="version-tag">v{{ driver.version }}</span>
            </div>
            <div class="driver-code"><code>{{ driver.driverCode }}</code></div>
          </div>
          <div class="driver-status">
            <StatusBadge :status="driver.online ? 1 : 0" :text="driver.online ? '在线' : '离线'" />
          </div>
        </div>

        <!-- Metrics Summary Grid -->
        <div class="card-metrics">
          <div class="m-item">
            <span class="m-label">已连接设备</span>
            <span class="m-val">{{ driver.deviceCount }} 台</span>
          </div>
          <div class="m-item">
            <span class="m-label">活动 Socket 通道</span>
            <span class="m-val">{{ driver.channelCount }} Channels</span>
          </div>
          <div class="m-item">
            <span class="m-label">传输规约</span>
            <span class="m-val">{{ driver.transport }}</span>
          </div>
          <div class="m-item">
            <span class="m-label">内存估算</span>
            <span class="m-val text-green">&lt; 2.5 MB</span>
          </div>
        </div>

        <!-- Driver Description -->
        <div class="driver-desc">
          {{ driver.description }}
        </div>

        <!-- Footer Actions -->
        <div class="card-footer">
          <el-button size="small" type="primary" plain @click="openSchemaDrawer(driver)">
            查看配置规约 (Schema)
          </el-button>
          <el-button
            size="small"
            :type="driver.online ? 'success' : 'warning'"
            plain
            :loading="driver.pingLoading"
            @click="testDriverPing(driver)"
          >
            通道心跳自检
          </el-button>
        </div>
      </div>
    </div>

    <!-- Empty State -->
    <div v-else class="iot-card empty-card margin-top">
      <el-empty description="暂无符合筛选条件的驱动记录" />
    </div>

    <!-- Driver Card Grid Pagination -->
    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[6, 12, 18, 24]"
        :total="totalDrivers"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- Driver Schema Drawer -->
    <el-drawer v-model="drawerVisible" title="驱动配置参数 Schema 规约" direction="rtl" size="420px">
      <div v-if="selectedDriver" class="drawer-content">
        <div class="drawer-header-info">
          <h3>{{ selectedDriver.driverName }}</h3>
          <p>驱动 Code: <code>{{ selectedDriver.driverCode }}</code></p>
        </div>
        <el-divider />
        <h4 class="schema-title">参数定义列表 (driver-manifest.yml):</h4>
        <div v-for="p in selectedDriver.params" :key="p.key" class="schema-item">
          <div class="s-row">
            <span class="s-name">{{ p.name }} (<code>{{ p.key }}</code>)</span>
            <el-tag size="small">{{ p.dataType }}</el-tag>
          </div>
          <div class="s-default">默认值: <code>{{ p.defaultValue }}</code> | 必填: {{ p.required ? '是' : '否' }}</div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import SvgIcon from '../../components/common/SvgIcon.vue'
import StatusBadge from '../../components/common/StatusBadge.vue'
import { getLoadedDrivers, pingDriver } from '../../api/protocol'

const queryForm = ref({ keyword: '', status: null })

const driverCards = ref([])

// Filtering & Pagination Logic
const currentPage = ref(1)
const pageSize = ref(6)
const totalDrivers = ref(0)

const fetchDriverStatus = async (page = 1) => {
  if (typeof page === 'number') {
    currentPage.value = page
  }
  try {
    const res = await getLoadedDrivers({
      keyword: queryForm.value.keyword,
      status: queryForm.value.status,
      pageNum: currentPage.value,
      pageSize: pageSize.value
    })
    
    const data = (res && res.data) ? res.data : res
    if (data) {
      const list = Array.isArray(data) ? data : (data.list || [])
      totalDrivers.value = (data.total !== undefined && data.total !== null) ? Number(data.total) : list.length

      if (list.length > 0) {
        driverCards.value = list.map(d => ({
          driverCode: d.driverCode,
          driverName: d.driverName,
          version: d.version || '1.0.0',
          online: d.status === 1,
          deviceCount: d.deviceCount !== undefined ? d.deviceCount : 0,
          channelCount: d.channelCount !== undefined ? d.channelCount : 0,
          transport: d.transport || 'TCP/IP',
          description: d.description || '已从 MySQL 数据库注册接入的协议驱动。',
          params: d.params || [],
          pingLoading: false
        }))
      } else {
        driverCards.value = []
      }
    }
  } catch (err) {
    console.error('获取驱动状态失败:', err)
  }
}

const handleSearch = () => {
  fetchDriverStatus(1)
}

const resetQuery = () => {
  queryForm.value = { keyword: '', status: null }
  fetchDriverStatus(1)
}

const handleSizeChange = (val) => {
  pageSize.value = val
  fetchDriverStatus(1)
}

const handleCurrentChange = (val) => {
  fetchDriverStatus(val)
}

// Drawer & Actions
const drawerVisible = ref(false)
const selectedDriver = ref(null)

const openSchemaDrawer = (driver) => {
  selectedDriver.value = driver
  drawerVisible.value = true
}

const testDriverPing = async (driver) => {
  driver.pingLoading = true
  try {
    const res = await pingDriver(driver.driverCode)
    const data = (res && res.data) ? res.data : res
    if (data && data.online) {
      driver.online = true
      if (data.activeDeviceCount !== undefined) {
        driver.deviceCount = data.activeDeviceCount
      }
      if (data.channelCount !== undefined) {
        driver.channelCount = data.channelCount
      }
      ElMessage.success({
        message: `驱动 [${driver.driverName}] 通道心跳自检正常！${data.latencyMs >= 0 ? `响应时延: ${data.latencyMs}ms, ` : ''}活跃设备: ${driver.deviceCount} 台`,
        duration: 3500
      })
    } else {
      driver.online = false
      ElMessage.warning({
        message: (data && data.message) ? data.message : `驱动 [${driver.driverName}] 通道未响应，状态已标记为离线`,
        duration: 4000
      })
    }
  } catch (err) {
    driver.online = false
    ElMessage.error({
      message: `驱动 [${driver.driverName}] 心跳自检异常: ${err.message || '网络连接超时'}`,
      duration: 4000
    })
  } finally {
    driver.pingLoading = false
    fetchDriverStatus(currentPage.value)
  }
}

onMounted(() => {
  fetchDriverStatus()
})
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

.filter-card {
  padding: 16px 20px 0 20px;
}

.driver-card-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

@media (max-width: 1100px) {
  .driver-card-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 680px) {
  .driver-card-grid {
    grid-template-columns: 1fr;
  }
}

.driver-card {
  display: flex;
  flex-direction: column;
  gap: 16px;
  background: var(--iot-card-bg);
  border: 1px solid var(--iot-border-color);
  border-radius: 8px;
  padding: 20px;
  transition: all 0.3s ease;
}

.driver-card.is-offline {
  border-color: rgba(239, 68, 68, 0.25);
  background: rgba(15, 23, 42, 0.6);
}

.driver-card.is-offline .driver-icon-box {
  background: rgba(100, 116, 139, 0.15);
}

.card-top {
  display: flex;
  align-items: center;
  gap: 12px;
  border-bottom: 1px solid var(--iot-border-color);
  padding-bottom: 12px;
}

.driver-icon-box {
  background: rgba(6, 182, 212, 0.1);
  padding: 10px;
  border-radius: 8px;
}

.driver-main-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.driver-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.driver-name {
  font-size: 1rem;
  font-weight: 600;
  color: var(--iot-text-primary);
}

.version-tag {
  font-size: 0.75rem;
  background: var(--iot-bg-tertiary);
  padding: 1px 6px;
  border-radius: 4px;
  color: var(--iot-text-secondary);
}

.driver-code {
  font-size: 0.85rem;
}

.card-metrics {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  background: var(--iot-bg-primary);
  padding: 12px;
  border-radius: 6px;
}

.m-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.m-label {
  font-size: 0.75rem;
  color: var(--iot-text-muted);
}

.m-val {
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--iot-text-primary);
}

.text-green {
  color: var(--iot-color-green);
}

.driver-desc {
  font-size: 0.85rem;
  color: var(--iot-text-secondary);
  line-height: 1.4;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  margin-top: auto;
  padding-top: 10px;
  border-top: 1px solid var(--iot-border-color);
}

.margin-top {
  margin-top: 10px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}

.drawer-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.schema-title {
  font-size: 0.9rem;
  color: var(--iot-color-cyan);
  margin-bottom: 8px;
}

.schema-item {
  background: var(--iot-bg-primary);
  border: 1px solid var(--iot-border-color);
  padding: 10px 12px;
  border-radius: 6px;
  margin-bottom: 8px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.s-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.s-name {
  font-size: 0.85rem;
  font-weight: 500;
}

.s-default {
  font-size: 0.8rem;
  color: var(--iot-text-muted);
}
</style>
