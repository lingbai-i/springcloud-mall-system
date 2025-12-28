<template>
  <div class="banner-review-page">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>轮播图审核</span>
          <el-badge :value="pendingCount" :hidden="pendingCount === 0" class="pending-badge">
            <el-tag type="warning" size="small">待审核</el-tag>
          </el-badge>
        </div>
      </template>
      
      <!-- 筛选区域 -->
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="全部状态" clearable style="width: 140px">
            <el-option label="待审核" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item label="申请日期">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 260px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
      
      <!-- 数据表格 -->
      <el-table
        v-loading="loading"
        :data="reviewList"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="轮播图" width="160">
          <template #default="{ row }">
            <el-image
              :src="row.imageUrl"
              :preview-src-list="[row.imageUrl]"
              fit="cover"
              class="banner-thumbnail"
            >
              <template #error>
                <div class="image-error">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="150" show-overflow-tooltip />
        <el-table-column prop="merchantName" label="商家" width="120" show-overflow-tooltip />
        <el-table-column label="展示周期" width="200">
          <template #default="{ row }">
            {{ row.startDate }} ~ {{ row.endDate }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="170">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleViewDetail(row)">
              查看
            </el-button>
            <template v-if="row.status === 'PENDING'">
              <el-button type="success" link size="small" @click="handleApprove(row)">
                通过
              </el-button>
              <el-button type="danger" link size="small" @click="handleReject(row)">
                拒绝
              </el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
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
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Picture } from '@element-plus/icons-vue'
import {
  getBannerReviewList,
  approveBannerApplication,
  rejectBannerApplication,
  getPendingBannerCount
} from '@/api/admin/banner'

const router = useRouter()

// 加载状态
const loading = ref(false)
const rejectLoading = ref(false)

// 待审核数量
const pendingCount = ref(0)

// 筛选表单
const filterForm = reactive({
  status: '',
  dateRange: []
})

// 分页
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 审核列表
const reviewList = ref([])

// 拒绝对话框
const rejectDialogVisible = ref(false)
const rejectFormRef = ref(null)
const rejectForm = reactive({
  id: null,
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
  PENDING: { text: '待审核', type: 'warning' },
  APPROVED: { text: '已通过', type: 'success' },
  REJECTED: { text: '已拒绝', type: 'danger' }
}

const getStatusType = (status) => statusMap[status]?.type || 'info'
const getStatusText = (status) => statusMap[status]?.text || status

// 格式化时间
const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

// 加载审核列表
const loadReviewList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page - 1, // Spring Data uses 0-based pagination
      size: pagination.size,
      status: filterForm.status || undefined,
      startDate: filterForm.dateRange?.[0] || undefined,
      endDate: filterForm.dateRange?.[1] || undefined
    }
    
    const response = await getBannerReviewList(params)
    if (response.success) {
      // 兼容 Spring Data Page 格式 (content) 和其他格式 (records/list)
      reviewList.value = response.data?.content || response.data?.records || response.data?.list || []
      pagination.total = response.data?.totalElements || response.data?.total || 0
    }
  } catch (error) {
    console.error('Load review list error:', error)
    ElMessage.error('加载审核列表失败')
  } finally {
    loading.value = false
  }
}

// 加载待审核数量
const loadPendingCount = async () => {
  try {
    const response = await getPendingBannerCount()
    if (response.success) {
      pendingCount.value = response.data || 0
    }
  } catch (error) {
    console.error('Load pending count error:', error)
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  loadReviewList()
}

// 重置
const handleReset = () => {
  filterForm.status = ''
  filterForm.dateRange = []
  pagination.page = 1
  loadReviewList()
}

// 分页大小变化
const handleSizeChange = () => {
  pagination.page = 1
  loadReviewList()
}

// 页码变化
const handlePageChange = () => {
  loadReviewList()
}

// 查看详情
const handleViewDetail = (row) => {
  router.push(`/admin/banner/detail/${row.id}`)
}

// 审核通过
const handleApprove = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定通过商家"${row.merchantName}"的轮播图申请吗？`,
      '审核确认',
      {
        confirmButtonText: '确定通过',
        cancelButtonText: '取消',
        type: 'success'
      }
    )
    
    const response = await approveBannerApplication(row.id)
    if (response.success) {
      ElMessage.success('审核通过成功')
      loadReviewList()
      loadPendingCount()
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
const handleReject = (row) => {
  rejectForm.id = row.id
  rejectForm.reason = ''
  rejectDialogVisible.value = true
}

// 确认拒绝
const confirmReject = async () => {
  try {
    await rejectFormRef.value.validate()
    
    rejectLoading.value = true
    const response = await rejectBannerApplication(rejectForm.id, {
      reason: rejectForm.reason
    })
    
    if (response.success) {
      ElMessage.success('已拒绝该申请')
      rejectDialogVisible.value = false
      loadReviewList()
      loadPendingCount()
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

onMounted(() => {
  loadReviewList()
  loadPendingCount()
})
</script>

<style scoped>
.banner-review-page {
  padding: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.pending-badge {
  margin-left: 8px;
}

.filter-form {
  margin-bottom: 20px;
}

.banner-thumbnail {
  width: 120px;
  height: 40px;
  border-radius: 4px;
  cursor: pointer;
}

.image-error {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f5f5;
  color: #909399;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
