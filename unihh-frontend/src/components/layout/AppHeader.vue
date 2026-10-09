<template>
  <header class="app-header">
    <div class="header-left">
      <SvgIcon name="server" :size="22" color="#06b6d4" />
      <span class="brand-title">UNIHH-IOT 物联网管理平台</span>
    </div>
    <div class="header-right">
      <el-dropdown trigger="hover" placement="bottom-end" popper-class="user-dropdown-popper">
        <div class="user-info-badge">
          <SvgIcon name="user" :size="16" color="#06b6d4" />
          <span class="user-name">{{ currentRoleName || currentUser.nickname || '超级管理员' }}</span>
          <span class="dropdown-arrow"></span>
        </div>
        <template #dropdown>
          <el-dropdown-menu class="user-dropdown-menu">
            <el-dropdown-item @click="handleLogout" class="dropdown-logout-item">
              <SvgIcon name="log-out" :size="15" color="#ef4444" />
              <span>退出登录</span>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import SvgIcon from '../common/SvgIcon.vue'
import { logout } from '../../api/auth'

const router = useRouter()

const currentUser = computed(() => {
  try {
    const raw = localStorage.getItem('unihh_user')
    return raw ? JSON.parse(raw) : { username: 'admin', nickname: '超级管理员' }
  } catch (e) {
    return { username: 'admin', nickname: '超级管理员' }
  }
})

const currentRoleName = computed(() => {
  try {
    const roles = JSON.parse(localStorage.getItem('unihh_roles') || '[]')
    if (roles.includes('admin')) return '超级管理员'
    if (roles.includes('operator')) return '工控运维员'
    return roles[0] || '系统用户'
  } catch (e) {
    return '超级管理员'
  }
})

function handleLogout() {
  ElMessageBox.confirm('确认要退出当前登录状态吗？', '安全退出提示', {
    confirmButtonText: '确定退出',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await logout()
    } catch (ignored) {}
    localStorage.removeItem('unihh_token')
    localStorage.removeItem('unihh_user')
    localStorage.removeItem('unihh_roles')
    localStorage.removeItem('unihh_perms')
    ElMessage.success('已安全退出登录')
    router.push('/login')
  }).catch(() => {})
}
</script>

<style scoped>
.app-header {
  height: 60px;
  background-color: var(--iot-bg-secondary);
  border-bottom: 1px solid var(--iot-border-color);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.brand-title {
  font-size: 1.15rem;
  font-weight: 600;
  letter-spacing: 0.5px;
  background: linear-gradient(90deg, #f8fafc, #06b6d4);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.header-right {
  display: flex;
  align-items: center;
  font-size: 0.875rem;
}

.user-info-badge {
  display: flex;
  align-items: center;
  gap: 8px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid var(--iot-border-color);
  padding: 6px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.25s ease;
  user-select: none;
}

.user-info-badge:hover {
  background: rgba(6, 182, 212, 0.08);
  border-color: rgba(6, 182, 212, 0.4);
}

.user-name {
  color: var(--iot-text-primary);
  font-weight: 500;
  font-size: 0.85rem;
}

.dropdown-arrow {
  width: 0;
  height: 0;
  border-left: 4px solid transparent;
  border-right: 4px solid transparent;
  border-top: 5px solid #94a3b8;
  margin-left: 2px;
  transition: transform 0.25s ease, border-top-color 0.25s ease;
}

.user-info-badge:hover .dropdown-arrow {
  transform: rotate(180deg);
  border-top-color: #06b6d4;
}
</style>
