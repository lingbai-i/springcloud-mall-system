<template>
  <div class="banner-detail-page">
    <el-card shadow="never" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>申请详情</span>
          <el-button text @click="goBack">
            <el-icon><ArrowLeft /></el-icon>
            返回列表
          </el-button>
        </div>
      </template>
      
      <el-row :gutter="40" v-if="applicationDetail">
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
            <div class="status-actions" v-if="applicationDetail.status === 'REJECTED'">
              <el-button type="primary" @click="handleResubmit">
                重新提交
              </el-button>
            </div>
          </div>
          
          <!-- 拒绝原因 -->
          <el-alert
            v-if="applicationDetail.status === 'REJECTED' && applicationDetail.rejectReason"
            type="error"
            :closable="false"
            class="reject-reason-alert"
          >
            <template #title>
              <span class="reject-reason-title">拒绝原因</span>
            </template>
            <p class="reject-reason-content">{{ applicationDetail.rejectReason }}</p>
          </el-alert>
          
          <!-- 基本信息 -->
          <el-descriptions title="基本信息" :column="2" border class="info-section">
            <el-descriptions-item label="申请ID">
              {{ applicationDetail.id }}
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="getStatusType(applicationDetail.status)" size="small">
                {{ getStatusText(applicationDetail.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="标题" :span="2">
              {{ applicationDetail.title }}
            </el-descriptions-item>
            <el-descriptions-item label="描述" :span="2">
              {{ applicationDetail.description || '无' }}
            </el-descriptions-item>
            <el-descriptions-item label="跳转链接" :span="2">
              <el-link :href="applicationDetail.targetUrl" target="_blank" type="primary">
                {{ applicationDetail.targetUrl }}
              </el-link>
            </el-descriptions-item>
            <el-descriptions-item label="展示开始日期">
              {{ applicationDetail.startDate }}
            </el-descriptions-item>
            <el-descriptions-item label="展示结束日期">
              {{ applicationDetail.endDate }}
            </el-descriptions-item>
            <el-descriptions-item label="提交时间">
              {{ formatTime(applicationDetail.createTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="审核时间">
              {{ applicationDetail.reviewTime ? formatTime(applicationDetail.reviewTime) : '-' }}
            </el-descriptions-item>
          </el-descriptions>
          
          <!-- 统计数据 -->
          <div class="statistics-section" v-if="applicationDetail.status === 'APPROVED'">
            <h3>数据统计</h3>
            <el-row :gutter="20">
              <el-col :span="8">
                <div class="stat-card">
                  <div class="stat-value">{{ statistics.totalImpressions || 0 }}</div>
                  <div class="stat-label">曝光量</div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="stat-card">
                  <div class="stat-value">{{ statistics.totalClicks || 0 }}</div>
                  <div class="stat-label">点击量</div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="stat-card">
                  <div class="stat-value">{{ statistics.ctr || '0.00' }}%</div>
                  <div class="stat-label">点击率 (CTR)</div>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-col>
        
        <!-- 右侧预览 -->
        <el-col :xs="24" :lg="10">
          <div class="preview-section">
            <h3>轮播图预览</h3>
            <BannerPreview
              :image-url="applicationDetail.imageUrl"
              :title="applicationDetail.title"
              :container-width="400"
            />
            
            <!-- 大图预览 -->
            <div class="full-preview">
              <el-image
                :src="applicationDetail.imageUrl"
                :preview-src-list="[applicationDetail.imageUrl]"
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
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  ArrowLeft, 
  Picture, 
  Clock, 
  CircleCheck, 
  CircleClose, 
  Warning,
  Remove
} from '@element-plus/icons-vue'
import BannerPreview from '@/components/BannerPreview.vue'
import { getBannerApplicationDetail, getBannerStatistics } from '@/api/merchant/banner'

const router = useRouter()
const route = useRoute()

// 加载状态
const loading = ref(false)

// 申请详情
const applicationDetail = ref(null)

// 统计数据
const statistics = ref({
  totalImpressions: 0,
  totalClicks: 0,
  ctr: '0.00'
})

// 状态映射
const statusMap = {
  PENDING: { 
    text: '待审核', 
    type: 'warning',
    icon: 'Clock',
    desc: '您的申请正在等待管理员审核',
    class: 'pending'
  },
  APPROVED: { 
    text: '已通过', 
    type: 'success',
    icon: 'CircleCheck',
    desc: '您的申请已通过审核，将在展示日期自动上架',
    class: 'approved'
  },
  REJECTED: { 
    text: '已拒绝', 
    type: 'danger',
    icon: 'CircleClose',
    desc: '您的申请未通过审核，请查看拒绝原因',
    class: 'rejected'
  },
  EXPIRED: { 
    text: '已过期', 
    type: 'info',
    icon: 'Warning',
    desc: '展示周期已结束',
    class: 'expired'
  },
  CANCELLED: { 
    text: '已取消', 
    type: 'info',
    icon: 'Remove',
    desc: '您已取消此申请',
    class: 'cancelled'
  }
}

const getStatusType = (status) => statusMap[status]?.type || 'info'
const getStatusText = (status) => statusMap[status]?.text || status

const statusText = computed(() => statusMap[applicationDetail.value?.status]?.text || '')
const statusDesc = computed(() => statusMap[applicationDetail.value?.status]?.desc || '')
const statusIcon = computed(() => statusMap[applicationDetail.value?.status]?.icon || 'Clock')
const statusClass = computed(() => statusMap[applicationDetail.value?.status]?.class || '')

// 格式化时间
const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

// 加载申请详情
const loadApplicationDetail = async () => {
  const id = route.params.id
  if (!id) {
    ElMessage.error('申请ID不存在')
    router.push('/merchant/banner/list')
    return
  }
  
  loading.value = true
  try {
    const response = await getBannerApplicationDetail(id)
    if (response.success && response.data) {
      applicationDetail.value = response.data
      
      // 如果是已通过状态，加载统计数据
      if (response.data.status === 'APPROVED') {
        loadStatistics(id)
      }
    } else {
      ElMessage.error('加载申请详情失败')
    }
  } catch (error) {
    console.error('Load detail error:', error)
    ElMessage.error('加载申请详情失败')
  } finally {
    loading.value = false
  }
}

// 加载统计数据
const loadStatistics = async (id) => {
  try {
    const response = await getBannerStatistics(id)
    if (response.success && response.data) {
      statistics.value = response.data
    }
  } catch (error) {
    console.error('Load statistics error:', error)
  }
}

// 重新提交
const handleResubmit = () => {
  router.push(`/merchant/banner/apply/${applicationDetail.value.id}`)
}

// 返回列表
const goBack = () => {
  router.push('/merchant/banner/list')
}

onMounted(() => {
  loadApplicationDetail()
})
</script>

<style scoped>
.banner-detail-page {
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

.status-card.expired,
.status-card.cancelled {
  background-color: #f5f5f5;
  border: 1px solid #d9d9d9;
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

.status-card.expired .status-icon,
.status-card.cancelled .status-icon {
  color: #909399;
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

/* 统计数据 */
.statistics-section {
  margin-top: 20px;
}

.statistics-section h3 {
  margin: 0 0 16px 0;
  font-size: 16px;
  color: #303133;
}

.stat-card {
  background-color: #f5f7fa;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #52c41a;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
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
