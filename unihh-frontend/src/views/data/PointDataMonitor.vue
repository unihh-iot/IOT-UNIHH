<template>
  <div class="page-container">
    <!-- Page Header & Global Controls -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="iot-header-title">
          <SvgIcon name="database" :size="24" color="#8b5cf6" />
          数据管理 (实时采集点位监控与历史数据回溯)
        </h2>
        <span class="header-subtitle"
          >点位映射、实时遥测快照与时序历史回溯分析</span
        >
      </div>

      <div class="header-right">
        <!-- Real-time SSE Telemetry Push Toggle & Status -->
        <div class="sse-status-wrap">
          <div class="sse-indicator" :class="sseStatusClass">
            <span class="pulse-dot"></span>
            <span class="status-text">{{ sseStatusText }}</span>
          </div>
          <el-switch
            v-model="sseEnabled"
            active-text="实时数据"
            inactive-text="手动"
            @change="handleSseToggle"
          />
        </div>
      </div>
    </div>

    <!-- 1. Device Selection Bar (设备选择区 - 核心步骤1) -->
    <div class="iot-card device-selector-card">
      <div class="selector-container">
        <div class="selector-label-wrap">
          <SvgIcon name="cpu" :size="20" color="#10b981" />
          <span class="selector-title">选择目标设备:</span>
        </div>
        <el-select
          v-model="selectedDeviceId"
          placeholder="-- 请选择物理设备以查看采集点位 --"
          filterable
          clearable
          size="large"
          class="device-select-dropdown"
          @change="handleDeviceChange"
        >
          <el-option
            v-for="dev in deviceList"
            :key="dev.deviceId"
            :label="`${dev.deviceName} (${dev.deviceId})`"
            :value="dev.deviceId"
          >
            <div class="device-option-item">
              <div class="opt-info">
                <span class="opt-name">{{ dev.deviceName }}</span>
                <code class="opt-id">{{ dev.deviceId }}</code>
              </div>
              <div class="opt-tags">
                <el-tag
                  size="small"
                  :type="dev.status === 1 ? 'success' : 'info'"
                >
                  {{ dev.status === 1 ? "在线" : "离线" }}
                </el-tag>
                <el-tag size="small" type="primary" plain>{{
                  dev.driverCode
                }}</el-tag>
              </div>
            </div>
          </el-option>
        </el-select>
        <el-button
          type="primary"
          plain
          size="large"
          @click="loadDevices"
          :loading="loadingDevices"
        >
          <SvgIcon name="refresh" :size="16" style="margin-right: 4px" />
          刷新设备列表
        </el-button>
      </div>

      <!-- Selected Device Summary Banner -->
      <div v-if="currentDevice" class="device-summary-banner">
        <div class="banner-item">
          <span class="banner-lbl">当前设备:</span>
          <span class="banner-val highlight">{{
            currentDevice.deviceName
          }}</span>
          <code class="banner-id">[{{ currentDevice.deviceId }}]</code>
        </div>
        <div class="banner-item">
          <span class="banner-lbl">物模型模板:</span>
          <el-tag size="small" type="success">{{
            currentDevice.modelKey
          }}</el-tag>
        </div>
        <div class="banner-item">
          <span class="banner-lbl">协议驱动:</span>
          <span class="banner-val">{{ currentDevice.driverCode }}</span>
        </div>
        <div class="banner-item">
          <span class="banner-lbl">网络通道:</span>
          <StatusBadge
            :status="currentDevice.status"
            :text="currentDevice.status === 1 ? '在线' : '离线'"
          />
        </div>
        <div class="banner-item">
          <span class="banner-lbl">数采状态:</span>
          <el-tag
            size="small"
            :type="currentDevice.acqStatus === 1 ? 'success' : 'warning'"
          >
            {{ currentDevice.acqStatus === 1 ? "数采运行中" : "已暂停" }}
          </el-tag>
        </div>
        <div class="banner-item">
          <span class="banner-lbl">设备基频周期:</span>
          <el-tag size="small" type="primary" plain
            >{{ currentDevice.baseIntervalMs || 1000 }} ms</el-tag
          >
        </div>
        <div class="banner-item">
          <span class="banner-lbl">点位数:</span>
          <span class="banner-val count-badge">{{ pointList.length }} 个</span>
        </div>
        <div class="banner-actions">
          <el-button
            size="small"
            type="success"
            plain
            @click="refreshLiveValues"
            :loading="loadingLive"
          >
            <SvgIcon name="refresh" :size="14" style="margin-right: 4px" />
            刷新数据
          </el-button>
        </div>
      </div>
    </div>

    <!-- 2. Unselected Device Empty State (未选择设备时的提示) -->
    <div v-if="!selectedDeviceId" class="iot-card unselected-empty-card">
      <div class="empty-content">
        <div class="empty-icon-circle">
          <SvgIcon name="database" :size="48" color="#8b5cf6" />
        </div>
        <h3>请先在上方选择目标设备</h3>
        <p>
          系统需要确认具体物理设备后，方可加载并呈现该设备关联的所有物模型点位数据卡片及实时采集数据。
        </p>

        <div class="quick-devices-section" v-if="deviceList.length > 0">
          <span class="quick-label">快捷选择常用设备：</span>
          <div class="quick-buttons">
            <el-button
              v-for="dev in deviceList.slice(0, 5)"
              :key="dev.deviceId"
              type="primary"
              plain
              size="default"
              @click="
                selectedDeviceId = dev.deviceId;
                handleDeviceChange(dev.deviceId);
              "
            >
              <SvgIcon name="cpu" :size="14" style="margin-right: 6px" />
              {{ dev.deviceName }} ({{ dev.deviceId }})
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 3. Point Query & Filtering Bar (选择完设备后可以查询点位 - 核心步骤2) -->
    <template v-else>
      <div class="iot-card filter-card">
        <div class="filter-header">
          <div class="filter-title">
            <SvgIcon name="filter" :size="18" color="#06b6d4" />
            <span>点位数据查询与筛选</span>
            <span class="filter-count"
              >（已找到 {{ filteredPoints.length }} /
              {{ pointList.length }} 个点位）</span
            >
          </div>
          <div class="view-mode-toggle">
            <el-radio-group v-model="viewMode" size="small">
              <el-radio-button value="card">
                <SvgIcon name="activity" :size="14" style="margin-right: 4px" />
                数据卡片视图
              </el-radio-button>
              <el-radio-button value="table">
                <SvgIcon
                  name="file-text"
                  :size="14"
                  style="margin-right: 4px"
                />
                列表视图
              </el-radio-button>
            </el-radio-group>
          </div>
        </div>

        <el-form
          :inline="true"
          :model="queryForm"
          class="filter-form"
          @submit.prevent="handleFilter"
        >
          <el-form-item label="点位查找">
            <el-input
              v-model="queryForm.keyword"
              placeholder="搜索点位 Code / 名称 / 地址"
              clearable
              style="width: 220px"
              @keyup.enter="handleFilter"
            />
          </el-form-item>
          <el-form-item label="数据类型">
            <el-select
              v-model="queryForm.dataType"
              placeholder="全部类型"
              clearable
              style="width: 140px"
            >
              <el-option label="全部类型" value="" />
              <el-option label="FLOAT (单精度浮点)" value="FLOAT" />
              <el-option label="DOUBLE (双精度浮点)" value="DOUBLE" />
              <el-option label="INT (整数)" value="INT" />
              <el-option label="BOOL (开关量)" value="BOOL" />
              <el-option label="STRING (文本)" value="STRING" />
              <el-option label="ENUM (枚举)" value="ENUM" />
            </el-select>
          </el-form-item>
          <el-form-item label="读写权限">
            <el-select
              v-model="queryForm.accessMode"
              placeholder="全部"
              clearable
              style="width: 120px"
            >
              <el-option label="全部" value="" />
              <el-option label="只读 (R)" value="R" />
              <el-option label="读写 (RW)" value="RW" />
            </el-select>
          </el-form-item>
          <el-form-item label="质量状态">
            <el-select
              v-model="queryForm.quality"
              placeholder="全部"
              clearable
              style="width: 120px"
            >
              <el-option label="全部" value="" />
              <el-option label="正常 (Quality=1)" :value="1" />
              <el-option label="异常 (Quality=0)" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleFilter">
              <SvgIcon name="filter" :size="14" style="margin-right: 4px" />
              查询
            </el-button>
            <el-button @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 4. Point Data Cards Grid View (点位数据卡片当前采集数据 - 核心步骤3) -->
      <div v-if="viewMode === 'card'" class="point-cards-grid margin-top">
        <div
          v-for="point in filteredPoints"
          :key="point.pointCode"
          class="iot-card point-card"
          @click="openHistoryDialog(point)"
        >
          <!-- Card Header -->
          <div class="point-card-header">
            <div class="point-name-box">
              <span class="point-name">{{
                point.pointName || point.pointCode
              }}</span>
              <code class="point-code">{{ point.pointCode }}</code>
            </div>
            <div class="point-badges">
              <el-tag
                size="small"
                :type="point.accessMode === 'RW' ? 'warning' : 'info'"
              >
                {{ point.accessMode || "R" }}
              </el-tag>
              <StatusBadge
                :status="point.quality"
                :text="point.quality === 1 ? '正常' : '异常'"
              />
            </div>
          </div>

          <!-- Card Body: Large Live Value Display -->
          <div class="point-card-body">
            <div class="live-value-wrap">
              <!-- Boolean Value Display -->
              <template v-if="point.dataType === 'BOOL'">
                <div
                  class="bool-value-box"
                  :class="{ 'is-on': point.value == 1 || point.value === true }"
                >
                  <span class="bool-pulse"></span>
                  <span class="bool-text">{{
                    point.value == 1 || point.value === true
                      ? "ON (开启/运行)"
                      : "OFF (关闭/停机)"
                  }}</span>
                </div>
              </template>

              <!-- Enum Value Display with Explanation -->
              <template v-else-if="point.dataType === 'ENUM'">
                <div class="enum-value-box">
                  <el-tag
                    size="large"
                    type="warning"
                    effect="dark"
                    class="enum-val-badge"
                  >
                    {{ getEnumDisplayWithCode(point, point.value) }}
                  </el-tag>
                </div>
              </template>

              <!-- String Value Display -->
              <template v-else-if="point.dataType === 'STRING'">
                <div class="string-value-box">
                  <span class="val-str">{{ point.value }}</span>
                </div>
              </template>

              <!-- Numeric Value (INT / DOUBLE) Display -->
              <template v-else>
                <div class="numeric-value-box">
                  <span class="live-num">{{
                    formatNumericValue(point.value)
                  }}</span>
                  <span v-if="point.unit" class="unit-text">{{
                    point.unit
                  }}</span>
                </div>
              </template>
            </div>

            <!-- Mini Sparkline indicator bar -->
            <div class="sparkline-bar">
              <div
                class="spark-fill"
                :style="{ width: getSparklineWidth(point) + '%' }"
              ></div>
            </div>

            <!-- Specs details footer inside card body -->
            <div class="point-specs-grid">
              <div class="spec-item">
                <span class="spec-lbl">数据类型:</span>
                <span class="spec-val">{{ point.dataType || "DOUBLE" }}</span>
              </div>
              <div class="spec-item">
                <span class="spec-lbl">物理地址:</span>
                <code class="spec-val-code">{{ point.address || "--" }}</code>
              </div>
              <div class="spec-item full-width">
                <span class="spec-lbl">数采周期:</span>
                <span class="spec-val" style="color: #38bdf8; font-weight: 500">
                  {{
                    point.triggerMode === "COV"
                      ? "COV 变化触发"
                      : `${(currentDevice?.baseIntervalMs || 1000) * (point.sampleFactor || 1)}ms (${point.sampleFactor || 1}x倍数)`
                  }}
                </span>
              </div>
              <div class="spec-item full-width">
                <span class="spec-lbl">最新采集时间:</span>
                <span class="spec-val time">{{
                  formatTime(point.timestamp)
                }}</span>
              </div>
            </div>
          </div>

          <!-- Card Click Prompt Action Footer -->
          <div class="point-card-footer">
            <span class="click-tip">
              <SvgIcon
                name="activity"
                :size="14"
                color="#06b6d4"
                style="margin-right: 4px"
              />
              点击查看历史趋势
            </span>
            <div class="card-footer-btns">
              <el-button
                size="small"
                type="primary"
                link
                @click.stop="openHistoryDialog(point)"
              >
                历史趋势 &gt;
              </el-button>
            </div>
          </div>
        </div>

        <div v-if="filteredPoints.length === 0" class="no-points-card">
          <el-empty description="未找到符合条件的点位数据" />
        </div>
      </div>

      <!-- 4. Table View Mode Alternative -->
      <div v-else class="iot-card margin-top">
        <el-table
          :data="paginatedTablePoints"
          style="width: 100%"
          stripe
          @row-click="openHistoryDialog"
        >
          <el-table-column prop="pointCode" label="点位 Code" width="160">
            <template #default="scope">
              <code>{{ scope.row.pointCode }}</code>
            </template>
          </el-table-column>
          <el-table-column prop="pointName" label="点位名称" min-width="160">
            <template #default="scope">
              <strong>{{ scope.row.pointName }}</strong>
            </template>
          </el-table-column>
          <el-table-column label="实时采集数值" width="200">
            <template #default="scope">
              <template v-if="scope.row.dataType === 'ENUM'">
                <div class="table-enum-cell">
                  <el-tag size="small" type="warning" effect="dark">
                    {{ getEnumLabel(scope.row, scope.row.value) }}
                  </el-tag>
                  <code class="enum-mini-code">[{{ scope.row.value }}]</code>
                </div>
              </template>
              <span v-else class="val-highlight">
                {{ formatNumericValue(scope.row.value) }}
                {{ scope.row.unit || "" }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="address" label="物理地址" width="130">
            <template #default="scope">
              <code>{{ scope.row.address || "--" }}</code>
            </template>
          </el-table-column>
          <el-table-column prop="dataType" label="数据类型" width="140">
            <template #default="scope">
              <div class="table-datatype-cell">
                <el-tag
                  size="small"
                  :type="scope.row.dataType === 'ENUM' ? 'warning' : (scope.row.dataType === 'FLOAT' ? 'success' : 'primary')"
                  plain
                >
                  {{ scope.row.dataType }}
                </el-tag>
                <el-popover
                  v-if="scope.row.dataType === 'ENUM'"
                  placement="top"
                  :width="260"
                  trigger="hover"
                >
                  <template #reference>
                    <span class="enum-info-icon" title="查看枚举定义说明">
                      <SvgIcon name="info" :size="15" color="#f59e0b" />
                    </span>
                  </template>
                  <div class="enum-popover-content">
                    <div class="popover-title">
                      <strong>枚举定义说明:</strong>
                    </div>
                    <div
                      v-for="opt in getPointEnumOptions(scope.row)"
                      :key="opt.value"
                      class="popover-enum-row"
                      :class="{
                        current: String(opt.value) === String(scope.row.value),
                      }"
                    >
                      <code>{{ opt.value }}</code> :
                      <span>{{ opt.label }}</span>
                      <span
                        v-if="String(opt.value) === String(scope.row.value)"
                        class="curr-mark"
                        >◀ 当前值</span
                      >
                    </div>
                  </div>
                </el-popover>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="accessMode" label="读写权限" width="90">
            <template #default="scope">
              <el-tag
                size="small"
                :type="scope.row.accessMode === 'RW' ? 'warning' : 'info'"
              >
                {{ scope.row.accessMode || "R" }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="质量码" width="110">
            <template #default="scope">
              <StatusBadge
                :status="scope.row.quality"
                :text="scope.row.quality === 1 ? '正常' : '异常'"
              />
            </template>
          </el-table-column>
          <el-table-column label="最新时间" width="180">
            <template #default="scope">
              {{ formatTime(scope.row.timestamp) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" align="center">
            <template #default="scope">
              <el-button
                size="small"
                type="primary"
                plain
                @click.stop="openHistoryDialog(scope.row)"
              >
                历史趋势
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div
          class="table-pagination"
          style="margin-top: 16px; display: flex; justify-content: flex-end"
        >
          <el-pagination
            v-model:current-page="pointTablePage"
            v-model:page-size="pointTablePageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="filteredPoints.length"
          />
        </div>
      </div>
    </template>

    <!-- 5. Point Historical Data View Modal (点位卡片点击查看历史数据 - 核心步骤4) -->
    <el-dialog
      v-model="historyDialogVisible"
      title="点位历史数据监控与趋势分析"
      width="960px"
      destroy-on-close
      class="history-dialog"
      @opened="handleDialogOpened"
      @closed="handleDialogClosed"
    >
      <div v-if="activePoint" class="history-dialog-content">
        <!-- Point Info Header in Dialog -->
        <div class="point-history-header">
          <div class="ph-main-title">
            <span class="ph-name">{{ activePoint.pointName }}</span>
            <code class="ph-code">({{ activePoint.pointCode }})</code>
            <el-tag
              size="small"
              :type="activePoint.dataType === 'ENUM' ? 'warning' : 'success'"
              style="margin-left: 10px"
              >{{ activePoint.dataType }}</el-tag
            >
            <span v-if="activePoint.unit" class="ph-unit"
              >单位: {{ activePoint.unit }}</span
            >
          </div>
          <div class="ph-dev-info">
            所属设备:
            <strong>{{
              currentDevice ? currentDevice.deviceName : selectedDeviceId
            }}</strong>
            <code style="margin-left: 6px">[{{ selectedDeviceId }}]</code>
          </div>
        </div>

        <!-- Filter & Time Range Selector for History -->
        <div class="history-filter-bar">
          <span class="range-lbl">时间开窗范围:</span>
          <el-radio-group
            v-model="historyTimeRange"
            size="small"
            @change="fetchHistoryData"
          >
            <el-radio-button value="15m">近 15 分钟</el-radio-button>
            <el-radio-button value="1h">近 1 小时</el-radio-button>
            <el-radio-button value="24h">近 24 小时</el-radio-button>
            <el-radio-button value="7d">近 7 天</el-radio-button>
          </el-radio-group>

          <div class="history-actions">
            <el-button
              size="small"
              type="primary"
              plain
              @click="fetchHistoryData"
              :loading="loadingHistory"
            >
              <SvgIcon name="refresh" :size="14" style="margin-right: 4px" />
              刷新曲线
            </el-button>
            <el-button size="small" type="info" plain @click="exportHistoryCSV">
              导出 CSV
            </el-button>
          </div>
        </div>

        <!-- Statistics Overview Cards -->
        <div class="stats-overview-grid">
          <div class="stat-card">
            <span class="stat-lbl">实时当前值</span>
            <span
              v-if="activePoint.dataType === 'ENUM'"
              class="stat-val live enum-stat-val"
            >
              {{ getEnumLabel(activePoint, activePoint.value) }} ({{
                activePoint.value !== undefined ? activePoint.value : "--"
              }})
            </span>
            <span v-else class="stat-val live"
              >{{ formatNumericValue(activePoint.value) }}
              {{ activePoint.unit || "" }}</span
            >
          </div>
          <div class="stat-card" v-if="activePoint.dataType !== 'ENUM'">
            <span class="stat-lbl">周期最高值 (Max)</span>
            <span class="stat-val max"
              >{{ historyStats.max }} {{ activePoint.unit || "" }}</span
            >
          </div>
          <div class="stat-card" v-if="activePoint.dataType !== 'ENUM'">
            <span class="stat-lbl">周期最低值 (Min)</span>
            <span class="stat-val min"
              >{{ historyStats.min }} {{ activePoint.unit || "" }}</span
            >
          </div>
          <div class="stat-card" v-if="activePoint.dataType !== 'ENUM'">
            <span class="stat-lbl">周期平均值 (Avg)</span>
            <span class="stat-val avg"
              >{{ historyStats.avg }} {{ activePoint.unit || "" }}</span
            >
          </div>
          <div class="stat-card">
            <span class="stat-lbl">历史样本数</span>
            <span class="stat-val count">{{ historyList.length }} 条</span>
          </div>
        </div>

        <!-- ECharts Historical Trend Curve Chart Container -->
        <div class="chart-container">
          <div
            ref="chartRef"
            class="echarts-box"
            style="width: 100%; height: 320px"
          ></div>
        </div>

        <!-- Historical Data Table Details -->
        <div class="history-table-section">
          <div class="section-title">
            <span>历史采集数据明细列表 (来自后端存储引擎时序缓冲)</span>
          </div>
          <el-table
            :data="paginatedHistory"
            style="width: 100%"
            size="small"
            stripe
            border
            empty-text="暂无历史时序数据"
          >
            <el-table-column
              label="序号"
              type="index"
              width="60"
              align="center"
            />
            <el-table-column label="采集时间戳 (Timestamp)" min-width="180">
              <template #default="scope">
                {{ formatTime(scope.row.timestamp) }}
              </template>
            </el-table-column>
            <el-table-column label="点位采集数值 (Value)" width="200">
              <template #default="scope">
                <template v-if="activePoint.dataType === 'ENUM'">
                  <el-tag size="small" type="warning" effect="dark">
                    {{ getEnumLabel(activePoint, scope.row.value) }}
                  </el-tag>
                  <code style="margin-left: 6px">[{{ scope.row.value }}]</code>
                </template>
                <span v-else class="val-highlight"
                  >{{ formatNumericValue(scope.row.value) }}
                  {{ activePoint.unit || "" }}</span
                >
              </template>
            </el-table-column>
            <el-table-column
              label="质量码 (Quality)"
              width="120"
              align="center"
            >
              <template #default="scope">
                <StatusBadge
                  :status="scope.row.quality"
                  :text="scope.row.quality === 1 ? '正常' : '异常'"
                />
              </template>
            </el-table-column>
            <el-table-column label="物模型Key" width="150" prop="modelKey" />
          </el-table>

          <div class="table-pagination">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next"
              :total="historyList.length"
            />
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from "vue";
import { ElMessage } from "element-plus";
import * as echarts from "echarts";
import SvgIcon from "../../components/common/SvgIcon.vue";
import StatusBadge from "../../components/common/StatusBadge.vue";
import { getDeviceList, getPointAddressMappings } from "../../api/device";
import { getThingModel } from "../../api/thingmodel";
import { queryHistoryData, getLatestPointData } from "../../api/data";

const selectedDeviceId = ref("");
const deviceList = ref([]);
const loadingDevices = ref(false);
const loadingPoints = ref(false);
const loadingLive = ref(false);
const pointList = ref([]);
const filteredPoints = ref([]);
const viewMode = ref("card");

// SSE Real-time Telemetry Push Stream
let sseSource = null;
const sseEnabled = ref(false);
const sseStatus = ref("disconnected"); // 'connecting' | 'connected' | 'disconnected' | 'error'

const sseStatusClass = computed(() => {
  if (!sseEnabled.value) return "status-off";
  if (sseStatus.value === "connected") return "status-on";
  if (sseStatus.value === "connecting") return "status-connecting";
  return "status-error";
});

const sseStatusText = computed(() => {
  if (!selectedDeviceId.value) return "待选设备";
  if (!sseEnabled.value) return "手动模式";
  if (sseStatus.value === "connected") return "实时推送中";
  if (sseStatus.value === "connecting") return "连接中...";
  return "连接断开";
});

// Query filters
const queryForm = ref({
  keyword: "",
  dataType: "",
  accessMode: "",
  quality: "",
});

// Table view pagination
const pointTablePage = ref(1);
const pointTablePageSize = ref(10);
const paginatedTablePoints = computed(() => {
  const start = (pointTablePage.value - 1) * pointTablePageSize.value;
  return filteredPoints.value.slice(start, start + pointTablePageSize.value);
});

// Dialog and history state
const historyDialogVisible = ref(false);
const activePoint = ref(null);
const historyTimeRange = ref("1h");
const loadingHistory = ref(false);
const historyList = ref([]);
const currentPage = ref(1);
const pageSize = ref(10);

// ECharts instance reference
const chartRef = ref(null);
let chartInstance = null;

// Current selected device object
const currentDevice = computed(() => {
  return (
    deviceList.value.find((d) => d.deviceId === selectedDeviceId.value) || null
  );
});

// Paginated history table data
const paginatedHistory = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  return historyList.value.slice(start, start + pageSize.value);
});

// Statistics for active point history
const historyStats = computed(() => {
  if (!historyList.value || historyList.value.length === 0) {
    return { max: "0", min: "0", avg: "0" };
  }
  const vals = historyList.value
    .map((item) => Number(item.value))
    .filter((v) => !isNaN(v));
  if (vals.length === 0) return { max: "0", min: "0", avg: "0" };

  const max = Math.max(...vals);
  const min = Math.min(...vals);
  const avg = vals.reduce((a, b) => a + b, 0) / vals.length;

  const maxItem = historyList.value.find((item) => Number(item.value) === max);
  const minItem = historyList.value.find((item) => Number(item.value) === min);

  return {
    max: maxItem ? maxItem.value : max,
    min: minItem ? minItem.value : min,
    avg: Number(avg.toFixed(2)),
  };
});

// Point ENUM options helper - retrieves defined or smart fallback options
const getPointEnumOptions = (point) => {
  if (!point) return [];
  if (Array.isArray(point.enumOptions) && point.enumOptions.length > 0) {
    return point.enumOptions;
  }
  // Smart fallback defaults for ENUM points if not defined in thing model
  if (point.dataType === "ENUM") {
    if (point.pointCode === "workMode") {
      return [
        { value: 0, label: "待机模式" },
        { value: 1, label: "自动生产" },
        { value: 2, label: "手动维护" },
        { value: 3, label: "节能巡检" },
      ];
    }
    return [
      { value: 0, label: "停止/复位" },
      { value: 1, label: "就绪/运行" },
      { value: 2, label: "检修/测试" },
    ];
  }
  return [];
};

const getEnumOption = (point, val) => {
  if (!point) return null;
  const options = getPointEnumOptions(point);
  return options.find((o) => String(o.value) === String(val)) || null;
};

const getEnumLabel = (point, val) => {
  if (val === null || val === undefined) return "--";
  const opt = getEnumOption(point, val);
  if (opt) return opt.label || opt.name || opt.text || `${opt.value}`;
  return `状态值: ${val}`;
};

// 枚举卡片显示: 将状态码置于枚举说明后面的括号中, 如 "待机模式 (0)"
const getEnumDisplayWithCode = (point, val) => {
  if (val === null || val === undefined || val === "--") return "--";
  const opt = getEnumOption(point, val);
  if (opt) {
    const label = opt.label || opt.name || opt.text || "";
    return `${label} (${val})`;
  }
  return `未知状态 (${val})`;
};

// Load physical devices list from backend API
const loadDevices = async () => {
  loadingDevices.value = true;
  try {
    const res = await getDeviceList();
    const devices = Array.isArray(res)
      ? res
      : res && res.data && Array.isArray(res.data)
        ? res.data
        : null;
    if (devices && devices.length > 0) {
      deviceList.value = devices;
      // If none selected, default to first device
      if (!selectedDeviceId.value && devices.length > 0) {
        selectedDeviceId.value = devices[0].deviceId;
        handleDeviceChange(devices[0].deviceId);
      }
    } else {
      deviceList.value = [];
    }
  } catch (err) {
    console.warn("后端设备接口异常:", err);
    deviceList.value = [];
  } finally {
    loadingDevices.value = false;
  }
};

// Device selection handler: loads real address mappings, thing model, and real-time snapshot
const handleDeviceChange = async (deviceId) => {
  if (!deviceId) {
    pointList.value = [];
    filteredPoints.value = [];
    return;
  }

  const dev = deviceList.value.find((d) => d.deviceId === deviceId);
  if (!dev) return;

  loadingPoints.value = true;
  try {
    // 1. 获取设备物理点位地址映射表 (Physical Point Address Mappings)
    let mappings = [];
    try {
      const mapRes = await getPointAddressMappings(dev.deviceId);
      mappings = Array.isArray(mapRes)
        ? mapRes
        : mapRes && mapRes.data
          ? mapRes.data
          : [];
    } catch (e) {
      console.warn("获取设备点位映射失败:", e);
    }

    // 2. 获取该物模型的完整属性定义 (Thing Model Properties)
    let thingProperties = [];
    try {
      const tmRes = await getThingModel(dev.modelKey);
      const model =
        tmRes && tmRes.properties
          ? tmRes
          : tmRes && tmRes.data
            ? tmRes.data
            : null;
      if (model && model.properties) {
        thingProperties = model.properties;
      }
    } catch (e) {
      console.warn("获取物模型定义失败:", e);
    }

    // 3. 获取后端真实最新点位快照 (Real-time Telemetry Snapshot)
    let latestSnapshot = {};
    try {
      const snapRes = await getLatestPointData(dev.deviceId);
      latestSnapshot = snapRes && snapRes.data ? snapRes.data : snapRes || {};
    } catch (e) {
      console.warn("获取最新点位快照失败:", e);
    }

    // 4. 融合生成真实点位列表
    let merged = [];
    if (mappings && mappings.length > 0) {
      merged = mappings.map((m) => {
        const prop =
          thingProperties.find((p) => p.pointCode === m.pointCode) || {};
        const snap = latestSnapshot[m.pointCode];
        return {
          pointCode: m.pointCode,
          pointName: m.pointName || prop.pointName || m.pointCode,
          dataType: prop.dataType || m.dataType || "FLOAT",
          accessMode: m.accessMode || prop.accessMode || "R",
          address: m.physicalAddress || prop.address || "--",
          scale: m.scale !== undefined ? m.scale : 1.0,
          offset: m.offset !== undefined ? m.offset : 0.0,
          unit: prop.unit || "",
          sampleFactor: prop.sampleFactor || 1,
          triggerMode: prop.triggerMode || "PERIODIC",
          enumOptions: prop.enumOptions || m.enumOptions || [],
          description: prop.description || m.description || "",
          value:
            snap && snap.value !== undefined
              ? snap.value
              : prop.defaultValue !== undefined
                ? prop.defaultValue
                : "--",
          quality: snap && snap.quality !== undefined ? snap.quality : 1,
          timestamp: snap && snap.timestamp ? snap.timestamp : Date.now(),
        };
      });
    } else if (thingProperties && thingProperties.length > 0) {
      merged = thingProperties.map((prop) => {
        const snap = latestSnapshot[prop.pointCode];
        return {
          pointCode: prop.pointCode,
          pointName: prop.pointName || prop.pointCode,
          dataType: prop.dataType || "DOUBLE",
          accessMode: prop.accessMode || "R",
          address: prop.address || "--",
          scale: 1.0,
          offset: 0.0,
          unit: prop.unit || "",
          sampleFactor: prop.sampleFactor || 1,
          triggerMode: prop.triggerMode || "PERIODIC",
          enumOptions: prop.enumOptions || [],
          description: prop.description || "",
          value:
            snap && snap.value !== undefined
              ? snap.value
              : prop.defaultValue !== undefined
                ? prop.defaultValue
                : "--",
          quality: snap && snap.quality !== undefined ? snap.quality : 1,
          timestamp: snap && snap.timestamp ? snap.timestamp : Date.now(),
        };
      });
    } else {
      pointList.value = [];
    }

    pointList.value = merged;
    handleFilter();
    ElMessage.success(
      `已选择设备 [${dev.deviceName}], 共加载 ${pointList.value.length} 个采集点位`,
    );
  } catch (err) {
    console.error("加载设备点位失败:", err);
    pointList.value = [];
    handleFilter();
  } finally {
    loadingPoints.value = false;
  }

  // 建立/切换当前目标设备的 SSE 实时遥测数据流通道
  if (sseEnabled.value) {
    initSse(dev.deviceId);
  }
};

// Filter points based on query inputs
const handleFilter = () => {
  pointTablePage.value = 1;
  if (!pointList.value) {
    filteredPoints.value = [];
    return;
  }

  filteredPoints.value = pointList.value.filter((point) => {
    // Keyword match
    if (queryForm.value.keyword) {
      const kw = queryForm.value.keyword.trim().toLowerCase();
      const matchCode = point.pointCode.toLowerCase().includes(kw);
      const matchName = (point.pointName || "").toLowerCase().includes(kw);
      const matchAddr = (point.address || "").toLowerCase().includes(kw);
      if (!matchCode && !matchName && !matchAddr) return false;
    }
    // Data type match
    if (
      queryForm.value.dataType &&
      point.dataType !== queryForm.value.dataType
    ) {
      return false;
    }
    // Access mode match
    if (
      queryForm.value.accessMode &&
      point.accessMode !== queryForm.value.accessMode
    ) {
      return false;
    }
    // Quality match
    if (queryForm.value.quality !== "" && queryForm.value.quality !== null) {
      if (point.quality !== queryForm.value.quality) return false;
    }
    return true;
  });
};

// Reset point query filter
const resetQuery = () => {
  queryForm.value = {
    keyword: "",
    dataType: "",
    accessMode: "",
    quality: "",
  };
  pointTablePage.value = 1;
  handleFilter();
};

// Refresh live telemetry values from backend real snapshot
const refreshLiveValues = async () => {
  if (!selectedDeviceId.value) {
    ElMessage.warning("请先选择目标物理设备");
    return;
  }
  loadingLive.value = true;
  try {
    const snapRes = await getLatestPointData(selectedDeviceId.value);
    const latestSnapshot =
      snapRes && snapRes.data ? snapRes.data : snapRes || {};

    let updatedCount = 0;
    pointList.value.forEach((p) => {
      const snap = latestSnapshot[p.pointCode];
      if (snap) {
        p.value = snap.value;
        p.quality = snap.quality !== undefined ? snap.quality : 1;
        p.timestamp = snap.timestamp || Date.now();
        updatedCount++;
      }
    });
    handleFilter();
    ElMessage.success(
      `已刷新当前物理设备的数据 (成功同步 ${updatedCount} 个点位最新数据)`,
    );
  } catch (err) {
    ElMessage.error("获取最新点位快照失败: " + err.message);
  } finally {
    loadingLive.value = false;
  }
};

// SSE stream subscription & event handlers
const applySnapshot = (snapshot) => {
  if (!snapshot || !pointList.value) return;
  pointList.value.forEach((p) => {
    const snap = snapshot[p.pointCode];
    if (snap) {
      p.value = snap.value;
      p.quality = snap.quality !== undefined ? snap.quality : 1;
      p.timestamp = snap.timestamp || Date.now();
    }
  });
};

const applySinglePointData = (pointData) => {
  if (!pointData || !pointList.value) return;
  const target = pointList.value.find(
    (p) => p.pointCode === pointData.pointCode,
  );
  if (target) {
    target.value = pointData.value;
    target.quality = pointData.quality !== undefined ? pointData.quality : 1;
    target.timestamp = pointData.timestamp || Date.now();
  }
};

const initSse = (deviceId) => {
  closeSse();
  if (!deviceId || !sseEnabled.value) return;

  sseStatus.value = "connecting";
  const url = `/api/v1/data/sse/${deviceId}`;

  try {
    sseSource = new EventSource(url);

    sseSource.onopen = () => {
      sseStatus.value = "connected";
      console.log(`[SSE] 设备 ${deviceId} 实时数据通道建立成功`);
    };

    sseSource.addEventListener("snapshot", (event) => {
      try {
        const snapshot = JSON.parse(event.data);
        applySnapshot(snapshot);
      } catch (e) {
        console.warn("[SSE] 解析快照失败:", e);
      }
    });

    sseSource.addEventListener("point-data", (event) => {
      try {
        const pointData = JSON.parse(event.data);
        applySinglePointData(pointData);
      } catch (e) {
        console.warn("[SSE] 解析点位数据失败:", e);
      }
    });

    sseSource.addEventListener("batch-data", (event) => {
      try {
        const dataList = JSON.parse(event.data);
        if (Array.isArray(dataList)) {
          dataList.forEach((p) => applySinglePointData(p));
        }
      } catch (e) {
        console.warn("[SSE] 解析批量数据失败:", e);
      }
    });

    sseSource.onerror = (err) => {
      console.warn("[SSE] 连接异常或断开，浏览器将自动重连:", err);
      sseStatus.value = "error";
    };
  } catch (err) {
    console.error("[SSE] 初始化失败:", err);
    sseStatus.value = "error";
  }
};

const closeSse = () => {
  if (sseSource) {
    sseSource.close();
    sseSource = null;
  }
  sseStatus.value = "disconnected";
};

const handleSseToggle = () => {
  if (sseEnabled.value) {
    if (selectedDeviceId.value) {
      initSse(selectedDeviceId.value);
      ElMessage.success("已切换为实时数据模式 (SSE推送)");
    } else {
      ElMessage.info("请选择设备以建立实时数据流");
    }
  } else {
    closeSse();
    ElMessage.info("已切换为手动模式 (可通过「刷新数据」按钮按需获取)");
  }
};

// Open Historical Data View Dialog for clicked point card
const openHistoryDialog = (point) => {
  activePoint.value = point;
  historyDialogVisible.value = true;
  fetchHistoryData();
};

// Fetch history records from backend real history API (no mock data fallback)
const fetchHistoryData = async () => {
  if (!activePoint.value || !selectedDeviceId.value) return;
  loadingHistory.value = true;

  const now = Date.now();
  let duration = 3600 * 1000; // default 1 hour
  if (historyTimeRange.value === "15m") duration = 15 * 60 * 1000;
  if (historyTimeRange.value === "24h") duration = 24 * 3600 * 1000;
  if (historyTimeRange.value === "7d") duration = 7 * 24 * 3600 * 1000;

  const startTime = now - duration;
  const point = activePoint.value;

  try {
    const res = await queryHistoryData({
      deviceId: selectedDeviceId.value,
      pointCode: point.pointCode,
      startTime,
      endTime: now,
      limit: 100,
    });

    const records = Array.isArray(res) ? res : res && res.data ? res.data : [];
    if (records && records.length > 0) {
      historyList.value = records.sort((a, b) => b.timestamp - a.timestamp);
    } else if (
      point.value !== undefined &&
      point.value !== null &&
      point.value !== "--"
    ) {
      const currentTs = point.timestamp || now;
      historyList.value = [
        {
          deviceId: selectedDeviceId.value,
          pointCode: point.pointCode,
          value: point.value,
          quality: point.quality !== undefined ? point.quality : 1,
          timestamp: currentTs,
        },
      ];
    } else {
      historyList.value = [];
    }
  } catch (err) {
    console.warn("查询历史数据接口异常:", err);
    if (
      point.value !== undefined &&
      point.value !== null &&
      point.value !== "--"
    ) {
      historyList.value = [
        {
          deviceId: selectedDeviceId.value,
          pointCode: point.pointCode,
          value: point.value,
          quality: point.quality !== undefined ? point.quality : 1,
          timestamp: point.timestamp || now,
        },
      ];
    } else {
      historyList.value = [];
    }
  } finally {
    loadingHistory.value = false;
    nextTick(() => {
      initOrResizeChart();
    });
  }
};

// Export history records to CSV file
const exportHistoryCSV = () => {
  if (!historyList.value || historyList.value.length === 0) {
    ElMessage.warning("当前无历史数据可导出");
    return;
  }
  const headers = "设备ID,点位代码,点位数值,单位,质量码,时间戳,采集时间\n";
  const rows = historyList.value
    .map((item) => {
      const timeStr = formatTime(item.timestamp).replace(",", " ");
      const unit = activePoint.value ? activePoint.value.unit : "";
      return `${item.deviceId},${item.pointCode},${item.value},${unit},${item.quality},${item.timestamp},"${timeStr}"`;
    })
    .join("\n");

  const blob = new Blob([`\uFEFF${headers}${rows}`], {
    type: "text/csv;charset=utf-8;",
  });
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.setAttribute("href", url);
  link.setAttribute(
    "download",
    `${selectedDeviceId.value}_${activePoint.value.pointCode}_history.csv`,
  );
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(url);
  ElMessage.success("历史时序数据导出成功！");
};

// Dialog open/close lifecycle for ECharts
const handleDialogOpened = () => {
  nextTick(() => {
    initOrResizeChart();
  });
};

const handleDialogClosed = () => {
  if (chartInstance) {
    chartInstance.dispose();
    chartInstance = null;
  }
};

// Ensure chart instance exists and is resized correctly
const initOrResizeChart = () => {
  if (!chartRef.value) return;
  if (chartInstance && chartInstance.getDom() !== chartRef.value) {
    chartInstance.dispose();
    chartInstance = null;
  }
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value);
  }
  chartInstance.resize();
  renderHistoryChart();

  // Deferred resize after modal animation finishes
  setTimeout(() => {
    if (chartInstance && chartRef.value) {
      chartInstance.resize();
      renderHistoryChart();
    }
  }, 100);
};

