/*
 * Copyright (c) 2026 unihh.com All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Website: https://unihh.com
 * Author: deversun
 */

import { createRouter, createWebHistory } from 'vue-router'
import OverviewDashboard from '../views/dashboard/OverviewDashboard.vue'
import DeviceList from '../views/devices/DeviceList.vue'
import PointDataMonitor from '../views/data/PointDataMonitor.vue'
import ProtocolDrivers from '../views/protocol/ProtocolDrivers.vue'
import ThingModelManager from '../views/thingmodels/ThingModelManager.vue'
import RuleManager from '../views/rules/RuleManager.vue'
import AlarmEvents from '../views/alarms/AlarmEvents.vue'
import LogManager from '../views/logs/LogManager.vue'

// 基础路由与页面配置
export const constantRoutes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/auth/Login.vue'),
    meta: { title: '用户登录', isPublic: true }
  },
  { path: '/', redirect: '/dashboard' },
  { path: '/dashboard', name: 'Dashboard', component: OverviewDashboard, meta: { title: '系统仪表盘' } },
  { path: '/drivers', name: 'Drivers', component: ProtocolDrivers, meta: { title: '驱动管理' } },
  { path: '/devices', name: 'Devices', component: DeviceList, meta: { title: '设备管理' } },
  { path: '/data', name: 'Data', component: PointDataMonitor, meta: { title: '数据监控' } },
  { path: '/thingmodels', name: 'ThingModels', component: ThingModelManager, meta: { title: '物模型管理' } },
  { path: '/rules', name: 'Rules', component: RuleManager, meta: { title: '规则引擎' } },
  { path: '/alarms', name: 'Alarms', component: AlarmEvents, meta: { title: '告警中心' } },
  { path: '/logs', name: 'Logs', component: LogManager, meta: { title: '日志审计' } },
  // 系统管理模块路由 (对应 sys_menu 动态路由组件)
  { path: '/system/users', name: 'SystemUsers', component: () => import('../views/system/UserList.vue'), meta: { title: '用户管理' } },
  { path: '/system/roles', name: 'SystemRoles', component: () => import('../views/system/RoleList.vue'), meta: { title: '角色权限' } },
  { path: '/system/menus', name: 'SystemMenus', component: () => import('../views/system/MenuList.vue'), meta: { title: '菜单路由' } },
  { path: '/system/dicts', name: 'SystemDicts', component: () => import('../views/system/DictList.vue'), meta: { title: '字典管理' } }
]

const router = createRouter({
  history: createWebHistory(),
  routes: constantRoutes
})

// 免登录白名单路由
const whiteList = ['/login']

// 全局路由导航守卫 (权限拦截与登录保持检测)
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('unihh_token')

  if (token) {
    // 已登录状态访问登录页，直接跳转至仪表盘
    if (to.path === '/login') {
      next({ path: '/dashboard' })
    } else {
      next()
    }
  } else {
    // 未登录状态
    if (whiteList.includes(to.path)) {
      next()
    } else {
      // 携带重定向目标地址跳转到登录页
      next(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
    }
  }
})

export default router
