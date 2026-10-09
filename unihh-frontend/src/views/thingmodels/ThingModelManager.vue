<!--
  Author: deversun
  Website: https://unihh.com
  License: Apache License 2.0
-->
<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="iot-header-title">
        <SvgIcon name="database" :size="24" color="#06b6d4" />
        物模型模板库 (Thing Model Schema Repository)
      </h2>
      <el-button v-if="hasPermission(['thingmodel:add', 'thingmodel:edit'])" type="primary" @click="openCreateModelModal">
        <SvgIcon name="send" :size="14" />
        新建物模型模板
      </el-button>
    </div>

    <!-- Filter Bar -->
    <div class="iot-card filter-card">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="模板编码 / 名称">
          <el-input v-model="queryForm.keyword" placeholder="搜索物模型编码或名称" clearable />
        </el-form-item>
        <el-form-item label="工业分类">
          <el-select v-model="queryForm.category" placeholder="全部分类" clearable style="width: 160px">
            <el-option label="旋转机械/变频电机" value="ROTATING_EQUIP" />
            <el-option label="PLC 控制器/自动化" value="PLC_AUTOMATION" />
            <el-option label="工业以太网网关" value="GATEWAY" />
            <el-option label="环境与能源监测" value="ENV_MONITOR" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- Thing Model Cards Grid -->
    <div v-if="filteredModels.length > 0" class="model-cards-grid">
      <div v-for="tmpl in paginatedModels" :key="tmpl.modelCode" class="iot-card model-card">
        <div class="card-top-bar">
          <div class="model-title-group">
            <span class="model-name">{{ tmpl.modelName }}</span>
            <code class="model-code">{{ tmpl.modelCode }}</code>
          </div>
          <el-tag size="small" type="info">{{ getCategoryLabel(tmpl.category) }}</el-tag>
        </div>

        <div class="model-desc">{{ tmpl.description }}</div>

        <!-- Metric Badges Row -->
        <div class="metrics-badges-row">
          <div class="metric-badge">
            <span class="m-val text-cyan">{{ tmpl.properties ? tmpl.properties.length : 0 }}</span>
            <span class="m-lbl">属性点位</span>
          </div>
          <div class="metric-divider"></div>
          <div class="metric-badge">
            <span class="m-val text-green">{{ tmpl.services ? tmpl.services.length : 0 }}</span>
            <span class="m-lbl">控制服务</span>
          </div>
          <div class="metric-divider"></div>
          <div class="metric-badge">
            <span class="m-val text-amber">{{ tmpl.events ? tmpl.events.length : 0 }}</span>
            <span class="m-lbl">告警规则</span>
          </div>
          <div class="metric-divider"></div>
          <div
            class="metric-badge"
            :class="{ 'is-clickable': (tmpl.deviceCount || 0) > 0 }"
            :title="(tmpl.deviceCount || 0) > 0 ? `点击跳转排查关联的 ${tmpl.deviceCount} 台设备` : '暂无设备关联'"
            @click="navigateToDeviceList(tmpl)"
          >
            <span class="m-val" :class="(tmpl.deviceCount || 0) > 0 ? 'text-indigo' : 'text-muted'">
              {{ tmpl.deviceCount || 0 }}
            </span>
            <span class="m-lbl link-lbl">
              引用设备
              <span v-if="(tmpl.deviceCount || 0) > 0" class="click-hint">↗</span>
            </span>
          </div>
        </div>

        <!-- Card Actions Footer -->
        <div class="card-actions-footer">
          <el-button
            v-if="hasPermission(['thingmodel:design', 'thingmodel:edit'])"
            type="success"
            size="small"
            @click="openSchemaDrawer(tmpl)"
          >
            设计物模型 Schema
          </el-button>
          <el-button v-if="hasPermission('thingmodel:edit')" type="primary" plain size="small" @click="openEditModelModal(tmpl)">
            编辑
          </el-button>
          <el-tooltip
            v-if="hasPermission(['thingmodel:delete', 'thingmodel:edit'])"
            :content="`当前物模型正被 ${tmpl.deviceCount} 台设备引用，禁止删除！请先解绑或删除关联设备。`"
            :disabled="!(tmpl.deviceCount > 0)"
            placement="top"
          >
            <span class="btn-tooltip-wrapper">
              <el-button
                type="danger"
                plain
                size="small"
                :disabled="(tmpl.deviceCount || 0) > 0"
                @click="handleDeleteModel(tmpl)"
              >
                删除
              </el-button>
            </span>
          </el-tooltip>
        </div>
      </div>
    </div>

    <!-- Empty State -->
    <div v-else class="iot-card empty-card margin-top">
      <el-empty description="暂无符合条件的物模型模板" />
    </div>

    <!-- Pagination -->
    <div class="pagination-wrapper margin-top">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[8, 16, 24, 32]"
        :total="filteredModels.length"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- Add/Edit Thing Model Metadata Dialog -->
    <el-dialog v-model="modelMetaDialogVisible" :title="isModelEdit ? '编辑物模型模板' : '新建物模型模板'" width="520px">
      <el-form :model="modelMetaForm" :rules="modelMetaRules" ref="modelMetaFormRef" label-width="130px">
        <el-form-item label="物模型编码 (ID)" prop="modelCode">
          <el-input v-model="modelMetaForm.modelCode" :disabled="isModelEdit" placeholder="不填则系统自动生成 (如 PK_A1B2C3D4)" />
        </el-form-item>
        <el-form-item label="物模型名称" prop="modelName">
          <el-input v-model="modelMetaForm.modelName" placeholder="如 标准工业电机数采物模型" />
        </el-form-item>
        <el-form-item label="工业分类" prop="category">
          <el-select v-model="modelMetaForm.category" style="width: 100%">
            <el-option label="旋转机械/变频电机" value="ROTATING_EQUIP" />
            <el-option label="PLC 控制器/自动化" value="PLC_AUTOMATION" />
            <el-option label="工业以太网网关" value="GATEWAY" />
            <el-option label="环境与能源监测" value="ENV_MONITOR" />
          </el-select>
        </el-form-item>
        <el-form-item label="模板功能描述">
          <el-input v-model="modelMetaForm.description" type="textarea" :rows="3" placeholder="描述该物模型适用的硬件型号与点位功能" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="modelMetaDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitModelMetaForm">保存物模型模板</el-button>
      </template>
    </el-dialog>

    <!-- FULL THING MODEL SCHEMA DESIGNER DRAWER (物模型 Schema 精细化编辑器抽屉) -->
    <el-drawer
      v-model="schemaDrawerVisible"
      title="物模型 Schema 属性与服务设计器"
      size="1080px"
      destroy-on-close
    >
      <div v-if="selectedModel" class="schema-designer-container">
        <!-- Hero Header -->
        <div class="designer-hero-card">
          <div class="hero-top">
            <span class="hero-name">{{ selectedModel.modelName }}</span>
            <code class="hero-code">{{ selectedModel.modelCode }}</code>
          </div>
          <div class="hero-desc">{{ selectedModel.description }}</div>
        </div>

        <el-tabs type="border-card" class="designer-tabs">
          <!-- TAB 1: PROPERTIES SPECIFICATIONS (属性点位) -->
          <el-tab-pane label="属性点位定义 (Properties)">
            <div class="tab-toolbar">
              <span class="tab-tip">定义设备实时上报数据与可写控制点位 (Telemetry & RW Attributes)</span>
              <div v-if="hasPermission(['thingmodel:design', 'thingmodel:edit'])" class="tab-actions">
                <el-button type="warning" plain size="small" @click="openImportPropModal">
                  <SvgIcon name="send" :size="13" style="margin-right: 4px;" />
                  导入点位
                </el-button>
                <el-button type="primary" size="small" @click="openAddPropModal">
                  <SvgIcon name="plus" :size="13" style="margin-right: 4px;" />
                  新增属性点位
                </el-button>
              </div>
            </div>

            <el-table :data="selectedModel.properties" style="width: 100%" stripe class="margin-top-small" border>
              <el-table-column prop="pointCode" label="点位 Code" width="135">
                <template #default="scope">
                  <code class="text-cyan">{{ scope.row.pointCode }}</code>
                </template>
              </el-table-column>

              <el-table-column prop="pointName" label="点位名称" width="145" show-overflow-tooltip />

              <el-table-column prop="dataType" label="数据类型" width="180">
                <template #default="scope">
                  <div class="data-type-cell">
                    <el-tag size="small" :type="scope.row.dataType === 'ENUM' ? 'warning' : (scope.row.dataType === 'FLOAT' ? 'success' : 'primary')">
                      {{ scope.row.dataType }}
                    </el-tag>
                    <div v-if="scope.row.dataType === 'ENUM' && scope.row.enumOptions && scope.row.enumOptions.length > 0" class="enum-tags-wrapper">
                      <el-tag
                        v-for="opt in scope.row.enumOptions"
                        :key="opt.value"
                        size="small"
                        type="info"
                        effect="plain"
                        class="enum-tag-item"
                      >
                        {{ opt.value }}: {{ opt.label }}
                      </el-tag>
                    </div>
                  </div>
                </template>
              </el-table-column>

              <el-table-column prop="accessMode" label="读写" width="75">
                <template #default="scope">
                  <el-tag size="small" :type="scope.row.accessMode === 'RW' ? 'warning' : 'info'">
                    {{ scope.row.accessMode }}
                  </el-tag>
                </template>
              </el-table-column>

              <el-table-column prop="unit" label="物理单位" width="100" />

              <el-table-column prop="description" label="功能描述与详细说明" min-width="220" show-overflow-tooltip />

              <el-table-column v-if="hasPermission(['thingmodel:design', 'thingmodel:edit'])" label="操作" width="150" fixed="right">
                <template #default="scope">
                  <div class="action-btn-flex">
                    <el-button size="small" type="primary" plain @click="openEditPropModal(scope.row)">编辑</el-button>
                    <el-button size="small" type="danger" plain @click="handleDeleteProp(scope.row)">删除</el-button>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <!-- TAB 2: CONTROL SERVICES & INPUT PARAMS (控制服务与入参) -->
          <el-tab-pane label="控制服务定义 (Services)">
            <div class="tab-toolbar">
              <span class="tab-tip">定义下位机反控服务命令与参数结构 (Command Services & Input Arguments)</span>
              <el-button v-if="hasPermission(['thingmodel:design', 'thingmodel:edit'])" type="success" size="small" @click="openAddServiceModal">
                新增控制服务
              </el-button>
            </div>

            <el-table :data="selectedModel.services" style="width: 100%" stripe class="margin-top-small">
              <el-table-column prop="serviceCode" label="服务 Code" width="160">
                <template #default="scope">
                  <code>{{ scope.row.serviceCode }}</code>
                </template>
              </el-table-column>
              <el-table-column prop="serviceName" label="服务名称" width="150" />
              <el-table-column label="入参定义 (Input Parameters)" min-width="280">
                <template #default="scope">
                  <div v-if="scope.row.inputParams && scope.row.inputParams.length > 0" class="params-badge-wrapper">
                    <el-tag
                      v-for="p in scope.row.inputParams"
                      :key="p.pointCode"
                      type="info"
                      size="small"
                      style="margin-right: 6px; margin-bottom: 4px;"
                    >
                      {{ p.pointName }} (<code>{{ p.pointCode }}</code>: {{ p.dataType }}<span v-if="p.defaultValue !== undefined && p.defaultValue !== null && p.defaultValue !== ''" style="color: #38bdf8; margin-left: 4px;">= {{ p.defaultValue }}</span>)
                    </el-tag>
                  </div>
                  <span v-else class="text-muted">无参控制服务</span>
                </template>
              </el-table-column>
              <el-table-column v-if="hasPermission(['thingmodel:design', 'thingmodel:edit'])" label="操作" width="150" fixed="right">
                <template #default="scope">
                  <div class="action-btn-flex">
                    <el-button size="small" type="primary" plain @click="openEditServiceModal(scope.row)">编辑</el-button>
                    <el-button size="small" type="danger" plain @click="handleDeleteService(scope.row)">删除</el-button>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <!-- TAB 3: ALARM EVENTS DEFINITION (告警事件定义) -->
          <el-tab-pane label="告警事件定义 (Events)">
            <!-- Architecture Tip Banner -->
            <div class="architecture-tip-banner margin-top-small">
              <div class="tip-header">
                <SvgIcon name="info" :size="14" />
                <span>【物模型事件能力与规则引擎架构分工说明】</span>
              </div>
              <div class="tip-body">
                • <strong>物模型 (Thing Model)</strong>：静态声明设备具备上报的<strong>事件能力元数据与 Payload 接口规范</strong>（如硬件自保护告警、故障编码）。<br/>
                • <strong>规则引擎与自动化 (Rule Engine & Automation)</strong>：基于点位阈值计算（如 <code>temperature > 85.0</code>）、告警推送及<strong>跨设备反控服务联动</strong>在 <strong>【规则管理】</strong> 中集中配置。
              </div>
            </div>

            <div class="tab-toolbar margin-top-small">
              <span class="tab-tip">定义设备硬件主动上报之事件编码与数据 Payload 结构</span>
              <el-button v-if="hasPermission(['thingmodel:design', 'thingmodel:edit'])" type="warning" size="small" @click="openAddEventModal">
                新增告警事件
              </el-button>
            </div>

            <el-table :data="selectedModel.events" style="width: 100%" stripe class="margin-top-small" border>
              <el-table-column prop="eventCode" label="事件 Code" width="160">
                <template #default="scope">
                  <code class="text-cyan">{{ scope.row.eventCode }}</code>
                </template>
              </el-table-column>

              <el-table-column prop="eventName" label="事件名称" width="160" show-overflow-tooltip />

              <el-table-column prop="eventType" label="事件类型" width="110">
                <template #default="scope">
                  <el-tag
                    size="small"
                    :type="scope.row.eventType === 'FAULT' ? 'danger' : (scope.row.eventType === 'ALERT' ? 'warning' : 'info')"
                  >
                    {{ scope.row.eventType === 'FAULT' ? '故障事件' : (scope.row.eventType === 'ALERT' ? '告警事件' : '信息事件') }}
                  </el-tag>
                </template>
              </el-table-column>

              <el-table-column prop="eventLevel" label="告警级别" width="110">
                <template #default="scope">
                  <el-tag
                    size="small"
                    :type="scope.row.eventLevel === 'CRITICAL' ? 'danger' : (scope.row.eventLevel === 'WARN' ? 'warning' : 'info')"
                    effect="dark"
                  >
                    {{ scope.row.eventLevel }}
                  </el-tag>
                </template>
              </el-table-column>

              <el-table-column prop="description" label="事件能力描述与 Payload 说明" min-width="220" show-overflow-tooltip />

              <el-table-column v-if="hasPermission(['thingmodel:design', 'thingmodel:edit'])" label="操作" width="150" fixed="right">
                <template #default="scope">
                  <div class="action-btn-flex">
                    <el-button size="small" type="primary" plain @click="openEditEventModal(scope.row)">编辑</el-button>
                    <el-button size="small" type="danger" plain @click="handleDeleteEvent(scope.row)">删除</el-button>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-drawer>

    <!-- Add/Edit Property Dialog -->
    <el-dialog v-model="propDialogVisible" :title="isPropEdit ? '编辑属性点位' : '新增属性点位'" width="500px">
      <el-form :model="propForm" :rules="propRules" ref="propFormRef" label-width="110px">
        <el-form-item label="点位 Code" prop="pointCode">
          <el-input v-model="propForm.pointCode" :disabled="isPropEdit" placeholder="如 temperature" />
        </el-form-item>
        <el-form-item label="点位名称" prop="pointName">
          <el-input v-model="propForm.pointName" placeholder="如 轴承实时温度" />
        </el-form-item>
        <el-form-item label="数据类型" prop="dataType">
          <el-select v-model="propForm.dataType" style="width: 100%" @change="onPropDataTypeChange">
            <el-option label="FLOAT (单精度浮点 32位)" value="FLOAT" />
            <el-option label="DOUBLE (双精度浮点 64位)" value="DOUBLE" />
            <el-option label="INT (整数)" value="INT" />
            <el-option label="STRING (文本)" value="STRING" />
            <el-option label="BOOL (布尔值 0/1)" value="BOOL" />
            <el-option label="ENUM (枚举模式 - 键值映射)" value="ENUM" />
          </el-select>
        </el-form-item>

        <!-- ENUM Options Configuration Card -->
        <template v-if="propForm.dataType === 'ENUM'">
          <el-form-item label="枚举映射定义">
            <div class="enum-options-builder">
              <div class="builder-header">
                <span class="builder-title">
                  <SvgIcon name="setting" :size="13" />
                  键值与标签映射
                </span>
                <el-button size="small" type="primary" plain @click="addEnumOption">
                  + 添加项
                </el-button>
              </div>
              <div v-for="(opt, idx) in propForm.enumOptions" :key="idx" class="enum-opt-row">
                <el-input-number
                  v-model="opt.value"
                  placeholder="键值"
                  size="small"
                  style="width: 95px"
                  controls-position="right"
                />
                <el-input
                  v-model="opt.label"
                  placeholder="标签描述 (如 ECO模式)"
                  size="small"
                  style="flex: 1"
                />
                <el-button type="danger" circle size="small" @click="removeEnumOption(idx)">✕</el-button>
              </div>
              <div v-if="!propForm.enumOptions || propForm.enumOptions.length === 0" class="empty-enum-tip">
                请点击“+ 添加项”配置枚举值
              </div>
            </div>
          </el-form-item>
        </template>

        <el-form-item label="读写模式" prop="accessMode">
          <el-radio-group v-model="propForm.accessMode">
            <el-radio label="R">R (只读上报)</el-radio>
            <el-radio label="RW">RW (可读可写反控)</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="采样触发模式" prop="triggerMode">
          <el-radio-group v-model="propForm.triggerMode">
            <el-radio label="PERIODIC">PERIODIC (周期轮询)</el-radio>
            <el-radio label="COV">COV (变化触发)</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="propForm.triggerMode === 'PERIODIC'" label="采样基频倍数" prop="sampleFactor">
          <el-input-number v-model="propForm.sampleFactor" :min="1" :max="1000" style="width: 100%" />
          <span style="font-size: 0.8rem; color: #94a3b8; display: block; margin-top: 4px;">
            计算公式: 实际采集间隔 = 设备基频 × 采样倍数 (如基频 1000ms, 5x 即为 5 秒)
          </span>
        </el-form-item>
        <el-form-item label="物理单位" prop="unit">
          <el-input v-model="propForm.unit" placeholder="如 ℃, kPa, RPM, %" />
        </el-form-item>
        <el-form-item label="功能描述">
          <el-input v-model="propForm.description" type="textarea" :rows="2" placeholder="抽象逻辑点位用途与物理含义说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="propDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPropForm">保存点位属性</el-button>
      </template>
    </el-dialog>

    <!-- Import Property Dialog (导入物模型属性点位定义弹窗) -->
    <el-dialog
      v-model="propImportDialogVisible"
      title="导入物模型属性点位定义"
      width="750px"
      destroy-on-close
    >
      <div class="prop-import-modal-content">
        <!-- 顶部指引与快捷导出模板 -->
        <div class="import-guide-alert">
          <SvgIcon name="info" :size="16" color="#06b6d4" />
          <div class="guide-text">
            <span>支持导入 <strong>.csv</strong> 或 <strong>.json</strong> 格式点位定义文件。首次使用推荐先下载标准模板。</span>
            <el-link type="primary" :underline="false" style="margin-left: 8px; font-size: 0.82rem;" @click="handleExportPropTemplate">
              下载点位定义模板 (CSV)
            </el-link>
          </div>
        </div>

        <!-- 文件选择操作栏 -->
        <div class="import-upload-zone" @click="triggerSelectFile">
          <input
            type="file"
            ref="fileInputRef"
            style="display: none"
            accept=".csv, .json, text/csv, application/json"
            @change="onFileSelected"
          />
          <SvgIcon name="file-text" :size="34" color="#06b6d4" />
          <div class="zone-text">
            <span class="main-tip">点击此处选择点位定义文件 (.csv / .json)</span>
            <span class="sub-tip">请确保包含 点位Code、点位名称、数据类型 等必填字段</span>
          </div>
        </div>

        <!-- 冲突处理策略 -->
        <div class="import-strategy-bar">
          <span class="strategy-label">冲突处理策略:</span>
          <el-radio-group v-model="importStrategy" size="small">
            <el-radio label="OVERWRITE">覆盖已有同名点位</el-radio>
            <el-radio label="SKIP">跳过已有同名点位</el-radio>
          </el-radio-group>
        </div>

        <!-- 待导入数据表格预览 -->
        <div v-if="parsedPropList.length > 0" class="import-preview-wrapper">
          <div class="preview-header">
            <span>待导入点位预览 (已成功解析 {{ parsedPropList.length }} 项):</span>
          </div>
          <el-table :data="parsedPropList" size="small" border max-height="240px" stripe>
            <el-table-column prop="pointCode" label="点位 Code" width="135">
              <template #default="scope">
                <code class="text-cyan">{{ scope.row.pointCode }}</code>
              </template>
            </el-table-column>
            <el-table-column prop="pointName" label="点位名称" width="135" show-overflow-tooltip />
            <el-table-column prop="dataType" label="类型" width="85">
              <template #default="scope">
                <el-tag size="small" effect="plain">{{ scope.row.dataType }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="accessMode" label="读写" width="65" />
            <el-table-column prop="unit" label="单位" width="70" />
            <el-table-column label="动作" width="80" align="center">
              <template #default="scope">
                <el-tag v-if="scope.row.isExisting" size="small" type="warning">覆盖</el-tag>
                <el-tag v-else size="small" type="success">新增</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <template #footer>
        <el-button @click="propImportDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="importLoading"
          :disabled="parsedPropList.length === 0"
          @click="submitImportProps"
        >
          确认导入 {{ parsedPropList.length > 0 ? `(${parsedPropList.length}条)` : '' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- Add/Edit Service Dialog -->
    <el-dialog v-model="serviceDialogVisible" :title="isServiceEdit ? '编辑控制服务' : '新增控制服务'" width="820px">
      <el-form :model="serviceForm" :rules="serviceRules" ref="serviceFormRef" label-width="120px">
        <el-form-item label="服务 Code" prop="serviceCode">
          <el-input v-model="serviceForm.serviceCode" :disabled="isServiceEdit" placeholder="如 startCoolingFan" />
        </el-form-item>
        <el-form-item label="服务名称" prop="serviceName">
          <el-input v-model="serviceForm.serviceName" placeholder="如 开启冷却风扇" />
        </el-form-item>
        <el-form-item label="服务描述">
          <el-input v-model="serviceForm.description" placeholder="功能与触发条件说明" />
        </el-form-item>

        <!-- Input Parameters Builder -->
        <div class="params-builder">
          <div class="builder-header">
            <div>
              <span style="font-weight: 600; color: #f8fafc;">服务输入参数定义 (Input Parameters)</span>
              <span style="font-size: 12px; color: #94a3b8; margin-left: 8px;">
                (可直接选择已有点位自动带出名称与类型，或手动输入自定义Code)
              </span>
            </div>
            <el-button size="small" type="primary" plain @click="addServiceParam">
              + 添加入参
            </el-button>
          </div>

          <!-- Column Headers -->
          <div v-if="serviceForm.inputParams && serviceForm.inputParams.length > 0" class="param-row-header">
            <span style="flex: 1.4;">参数 Code</span>
            <span style="flex: 1.2;">参数名称</span>
            <span style="width: 110px;">数据类型</span>
            <span style="width: 140px;">默认值 (Default)</span>
            <span style="width: 40px; text-align: center;">操作</span>
          </div>

          <div v-for="(p, index) in serviceForm.inputParams" :key="index" class="param-row">
            <!-- 1. 参数 Code: 直接显示 code，支持任意自定义输入，并提供已有测点下拉建议 -->
            <el-autocomplete
              v-model="p.pointCode"
              :fetch-suggestions="queryPropertySuggestions"
              :trigger-on-focus="true"
              placeholder="输入Code或选点位"
              clearable
              style="flex: 1.4;"
              @select="(item) => onParamCodeSelect(p, item.value)"
              @change="(val) => onParamCodeChange(p, val)"
            >
              <template #default="{ item }">
                <div style="display: flex; justify-content: space-between; align-items: center; width: 100%;">
                  <span style="font-family: monospace; font-weight: 600; color: #38bdf8;">{{ item.value }}</span>
                  <span style="font-size: 12px; color: #94a3b8; margin-left: 10px;">{{ item.pointName }} ({{ item.dataType }})</span>
                </div>
              </template>
            </el-autocomplete>

            <!-- 2. 参数名称: 联动带出，亦可自定义修改 -->
            <el-input
              v-model="p.pointName"
              placeholder="参数名称"
              style="flex: 1.2;"
            />

            <!-- 3. 数据类型: 联动锁定/可选 -->
            <el-select v-model="p.dataType" style="width: 110px;" @change="() => onParamDataTypeChange(p)">
              <el-option label="INT" value="INT" />
              <el-option label="FLOAT" value="FLOAT" />
              <el-option label="DOUBLE" value="DOUBLE" />
              <el-option label="STRING" value="STRING" />
              <el-option label="BOOL" value="BOOL" />
            </el-select>

            <!-- 4. 预设默认值: 针对布尔、数字、字符串适配控件 -->
            <div style="width: 140px;">
              <el-select
                v-if="p.dataType === 'BOOL'"
                v-model="p.defaultValue"
                placeholder="默认值"
                clearable
                style="width: 100%;"
              >
                <el-option label="True (开启)" :value="true" />
                <el-option label="False (关闭)" :value="false" />
              </el-select>
              <el-input-number
                v-else-if="p.dataType === 'INT' || p.dataType === 'FLOAT' || p.dataType === 'DOUBLE'"
                v-model="p.defaultValue"
                placeholder="默认值"
                :precision="p.dataType === 'INT' ? 0 : 2"
                :step="p.dataType === 'INT' ? 1 : 0.1"
                controls-position="right"
                style="width: 100%;"
              />
              <el-input
                v-else
                v-model="p.defaultValue"
                placeholder="默认值(可选)"
                clearable
                style="width: 100%;"
              />
            </div>

            <!-- 5. 操作按钮 -->
            <div style="width: 40px; text-align: center;">
              <el-button type="danger" circle size="small" @click="removeServiceParam(index)">✕</el-button>
            </div>
          </div>

          <div v-if="!serviceForm.inputParams || serviceForm.inputParams.length === 0" class="empty-params-tip">
            暂无输入参数 (该服务为无参控制命令，如需传参请点击上方 “+ 添加入参”)
          </div>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="serviceDialogVisible = false">取消</el-button>
        <el-button type="success" @click="submitServiceForm">保存控制服务</el-button>
      </template>
    </el-dialog>

    <!-- Add/Edit Event Dialog -->
    <el-dialog v-model="eventDialogVisible" :title="isEventEdit ? '编辑事件定义' : '新增事件定义'" width="520px">
      <el-form :model="eventForm" :rules="eventRules" ref="eventFormRef" label-width="120px">
        <el-form-item label="事件 Code" prop="eventCode">
          <el-input v-model="eventForm.eventCode" :disabled="isEventEdit" placeholder="如 OVER_TEMP_WARN" />
        </el-form-item>
        <el-form-item label="事件名称" prop="eventName">
          <el-input v-model="eventForm.eventName" placeholder="如 轴承温度超温告警" />
        </el-form-item>
        <el-form-item label="事件类型" prop="eventType">
          <el-select v-model="eventForm.eventType" style="width: 100%">
            <el-option label="ALERT (告警事件)" value="ALERT" />
            <el-option label="FAULT (故障事件)" value="FAULT" />
            <el-option label="INFO (信息事件)" value="INFO" />
          </el-select>
        </el-form-item>
        <el-form-item label="告警级别" prop="eventLevel">
          <el-select v-model="eventForm.eventLevel" style="width: 100%">
            <el-option label="INFO (一般提示)" value="INFO" />
            <el-option label="WARN (主要警告)" value="WARN" />
            <el-option label="CRITICAL (严重故障)" value="CRITICAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="功能与 Payload 描述">
          <el-input v-model="eventForm.description" type="textarea" :rows="2" placeholder="描述硬件设备触发此事件的内部条件与数据结构说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="eventDialogVisible = false">取消</el-button>
        <el-button type="warning" @click="submitEventForm">保存事件定义</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import SvgIcon from '../../components/common/SvgIcon.vue'
import { hasPermission } from '@/utils/permission'
import {
  getAllThingModels,
  getThingModel,
  addThingModel,
  updateThingModel,
  deleteThingModel,
  addProperty,
  updateProperty,
  deleteProperty,
  addService,
  updateService,
  deleteService,
  addEvent,
  updateEvent,
  deleteEvent
} from '../../api/thingmodel'

const router = useRouter()

const navigateToDeviceList = (tmpl) => {
  if (!tmpl || (tmpl.deviceCount || 0) <= 0) return
  router.push({
    path: '/devices',
    query: { modelKey: tmpl.modelKey || tmpl.modelCode }
  })
}

const queryForm = ref({ keyword: '', category: '' })
const activeQueryParams = ref({ keyword: '', category: '' })

const thingModels = ref([])

const fetchModels = async () => {
  try {
    const res = await getAllThingModels()
    const list = (res && res.data) ? res.data : (Array.isArray(res) ? res : [])
    thingModels.value = list.map(item => ({
      modelCode: item.modelKey || item.modelCode,
      modelKey: item.modelKey || item.modelCode,
      modelName: item.modelName || ('物模型模板 (' + (item.modelKey || item.modelCode) + ')'),
      category: item.category || 'ROTATING_EQUIP',
      description: item.description || '',
      properties: item.properties || [],
      services: item.services || [],
      events: item.events || [],
      deviceCount: typeof item.deviceCount === 'number' ? item.deviceCount : 0
    }))
  } catch (e) {
    console.error('Failed to fetch thing models from backend API:', e)
    ElMessage.error('无法加载物模型模板列表')
  }
}

const refreshSelectedModel = async (modelKey) => {
  try {
    const res = await getThingModel(modelKey)
    const data = (res && res.data) ? res.data : res
    if (data) {
      selectedModel.value = {
        modelCode: data.modelKey || data.modelCode,
        modelKey: data.modelKey || data.modelCode,
        modelName: data.modelName || ('物模型模板 (' + (data.modelKey || data.modelCode) + ')'),
        category: data.category || selectedModel.value?.category || 'ROTATING_EQUIP',
        description: data.description || selectedModel.value?.description || '',
        properties: data.properties || [],
        services: data.services || [],
        events: data.events || [],
        deviceCount: typeof data.deviceCount === 'number' ? data.deviceCount : (selectedModel.value?.deviceCount || 0)
      }
    }
    await fetchModels()
  } catch (e) {
    console.error('Failed to refresh model:', e)
  }
}

onMounted(() => {
  fetchModels()
})

const categoryMap = {
  'ROTATING_EQUIP': '旋转机械/变频电机',
  'PLC_AUTOMATION': 'PLC 控制器/自动化',
  'GATEWAY': '工业以太网网关',
  'ENV_MONITOR': '环境与能源监测'
}

const getCategoryLabel = (cat) => categoryMap[cat] || '通用硬件'

// Filter & Pagination
const currentPage = ref(1)
const pageSize = ref(8)

const filteredModels = computed(() => {
  return thingModels.value.filter(m => {
    const kw = activeQueryParams.value.keyword ? activeQueryParams.value.keyword.trim().toLowerCase() : ''
    const cat = activeQueryParams.value.category

    const matchKw = !kw ||
      (m.modelCode && m.modelCode.toLowerCase().includes(kw)) ||
      (m.modelName && m.modelName.toLowerCase().includes(kw))
    const matchCat = !cat || m.category === cat
    return matchKw && matchCat
  })
})

const paginatedModels = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredModels.value.slice(start, end)
})

const handleSearch = () => {
  activeQueryParams.value = { ...queryForm.value }
  currentPage.value = 1
}

const resetQuery = () => {
  queryForm.value = { keyword: '', category: '' }
  activeQueryParams.value = { keyword: '', category: '' }
  currentPage.value = 1
}

const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
}

