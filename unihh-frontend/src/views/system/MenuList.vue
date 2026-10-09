<!--
  Author: deversun
  Website: https://unihh.com
  License: Apache License 2.0
-->
<template>
  <div class="system-menu-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">系统菜单管理</h2>
        <p class="page-subtitle">集中维护系统导航菜单、页面路由与操作权限，实现平台功能界面的规范组织与访问控制</p>
      </div>
      <div class="header-actions">
        <el-button v-if="hasPermission('system:menu:add')" type="primary" @click="handleOpenMenuDialog('create', null)">
          <SvgIcon name="plus" :size="14" style="margin-right: 6px" />
          <span>新增顶级菜单</span>
        </el-button>
        <el-button @click="toggleExpandAll">
          <span>{{ isAllExpanded ? '折叠全部' : '展开全部' }}</span>
        </el-button>
        <el-button @click="loadMenus">
          <SvgIcon name="refresh" :size="14" style="margin-right: 6px" />
          <span>刷新</span>
        </el-button>
      </div>
    </div>

    <!-- 菜单动态路由表格 -->
    <div class="content-card">
      <el-table
        ref="menuTableRef"
        :data="menuTreeList"
        row-key="id"
        :default-expand-all="true"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        v-loading="loading"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="menuName" label="菜单名称" min-width="220">
          <template #default="{ row }">
            <div class="menu-name-cell">
              <SvgIcon :name="row.icon || 'menu'" :size="16" color="#06b6d4" />
              <span class="menu-title-text">{{ row.menuName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="icon" label="图标" width="70" align="center">
          <template #default="{ row }">
            <SvgIcon v-if="row.icon" :name="row.icon" :size="16" color="#38bdf8" />
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="70" align="center" />
        <el-table-column prop="menuType" label="类型" width="85" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.menuType === 'M'" type="warning" size="small">目录</el-tag>
            <el-tag v-else-if="row.menuType === 'C'" type="success" size="small">菜单</el-tag>
            <el-tag v-else type="info" size="small">按钮</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路由访问路径" min-width="160">
          <template #default="{ row }">
            <code v-if="row.path" class="code-path">{{ row.path }}</code>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="component" label="组件路径" min-width="210">
          <template #default="{ row }">
            <span class="component-text">{{ row.component || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="perms" label="权限标识符" min-width="160">
          <template #default="{ row }">
            <span v-if="row.perms" class="perm-tag">{{ row.perms }}</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="visible" label="显示" width="75" align="center">
          <template #default="{ row }">
            <el-tag :type="row.visible === 1 ? 'success' : 'info'" size="small">
              {{ row.visible === 1 ? '显示' : '隐藏' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="75" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="hasPermission('system:menu:add') || hasPermission('system:menu:edit')" label="操作" width="190" fixed="right" align="center">
          <template #default="{ row }">
            <el-button
              v-if="row.menuType !== 'F' && hasPermission('system:menu:add')"
              link
              type="primary"
              size="small"
              @click="handleOpenMenuDialog('create', row)"
            >
              新增子项
            </el-button>
            <el-button v-if="hasPermission('system:menu:edit')" link type="warning" size="small" @click="handleOpenMenuDialog('edit', row)">
              编辑
            </el-button>
            <el-button v-if="hasPermission('system:menu:edit')" link type="danger" size="small" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 菜单新增/编辑对话框 -->
    <el-dialog
      v-model="menuDialogVisible"
      :title="menuDialogMode === 'create' ? '新增菜单路由' : '编辑菜单路由'"
      width="600px"
      append-to-body
      destroy-on-close
    >
      <el-form ref="menuFormRef" :model="menuForm" :rules="menuRules" label-width="100px">
        <el-form-item label="上级菜单" prop="parentId">
          <el-tree-select
            v-model="menuForm.parentId"
            :data="parentSelectTree"
            :props="{ label: 'menuName', value: 'id', children: 'children' }"
            value-key="id"
            placeholder="请选择上级菜单 (不选则为顶级菜单/目录)"
            check-strictly
            clearable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="菜单类型" prop="menuType">
          <el-radio-group v-model="menuForm.menuType">
            <el-radio label="M">目录 (Directory)</el-radio>
            <el-radio label="C">菜单 (Menu)</el-radio>
            <el-radio label="F">按钮 (Action)</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="菜单名称" prop="menuName">
          <el-input v-model="menuForm.menuName" placeholder="例: 物理网关设备管理" />
        </el-form-item>
        <el-form-item v-if="menuForm.menuType !== 'F'" label="路由路径" prop="path">
          <el-input v-model="menuForm.path" placeholder="例: /devices 或 /system/users" />
        </el-form-item>
        <el-form-item v-if="menuForm.menuType === 'C'" label="组件路径" prop="component">
          <el-input v-model="menuForm.component" placeholder="例: views/devices/DeviceList.vue" />
        </el-form-item>
        <el-form-item label="权限标识" prop="perms">
          <el-input v-model="menuForm.perms" placeholder="例: system:user:list 或 device:control" />
        </el-form-item>
        <el-form-item v-if="menuForm.menuType !== 'F'" label="菜单图标" prop="icon">
          <IconSelect v-model="menuForm.icon" />
        </el-form-item>
        <el-form-item label="显示排序" prop="sortOrder">
          <el-input-number v-model="menuForm.sortOrder" :min="0" :max="999" controls-position="right" />
        </el-form-item>
        <el-form-item v-if="menuForm.menuType !== 'F'" label="导航显示">
          <el-radio-group v-model="menuForm.visible">
            <el-radio :label="1">显示在侧边栏</el-radio>
            <el-radio :label="0">隐藏</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="菜单状态">
          <el-radio-group v-model="menuForm.status">
            <el-radio :label="1">正常启用</el-radio>
            <el-radio :label="0">停用禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注说明">
          <el-input v-model="menuForm.remark" type="textarea" :rows="2" placeholder="菜单业务用途说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="menuDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="menuSubmitLoading" @click="handleSubmitMenu">
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
import IconSelect from '../../components/common/IconSelect.vue';
import { hasPermission } from '@/utils/permission';
import {
  getMenuTree,
  addMenu,
  updateMenu,
  deleteMenu
} from '../../api/menu';

const loading = ref(false);
const menuTreeList = ref([]);
const isAllExpanded = ref(true);
const menuTableRef = ref(null);

// 对话框表单
const menuDialogVisible = ref(false);
const menuDialogMode = ref('create');
const menuSubmitLoading = ref(false);
const menuFormRef = ref(null);

const menuForm = reactive({
  id: null,
  parentId: 0,
  menuName: '',
  path: '',
  component: '',
  icon: '',
  sortOrder: 1,
  menuType: 'C',
  perms: '',
  visible: 1,
  status: 1,
  remark: ''
});

const menuRules = {
  menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  path: [{ required: true, message: '请输入路由访问路径', trigger: 'blur' }],
  component: [{ required: true, message: '请输入前端组件路径', trigger: 'blur' }]
};

/**
 * 供上级菜单下拉选择的树形数据 (增加顶级根节点 0)
 */
const parentSelectTree = computed(() => {
  return [
    {
      id: 0,
      menuName: '主目录 (顶级菜单)',
      children: menuTreeList.value
    }
  ];
});

/**
 * 加载全量菜单树
 */
async function loadMenus() {
  loading.value = true;
  try {
    const res = await getMenuTree();
    menuTreeList.value = res || [];
  } catch (err) {
    ElMessage.error(err.message || '获取菜单列表失败');
  } finally {
    loading.value = false;
  }
}

/**
 * 展开/折叠表格全部层级
 */
function toggleExpandAll() {
  isAllExpanded.value = !isAllExpanded.value;
  function traverse(nodes) {
    for (const node of nodes) {
      menuTableRef.value?.toggleRowExpansion(node, isAllExpanded.value);
      if (node.children && node.children.length > 0) {
        traverse(node.children);
      }
    }
  }
  traverse(menuTreeList.value);
}

/**
 * 打开菜单新增/编辑弹窗
 */
function handleOpenMenuDialog(mode, parentOrRow = null) {
  menuDialogMode.value = mode;
  menuDialogVisible.value = true;

  if (mode === 'create') {
    const parentId = parentOrRow ? parentOrRow.id : 0;
    Object.assign(menuForm, {
      id: null,
      parentId: parentId,
      menuName: '',
      path: '',
      component: '',
      icon: '',
      sortOrder: (parentOrRow?.children?.length || 0) + 1,
      menuType: parentId === 0 ? 'M' : 'C',
      perms: '',
      visible: 1,
      status: 1,
      remark: ''
    });
  } else if (parentOrRow) {
    Object.assign(menuForm, {
      id: parentOrRow.id,
      parentId: parentOrRow.parentId || 0,
      menuName: parentOrRow.menuName,
      path: parentOrRow.path || '',
      component: parentOrRow.component || '',
      icon: parentOrRow.icon || '',
      sortOrder: parentOrRow.sortOrder !== undefined ? parentOrRow.sortOrder : 0,
      menuType: parentOrRow.menuType || 'C',
      perms: parentOrRow.perms || '',
      visible: parentOrRow.visible !== undefined ? parentOrRow.visible : 1,
      status: parentOrRow.status !== undefined ? parentOrRow.status : 1,
      remark: parentOrRow.remark || ''
    });
  }
}

/**
 * 提交菜单新增/编辑
 */
async function handleSubmitMenu() {
  if (!menuFormRef.value) return;
  await menuFormRef.value.validate(async (valid) => {
    if (!valid) return;
    menuSubmitLoading.value = true;
    try {
      if (menuDialogMode.value === 'create') {
        await addMenu(menuForm);
        ElMessage.success('新增菜单成功');
      } else {
        await updateMenu(menuForm);
        ElMessage.success('修改菜单成功');
      }
      menuDialogVisible.value = false;
      loadMenus();
    } catch (err) {
      ElMessage.error(err.message || '保存菜单失败');
    } finally {
      menuSubmitLoading.value = false;
    }
  });
}

/**
 * 删除菜单
 */
async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确定要删除菜单 "${row.menuName}" 吗？如果存在子菜单或按钮需先处理。`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );
    await deleteMenu(row.id);
    ElMessage.success('菜单已成功删除');
    loadMenus();
  } catch (action) {
    if (action !== 'cancel') {
      ElMessage.error(action?.message || '删除失败');
    }
  }
}

onMounted(() => {
  loadMenus();
});
</script>

<style scoped>
.system-menu-container {
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

.menu-name-cell {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
}

.menu-title-text {
  color: var(--iot-text-primary);
}

.code-path {
  background: rgba(6, 182, 212, 0.1);
  color: #38bdf8;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: monospace;
  font-size: 0.82rem;
}

.component-text {
  color: var(--iot-text-secondary);
  font-size: 0.82rem;
}

.perm-tag {
  color: #a78bfa;
  font-size: 0.8rem;
  background: rgba(167, 139, 250, 0.1);
  padding: 2px 6px;
  border-radius: 4px;
}

.text-muted {
  color: var(--iot-text-muted);
  font-size: 0.8rem;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
