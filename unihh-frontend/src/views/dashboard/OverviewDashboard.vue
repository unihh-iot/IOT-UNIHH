<!--
  Author: deversun
  Website: https://unihh.com
  License: Apache License 2.0
-->
<template>
  <div class="dashboard-container">
    <!-- Top Header -->
    <div class="page-header">
      <div class="header-title-group">
        <h2 class="iot-header-title">
          <SvgIcon name="activity" :size="24" color="#06b6d4" />
          工业物联网全局监控仪表盘 (IoT Operations Command Center)
        </h2>
        <span class="header-subtitle">
          全景监控驱动插件、数采设备、物模型标准、规则引擎及消息吞吐量运行状态
        </span>
      </div>
      <div class="header-actions">
        <el-button 
          :type="autoRefresh ? 'success' : 'default'" 
          plain 
          size="default"
          @click="toggleAutoRefresh"
        >
          <SvgIcon name="refresh" :size="16" />
          {{ autoRefresh ? '自动刷新中 (5s)' : '开启自动刷新' }}
        </el-button>
        <el-button type="primary" size="default" @click="refreshDashboard">
          <SvgIcon name="refresh" :size="16" />
          手动刷新
        </el-button>
      </div>
    </div>

    <!-- Top Metric Cards Grid (6 High-Tech Glowing Cards) -->
    <div class="metrics-grid">
      <!-- 1. Drivers -->
      <div class="iot-card metric-card grad-cyan">
        <div class="metric-icon icon-cyan">
          <SvgIcon name="cpu" :size="24" color="#06b6d4" />
        </div>
        <div class="metric-info">
          <span class="metric-label">挂载协议驱动数</span>
          <div class="metric-val-wrap">
            <span class="metric-value text-cyan">{{ stats.drivers.total }}</span>
            <span class="metric-unit">个驱动</span>
          </div>
          <div class="metric-sub">
            <span class="sub-dot online"></span> {{ stats.drivers.active }} 已激活
            <span class="sub-dot idle" style="margin-left: 8px;"></span> {{ stats.drivers.inactive }} 待加载
          </div>
        </div>
      </div>

      <!-- 2. Devices -->
      <div class="iot-card metric-card grad-green">
        <div class="metric-icon icon-green">
          <SvgIcon name="server" :size="24" color="#10b981" />
        </div>
        <div class="metric-info">
          <span class="metric-label">接入数采设备总数</span>
          <div class="metric-val-wrap">
            <span class="metric-value text-green">{{ stats.devices.total }}</span>
            <span class="metric-unit">台设备</span>
          </div>
          <div class="metric-sub">
            <span class="sub-dot online"></span> {{ stats.devices.online }} 在线 ({{ stats.devices.onlineRate }})
            <span class="sub-dot offline" style="margin-left: 8px;"></span> {{ stats.devices.offline }} 离线
          </div>
        </div>
      </div>

      <!-- 3. Thing Models -->
      <div class="iot-card metric-card grad-purple">
        <div class="metric-icon icon-purple">
          <SvgIcon name="layers" :size="24" color="#a855f7" />
        </div>
        <div class="metric-info">
          <span class="metric-label">物模型定义标准</span>
          <div class="metric-val-wrap">
            <span class="metric-value text-purple">{{ stats.thingModels.totalModels }}</span>
            <span class="metric-unit">套模型</span>
          </div>
          <div class="metric-sub">
            <span>{{ stats.thingModels.properties }} 属性 / {{ stats.thingModels.services }} 服务 / {{ stats.thingModels.events }} 事件</span>
          </div>
        </div>
      </div>

      <!-- 4. Rules -->
      <div class="iot-card metric-card grad-indigo">
        <div class="metric-icon icon-indigo">
          <SvgIcon name="refresh" :size="24" color="#6366f1" />
        </div>
        <div class="metric-info">
          <span class="metric-label">规则引擎联动链路</span>
          <div class="metric-val-wrap">
            <span class="metric-value text-indigo">{{ stats.rules.total }}</span>
            <span class="metric-unit">条规则</span>
          </div>
          <div class="metric-sub">
            <span class="sub-dot online"></span> {{ stats.rules.enabled }} 启用中
            <span class="sub-dot idle" style="margin-left: 8px;"></span> {{ stats.rules.disabled }} 已暂停
          </div>
        </div>
      </div>

      <!-- 5. Telemetry Throughput -->
      <div class="iot-card metric-card grad-orange">
        <div class="metric-icon icon-orange">
          <SvgIcon name="activity" :size="24" color="#f59e0b" />
        </div>
        <div class="metric-info">
          <span class="metric-label">数据吞吐采集总量</span>
          <div class="metric-val-wrap">
            <span class="metric-value text-orange">{{ stats.throughput.todayCount }}</span>
            <span class="metric-unit">条记录</span>
          </div>
          <div class="metric-sub">
            <span>平均响应: <strong style="color: #f59e0b;">{{ stats.throughput.avgLatencyMs }}</strong></span>
          </div>
        </div>
      </div>

      <!-- 6. Active Alarms -->
      <div class="iot-card metric-card grad-red">
        <div class="metric-icon icon-red">
          <SvgIcon name="bell" :size="24" color="#ef4444" />
        </div>
        <div class="metric-info">
          <span class="metric-label">待处理告警事件</span>
          <div class="metric-val-wrap">
            <span class="metric-value text-red">{{ stats.alarms.pending }}</span>
            <span class="metric-unit">起事件</span>
          </div>
          <div class="metric-sub">
            <span class="alarm-tag critical">{{ stats.alarms.critical }} 紧急</span>
            <span class="alarm-tag high">{{ stats.alarms.high }} 高风险</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Charts Row 1: Telemetry Line Chart & Protocol Pie Chart -->
    <div class="charts-row">
      <!-- Line Chart: 24h Ingestion vs Dispatch Trend -->
      <div class="iot-card chart-card flex-2">
        <div class="card-header-line">
          <div class="card-title">
            <SvgIcon name="activity" :size="18" color="#06b6d4" />
            <span>24小时采集数据吞吐与反控指令下发趋势 (Msg/min)</span>
          </div>
          <div class="card-extra">
            <el-tag size="small" type="info" effect="dark">实时窗口: 00:00 - 24:00</el-tag>
          </div>
        </div>
        <div ref="lineChartRef" class="echarts-box"></div>
      </div>

      <!-- Pie Chart: Device Protocol Distribution -->
      <div class="iot-card chart-card flex-1">
        <div class="card-header-line">
          <div class="card-title">
            <SvgIcon name="pie-chart" :size="18" color="#a855f7" />
            <span>接入设备通信协议分布</span>
          </div>
        </div>
        <div ref="protocolPieRef" class="echarts-box"></div>
      </div>
    </div>

    <!-- Charts Row 2: Driver Throughput Bar & Alarm Level Doughnut -->
    <div class="charts-row margin-top-sm">
      <!-- Bar Chart: Top 5 Driver Communication Volume -->
      <div class="iot-card chart-card flex-1">
        <div class="card-header-line">
          <div class="card-title">
            <SvgIcon name="bar-chart" :size="18" color="#10b981" />
            <span>驱动协议通信包吞吐 TOP 5 (万条/日)</span>
          </div>
        </div>
        <div ref="driverBarRef" class="echarts-box"></div>
      </div>

      <!-- Doughnut Chart: Alarm Level Distribution -->
      <div class="iot-card chart-card flex-1">
        <div class="card-header-line">
          <div class="card-title">
            <SvgIcon name="alert-circle" :size="18" color="#ef4444" />
            <span>告警事件等级分布统计</span>
          </div>
        </div>
        <div ref="alarmPieRef" class="echarts-box"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import SvgIcon from '../../components/common/SvgIcon.vue'
