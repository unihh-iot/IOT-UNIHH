<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="iot-header-title">
        <SvgIcon name="cpu" :size="24" color="#10b981" />
        设备管理 (动态驱动参数, 物模型关联, 数采状态与指令下发)
      </h2>
      <div class="header-actions">
        <el-button :loading="refreshing" @click="handleManualRefresh" style="margin-right: 8px;">
          <SvgIcon name="refresh" :size="14" style="margin-right: 4px;" />
          刷新状态
        </el-button>
        <el-button v-if="hasPerm('device:add')" type="primary" @click="openAddModal">
          <SvgIcon name="send" :size="14" />
          新增接入物理设备
        </el-button>
      </div>
    </div>

    <!-- Filter Bar -->
    <div class="iot-card filter-card">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="设备 ID / 名称">
          <el-input v-model="queryForm.keyword" placeholder="搜索设备 ID 或名称" clearable />
        </el-form-item>
        <el-form-item label="绑定驱动">
          <el-select v-model="queryForm.driverCode" placeholder="全部驱动" clearable style="width: 170px">
            <el-option
              v-for="d in driverOptions"
              :key="d.driverCode"
              :label="d.driverName"
              :value="d.driverCode"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="物模型模板">
          <el-select v-model="queryForm.modelKey" placeholder="全部物模型" clearable style="width: 170px">
            <el-option
              v-for="m in thingModelOptions"
              :key="m.modelCode"
              :label="m.modelName"
              :value="m.modelCode"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="数采状态">
          <el-select v-model="queryForm.acqStatus" placeholder="全部状态" clearable style="width: 130px">
            <el-option label="数采运行中" :value="1" />
            <el-option label="已暂停数采" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- Device Cards Grid Layout -->
    <div v-if="filteredDevices.length > 0" class="device-cards-grid margin-top">
      <div v-for="dev in paginatedDevices" :key="dev.deviceId" class="iot-card device-card" :class="{ 'is-disabled': dev.acqStatus === 0 }">
        <!-- Card Top Header Bar -->
        <div class="device-card-header">
          <!-- Top Row: Device ID & Online Status & Acquisition Switch -->
          <div class="card-meta-top-row">
            <div class="id-status-group">
              <code class="dev-card-id">{{ dev.deviceId }}</code>
              <StatusBadge :status="dev.status" :text="dev.status === 1 ? '在线' : '离线'" />
            </div>
            <div class="acq-switch-box">
              <span class="acq-lbl">数采状态</span>
              <el-switch
                v-model="dev.acqStatus"
                :active-value="1"
                :inactive-value="0"
                :disabled="!hasPerm('device:control')"
                inline-prompt
                active-text="运行"
                inactive-text="暂停"
                @change="handleAcqStatusChange(dev)"
              />
            </div>
          </div>

          <!-- Dedicated Device Name Row (单独一行) -->
          <div class="dev-name-full-row">
            <span class="live-status-dot" :class="{ online: dev.status === 1 }"></span>
            <span class="dev-card-name" :title="dev.deviceName">{{ dev.deviceName }}</span>
          </div>
        </div>

        <!-- Meta Specification Body -->
        <div class="device-card-body">
          <div class="specs-grid">
            <div class="meta-spec-item">
              <span class="spec-lbl">物模型模板:</span>
              <el-tag size="small" type="success" class="clickable-tag" @click="viewThingModel(dev)">
                {{ dev.modelKey }} (物模型)
              </el-tag>
            </div>
            <div class="meta-spec-item">
              <span class="spec-lbl">通信协议:</span>
              <span class="driver-code">{{ dev.driverCode }}</span>
            </div>
            <div class="meta-spec-item">
              <span class="spec-lbl">基础数采周期:</span>
              <el-tag size="small" type="primary" plain>{{ dev.baseIntervalMs || 1000 }} ms</el-tag>
            </div>
            <div class="meta-spec-item">
              <span class="spec-lbl">物理通道:</span>
              <StatusBadge :status="dev.status" :text="dev.status === 1 ? '通道在线' : '断开'" />
            </div>
            <div class="meta-spec-item">
              <span class="spec-lbl">连接配置:</span>
              <span class="conn-spec-text">{{ formatConnParams(dev) }}</span>
            </div>
          </div>

        </div>

        <!-- Card Actions Footer -->
        <div class="device-card-footer">
          <el-button size="small" type="warning" plain @click="openAddressMappingDrawer(dev)">
            地址映射
          </el-button>
          <el-button v-if="hasPerm('device:control')" size="small" type="success" @click="openControlPanel(dev)">
            反控调试
          </el-button>
          <el-button v-if="hasPerm(['device:add', 'device:edit'])" size="small" type="primary" plain @click="openEditModal(dev)">
            编辑
          </el-button>
          <el-button v-if="hasPerm(['device:add', 'device:edit'])" size="small" type="danger" plain @click="handleDelete(dev)">
            删除
          </el-button>
        </div>
      </div>
    </div>

    <!-- Empty State -->
    <div v-else class="iot-card empty-card margin-top">
      <el-empty description="暂无符合筛选条件的设备记录" />
    </div>

    <!-- Device Table Pagination -->
    <div class="pagination-wrapper margin-top">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[8, 16, 24, 32]"
        :total="filteredDevices.length"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- Add / Edit Device Dialog (With DYNAMIC DRIVER PARAMS FORM) -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑设备连接配置' : '新增接入物理设备'"
      width="640px"
      class="device-modal-dialog"
      destroy-on-close
    >
      <el-form :model="deviceForm" :rules="rules" ref="deviceFormRef" label-width="150px" class="device-dialog-form">
        <el-form-item label="设备唯一 ID" prop="deviceId">
          <el-input v-model="deviceForm.deviceId" :disabled="isEdit" placeholder="不填则系统自动生成 (如 DEV_A1B2C3D4)" style="width: 360px" />
        </el-form-item>
        <el-form-item label="设备显示名称" prop="deviceName">
          <el-input v-model="deviceForm.deviceName" placeholder="如 4号离心泵PLC" style="width: 360px" />
        </el-form-item>
        <el-form-item label="绑定物模型模板" prop="modelKey">
          <el-select v-model="deviceForm.modelKey" style="width: 360px" @change="onModelChange">
            <el-option
              v-for="tm in thingModelOptions"
              :key="tm.modelCode"
              :label="`${tm.modelCode} (${tm.modelName})`"
              :value="tm.modelCode"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="通信协议驱动" prop="driverCode">
          <el-select v-model="deviceForm.driverCode" placeholder="请选择协议驱动" style="width: 360px" @change="onDriverCodeChange">
            <el-option
              v-for="d in driverOptions"
              :key="d.driverCode"
              :label="`${d.driverName} (${d.driverCode})`"
              :value="d.driverCode"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="基础数采周期" prop="baseIntervalMs">
          <div class="input-with-unit">
            <el-input-number v-model="deviceForm.baseIntervalMs" :min="100" :max="3600000" :step="100" style="width: 180px" />
            <span class="unit-text">毫秒 (ms)</span>
          </div>
          <span style="font-size: 0.78rem; color: #94a3b8; display: block; margin-top: 4px;">
            物理通道巡检该设备的基础轮询采样周期基频 (默认 1000 ms)
          </span>
        </el-form-item>

        <!-- DYNAMIC PARAMS FORM (Adapts to Selected Driver Schema) -->
        <el-divider content-position="left">驱动特定连接属性配置 (Dynamic Driver Params)</el-divider>

        <template v-for="param in currentDriverParams" :key="param.key">
          <el-form-item :label="param.name" :required="param.required">
            <!-- SELECT / ENUM 下拉选择框 -->
            <el-select
              v-if="param.dataType === 'SELECT' || param.dataType === 'ENUM'"
              v-model="deviceForm.connectionParams[param.key]"
              placeholder="请选择"
              style="width: 360px"
            >
              <el-option
                v-for="opt in param.options"
                :key="opt.value"
                :label="opt.label"
                :value="opt.value"
              />
            </el-select>

            <!-- INT 整数数字输入框 (端口号、从站ID、机架/插槽等) -->
            <div v-else-if="param.dataType === 'INT'" class="input-with-unit">
              <el-input-number
                v-model="deviceForm.connectionParams[param.key]"
                :min="param.key === 'port' ? 1 : 0"
                :max="param.key === 'port' ? 65535 : 65535"
                style="width: 180px"
              />
            </div>

            <!-- BOOL 开关 -->
            <el-switch
              v-else-if="param.dataType === 'BOOL'"
              v-model="deviceForm.connectionParams[param.key]"
            />

            <!-- 普通文本输入框 (STRING 等) -->
            <el-input
              v-else
              v-model="deviceForm.connectionParams[param.key]"
              :placeholder="param.defaultValue ? `默认: ${param.defaultValue}` : `请输入 ${param.name}`"
              style="width: 360px"
            />

            <span v-if="param.description" style="font-size: 0.78rem; color: #94a3b8; display: block; margin-top: 4px;">
              {{ param.description }}
            </span>
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitDeviceForm">保存设备属性</el-button>
      </template>
    </el-dialog>

    <!-- DEVICE CONTROL & WRITE PANEL DRAWER (控制调试/数据写入面板) -->
    <el-drawer
      v-model="controlDrawerVisible"
      title="设备反向控制与点位调试终端"
      size="720px"
      destroy-on-close
    >
      <div v-if="selectedDev" class="control-panel">
        <!-- Hero Device Connection Banner -->
        <div class="hero-banner-card">
          <div class="banner-top-row">
            <div class="dev-title">
              <span class="live-pulse-dot"></span>
              <span class="dev-name">{{ selectedDev.deviceName }}</span>
              <code class="dev-code">{{ selectedDev.deviceId }}</code>
            </div>
            <div class="live-status-tag">
              <span class="ping-text">ONLINE</span>
              <span class="latency-text">12ms</span>
            </div>
          </div>
          <div class="banner-meta-row">
            <div class="meta-item">
              <span class="meta-label">物模型:</span>
              <span class="meta-val text-cyan">{{ selectedDev.modelKey }}</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">驱动协议:</span>
              <span class="meta-val text-green">{{ selectedDev.driverCode }}</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">物理通道:</span>
              <span class="meta-val text-mono">{{ formatConnParams(selectedDev) }}</span>
            </div>
          </div>
        </div>

        <!-- Command Execution Feedback Alert -->
        <div v-if="writeResult" class="margin-top-small">
          <el-alert
            :title="writeResult"
            :type="writeResult.includes('失败') || writeResult.includes('异常') || writeResult.includes('FAIL') ? 'error' : 'success'"
            show-icon
            closable
            @close="writeResult = ''"
          />
        </div>

        <!-- Custom Segmented Tab Header -->
        <div class="segment-tab-bar">
          <div
            class="tab-btn"
            :class="{ active: activeControlTab === 'properties' }"
            @click="activeControlTab = 'properties'"
          >
            <SvgIcon name="send" :size="14" />
            <span>点位属性反写 ({{ writableProperties.length }})</span>
          </div>
          <div
            class="tab-btn"
            :class="{ active: activeControlTab === 'services' }"
            @click="activeControlTab = 'services'"
          >
            <SvgIcon name="cpu" :size="14" />
            <span>服务指令下发 ({{ serviceList.length }})</span>
          </div>
          <div
            class="tab-btn"
            :class="{ active: activeControlTab === 'events' }"
            @click="activeControlTab = 'events'"
          >
            <SvgIcon name="bell" :size="14" />
            <span>物模型事件明细 ({{ eventList.length }})</span>
          </div>
        </div>

        <!-- TAB 1: PROPERTIES WRITE PANEL -->
        <div v-if="activeControlTab === 'properties'" class="panel-card-container">
          <div v-if="writableProperties.length === 0" class="padding-20">
            <el-empty description="当前关联的物模型模板未定义属性点位" />
          </div>
          <el-form v-else label-position="top">
            <el-form-item label="目标控制点位 (Select Property Point)">
              <el-select
                v-model="selectedPropertyIdentifier"
                style="width: 100%"
                placeholder="请选择需要查看或写入下发的属性点位"
                @change="onPropertySelectChange"
              >
                <el-option
                  v-for="prop in writableProperties"
                  :key="prop.identifier"
                  :label="`${prop.name} (${prop.identifier}) - [${prop.accessMode}]`"
                  :value="prop.identifier"
                />
              </el-select>
            </el-form-item>

            <!-- Property Spec & Metric Preview Card -->
            <div v-if="currentProperty" class="property-preview-card">
              <div class="preview-header">
                <div class="prop-title-group">
                  <span class="prop-title">{{ currentProperty.name }}</span>
                  <code class="prop-ident">{{ currentProperty.identifier }}</code>
                </div>
                <div class="prop-badges">
                  <el-tag size="small" :type="currentProperty.isWritable ? 'warning' : 'info'" effect="dark">
                    {{ currentProperty.accessMode === 'RW' ? 'RW 读写' : (currentProperty.isWritable ? 'W 只写' : 'R 只读') }}
                  </el-tag>
                  <el-tag size="small" type="primary" effect="dark">{{ currentProperty.dataType }}</el-tag>
                  <el-tag v-if="currentProperty.physicalAddress" size="small" type="info">物理地址: {{ currentProperty.physicalAddress }}</el-tag>
                  <el-tag v-if="selectedDev?.connectionParams?.byteOrder" size="small" type="warning">字节序: {{ selectedDev.connectionParams.byteOrder }}</el-tag>
                  <el-tag v-if="currentProperty.unit" size="small" type="success">{{ currentProperty.unit }}</el-tag>
                </div>
              </div>
              <div class="prop-desc">{{ currentProperty.description }}</div>

              <div class="metric-comparison-row">
                <div class="metric-box">
                  <div class="metric-lbl-row">
                    <span class="metric-lbl">实时读数值</span>
                    <el-button
                      size="small"
                      type="primary"
                      link
                      :loading="loadingLiveSnapshot"
                      @click="fetchDeviceLiveSnapshot(selectedDev.deviceId)"
                      title="点击刷新当前测点最新实时数据快照"
                    >
                      <SvgIcon name="refresh" :size="13" />
                      <span>刷新</span>
                    </el-button>
                  </div>
                  <div class="metric-val text-green" :class="{ 'live-active': getCurrentPropertyLiveInfo(currentProperty).hasLive }">
                    {{ getCurrentPropertyLiveInfo(currentProperty).displayVal }}
                  </div>
                  <div v-if="getCurrentPropertyLiveInfo(currentProperty).hasLive" class="metric-live-sub">
                    <span class="quality-badge" :class="getCurrentPropertyLiveInfo(currentProperty).quality === 1 ? 'good' : 'bad'">
                      {{ getCurrentPropertyLiveInfo(currentProperty).quality === 1 ? '● 质量良好' : '▲ 质量异常' }}
                    </span>
                    <span v-if="getCurrentPropertyLiveInfo(currentProperty).timeText" class="time-text">
                      更新于 {{ getCurrentPropertyLiveInfo(currentProperty).timeText }}
                    </span>
                  </div>
                </div>
                <div v-if="currentProperty.isWritable" class="metric-divider"></div>
                <div v-if="currentProperty.isWritable" class="metric-box">
                  <div class="metric-lbl">下发目标设定值</div>
                  <div class="metric-val text-cyan">
                    {{ writeFormValue !== null && writeFormValue !== undefined ? writeFormValue : '--' }} {{ currentProperty.unit || '' }}
                  </div>
                </div>
              </div>
            </div>

            <!-- Dynamic Input Control Section -->
            <div v-if="currentProperty && currentProperty.isWritable" class="input-control-card margin-top">
              <div class="control-card-header">设定写入数值 (Set Target Value)</div>

              <!-- INT / DOUBLE / FLOAT SLIDER & NUMBER INPUT -->
              <template v-if="currentProperty.dataType === 'DOUBLE' || currentProperty.dataType === 'FLOAT' || currentProperty.dataType === 'INT' || currentProperty.dataType === 'INT32' || currentProperty.dataType === 'INT64'">
                <div class="slider-wrapper margin-top-small">
                  <el-slider
                    v-model="writeFormValue"
                    :min="currentProperty.min ?? 0"
                    :max="currentProperty.max ?? 500"
                    :step="isFloatingType(currentProperty.dataType) ? 0.01 : 1"
                  />
                </div>
                <div class="input-row margin-top-small">
                  <el-input-number
                    v-model="writeFormValue"
                    :precision="isFloatingType(currentProperty.dataType) ? 4 : 0"
                    :step="isFloatingType(currentProperty.dataType) ? 0.01 : 1"
                    :min="currentProperty.min ?? 0"
                    :max="currentProperty.max ?? 500"
                    style="flex: 1;"
                  />
                  <!-- Quick Preset Percentage Buttons -->
                  <div class="quick-preset-btns">
                    <el-button size="small" @click="setPresetValue(0.25)">25%</el-button>
                    <el-button size="small" @click="setPresetValue(0.50)">50%</el-button>
                    <el-button size="small" @click="setPresetValue(0.75)">75%</el-button>
                    <el-button size="small" @click="setPresetValue(1.00)">100%</el-button>
                  </div>
                </div>
              </template>

              <!-- BOOL SWITCH CARD -->
              <template v-else-if="currentProperty.dataType === 'BOOL'">
                <div class="switch-card-wrapper margin-top-small">
                  <div
                    class="switch-state-box"
                    :class="{ active: writeFormValue === true }"
                    @click="writeFormValue = true"
                  >
                    <span class="state-dot green"></span>
                    <span>开启 (TRUE / 1)</span>
                  </div>
                  <div
                    class="switch-state-box"
                    :class="{ active: writeFormValue === false }"
                    @click="writeFormValue = false"
                  >
                    <span class="state-dot red"></span>
                    <span>关闭 (FALSE / 0)</span>
                  </div>
                </div>
              </template>

              <!-- ENUM PILL SELECTOR CARDS -->
              <template v-else-if="currentProperty.dataType === 'ENUM'">
                <div class="enum-pills-wrapper margin-top-small">
                  <div
                    v-for="opt in currentProperty.options"
                    :key="opt.value"
                    class="enum-pill-card"
                    :class="{ active: writeFormValue === opt.value }"
                    @click="writeFormValue = opt.value"
                  >
                    <span class="pill-title">{{ opt.label }}</span>
                    <span class="pill-val">Value: {{ opt.value }}</span>
                  </div>
                </div>
              </template>

              <!-- STRING INPUT -->
              <template v-else>
                <el-input v-model="writeFormValue" placeholder="请输入要下发的文本数据" class="margin-top-small" />
              </template>
            </div>

            <!-- Read Only Notice -->
            <div v-else-if="currentProperty && !currentProperty.isWritable" class="margin-top">
              <el-alert
                title="只读 (R) 测点提示"
                type="info"
                description="该物模型点位为只读(R)监测数据，由底层设备采集上报，不支持反控下发写入。"
                show-icon
                :closable="false"
              />
            </div>

            <!-- Write Command Action Button -->
            <div v-if="currentProperty && currentProperty.isWritable" class="margin-top">
              <el-button :disabled="!hasPerm('device:control')" type="success" size="large" class="cyber-btn full-width" @click="handleWriteProperty">
                <SvgIcon name="send" :size="16" />
                下发点位写入指令 (Send Write Command)
              </el-button>
            </div>
          </el-form>
        </div>

        <!-- TAB 2: SERVICE EXECUTION PANEL -->
        <div v-if="activeControlTab === 'services'" class="panel-card-container">
          <div v-if="serviceList.length === 0" class="padding-20">
            <el-empty description="当前关联的物模型模板未定义控制服务方法" />
          </div>
          <el-form v-else label-position="top">
            <el-form-item label="选择控制服务 (Select Command Service)">
              <el-select
                v-model="selectedServiceIdentifier"
                style="width: 100%"
                placeholder="请选择要调用的控制服务"
                @change="onServiceSelectChange"
              >
                <el-option
                  v-for="srv in serviceList"
                  :key="srv.identifier"
                  :label="`${srv.name} (${srv.identifier})`"
                  :value="srv.identifier"
                />
              </el-select>
            </el-form-item>

            <!-- Service Info Card -->
            <div v-if="currentService" class="property-preview-card">
              <div class="preview-header">
                <div class="prop-title-group">
                  <span class="prop-title">{{ currentService.name }}</span>
                  <code class="prop-ident">{{ currentService.identifier }}</code>
                </div>
                <el-tag size="small" type="danger" effect="dark">SERVICE METHOD</el-tag>
              </div>
              <div class="prop-desc">{{ currentService.description }}</div>
            </div>

            <!-- Dynamic Service Params Inputs -->
            <div v-if="currentService && currentService.params.length > 0" class="input-control-card margin-top">
              <div class="control-card-header">设定服务参数 (Service Arguments)</div>
              <div class="params-form-grid margin-top-small">
                <div
                  v-for="param in currentService.params"
                  :key="param.identifier"
                  class="param-field-item"
                >
                  <label class="param-label">{{ param.name }} (<code>{{ param.identifier }}</code>)</label>

                  <el-input-number
                    v-if="param.dataType === 'INT' || param.dataType === 'DOUBLE' || param.dataType === 'FLOAT'"
                    v-model="dynamicServiceArgs[param.identifier]"
                    :min="param.min ?? 0"
                    :max="param.max ?? 65535"
                    :precision="param.dataType === 'INT' ? 0 : 3"
                    :step="param.dataType === 'INT' ? 1 : 0.1"
                    style="width: 100%"
                  />
                  <el-switch
                    v-else-if="param.dataType === 'BOOL'"
                    v-model="dynamicServiceArgs[param.identifier]"
                    active-text="True / 开启"
                    inactive-text="False / 关闭"
                    inline-prompt
                  />
                  <el-input
                    v-else
                    v-model="dynamicServiceArgs[param.identifier]"
                    :placeholder="`请输入 ${param.name}`"
                  />
                </div>
              </div>
            </div>

            <!-- Service Execution Button -->
            <div v-if="currentService" class="margin-top">
              <el-button :disabled="!hasPerm('device:control')" type="primary" size="large" class="cyber-btn full-width" @click="handleSendService">
                <SvgIcon name="send" :size="16" />
                执行服务指令下发 (Execute Service Method)
              </el-button>
            </div>
          </el-form>
        </div>

        <!-- TAB 3: EVENT DEFINITIONS PANEL -->
        <div v-if="activeControlTab === 'events'" class="panel-card-container">
          <div v-if="eventList.length === 0" class="padding-20">
            <el-empty description="当前关联的物模型模板未定义告警/通知事件" />
          </div>
          <div v-else class="events-list-container margin-top-small">
            <div
              v-for="evt in eventList"
              :key="evt.identifier"
              class="property-preview-card margin-bottom-small"
            >
              <div class="preview-header">
                <div class="prop-title-group">
                  <span class="prop-title">{{ evt.name }}</span>
                  <code class="prop-ident">{{ evt.identifier }}</code>
                </div>
                <div class="prop-badges">
                  <el-tag size="small" :type="evt.eventType === 'ALERT' ? 'danger' : 'warning'" effect="dark">
                    {{ evt.eventType }}
                  </el-tag>
                  <el-tag size="small" :type="evt.eventLevel === 'CRITICAL' ? 'danger' : (evt.eventLevel === 'WARN' ? 'warning' : 'info')">
                    {{ evt.eventLevel }}
                  </el-tag>
                </div>
              </div>
              <div class="prop-desc" style="margin-top: 8px;">{{ evt.description }}</div>
            </div>
          </div>
        </div>
      </div>
    </el-drawer>

    <!-- DEVICE POINT ADDRESS SPEC MAPPING DRAWER (设备物理点位/协议地址映射配置) -->
    <el-drawer
      v-model="addressMappingDrawerVisible"
      title="设备物理点位/协议地址映射配置 (Point Address Mapping)"
      size="980px"
      destroy-on-close
    >
      <div v-if="selectedDevForAddress" class="control-panel">
        <div class="hero-banner-card">
          <div class="banner-top-row">
            <div class="dev-title">
              <span class="dev-name">{{ selectedDevForAddress.deviceName }}</span>
              <code class="dev-code">{{ selectedDevForAddress.deviceId }}</code>
            </div>
            <el-tag size="small" type="warning" effect="dark">物理地址映射配置</el-tag>
          </div>
          <div class="banner-meta-row">
            <span>物模型: <code class="text-cyan">{{ selectedDevForAddress.modelKey }}</code></span>
            <span>协议驱动: <span class="driver-code">{{ selectedDevForAddress.driverCode }}</span></span>
            <span>通信通道: <span class="text-mono">{{ formatConnParams(selectedDevForAddress) }}</span></span>
          </div>
        </div>

        <!-- Protocol & Formula Explanation Banner -->
        <div class="protocol-guide-banner margin-top-small">
          <div class="guide-title">
            <SvgIcon name="info" :size="15" />
            <span>【工业采集物理地址与数据换算原理】</span>
          </div>
          <div class="guide-content">
            <p style="margin: 0 0 6px 0;">
              <strong>计算公式：</strong>
              <code class="formula-box">实际工程值 = (底层硬件原始读取值 × 缩放系数 Scale) + 零点偏置 Offset</code>
            </p>
            <p style="margin: 0; font-size: 0.8rem; color: #94a3b8;">
              • <strong>缩放系数 (Scale)</strong>：用于单位缩放或倍率换算。例如 Modbus 寄存器读取值为 <code>253</code>，系数设为 <code>0.1</code>，转换为 <code>25.3 ℃</code>。<br/>
              • <strong>零点偏置 (Offset)</strong>：用于零点校准或环境偏差修正。例如传感器默认存在 <code>-1.5</code> 零漂，偏置设为 <code>1.5</code> 进行修正。
            </p>
          </div>
        </div>

        <div class="panel-card-container margin-top">
          <div class="tab-toolbar">
            <span class="tab-tip">为当前物理设备的各逻辑点位独立映射硬件内存偏置地址、缩放系数与零点偏置</span>
            <div class="toolbar-actions">
              <el-button size="small" @click="autoFillDefaultAddresses">
                自动填充示例地址
              </el-button>
              <el-button type="primary" size="small" @click="saveAddressMappings">
                保存物理点位地址映射
              </el-button>
            </div>
          </div>

          <el-table :data="devicePointAddressList" style="width: 100%" stripe class="margin-top-small" border>
            <el-table-column prop="pointCode" label="逻辑点位 Code" width="135">
              <template #default="scope">
                <code class="text-cyan">{{ scope.row.pointCode }}</code>
              </template>
            </el-table-column>

            <el-table-column prop="pointName" label="点位名称" width="135" show-overflow-tooltip />

            <el-table-column prop="dataType" label="数据类型" width="130">
              <template #default="scope">
                <el-select v-model="scope.row.dataType" size="small" style="width: 100%">
                  <el-option label="FLOAT (32位单精度)" value="FLOAT" />
                  <el-option label="DOUBLE (64位双精度)" value="DOUBLE" />
                  <el-option label="INT (16位整数)" value="INT" />
                  <el-option label="INT32 (32位整数)" value="INT32" />
                  <el-option label="INT64 (64位长整数)" value="INT64" />
                  <el-option label="BOOL (布尔)" value="BOOL" />
                  <el-option label="STRING (文本)" value="STRING" />
                  <el-option label="ENUM (枚举)" value="ENUM" />
                </el-select>
              </template>
            </el-table-column>

            <el-table-column prop="rwMode" label="读写" width="70">
              <template #default="scope">
                <el-tag size="small" :type="scope.row.rwMode === 'RW' ? 'success' : 'warning'">
                  {{ scope.row.rwMode === 'RW' ? '读写' : '只读' }}
                </el-tag>
              </template>
            </el-table-column>

            <el-table-column label="物理寄存器地址" min-width="240">
              <template #header>
                <div class="col-header-with-tip">
                  <span>物理寄存器地址</span>
                  <el-tooltip content="底层 PLC/Modbus/OPC UA 对应的硬件内存偏置或变量节点" placement="top">
                    <SvgIcon name="info" :size="12" style="color: #38bdf8" />
                  </el-tooltip>
                </div>
              </template>
              <template #default="scope">
                <el-input
                  v-model="scope.row.physicalAddress"
                  clearable
                  size="default"
                  :placeholder="getAddressPlaceholder(selectedDevForAddress.driverCode)"
                >
                  <template #prefix>
                    <SvgIcon name="setting" :size="14" style="color: #64748b" />
                  </template>
                </el-input>
              </template>
            </el-table-column>

            <el-table-column label="缩放系数 (Scale)" width="130">
              <template #header>
                <div class="col-header-with-tip">
                  <span>缩放系数</span>
                  <el-tooltip content="实际值 = 原始值 × 系数 + 偏置" placement="top">
                    <SvgIcon name="info" :size="12" style="color: #38bdf8" />
                  </el-tooltip>
                </div>
              </template>
              <template #default="scope">
                <el-input-number
                  v-model="scope.row.scale"
                  :precision="2"
                  :step="0.1"
                  size="small"
                  style="width: 100%"
                />
              </template>
            </el-table-column>

            <el-table-column label="零点偏置 (Offset)" width="130">
              <template #header>
                <div class="col-header-with-tip">
                  <span>零点偏置</span>
                  <el-tooltip content="零点校准或偏差修正量" placement="top">
                    <SvgIcon name="info" :size="12" style="color: #38bdf8" />
                  </el-tooltip>
                </div>
              </template>
              <template #default="scope">
                <el-input-number
                  v-model="scope.row.offset"
                  :precision="1"
                  :step="1"
                  size="small"
                  style="width: 100%"
                />
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import SvgIcon from '../../components/common/SvgIcon.vue'
import StatusBadge from '../../components/common/StatusBadge.vue'
import { sendControlCommand, getLoadedDrivers } from '../../api/protocol'
import {
  getDeviceList,
  addDevice,
  updateDevice,
  deleteDevice,
  updateAcqStatus,
  getPointAddressMappings,
  suggestPointAddressMappings,
  savePointAddressMappings,
  controlDevicePoint,
  invokeDeviceService
} from '../../api/device'
import { getThingModel, getAllThingModels } from '../../api/thingmodel'
import { getLatestPointData } from '../../api/data'
import { hasPermission } from '../../utils/permission'