// Render or update ECharts trend curve
const renderHistoryChart = () => {
  if (!chartRef.value) return;
  if (!chartInstance || chartInstance.getDom() !== chartRef.value) {
    if (chartInstance) chartInstance.dispose();
    chartInstance = echarts.init(chartRef.value);
  }
  chartInstance.resize();

  const point = activePoint.value;
  const pointName = point ? point.pointName || point.pointCode : "点位数值";
  const unit = point && point.unit ? point.unit : "";
  const isEnum = point && point.dataType === "ENUM";
  const isBool = point && point.dataType === "BOOL";

  if (!historyList.value || historyList.value.length === 0) {
    chartInstance.setOption(
      {
        backgroundColor: "transparent",
        title: {
          text: "暂无历史时序回溯数据 (当前开窗周期内未检索到采样记录)",
          left: "center",
          top: "middle",
          textStyle: {
            color: "#64748b",
            fontSize: 13,
            fontWeight: "normal",
          },
        },
        xAxis: { show: false },
        yAxis: { show: false },
        series: [],
      },
      true,
    );
    return;
  }

  const sortedList = [...historyList.value].sort(
    (a, b) => a.timestamp - b.timestamp,
  );
  const timeData = sortedList.map((item) =>
    new Date(item.timestamp).toLocaleTimeString(),
  );
  const valData = sortedList.map((item) => {
    if (isBool) {
      return item.value == 1 || item.value === true ? 1 : 0;
    }
    const num = Number(item.value);
    return isNaN(num) ? 0 : num;
  });

  let yAxisConfig = {
    type: "value",
    scale: true,
    axisLine: { lineStyle: { color: "#475569" } },
    splitLine: { lineStyle: { color: "#334155", type: "dashed" } },
    axisLabel: { color: "#94a3b8", formatter: `{value} ${unit}` },
  };

  if (isBool) {
    yAxisConfig = {
      type: "value",
      min: 0,
      max: 1,
      interval: 1,
      axisLine: { lineStyle: { color: "#475569" } },
      splitLine: { lineStyle: { color: "#334155", type: "dashed" } },
      axisLabel: {
        color: "#94a3b8",
        formatter: (val) => (val === 1 ? "ON (1)" : val === 0 ? "OFF (0)" : ""),
      },
    };
  } else if (isEnum) {
    const enumOpts = getPointEnumOptions(point);
    yAxisConfig = {
      type: "value",
      min: 0,
      interval: 1,
      axisLine: { lineStyle: { color: "#475569" } },
      splitLine: { lineStyle: { color: "#334155", type: "dashed" } },
      axisLabel: {
        color: "#94a3b8",
        formatter: (val) => {
          const opt = enumOpts.find((o) => String(o.value) === String(val));
          return opt ? `${val}:${opt.label}` : `${val}`;
        },
      },
    };
  }

  const option = {
    backgroundColor: "transparent",
    title: { text: "" },
    tooltip: {
      trigger: "axis",
      backgroundColor: "rgba(15, 23, 42, 0.95)",
      borderColor: isEnum ? "#fbbf24" : "#06b6d4",
      textStyle: { color: "#f8fafc" },
      formatter: (params) => {
        const item = params[0];
        let displayVal = `${item.value} ${unit}`;
        if (isEnum) {
          displayVal = `${getEnumLabel(point, item.value)} (状态码: ${item.value})`;
        } else if (isBool) {
          displayVal =
            item.value == 1 || item.value === true ? "ON (1)" : "OFF (0)";
        }
        return `<div>
          <div style="font-size:12px;color:#94a3b8;margin-bottom:4px;">${item.axisValue}</div>
          <div style="color:${isEnum ? "#fbbf24" : "#06b6d4"};font-weight:bold;">
            ${pointName}: ${displayVal}
          </div>
        </div>`;
      },
    },
    grid: {
      top: "14%",
      left: "4%",
      right: "4%",
      bottom: "16%",
      containLabel: true,
    },
    xAxis: {
      type: "category",
      data: timeData,
      boundaryGap: false,
      axisLine: { lineStyle: { color: "#475569" } },
      axisLabel: { color: "#94a3b8", fontSize: 11 },
    },
    yAxis: yAxisConfig,
    dataZoom: [
      { type: "inside", start: 0, end: 100 },
      {
        type: "slider",
        bottom: "2%",
        height: 18,
        borderColor: "#334155",
        textStyle: { color: "#94a3b8" },
      },
    ],
    series: [
      {
        name: pointName,
        type: "line",
        data: valData,
        step: isBool || isEnum ? "start" : false,
        smooth: !(isBool || isEnum),
        showSymbol: true,
        symbolSize: sortedList.length > 50 ? 4 : 6,
        itemStyle: { color: isEnum ? "#fbbf24" : "#06b6d4" },
        lineStyle: { width: 3, color: isEnum ? "#fbbf24" : "#06b6d4" },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            {
              offset: 0,
              color: isEnum
                ? "rgba(251, 191, 36, 0.35)"
                : "rgba(6, 182, 212, 0.4)",
            },
            {
              offset: 1,
              color: isEnum
                ? "rgba(251, 191, 36, 0.02)"
                : "rgba(6, 182, 212, 0.02)",
            },
          ]),
        },
        markLine:
          !isEnum && !isBool && sortedList.length > 1
            ? {
                symbol: ["none", "none"],
                data: [{ type: "average", name: "平均值" }],
                label: {
                  show: true,
                  position: "insideEndTop",
                  distance: 6,
                  formatter: "平均值: {c}",
                  color: "#fbbf24",
                  fontSize: 11,
                  fontWeight: "600",
                  backgroundColor: "rgba(15, 23, 42, 0.85)",
                  borderColor: "#f59e0b",
                  borderWidth: 1,
                  borderRadius: 4,
                  padding: [2, 6],
                },
                lineStyle: {
                  color: "#f59e0b",
                  type: "dashed",
                  width: 2,
                },
              }
            : undefined,
      },
    ],
  };

  chartInstance.setOption(option, true);
};

