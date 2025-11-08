<template>
  <div class="merchant-shop-index">
    店铺管理（占位页面）
  </div>
</template>

<script setup>
/**
 * 商家端店铺管理入口视图（占位组件）
 * 目的：满足路由依赖，后续再实现店铺功能模块。
 * @author lingbai
 * @version 0.1.0
 * @since 2025-11-08T09:35:29+08:00
 */
import { onMounted } from 'vue'
import logger from '@/utils/logger'

onMounted(() => {
  logger.info('[MerchantShopIndex] mounted placeholder component')
})
</script>

<style scoped>
.merchant-shop-index { padding: 16px; }
</style>