const hasPerm = hasPermission

const router = useRouter()
const route = useRoute()
const queryForm = ref({ keyword: '', modelKey: '', driverCode: '', acqStatus: null })
const activeQueryParams = ref({ keyword: '', modelKey: '', driverCode: '', acqStatus: null })

const devices = ref([])
const thingModelOptions = ref([])

const fetchThingModelOptions = async () => {
  try {
    const res = await getAllThingModels()
    const list = (res && res.data) ? res.data : (Array.isArray(res) ? res : [])
    if (Array.isArray(list)) {
      thingModelOptions.value = list.map(m => ({
        modelCode: m.modelKey || m.modelCode,
        modelName: m.modelName || m.modelKey || m.modelCode
      }))
    } else {
      thingModelOptions.value = []
    }
  } catch (e) {
    console.warn('Fetch thing model options failed:', e)
    thingModelOptions.value = []
  }
}

const fetchDevices = async () => {
  try {
    const res = await getDeviceList({
      keyword: activeQueryParams.value.keyword,
      driverCode: activeQueryParams.value.driverCode,
      acqStatus: activeQueryParams.value.acqStatus
    })
    const data = (res && res.data) ? res.data : res
    const list = Array.isArray(data) ? data : ((data && Array.isArray(data.list)) ? data.list : [])
    devices.value = list.map(item => {
      let conn = item.connectionParams
      if (typeof conn === 'string') {
        try { conn = JSON.parse(conn) } catch (err) { conn = {} }
      }
      return {
        ...item,
        connectionParams: conn || { host: item.host || '127.0.0.1', port: item.port || 502 }
      }
    })
  } catch (e) {
    console.warn('获取设备列表失败', e)
    devices.value = []
  }
}

