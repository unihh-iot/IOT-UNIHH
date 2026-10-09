<!--
  Author: deversun
  Website: https://unihh.com
  License: Apache License 2.0
-->
<template>
  <div class="system-role-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">系统角色与权限管理</h2>
        <p class="page-subtitle">配置平台角色身份 (Role) 与细粒度功能权限 (Permission)，支持设备反控、物模型与规则授权</p>
      </div>
      <div class="header-actions">
        <el-button v-if="hasPermission('system:role:add')" type="primary" @click="handleOpenRoleDialog('create')">
          <SvgIcon name="plus" :size="14" style="margin-right: 6px" />
          <span>新增角色</span>
        </el-button>
        <el-button @click="loadRoles">
          <SvgIcon name="refresh" :size="14" style="margin-right: 6px" />
          <span>刷新</span>
        </el-button>
      </div>
    </div>

    <!-- 搜索筛选栏 -->
    <div class="content-card filter-card">
      <el-form :inline="true" :model="queryParams" class="filter-form">
        <el-form-item label="关键字">
          <el-input
            v-model="queryParams.keyword"
            placeholder="角色名称 / 权限标识字符"
            clearable
            style="width: 220px"
            @keyup.enter="loadRoles"
          />
        </el-form-item>
        <el-form-item label="角色状态">
          <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 140px">
            <el-option label="正常启用" :value="1" />
            <el-option label="停用冻结" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadRoles">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 角色列表卡片 -->
    <div class="content-card">
      <el-table :data="roleList" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="roleName" label="角色名称" width="180">
          <template #default="{ row }">
            <div class="role-name-cell">
              <SvgIcon name="shield" :size="16" color="#06b6d4" />
              <span class="role-name-text">{{ row.roleName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="roleKey" label="权限标识字符" width="160">
          <template #default="{ row }">
            <el-tag :type="row.roleKey === 'admin' ? 'danger' : 'primary'" size="small">
              {{ row.roleKey }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="显示排序" width="90" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              :disabled="row.roleKey === 'admin' || row.id === 1 || !hasPermission('system:role:edit')"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="permsCount" label="分配功能权限" width="140" align="center">
          <template #default="{ row }">
            <span class="badge-count">{{ row.permsCount || 0 }} 项权限</span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注说明" min-width="200">
          <template #default="{ row }">{{ row.remark || '-' }}</template>
        </el-table-column>
        <el-table-column v-if="hasPermission('system:role:edit')" label="操作" width="220" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleOpenRoleDialog('edit', row)">
              编辑
            </el-button>
            <el-button link type="success" size="small" @click="handleOpenAssignDialog(row)">
              权限分配
            </el-button>
            <el-button
              link
              type="danger"
              size="small"
              :disabled="row.roleKey === 'admin' || row.id === 1"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 角色新增/编辑对话框 -->
    <el-dialog
      v-model="roleDialogVisible"
      :title="roleDialogMode === 'create' ? '新增系统角色' : '编辑系统角色'"
      width="500px"
      append-to-body
      destroy-on-close
    >
      <el-form ref="roleFormRef" :model="roleForm" :rules="roleRules" label-width="95px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="roleForm.roleName" placeholder="例: 工业运维专员" />
        </el-form-item>
        <el-form-item label="权限字符" prop="roleKey">
          <el-input
            v-model="roleForm.roleKey"
            placeholder="例: operator, viewer (全局唯一字符)"
            :disabled="roleDialogMode === 'edit' && (roleForm.roleKey === 'admin' || roleForm.id === 1)"
          />
        </el-form-item>
        <el-form-item label="显示排序" prop="sortOrder">
          <el-input-number v-model="roleForm.sortOrder" :min="0" :max="999" controls-position="right" />
        </el-form-item>
        <el-form-item label="角色状态">
          <el-radio-group v-model="roleForm.status" :disabled="roleForm.roleKey === 'admin'">
            <el-radio :label="1">正常启用</el-radio>
            <el-radio :label="0">停用冻结</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注说明">
          <el-input
            v-model="roleForm.remark"
            type="textarea"
            :rows="3"
            placeholder="角色职责、允许反控范围与权限说明"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="roleDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="roleSubmitLoading" @click="handleSubmitRole">
            确定保存
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 角色权限与菜单分配综合对话框 -->
    <el-dialog
      v-model="assignDialogVisible"
      :title="'分配权限与菜单 - ' + (currentRoleRow?.roleName || '')"
      width="680px"
      append-to-body
      destroy-on-close
    >
      <el-tabs v-model="activeAssignTab" class="assign-tabs">
        <!-- 选项卡 1: 功能权限分配 -->
        <el-tab-pane label="功能权限分配" name="permissions">
          <div class="tab-toolbar">
            <span class="tab-tip">勾选该角色具备的操作权限（细粒度鉴权拦截）</span>
            <div>
              <el-button size="small" @click="handleSelectAllPerms(true)">全选</el-button>
              <el-button size="small" @click="handleSelectAllPerms(false)">清空</el-button>
            </div>
          </div>
          <div class="perms-group-list">
            <div v-for="(group, module) in groupedPermissions" :key="module" class="module-group-card">
              <div class="module-group-header">
                <span class="module-title">{{ module }}</span>
                <el-button link type="primary" size="small" @click="toggleModulePerms(module)">
                  全选/反选本模块
                </el-button>
              </div>
              <div class="module-perms-items">
                <el-checkbox-group v-model="selectedPermIds">
                  <el-checkbox
                    v-for="perm in group"
                    :key="perm.id"
                    :label="perm.id"
                    class="perm-checkbox-item"
                  >
                    <span>{{ perm.permName }}</span>
                    <span class="perm-key-hint">({{ perm.permKey }})</span>
                  </el-checkbox>
                </el-checkbox-group>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 选项卡 2: 菜单路由分配 -->
        <el-tab-pane label="菜单路由分配" name="menus">
          <div class="tab-toolbar">
            <span class="tab-tip">勾选该角色在左侧导航栏与页面中可见的菜单路由</span>
            <div>
              <el-button size="small" @click="handleExpandAllMenus(true)">展开全部</el-button>
              <el-button size="small" @click="handleExpandAllMenus(false)">折叠全部</el-button>
            </div>
          </div>
          <div class="menu-tree-wrapper">
            <el-tree
              ref="menuTreeRef"
              :data="menuTreeData"
              show-checkbox
              node-key="id"
              :props="{ label: 'menuName', children: 'children' }"
              default-expand-all
              highlight-current
              check-strictly
            >
              <template #default="{ data }">
                <div class="tree-node-cell">
                  <SvgIcon :name="data.icon || 'menu'" :size="15" color="#06b6d4" />
                  <span class="tree-node-title">{{ data.menuName }}</span>
                  <el-tag v-if="data.menuType === 'M'" size="small" type="warning" class="tree-tag">目录</el-tag>
                  <el-tag v-else-if="data.menuType === 'C'" size="small" type="success" class="tree-tag">菜单</el-tag>
                  <el-tag v-else size="small" type="info" class="tree-tag">按钮</el-tag>
                  <code class="tree-path">{{ data.path }}</code>
                </div>
              </template>
            </el-tree>
          </div>
        </el-tab-pane>
      </el-tabs>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="assignDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="assignSubmitLoading" @click="handleSubmitAssign">
            保存生效
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
  getRoleList,
  addRole,
  updateRole,
  deleteRole,
  changeRoleStatus,
  getAllPermissions,
  getRolePermissionIds,
  updateRolePermissions,
  getRoleMenuIds,
  updateRoleMenus
} from '../../api/role';
import { getMenuTree } from '../../api/menu';

const loading = ref(false);
const roleList = ref([]);

const queryParams = reactive({
  keyword: '',
  status: null
});

// 角色编辑弹窗
const roleDialogVisible = ref(false);
const roleDialogMode = ref('create');
const roleSubmitLoading = ref(false);
const roleFormRef = ref(null);

const roleForm = reactive({
  id: null,
  roleName: '',
  roleKey: '',
  sortOrder: 1,
  status: 1,
  remark: ''
});

const roleRules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleKey: [
    { required: true, message: '请输入角色权限标识', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_-]+$/, message: '仅支持字母、数字、下划线及减号', trigger: 'blur' }
  ]
};

