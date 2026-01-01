import { createApp } from 'vue'
import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
// 导入Element Plus完整组件库
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'

import App from './App.vue'
import router from './router'
import { useUserStore } from '@/stores/user'
import '@/styles/index.scss'

/**
 * @Description: 项目入口文件 - 百物语微服务商城前端
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */

// 创建Vue应用实例
const app = createApp(App)

// 创建Pinia状态管理实例
const pinia = createPinia()
// 添加持久化插件
pinia.use(piniaPluginPersistedstate)

// 注册Element Plus图标组件
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 使用插件和库
app.use(pinia)
app.use(router)
app.use(ElementPlus, {
  locale: zhCn,
})

// 挂载应用到DOM
app.mount('#app')

// 初始化用户状态
const userStore = useUserStore()
userStore.initUserState()

// 启动跨标签页状态同步（退出登录时同步所有标签页）
userStore.startStorageSync()

// 开发环境下的调试信息
if (import.meta.env.DEV) {
  console.log('🚀 在线商城前端应用启动成功')
  console.log('📦 Vue版本:', app.version)
  console.log('🛠️ 开发模式已启用')
  console.log('🔄 跨标签页状态同步已启用')
}