// Window resize handler for ECharts
const handleWindowResize = () => {
  if (chartInstance) chartInstance.resize();
};

// Formatting helpers
const formatNumericValue = (val) => {
  if (val === null || val === undefined) return "--";
  if (typeof val === "boolean") return val ? "1 (ON)" : "0 (OFF)";
  if (typeof val === "number" || (!isNaN(val) && val !== "")) {
    const num = Number(val);
    if (!Number.isFinite(num)) return `${val}`;
    // 消除浮点数 IEEE-754 精度漂移 (如 1325.6599999999999 -> 1325.66)
    return String(parseFloat(num.toFixed(6)));
  }
  return val;
};

const formatTime = (ts) => {
  if (!ts) return "--";
  return new Date(ts).toLocaleString("zh-CN");
};

const getSparklineWidth = (point) => {
  if (point.dataType === "BOOL") return point.value ? 100 : 10;
  if (point.dataType === "ENUM") return 60;
  const val = Number(point.value) || 0;
  return Math.min(100, Math.max(15, val % 100));
};

onMounted(() => {
  loadDevices();
  window.addEventListener("resize", handleWindowResize);
});

onUnmounted(() => {
  closeSse();
  window.removeEventListener("resize", handleWindowResize);
  if (chartInstance) {
    chartInstance.dispose();
    chartInstance = null;
  }
});
</script>

