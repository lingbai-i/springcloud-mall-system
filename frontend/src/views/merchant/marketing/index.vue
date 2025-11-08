<template>
  <div class="merchant-marketing-index">营销工具（占位页面）</div>
</template>

<script setup>
/**
 * 商家端营销工具视图（占位组件）
 * @author lingbai
 * @version 0.1.0
 * @since 2025-11-08T09:39:57+08:00
 */
import { onMounted } from 'vue'
import logger from '@/utils/logger'

onMounted(() => {
  logger.info('[MerchantMarketingIndex] mounted placeholder component')
})
</script>

<style scoped>
.merchant-marketing-index { padding: 16px; }
</style>

