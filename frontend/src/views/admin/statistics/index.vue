<template>
  <div class="admin-statistics-index">数据统计（占位页面）</div>
</template>

<script setup>
/**
 * 管理员端数据统计视图（占位组件）
 * @author lingbai
 * @version 0.1.0
 * @since 2025-11-08T09:39:57+08:00
 */
import { onMounted } from 'vue'
import logger from '@/utils/logger'

onMounted(() => {
  logger.info('[AdminStatisticsIndex] mounted placeholder component')
})
</script>

<style scoped>
.admin-statistics-index { padding: 16px; }
</style>

