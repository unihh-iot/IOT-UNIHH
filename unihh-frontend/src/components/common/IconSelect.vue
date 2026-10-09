<!--
  Author: deversun
  Website: https://unihh.com
  License: Apache License 2.0
-->
<template>
  <div class="icon-select-container">
    <el-popover
      v-model:visible="popoverVisible"
      placement="bottom-start"
      :width="420"
      trigger="click"
      popper-class="icon-select-popper"
      :teleported="true"
    >
      <template #reference>
        <el-input
          :model-value="modelValue"
          placeholder="点击选择或输入图标"
          readonly
          class="icon-trigger-input"
          @click="popoverVisible = true"
        >
          <template #prepend>
            <div class="icon-preview-box">
              <SvgIcon v-if="modelValue" :name="modelValue" :size="18" color="#06b6d4" />
              <SvgIcon v-else name="menu" :size="18" color="#64748b" />
            </div>
          </template>
          <template #suffix>
            <div class="trigger-actions">
              <span
                v-if="modelValue"
                class="clear-btn"
                title="清空图标"
                @click.stop="handleClear"
              >
                ✕
              </span>
              <span class="dropdown-arrow" :class="{ 'is-active': popoverVisible }">▼</span>
            </div>
          </template>
        </el-input>
      </template>

      <!-- 下拉选择面板内容 -->
      <div class="icon-picker-panel">
        <div class="panel-header">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索图标名称、拼音或中文说明..."
            size="small"
            clearable
            class="search-input"
          >
            <template #prefix>
              <SvgIcon name="search" :size="14" color="#06b6d4" />
            </template>
          </el-input>
        </div>

        <div class="icon-grid-scroll">
          <div v-if="filteredIcons.length === 0" class="empty-tip">
            暂无匹配的图标
          </div>
          <div v-else class="icon-grid">
            <div
              v-for="item in filteredIcons"
              :key="item.name"
              class="icon-item"
              :class="{ 'is-selected': modelValue === item.name }"
              :title="item.label + ' (' + item.name + ')'"
              @click="handleSelect(item.name)"
            >
              <div class="icon-icon-wrap">
                <SvgIcon :name="item.name" :size="20" :color="modelValue === item.name ? '#06b6d4' : '#94a3b8'" />
              </div>
              <span class="icon-name-text">{{ item.name }}</span>
            </div>
          </div>
        </div>

        <div class="panel-footer">
          <span class="hint-text">点击即可选中并应用至当前菜单</span>
          <span class="count-tag">共 {{ filteredIcons.length }} 个</span>
        </div>
      </div>
    </el-popover>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import SvgIcon from './SvgIcon.vue';

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  }
});

const emit = defineEmits(['update:modelValue', 'change']);

const popoverVisible = ref(false);
const searchKeyword = ref('');

// 系统预置标准工业物联网与后台管理高频图标
const ICON_LIST = [
  { name: 'activity', label: '仪表盘/全景监控/心跳', category: '监控' },
  { name: 'server', label: '工业驱动/接入服务器', category: '工业' },
  { name: 'cpu', label: '边缘计算/硬件网关设备', category: '工业' },
  { name: 'database', label: '数据监控/点位指标', category: '数据' },
  { name: 'layers', label: '物模型/Schema层级定义', category: '工业' },
  { name: 'refresh', label: '联动规则/自动化引擎', category: '自动化' },
  { name: 'bell', label: '告警中心/异常预警', category: '告警' },
  { name: 'file-text', label: '日志审计/运行记录', category: '系统' },
  { name: 'shield', label: '角色权限/安全授权', category: '安全' },
  { name: 'user', label: '用户管理/操作账号', category: '安全' },
  { name: 'menu', label: '菜单路由/功能导航', category: '系统' },
  { name: 'tag', label: '字典管理/常量标签', category: '系统' },
  { name: 'settings', label: '系统设置/参数配置', category: '系统' },
  { name: 'terminal', label: '设备终端/远程控制台', category: '工业' },
  { name: 'zap', label: '规则动作/快速执行', category: '自动化' },
  { name: 'monitor', label: '大屏展板/实时监视', category: '监控' },
  { name: 'folder', label: '功能目录/上级分类', category: '系统' },
  { name: 'tool', label: '现场调试/运维工具', category: '工业' },
  { name: 'key', label: '密钥管理/Token权限', category: '安全' },
  { name: 'link', label: '网络链路/通道通信', category: '工业' },
  { name: 'alert-triangle', label: '紧急警报/风险处置', category: '告警' },
  { name: 'clock', label: '定时任务/时间调度', category: '自动化' },
  { name: 'filter', label: '点位过滤/数据清洗', category: '数据' },
  { name: 'send', label: '下发指令/消息投递', category: '工业' },
  { name: 'lock', label: '加锁控制/安全防护', category: '安全' },
  { name: 'check-circle', label: '运行健康/正常状态', category: '通用' }
];

