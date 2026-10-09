<!--
  Author: deversun
  Website: https://unihh.com
  License: Apache License 2.0
-->
<template>
  <div class="login-container">
    <!-- 背景几何科技粒子与网格装饰 -->
    <div class="cyber-grid"></div>
    <div class="glow-orb orb-1"></div>
    <div class="glow-orb orb-2"></div>

    <div class="login-box">
      <!-- 平台品牌标识 -->
      <div class="brand-section">
        <div class="logo-wrapper">
          <SvgIcon name="server" :size="36" color="#06b6d4" />
        </div>
        <h1 class="brand-name">UNIHH-IOT</h1>
        <p class="brand-subtitle">工业物联网智能感知与边缘控制平台</p>
      </div>

      <!-- 登录表单卡片 -->
      <div class="form-card">
        <div class="card-header">
          <h2 class="card-title">系统账号安全登录</h2>
          <p class="card-desc">请输入凭据以访问工业边缘设备与监控控制台</p>
        </div>

        <el-alert
          v-if="errorMessage"
          :title="errorMessage"
          type="error"
          show-icon
          :closable="true"
          @close="errorMessage = ''"
          class="login-alert"
        />

        <el-form :model="form" :rules="rules" ref="loginFormRef" class="login-form" @keyup.enter="handleLogin">
          <!-- 登录名 -->
          <el-form-item prop="username">
            <el-input
              v-model="form.username"
              placeholder="请输入登录用户名 (如 admin)"
              size="large"
              clearable
              :prefix-icon="renderUserIcon"
            >
              <template #prefix>
                <SvgIcon name="user" :size="18" color="#06b6d4" />
              </template>
            </el-input>
          </el-form-item>

          <!-- 密码 -->
          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              :type="showPassword ? 'text' : 'password'"
              placeholder="请输入登录密码"
              size="large"
              clearable
            >
              <template #prefix>
                <SvgIcon name="lock" :size="18" color="#06b6d4" />
              </template>
              <template #suffix>
                <div class="pwd-toggle" @click="showPassword = !showPassword">
                  <SvgIcon :name="showPassword ? 'eye-off' : 'eye'" :size="18" color="#94a3b8" />
                </div>
              </template>
            </el-input>
          </el-form-item>

          <!-- 记住我与辅助选项 -->
          <div class="form-options">
            <el-checkbox v-model="form.rememberMe" class="remember-checkbox">
              <span class="checkbox-label">记住我 (保持账号密码)</span>
            </el-checkbox>
          </div>

          <!-- 登录按钮 -->
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="submit-btn"
            @click="handleLogin"
          >
            <span v-if="!loading">立 即 登 录</span>
            <span v-else>安全身份鉴权中...</span>
          </el-button>
        </el-form>
      </div>

      <!-- 底部安全说明 -->
      <footer class="login-footer">
        <p>Copyright © 2026 UNIHH. All Rights Reserved. Apache License 2.0</p>
      </footer>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import SvgIcon from '../../components/common/SvgIcon.vue'
import { login } from '../../api/auth'

const router = useRouter()
const route = useRoute()

const loginFormRef = ref(null)
const loading = ref(false)
const showPassword = ref(false)
const errorMessage = ref('')

const form = reactive({
  username: '',
  password: '',
  rememberMe: false
})

const rules = {
  username: [
    { required: true, message: '请输入登录用户名', trigger: 'blur' },
    { min: 3, max: 32, message: '用户名长度在 3 到 32 个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入登录密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度不得少于 6 位', trigger: 'blur' }
  ]
}

const REMEMBER_ME_KEY = 'unihh_remember_me'
const REMEMBER_USER_KEY = 'unihh_remember_username'
const REMEMBER_PWD_KEY = 'unihh_remember_pwd'

onMounted(() => {
  // 检查是否保持了"记住我"凭据
  const savedRemember = localStorage.getItem(REMEMBER_ME_KEY) === 'true'
  if (savedRemember) {
    const savedUsername = localStorage.getItem(REMEMBER_USER_KEY)
    const savedPassword = localStorage.getItem(REMEMBER_PWD_KEY)
    if (savedUsername) form.username = savedUsername
    if (savedPassword) form.password = savedPassword
    form.rememberMe = true
  }
})

