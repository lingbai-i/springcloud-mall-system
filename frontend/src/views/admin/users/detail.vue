<template>
  <div class="admin-users-detail">用户详情（占位页面）</div>
</template>

<script setup>
/**
 * 管理员端用户详情视图（占位组件）
 * @author lingbai
 * @version 0.1.0
 * @since 2025-11-08T09:39:57+08:00
 */
import { onMounted } from 'vue'
import logger from '@/utils/logger'

onMounted(() => {
  logger.info('[AdminUsersDetail] mounted placeholder component')
})
</script>

<style scoped>
.admin-users-detail { padding: 16px; }
</style>

