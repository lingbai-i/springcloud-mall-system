<template>
  <div class="audit-page">
    <h2>商品审核管理</h2>
    <p class="hint">此页面为占位实现，后续将接入真实审核数据与操作。</p>

    <div class="audit-list">
      <div class="audit-item" v-for="item in mockAudits" :key="item.id">
        <div class="info">
          <span class="name">{{ item.name }}</span>
          <span class="status" :class="item.status">{{ item.statusLabel }}</span>
        </div>
        <div class="actions">
          <el-button type="primary" size="small" @click="approve(item.id)">通过</el-button>
          <el-button type="danger" size="small" @click="reject(item.id)">驳回</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
/**
 * V1.0 2025-11-08T08:58:56+08:00 修改日志：
 * - 新增管理员端商品审核占位页面，修复路由导入缺失导致的构建错误
 * @author lingbai
 * @description 提供基本的审核列表占位与日志记录，后续可接入真实数据源
 */
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import * as logger from '@/utils/logger'

const mockAudits = ref([
  { id: 11, name: '商品 X', status: 'pending', statusLabel: '待审核' },
  { id: 12, name: '商品 Y', status: 'pending', statusLabel: '待审核' },
  { id: 13, name: '商品 Z', status: 'approved', statusLabel: '已通过' }
])

const approve = (id) => {
  ElMessage.success(`已通过商品 ID=${id} 的审核（占位）`)
  logger.info('商品审核通过（占位）', { id })
}

const reject = (id) => {
  ElMessage.warning(`已驳回商品 ID=${id} 的审核（占位）`)
  logger.warn('商品审核驳回（占位）', { id })
}

onMounted(() => {
  logger.info('进入商品审核占位页面')
})
</script>

<style scoped>
.audit-page { padding: 16px; }
.hint { color: #666; margin-bottom: 12px; }
.audit-list { display: flex; flex-direction: column; gap: 10px; }
.audit-item { display: flex; justify-content: space-between; align-items: center; border: 1px solid #eee; border-radius: 6px; padding: 10px; }
.info { display: flex; align-items: center; gap: 12px; }
.name { font-weight: 600; }
.status { font-size: 12px; padding: 2px 6px; border-radius: 4px; }
.status.pending { background: #fff7e6; color: #d48806; border: 1px solid #ffd591; }
.status.approved { background: #f6ffed; color: #389e0d; border: 1px solid #b7eb8f; }
</style>
