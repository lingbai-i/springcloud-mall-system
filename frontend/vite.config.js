import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

/**
 * Vite配置文件
 * @author lingbai
 * @date 2025-10-21
 */
/**
 * 修改日志：
 * V1.2 2025-11-09T21:06:40+08:00：将代理目标临时改为 `http://localhost:8082`（不推荐）。
 * V1.3 2025-11-09T21:21:54+08:00：修正代理目标为本机网关 `http://localhost:8080`，与 start.bat/check-services.bat 保持一致；前端所有 `/api/*` 路径经网关转发到对应微服务（含短信服务 `/api/sms/send`）。
 * V1.4 2025-11-09T21:36:53+08:00：为本地开发新增 `/api/sms` 直连短信服务（端口 8083）的代理规则，并重写路径为 `/sms/*`；用于网关未启动时的本地联调。
 * @author lingbai
 */
export default defineConfig({
  plugins: [
    vue(),
    AutoImport({
      resolvers: [ElementPlusResolver()],
      imports: ['vue', 'vue-router', 'pinia'],
      dts: true
    }),
    Components({
      resolvers: [ElementPlusResolver()],
      dts: true
    })
  ],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    port: 5173,
    host: true,
    proxy: {
      '/api': {
        // 通过API网关访问所有服务
        // 本机开发：使用本地网关端口 8080；如使用Docker容器网络，请切换为容器服务名（gateway-service:8080）
        // 说明：统一让前端请求走网关，例如：/api/sms/send -> 网关 -> sms-service
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      // 本地直连短信服务（当网关未运行时使用）
      // 说明：Axios baseURL 为 /api，验证码接口实际为 /api/sms/send，因此此处对 /api/sms 做更具体的代理并重写为 /sms
      '/api/sms': {
        target: 'http://localhost:8089',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api\/sms/, '/sms')
      }
    },
    // 配置静态资源目录，使 /images 路径可以访问项目根目录的 images 文件夹
    fs: {
      allow: ['..']
    }
  },
  // 配置公共基础路径
  publicDir: 'public',
  // 解决dayjs插件问题
  define: {
    __VUE_PROD_DEVTOOLS__: false
  },
  // 解决dayjs插件问题
  optimizeDeps: {
    include: [
      'dayjs',
      'dayjs/plugin/advancedFormat',
      'dayjs/plugin/customParseFormat',
      'dayjs/plugin/localeData',
      'dayjs/plugin/dayOfYear'
    ]
  }
})