const driverList = ref([])

const fetchDrivers = async () => {
  try {
    const res = await getLoadedDrivers({ pageNum: 1, pageSize: 50 })
    const data = (res && res.data) ? res.data : res
    const list = Array.isArray(data) ? data : (data && Array.isArray(data.list) ? data.list : [])
    if (list.length > 0) {
      driverList.value = list
    }
  } catch (e) {
    console.warn('Fetch driver list failed:', e)
  }
}

const refreshing = ref(false)
let refreshTimer = null

const handleManualRefresh = async () => {
  refreshing.value = true
  try {
    await fetchDevices()
    ElMessage.success('设备通道状态已同步刷新')
  } catch (e) {
    ElMessage.error('刷新失败')
  } finally {
    refreshing.value = false
  }
}

onMounted(() => {
  if (route.query && route.query.modelKey) {
    const mk = String(route.query.modelKey).trim()
    queryForm.value.modelKey = mk
    activeQueryParams.value.modelKey = mk
  }

  fetchDevices()
  fetchThingModelOptions()
  fetchDrivers()

  // 周期性自动同步设备物理通信在线/离线通道状态 (每4秒自动检测一次)
  refreshTimer = setInterval(() => {
    fetchDevices()
  }, 4000)
})

watch(() => route.query.modelKey, (newVal) => {
  if (newVal) {
    const mk = String(newVal).trim()
    queryForm.value.modelKey = mk
    activeQueryParams.value.modelKey = mk
    currentPage.value = 1
  }
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
})