const handleCurrentChange = (val) => {
  currentPage.value = val
}

// Model Meta Dialog (Create / Edit Template)
const modelMetaDialogVisible = ref(false)
const isModelEdit = ref(false)
const modelMetaFormRef = ref(null)
const modelMetaForm = ref({ modelCode: '', modelName: '', category: 'ROTATING_EQUIP', description: '' })
const modelMetaRules = {
  modelName: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }]
}

const openCreateModelModal = () => {
  if (!hasPermission(['thingmodel:add', 'thingmodel:edit'])) {
    ElMessage.warning('暂无新建物模型权限 (thingmodel:add)')
    return
  }
  isModelEdit.value = false
  modelMetaForm.value = {
    modelCode: '',
    modelName: '',
    category: 'ROTATING_EQUIP',
    description: ''
  }
  modelMetaDialogVisible.value = true
}

const openEditModelModal = (tmpl) => {
  if (!hasPermission('thingmodel:edit')) {
    ElMessage.warning('暂无编辑物模型权限 (thingmodel:edit)')
    return
  }
  isModelEdit.value = true
  modelMetaForm.value = JSON.parse(JSON.stringify(tmpl))
  modelMetaDialogVisible.value = true
}

const submitModelMetaForm = () => {
  modelMetaFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        let finalModelCode = modelMetaForm.value.modelCode ? modelMetaForm.value.modelCode.trim() : ''
        if (!finalModelCode && !isModelEdit.value) {
          finalModelCode = 'PK_' + Math.random().toString(36).substring(2, 10).toUpperCase()
        }
        const payload = {
          modelKey: finalModelCode,
          modelName: modelMetaForm.value.modelName,
          category: modelMetaForm.value.category,
          description: modelMetaForm.value.description
        }
        if (isModelEdit.value) {
          await updateThingModel(modelMetaForm.value.modelCode, payload)
          ElMessage.success('物模型模板基础信息更新成功')
        } else {
          await addThingModel(payload)
          ElMessage.success('新建物模型模板成功')
        }
        modelMetaDialogVisible.value = false
        await fetchModels()
      } catch (e) {
        console.error('Submit model meta failed:', e)
        ElMessage.error(isModelEdit.value ? '物模型模板更新失败' : '新建物模型模板失败')
      }
    }
  })
}

