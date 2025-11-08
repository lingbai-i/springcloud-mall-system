<template>
  <div class="merchant-finance-index">财务管理（占位页面）</div>
</template>

<script setup>
/**
 * 商家端财务管理视图（占位组件）
 * @author lingbai
 * @version 0.1.0
 * @since 2025-11-08T09:39:57+08:00
 */
import { onMounted } from 'vue'
import logger from '@/utils/logger'

onMounted(() => {
  logger.info('[MerchantFinanceIndex] mounted placeholder component')
})
</script>

<style scoped>
.merchant-finance-index { padding: 16px; }
</style>

