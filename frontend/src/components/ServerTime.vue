<!--
 * @Author: lingbai
 * @Date: 2025-01-01
 * @Description: 服务器时间显示组件 - 实时显示北京时间（GMT+8）
-->
<template>
  <div class="server-time-display">
    <el-icon class="time-icon"><Clock /></el-icon>
    <span class="time-text">{{ currentTime }}</span>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { Clock } from '@element-plus/icons-vue'
import serverTimeManager from '@/utils/serverTime'

/**
 * Props
 */
const props = defineProps({
  // 时间格式，默认：YYYY-MM-DD WW HH:mm:ss
  format: {
    type: String,
    default: 'YYYY-MM-DD WW HH:mm:ss'
  },
  // 是否在组件挂载时启动自动同步（如果页面已经启动了同步，可以设置为false）
  autoSync: {
    type: Boolean,
    default: false
  }
})

// 当前服务器时间
const currentTime = ref('')

// 定时器
let timeTimer = null

/**
 * 更新时间显示
 */
const updateTime = () => {
  currentTime.value = serverTimeManager.format(props.format)
}

/**
 * 组件挂载
 */
onMounted(() => {
  // 如果需要自动同步，启动同步
  if (props.autoSync && !serverTimeManager.isSynced()) {
    serverTimeManager.startAutoSync()
  }
  
  // 初始化时间显示
  updateTime()
  
  // 每秒更新一次
  timeTimer = setInterval(updateTime, 1000)
})

/**
 * 组件卸载
 */
onUnmounted(() => {
  // 清理定时器
  if (timeTimer) {
    clearInterval(timeTimer)
    timeTimer = null
  }
})
</script>

<style scoped>
.server-time-display {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #606266;
  padding: 4px 12px;
  background: rgba(64, 158, 255, 0.05);
  border-radius: 4px;
  border: 1px solid rgba(64, 158, 255, 0.2);
}

.time-icon {
  font-size: 16px;
  color: #409eff;
}

.time-text {
  font-weight: 500;
  letter-spacing: 0.5px;
}
</style>