// Format Connection Spec for Table
const formatConnParams = (row) => {
  const p = row.connectionParams || {}
  if (row.driverCode === 'modbus-tcp') {
    const orderStr = p.byteOrder ? ` [${p.byteOrder}]` : ''
    return `IP: ${p.host || '192.168.1.100'}:${p.port || 502} (Slave ${p.slaveId || 1})${orderStr}`
  } else if (row.driverCode === 's7-driver') {
    const orderStr = p.byteOrder ? ` [${p.byteOrder}]` : ''
    const plcStr = p.plcType ? ` ${p.plcType}` : ''
    return `IP: ${p.host || '192.168.1.200'}:${p.port || 102} (R${p.rack ?? 0}/S${p.slot ?? 1})${plcStr}${orderStr}`
  } else if (row.driverCode === 'opc-ua') {
    return p.endpointUrl || 'opc.tcp://192.168.1.50:4840'
  }
  return `${row.host || ''}:${row.port || ''}`
}

// Telemetry Acquisition Switch Handler
const handleAcqStatusChange = async (row) => {
  if (!hasPerm('device:control')) {
    row.acqStatus = row.acqStatus === 1 ? 0 : 1
    ElMessage.warning('当前账号无切换设备数采状态控制权限 (device:control)')
    return
  }
  try {
    await updateAcqStatus(row.deviceId, row.acqStatus)
    const stateText = row.acqStatus === 1 ? '开启' : '暂停'
    ElMessage.success(`设备 [${row.deviceName}] 数采状态已切换为: ${stateText}`)
  } catch (err) {
    row.acqStatus = row.acqStatus === 1 ? 0 : 1
    ElMessage.error('更新设备数采状态失败: ' + (err.message || ''))
  }
}

// Filter & Pagination
const currentPage = ref(1)
const pageSize = ref(8)

const filteredDevices = computed(() => {
  return devices.value.filter(dev => {
    const matchKw = !activeQueryParams.value.keyword || dev.deviceId.includes(activeQueryParams.value.keyword) || dev.deviceName.includes(activeQueryParams.value.keyword)
    const matchDriver = !activeQueryParams.value.driverCode || dev.driverCode === activeQueryParams.value.driverCode
    const matchModel = !activeQueryParams.value.modelKey || dev.modelKey === activeQueryParams.value.modelKey
    const matchAcq = activeQueryParams.value.acqStatus === null || activeQueryParams.value.acqStatus === '' || dev.acqStatus === activeQueryParams.value.acqStatus
    return matchKw && matchDriver && matchModel && matchAcq
  })
})

const paginatedDevices = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredDevices.value.slice(start, start + pageSize.value)
})

const totalDevices = computed(() => filteredDevices.value.length)

const handleSearch = () => {
  activeQueryParams.value = { ...queryForm.value }
  currentPage.value = 1
  fetchDevices()
}

const resetQuery = () => {
  queryForm.value = { keyword: '', modelKey: '', driverCode: '', acqStatus: null }
  activeQueryParams.value = { keyword: '', modelKey: '', driverCode: '', acqStatus: null }
  currentPage.value = 1
  fetchDevices()
}
const handleSizeChange = (val) => { pageSize.value = val; currentPage.value = 1 }
const handleCurrentChange = (val) => { currentPage.value = val }

// Associated Thing Model Redirect
const viewThingModel = (row) => {
  router.push('/thingmodels')
}

// Dynamic Driver Schemas & Parameter Definition
const driverOptions = computed(() => {
  if (driverList.value && driverList.value.length > 0) {
    return driverList.value.map(d => ({
      driverCode: d.driverCode,
      driverName: d.driverName || d.driverCode
    }))
  }
  return [
    { driverCode: 'modbus-tcp', driverName: 'Modbus TCP 工业协议驱动' },
    { driverCode: 's7-driver', driverName: '西门子 S7 工业 PLC 协议驱动' },
    { driverCode: 'opc-ua', driverName: 'OPC UA 规范协议驱动' }
  ]
})

const currentDriverParams = computed(() => {
  const code = deviceForm.value.driverCode
  const driver = driverList.value.find(d => d.driverCode === code)

  let fallback = []
  if (code === 'modbus-tcp') {
    fallback = [
      { key: 'host', name: 'IP 地址', dataType: 'STRING', required: true, defaultValue: '192.168.1.100' },
      { key: 'port', name: '通信端口', dataType: 'INT', required: true, defaultValue: '502' },
      { key: 'slaveId', name: '从站 ID', dataType: 'INT', required: true, defaultValue: '1' },
      {
        key: 'byteOrder',
        name: '寄存器字节序',
        dataType: 'SELECT',
        required: false,
        defaultValue: 'CDAB',
        description: '32位/64位整型与浮点数在寄存器中的存储排列顺序 (推荐CDAB)',
        options: [
          { label: 'CDAB (工业推荐 / 32位小端字大端字节)', value: 'CDAB' },
          { label: 'ABCD (标准大端模式 / Big-Endian)', value: 'ABCD' },
          { label: 'DCBA (完全小端模式 / Little-Endian)', value: 'DCBA' },
          { label: 'BADC (字节交换模式 / Byte Swap)', value: 'BADC' },
          { label: 'AB (16位标准大端)', value: 'AB' },
          { label: 'BA (16位字节交换)', value: 'BA' },
          { label: 'ABCDEFGH (64位标准大端)', value: 'ABCDEFGH' },
          { label: 'GHEFCDAB (64位双字交换)', value: 'GHEFCDAB' },
          { label: 'HGFEDCBA (64位完全小端)', value: 'HGFEDCBA' },
          { label: 'BADCFEHG (64位字节交换)', value: 'BADCFEHG' }
        ]
      }
    ]
  } else if (code === 's7-driver') {
    fallback = [
      { key: 'host', name: 'PLC IP地址', dataType: 'STRING', required: true, defaultValue: '192.168.1.200' },
      { key: 'port', name: '端口号', dataType: 'INT', required: true, defaultValue: '102' },
      { key: 'rack', name: '机架号 (Rack)', dataType: 'INT', required: true, defaultValue: '0' },
      { key: 'slot', name: '插槽号 (Slot)', dataType: 'INT', required: true, defaultValue: '1' },
      {
        key: 'plcType',
        name: 'PLC 型号',
        dataType: 'SELECT',
        required: false,
        defaultValue: 'S7-1200',
        description: '西门子 S7 系列控制器型号 (自动适配机架与插槽)',
        options: [
          { label: 'S7-1200 (默认 Rack 0, Slot 1)', value: 'S7-1200' },
          { label: 'S7-1500 (默认 Rack 0, Slot 1)', value: 'S7-1500' },
          { label: 'S7-300 (默认 Rack 0, Slot 2)', value: 'S7-300' },
          { label: 'S7-400 (默认 Rack 0, Slot 2)', value: 'S7-400' },
          { label: 'S7-200 SMART (默认 Rack 0, Slot 1)', value: 'S7-200-SMART' }
        ]
      },
      {
        key: 'byteOrder',
        name: '寄存器字节序',
        dataType: 'SELECT',
        required: false,
        defaultValue: 'ABCD',
        description: '32位/64位整型与浮点数在S7数据块中的存储排列顺序 (推荐ABCD)',
        options: [
          { label: 'ABCD (西门子原生标准大端模式 / Big-Endian)', value: 'ABCD' },
          { label: 'CDAB (32位小端字大端字节)', value: 'CDAB' },
          { label: 'DCBA (完全小端模式 / Little-Endian)', value: 'DCBA' },
          { label: 'BADC (字节交换模式 / Byte Swap)', value: 'BADC' },
          { label: 'AB (16位标准大端)', value: 'AB' },
          { label: 'BA (16位字节交换)', value: 'BA' },
          { label: 'ABCDEFGH (64位标准大端)', value: 'ABCDEFGH' },
          { label: 'GHEFCDAB (64位双字交换)', value: 'GHEFCDAB' },
          { label: 'HGFEDCBA (64位完全小端)', value: 'HGFEDCBA' },
          { label: 'BADCFEHG (64位字节交换)', value: 'BADCFEHG' }
        ]
      }
    ]
  } else if (code === 'opc-ua') {
    fallback = [
      { key: 'endpointUrl', name: 'Endpoint URL', dataType: 'STRING', required: true, defaultValue: 'opc.tcp://192.168.1.50:4840' },
      {
        key: 'securityPolicy',
        name: '安全策略',
        dataType: 'SELECT',
        required: false,
        defaultValue: 'None',
        options: [
          { label: 'None (无加密认证)', value: 'None' },
          { label: 'Basic256', value: 'Basic256' },
          { label: 'Basic256Sha256', value: 'Basic256Sha256' }
        ]
      }
    ]
  }

  if (driver && Array.isArray(driver.params) && driver.params.length > 0) {
    const existingKeys = new Set(driver.params.map(p => p.key))
    const missingFallback = fallback.filter(p => !existingKeys.has(p.key))
    return [...driver.params, ...missingFallback]
  }
  return fallback
})