<style scoped>
.page-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.header-subtitle {
  font-size: 0.85rem;
  color: var(--iot-text-secondary);
  margin-top: 4px;
  display: block;
}

.engine-selector {
  display: flex;
  align-items: center;
  gap: 10px;
}

.engine-selector .label {
  font-size: 0.85rem;
  color: var(--iot-text-secondary);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.sse-status-wrap {
  display: flex;
  align-items: center;
  gap: 12px;
  background: var(--iot-bg-secondary);
  padding: 4px 12px;
  border-radius: 6px;
  border: 1px solid var(--iot-border-color);
}

.sse-indicator {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 0.8rem;
  font-weight: 500;
}

.pulse-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  display: inline-block;
}

.sse-indicator.status-on {
  color: #10b981;
}
.sse-indicator.status-on .pulse-dot {
  background: #10b981;
  box-shadow: 0 0 8px #10b981;
  animation: pulse-green 1.5s infinite;
}

.sse-indicator.status-connecting {
  color: #fbbf24;
}
.sse-indicator.status-connecting .pulse-dot {
  background: #fbbf24;
  animation: pulse-yellow 1s infinite;
}

.sse-indicator.status-off {
  color: #64748b;
}
.sse-indicator.status-off .pulse-dot {
  background: #64748b;
}

