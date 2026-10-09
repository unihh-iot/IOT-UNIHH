<!--
  Author: deversun
  Website: https://unihh.com
  License: Apache License 2.0
-->
<template>
  <div class="system-dict-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">系统字典管理</h2>
        <p class="page-subtitle">统一维护平台常量、系统枚举与工业点位状态字典</p>
      </div>
      <div class="header-actions">
        <el-button v-if="hasPermission('system:dict:add')" type="primary" @click="handleOpenTypeDialog('create')">
          <SvgIcon name="plus" :size="14" style="margin-right: 6px" />
          <span>新增字典类型</span>
        </el-button>
        <el-button @click="loadTypes">
          <SvgIcon name="refresh" :size="14" style="margin-right: 6px" />
          <span>刷新</span>
        </el-button>
      </div>
    </div>

    <!-- 左右布局：左侧字典类型，右侧对应键值明细 -->
    <div class="dict-layout">
      <!-- 左侧字典类型列表 -->
      <div class="content-card type-panel">
        <div class="panel-header">
          <h3 class="panel-title">字典类型列表</h3>
          <el-input
            v-model="typeKeyword"
            placeholder="搜索名称 / 编码"
            size="small"
            clearable
            style="width: 150px"
          />
        </div>
        <div class="type-list" v-loading="typesLoading">
          <div
            v-for="type in filteredTypes"
            :key="type.dictType"
            class="type-item"
            :class="{ active: currentType?.dictType === type.dictType }"
            @click="selectType(type)"
          >
            <div class="type-main">
              <span class="type-name">{{ type.dictName }}</span>
              <code class="type-code">{{ type.dictType }}</code>
            </div>
            <div class="type-actions">
              <el-tag :type="type.status === 1 ? 'success' : 'info'" size="small">
                {{ type.status === 1 ? '启用' : '停用' }}
              </el-tag>
              <div v-if="hasPermission('system:dict:edit')" class="action-icons" @click.stop>
                <el-button link type="primary" size="small" @click="handleOpenTypeDialog('edit', type)">
                  编辑
                </el-button>
                <el-button link type="danger" size="small" @click="handleDeleteType(type)">
                  删除
                </el-button>
              </div>
            </div>
          </div>
          <div v-if="filteredTypes.length === 0" class="empty-text">
            暂无匹配字典类型
          </div>
        </div>
      </div>

      <!-- 右侧字典键值明细 -->
      <div class="content-card data-panel">
        <div class="panel-header">
          <div>
            <h3 class="panel-title">
              字典键值明细:
              <span v-if="currentType">{{ currentType.dictName }} ({{ currentType.dictType }})</span>
              <span v-else class="text-muted">请选择左侧字典类型</span>
            </h3>
            <p v-if="currentType" class="panel-subtitle">{{ currentType.remark || '无备注信息' }}</p>
          </div>
          <el-button
            v-if="hasPermission('system:dict:add')"
            type="primary"
            size="small"
            :disabled="!currentType"
            @click="handleOpenDataDialog('create')"
          >
            <SvgIcon name="plus" :size="12" style="margin-right: 4px" />
            <span>新增键值</span>
          </el-button>
        </div>

        <el-table :data="dictDataList" v-loading="dataLoading" stripe style="width: 100%">
          <el-table-column prop="sortOrder" label="排序" width="70" align="center" />
          <el-table-column prop="dictLabel" label="字典标签" min-width="160">
            <template #default="{ row }">
              <span class="label-text">{{ row.dictLabel }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="dictValue" label="字典键值" min-width="130">
            <template #default="{ row }">
              <code class="val-code">{{ row.dictValue }}</code>
            </template>
          </el-table-column>
          <el-table-column label="表格回显预览" width="140" align="center">
            <template #default="{ row }">
              <el-tag :type="row.listClass || 'primary'" size="small">
                {{ row.dictLabel }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="isDefault" label="默认" width="80" align="center">
            <template #default="{ row }">
              <el-tag :type="row.isDefault === 1 ? 'warning' : 'info'" size="small">
                {{ row.isDefault === 1 ? '是' : '否' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="80" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                {{ row.status === 1 ? '正常' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注说明" min-width="160">
            <template #default="{ row }">{{ row.remark || '-' }}</template>
          </el-table-column>
          <el-table-column v-if="hasPermission('system:dict:edit')" label="操作" width="140" fixed="right" align="center">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="handleOpenDataDialog('edit', row)">
                编辑
              </el-button>
              <el-button link type="danger" size="small" @click="handleDeleteData(row)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- 字典类型新增/编辑对话框 -->
    <el-dialog
      v-model="typeDialogVisible"
      :title="typeDialogMode === 'create' ? '新增字典类型' : '编辑字典类型'"
      width="480px"
      append-to-body
      destroy-on-close
    >
      <el-form ref="typeFormRef" :model="typeForm" :rules="typeRules" label-width="95px">
        <el-form-item label="字典名称" prop="dictName">
          <el-input v-model="typeForm.dictName" placeholder="例: 设备通信状态" />
        </el-form-item>
        <el-form-item label="类型编码" prop="dictType">
          <el-input
            v-model="typeForm.dictType"
            placeholder="例: sys_device_status (唯一字符)"
            :disabled="typeDialogMode === 'edit'"
          />
        </el-form-item>
        <el-form-item label="类型状态">
          <el-radio-group v-model="typeForm.status">
            <el-radio :label="1">正常启用</el-radio>
            <el-radio :label="0">停用禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注说明">
          <el-input v-model="typeForm.remark" type="textarea" :rows="2" placeholder="类型定义说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="typeDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="typeSubmitLoading" @click="handleSubmitType">
            确定保存
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 字典键值新增/编辑对话框 -->
    <el-dialog
      v-model="dataDialogVisible"
      :title="dataDialogMode === 'create' ? '新增字典键值' : '编辑字典键值'"
      width="520px"
      append-to-body
      destroy-on-close
    >
      <el-form ref="dataFormRef" :model="dataForm" :rules="dataRules" label-width="95px">
        <el-form-item label="字典类型">
          <el-input :model-value="dataForm.dictType" disabled />
        </el-form-item>
        <el-form-item label="字典标签" prop="dictLabel">
          <el-input v-model="dataForm.dictLabel" placeholder="例: 在线运行" />
        </el-form-item>
        <el-form-item label="字典键值" prop="dictValue">
          <el-input v-model="dataForm.dictValue" placeholder="例: 1 或 ONLINE" />
        </el-form-item>
        <el-form-item label="回显样式">
          <el-select v-model="dataForm.listClass" placeholder="选择标签样式" style="width: 100%">
            <el-option label="默认蓝色 (primary)" value="primary" />
            <el-option label="成功绿色 (success)" value="success" />
            <el-option label="警告黄色 (warning)" value="warning" />
            <el-option label="危险红色 (danger)" value="danger" />
            <el-option label="中性灰色 (info)" value="info" />
          </el-select>
        </el-form-item>
        <el-form-item label="显示排序" prop="sortOrder">
          <el-input-number v-model="dataForm.sortOrder" :min="0" :max="999" controls-position="right" />
        </el-form-item>
        <el-form-item label="是否默认">
          <el-radio-group v-model="dataForm.isDefault">
            <el-radio :label="1">是</el-radio>
            <el-radio :label="0">否</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="dataForm.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注说明">
          <el-input v-model="dataForm.remark" type="textarea" :rows="2" placeholder="键值具体释义" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dataDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="dataSubmitLoading" @click="handleSubmitData">
            确定保存
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import SvgIcon from '../../components/common/SvgIcon.vue';
import { hasPermission } from '@/utils/permission';
import {
  getDictTypes,
  addDictType,
  updateDictType,
  deleteDictType,
  getDictDataByType,
  addDictData,
  updateDictData,
  deleteDictData
} from '../../api/dict';

const typesLoading = ref(false);
const dataLoading = ref(false);
const dictTypes = ref([]);
const currentType = ref(null);
const dictDataList = ref([]);
const typeKeyword = ref('');

// 字典类型表单
const typeDialogVisible = ref(false);
const typeDialogMode = ref('create');
const typeSubmitLoading = ref(false);
const typeFormRef = ref(null);
const typeForm = reactive({
  id: null,
  dictName: '',
  dictType: '',
  status: 1,
  remark: ''
});

const typeRules = {
  dictName: [{ required: true, message: '请输入字典名称', trigger: 'blur' }],
  dictType: [
    { required: true, message: '请输入字典类型编码', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '仅支持字母、数字与下划线', trigger: 'blur' }
  ]
};

// 字典数据表单
const dataDialogVisible = ref(false);
const dataDialogMode = ref('create');
const dataSubmitLoading = ref(false);
const dataFormRef = ref(null);
const dataForm = reactive({
  id: null,
  dictType: '',
  dictLabel: '',
  dictValue: '',
  sortOrder: 1,
  listClass: 'primary',
  isDefault: 0,
  status: 1,
  remark: ''
});

const dataRules = {
  dictLabel: [{ required: true, message: '请输入字典标签', trigger: 'blur' }],
  dictValue: [{ required: true, message: '请输入字典键值', trigger: 'blur' }]
};

const filteredTypes = computed(() => {
  if (!typeKeyword.value.trim()) return dictTypes.value;
  const kw = typeKeyword.value.trim().toLowerCase();
  return dictTypes.value.filter(
    t => t.dictName.toLowerCase().includes(kw) || t.dictType.toLowerCase().includes(kw)
  );
});

/**
 * 加载字典类型列表
 */
async function loadTypes() {
  typesLoading.value = true;
  try {
    const res = await getDictTypes();
    dictTypes.value = res || [];
    if (dictTypes.value.length > 0) {
      if (!currentType.value || !dictTypes.value.find(t => t.dictType === currentType.value.dictType)) {
        selectType(dictTypes.value[0]);
      } else {
        // 保持选中
        const refreshed = dictTypes.value.find(t => t.dictType === currentType.value.dictType);
        selectType(refreshed);
      }
    } else {
      currentType.value = null;
      dictDataList.value = [];
    }
  } catch (err) {
    ElMessage.error(err.message || '获取字典类型失败');
  } finally {
    typesLoading.value = false;
  }
}

/**
 * 选中字典类型并加载对应键值
 */
async function selectType(type) {
  currentType.value = type;
  if (!type) {
    dictDataList.value = [];
    return;
  }
  dataLoading.value = true;
  try {
    const res = await getDictDataByType(type.dictType);
    dictDataList.value = res || [];
  } catch (err) {
    ElMessage.error(err.message || '获取字典键值失败');
  } finally {
    dataLoading.value = false;
  }
}

/**
 * 打开字典类型新增/修改弹窗
 */
function handleOpenTypeDialog(mode, row = null) {
  typeDialogMode.value = mode;
  typeDialogVisible.value = true;
  if (mode === 'create') {
    Object.assign(typeForm, {
      id: null,
      dictName: '',
      dictType: '',
      status: 1,
      remark: ''
    });
  } else if (row) {
    Object.assign(typeForm, {
      id: row.id,
      dictName: row.dictName,
      dictType: row.dictType,
      status: row.status !== undefined ? row.status : 1,
      remark: row.remark || ''
    });
  }
}

/**
 * 提交字典类型
 */
async function handleSubmitType() {
  if (!typeFormRef.value) return;
  await typeFormRef.value.validate(async (valid) => {
    if (!valid) return;
    typeSubmitLoading.value = true;
    try {
      if (typeDialogMode.value === 'create') {
        await addDictType(typeForm);
        ElMessage.success('新增字典类型成功');
      } else {
        await updateDictType(typeForm);
        ElMessage.success('修改字典类型成功');
      }
      typeDialogVisible.value = false;
      loadTypes();
    } catch (err) {
      ElMessage.error(err.message || '保存字典类型失败');
    } finally {
      typeSubmitLoading.value = false;
    }
  });
}

/**
 * 删除字典类型
 */
async function handleDeleteType(type) {
  try {
    await ElMessageBox.confirm(
      `确定要删除字典类型 "${type.dictName}" (${type.dictType}) 及其所有键值数据吗？`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );
    await deleteDictType(type.id);
    ElMessage.success('字典类型已成功删除');
    loadTypes();
  } catch (action) {
    if (action !== 'cancel') {
      ElMessage.error(action?.message || '删除失败');
    }
  }
}

/**
 * 打开字典键值新增/编辑弹窗
 */
function handleOpenDataDialog(mode, row = null) {
  dataDialogMode.value = mode;
  dataDialogVisible.value = true;
  if (mode === 'create') {
    Object.assign(dataForm, {
      id: null,
      dictType: currentType.value?.dictType || '',
      dictLabel: '',
      dictValue: '',
      sortOrder: (dictDataList.value.length || 0) + 1,
      listClass: 'primary',
      isDefault: 0,
      status: 1,
      remark: ''
    });
  } else if (row) {
    Object.assign(dataForm, {
      id: row.id,
      dictType: row.dictType,
      dictLabel: row.dictLabel,
      dictValue: row.dictValue,
      sortOrder: row.sortOrder !== undefined ? row.sortOrder : 0,
      listClass: row.listClass || 'primary',
      isDefault: row.isDefault !== undefined ? row.isDefault : 0,
      status: row.status !== undefined ? row.status : 1,
      remark: row.remark || ''
    });
  }
}

/**
 * 提交字典键值
 */
async function handleSubmitData() {
  if (!dataFormRef.value) return;
  await dataFormRef.value.validate(async (valid) => {
    if (!valid) return;
    dataSubmitLoading.value = true;
    try {
      if (dataDialogMode.value === 'create') {
        await addDictData(dataForm);
        ElMessage.success('新增字典键值成功');
      } else {
        await updateDictData(dataForm);
        ElMessage.success('修改字典键值成功');
      }
      dataDialogVisible.value = false;
      selectType(currentType.value);
    } catch (err) {
      ElMessage.error(err.message || '保存字典键值失败');
    } finally {
      dataSubmitLoading.value = false;
    }
  });
}

/**
 * 删除字典键值
 */
async function handleDeleteData(row) {
  try {
    await ElMessageBox.confirm(
      `确定要删除键值 "${row.dictLabel}" (${row.dictValue}) 吗？`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );
    await deleteDictData(row.id);
    ElMessage.success('键值已成功删除');
    selectType(currentType.value);
  } catch (action) {
    if (action !== 'cancel') {
      ElMessage.error(action?.message || '删除失败');
    }
  }
}

onMounted(() => {
  loadTypes();
});
</script>

<style scoped>
.system-dict-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  margin: 0;
  font-size: 1.5rem;
  color: var(--iot-text-primary);
}

.page-subtitle {
  margin: 4px 0 0 0;
  font-size: 0.875rem;
  color: var(--iot-text-secondary);
}

.header-actions {
  display: flex;
  gap: 12px;
}

.dict-layout {
  display: grid;
  grid-template-columns: 360px 1fr;
  gap: 18px;
}

.content-card {
  background-color: var(--iot-card-bg);
  border: 1px solid var(--iot-border-color);
  border-radius: var(--iot-radius);
  padding: 18px 20px;
}

.panel-title {
  margin: 0 0 4px 0;
  font-size: 1.05rem;
  color: var(--iot-text-primary);
}

.panel-subtitle {
  margin: 0;
  font-size: 0.82rem;
  color: var(--iot-text-secondary);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--iot-border-color);
}

.type-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: calc(100vh - 270px);
  overflow-y: auto;
  padding-right: 4px;
}

.type-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 14px;
  border-radius: 8px;
  border: 1px solid var(--iot-border-color);
  background: rgba(15, 23, 42, 0.4);
  cursor: pointer;
  transition: all 0.2s ease;
}

.type-item:hover {
  background: rgba(6, 182, 212, 0.06);
  border-color: rgba(6, 182, 212, 0.3);
}

.type-item.active {
  background: rgba(6, 182, 212, 0.12);
  border-color: #06b6d4;
}

.type-main {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
  min-width: 0;
}

.type-name {
  font-weight: 500;
  color: var(--iot-text-primary);
  font-size: 0.88rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.type-code {
  font-size: 0.76rem;
  color: #38bdf8;
  font-family: monospace;
}

.type-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
}

.action-icons {
  display: flex;
  gap: 4px;
}

.empty-text {
  padding: 30px 0;
  text-align: center;
  color: var(--iot-text-muted);
  font-size: 0.85rem;
}

.label-text {
  font-weight: 500;
  color: #f8fafc;
}

.val-code {
  font-family: monospace;
  background: rgba(255, 255, 255, 0.05);
  padding: 2px 6px;
  border-radius: 4px;
  color: #a78bfa;
}

.text-muted {
  color: var(--iot-text-muted);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
