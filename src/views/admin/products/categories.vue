<template>
  <div class="admin-products-categories">
    分类管理（占位页面）
  </div>
</template>

<script setup>
/**
 * 管理员端商品分类管理视图（占位组件）
 * @author lingbai
 * @version 0.1.0
 * @since 2025-11-08T09:35:29+08:00
 */
import { onMounted } from 'vue'
import logger from '@/utils/logger'

onMounted(() => {
  logger.info('[AdminProductsCategories] mounted placeholder component')
})
</script>

<style scoped>
.admin-products-categories { padding: 16px; }
</style>

