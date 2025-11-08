<template>
  <div class="admin-products-index">商品管理（占位页面）</div>
</template>

<script setup>
/**
 * 管理员端商品管理视图（占位组件）
 * @author lingbai
 * @version 0.1.0
 * @since 2025-11-08T09:39:57+08:00
 */
import { onMounted } from 'vue'
import logger from '@/utils/logger'

onMounted(() => {
  logger.info('[AdminProductsIndex] mounted placeholder component')
})
</script>

<style scoped>
.admin-products-index { padding: 16px; }
</style>