const filteredIcons = computed(() => {
  const kw = searchKeyword.value.trim().toLowerCase();
  if (!kw) return ICON_LIST;
  return ICON_LIST.filter(
    item => item.name.toLowerCase().includes(kw) || item.label.toLowerCase().includes(kw)
  );
});

function handleSelect(name) {
  emit('update:modelValue', name);
  emit('change', name);
  popoverVisible.value = false;
}

function handleClear() {
  emit('update:modelValue', '');
  emit('change', '');
}
</script>

<style scoped>
.icon-select-container {
  width: 100%;
}

.icon-trigger-input :deep(.el-input__wrapper) {
  cursor: pointer;
  background-color: #0d1527;
  box-shadow: 0 0 0 1px #1e293b inset;
}

.icon-trigger-input :deep(.el-input__inner) {
  cursor: pointer;
  color: #f1f5f9;
}

.icon-trigger-input :deep(.el-input-group__prepend) {
  background-color: #131d33;
  border-color: #1e293b;
  padding: 0 12px;
}

.icon-preview-box {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
}

.trigger-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.clear-btn {
  color: #94a3b8;
  font-size: 13px;
  line-height: 1;
  padding: 2px;
  cursor: pointer;
  transition: color 0.2s;
}

.clear-btn:hover {
  color: #ef4444;
}

.dropdown-arrow {
  color: #64748b;
  font-size: 10px;
  transition: transform 0.25s;
  user-select: none;
}

.dropdown-arrow.is-active {
  transform: rotate(180deg);
  color: #06b6d4;
}

.icon-picker-panel {
  padding: 4px;
}

.panel-header {
  margin-bottom: 10px;
}

.icon-grid-scroll {
  max-height: 260px;
  overflow-y: auto;
  padding-right: 2px;
}

.icon-grid-scroll::-webkit-scrollbar {
  width: 5px;
}

.icon-grid-scroll::-webkit-scrollbar-thumb {
  background: #334155;
  border-radius: 4px;
}

.icon-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
}

.icon-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 10px 4px;
  border-radius: 6px;
  background-color: #101a2f;
  border: 1px solid #1e293b;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.icon-item:hover {
  background-color: #162442;
  border-color: #06b6d4;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(6, 182, 212, 0.15);
}

.icon-item.is-selected {
  background-color: rgba(6, 182, 212, 0.12);
  border-color: #06b6d4;
  box-shadow: 0 0 0 1px #06b6d4 inset;
}

.icon-icon-wrap {
  margin-bottom: 6px;
}

.icon-name-text {
  font-size: 11px;
  color: #94a3b8;
  max-width: 80px;
  text-align: center;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.icon-item:hover .icon-name-text,
.icon-item.is-selected .icon-name-text {
  color: #38bdf8;
  font-weight: 500;
}

.empty-tip {
  text-align: center;
  padding: 24px 0;
  color: #64748b;
  font-size: 13px;
}

.panel-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 10px;
  padding-top: 8px;
  border-top: 1px solid #1e293b;
  font-size: 12px;
}

.hint-text {
  color: #64748b;
}

.count-tag {
  color: #38bdf8;
  font-family: monospace;
}
</style>
