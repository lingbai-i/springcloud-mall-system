<template>
  <div class="merchant-orders-after-sales">
    售后处理（占位页面）
  </div>
</template>

<script setup>
/**
 * 商家端售后处理视图（占位组件）
 * 目的：保证路由可用，便于联调与开发预览。
 * @author lingbai
 * @version 0.1.0
 * @since 2025-11-08T09:35:29+08:00
 */
import { onMounted } from 'vue'
import logger from '@/utils/logger'

onMounted(() => {
  logger.info('[MerchantOrdersAfterSales] mounted placeholder component')
})
</script>

<style scoped>
.merchant-orders-after-sales { padding: 16px; }
</style>

