<template>
  <div class="admin-orders-refunds">
    退款处理（占位页面）
  </div>
</template>

<script setup>
/**
 * 管理员端退款处理视图（占位组件）
 * 用途：解除路由依赖，确保开发服务器正常启动。
 * @author lingbai
 * @version 0.1.0
 * @since 2025-11-08T09:38:33+08:00
 */
import { onMounted } from 'vue'
import logger from '@/utils/logger'

onMounted(() => {
  logger.info('[AdminOrdersRefunds] mounted placeholder component')
})
</script>

<style scoped>
.admin-orders-refunds { padding: 16px; }
</style>

