<template>
  <div class="merchant-products-edit">
    编辑商品（占位页面）
  </div>
</template>

<script setup>
/**
 * 商家端编辑商品视图（占位组件）
 * 目的：避免路由动态导入文件缺失导致的开发服务器启动失败。
 * @author lingbai
 * @version 0.1.0
 * @since 2025-11-08T09:35:29+08:00
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

