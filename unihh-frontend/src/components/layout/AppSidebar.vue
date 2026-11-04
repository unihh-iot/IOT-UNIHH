<!--
  Author: deversun
  Website: https://unihh.com
  License: Apache License 2.0
-->
<template>
  <aside class="app-sidebar">
    <nav class="sidebar-menu">
      <template v-if="displayMenus.length > 0">
        <template v-for="menu in displayMenus" :key="menu.id || menu.path">
          <!-- 目录类型：包含子菜单 -->
          <template v-if="menu.menuType === 'M' || (menu.children && menu.children.length > 0)">
            <div class="menu-divider">
              <span>{{ menu.menuName }}</span>
            </div>
            <template v-for="sub in (menu.children || [])" :key="sub.id || sub.path">
              <router-link
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
            v-else
            :to="menu.path"
            class="menu-item"
            active-class="active"
          >
            <SvgIcon :name="menu.icon || 'activity'" :size="18" :color="getIconColor(menu.icon)" />
            <span>{{ menu.menuName }}</span>
          </router-link>
        </template>
      </template>

      <!-- 加载中骨架占位 -->
      <div v-else-if="loading" class="sidebar-skeleton">
        <div v-for="i in 6" :key="i" class="skeleton-item shimmer"></div>
      </div>

      <!-- 空菜单提示 -->
      <div v-else class="sidebar-empty">
        <span>暂无分配菜单</span>
      </div>
    </nav>
  </aside>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from "vue";
import SvgIcon from "../common/SvgIcon.vue";
import { getUserRoutes } from "../../api/menu";

const loading = ref(false);
const dynamicMenus = ref([]);

// 图标高亮配色映射 (未命中时优雅回退为统一高亮工业青蓝)
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
  menu: '#a78bfa',
  setting: '#14b8a6',
  pie: '#f97316'
};

function getIconColor(iconName) {
  return iconColorMap[iconName] || '#06b6d4';
}

/**
 * 结构化构建嵌套树型菜单 (兼容后端扁平数组或已构建树)
 */
function buildMenuTree(list) {
  if (!Array.isArray(list) || list.length === 0) return [];

  // 若返回的元素已经包含嵌套 children 结构
  const hasNested = list.some(item => Array.isArray(item.children) && item.children.length > 0);
  if (hasNested) {
    return list;
  }

  const map = new Map();
  const roots = [];

  list.forEach(item => {
    map.set(item.id, { ...item, children: [] });
  });

  list.forEach(item => {
    const node = map.get(item.id);
    const parentId = item.parentId;
    if (parentId && map.has(parentId)) {
      map.get(parentId).children.push(node);
    } else {
      roots.push(node);
    }
  });

  return roots;
}

/**
 * 递归过滤展示菜单 (过滤隐藏、禁用及按钮权限项)
 */
const displayMenus = computed(() => {
  const filterList = (list) => {
    if (!Array.isArray(list)) return [];
    const result = [];
    for (const item of list) {
      // 过滤未显示 (visible === 0)、停用 (status === 0) 或按钮类型 (menuType === 'F')
      if (item.visible === 0 || item.status === 0 || item.menuType === 'F') {
        continue;
      }
      const cloned = { ...item };
      if (Array.isArray(cloned.children) && cloned.children.length > 0) {
        cloned.children = filterList(cloned.children);
      }
      // 如果为目录类型 (M)，但子项全为空或无权限，则不展示空目录分割条
      if (cloned.menuType === 'M' && (!cloned.children || cloned.children.length === 0)) {
        continue;
      }
      result.push(cloned);
    }
    // 按 sortOrder 排序
    return result.sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0));
  };

  return filterList(dynamicMenus.value);
});

/**
 * 从本地存储优先读取当前登录会话菜单 (避免页面初载刷新白屏闪烁)
 */
function loadCachedMenus() {
  try {
    const raw = localStorage.getItem('unihh_menus');
    if (raw) {
      const parsed = JSON.parse(raw);
      if (Array.isArray(parsed) && parsed.length > 0) {
        dynamicMenus.value = buildMenuTree(parsed);
      }
    }
  } catch (e) {
    console.warn('[AppSidebar] 解析本地缓存菜单失败', e);
  }
}

/**
 * 从后端 API 动态获取当前登录用户的菜单路由树
 */
async function fetchUserRoutes() {
  if (dynamicMenus.value.length === 0) {
    loading.value = true;
  }
  try {
    const res = await getUserRoutes();
    if (Array.isArray(res) && res.length > 0) {
      dynamicMenus.value = buildMenuTree(res);
      localStorage.setItem('unihh_menus', JSON.stringify(res));
    }
  } catch (err) {
    console.warn('[AppSidebar] 调取后端动态菜单接口失败', err);
  } finally {
    loading.value = false;
  }
}

function handleMenuUpdate() {
  fetchUserRoutes();
}

function handleStorageChange(e) {
  if (e.key === 'unihh_menus') {
    loadCachedMenus();
  }
}

onMounted(() => {
  loadCachedMenus();
  fetchUserRoutes();
  window.addEventListener('unihh:menu-updated', handleMenuUpdate);
  window.addEventListener('storage', handleStorageChange);
});

onUnmounted(() => {
  window.removeEventListener('unihh:menu-updated', handleMenuUpdate);
  window.removeEventListener('storage', handleStorageChange);
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

.sidebar-skeleton {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 6px 4px;
}

.skeleton-item {
  height: 38px;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.04);
}

.shimmer {
  animation: pulse 1.6s infinite ease-in-out;
}

@keyframes pulse {
  0% { opacity: 0.3; }
  50% { opacity: 0.7; }
  100% { opacity: 0.3; }
}

.sidebar-empty {
  padding: 30px 12px;
  text-align: center;
  font-size: 0.85rem;
  color: var(--iot-text-muted);
}
</style>
