import { defineClientConfig } from 'vuepress/client'
import TechBackground from './components/TechBackground.vue'
import UniHome from './components/UniHome.vue'

export default defineClientConfig({
  enhance({ app }) {
    app.component('TechBackground', TechBackground)
    app.component('UniHome', UniHome)
  },
  rootComponents: [TechBackground],
})
