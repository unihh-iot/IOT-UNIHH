import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './assets/styles/global.css'

const app = createApp(App)
app.use(router)
app.use(ElementPlus)

// 必须等待路由就绪后再挂载应用，防止在登录页刷新时因初始路由尚未就绪而闪烁主页菜单
router.isReady().then(() => {
  app.mount('#app')
})