const handleDeleteModel = (tmpl) => {
  if (!hasPermission(['thingmodel:delete', 'thingmodel:edit'])) {
    ElMessage.warning('暂无删除物模型权限 (thingmodel:delete)')
    return
  }
  if ((tmpl.deviceCount || 0) > 0) {
    ElMessage.warning(`物模型 [${tmpl.modelName}] 当前正被 ${tmpl.deviceCount} 台设备引用，禁止删除！请先解绑或删除关联设备。`)
    return
  }

  ElMessageBox.confirm(`确定要删除物模型模板 [${tmpl.modelName}] 吗？删除后不可恢复。`, '警告', { type: 'warning' }).then(async () => {
    try {
      await deleteThingModel(tmpl.modelCode)
      ElMessage.success('删除成功')
      await fetchModels()
    } catch (e) {
      console.error('Delete model failed:', e)
      const errorMsg = (e && e.response && e.response.data && (e.response.data.msg || e.response.data.message)) || e.message || '删除物模型模板失败'
      ElMessage.error(errorMsg)
    }
  })
}

// FULL SCHEMA DESIGNER DRAWER
const schemaDrawerVisible = ref(false)
const selectedModel = ref(null)

const openSchemaDrawer = (tmpl) => {
  if (!hasPermission(['thingmodel:design', 'thingmodel:edit'])) {
    ElMessage.warning('暂无设计物模型权限 (thingmodel:design)');
    return;
  }
  selectedModel.value = tmpl
  schemaDrawerVisible.value = true
}

