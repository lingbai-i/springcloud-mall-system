/**
 * 自动化测试：验证登录页与验证码按钮加载
 * 设计说明：使用 Playwright 访问 `/auth/login`，断言“获取验证码”按钮可见。
 * 运行前置：需执行 `npx playwright install` 安装浏览器。
 * @author lingbai
 * @version 0.1.0
 * @since 2025-11-08T09:47:38+08:00
 */
import { test, expect } from '@playwright/test'

// 使用环境变量端口，默认 5173/5174 兼容开发态
const DEV_PORT = process.env.VITE_PORT || '5174'
const BASE_URL = `http://localhost:${DEV_PORT}`

test('登录页应展示“获取验证码”按钮', async ({ page }) => {
  // WHY: 直接导航到路由以绕过首页交互，减少干扰因素
  await page.goto(`${BASE_URL}/auth/login`, { waitUntil: 'domcontentloaded' })

  // HOW: 通过包含文本的按钮断言，提高选择器鲁棒性
  const getCodeButton = page.getByRole('button', { name: /获取验证码/ })

  // WHEN: 页面渲染完成后进行断言
  await expect(getCodeButton).toBeVisible()
})