.sse-indicator.status-error {
  color: #f87171;
}
.sse-indicator.status-error .pulse-dot {
  background: #f87171;
}

@keyframes pulse-green {
  0% {
    transform: scale(0.95);
    box-shadow: 0 0 0 0 rgba(16, 185, 129, 0.7);
  }
  70% {
    transform: scale(1.1);
    box-shadow: 0 0 0 6px rgba(16, 185, 129, 0);
  }
  100% {
    transform: scale(0.95);
    box-shadow: 0 0 0 0 rgba(16, 185, 129, 0);
  }
}

@keyframes pulse-yellow {
  0% {
    opacity: 0.4;
  }
  50% {
    opacity: 1;
  }
  100% {
    opacity: 0.4;
  }
}

.card-footer-btns {
  display: flex;
  align-items: center;
  gap: 8px;
}

.write-header-info {
  background: rgba(30, 41, 59, 0.6);
  border: 1px solid var(--iot-border-color);
  padding: 12px 16px;
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 0.9rem;
  color: var(--iot-text-secondary);
}

.write-header-info strong {
  color: var(--iot-text-primary);
}

.write-header-info code {
  color: var(--iot-color-cyan);
}

/* Device Selector Container */
.device-selector-card {
  padding: 16px;
  background: var(--iot-bg-secondary);
  border: 1px solid var(--iot-border-color);
  border-radius: 8px;
}