// Property CRUD
const propDialogVisible = ref(false)
const isPropEdit = ref(false)
const propFormRef = ref(null)
const propForm = ref({
  pointCode: '',
  pointName: '',
  dataType: 'DOUBLE',
  accessMode: 'R',
  unit: '',
  address: '',
  description: '',
  enumOptions: []
})
const propRules = {
  pointCode: [{ required: true, message: '请输入点位Code', trigger: 'blur' }],
  pointName: [{ required: true, message: '请输入点位名称', trigger: 'blur' }]
}

const onPropDataTypeChange = (val) => {
  if (val === 'ENUM' && (!propForm.value.enumOptions || propForm.value.enumOptions.length === 0)) {
    propForm.value.enumOptions = [
      { value: 0, label: '关 / OFF / 模式0' },
      { value: 1, label: '开 / ON / 模式1' }
    ]
  }
}

const addEnumOption = () => {
  if (!propForm.value.enumOptions) propForm.value.enumOptions = []
  const nextVal = propForm.value.enumOptions.length
  propForm.value.enumOptions.push({ value: nextVal, label: `工况状态 ${nextVal}` })
}

const removeEnumOption = (idx) => {
  propForm.value.enumOptions.splice(idx, 1)
}

const openAddPropModal = () => {
  isPropEdit.value = false
  propForm.value = {
    pointCode: '',
    pointName: '',
    dataType: 'DOUBLE',
    accessMode: 'R',
    min: 0,
    max: 500,
    unit: '',
    address: '',
    description: '',
    enumOptions: [
      { value: 0, label: '关 / OFF / 模式0' },
      { value: 1, label: '开 / ON / 模式1' }
    ]
  }
  propDialogVisible.value = true
}

