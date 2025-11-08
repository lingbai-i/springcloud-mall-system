<template>
  <div class="admin-merchants-detail">
    商家详情（占位页面）
  </div>
</template>

<script setup>
/**
 * 管理员端商家详情视图（占位组件）
 * @author lingbai
 * @version 0.1.0
 * @since 2025-11-08T09:35:29+08:00
 */
import { onMounted } from 'vue'
import logger from '@/utils/logger'

onMounted(() => {
  logger.info('[AdminMerchantsDetail] mounted placeholder component')
})
</script>

<style scoped>
.admin-merchants-detail { padding: 16px; }
</style>