// Add/Edit Device Modal (Dynamic Driver Form)
const dialogVisible = ref(false)
const isEdit = ref(false)
const deviceFormRef = ref(null)
const deviceForm = ref({
  deviceId: '',
  deviceName: '',
  modelKey: 'pk_demo',
  driverCode: 'modbus-tcp',
  baseIntervalMs: 1000,
  connectionParams: {}
})

const rules = {
  deviceName: [{ required: true, message: '请输入设备名称', trigger: 'blur' }],
  modelKey: [{ required: true, message: '请选择关联物模型', trigger: 'change' }],
  driverCode: [{ required: true, message: '请选择绑定驱动', trigger: 'change' }]
}

const modelDriverMap = {
  'pk_demo': 'modbus-tcp',
  'pk_plc_s7': 's7-driver',
  'pk_opcua_server': 'opc-ua'
}

const onModelChange = (pk) => {
  const driverCode = modelDriverMap[pk] || 'modbus-tcp'
  deviceForm.value.driverCode = driverCode
  onDriverCodeChange(driverCode)
}

const onDriverCodeChange = (code) => {
  const newParams = {}
  currentDriverParams.value.forEach(p => {
    let val = p.defaultValue
    if (p.dataType === 'INT') {
      val = Number(val) || 0
    } else if (p.dataType === 'BOOL') {
      val = val === 'true' || val === true
    }
    newParams[p.key] = val
  })
  deviceForm.value.connectionParams = newParams
}

const openAddModal = () => {
  if (!hasPerm('device:add')) {
    ElMessage.warning('当前账号无新增接入物理设备权限 (device:add)')
    return
  }
  isEdit.value = false
  deviceForm.value = {
    deviceId: '',
    deviceName: '',
    modelKey: 'pk_demo',
    driverCode: 'modbus-tcp',
    baseIntervalMs: 1000,
    connectionParams: {}
  }
  onDriverCodeChange('modbus-tcp')
  dialogVisible.value = true
}

const openEditModal = (row) => {
  if (!hasPerm(['device:add', 'device:edit'])) {
    ElMessage.warning('当前账号无编辑物理设备权限 (device:edit)')
    return
  }
  isEdit.value = true
  deviceForm.value = JSON.parse(JSON.stringify(row))
  if (!deviceForm.value.baseIntervalMs) deviceForm.value.baseIntervalMs = 1000
  if (!deviceForm.value.connectionParams) deviceForm.value.connectionParams = {}
  currentDriverParams.value.forEach(p => {
    if (deviceForm.value.connectionParams[p.key] === undefined && p.defaultValue !== undefined) {
      let val = p.defaultValue
      if (p.dataType === 'INT') val = Number(val) || 0
      else if (p.dataType === 'BOOL') val = val === 'true' || val === true
      deviceForm.value.connectionParams[p.key] = val
    }
  })
  dialogVisible.value = true
}

const submitDeviceForm = () => {
  if (!isEdit.value && !hasPerm('device:add')) {
    ElMessage.warning('当前账号无新增接入物理设备权限 (device:add)')
    return
  }
  if (isEdit.value && !hasPerm(['device:add', 'device:edit'])) {
    ElMessage.warning('当前账号无修改物理设备权限 (device:edit)')
    return
  }
  deviceFormRef.value.validate(async (valid) => {
    if (valid) {
      let finalDeviceId = deviceForm.value.deviceId ? deviceForm.value.deviceId.trim() : ''
      if (!finalDeviceId && !isEdit.value) {
        finalDeviceId = 'DEV_' + Math.random().toString(36).substring(2, 10).toUpperCase()
      }
      const payload = {
        deviceId: finalDeviceId,
        deviceName: deviceForm.value.deviceName,
        modelKey: deviceForm.value.modelKey,
        driverCode: deviceForm.value.driverCode,
        baseIntervalMs: Number(deviceForm.value.baseIntervalMs) || 1000,
        host: deviceForm.value.connectionParams?.host || '192.168.1.100',
        port: Number(deviceForm.value.connectionParams?.port) || 502,
        connectionParams: deviceForm.value.connectionParams || {},
        status: deviceForm.value.status ?? 1,
        acqStatus: deviceForm.value.acqStatus ?? 1
      }
      try {
        if (isEdit.value) {
          await updateDevice(deviceForm.value.deviceId, payload)
          ElMessage.success('设备连接属性更新成功')
        } else {
          await addDevice(payload)
          ElMessage.success('设备新增成功，已关联物模型')
        }
        dialogVisible.value = false
        fetchDevices()
      } catch (err) {
        ElMessage.error('保存设备失败: ' + (err.message || err))
      }
    }
  })
}

const handleDelete = (row) => {
  if (!hasPerm(['device:add', 'device:edit'])) {
    ElMessage.warning('当前账号无删除物理设备权限 (device:edit)')
    return
  }
  ElMessageBox.confirm(`确定要删除设备 [${row.deviceName}] 吗？`, '删除确认', { type: 'warning' }).then(async () => {
    try {
      await deleteDevice(row.deviceId)
      ElMessage.success('删除设备成功')
      fetchDevices()
    } catch (err) {
      ElMessage.error('删除设备失败')
    }
  })
}

// Device Control & Write Panel Drawer
const controlDrawerVisible = ref(false)
const selectedDev = ref(null)
const writeResult = ref('')
const activeControlTab = ref('properties')

// Dynamic Writable Properties, Services & Events from Thing Model
const writableProperties = ref([])
const serviceList = ref([])
const eventList = ref([])

const selectedPropertyIdentifier = ref('')
const currentProperty = ref(null)
const writeFormValue = ref(null)

// Real-time telemetry snapshot for reverse control debugging
const liveSnapshot = ref({})
const loadingLiveSnapshot = ref(false)

const fetchDeviceLiveSnapshot = async (deviceId) => {
  if (!deviceId) return
  loadingLiveSnapshot.value = true
  try {
    const res = await getLatestPointData(deviceId)
    liveSnapshot.value = (res && res.data) ? res.data : (res || {})
  } catch (err) {
    console.warn('获取设备实时点位快照失败:', err)
  } finally {
    loadingLiveSnapshot.value = false
  }
}

const getCurrentPropertyLiveInfo = (prop) => {
  if (!prop || !prop.identifier) {
    return { hasLive: false, displayVal: '--', rawValue: null, quality: 0, timeText: '' }
  }
  const snap = liveSnapshot.value[prop.identifier]
  if (!snap || snap.value === undefined || snap.value === null) {
    return { hasLive: false, displayVal: '-- (未采集)', rawValue: null, quality: 0, timeText: '' }
  }

  let displayVal = `${snap.value}`
  if (typeof snap.value === 'number' || (!isNaN(snap.value) && snap.value !== '')) {
    const num = Number(snap.value)
    if (Number.isFinite(num)) {
      displayVal = String(parseFloat(num.toFixed(6)))
    }
  }
  if (prop.dataType === 'ENUM') {
    const opt = (prop.options || []).find(o => String(o.value) === String(snap.value))
    displayVal = opt ? `${opt.label} (${snap.value})` : `状态值: ${snap.value}`
  } else if (prop.dataType === 'BOOL') {
    displayVal = (snap.value > 0 || String(snap.value) === 'true') ? '开启 (1 / TRUE)' : '关闭 (0 / FALSE)'
  } else if (prop.unit) {
    displayVal = `${displayVal} ${prop.unit}`
  }

  let timeText = ''
  if (snap.timestamp) {
    const d = new Date(snap.timestamp)
    timeText = d.toLocaleTimeString()
  }

  return {
    hasLive: true,
    displayVal,
    rawValue: snap.value,
    quality: snap.quality !== undefined ? snap.quality : 1,
    timeText
  }
}

const selectedServiceIdentifier = ref('')
const currentService = ref(null)
const dynamicServiceArgs = ref({})

const onPropertySelectChange = (id) => {
  const prop = writableProperties.value.find(p => p.identifier === id)
  if (prop) {
    currentProperty.value = prop
    // 优先采用当前真实采集上来的实时值作为下发初始底数
    const snap = liveSnapshot.value[id]
    if (snap && snap.value !== undefined) {
      if (prop.dataType === 'BOOL') {
        writeFormValue.value = (snap.value > 0 || String(snap.value) === 'true')
      } else if (prop.dataType === 'DOUBLE' || prop.dataType === 'FLOAT' || prop.dataType === 'INT') {
        writeFormValue.value = Number(snap.value)
      } else {
        writeFormValue.value = snap.value
      }
    } else {
      if (prop.dataType === 'BOOL') {
        writeFormValue.value = false
      } else if (prop.dataType === 'ENUM' && prop.options && prop.options.length > 0) {
        writeFormValue.value = prop.options[0].value
      } else {
        writeFormValue.value = prop.default !== undefined ? prop.default : (prop.min ?? 0)
      }
    }
  } else {
    currentProperty.value = null
    writeFormValue.value = null
  }
}

const isFloatingType = (dt) => {
  if (!dt) return false
  const t = String(dt).toUpperCase()
  return t === 'DOUBLE' || t === 'FLOAT'
}

const setPresetValue = (pct) => {
  if (currentProperty.value) {
    const min = currentProperty.value.min ?? 0
    const max = currentProperty.value.max ?? 100
    const val = min + (max - min) * pct
    writeFormValue.value = isFloatingType(currentProperty.value.dataType) ? parseFloat(val.toFixed(4)) : Math.round(val)
  }
}

const onServiceSelectChange = (id) => {
  const srv = serviceList.value.find(s => s.identifier === id)
  if (srv) {
    currentService.value = srv
    const args = {}
    if (Array.isArray(srv.params)) {
      srv.params.forEach(p => {
        if (p.dataType === 'BOOL') {
          args[p.identifier] = p.default !== undefined && p.default !== null ? Boolean(p.default) : false
        } else if (p.dataType === 'INT' || p.dataType === 'DOUBLE' || p.dataType === 'FLOAT') {
          args[p.identifier] = p.default !== undefined && p.default !== null && p.default !== '' ? Number(p.default) : (p.min ?? 0)
        } else {
          args[p.identifier] = p.default !== undefined && p.default !== null ? p.default : ''
        }
      })
    }
    dynamicServiceArgs.value = args
  } else {
    currentService.value = null
    dynamicServiceArgs.value = {}
  }
}

