<template>
  <!--
    商家端 - 添加商品页面占位
    此占位用于解除路由依赖扫描错误，便于开发服务器启动
  -->
  <div class="merchant-products-add">
    添加商品（占位页面）
  </div>
</template>

<script setup>
/**
 * 商家端添加商品视图（占位组件）
 * 用于在路由引用存在但页面尚未实现时，保证开发环境可正常启动。
 * 设计约束：仅提供最小渲染与挂载日志；业务功能待后续实现。
 * @author lingbai
 * @version 0.1.0
 * @since 2025-11-08T09:35:29+08:00
 */
import { onMounted } from 'vue'
import logger from '@/utils/logger'

// 组件挂载后记录信息，便于追踪页面渲染流程
onMounted(() => {
  logger.info('[MerchantProductsAdd] mounted placeholder component')
})
</script>

<style scoped>
.merchant-products-add { padding: 16px; }
</style>

