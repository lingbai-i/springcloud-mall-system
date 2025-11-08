<template>
  <div class="merchant-analytics-index">
    数据分析（占位页面）
  </div>
</template>

<script setup>
/**
 * 商家端数据分析视图（占位组件）
 * @author lingbai
 * @version 0.1.0
 * @since 2025-11-08T09:35:29+08:00
 */
import { onMounted } from 'vue'
import logger from '@/utils/logger'

onMounted(() => {
  logger.info('[MerchantAnalyticsIndex] mounted placeholder component')
})
</script>

<style scoped>
.merchant-analytics-index { padding: 16px; }
</style>

