<template>
  <div class="admin-settings-index">
    系统设置（占位页面）
  </div>
</template>

<script setup>
/**
 * 管理员端系统设置视图（占位组件）
 * 用途：解除路由依赖，确保开发服务器正常启动。
 * @author lingbai
 * @version 0.1.0
 * @since 2025-11-08T09:38:33+08:00
 */
import { onMounted } from 'vue'
import logger from '@/utils/logger'

onMounted(() => {
  logger.info('[AdminSettingsIndex] mounted placeholder component')
})
</script>

<style scoped>
.admin-settings-index { padding: 16px; }
</style>

