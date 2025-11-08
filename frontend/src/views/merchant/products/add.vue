<template>
  <!-- 商家端 - 添加商品页面占位 -->
  <div class="merchant-products-add">添加商品（占位页面）</div>
</template>

<script setup>
/**
 * 商家端添加商品视图（占位组件）
 * 目的：解除路由依赖，确保开发服务器正常启动。
 * @author lingbai
 * @version 0.1.0
 * @since 2025-11-08T09:39:57+08:00
 */
import { onMounted } from 'vue'
import logger from '@/utils/logger'

onMounted(() => {
  logger.info('[MerchantProductsAdd] mounted placeholder component')
})
</script>

<style scoped>
.merchant-products-add { padding: 16px; }
</style>