// 权限分配弹窗
const assignDialogVisible = ref(false);
const activeAssignTab = ref('permissions');
const assignSubmitLoading = ref(false);
const currentRoleRow = ref(null);

const allPermissions = ref([]);
const selectedPermIds = ref([]);

const menuTreeData = ref([]);
const menuTreeRef = ref(null);

/**
 * 将全部权限按业务模块分组展示
 */
const groupedPermissions = computed(() => {
  const map = {};
  for (const p of allPermissions.value) {
    const mod = p.module || '通用系统功能';
    if (!map[mod]) map[mod] = [];
    map[mod].push(p);
  }
  return map;
});

/**
 * 加载角色列表
 */
async function loadRoles() {
  loading.value = true;
  try {
    const res = await getRoleList({
      keyword: queryParams.keyword || undefined,
      status: queryParams.status !== null && queryParams.status !== '' ? queryParams.status : undefined
    });
    roleList.value = res || [];
  } catch (err) {
    ElMessage.error(err.message || '获取角色列表失败');
  } finally {
    loading.value = false;
  }
}

/**
 * 加载全部功能权限列表与菜单树字典
 */
async function loadSystemPermsAndMenus() {
  try {
    const [permsRes, menusRes] = await Promise.all([
      getAllPermissions(),
      getMenuTree()
    ]);
    allPermissions.value = permsRes || [];
    menuTreeData.value = menusRes || [];
  } catch (err) {
    console.warn('获取权限与菜单元数据失败', err);
  }
}

