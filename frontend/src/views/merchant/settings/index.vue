<template>
  <!-- 简单占位页面，避免路由导入缺失导致 Vite 构建失败 -->
  <div class="merchant-settings">
    <h1>商家设置</h1>
    <p>此为占位页面，后续可替换为真实设置界面。</p>
  </div>
</template>

<script setup>
/**
 * 商家设置页面占位组件
 *
 * 设计目的：
 * - 修复前端启动错误：路由引用 '@/views/merchant/settings/index.vue' 文件不存在
 * - 提供最小可渲染组件以确保 Vite 开发服务器能够成功启动
 *
 * 后续扩展：
 * - 替换为实际的商家设置表单与逻辑
 * - 集成状态管理与 API 接口
 */

// 当前占位组件无业务逻辑，仅用于消除构建时缺失文件错误
</script>

<style scoped>
.merchant-settings {
  padding: 24px;
}

h1 {
  font-size: 20px;
  margin-bottom: 8px;
}

p {
  color: #666;
}
</style>