const openEditPropModal = (row) => {
  isPropEdit.value = true
  const data = JSON.parse(JSON.stringify(row))
  if (data.min === undefined) data.min = 0
  if (data.max === undefined) data.max = 500
  if (!data.enumOptions) {
    data.enumOptions = [
      { value: 0, label: '关 / OFF / 模式0' },
      { value: 1, label: '开 / ON / 模式1' }
    ]
  }
  propForm.value = data
  propDialogVisible.value = true
}

const submitPropForm = () => {
  propFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const modelKey = selectedModel.value.modelCode || selectedModel.value.modelKey
        if (isPropEdit.value) {
          await updateProperty(modelKey, propForm.value.pointCode, propForm.value)
          ElMessage.success('属性点位更新成功')
        } else {
          await addProperty(modelKey, propForm.value)
          ElMessage.success('属性点位新增成功')
        }
        propDialogVisible.value = false
        await refreshSelectedModel(modelKey)
      } catch (e) {
        console.error('Submit property failed:', e)
        ElMessage.error(isPropEdit.value ? '属性点位更新失败' : '属性点位新增失败')
      }
    }
  })
}

const handleDeleteProp = (row) => {
  ElMessageBox.confirm(`确定要删除属性点位 [${row.pointName}] 吗？`, '提示', { type: 'warning' }).then(async () => {
    try {
      const modelKey = selectedModel.value.modelCode || selectedModel.value.modelKey
      await deleteProperty(modelKey, row.pointCode)
      ElMessage.success('删除成功')
      await refreshSelectedModel(modelKey)
    } catch (e) {
      console.error('Delete property failed:', e)
      ElMessage.error('删除属性点位失败')
    }
  })
}

// ==================== 属性点位导入与导出模板功能 ====================
const propImportDialogVisible = ref(false)
const parsedPropList = ref([])
const importStrategy = ref('OVERWRITE')
const importLoading = ref(false)
const fileInputRef = ref(null)

