<template>
  <div class="merchant-products-edit">编辑商品（占位页面）</div>
</template>

<script setup>
/**
 * 商家端编辑商品视图（占位组件）
 * @author lingbai
 * @version 0.1.0
 * @since 2025-11-08T09:39:57+08:00
 */
import { onMounted } from 'vue'
import logger from '@/utils/logger'

onMounted(() => {
  logger.info('[MerchantProductsEdit] mounted placeholder component')
})
</script>

<style scoped>
.merchant-products-edit { padding: 16px; }
</style>

