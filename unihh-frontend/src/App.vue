<template>
  <div v-if="isLoginRoute" class="login-page-container">
    <router-view />
  </div>
  <div v-else class="app-layout">
    <AppHeader />
    <div class="app-body">
      <AppSidebar />
      <main class="app-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from './components/layout/AppHeader.vue'
import AppSidebar from './components/layout/AppSidebar.vue'

const route = useRoute()
const isLoginRoute = computed(() => {
  return route.path === '/login' || route.meta?.isPublic || window.location.pathname === '/login'
})
</script>

<style scoped>
.app-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.app-body {
  display: flex;
  flex: 1;
}

.app-content {
  flex: 1;
  padding: 24px;
  background-color: var(--iot-bg-primary);
  overflow-y: auto;
}
</style>