const generateServicePayloadDraft = () => {
  if (!currentService.value || !selectedDev.value) return ''
  return JSON.stringify({
    messageId: 'msg_' + Date.now(),
    modelKey: selectedDev.value.modelKey,
    deviceId: selectedDev.value.deviceId,
    driverCode: selectedDev.value.driverCode,
    serviceCode: currentService.value.identifier,
    params: dynamicServiceArgs.value
  }, null, 2)
}

const openControlPanel = async (row) => {
  if (!hasPerm('device:control')) {
    ElMessage.warning('当前账号无设备反控与点位调试权限 (device:control)')
    return
  }
  selectedDev.value = row
  writeResult.value = ''
  activeControlTab.value = 'properties'
  writableProperties.value = []
  serviceList.value = []
  eventList.value = []
  currentProperty.value = null
  currentService.value = null

  // 1. 同步调取目标设备底层最新点位快照
  if (row && row.deviceId) {
    fetchDeviceLiveSnapshot(row.deviceId)
  }

  // 2. 调取物模型定义与设备实际物理点位映射
  if (row && row.modelKey) {
    try {
      const [res, mapRes] = await Promise.all([
        getThingModel(row.modelKey),
        getPointAddressMappings(row.deviceId)
      ])
      const tm = (res && res.properties) ? res : ((res && res.data) ? res.data : res)
      const mappings = (mapRes && mapRes.data) ? mapRes.data : (Array.isArray(mapRes) ? mapRes : [])
      const mapDict = {}
      if (Array.isArray(mappings)) {
        mappings.forEach(m => {
          if (m.pointCode) mapDict[m.pointCode.toLowerCase()] = m
        })
      }

      if (tm) {
        // 1. Properties (All Properties from Thing Model Schema merged with device point mapping)
        const rawProps = Array.isArray(tm.properties) ? tm.properties : []
        writableProperties.value = rawProps.map(p => {
          const ident = p.pointCode || p.identifier
          const mapped = mapDict[ident.toLowerCase()]
          const effectiveDataType = (mapped && mapped.dataType) ? mapped.dataType.toUpperCase() : (p.dataType || 'FLOAT').toUpperCase()
          const pName = p.pointName || p.name || ident
          const mode = (mapped && mapped.rwMode ? mapped.rwMode : (p.accessMode || p.rwMode || 'R')).toUpperCase()
          const isWritable = mode === 'RW' || mode === 'W' || mode === 'WRITE'
          let defaultVal = p.min ?? 0
          if (effectiveDataType === 'BOOL') {
            defaultVal = false
          } else if (effectiveDataType === 'ENUM' && Array.isArray(p.enumOptions) && p.enumOptions.length > 0) {
            defaultVal = p.enumOptions[0].value
          }
          return {
            identifier: ident,
            name: pName,
            accessMode: mode,
            isWritable: isWritable,
            dataType: effectiveDataType,
            physicalAddress: mapped ? mapped.physicalAddress : (p.address || ''),
            unit: p.unit || '',
            min: p.min ?? 0,
            max: p.max ?? 500,
            default: defaultVal,
            options: (p.enumOptions || []).map(o => ({ label: o.label || o.name || o.text || o.value, value: o.value })),
            description: p.description || `测点 [${ident}]`
          }
        })

        // 2. Services
        const rawServices = Array.isArray(tm.services) ? tm.services : []
        serviceList.value = rawServices.map(s => {
          const sCode = s.serviceCode || s.identifier
          const sName = s.serviceName || s.name || sCode
          return {
            identifier: sCode,
            name: sName,
            description: s.description || `物模型控制服务方法 [${sCode}]`,
            params: (s.inputParams || s.params || []).map(ip => {
              const defVal = (ip.defaultValue !== undefined && ip.defaultValue !== null && ip.defaultValue !== '')
                ? ip.defaultValue
                : ((ip.default !== undefined && ip.default !== null && ip.default !== '') ? ip.default : (ip.min ?? 0))
              return {
                identifier: ip.pointCode || ip.identifier,
                name: ip.pointName || ip.name || ip.pointCode,
                dataType: (ip.dataType || 'INT').toUpperCase(),
                min: ip.min ?? 0,
                max: ip.max ?? 65535,
                default: defVal
              }
            })
          }
        })

        // 3. Events
        const rawEvents = Array.isArray(tm.events) ? tm.events : []
        eventList.value = rawEvents.map(e => {
          const eCode = e.eventCode || e.identifier
          const eName = e.eventName || e.name || eCode
          return {
            identifier: eCode,
            name: eName,
            eventType: e.eventType || 'ALERT',
            eventLevel: e.eventLevel || 'WARN',
            description: e.description || `物模型事件定义 [${eCode}]`
          }
        })
      }
    } catch (e) {
      console.warn('动态调取关联物模型反控配置失败', e)
    }
  }

  if (writableProperties.value.length > 0) {
    selectedPropertyIdentifier.value = writableProperties.value[0].identifier
    onPropertySelectChange(writableProperties.value[0].identifier)
  }
  if (serviceList.value.length > 0) {
    selectedServiceIdentifier.value = serviceList.value[0].identifier
    onServiceSelectChange(serviceList.value[0].identifier)
  }

  controlDrawerVisible.value = true
}

const handleWriteProperty = async () => {
  if (!hasPerm('device:control')) {
    ElMessage.warning('当前账号无设备点位写入反控权限 (device:control)')
    return
  }
  if (!currentProperty.value || !selectedDev.value) return
  if (writeFormValue.value === null || writeFormValue.value === undefined || writeFormValue.value === '') {
    ElMessage.warning('请先设置要下发写入的目标数值')
    return
  }

  const deviceId = selectedDev.value.deviceId
  const pointCode = currentProperty.value.identifier
  const pointName = currentProperty.value.name || pointCode
  const val = writeFormValue.value
  const unit = currentProperty.value.unit || ''

  let displayVal = val
  if (currentProperty.value.dataType === 'BOOL') {
    displayVal = val ? '开启 (TRUE / 1)' : '关闭 (FALSE / 0)'
  }

  try {
    await ElMessageBox.confirm(
      `<div style="line-height: 1.8; color: #e2e8f0; font-size: 13px;">
        <p style="margin-bottom: 8px;">确定要向物理设备下发点位写入控制指令吗？</p>
        <div style="background: #0f172a; border: 1px solid #334155; padding: 12px 14px; border-radius: 6px; margin: 8px 0;">
          <div style="margin-bottom: 5px;">设备标识：<strong style="color: #f8fafc;">${selectedDev.value.deviceName}</strong> <code style="color: #38bdf8; background: #1e293b; padding: 2px 6px; border-radius: 3px; font-family: monospace;">${deviceId}</code></div>
          <div style="margin-bottom: 5px;">目标测点：<strong style="color: #f8fafc;">${pointName}</strong> <code style="color: #38bdf8; background: #1e293b; padding: 2px 6px; border-radius: 3px; font-family: monospace;">${pointCode}</code></div>
          <div>写入设定值：<strong style="color: #10b981; font-size: 15px; font-family: monospace;">${displayVal}</strong> <span style="color: #94a3b8;">${unit}</span></div>
        </div>
        <div style="color: #fb923c; font-size: 12px; margin-top: 8px;">⚠️ 提示：写入指令将实时下发到底层物理设备/PLC，可能改变设备运行状态，请谨慎操作。</div>
      </div>`,
      '点位写入指令确认',
      {
        confirmButtonText: '确认下发',
        cancelButtonText: '取消',
        type: 'warning',
        dangerouslyUseHTMLString: true,
        confirmButtonClass: 'el-button--danger'
      }
    )
  } catch {
    return
  }

  try {
    const dataType = currentProperty.value.dataType || 'FLOAT'
    const byteOrder = selectedDev.value.connectionParams?.byteOrder || 'CDAB'
    const payload = {
      messageId: 'msg_' + Date.now(),
      modelKey: selectedDev.value.modelKey,
      deviceId: deviceId,
      driverCode: selectedDev.value.driverCode,
      serviceCode: 'writeProperty',
      pointCode: pointCode,
      value: val,
      dataType: dataType,
      byteOrder: byteOrder,
      params: {
        [pointCode]: val
      }
    }

    let res
    try {
      res = await controlDevicePoint(deviceId, pointCode, val, dataType)
    } catch (e) {
      // 降级使用协议网关标准指令接口
      res = await sendControlCommand(payload)
    }

    const resMsg = (res && res.message) ? res.message : (res && res.msg ? res.msg : (typeof res === 'string' ? res : JSON.stringify(res)))
    writeResult.value = resMsg || `点位 [${currentProperty.value.name}] 目标值 ${val} 下发成功`
    ElMessage.success(`属性点位 [${currentProperty.value.name}] 写入指令已发送`)

    // 下发成功后，延时 600ms 自动回读刷新最新实时值
    setTimeout(() => {
      fetchDeviceLiveSnapshot(deviceId)
    }, 600)
  } catch (err) {
    writeResult.value = '写入异常: ' + (err.message || err)
    ElMessage.error('指令下发失败: ' + (err.message || err))
  }
}

