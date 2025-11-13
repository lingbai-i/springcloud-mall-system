<template>
  <div class="audit-redirect">
    <el-result
      icon="info"
      title="页面已迁移"
      sub-title="商家审核功能已迁移到新的申请管理页面"
    >
      <template #extra>
        <el-button type="primary" @click="goToApplications">
          前往商家申请管理
        </el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { onMounted } from 'vue'

const router = useRouter()

const goToApplications = () => {
  router.push('/admin/merchants/applications')
}

// 自动跳转
onMounted(() => {
  setTimeout(() => {
    goToApplications()
  }, 1500)
})
</script>

<style scoped>
.audit-redirect {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  padding: 40px;
}
</style>
