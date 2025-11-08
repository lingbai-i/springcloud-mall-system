<template>
  <div class="merchant-shop-decoration">店铺装修（占位页面）</div>
</template>

<script setup>
/**
 * 商家端店铺装修视图（占位组件）
 * @author lingbai
 * @version 0.1.0
 * @since 2025-11-08T09:39:57+08:00
 */
import { onMounted } from 'vue'
import logger from '@/utils/logger'

onMounted(() => {
  logger.info('[MerchantShopDecoration] mounted placeholder component')
})
</script>

<style scoped>
.merchant-shop-decoration { padding: 16px; }
</style>