import { getGatewayStatus } from '../../api/gateway'
import { getDashboardStats } from '../../api/dashboard'

// Real Dashboard API Data State
const stats = ref({
  drivers: { total: 3, active: 3, inactive: 0 },
  devices: { total: 3, online: 2, offline: 1, error: 0, onlineRate: '66.7%' },
  thingModels: { totalModels: 3, properties: 11, services: 3, events: 3 },
  rules: { total: 3, enabled: 2, disabled: 1 },
  throughput: { todayCount: 15, avgLatencyMs: '18.6 ms' },
  alarms: { total: 3, pending: 1, confirmed: 1, resolved: 1, critical: 1, high: 1, warn: 1, info: 0 },
  protocolDistribution: [],
  alarmLevelDistribution: [],
  driverThroughput: { categories: [], values: [] },
  hourlyTrends: { hours: [], telemetry: [], control: [] },
  microservices: []
})

// State
const autoRefresh = ref(false)
let refreshTimer = null

// Chart Refs
const lineChartRef = ref(null)
const protocolPieRef = ref(null)
const driverBarRef = ref(null)
const alarmPieRef = ref(null)

let lineChartInst = null
let protocolPieInst = null
let driverBarInst = null
let alarmPieInst = null

// Microservices Data
const microservicesList = ref([
  { serviceId: 'unihh-gateway', name: 'API 网关中心', address: '127.0.0.1:28000', grpcPort: null, duty: '客户端统一路由、JWT token 鉴权与限流熔断', cpu: 12, mem: 240, status: 1 },
  { serviceId: 'unihh-admin', name: '后台管理中心', address: '127.0.0.1:28001', grpcPort: '29001', duty: 'RBAC 权限、设备生命周期注册、物模型定义管理', cpu: 18, mem: 480, status: 1 },
  { serviceId: 'unihh-data', name: '数据与规则引擎中心', address: '127.0.0.1:28002', grpcPort: '29002', duty: '时序数据清洗、1:N 规则计算与指令反控下发', cpu: 28, mem: 620, status: 1 },
  { serviceId: 'unihh-protocol', name: '协议 SDK 通道中心', address: '127.0.0.1:28003', grpcPort: '29003', duty: 'Netty 高并发长连接通信、SPI 驱动动态加载编解码', cpu: 34, mem: 890, status: 1 }
])

