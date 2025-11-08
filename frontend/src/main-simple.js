import { createApp } from 'vue'

import App from './App-simple.vue'

/**
 * Vue3应用程序入口文件 - 简化版本
 * 不使用Element Plus，避免dayjs插件问题
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */

// 创建Vue应用实例
const app = createApp(App)

// 挂载应用到DOM
app.mount('#app')

// 开发环境下的调试信息
if (import.meta.env.DEV) {
  console.log('🚀 在线商城前端应用启动成功 (简化版)')
  console.log('📦 Vue版本:', app.version)
  console.log('🛠️ 开发模式已启用')
}