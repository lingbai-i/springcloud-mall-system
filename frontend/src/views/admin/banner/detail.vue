<template>
  <div class="banner-review-detail-page">
    <el-card shadow="never" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>审核详情</span>
          <el-button text @click="goBack">
            <el-icon><ArrowLeft /></el-icon>
            返回列表
          </el-button>
        </div>
      </template>
      
      <el-row :gutter="40" v-if="reviewDetail">
        <!-- 左侧信息 -->
        <el-col :xs="24" :lg="14">
          <!-- 状态卡片 -->
          <div class="status-card" :class="statusClass">
            <div class="status-icon">
              <el-icon :size="32">
                <component :is="statusIcon" />
              </el-icon>
            </div>
            <div class="status-info">
              <div class="status-text">{{ statusText }}</div>
              <div class="status-desc">{{ statusDesc }}</div>
            </div>
            <div class="status-actions" v-if="reviewDetail.status === 'PENDING'">
              <el-button type="success" @click="handleApprove">
                <el-icon><Check /></el-icon>
                通过
              </el-button>
              <el-button type="danger" @click="handleReject">
                <el-icon><Close /></el-icon>
                拒绝
              </el-button>
            </div>
          </div>
          
          <!-- 拒绝原因 -->
          <el-alert
            v-if="reviewDetail.status === 'REJECTED' && reviewDetail.rejectReason"
            type="error"
            :closable="false"
            class="reject-reason-alert"
          >
            <template #title>
              <span class="reject-reason-title">拒绝原因</span>
            </template>
            <p class="reject-reason-content">{{ reviewDetail.rejectReason }}</p>
          </el-alert>
          
          <!-- 商家信息 -->
          <el-descriptions title="商家信息" :column="2" border class="info-section">
            <el-descriptions-item label="商家ID">
              {{ reviewDetail.merchantId }}
            </el-descriptions-item>
            <el-descriptions-item label="商家名称">
              {{ reviewDetail.merchantName }}
            </el-descriptions-item>
          </el-descriptions>
          
          <!-- 申请信息 -->
          <el-descriptions title="申请信息" :column="2" border class="info-section">
            <el-descriptions-item label="申请ID">
              {{ reviewDetail.id }}
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="getStatusType(reviewDetail.status)" size="small">
                {{ getStatusText(reviewDetail.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="标题" :span="2">
              {{ reviewDetail.title }}
            </el-descriptions-item>
            <el-descriptions-item label="描述" :span="2">
              {{ reviewDetail.description || '无' }}
            </el-descriptions-item>
            <el-descriptions-item label="跳转链接" :span="2">
              <el-link :href="reviewDetail.targetUrl" target="_blank" type="primary">
                {{ reviewDetail.targetUrl }}
              </el-link>
            </el-descriptions-item>
            <el-descriptions-item label="展示开始日期">
              {{ reviewDetail.startDate }}
            </el-descriptions-item>
            <el-descriptions-item label="展示结束日期">
              {{ reviewDetail.endDate }}
            </el-descriptions-item>
            <el-descriptions-item label="商家申请时间">
              {{ formatTime(reviewDetail.submitTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="管理员审核时间">
              {{ reviewDetail.reviewTime ? formatTime(reviewDetail.reviewTime) : '-' }}
            </el-descriptions-item>
          </el-descriptions>
        </el-col>
        
        <!-- 右侧预览 -->
        <el-col :xs="24" :lg="10">
          <div class="preview-section">
            <h3>轮播图预览</h3>
            <BannerPreview
              :image-url="reviewDetail.imageUrl"
              :title="reviewDetail.title"
              :container-width="400"
            />
            
            <!-- 大图预览 -->
            <div class="full-preview">
              <el-image
                :src="reviewDetail.imageUrl"
                :preview-src-list="[reviewDetail.imageUrl]"
                fit="contain"
                class="full-image"
              >
                <template #error>
                  <div class="image-error">
                    <el-icon><Picture /></el-icon>
                    <span>图片加载失败</span>
                  </div>
                </template>
              </el-image>
              <div class="preview-tip">点击图片可查看大图</div>
            </div>
          </div>
        </el-col>
      </el-row>
      
      <!-- 空状态 -->
      <el-empty v-else-if="!loading" description="申请不存在或已被删除" />
    </el-card>
    
    <!-- 拒绝原因对话框 -->
    <el-dialog
      v-model="rejectDialogVisible"
      title="拒绝申请"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form ref="rejectFormRef" :model="rejectForm" :rules="rejectRules" label-width="80px">
        <el-form-item label="拒绝原因" prop="reason">
          <el-input
            v-model="rejectForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入拒绝原因，将展示给商家"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="rejectLoading" @click="confirmReject">
          确认拒绝
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  ArrowLeft, 
  Picture, 
  Clock, 
  CircleCheck, 
  CircleClose,
  Check,
  Close
} from '@element-plus/icons-vue'
import BannerPreview from '@/components/BannerPreview.vue'
import { 
  getBannerReviewDetail, 
  approveBannerApplication, 
  rejectBannerApplication 
} from '@/api/admin/banner'

const router = useRouter()
const route = useRoute()

// 加载状态
const loading = ref(false)
const rejectLoading = ref(false)

// 审核详情
const reviewDetail = ref(null)

// 拒绝对话框
const rejectDialogVisible = ref(false)
const rejectFormRef = ref(null)
const rejectForm = reactive({
  reason: ''
})
const rejectRules = {
  reason: [
    { required: true, message: '请输入拒绝原因', trigger: 'blur' },
    { min: 5, message: '拒绝原因至少5个字符', trigger: 'blur' }
  ]
}

// 状态映射
const statusMap = {
  PENDING: { 
    text: '待审核', 
    type: 'warning',
    icon: 'Clock',
    desc: '该申请正在等待审核',
    class: 'pending'
  },
  APPROVED: { 
    text: '已通过', 
    type: 'success',
    icon: 'CircleCheck',
    desc: '该申请已通过审核',
    class: 'approved'
  },
  REJECTED: { 
    text: '已拒绝', 
    type: 'danger',
    icon: 'CircleClose',
    desc: '该申请已被拒绝',
    class: 'rejected'
  }
}

const getStatusType = (status) => statusMap[status]?.type || 'info'
const getStatusText = (status) => statusMap[status]?.text || status

const statusText = computed(() => statusMap[reviewDetail.value?.status]?.text || '')
const statusDesc = computed(() => statusMap[reviewDetail.value?.status]?.desc || '')
const statusIcon = computed(() => statusMap[reviewDetail.value?.status]?.icon || 'Clock')
const statusClass = computed(() => statusMap[reviewDetail.value?.status]?.class || '')

// 格式化时间
const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

// 加载审核详情
const loadReviewDetail = async () => {
  const id = route.params.id
  if (!id) {
    ElMessage.error('申请ID不存在')
    router.push('/admin/banner/review')
    return
  }
  
  loading.value = true
  try {
    const response = await getBannerReviewDetail(id)
    if (response.success && response.data) {
      reviewDetail.value = response.data
    } else {
      ElMessage.error('加载审核详情失败')
    }
  } catch (error) {
    console.error('Load detail error:', error)
    ElMessage.error('加载审核详情失败')
  } finally {
    loading.value = false
  }
}

// 审核通过
const handleApprove = async () => {
  try {
    await ElMessageBox.confirm(
      '确定通过该轮播图申请吗？',
      '审核确认',
      {
        confirmButtonText: '确定通过',
        cancelButtonText: '取消',
        type: 'success'
      }
    )
    
    const response = await approveBannerApplication(reviewDetail.value.id)
    if (response.success) {
      ElMessage.success('审核通过成功')
      loadReviewDetail()
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Approve error:', error)
      ElMessage.error('操作失败')
    }
  }
}

// 打开拒绝对话框
const handleReject = () => {
  rejectForm.reason = ''
  rejectDialogVisible.value = true
}

// 确认拒绝
const confirmReject = async () => {
  try {
    await rejectFormRef.value.validate()
    
    rejectLoading.value = true
    const response = await rejectBannerApplication(reviewDetail.value.id, {
      reason: rejectForm.reason
    })
    
    if (response.success) {
      ElMessage.success('已拒绝该申请')
      rejectDialogVisible.value = false
      loadReviewDetail()
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error) {
    if (error !== false) {
      console.error('Reject error:', error)
      ElMessage.error('操作失败')
    }
  } finally {
    rejectLoading.value = false
  }
}

// 返回列表
const goBack = () => {
  router.push('/admin/banner/review')
}

onMounted(() => {
  loadReviewDetail()
})
</script>

<style scoped>
.banner-review-detail-page {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 状态卡片 */
.status-card {
  display: flex;
  align-items: center;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.status-card.pending {
  background-color: #fef0e6;
  border: 1px solid #f5a623;
}

.status-card.approved {
  background-color: #e6f7e6;
  border: 1px solid #52c41a;
}

.status-card.rejected {
  background-color: #fef0f0;
  border: 1px solid #f56c6c;
}

.status-icon {
  margin-right: 16px;
}

.status-card.pending .status-icon {
  color: #f5a623;
}

.status-card.approved .status-icon {
  color: #52c41a;
}

.status-card.rejected .status-icon {
  color: #f56c6c;
}

.status-info {
  flex: 1;
}

.status-text {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 4px;
}

.status-desc {
  font-size: 14px;
  color: #606266;
}

.status-actions {
  display: flex;
  gap: 12px;
}

/* 拒绝原因 */
.reject-reason-alert {
  margin-bottom: 20px;
}

.reject-reason-title {
  font-weight: 600;
}

.reject-reason-content {
  margin: 8px 0 0 0;
  color: #606266;
}

/* 信息区域 */
.info-section {
  margin-bottom: 20px;
}

/* 预览区域 */
.preview-section {
  position: sticky;
  top: 20px;
}

.preview-section h3 {
  margin: 0 0 16px 0;
  font-size: 16px;
  color: #303133;
}

.full-preview {
  margin-top: 20px;
}

.full-image {
  width: 100%;
  border-radius: 8px;
  cursor: pointer;
}

.image-error {
  width: 100%;
  height: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #f5f5f5;
  color: #909399;
  gap: 8px;
}

.preview-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
  text-align: center;
}

@media (max-width: 992px) {
  .preview-section {
    margin-top: 24px;
    position: static;
  }
}
</style>