// Init Charts
const initLineChart = () => {
  if (!lineChartRef.value) return
  lineChartInst = echarts.init(lineChartRef.value)
  
  const hours = Array.from({ length: 24 }, (_, i) => `${String(i).padStart(2, '0')}:00`)
  const telemetryData = [1200, 980, 850, 600, 750, 1400, 3200, 4800, 6200, 7500, 8100, 7900, 8400, 8200, 7800, 8300, 9100, 8800, 7600, 6400, 4900, 3800, 2500, 1800]
  const controlData = [12, 5, 3, 2, 4, 18, 45, 89, 120, 142, 135, 110, 156, 140, 128, 160, 185, 172, 115, 80, 52, 34, 21, 15]

  const option = {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(15, 23, 42, 0.9)',
      borderColor: 'rgba(51, 65, 85, 0.8)',
      textStyle: { color: '#f8fafc' },
      axisPointer: { type: 'cross', label: { backgroundColor: '#0284c7' } }
    },
    legend: {
      data: ['采集数据量 (条/分)', '反控服务下发 (次/分)'],
      textStyle: { color: '#94a3b8' },
      top: 0,
      left: 'center'
    },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '40px', containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: hours,
      axisLine: { lineStyle: { color: '#334155' } },
      axisLabel: { color: '#94a3b8' }
    },
    yAxis: [
      {
        type: 'value',
        name: '采集条数',
        nameTextStyle: { color: '#06b6d4' },
        axisLine: { show: false },
        splitLine: { lineStyle: { color: 'rgba(51, 65, 85, 0.4)', type: 'dashed' } },
        axisLabel: { color: '#94a3b8' }
      },
      {
        type: 'value',
        name: '下发次数',
        nameTextStyle: { color: '#a855f7' },
        axisLine: { show: false },
        splitLine: { show: false },
        axisLabel: { color: '#94a3b8' }
      }
    ],
    series: [
      {
        name: '采集数据量 (条/分)',
        type: 'line',
        smooth: true,
        showSymbol: false,
        lineStyle: { width: 3, color: '#06b6d4' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(6, 182, 212, 0.4)' },
            { offset: 1, color: 'rgba(6, 182, 212, 0.0)' }
          ])
        },
        data: telemetryData
      },
      {
        name: '反控服务下发 (次/分)',
        type: 'line',
        yAxisIndex: 1,
        smooth: true,
        showSymbol: false,
        lineStyle: { width: 3, color: '#a855f7' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(168, 85, 247, 0.3)' },
            { offset: 1, color: 'rgba(168, 85, 247, 0.0)' }
          ])
        },
        data: controlData
      }
    ]
  }

  lineChartInst.setOption(option)
}

