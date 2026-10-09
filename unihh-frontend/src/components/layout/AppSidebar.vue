<!--
  Author: deversun
  Website: https://unihh.com
  License: Apache License 2.0
-->
<template>
  <aside class="app-sidebar">
    <nav class="sidebar-menu">
      <template v-for="menu in displayMenus" :key="menu.id || menu.path">
        <!-- 目录类型：包含子菜单 -->
        <template v-if="menu.menuType === 'M' || (menu.children && menu.children.length > 0)">
          <div class="menu-divider">
            <span>{{ menu.menuName }}</span>
          </div>
          <template v-for="sub in (menu.children || [])" :key="sub.id || sub.path">
            <router-link
              v-if="sub.visible !== 0 && sub.status !== 0"
              :to="sub.path"
              class="menu-item sub-item"
              active-class="active"
            >
              <SvgIcon :name="sub.icon || 'menu'" :size="17" :color="getIconColor(sub.icon)" />
              <span>{{ sub.menuName }}</span>
            </router-link>
          </template>
        </template>

        <!-- 常规菜单项 (无子项) -->
        <router-link
          v-else-if="menu.visible !== 0 && menu.status !== 0"
          :to="menu.path"
          class="menu-item"
          active-class="active"
        >
          <SvgIcon :name="menu.icon || 'activity'" :size="18" :color="getIconColor(menu.icon)" />
          <span>{{ menu.menuName }}</span>
        </router-link>
      </template>
    </nav>
  </aside>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import SvgIcon from "../common/SvgIcon.vue";
import { getUserRoutes } from "../../api/menu";

// 基础回退菜单路由 (未连接后台或初始化时兜底)
const fallbackMenus = [
  { id: 1, menuName: '系统仪表盘', path: '/dashboard', icon: 'activity', menuType: 'C', visible: 1, status: 1 },
  { id: 2, menuName: '驱动管理', path: '/drivers', icon: 'server', menuType: 'C', visible: 1, status: 1 },
  { id: 3, menuName: '设备管理', path: '/devices', icon: 'cpu', menuType: 'C', visible: 1, status: 1 },
  { id: 4, menuName: '数据管理', path: '/data', icon: 'database', menuType: 'C', visible: 1, status: 1 },
  { id: 5, menuName: '物模型管理', path: '/thingmodels', icon: 'database', menuType: 'C', visible: 1, status: 1 },
  { id: 6, menuName: '规则管理', path: '/rules', icon: 'refresh', menuType: 'C', visible: 1, status: 1 },
  { id: 7, menuName: '告警中心', path: '/alarms', icon: 'bell', menuType: 'C', visible: 1, status: 1 },
  { id: 8, menuName: '日志管理', path: '/logs', icon: 'file-text', menuType: 'C', visible: 1, status: 1 },
  {
    id: 9,
    menuName: '系统与权限配置',
    path: '/system',
    menuType: 'M',
    visible: 1,
    status: 1,
    children: [
      { id: 10, menuName: '用户管理', path: '/system/users', icon: 'user', menuType: 'C', visible: 1, status: 1 },
      { id: 11, menuName: '角色权限', path: '/system/roles', icon: 'shield', menuType: 'C', visible: 1, status: 1 },
      { id: 12, menuName: '菜单路由', path: '/system/menus', icon: 'menu', menuType: 'C', visible: 1, status: 1 },
      { id: 13, menuName: '字典管理', path: '/system/dicts', icon: 'file-text', menuType: 'C', visible: 1, status: 1 }
    ]
  }
];

const dynamicMenus = ref([]);

const displayMenus = computed(() => {
  return dynamicMenus.value.length > 0 ? dynamicMenus.value : fallbackMenus;
});

const iconColorMap = {
  activity: '#06b6d4',
  server: '#38bdf8',
  cpu: '#10b981',
  database: '#8b5cf6',
  refresh: '#ef4444',
  bell: '#f59e0b',
  'file-text': '#3b82f6',
  user: '#06b6d4',
  shield: '#38bdf8',
  menu: '#a78bfa'
};

function getIconColor(iconName) {
  return iconColorMap[iconName] || '';
}

/**
 * 从后端动态获取当前登录用户已分配的菜单路由树
 */
async function fetchUserRoutes() {
  try {
    const res = await getUserRoutes();
    if (Array.isArray(res) && res.length > 0) {
      dynamicMenus.value = res;
    }
  } catch (err) {
    console.warn('[AppSidebar] 动态菜单获取降级使用本地配置', err);
  }
}

onMounted(() => {
  fetchUserRoutes();
});
</script>

<style scoped>
.app-sidebar {
  width: 230px;
  background-color: var(--iot-bg-secondary);
  border-right: 1px solid var(--iot-border-color);
  padding: 20px 12px;
  display: flex;
  flex-direction: column;
}

.sidebar-menu {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 11px 16px;
  border-radius: 6px;
  color: var(--iot-text-secondary);
  font-size: 0.92rem;
  font-weight: 500;
  transition: all 0.2s ease;
}

.menu-item:hover {
  background-color: rgba(255, 255, 255, 0.04);
  color: var(--iot-text-primary);
}

.menu-item.active {
  background-color: rgba(6, 182, 212, 0.15);
  color: var(--iot-color-cyan);
  border-left: 3px solid var(--iot-color-cyan);
}

.sub-item {
  padding-left: 20px;
  font-size: 0.88rem;
}

.menu-divider {
  margin: 14px 0 6px 0;
  padding: 0 16px;
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--iot-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  padding-top: 12px;
}
</style>