// 导出物模型属性点位定义模板 (CSV 格式, 带 UTF-8 BOM 兼容 Excel)
const handleExportPropTemplate = () => {
  if (!selectedModel.value) return
  const modelName = selectedModel.value.modelName || selectedModel.value.modelKey || '物模型'

  // CSV 表头规范
  const header = '点位Code(必填),点位名称(必填),数据类型(FLOAT/DOUBLE/INT/STRING/BOOL/ENUM),读写模式(R/RW),物理单位,采样触发模式(PERIODIC/COV),采样基频倍数,功能描述,枚举映射(例:0:关|1:开)\n'

  // 若当前物模型中已有属性点位，则将其完整导出为模板行，方便用户增量编辑；若为空，则导出工业级规范示例行
  let rows = ''
  if (selectedModel.value.properties && selectedModel.value.properties.length > 0) {
    rows = selectedModel.value.properties.map(p => {
      let enumStr = ''
      if (p.dataType === 'ENUM' && p.enumOptions && p.enumOptions.length > 0) {
        enumStr = p.enumOptions.map(opt => `${opt.value}:${opt.label}`).join('|')
      }
      const safeDesc = (p.description || '').replace(/"/g, '""')
      return `"${p.pointCode}","${p.pointName}","${p.dataType || 'DOUBLE'}","${p.accessMode || 'R'}","${p.unit || ''}","${p.triggerMode || 'PERIODIC'}","${p.sampleFactor || 1}","${safeDesc}","${enumStr}"`
    }).join('\n')
  } else {
    rows = [
      '"temperature","主轴承温度","DOUBLE","R","℃","PERIODIC","1","电机主轴承温度传感器只读数据",""',
      '"humidity","环境相对湿度","DOUBLE","R","%RH","PERIODIC","1","车间环境湿度传感器",""',
      '"pressure","进气口设定压力","DOUBLE","RW","kPa","PERIODIC","1","控制风机进气目标压力",""',
      '"targetSpeed","变频目标转速","INT","RW","RPM","PERIODIC","1","额定转速设定值",""',
      '"runStatus","运行启停状态","BOOL","R","","COV","1","设备RUN/STOP运行标志",""',
      '"workMode","工况模式选择","ENUM","RW","","PERIODIC","1","工况运行工作模式切换","0:待机模式|1:自动运行|2:手动维护|3:节能巡检"'
    ].join('\n')
  }

  const blob = new Blob([`\uFEFF${header}${rows}\n`], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.setAttribute('href', url)
  link.setAttribute('download', `${modelName}_属性点位定义模板.csv`)
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
  ElMessage.success('属性点位模板导出成功！可编辑后通过【导入点位】导入系统。')
}

// 打开导入点位弹窗
const openImportPropModal = () => {
  parsedPropList.value = []
  importStrategy.value = 'OVERWRITE'
  propImportDialogVisible.value = true
}

// 触发原生文件选择器
const triggerSelectFile = () => {
  if (fileInputRef.value) {
    fileInputRef.value.value = ''
    fileInputRef.value.click()
  }
}

// 解析 CSV 文本
const parsePropCSV = (text) => {
  const lines = text.split(/\r?\n/).filter(line => line.trim().length > 0)
  if (lines.length < 2) {
    throw new Error('CSV 文件内容为空或缺少数据行')
  }

  const result = []
  for (let i = 1; i < lines.length; i++) {
    const rawLine = lines[i].trim()
    if (!rawLine || rawLine.startsWith('#')) continue

    const cols = []
    let curr = ''
    let inQuotes = false
    for (let charIndex = 0; charIndex < rawLine.length; charIndex++) {
      const char = rawLine[charIndex]
      if (char === '"') {
        if (inQuotes && rawLine[charIndex + 1] === '"') {
          curr += '"'
          charIndex++
        } else {
          inQuotes = !inQuotes
        }
      } else if (char === ',' && !inQuotes) {
        cols.push(curr.trim())
        curr = ''
      } else {
        curr += char
      }
    }
    cols.push(curr.trim())

    const pointCode = (cols[0] || '').replace(/^["']|["']$/g, '').trim()
    const pointName = (cols[1] || '').replace(/^["']|["']$/g, '').trim()
    if (!pointCode || !pointName) continue

    let dataType = ((cols[2] || 'DOUBLE').replace(/^["']|["']$/g, '').trim()).toUpperCase()
    if (!['FLOAT', 'DOUBLE', 'INT', 'STRING', 'BOOL', 'ENUM'].includes(dataType)) {
      dataType = 'DOUBLE'
    }

    let accessMode = ((cols[3] || 'R').replace(/^["']|["']$/g, '').trim()).toUpperCase()
    if (!['R', 'RW'].includes(accessMode)) {
      accessMode = 'R'
    }

    const unit = (cols[4] || '').replace(/^["']|["']$/g, '').trim()
    let triggerMode = ((cols[5] || 'PERIODIC').replace(/^["']|["']$/g, '').trim()).toUpperCase()
    if (!['PERIODIC', 'COV'].includes(triggerMode)) {
      triggerMode = 'PERIODIC'
    }

    const sampleFactor = parseInt((cols[6] || '1').replace(/^["']|["']$/g, '').trim(), 10) || 1
    const description = (cols[7] || '').replace(/^["']|["']$/g, '').trim()

    const rawEnum = (cols[8] || '').replace(/^["']|["']$/g, '').trim()
    const enumOptions = []
    if (dataType === 'ENUM' && rawEnum) {
      const parts = rawEnum.split(/[|,;]/)
      for (const p of parts) {
        const [vStr, lStr] = p.split(/[:：=]/)
        if (vStr !== undefined && lStr !== undefined) {
          enumOptions.push({
            value: parseInt(vStr.trim(), 10) || 0,
            label: lStr.trim()
          })
        }
      }
    }

    const existing = (selectedModel.value?.properties || []).some(item => item.pointCode === pointCode)

    result.push({
      pointCode,
      pointName,
      dataType,
      accessMode,
      unit,
      triggerMode,
      sampleFactor,
      description,
      enumOptions: enumOptions.length > 0 ? enumOptions : (dataType === 'ENUM' ? [{ value: 0, label: '模式0' }, { value: 1, label: '模式1' }] : []),
      isExisting: existing
    })
  }

  return result
}

// 处理文件选择解析
const onFileSelected = (e) => {
  const file = e.target.files?.[0]
  if (!file) return

  const isCSV = file.name.endsWith('.csv')
  const isJSON = file.name.endsWith('.json')
  if (!isCSV && !isJSON) {
    ElMessage.error('仅支持上传 .csv 或 .json 格式文件')
    return
  }

  const reader = new FileReader()
  reader.onload = (event) => {
    try {
      const content = event.target?.result
      if (typeof content !== 'string') return

      if (isCSV) {
        parsedPropList.value = parsePropCSV(content)
      } else if (isJSON) {
        const json = JSON.parse(content)
        const arr = Array.isArray(json) ? json : (json.properties || [])
        parsedPropList.value = arr.map(item => {
          const pointCode = item.pointCode || item.identifier || ''
          const pointName = item.pointName || item.name || pointCode
          const isExisting = (selectedModel.value?.properties || []).some(p => p.pointCode === pointCode)
          return {
            pointCode,
            pointName,
            dataType: (item.dataType || 'DOUBLE').toUpperCase(),
            accessMode: (item.accessMode || 'R').toUpperCase(),
            unit: item.unit || '',
            triggerMode: item.triggerMode || 'PERIODIC',
            sampleFactor: item.sampleFactor || 1,
            description: item.description || '',
            enumOptions: item.enumOptions || [],
            isExisting
          }
        }).filter(item => item.pointCode && item.pointName)
      }

      if (parsedPropList.value.length === 0) {
        ElMessage.warning('未能从文件中解析出有效的属性点位定义数据')
      } else {
        ElMessage.success(`成功解析 ${parsedPropList.value.length} 个属性点位，请核对预览`)
      }
    } catch (err) {
      console.error('File parse error:', err)
      ElMessage.error(`文件解析失败: ${err.message}`)
    }
  }
  reader.readAsText(file, 'utf-8')
}

// 提交批量导入属性点位
const submitImportProps = async () => {
  if (!parsedPropList.value || parsedPropList.value.length === 0) {
    ElMessage.warning('当前暂无可导入的点位数据，请先选择文件')
    return
  }
  if (!selectedModel.value) return

  const modelKey = selectedModel.value.modelCode || selectedModel.value.modelKey
  importLoading.value = true
  let addCount = 0
  let updateCount = 0
  let skipCount = 0
  let failCount = 0

  try {
    for (const prop of parsedPropList.value) {
      try {
        if (prop.isExisting) {
          if (importStrategy.value === 'OVERWRITE') {
            await updateProperty(modelKey, prop.pointCode, prop)
            updateCount++
          } else {
            skipCount++
          }
        } else {
          await addProperty(modelKey, prop)
          addCount++
        }
      } catch (err) {
        console.error(`Import property [${prop.pointCode}] failed:`, err)
        failCount++
      }
    }

    ElMessage.success(`导入完成: 新增 ${addCount} 个，更新 ${updateCount} 个，跳过 ${skipCount} 个${failCount > 0 ? `，失败 ${failCount} 个` : ''}`)
    propImportDialogVisible.value = false
    await refreshSelectedModel(modelKey)
  } catch (e) {
    ElMessage.error(`导入过程出现异常: ${e.message}`)
  } finally {
    importLoading.value = false
  }
}

// Service CRUD
const serviceDialogVisible = ref(false)
const isServiceEdit = ref(false)
const serviceFormRef = ref(null)
const serviceForm = ref({ serviceCode: '', serviceName: '', description: '', inputParams: [] })
const serviceRules = {
  serviceCode: [{ required: true, message: '请输入服务Code', trigger: 'blur' }],
  serviceName: [{ required: true, message: '请输入服务名称', trigger: 'blur' }]
}

const openAddServiceModal = () => {
  isServiceEdit.value = false
  serviceForm.value = { serviceCode: '', serviceName: '', description: '', inputParams: [] }
  serviceDialogVisible.value = true
}

const openEditServiceModal = (row) => {
  isServiceEdit.value = true
  const cloned = JSON.parse(JSON.stringify(row))
  if (Array.isArray(cloned.inputParams)) {
    cloned.inputParams = cloned.inputParams.map(p => ({
      ...p,
      defaultValue: p.defaultValue !== undefined ? p.defaultValue : (p.default !== undefined ? p.default : '')
    }))
  }
  serviceForm.value = cloned
  serviceDialogVisible.value = true
}

const addServiceParam = () => {
  if (!serviceForm.value.inputParams) serviceForm.value.inputParams = []
  serviceForm.value.inputParams.push({
    pointCode: '',
    pointName: '',
    dataType: 'INT',
    defaultValue: 0
  })
}

const queryPropertySuggestions = (queryString, cb) => {
  const props = selectedModel.value?.properties || []
  const list = props.map(p => ({
    value: p.pointCode,
    pointName: p.pointName,
    dataType: p.dataType
  }))
  if (queryString) {
    const kw = String(queryString).trim().toLowerCase()
    cb(list.filter(item =>
      item.value.toLowerCase().includes(kw) ||
      (item.pointName && item.pointName.toLowerCase().includes(kw))
    ))
  } else {
    cb(list)
  }
}

const onParamCodeSelect = (p, val) => {
  if (!val) return
  p.pointCode = String(val).trim()
  const matchedProp = (selectedModel.value?.properties || []).find(
    item => item.pointCode && item.pointCode.toLowerCase() === p.pointCode.toLowerCase()
  )
  if (matchedProp) {
    p.pointName = matchedProp.pointName
    p.dataType = matchedProp.dataType || 'INT'
    if (p.defaultValue === undefined || p.defaultValue === null || p.defaultValue === '') {
      if (p.dataType === 'BOOL') {
        p.defaultValue = false
      } else if (p.dataType === 'INT' || p.dataType === 'FLOAT' || p.dataType === 'DOUBLE') {
        p.defaultValue = matchedProp.min ?? 0
      }
    }
  }
}

const onParamCodeChange = (p, val) => {
  if (!val) return
  const str = String(val).trim()
  p.pointCode = str
  const matchedProp = (selectedModel.value?.properties || []).find(
    item => item.pointCode && item.pointCode.toLowerCase() === str.toLowerCase()
  )
  if (matchedProp) {
    if (!p.pointName || p.pointName === str) {
      p.pointName = matchedProp.pointName
    }
    p.dataType = matchedProp.dataType || 'INT'
  }
}

const onParamDataTypeChange = (p) => {
  if (p.dataType === 'BOOL') {
    if (typeof p.defaultValue !== 'boolean') p.defaultValue = false
  } else if (p.dataType === 'INT' || p.dataType === 'FLOAT' || p.dataType === 'DOUBLE') {
    if (typeof p.defaultValue !== 'number') p.defaultValue = 0
  }
}

const removeServiceParam = (idx) => {
  serviceForm.value.inputParams.splice(idx, 1)
}

const submitServiceForm = () => {
  serviceFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const modelKey = selectedModel.value.modelCode || selectedModel.value.modelKey
        // 数据清洗：去除空参数并格式化默认值
        const cleanedParams = (serviceForm.value.inputParams || [])
          .filter(p => p.pointCode && String(p.pointCode).trim() !== '')
          .map(p => ({
            pointCode: String(p.pointCode).trim(),
            pointName: p.pointName ? String(p.pointName).trim() : String(p.pointCode).trim(),
            dataType: p.dataType || 'INT',
            defaultValue: p.defaultValue !== undefined ? p.defaultValue : null
          }))

        const payload = {
          ...serviceForm.value,
          inputParams: cleanedParams
        }

        if (isServiceEdit.value) {
          await updateService(modelKey, serviceForm.value.serviceCode, payload)
          ElMessage.success('控制服务更新成功')
        } else {
          await addService(modelKey, payload)
          ElMessage.success('控制服务新增成功')
        }
        serviceDialogVisible.value = false
        await refreshSelectedModel(modelKey)
      } catch (e) {
        console.error('Submit service failed:', e)
        ElMessage.error(isServiceEdit.value ? '控制服务更新失败' : '控制服务新增失败')
      }
    }
  })
}

const handleDeleteService = (row) => {
  ElMessageBox.confirm(`确定要删除控制服务 [${row.serviceName}] 吗？`, '提示', { type: 'warning' }).then(async () => {
    try {
      const modelKey = selectedModel.value.modelCode || selectedModel.value.modelKey
      await deleteService(modelKey, row.serviceCode)
      ElMessage.success('删除成功')
      await refreshSelectedModel(modelKey)
    } catch (e) {
      console.error('Delete service failed:', e)
      ElMessage.error('删除控制服务失败')
    }
  })
}

// Event CRUD
const eventDialogVisible = ref(false)
const isEventEdit = ref(false)
const eventFormRef = ref(null)
const eventForm = ref({ eventCode: '', eventName: '', eventType: 'ALERT', eventLevel: 'WARN', description: '' })
const eventRules = {
  eventCode: [{ required: true, message: '请输入事件Code', trigger: 'blur' }],
  eventName: [{ required: true, message: '请输入事件名称', trigger: 'blur' }]
}

const openAddEventModal = () => {
  isEventEdit.value = false
  eventForm.value = { eventCode: '', eventName: '', eventType: 'ALERT', eventLevel: 'WARN', description: '' }
  eventDialogVisible.value = true
}

const openEditEventModal = (row) => {
  isEventEdit.value = true
  const data = JSON.parse(JSON.stringify(row))
  if (!data.eventType) data.eventType = 'ALERT'
  eventForm.value = data
  eventDialogVisible.value = true
}

const submitEventForm = () => {
  eventFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const modelKey = selectedModel.value.modelCode || selectedModel.value.modelKey
        if (isEventEdit.value) {
          await updateEvent(modelKey, eventForm.value.eventCode, eventForm.value)
          ElMessage.success('事件定义更新成功')
        } else {
          await addEvent(modelKey, eventForm.value)
          ElMessage.success('事件定义新增成功')
        }
        eventDialogVisible.value = false
        await refreshSelectedModel(modelKey)
      } catch (e) {
        console.error('Submit event failed:', e)
        ElMessage.error(isEventEdit.value ? '事件定义更新失败' : '事件定义新增失败')
      }
    }
  })
}

const handleDeleteEvent = (row) => {
  ElMessageBox.confirm(`确定要删除告警规则 [${row.eventName}] 吗？`, '提示', { type: 'warning' }).then(async () => {
    try {
      const modelKey = selectedModel.value.modelCode || selectedModel.value.modelKey
      await deleteEvent(modelKey, row.eventCode)
      ElMessage.success('删除成功')
      await refreshSelectedModel(modelKey)
    } catch (e) {
      console.error('Delete event failed:', e)
      ElMessage.error('删除告警事件失败')
    }
  })
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

/* Model Cards Grid */
.model-cards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 20px;
}

.model-card {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 20px;
  gap: 14px;
}

.card-top-bar {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.model-title-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.model-name {
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--iot-text-primary);
}

.model-code {
  background: var(--iot-bg-tertiary);
  padding: 2px 8px;
  border-radius: 4px;
  font-family: monospace;
  font-size: 0.82rem;
  color: var(--iot-color-cyan);
  align-self: flex-start;
}

.model-desc {
  font-size: 0.85rem;
  color: var(--iot-text-secondary);
  line-height: 1.5;
  min-height: 40px;
}

/* Metrics Badges Row */
.metrics-badges-row {
  display: flex;
  align-items: center;
  justify-content: space-around;
  background: rgba(15, 23, 42, 0.6);
  border: 1px solid var(--iot-border-color);
  padding: 12px;
  border-radius: 6px;
}

.metric-badge {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.m-val {
  font-size: 1.2rem;
  font-weight: 700;
  font-family: monospace;
}

.m-lbl {
  font-size: 0.78rem;
  color: var(--iot-text-secondary);
  margin-top: 2px;
}

.metric-divider {
  width: 1px;
  height: 24px;
  background: rgba(255, 255, 255, 0.1);
}

.text-cyan { color: var(--iot-color-cyan); }
.text-green { color: var(--iot-color-green); }
.text-amber { color: #f59e0b; }
.text-indigo { color: #818cf8; }
.text-muted { color: #64748b; }
.text-mono { font-family: monospace; color: #38bdf8; }

.card-top-tags {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.ref-tag {
  font-weight: 500;
}

.btn-tooltip-wrapper {
  display: inline-block;
}

.metric-badge.is-clickable {
  cursor: pointer;
  user-select: none;
  transition: all 0.2s ease;
  border-radius: 4px;
  padding: 2px 6px;
}

.metric-badge.is-clickable:hover {
  background: rgba(129, 140, 248, 0.12);
  transform: translateY(-2px);
}

.metric-badge.is-clickable:hover .m-val {
  text-decoration: underline;
  color: #a5b4fc;
}

.metric-badge.is-clickable:hover .link-lbl {
  color: #818cf8;
}

.link-lbl {
  display: inline-flex;
  align-items: center;
  gap: 2px;
}

.click-hint {
  font-size: 0.72rem;
  opacity: 0.85;
  font-weight: bold;
}

.card-actions-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  border-top: 1px solid var(--iot-border-color);
  padding-top: 14px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
}

/* Schema Designer Drawer */
.schema-designer-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.designer-hero-card {
  background: linear-gradient(135deg, rgba(15, 23, 42, 0.9), rgba(30, 41, 59, 0.8));
  border: 1px solid rgba(6, 182, 212, 0.3);
  padding: 16px 20px;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.hero-top {
  display: flex;
  align-items: center;
  gap: 12px;
}

.hero-name {
  font-size: 1.15rem;
  font-weight: 700;
  color: #f8fafc;
}

.hero-code {
  background: rgba(6, 182, 212, 0.15);
  color: var(--iot-color-cyan);
  padding: 2px 8px;
  border-radius: 4px;
  font-family: monospace;
}

.hero-desc {
  font-size: 0.85rem;
  color: #94a3b8;
}

.tab-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.tab-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.tab-tip {
  font-size: 0.82rem;
  color: var(--iot-text-secondary);
}

/* 导入属性点位弹窗样式 */
.prop-import-modal-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.import-guide-alert {
  display: flex;
  align-items: center;
  gap: 8px;
  background: rgba(6, 182, 212, 0.08);
  border: 1px solid rgba(6, 182, 212, 0.25);
  padding: 10px 14px;
  border-radius: 6px;
}

.guide-text {
  font-size: 0.85rem;
  color: #cbd5e1;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}

.import-upload-zone {
  border: 2px dashed rgba(6, 182, 212, 0.4);
  background: rgba(15, 23, 42, 0.6);
  border-radius: 8px;
  padding: 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  cursor: pointer;
  transition: all 0.25s ease;
}

.import-upload-zone:hover {
  border-color: #06b6d4;
  background: rgba(6, 182, 212, 0.06);
}

.zone-text {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.main-tip {
  color: #f8fafc;
  font-weight: 500;
  font-size: 0.92rem;
}

.sub-tip {
  color: #94a3b8;
  font-size: 0.78rem;
}

.import-strategy-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 4px 0;
}

.strategy-label {
  font-size: 0.85rem;
  color: #94a3b8;
}

.import-preview-wrapper {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.preview-header {
  font-size: 0.82rem;
  color: var(--iot-color-cyan);
  font-weight: 500;
}

.params-badge-wrapper {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.text-muted {
  font-size: 0.82rem;
  color: #64748b;
  font-style: italic;
}

.params-builder {
  border: 1px solid var(--iot-border-color);
  border-radius: 6px;
  padding: 12px;
  background: var(--iot-bg-primary);
  margin-top: 10px;
}

.builder-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  font-size: 0.85rem;
  color: var(--iot-text-secondary);
}

.param-row {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 8px;
}

.param-row-header {
  display: flex;
  gap: 8px;
  font-size: 12px;
  color: var(--iot-text-secondary);
  padding: 0 4px 6px;
  border-bottom: 1px dashed rgba(255, 255, 255, 0.1);
  margin-bottom: 8px;
  font-weight: 500;
}

.empty-params-tip {
  text-align: center;
  font-size: 12px;
  color: var(--iot-text-muted);
  padding: 14px 0 6px;
}

/* ENUM Options Builder & Table Tags */
.enum-options-builder {
  background: rgba(15, 23, 42, 0.5);
  border: 1px dashed rgba(6, 182, 212, 0.35);
  border-radius: 6px;
  padding: 10px;
  width: 100%;
}

.builder-title {
  font-size: 0.8rem;
  font-weight: 600;
  color: var(--iot-color-cyan);
  display: flex;
  align-items: center;
  gap: 4px;
}

.enum-opt-row {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 6px;
}

.empty-enum-tip {
  font-size: 0.8rem;
  color: #64748b;
  text-align: center;
  padding: 8px 0;
}

.data-type-cell {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
}

.enum-tags-wrapper {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-top: 2px;
}

.enum-tag-item {
  font-size: 0.72rem;
  padding: 0 4px;
}

/* Range & Unit Box Styling */
.range-unit-box {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 2px;
}

.range-text {
  font-family: monospace;
  font-size: 0.78rem;
  font-weight: 600;
}

.unit-tag {
  font-size: 0.78rem;
  color: #94a3b8;
  background: rgba(255, 255, 255, 0.06);
  padding: 1px 5px;
  border-radius: 4px;
}

.range-inputs-flex {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
}

.range-sep {
  color: #94a3b8;
  font-weight: 600;
}

.action-btn-flex {
  display: flex;
  align-items: center;
  gap: 6px;
  white-space: nowrap;
}

/* Architecture Tip Banner */
.architecture-tip-banner {
  background: rgba(245, 158, 11, 0.08);
  border: 1px solid rgba(245, 158, 11, 0.3);
  border-radius: 6px;
  padding: 10px 14px;
}

.tip-header {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.85rem;
  font-weight: 600;
  color: #f59e0b;
}

.tip-body {
  font-size: 0.82rem;
  color: #94a3b8;
  margin-top: 4px;
  line-height: 1.5;
}

.tip-body code {
  background: rgba(255, 255, 255, 0.08);
  padding: 2px 6px;
  border-radius: 4px;
  color: #38bdf8;
  font-family: monospace;
}
</style>