.selector-container {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.selector-label-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
}

.selector-title {
  font-size: 1rem;
  font-weight: 600;
  color: var(--iot-text-primary);
}

.device-select-dropdown {
  flex: 1;
  min-width: 300px;
}

.device-option-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.opt-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.opt-name {
  font-weight: 500;
}

.opt-id {
  color: var(--iot-color-cyan);
  font-family: monospace;
  font-size: 0.82rem;
}

.opt-tags {
  display: flex;
  gap: 6px;
}

/* Device Summary Banner */
.device-summary-banner {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
  margin-top: 16px;
  padding-top: 14px;
  border-top: 1px dashed var(--iot-border-color);
}

.banner-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.88rem;
}

.banner-lbl {
  color: var(--iot-text-secondary);
}

.banner-val {
  color: var(--iot-text-primary);
  font-weight: 500;
}

.banner-val.highlight {
  color: var(--iot-color-cyan);
  font-weight: 700;
  font-size: 0.95rem;
}

.banner-id {
  color: #a7f3d0;
  font-family: monospace;
  font-size: 0.85rem;
}

.count-badge {
  background: rgba(139, 92, 246, 0.15);
  color: #a78bfa;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 0.82rem;
  font-weight: 600;
}

.banner-actions {
  margin-left: auto;
  display: flex;
  gap: 10px;
}