const initProtocolPie = () => {
  if (!protocolPieRef.value) return
  protocolPieInst = echarts.init(protocolPieRef.value)

  const option = {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(15, 23, 42, 0.9)',
      borderColor: 'rgba(51, 65, 85, 0.8)',
      textStyle: { color: '#f8fafc' },
      formatter: '{b}: {c} 台 ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: '5%',
      top: 'center',
      textStyle: { color: '#94a3b8' }
    },
    series: [
      {
        name: '协议分布',
        type: 'pie',
        radius: ['45%', '72%'],
        center: ['38%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 6,
          borderColor: '#0f172a',
          borderWidth: 2
        },
        label: { show: false, position: 'center' },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold',
            color: '#38bdf8',
            formatter: '{b}\n{d}%'
          }
        },
        data: [
          { value: 52, name: 'Modbus-TCP', itemStyle: { color: '#06b6d4' } },
          { value: 38, name: 'MQTT', itemStyle: { color: '#10b981' } },
          { value: 18, name: 'OPC-UA', itemStyle: { color: '#a855f7' } },
          { value: 12, name: 'Siemens S7', itemStyle: { color: '#f59e0b' } },
          { value: 8, name: 'BACnet / 其他', itemStyle: { color: '#64748b' } }
        ]
      }
    ]
  }

  protocolPieInst.setOption(option)
}

const initDriverBar = () => {
  if (!driverBarRef.value) return
  driverBarInst = echarts.init(driverBarRef.value)

  const option = {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(15, 23, 42, 0.9)',
      borderColor: 'rgba(51, 65, 85, 0.8)',
      textStyle: { color: '#f8fafc' }
    },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '15%', containLabel: true },
    xAxis: {
      type: 'category',
      data: ['Modbus Driver', 'MQTT Client', 'OPC-UA Server', 'Siemens S7', 'BACnet/IP'],
      axisLine: { lineStyle: { color: '#334155' } },
      axisLabel: { color: '#94a3b8', interval: 0, fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      name: '吞吐包(万条)',
      nameTextStyle: { color: '#94a3b8' },
      splitLine: { lineStyle: { color: 'rgba(51, 65, 85, 0.4)', type: 'dashed' } },
      axisLabel: { color: '#94a3b8' }
    },
    series: [
      {
        name: '日数据吞吐量',
        type: 'bar',
        barWidth: '35%',
        itemStyle: {
          borderRadius: [4, 4, 0, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#10b981' },
            { offset: 1, color: 'rgba(16, 185, 129, 0.2)' }
          ])
        },
        data: [142, 98, 45, 28, 12]
      }
    ]
  }

  driverBarInst.setOption(option)
}

const initAlarmPie = () => {
  if (!alarmPieRef.value) return
  alarmPieInst = echarts.init(alarmPieRef.value)

  const option = {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(15, 23, 42, 0.9)',
      borderColor: 'rgba(51, 65, 85, 0.8)',
      textStyle: { color: '#f8fafc' },
      formatter: '{b}: {c} 起 ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: '5%',
      top: 'center',
      textStyle: { color: '#94a3b8' }
    },
    series: [
      {
        name: '告警等级',
        type: 'pie',
        radius: ['45%', '72%'],
        center: ['38%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 6,
          borderColor: '#0f172a',
          borderWidth: 2
        },
        data: [
          { value: 1, name: 'CRITICAL (紧急)', itemStyle: { color: '#ef4444' } },
          { value: 2, name: 'HIGH (高风险)', itemStyle: { color: '#f97316' } },
          { value: 5, name: 'WARN (警告)', itemStyle: { color: '#eab308' } },
          { value: 12, name: 'INFO (提示)', itemStyle: { color: '#3b82f6' } }
        ]
      }
    ]
  }

  alarmPieInst.setOption(option)
}

