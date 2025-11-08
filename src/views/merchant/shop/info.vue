<template>
  <div class="merchant-shop-info">
    店铺信息（占位页面）
  </div>
</template>

<script setup>
/**
 * 商家端店铺信息视图（占位组件）
 * 目的：解除路由引用缺失导致的依赖扫描失败。
 * @author lingbai
 * @version 0.1.0
 * @since 2025-11-08T09:35:29+08:00
 */
import { onMounted } from 'vue'
import logger from '@/utils/logger'

onMounted(() => {
  logger.info('[MerchantShopInfo] mounted placeholder component')
})
</script>

<style scoped>
.merchant-shop-info { padding: 16px; }
</style>