/* Empty State Card */
.unselected-empty-card {
  padding: 48px 24px;
  text-align: center;
  background: var(--iot-bg-secondary);
  border: 2px dashed rgba(139, 92, 246, 0.3);
  border-radius: 8px;
}

.empty-content {
  max-width: 560px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.empty-icon-circle {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: rgba(139, 92, 246, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
}

.empty-content h3 {
  font-size: 1.25rem;
  color: var(--iot-text-primary);
  font-weight: 600;
}

.empty-content p {
  color: var(--iot-text-secondary);
  font-size: 0.9rem;
  line-height: 1.5;
}

.quick-devices-section {
  margin-top: 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  align-items: center;
}

.quick-label {
  font-size: 0.85rem;
  color: var(--iot-text-secondary);
}

.quick-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
}

/* Filter Card */
.filter-card {
  padding: 16px;
  background: var(--iot-bg-secondary);
}

.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--iot-border-color);
}

.filter-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: var(--iot-text-primary);
  font-size: 0.95rem;
}

.filter-count {
  font-size: 0.85rem;
  color: var(--iot-text-secondary);
  font-weight: normal;
}

.filter-form {
  margin-bottom: 0;
}

/* Point Cards Grid Layout */
.point-cards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(310px, 1fr));
  gap: 16px;
}