// Window resize handler
const handleResize = () => {
  lineChartInst?.resize()
  protocolPieInst?.resize()
  driverBarInst?.resize()
  alarmPieInst?.resize()
}

// Update ECharts with real data from backend
const updateCharts = (data) => {
  if (!data) return

  // 1. Line Chart: 24h Telemetry & Control Trends
  if (lineChartInst && data.hourlyTrends) {
    lineChartInst.setOption({
      xAxis: {
        data: data.hourlyTrends.hours || []
      },
      series: [
        { data: data.hourlyTrends.telemetry || [] },
        { data: data.hourlyTrends.control || [] }
      ]
    })
  }

  // 2. Pie Chart: Protocol Distribution
  if (protocolPieInst && data.protocolDistribution && data.protocolDistribution.length > 0) {
    protocolPieInst.setOption({
      series: [
        {
          data: data.protocolDistribution
        }
      ]
    })
  }

  // 3. Bar Chart: Driver Throughput Top 5
  if (driverBarInst && data.driverThroughput) {
    driverBarInst.setOption({
      xAxis: {
        data: data.driverThroughput.categories || []
      },
      series: [
        {
          data: data.driverThroughput.values || []
        }
      ]
    })
  }

  // 4. Doughnut Chart: Alarm Level Distribution
  if (alarmPieInst && data.alarmLevelDistribution && data.alarmLevelDistribution.length > 0) {
    alarmPieInst.setOption({
      series: [
        {
          data: data.alarmLevelDistribution
        }
      ]
    })
  }
}

// Controls: 对接真实后端统计接口
const refreshDashboard = async () => {
  try {
    const data = await getDashboardStats()
    if (data) {
      stats.value = { ...stats.value, ...data }
      if (data.microservices && data.microservices.length > 0) {
        microservicesList.value = data.microservices
      }
      updateCharts(data)
    }
  } catch (err) {
    console.warn('[仪表盘真实接口调用提示]', err)
  }

  try {
    const gw = await getGatewayStatus()
    if (gw && gw.microservices) {
      microservicesList.value = gw.microservices
    }
  } catch (ignored) {}
}

const toggleAutoRefresh = () => {
  autoRefresh.value = !autoRefresh.value
  if (autoRefresh.value) {
    refreshTimer = setInterval(() => {
      refreshDashboard()
    }, 5000)
  } else {
    if (refreshTimer) clearInterval(refreshTimer)
  }
}

onMounted(() => {
  nextTick(async () => {
    initLineChart()
    initProtocolPie()
    initDriverBar()
    initAlarmPie()
    window.addEventListener('resize', handleResize)
    await refreshDashboard()
  })
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (refreshTimer) clearInterval(refreshTimer)
  lineChartInst?.dispose()
  protocolPieInst?.dispose()
  driverBarInst?.dispose()
  alarmPieInst?.dispose()
})
</script>

<style scoped>
.dashboard-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: rgba(15, 23, 42, 0.6);
  padding: 16px 20px;
  border-radius: 10px;
  border: 1px solid rgba(51, 65, 85, 0.5);
  backdrop-filter: blur(8px);
}

.header-title-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.header-subtitle {
  font-size: 0.85rem;
  color: #94a3b8;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* Metrics Cards Grid */
.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(210px, 1fr));
  gap: 16px;
}

.metric-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  border-radius: 10px;
  position: relative;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.08);
  transition: all 0.25s ease;
}

.metric-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.35);
}

/* Gradients */
.grad-cyan { background: linear-gradient(135deg, rgba(6, 182, 212, 0.12) 0%, rgba(15, 23, 42, 0.8) 100%); }
.grad-green { background: linear-gradient(135deg, rgba(16, 185, 129, 0.12) 0%, rgba(15, 23, 42, 0.8) 100%); }
.grad-purple { background: linear-gradient(135deg, rgba(168, 85, 247, 0.12) 0%, rgba(15, 23, 42, 0.8) 100%); }
.grad-indigo { background: linear-gradient(135deg, rgba(99, 102, 241, 0.12) 0%, rgba(15, 23, 42, 0.8) 100%); }
.grad-orange { background: linear-gradient(135deg, rgba(245, 158, 11, 0.12) 0%, rgba(15, 23, 42, 0.8) 100%); }
.grad-red { background: linear-gradient(135deg, rgba(239, 68, 68, 0.12) 0%, rgba(15, 23, 42, 0.8) 100%); }

