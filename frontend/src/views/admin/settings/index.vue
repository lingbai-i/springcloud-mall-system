<template>
  <div class="admin-settings-index">系统设置（占位页面）</div>
</template>

<script setup>
/**
 * 管理员端系统设置视图（占位组件）
 * 说明：用于解除路由依赖，确保开发服务器可正常启动。
 * @author lingbai
 * @version 0.1.0
 * @since 2025-11-08T09:43:14+08:00
 */
import { onMounted } from 'vue'
import logger from '@/utils/logger'

onMounted(() => {
  // 占位组件挂载日志，便于追踪路由加载与页面生命周期
  logger.info('[AdminSettingsIndex] mounted placeholder component')
})
</script>

<style scoped>
.admin-settings-index { padding: 16px; }
</style>

