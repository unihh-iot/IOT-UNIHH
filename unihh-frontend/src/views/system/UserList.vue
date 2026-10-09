<!--
  Author: deversun
  Website: https://unihh.com
  License: Apache License 2.0
-->
<template>
  <div class="system-user-container">
    <!-- 顶部标题与快速操作 -->
    <div class="page-header">
      <div>
        <h2 class="page-title">系统用户管理</h2>
        <p class="page-subtitle">维护平台操作人员账号、登录凭证、角色授权与安全状态</p>
      </div>
      <div class="header-actions">
        <el-button v-if="hasPermission('system:user:add')" type="primary" @click="handleOpenUserDialog('create')">
          <SvgIcon name="plus" :size="14" style="margin-right: 6px" />
          <span>新增用户</span>
        </el-button>
        <el-button @click="loadUsers">
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
            placeholder="用户名 / 姓名 / 手机号"
            clearable
            style="width: 220px"
            @keyup.enter="loadUsers"
          />
        </el-form-item>
        <el-form-item label="账号状态">
          <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 140px">
            <el-option label="正常启用" :value="1" />
            <el-option label="冻结停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadUsers">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 用户列表表格 -->
    <div class="content-card">
      <el-table :data="userList" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="username" label="登录账号" width="150">
          <template #default="{ row }">
            <div class="user-cell">
              <SvgIcon name="user" :size="16" color="#06b6d4" />
              <span class="user-name-text">{{ row.username }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="nickname" label="用户姓名/昵称" width="150" />
        <el-table-column label="分配角色" min-width="180">
          <template #default="{ row }">
            <el-tag
              v-for="role in (row.roles || [])"
              :key="role.id || role.roleKey"
              :type="role.roleKey === 'admin' ? 'danger' : 'primary'"
              size="small"
              class="role-tag-item"
            >
              {{ role.roleName || role.roleKey }}
            </el-tag>
            <span v-if="!row.roles || row.roles.length === 0" class="text-muted">未分配角色</span>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="联系手机" width="130">
          <template #default="{ row }">{{ row.phone || '-' }}</template>
        </el-table-column>
        <el-table-column prop="email" label="电子邮箱" min-width="170">
          <template #default="{ row }">{{ row.email || '-' }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              :disabled="row.id === 1 || !hasPermission('system:user:edit')"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginTime" label="最后登录时间" width="170">
          <template #default="{ row }">
            <span>{{ row.lastLoginTime || '从未登录' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="140">
          <template #default="{ row }">{{ row.remark || '-' }}</template>
        </el-table-column>
        <el-table-column v-if="hasPermission('system:user:edit')" label="操作" width="190" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleOpenUserDialog('edit', row)">
              编辑
            </el-button>
            <el-button link type="warning" size="small" @click="handleOpenResetPwd(row)">
              重置密码
            </el-button>
            <el-button
              link
              type="danger"
              size="small"
              :disabled="row.id === 1"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 用户新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? '新增系统用户' : '编辑系统用户'"
      width="560px"
      append-to-body
      destroy-on-close
    >
      <el-form ref="userFormRef" :model="userForm" :rules="userRules" label-width="95px">
        <el-form-item label="登录账号" prop="username">
          <el-input
            v-model="userForm.username"
            placeholder="请输入登录用户名 (字母/数字组合)"
            :disabled="dialogMode === 'edit'"
          />
        </el-form-item>
        <el-form-item label="用户姓名" prop="nickname">
          <el-input v-model="userForm.nickname" placeholder="请输入用户显示姓名或昵称" />
        </el-form-item>
        <el-form-item v-if="dialogMode === 'create'" label="登录密码" prop="password">
          <el-input
            v-model="userForm.password"
            type="password"
            show-password
            placeholder="建议包含大小写字母与数字，至少6位"
          />
        </el-form-item>
        <el-form-item label="分配角色" prop="roleIds">
          <el-select
            v-model="userForm.roleIds"
            multiple
            placeholder="请选择分配的角色 (可多选)"
            style="width: 100%"
          >
            <el-option
              v-for="role in roleOptions"
              :key="role.id"
              :label="role.roleName + ' (' + role.roleKey + ')'"
              :value="role.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="userForm.phone" placeholder="请输入手机号码" />
        </el-form-item>
        <el-form-item label="电子邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="请输入电子邮箱" />
        </el-form-item>
        <el-form-item label="账号状态">
          <el-radio-group v-model="userForm.status">
            <el-radio :label="1">正常启用</el-radio>
            <el-radio :label="0">冻结停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注信息">
          <el-input
            v-model="userForm.remark"
            type="textarea"
            :rows="2"
            placeholder="岗位职责、部门信息或授权备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="handleSubmitUser">
            确定保存
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 重置密码对话框 -->
    <el-dialog
      v-model="resetPwdVisible"
      title="重置用户密码"
      width="440px"
      append-to-body
      destroy-on-close
    >
      <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="90px">
        <el-form-item label="用户账号">
          <el-input :model-value="currentUserRow?.username" disabled />
        </el-form-item>
        <el-form-item label="新密码" prop="password">
          <el-input
            v-model="pwdForm.password"
            type="password"
            show-password
            placeholder="请输入至少6位新密码"
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="pwdForm.confirmPassword"
            type="password"
            show-password
            placeholder="请再次输入新密码"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="resetPwdVisible = false">取消</el-button>
          <el-button type="primary" :loading="resetPwdLoading" @click="handleConfirmResetPwd">
            确认重置
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import SvgIcon from '../../components/common/SvgIcon.vue';
import { hasPermission } from '@/utils/permission';
import {
  getUserList,
  addUser,
  updateUser,
  deleteUser,
  changeUserStatus,
  resetUserPassword
} from '../../api/user';
import { getRoleList } from '../../api/role';

const loading = ref(false);
const submitLoading = ref(false);
const resetPwdLoading = ref(false);
const userList = ref([]);
const roleOptions = ref([]);

const queryParams = reactive({
  keyword: '',
  status: null
});

// 对话框表单
const dialogVisible = ref(false);
const dialogMode = ref('create'); // 'create' | 'edit'
const userFormRef = ref(null);

const userForm = reactive({
  id: null,
  username: '',
  nickname: '',
  password: '',
  roleIds: [],
  phone: '',
  email: '',
  status: 1,
  remark: ''
});

const userRules = {
  username: [
    { required: true, message: '请输入登录用户名', trigger: 'blur' },
    { min: 3, max: 30, message: '长度需在 3 到 30 个字符之间', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入用户显示姓名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入初始密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度至少为 6 个字符', trigger: 'blur' }
  ]
};

// 重置密码
const resetPwdVisible = ref(false);
const currentUserRow = ref(null);
const pwdFormRef = ref(null);
const pwdForm = reactive({
  password: '',
  confirmPassword: ''
});

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== pwdForm.password) {
    callback(new Error('两次输入的密码不一致'));
  } else {
    callback();
  }
};

const pwdRules = {
  password: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度至少为 6 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
};

/**
 * 加载用户列表
 */
async function loadUsers() {
  loading.value = true;
  try {
    const res = await getUserList({
      keyword: queryParams.keyword || undefined,
      status: queryParams.status !== null && queryParams.status !== '' ? queryParams.status : undefined
    });
    userList.value = res || [];
  } catch (err) {
    ElMessage.error(err.message || '获取用户列表失败');
  } finally {
    loading.value = false;
  }
}

/**
 * 加载全部角色列表供下拉选择
 */
async function loadRoleOptions() {
  try {
    const res = await getRoleList();
    roleOptions.value = res || [];
  } catch (err) {
    console.warn('获取角色选项失败', err);
  }
}

function resetQuery() {
  queryParams.keyword = '';
  queryParams.status = null;
  loadUsers();
}

/**
 * 打开用户新增/编辑弹窗
 */
function handleOpenUserDialog(mode, row = null) {
  dialogMode.value = mode;
  dialogVisible.value = true;
  if (mode === 'create') {
    Object.assign(userForm, {
      id: null,
      username: '',
      nickname: '',
      password: '',
      roleIds: [],
      phone: '',
      email: '',
      status: 1,
      remark: ''
    });
  } else if (row) {
    Object.assign(userForm, {
      id: row.id,
      username: row.username,
      nickname: row.nickname,
      password: '',
      roleIds: row.roleIds || (row.roles ? row.roles.map(r => r.id) : []),
      phone: row.phone || '',
      email: row.email || '',
      status: row.status !== undefined ? row.status : 1,
      remark: row.remark || ''
    });
  }
}

/**
 * 提交新增/修改用户
 */
async function handleSubmitUser() {
  if (!userFormRef.value) return;
  await userFormRef.value.validate(async (valid) => {
    if (!valid) return;
    submitLoading.value = true;
    try {
      if (dialogMode.value === 'create') {
        await addUser(userForm);
        ElMessage.success('新增用户成功');
      } else {
        await updateUser(userForm);
        ElMessage.success('修改用户成功');
      }
      dialogVisible.value = false;
      loadUsers();
    } catch (err) {
      ElMessage.error(err.message || '保存失败');
    } finally {
      submitLoading.value = false;
    }
  });
}

/**
 * 快速修改状态
 */
async function handleStatusChange(row) {
  const targetStatus = row.status;
  const actionText = targetStatus === 1 ? '启用' : '停用';
  try {
    await changeUserStatus(row.id, targetStatus);
    ElMessage.success(`已${actionText}账号: ${row.username}`);
  } catch (err) {
    row.status = targetStatus === 1 ? 0 : 1;
    ElMessage.error(err.message || '修改状态失败');
  }
}

/**
 * 打开重置密码弹窗
 */
function handleOpenResetPwd(row) {
  currentUserRow.value = row;
  pwdForm.password = '';
  pwdForm.confirmPassword = '';
  resetPwdVisible.value = true;
}

/**
 * 确认重置密码
 */
async function handleConfirmResetPwd() {
  if (!pwdFormRef.value || !currentUserRow.value) return;
  await pwdFormRef.value.validate(async (valid) => {
    if (!valid) return;
    resetPwdLoading.value = true;
    try {
      await resetUserPassword(currentUserRow.value.id, pwdForm.password);
      ElMessage.success('密码重置成功');
      resetPwdVisible.value = false;
    } catch (err) {
      ElMessage.error(err.message || '密码重置失败');
    } finally {
      resetPwdLoading.value = false;
    }
  });
}

/**
 * 删除用户
 */
async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 "${row.nickname || row.username}" 吗？此操作不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );
    await deleteUser(row.id);
    ElMessage.success('用户已成功删除');
    loadUsers();
  } catch (action) {
    if (action !== 'cancel') {
      ElMessage.error(action?.message || '删除失败');
    }
  }
}

onMounted(() => {
  loadUsers();
  loadRoleOptions();
});
</script>

<style scoped>
.system-user-container {
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

.user-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-name-text {
  font-weight: 500;
  color: #38bdf8;
}

.role-tag-item {
  margin-right: 6px;
  margin-bottom: 2px;
}

.text-muted {
  color: var(--iot-text-muted);
  font-size: 0.82rem;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