function resetQuery() {
  queryParams.keyword = '';
  queryParams.status = null;
  loadRoles();
}

/**
 * 打开角色新增/修改对话框
 */
function handleOpenRoleDialog(mode, row = null) {
  roleDialogMode.value = mode;
  roleDialogVisible.value = true;
  if (mode === 'create') {
    Object.assign(roleForm, {
      id: null,
      roleName: '',
      roleKey: '',
      sortOrder: (roleList.value.length || 0) + 1,
      status: 1,
      remark: ''
    });
  } else if (row) {
    Object.assign(roleForm, {
      id: row.id,
      roleName: row.roleName,
      roleKey: row.roleKey,
      sortOrder: row.sortOrder !== undefined ? row.sortOrder : 0,
      status: row.status !== undefined ? row.status : 1,
      remark: row.remark || ''
    });
  }
}

/**
 * 提交角色新增/修改
 */
async function handleSubmitRole() {
  if (!roleFormRef.value) return;
  await roleFormRef.value.validate(async (valid) => {
    if (!valid) return;
    roleSubmitLoading.value = true;
    try {
      if (roleDialogMode.value === 'create') {
        await addRole(roleForm);
        ElMessage.success('新增角色成功');
      } else {
        await updateRole(roleForm);
        ElMessage.success('修改角色成功');
      }
      roleDialogVisible.value = false;
      loadRoles();
    } catch (err) {
      ElMessage.error(err.message || '保存角色失败');
    } finally {
      roleSubmitLoading.value = false;
    }
  });
}

/**
 * 修改角色状态
 */
async function handleStatusChange(row) {
  const targetStatus = row.status;
  const actionText = targetStatus === 1 ? '启用' : '停用';
  try {
    await changeRoleStatus(row.id, targetStatus);
    ElMessage.success(`已${actionText}角色: ${row.roleName}`);
  } catch (err) {
    row.status = targetStatus === 1 ? 0 : 1;
    ElMessage.error(err.message || '修改状态失败');
  }
}

/**
 * 打开分配权限/菜单弹窗
 */
async function handleOpenAssignDialog(row) {
  currentRoleRow.value = row;
  activeAssignTab.value = 'permissions';
  assignDialogVisible.value = true;

  try {
    const [permIds, menuIds] = await Promise.all([
      getRolePermissionIds(row.id),
      getRoleMenuIds(row.id)
    ]);
    selectedPermIds.value = permIds || [];
    setTimeout(() => {
      if (menuTreeRef.value) {
        menuTreeRef.value.setCheckedKeys(menuIds || []);
      }
    }, 100);
  } catch (err) {
    ElMessage.error(err.message || '获取分配数据失败');
  }
}

/**
 * 全选/清空所有权限
 */
function handleSelectAllPerms(select) {
  if (select) {
    selectedPermIds.value = allPermissions.value.map(p => p.id);
  } else {
    selectedPermIds.value = [];
  }
}

/**
 * 模块内全选/反选
 */