const handleSendService = async () => {
  if (!hasPerm('device:control')) {
    ElMessage.warning('当前账号无设备服务指令下发反控权限 (device:control)')
    return
  }
  if (!currentService.value || !selectedDev.value) return

  const deviceId = selectedDev.value.deviceId
  const serviceCode = currentService.value.identifier
  const serviceName = currentService.value.name || serviceCode
  const params = { ...dynamicServiceArgs.value }

  // 构造参数展示表格
  let paramsTableHtml = ''
  if (currentService.value.params && currentService.value.params.length > 0) {
    const rowsHtml = currentService.value.params.map(p => {
      let val = params[p.identifier]
      let valText = val !== undefined && val !== null && val !== '' ? String(val) : '<span style="color: #64748b;">(未填写/空)</span>'
      if (p.dataType === 'BOOL') {
        valText = val ? '<span style="color: #10b981;">TRUE (开启)</span>' : '<span style="color: #ef4444;">FALSE (关闭)</span>'
      }
      return `<tr style="border-bottom: 1px solid #334155;">
        <td style="padding: 6px 10px; color: #f8fafc; font-weight: 500;">${p.name || p.identifier}</td>
        <td style="padding: 6px 10px; color: #38bdf8; font-family: monospace;">${p.identifier}</td>
        <td style="padding: 6px 10px; color: #10b981; font-family: monospace; font-weight: bold;">${valText}</td>
      </tr>`
    }).join('')

    paramsTableHtml = `
      <div style="margin-top: 8px;">
        <div style="color: #94a3b8; font-size: 12px; margin-bottom: 5px;">调用参数列表：</div>
        <table style="width: 100%; border-collapse: collapse; background: #0f172a; border: 1px solid #334155; border-radius: 6px; font-size: 12px; overflow: hidden;">
          <thead>
            <tr style="background: #1e293b; color: #cbd5e1; text-align: left; border-bottom: 1px solid #334155;">
              <th style="padding: 6px 10px;">参数名称</th>
              <th style="padding: 6px 10px;">参数标识</th>
              <th style="padding: 6px 10px;">设定数值</th>
            </tr>
          </thead>
          <tbody>
            ${rowsHtml}
          </tbody>
        </table>
      </div>
    `
  } else {
    paramsTableHtml = `
      <div style="margin-top: 8px; padding: 8px 12px; background: #0f172a; border: 1px solid #334155; border-radius: 6px; color: #94a3b8; font-size: 12px;">
        调用参数：<span style="color: #38bdf8;">无入参（无参数方法）</span>
      </div>
    `
  }

  const jsonStr = Object.keys(params).length > 0 ? JSON.stringify(params, null, 2) : '{}'

  try {
    await ElMessageBox.confirm(
      `<div style="line-height: 1.8; color: #e2e8f0; font-size: 13px;">
        <p style="margin-bottom: 8px;">确定要向物理设备下发执行该控制服务吗？</p>
        <div style="background: #0f172a; border: 1px solid #334155; padding: 12px 14px; border-radius: 6px; margin: 8px 0;">
          <div style="margin-bottom: 5px;">设备标识：<strong style="color: #f8fafc;">${selectedDev.value.deviceName}</strong> <code style="color: #38bdf8; background: #1e293b; padding: 2px 6px; border-radius: 3px; font-family: monospace;">${deviceId}</code></div>
          <div>服务方法：<strong style="color: #f8fafc;">${serviceName}</strong> <code style="color: #ec4899; background: #1e293b; padding: 2px 6px; border-radius: 3px; font-family: monospace;">${serviceCode}</code></div>
        </div>
        ${paramsTableHtml}
        <details style="margin-top: 8px;">
          <summary style="font-size: 12px; color: #64748b; cursor: pointer; user-select: none;">查看原始 JSON 载荷 (Payload)</summary>
          <pre style="margin: 6px 0 0; padding: 8px; background: #060911; border: 1px solid #1e293b; border-radius: 4px; font-family: monospace; font-size: 12px; color: #38bdf8; max-height: 100px; overflow-y: auto;">${jsonStr}</pre>
        </details>
        <div style="color: #fb923c; font-size: 12px; margin-top: 10px;">⚠️ 提示：执行服务将触发设备动作或状态变更，请确认参数准确无误后执行。</div>
      </div>`,
      '服务指令下发确认',
      {
        confirmButtonText: '确认下发',
        cancelButtonText: '取消',
        type: 'warning',
        dangerouslyUseHTMLString: true,
        confirmButtonClass: 'el-button--danger'
      }
    )
  } catch {
    return
  }

  try {
    const payload = {
      messageId: 'msg_' + Date.now(),
      modelKey: selectedDev.value.modelKey,
      deviceId: deviceId,
      driverCode: selectedDev.value.driverCode,
      serviceCode: serviceCode,
      params: params
    }

    let res
    try {
      res = await invokeDeviceService(deviceId, serviceCode, params)
    } catch (e) {
      res = await sendControlCommand(payload)
    }

    const resMsg = (res && res.message) ? res.message : (res && res.msg ? res.msg : (typeof res === 'string' ? res : JSON.stringify(res)))
    writeResult.value = resMsg || `控制服务 [${currentService.value.name}] 指令已成功下发`
    ElMessage.success(`控制服务 [${currentService.value.name}] 执行指令已发送`)

    setTimeout(() => {
      fetchDeviceLiveSnapshot(deviceId)
    }, 800)
  } catch (err) {
    writeResult.value = '服务下发异常: ' + (err.message || err)
    ElMessage.error('服务下发失败: ' + (err.message || err))
  }
}

// Device Point Address Spec Mapping Drawer Handlers
const addressMappingDrawerVisible = ref(false)
const selectedDevForAddress = ref(null)
const devicePointAddressList = ref([])

const getAddressPlaceholder = (driverCode) => {
  const d = driverList.value.find(item => item.driverCode === driverCode)
  if (d && d.addressRule && d.addressRule.placeholder) {
    return d.addressRule.placeholder
  }
  if (driverCode === 'modbus-tcp') return '例: 40001 (保持寄存器) 或 00001 (线圈)'
  if (driverCode === 's7-driver') return '例: DB1.DBD0 (Float/DInt) 或 DB1.DBW2 (Int)'
  if (driverCode === 'opc-ua') return '例: ns=2;s=Device1.Temperature'
  return '物理寄存器地址'
}

const openAddressMappingDrawer = async (row) => {
  selectedDevForAddress.value = row
  devicePointAddressList.value = []
  try {
    const [res, tmRes] = await Promise.all([
      getPointAddressMappings(row.deviceId),
      getThingModel(row.modelKey)
    ])
    const list = (res && res.data) ? res.data : res
    const tm = (tmRes && tmRes.properties) ? tmRes : ((tmRes && tmRes.data) ? tmRes.data : tmRes)
    const tmProps = (tm && Array.isArray(tm.properties)) ? tm.properties : []

    if (Array.isArray(list) && list.length > 0) {
      // 保持最新物模型的 dataType，如果物模型定义已变更，以物模型最新类型优先同步
      devicePointAddressList.value = list.map(m => {
        const prop = tmProps.find(p => p.pointCode === m.pointCode) || {}
        return {
          ...m,
          dataType: m.dataType || prop.dataType || 'FLOAT',
          pointName: prop.pointName || m.pointName,
          rwMode: m.rwMode || prop.accessMode || m.accessMode || 'R'
        }
      })
    } else if (tmProps.length > 0) {
      // 动态从绑定的物模型获取属性点位配置
      devicePointAddressList.value = tmProps.map(p => ({
        deviceId: row.deviceId,
        modelKey: row.modelKey,
        pointCode: p.pointCode || p.identifier,
        pointName: p.pointName || p.name || p.pointCode,
        dataType: p.dataType || 'FLOAT',
        rwMode: p.accessMode || p.rwMode || 'R',
        physicalAddress: p.address || '',
        scale: 1.0,
        offset: 0.0
      }))
    }
  } catch (err) {
    console.warn('获取点位物理地址映射失败', err)
  }
  addressMappingDrawerVisible.value = true
}

const autoFillDefaultAddresses = async () => {
  if (!selectedDevForAddress.value) return
  try {
    const res = await suggestPointAddressMappings(selectedDevForAddress.value.deviceId)
    const list = (res && res.data) ? res.data : res
    if (Array.isArray(list) && list.length > 0) {
      if (devicePointAddressList.value.length === 0) {
        devicePointAddressList.value = list
      } else {
        // 智能填充：保留表格已有的字段配置，将后端驱动元数据推算的物理地址填充进去
        devicePointAddressList.value = devicePointAddressList.value.map(item => {
          const match = list.find(s => s.pointCode === item.pointCode)
          return {
            ...item,
            physicalAddress: match?.physicalAddress || item.physicalAddress
          }
        })
      }
      ElMessage.success('已根据后端驱动元数据规则自动填充示例物理地址')
    }
  } catch (err) {
    ElMessage.error('获取后端推荐映射地址失败: ' + (err.message || ''))
  }
}

const saveAddressMappings = async () => {
  if (!selectedDevForAddress.value) return
  try {
    await savePointAddressMappings(selectedDevForAddress.value.deviceId, devicePointAddressList.value)
    ElMessage.success(`设备 [${selectedDevForAddress.value.deviceName}] 物理点位硬件地址映射保存成功`)
    addressMappingDrawerVisible.value = false
  } catch (err) {
    ElMessage.error('保存点位映射配置失败')
  }
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

.filter-card {
  padding: 16px 20px 0 20px;
}

.driver-code {
  background: var(--iot-bg-tertiary);
  padding: 2px 8px;
  border-radius: 4px;
  font-family: monospace;
}

.clickable-tag {
  cursor: pointer;
}

.conn-spec {
  font-family: monospace;
  color: var(--iot-color-cyan);
}

/* Device Cards Grid Layout */
.device-cards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 20px;
}

.device-card {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 18px;
  gap: 14px;
  background: linear-gradient(145deg, rgba(30, 41, 59, 0.75) 0%, rgba(15, 23, 42, 0.9) 100%);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-top: 3px solid #10b981;
  border-radius: 10px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  backdrop-filter: blur(8px);
}

.device-card:hover {
  border-color: #10b981;
  border-top-color: #34d399;
  background: linear-gradient(145deg, rgba(30, 41, 59, 0.95) 0%, rgba(15, 23, 42, 0.98) 100%);
  box-shadow: 0 8px 28px rgba(16, 185, 129, 0.2);
  transform: translateY(-3px);
}

.device-card.is-disabled {
  opacity: 0.65;
  filter: grayscale(0.2);
  border-top-color: #64748b;
  background: linear-gradient(145deg, rgba(15, 23, 42, 0.6) 0%, rgba(15, 23, 42, 0.8) 100%);
}

.device-card-header {
  display: flex;
  flex-direction: column;
  gap: 10px;
  border-bottom: 1px solid var(--iot-border-color);
  padding-bottom: 12px;
}

.card-meta-top-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.id-status-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.dev-name-full-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.dev-card-name {
  font-size: 1.05rem;
  font-weight: 700;
  color: var(--iot-text-primary);
  line-height: 1.4;
  word-break: break-word;
}

.dev-card-id {
  background: var(--iot-bg-tertiary);
  color: var(--iot-color-cyan);
  padding: 2px 8px;
  border-radius: 4px;
  font-family: monospace;
  font-size: 0.8rem;
}

.live-status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #64748b;
}

.live-status-dot.online {
  background: #10b981;
  box-shadow: 0 0 8px #10b981;
}

.acq-switch-box {
  display: flex;
  align-items: center;
  gap: 8px;
}

.acq-lbl {
  font-size: 0.8rem;
  color: var(--iot-text-secondary);
}