.point-card {
  background: var(--iot-bg-secondary);
  border: 1px solid var(--iot-border-color);
  border-radius: 8px;
  padding: 14px;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
}

.point-card:hover {
  transform: translateY(-3px);
  border-color: var(--iot-color-cyan);
  box-shadow: 0 8px 20px rgba(6, 182, 212, 0.15);
}

.point-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 12px;
}

.point-name-box {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.point-name {
  font-size: 0.98rem;
  font-weight: 600;
  color: var(--iot-text-primary);
}

.point-code {
  font-size: 0.8rem;
  color: var(--iot-color-cyan);
  font-family: monospace;
}

.point-badges {
  display: flex;
  align-items: center;
  gap: 6px;
}

/* Live Value Box */
.point-card-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.live-value-wrap {
  background: rgba(15, 23, 42, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 6px;
  padding: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 72px;
}

.numeric-value-box {
  display: flex;
  align-items: baseline;
  gap: 6px;
}

.live-num {
  font-size: 1.8rem;
  font-weight: 800;
  color: #38bdf8;
  letter-spacing: -0.5px;
}

.unit-text {
  font-size: 0.9rem;
  color: #94a3b8;
  font-weight: 500;
}

.bool-value-box {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 14px;
  border-radius: 20px;
  background: rgba(239, 68, 68, 0.15);
  color: #f87171;
  font-weight: 700;
  font-size: 0.95rem;
}

.bool-value-box.is-on {
  background: rgba(16, 185, 129, 0.15);
  color: #34d399;
}

.bool-pulse {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: currentColor;
  box-shadow: 0 0 8px currentColor;
}

.string-value-box {
  font-size: 1.1rem;
  font-weight: 600;
  color: #a78bfa;
}

/* ENUM Value Box & Pills */
.enum-value-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.enum-val-badge {
  font-size: 0.95rem;
  font-weight: 700;
  padding: 6px 14px;
}

.enum-code-sub {
  font-size: 0.78rem;
  color: #94a3b8;
}

.enum-code-sub code {
  color: #fbbf24;
  font-weight: 700;
}

.enum-spec-row {
  flex-direction: column;
  align-items: flex-start !important;
  gap: 6px !important;
}

.enum-options-pill-list {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.enum-mini-pill {
  font-size: 0.72rem;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid var(--iot-border-color);
  padding: 1px 6px;
  border-radius: 4px;
  color: #94a3b8;
}

.enum-mini-pill.active {
  background: rgba(251, 191, 36, 0.2);
  border-color: #fbbf24;
  color: #fbbf24;
  font-weight: 600;
}

.table-enum-cell {
  display: flex;
  align-items: center;
  gap: 6px;
}

.enum-mini-code {
  color: #fbbf24;
  font-weight: 600;
  font-size: 0.82rem;
}

.table-datatype-cell {
  display: flex;
  align-items: center;
  gap: 6px;
}

.enum-info-icon {
  cursor: pointer;
  font-size: 0.9rem;
  transition: transform 0.2s ease;
}

.enum-info-icon:hover {
  transform: scale(1.2);
}

.enum-popover-content {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 0.82rem;
}

.popover-title {
  color: #e2e8f0;
  margin-bottom: 2px;
  padding-bottom: 4px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.popover-enum-row {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #94a3b8;
}

.popover-enum-row.current {
  color: #fbbf24;
  font-weight: 600;
}

.popover-enum-row code {
  color: #38bdf8;
  font-weight: 700;
}

.curr-mark {
  color: #fbbf24;
  font-size: 0.75rem;
  margin-left: auto;
}

.history-enum-banner {
  display: flex;
  align-items: center;
  gap: 10px;
  background: rgba(251, 191, 36, 0.08);
  border: 1px solid rgba(251, 191, 36, 0.2);
  border-radius: 6px;
  padding: 8px 12px;
  flex-wrap: wrap;
}

.enum-banner-lbl {
  font-size: 0.85rem;
  color: #fbbf24;
  font-weight: 600;
}

.enum-banner-pills {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.enum-stat-val {
  font-size: 0.95rem !important;
  color: #fbbf24 !important;
}

.sparkline-bar {
  height: 3px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 2px;
  overflow: hidden;
}

.spark-fill {
  height: 100%;
  background: linear-gradient(90deg, #06b6d4, #3b82f6);
  transition: width 0.3s ease;
}

.point-specs-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 6px;
  font-size: 0.8rem;
}

.spec-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.spec-item.full-width {
  grid-column: span 2;
}

.spec-lbl {
  color: var(--iot-text-secondary);
}

.spec-val {
  color: var(--iot-text-primary);
}

.spec-val.time {
  color: #94a3b8;
  font-size: 0.78rem;
}

.spec-val-code {
  color: #fca5a5;
  font-size: 0.78rem;
}

.point-card-footer {
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px solid var(--iot-border-color);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.click-tip {
  font-size: 0.8rem;
  color: var(--iot-color-cyan);
  display: flex;
  align-items: center;
}

.no-points-card {
  grid-column: 1 / -1;
  padding: 40px;
  background: var(--iot-bg-secondary);
  border-radius: 8px;
}

/* History Dialog Styling */
.history-dialog-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.point-history-header {
  background: var(--iot-bg-primary);
  border: 1px solid var(--iot-border-color);
  border-radius: 6px;
  padding: 12px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.ph-name {
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--iot-text-primary);
}

.ph-code {
  color: var(--iot-color-cyan);
  font-family: monospace;
  margin-left: 6px;
}

.ph-unit {
  font-size: 0.85rem;
  color: var(--iot-text-secondary);
  margin-left: 12px;
}

.ph-dev-info {
  font-size: 0.88rem;
  color: var(--iot-text-secondary);
}

.history-filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.range-lbl {
  font-size: 0.85rem;
  color: var(--iot-text-secondary);
  margin-right: 8px;
}

.history-actions {
  display: flex;
  gap: 10px;
}

/* Stats Cards */
.stats-overview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 12px;
}

.stat-card {
  background: var(--iot-bg-primary);
  border: 1px solid var(--iot-border-color);
  padding: 10px 12px;
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-lbl {
  font-size: 0.78rem;
  color: var(--iot-text-secondary);
}

.stat-val {
  font-size: 1.1rem;
  font-weight: 700;
}

.stat-val.live {
  color: #38bdf8;
}
.stat-val.max {
  color: #f87171;
}
.stat-val.min {
  color: #34d399;
}
.stat-val.avg {
  color: #fbbf24;
}
.stat-val.count {
  color: #c084fc;
}

.chart-container {
  background: var(--iot-bg-primary);
  border: 1px solid var(--iot-border-color);
  border-radius: 6px;
  padding: 12px;
}

.echarts-box {
  width: 100%;
  height: 320px;
}

.history-table-section {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.section-title {
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--iot-text-primary);
}

.table-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
}

.val-highlight {
  font-weight: 700;
  color: var(--iot-color-cyan);
}

.margin-top {
  margin-top: 16px;
}
</style>