async function handleLogin() {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    errorMessage.value = ''

    try {
      const res = await login({
        username: form.username,
        password: form.password,
        rememberMe: form.rememberMe
      })

      // 存储 Remember Me 状态
      if (form.rememberMe) {
        localStorage.setItem(REMEMBER_ME_KEY, 'true')
        localStorage.setItem(REMEMBER_USER_KEY, form.username)
        localStorage.setItem(REMEMBER_PWD_KEY, form.password)
      } else {
        localStorage.removeItem(REMEMBER_ME_KEY)
        localStorage.removeItem(REMEMBER_USER_KEY)
        localStorage.removeItem(REMEMBER_PWD_KEY)
      }

      // 存储 Token 与登录用户信息
      const token = res.token || 'demo_token_' + Date.now()
      localStorage.setItem('unihh_token', token)
      localStorage.setItem('unihh_user', JSON.stringify(res.user || { username: form.username, nickname: form.username }))
      localStorage.setItem('unihh_roles', JSON.stringify(res.roles || []))
      localStorage.setItem('unihh_perms', JSON.stringify(res.permissions || []))
      localStorage.setItem('unihh_menus', JSON.stringify(res.menus || []))

      ElMessage.success(`登录成功！欢迎回来，${(res.user && res.user.nickname) || form.username}`)

      // 页面重定向
      const redirectUrl = route.query.redirect || '/dashboard'
      router.push(redirectUrl)
    } catch (err) {
      console.error('[登录异常]', err)
      errorMessage.value = err.message || '登录校验失败，请检查账号密码或后端微服务状态'
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #070d19;
  position: relative;
  overflow: hidden;
  padding: 24px;
}

/* 科技感网格背景 */
.cyber-grid {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: 
    linear-gradient(rgba(6, 182, 212, 0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(6, 182, 212, 0.05) 1px, transparent 1px);
  background-size: 40px 40px;
  pointer-events: none;
}

/* 渐变发光球 */
.glow-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(100px);
  pointer-events: none;
  opacity: 0.35;
}

.orb-1 {
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, #06b6d4 0%, rgba(6, 182, 212, 0) 70%);
  top: -150px;
  left: -150px;
}

.orb-2 {
  width: 450px;
  height: 450px;
  background: radial-gradient(circle, #3b82f6 0%, rgba(59, 130, 246, 0) 70%);
  bottom: -120px;
  right: -120px;
}

.login-box {
  position: relative;
  z-index: 10;
  width: 100%;
  max-width: 460px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 品牌头部 */
.brand-section {
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.logo-wrapper {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  background: rgba(6, 182, 212, 0.1);
  border: 1px solid rgba(6, 182, 212, 0.3);
  box-shadow: 0 0 25px rgba(6, 182, 212, 0.25);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 4px;
}

.brand-name {
  margin: 0;
  font-size: 2rem;
  font-weight: 700;
  letter-spacing: 2px;
  background: linear-gradient(135deg, #f8fafc 30%, #06b6d4 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.brand-subtitle {
  margin: 0;
  color: var(--iot-text-secondary);
  font-size: 0.95rem;
  font-weight: 400;
}

/* 卡片样式 */
.form-card {
  background: rgba(30, 41, 59, 0.85);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  padding: 32px 28px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.5), 0 0 1px 1px rgba(6, 182, 212, 0.15);
}

.card-header {
  margin-bottom: 24px;
  text-align: left;
}

.card-title {
  margin: 0 0 6px 0;
  font-size: 1.25rem;
  font-weight: 600;
  color: #f8fafc;
}

.card-desc {
  margin: 0;
  font-size: 0.85rem;
  color: var(--iot-text-secondary);
}

.login-alert {
  margin-bottom: 20px;
}

.login-form :deep(.el-input__wrapper) {
  background-color: rgba(15, 23, 42, 0.8) !important;
  border: 1px solid #334155;
  box-shadow: none !important;
  padding: 6px 14px;
  border-radius: 8px;
  transition: all 0.25s ease;
}

.login-form :deep(.el-input__wrapper:hover) {
  border-color: #475569;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  border-color: #06b6d4 !important;
  box-shadow: 0 0 0 1px #06b6d4, 0 0 12px rgba(6, 182, 212, 0.25) !important;
}

.login-form :deep(.el-input__inner) {
  color: #f8fafc;
  font-size: 0.95rem;
}

.pwd-toggle {
  cursor: pointer;
  display: flex;
  align-items: center;
  padding: 4px;
}

.form-options {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  font-size: 0.875rem;
}

.remember-checkbox :deep(.el-checkbox__label) {
  color: #cbd5e1;
  font-size: 0.875rem;
}

.remember-checkbox :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #06b6d4;
  border-color: #06b6d4;
}

.submit-btn {
  width: 100%;
  height: 44px;
  font-size: 1rem;
  font-weight: 600;
  letter-spacing: 1px;
  border: none;
  background: linear-gradient(135deg, #06b6d4 0%, #0284c7 100%);
  border-radius: 8px;
  box-shadow: 0 4px 15px rgba(6, 182, 212, 0.35);
  transition: all 0.25s ease;
}

.submit-btn:hover {
  background: linear-gradient(135deg, #22d3ee 0%, #0369a1 100%);
  box-shadow: 0 6px 20px rgba(6, 182, 212, 0.5);
  transform: translateY(-1px);
}

.submit-btn:active {
  transform: translateY(0);
}

/* 底部声明 */
.login-footer {
  text-align: center;
  font-size: 0.78rem;
  color: var(--iot-text-muted);
  line-height: 1.6;
}
</style>