.device-card-body {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.specs-grid {
  display: flex;
  flex-direction: column;
  gap: 8px;
  font-size: 0.85rem;
  background: rgba(15, 23, 42, 0.4);
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 6px;
  padding: 12px;
}

.meta-spec-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.spec-lbl {
  color: var(--iot-text-secondary);
  font-size: 0.82rem;
}

.conn-spec-text {
  font-family: monospace;
  color: var(--iot-color-cyan);
  font-size: 0.82rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.telemetry-preview-box {
  background: rgba(15, 23, 42, 0.6);
  border: 1px solid var(--iot-border-color);
  border-radius: 6px;
  padding: 10px 14px;
}

.preview-title {
  font-size: 0.78rem;
  color: var(--iot-text-secondary);
  margin-bottom: 6px;
  font-weight: 500;
}

.metrics-row {
  display: flex;
  align-items: center;
  justify-content: space-around;
}

.m-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.m-name {
  font-size: 0.75rem;
  color: #64748b;
}

.m-val {
  font-size: 0.95rem;
  font-weight: 700;
  font-family: monospace;
}

.m-divider {
  width: 1px;
  height: 20px;
  background: rgba(255, 255, 255, 0.08);
}

.device-card-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  border-top: 1px solid var(--iot-border-color);
  padding-top: 12px;
}

.margin-top {
  margin-top: 12px;
}

.margin-top-small {
  margin-top: 8px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}

/* Control Panel Drawer Hero Styling */
.control-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.hero-banner-card {
  background: linear-gradient(135deg, rgba(15, 23, 42, 0.95), rgba(30, 41, 59, 0.85));
  border: 1px solid rgba(6, 182, 212, 0.3);
  padding: 16px 20px;
  border-radius: 10px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.banner-top-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dev-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.live-pulse-dot {
  width: 10px;
  height: 10px;
  background-color: #10b981;
  border-radius: 50%;
  box-shadow: 0 0 10px #10b981;
  animation: pulse-glow 1.8s infinite;
}

@keyframes pulse-glow {
  0% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(16, 185, 129, 0.7); }
  70% { transform: scale(1.1); box-shadow: 0 0 0 8px rgba(16, 185, 129, 0); }
  100% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(16, 185, 129, 0); }
}

.dev-name {
  font-size: 1.15rem;
  font-weight: 700;
  color: #f8fafc;
}

.dev-code {
  background: rgba(6, 182, 212, 0.2);
  color: var(--iot-color-cyan);
  padding: 2px 8px;
  border-radius: 4px;
  font-family: monospace;
  font-size: 0.85rem;
}

.live-status-tag {
  display: flex;
  align-items: center;
  gap: 6px;
  background: rgba(16, 185, 129, 0.15);
  border: 1px solid rgba(16, 185, 129, 0.4);
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 600;
  color: #34d399;
}

.banner-meta-row {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  font-size: 0.85rem;
}

.meta-label { color: #94a3b8; margin-right: 4px; }
.text-cyan { color: var(--iot-color-cyan); font-weight: 600; }
.text-green { color: #34d399; font-weight: 600; }
.text-mono { font-family: monospace; color: #cbd5e1; }

/* Custom Segmented Tab Bar */
.segment-tab-bar {
  display: flex;
  background: rgba(15, 23, 42, 0.8);
  border: 1px solid var(--iot-border-color);
  padding: 4px;
  border-radius: 8px;
  gap: 4px;
}

.tab-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 14px;
  border-radius: 6px;
  font-size: 0.88rem;
  font-weight: 500;
  color: #94a3b8;
  cursor: pointer;
  transition: all 0.2s ease;
}

.tab-btn:hover {
  background: rgba(255, 255, 255, 0.05);
  color: #f1f5f9;
}

.tab-btn.active {
  background: var(--iot-color-cyan);
  color: #0f172a;
  font-weight: 700;
  box-shadow: 0 2px 10px rgba(6, 182, 212, 0.3);
}

.panel-card-container {
  background: var(--iot-bg-secondary);
  border: 1px solid var(--iot-border-color);
  border-radius: 8px;
  padding: 18px;
}

/* Property & Service Cards */
.property-preview-card {
  background: rgba(15, 23, 42, 0.7);
  border: 1px dashed rgba(6, 182, 212, 0.4);
  border-radius: 8px;
  padding: 16px;
}

.preview-header {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  align-items: flex-start;
  gap: 10px 14px;
}

.prop-title-group {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px 10px;
  flex: 1 1 auto;
  min-width: 200px;
}

.prop-title {
  font-size: 1.05rem;
  font-weight: 600;
  color: #f8fafc;
  line-height: 1.4;
  word-break: break-word;
}

.prop-ident {
  background: rgba(255, 255, 255, 0.08);
  padding: 2px 6px;
  border-radius: 4px;
  font-family: monospace;
  font-size: 0.8rem;
  color: #38bdf8;
  flex-shrink: 0;
  white-space: nowrap;
}

.prop-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  align-items: center;
}
.prop-desc { font-size: 0.84rem; color: #94a3b8; margin-top: 8px; line-height: 1.5; }

.metric-comparison-row {
  display: flex;
  align-items: center;
  justify-content: space-around;
  background: rgba(0, 0, 0, 0.3);
  border-radius: 6px;
  padding: 12px;
  margin-top: 14px;
}

.metric-box { text-align: center; }
.metric-lbl-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin-bottom: 4px;
}
.metric-lbl { font-size: 0.78rem; color: #64748b; }
.metric-val { font-size: 1.25rem; font-weight: 700; font-family: monospace; }
.metric-live-sub {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 5px;
}
.quality-badge {
  font-size: 0.72rem;
  padding: 1px 6px;
  border-radius: 4px;
  font-weight: 500;
}
.quality-badge.good {
  background: rgba(16, 185, 129, 0.15);
  color: #34d399;
  border: 1px solid rgba(16, 185, 129, 0.3);
}
.quality-badge.bad {
  background: rgba(239, 68, 68, 0.15);
  color: #f87171;
  border: 1px solid rgba(239, 68, 68, 0.3);
}
.time-text {
  font-size: 0.72rem;
  color: #94a3b8;
  font-family: monospace;
}
.metric-divider { width: 1px; height: 30px; background: rgba(255, 255, 255, 0.1); }

.input-control-card {
  background: rgba(15, 23, 42, 0.5);
  border: 1px solid var(--iot-border-color);
  border-radius: 8px;
  padding: 16px;
}

.control-card-header {
  font-size: 0.88rem;
  font-weight: 600;
  color: var(--iot-color-cyan);
  margin-bottom: 8px;
}

.slider-wrapper { padding: 0 10px; }

.input-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.quick-preset-btns {
  display: flex;
  gap: 4px;
}

/* Switch & Enum Cards */
.switch-card-wrapper {
  display: flex;
  gap: 12px;
}

.switch-state-box {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 14px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid var(--iot-border-color);
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.2s ease;
}

.switch-state-box:hover { background: rgba(255, 255, 255, 0.08); }

.switch-state-box.active {
  background: rgba(16, 185, 129, 0.15);
  border-color: #10b981;
  color: #34d399;
}

.state-dot { width: 8px; height: 8px; border-radius: 50%; }
.state-dot.green { background: #10b981; }
.state-dot.red { background: #ef4444; }

.enum-pills-wrapper {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.enum-pill-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid var(--iot-border-color);
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.enum-pill-card:hover { background: rgba(255, 255, 255, 0.08); }

.enum-pill-card.active {
  background: rgba(6, 182, 212, 0.15);
  border-color: var(--iot-color-cyan);
}

.enum-pill-card.active .pill-title { color: var(--iot-color-cyan); font-weight: 700; }
.pill-title { font-size: 0.9rem; color: #cbd5e1; }
.pill-val { font-size: 0.8rem; font-family: monospace; color: #64748b; }

.params-form-grid {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.param-field-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.param-label {
  font-size: 0.85rem;
  color: #cbd5e1;
}

.payload-draft-card {
  background: #040711;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 6px;
  padding: 12px;
}

.draft-title { font-size: 0.78rem; color: #64748b; margin-bottom: 6px; }
.draft-code { font-family: monospace; font-size: 0.8rem; color: #a5f3fc; margin: 0; white-space: pre-wrap; }

.cyber-btn {
  font-weight: 700;
  letter-spacing: 0.5px;
}

.cyber-btn.full-width { width: 100%; }

/* Console Terminal Box */
.terminal-console-box {
  background: #040711;
  border: 1px solid rgba(6, 182, 212, 0.35);
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.5);
}

.console-header {
  background: #0f172a;
  padding: 8px 14px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.window-dots { display: flex; align-items: center; gap: 8px; }
.dot { width: 10px; height: 10px; border-radius: 50%; }
.dot.red { background: #ef4444; }
.dot.yellow { background: #f59e0b; }
.dot.green { background: #10b981; }

.console-title { font-size: 0.8rem; font-family: monospace; color: #94a3b8; margin-left: 6px; }
.console-body { padding: 14px; }
.console-text { font-family: 'Fira Code', monospace; font-size: 0.82rem; color: #38bdf8; margin: 0; white-space: pre-wrap; word-break: break-all; line-height: 1.6; }

/* Protocol Guide Banner & Address Form Toolbar */
.protocol-guide-banner {
  background: rgba(6, 182, 212, 0.08);
  border: 1px solid rgba(6, 182, 212, 0.3);
  border-radius: 6px;
  padding: 10px 14px;
}

.guide-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--iot-color-cyan);
}

.guide-content {
  font-size: 0.82rem;
  color: #94a3b8;
  margin-top: 4px;
  line-height: 1.4;
}

.guide-content code {
  background: rgba(255, 255, 255, 0.08);
  padding: 2px 6px;
  border-radius: 4px;
  color: #38bdf8;
  font-family: monospace;
}

.toolbar-actions {
  display: flex;
  gap: 8px;
}

.scale-offset-inputs {
  display: flex;
  gap: 6px;
  align-items: center;
}

.formula-box {
  background: rgba(6, 182, 212, 0.15);
  color: var(--iot-color-cyan);
  padding: 2px 8px;
  border-radius: 4px;
  font-family: monospace;
  font-weight: 600;
}

.col-header-with-tip {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* Device Modal Dialog Styling */
.device-dialog-form :deep(.el-form-item__label) {
  white-space: nowrap !important;
  font-weight: 500;
  color: var(--iot-text-primary);
}

.device-dialog-form :deep(.el-form-item) {
  margin-bottom: 18px;
}

.input-with-unit {
  display: flex;
  align-items: center;
  gap: 10px;
}

.unit-text {
  font-size: 0.85rem;
  color: var(--iot-text-secondary);
}
</style>