function toggleModulePerms(module) {
  const group = groupedPermissions.value[module] || [];
  const groupIds = group.map(p => p.id);
  const isAllIncluded = groupIds.every(id => selectedPermIds.value.includes(id));

  if (isAllIncluded) {
    selectedPermIds.value = selectedPermIds.value.filter(id => !groupIds.includes(id));
  } else {
    const set = new Set([...selectedPermIds.value, ...groupIds]);
    selectedPermIds.value = Array.from(set);
  }
}

/**
 * 展开/折叠全部菜单
 */
function handleExpandAllMenus(expand) {
  if (!menuTreeRef.value) return;
  const nodes = menuTreeRef.value.store.nodesMap;
  for (const key in nodes) {
    nodes[key].expanded = expand;
  }
}

/**
 * 提交分配保存
 */
async function handleSubmitAssign() {
  if (!currentRoleRow.value) return;
  assignSubmitLoading.value = true;
  try {
    const roleId = currentRoleRow.value.id;
    // 1. 保存功能权限
    await updateRolePermissions(roleId, selectedPermIds.value);

    // 2. 保存菜单路由
    const checkedMenuKeys = menuTreeRef.value ? menuTreeRef.value.getCheckedKeys() : [];
    await updateRoleMenus(roleId, checkedMenuKeys);

    ElMessage.success(`角色 [${currentRoleRow.value.roleName}] 权限与菜单分配已成功生效`);
    assignDialogVisible.value = false;
    loadRoles();
  } catch (err) {
    ElMessage.error(err.message || '保存分配失败');
  } finally {
    assignSubmitLoading.value = false;
  }
}

/**
 * 删除角色
 */
async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确定要删除角色 "${row.roleName}" (${row.roleKey}) 吗？此操作不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );
    await deleteRole(row.id);
    ElMessage.success('角色已成功删除');
    loadRoles();
  } catch (action) {
    if (action !== 'cancel') {
      ElMessage.error(action?.message || '删除失败');
    }
  }
}

onMounted(() => {
  loadRoles();
  loadSystemPermsAndMenus();
});
</script>

<style scoped>
.system-role-container {
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

.content-card {
  background-color: var(--iot-card-bg);
  border: 1px solid var(--iot-border-color);
  border-radius: var(--iot-radius);
  padding: 18px 20px;
}

.filter-card {
  padding-bottom: 2px;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.role-name-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.role-name-text {
  font-weight: 500;
  color: #f8fafc;
}

.badge-count {
  background: rgba(6, 182, 212, 0.1);
  color: #38bdf8;
  padding: 2px 8px;
  border-radius: 9999px;
  font-size: 0.8rem;
  font-weight: 500;
}

.assign-tabs {
  margin-top: -10px;
}

.tab-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  margin-bottom: 12px;
  border-bottom: 1px solid var(--iot-border-color);
}

.tab-tip {
  font-size: 0.85rem;
  color: var(--iot-text-secondary);
}

.perms-group-list {
  max-height: 400px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding-right: 6px;
}

.module-group-card {
  border: 1px solid var(--iot-border-color);
  background: rgba(15, 23, 42, 0.4);
  border-radius: 6px;
  padding: 12px 14px;
}

.module-group-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  padding-bottom: 6px;
  border-bottom: 1px dashed rgba(255, 255, 255, 0.08);
}

.module-title {
  font-weight: 600;
  color: var(--iot-color-cyan);
  font-size: 0.9rem;
}

.module-perms-items {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.perm-checkbox-item {
  margin-right: 16px;
  margin-bottom: 6px;
}

.perm-key-hint {
  color: var(--iot-text-muted);
  font-size: 0.78rem;
  margin-left: 4px;
}

.menu-tree-wrapper {
  max-height: 400px;
  overflow-y: auto;
  border: 1px solid var(--iot-border-color);
  border-radius: 6px;
  padding: 12px;
  background: rgba(15, 23, 42, 0.4);
}

.tree-node-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.88rem;
}

.tree-node-title {
  font-weight: 500;
  color: var(--iot-text-primary);
}

.tree-tag {
  font-size: 0.75rem;
}

.tree-path {
  font-size: 0.78rem;
  color: #38bdf8;
  background: rgba(6, 182, 212, 0.1);
  padding: 1px 6px;
  border-radius: 4px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