.metric-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 10px;
  flex-shrink: 0;
}

.icon-cyan { background: rgba(6, 182, 212, 0.2); }
.icon-green { background: rgba(16, 185, 129, 0.2); }
.icon-purple { background: rgba(168, 85, 247, 0.2); }
.icon-indigo { background: rgba(99, 102, 241, 0.2); }
.icon-orange { background: rgba(245, 158, 11, 0.2); }
.icon-red { background: rgba(239, 68, 68, 0.2); }

.metric-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  flex: 1;
}

.metric-label {
  font-size: 0.8rem;
  color: #94a3b8;
  font-weight: 500;
}

.metric-val-wrap {
  display: flex;
  align-items: baseline;
  gap: 6px;
}

.metric-value {
  font-size: 1.5rem;
  font-weight: 800;
  line-height: 1.2;
}

.metric-unit {
  font-size: 0.75rem;
  color: #64748b;
}

.metric-sub {
  font-size: 0.75rem;
  color: #94a3b8;
  margin-top: 4px;
  display: flex;
  align-items: center;
}

.sub-dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  margin-right: 4px;
}
.sub-dot.online { background: #10b981; box-shadow: 0 0 6px #10b981; }
.sub-dot.offline { background: #ef4444; }
.sub-dot.idle { background: #64748b; }

.alarm-tag {
  font-size: 0.7rem;
  padding: 1px 6px;
  border-radius: 4px;
  margin-right: 4px;
  font-weight: 600;
}
.alarm-tag.critical { background: rgba(239, 68, 68, 0.2); color: #ef4444; border: 1px solid rgba(239, 68, 68, 0.4); }
.alarm-tag.high { background: rgba(249, 115, 22, 0.2); color: #f97316; border: 1px solid rgba(249, 115, 22, 0.4); }

.text-cyan { color: #38bdf8; }
.text-green { color: #34d399; }
.text-purple { color: #c084fc; }
.text-indigo { color: #818cf8; }
.text-orange { color: #fbbf24; }
.text-red { color: #f87171; }

/* Charts Layout */
.charts-row {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.margin-top-sm {
  margin-top: 4px;
}

.chart-card {
  background: rgba(15, 23, 42, 0.75);
  border: 1px solid rgba(51, 65, 85, 0.6);
  border-radius: 10px;
  padding: 16px;
  display: flex;
  flex-direction: column;
}

.flex-1 { flex: 1; min-width: 320px; }
.flex-2 { flex: 2; min-width: 480px; }

.card-header-line {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.95rem;
  font-weight: 600;
  color: #f8fafc;
}

.echarts-box {
  width: 100%;
  height: 280px;
}

/* Code & Status Badges */
.code-badge {
  font-family: monospace;
  font-size: 0.8rem;
  padding: 2px 8px;
  border-radius: 4px;
}
.code-badge.cyan { background: rgba(6, 182, 212, 0.15); color: #38bdf8; border: 1px solid rgba(6, 182, 212, 0.3); }
.code-badge.purple { background: rgba(168, 85, 247, 0.15); color: #c084fc; border: 1px solid rgba(168, 85, 247, 0.3); }

.load-text {
  font-family: monospace;
  font-size: 0.85rem;
  color: #cbd5e1;
}

.text-muted {
  color: #64748b;
}

/* Custom Table Dark Adjustments */
:deep(.el-table) {
  --el-table-bg-color: transparent !important;
  --el-table-tr-bg-color: transparent !important;
  --el-table-header-bg-color: rgba(30, 41, 59, 0.8) !important;
  --el-table-border-color: rgba(51, 65, 85, 0.5) !important;
  --el-table-text-color: #cbd5e1 !important;
  --el-table-header-text-color: #f8fafc !important;
  --el-table-row-hover-bg-color: rgba(30, 41, 59, 0.9) !important;
}
</style>
