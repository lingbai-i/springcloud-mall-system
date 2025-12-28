<template>
  <div class="banner-list-page">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>轮播图投流</span>
          <el-button type="primary" @click="goToApply">
            <el-icon><Plus /></el-icon>
            新建申请
          </el-button>
        </div>
      </template>
      
      <!-- 筛选区域 -->
      <div class="filter-section">
        <el-radio-group v-model="filterStatus" @change="handleFilterChange">
          <el-radio-button value="">全部</el-radio-button>
          <el-radio-button value="PENDING">待审核</el-radio-button>
          <el-radio-button value="APPROVED">已通过</el-radio-button>
          <el-radio-button value="REJECTED">已拒绝</el-radio-button>
          <el-radio-button value="EXPIRED">已过期</el-radio-button>
          <el-radio-button value="CANCELLED">已取消</el-radio-button>
        </el-radio-group>
      </div>
      
      <!-- 申请列表 -->
      <el-table
        v-loading="loading"
        :data="applicationList"
        style="width: 100%"
        @row-click="handleRowClick"
      >
        <el-table-column label="轮播图" width="160">
          <template #default="{ row }">
            <el-image
              :src="row.imageUrl"
              :preview-src-list="[row.imageUrl]"
              fit="cover"
              class="banner-thumbnail"
              @click.stop
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
        
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="展示周期" width="200">
          <template #default="{ row }">
            <span>{{ row.startDate }} 至 {{ row.endDate }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="统计数据" width="150">
          <template #default="{ row }">
            <div v-if="row.status === 'APPROVED'" class="stats-info">
              <span>曝光: {{ row.totalImpressions || 0 }}</span>
              <span>点击: {{ row.totalClicks || 0 }}</span>
            </div>
            <span v-else class="no-stats">-</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="createTime" label="提交时间" width="170">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button
              text
              type="primary"
              size="small"
              @click.stop="viewDetail(row.id)"
            >
              详情
            </el-button>
            <el-button
              v-if="row.status === 'PENDING'"
              text
              type="warning"
              size="small"
              @click.stop="goToEdit(row.id)"
            >
              编辑
            </el-button>
            <el-button
              v-if="row.status === 'PENDING'"
              text
              type="danger"
              size="small"
              @click.stop="handleCancel(row)"
            >
              取消
            </el-button>
            <el-button
              v-if="row.status === 'REJECTED'"
              text
              type="primary"
              size="small"
              @click.stop="goToEdit(row.id)"
            >
              重新提交
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
    
    <!-- 取消确认对话框 -->
    <el-dialog
      v-model="cancelDialogVisible"
      title="取消申请"
      width="400px"
    >
      <p>确定要取消这个轮播图申请吗？取消后无法恢复。</p>
      <template #footer>
        <el-button @click="cancelDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="cancelling" @click="confirmCancel">
          确定取消
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Picture } from '@element-plus/icons-vue'
import { 
  getBannerApplicationList, 
  cancelBannerApplication
} from '@/api/merchant/banner'

const router = useRouter()

// 加载状态
const loading = ref(false)
const cancelling = ref(false)

// 筛选状态
const filterStatus = ref('')

// 申请列表
const applicationList = ref([])

// 分页
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 取消对话框
const cancelDialogVisible = ref(false)
const cancelTarget = ref(null)

// 状态映射
const statusMap = {
  PENDING: { text: '待审核', type: 'warning' },
  APPROVED: { text: '已通过', type: 'success' },
  REJECTED: { text: '已拒绝', type: 'danger' },
  EXPIRED: { text: '已过期', type: 'info' },
  CANCELLED: { text: '已取消', type: 'info' }
}

const getStatusType = (status) => statusMap[status]?.type || 'info'
const getStatusText = (status) => statusMap[status]?.text || status

// 格式化时间
const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

// 加载申请列表
const loadApplicationList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page - 1, // Spring Data uses 0-based pagination
      size: pagination.size
    }
    if (filterStatus.value) {
      params.status = filterStatus.value
    }
    
    const response = await getBannerApplicationList(params)
    if (response.success && response.data) {
      // 兼容 Spring Data Page 格式 (content) 和其他格式 (records/list)
      applicationList.value = response.data.content || response.data.records || response.data.list || []
      pagination.total = response.data.totalElements || response.data.total || 0
    }
  } catch (error) {
    console.error('Load list error:', error)
    ElMessage.error('加载申请列表失败')
  } finally {
    loading.value = false
  }
}

// 筛选变化
const handleFilterChange = () => {
  pagination.page = 1
  loadApplicationList()
}

// 分页变化
const handleSizeChange = () => {
  pagination.page = 1
  loadApplicationList()
}

const handlePageChange = () => {
  loadApplicationList()
}

// 行点击
const handleRowClick = (row) => {
  viewDetail(row.id)
}

// 查看详情
const viewDetail = (id) => {
  router.push(`/merchant/banner/detail/${id}`)
}

// 跳转到新建申请页面
const goToApply = () => {
  router.push('/merchant/banner/apply')
}

// 跳转到编辑页面
const goToEdit = (id) => {
  router.push(`/merchant/banner/apply/${id}`)
}

// 取消申请
const handleCancel = (row) => {
  cancelTarget.value = row
  cancelDialogVisible.value = true
}

const confirmCancel = async () => {
  if (!cancelTarget.value) return
  
  cancelling.value = true
  try {
    await cancelBannerApplication(cancelTarget.value.id)
    ElMessage.success('申请已取消')
    cancelDialogVisible.value = false
    loadApplicationList()
  } catch (error) {
    console.error('Cancel error:', error)
    ElMessage.error('取消失败，请重试')
  } finally {
    cancelling.value = false
  }
}

onMounted(() => {
  loadApplicationList()
})
</script>

<style scoped>
.banner-list-page {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-section {
  margin-bottom: 20px;
}

.banner-thumbnail {
  width: 140px;
  height: 44px;
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

.stats-info {
  display: flex;
  flex-direction: column;
  font-size: 12px;
  color: #606266;
}

.no-stats {
  color: #909399;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

:deep(.el-table__row) {
  cursor: pointer;
}

:deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}
</style>
