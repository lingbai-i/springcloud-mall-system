<template>
  <div class="history-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>商家审核历史</h2>
          <p class="subtitle">查看所有商家申请的审核记录</p>
        </div>
      </template>
      
      <!-- 筛选器 -->
      <el-form :inline="true" class="filter-form">
        <el-form-item label="审批结果">
          <el-select v-model="filterForm.status" placeholder="全部" clearable @change="loadHistory">
            <el-option label="已通过" :value="1" />
            <el-option label="已拒绝" :value="2" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            @change="loadHistory"
          />
        </el-form-item>
        
        <el-form-item label="搜索">
          <el-input
            v-model="filterForm.keyword"
            placeholder="店铺名称/审批人"
            clearable
            @clear="loadHistory"
            @keyup.enter="loadHistory"
            style="width: 200px"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="loadHistory" icon="Search">查询</el-button>
          <el-button @click="resetFilter" icon="Refresh">重置</el-button>
        </el-form-item>
      </el-form>
      
      <!-- 历史记录表格 -->
      <el-table
        :data="historyList"
        :loading="loading"
        stripe
        border
        style="margin-top: 20px"
      >
        <el-table-column prop="id" label="申请ID" width="80" align="center" />
        <el-table-column prop="shopName" label="店铺名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="contactName" label="联系人" width="100" />
        <el-table-column prop="contactPhone" label="联系电话" width="130" />
        <el-table-column label="审批结果" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.approvalStatus === 1" type="success">已通过</el-tag>
            <el-tag v-else-if="row.approvalStatus === 2" type="danger">已拒绝</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="approvalByName" label="审批人" width="100" />
        <el-table-column prop="approvalTime" label="审批时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.approvalTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="approvalReason" label="审批意见" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="120" fixed="right" align="center">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="viewDetail(row)">
              <el-icon><View /></el-icon>
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadHistory"
        @current-change="loadHistory"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>
    
    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      :title="'审核详情 - ' + (currentRecord?.shopName || '')"
      width="700px"
    >
      <el-descriptions :column="2" border v-if="currentRecord">
        <el-descriptions-item label="申请ID">
          <el-tag>{{ currentRecord.id }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="店铺名称">
          <span class="highlight-text">{{ currentRecord.shopName }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="主体类型">
          {{ currentRecord.entityTypeText }}
        </el-descriptions-item>
        <el-descriptions-item label="店铺类型">
          {{ currentRecord.shopTypeText || '未填写' }}
        </el-descriptions-item>
        <el-descriptions-item label="联系人">
          {{ currentRecord.contactName }}
        </el-descriptions-item>
        <el-descriptions-item label="联系电话">
          {{ currentRecord.contactPhone }}
        </el-descriptions-item>
        <el-descriptions-item label="登录账号" :span="2">
          {{ currentRecord.username }}
        </el-descriptions-item>
        <el-descriptions-item label="申请时间" :span="2">
          {{ formatDateTime(currentRecord.createdTime) }}
        </el-descriptions-item>
        
        <el-descriptions-item label="审批结果" :span="2">
          <el-tag v-if="currentRecord.approvalStatus === 1" type="success" size="large">
            <el-icon><CircleCheck /></el-icon>
            已通过
          </el-tag>
          <el-tag v-else-if="currentRecord.approvalStatus === 2" type="danger" size="large">
            <el-icon><CircleClose /></el-icon>
            已拒绝
          </el-tag>
        </el-descriptions-item>
        
        <el-descriptions-item label="审批时间" :span="2">
          {{ formatDateTime(currentRecord.approvalTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="审批人">
          {{ currentRecord.approvalByName }}
        </el-descriptions-item>
        <el-descriptions-item label="审批人ID">
          {{ currentRecord.approvalBy }}
        </el-descriptions-item>
        <el-descriptions-item label="审批意见" :span="2">
          <div class="approval-reason">
            {{ currentRecord.approvalReason || '无' }}
          </div>
        </el-descriptions-item>
        
        <el-descriptions-item v-if="currentRecord.merchantId" label="关联商家ID" :span="2">
          <el-tag type="success">{{ currentRecord.merchantId }}</el-tag>
          <el-button 
            type="primary" 
            link 
            size="small" 
            @click="goToMerchant(currentRecord.merchantId)"
            style="margin-left: 10px"
          >
            查看商家详情
          </el-button>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { View, Search, Refresh, CircleCheck, CircleClose } from '@element-plus/icons-vue'
import { getApplicationList } from '@/api/merchant'

const router = useRouter()

// 数据
const loading = ref(false)
const historyList = ref([])

// 筛选表单
const filterForm = reactive({
  status: null,
  keyword: '',
  dateRange: []
})

// 分页
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 对话框
const detailDialogVisible = ref(false)
const currentRecord = ref(null)

/**
 * 加载历史记录
 */
const loadHistory = async () => {
  loading.value = true
  
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      status: filterForm.status,
      keyword: filterForm.keyword
    }
    
    // 只查询已审核的（状态为1或2）
    if (!params.status) {
      // 如果没有指定状态，默认查询所有已审核的
      params.status = filterForm.status
    }
    
    const response = await getApplicationList(params)
    
    if (response.success || response.code === 200) {
      const data = response.data
      // 只显示已审核的记录
      historyList.value = (data.records || []).filter(item => item.approvalStatus !== 0)
      pagination.total = historyList.value.length
      
      console.log('加载审核历史成功:', historyList.value.length, '条')
    } else {
      ElMessage.error(response.message || '加载失败')
    }
  } catch (error) {
    console.error('加载审核历史失败:', error)
    ElMessage.error('加载失败: ' + (error.message || '网络错误'))
  } finally {
    loading.value = false
  }
}

/**
 * 重置筛选
 */
const resetFilter = () => {
  filterForm.status = null
  filterForm.keyword = ''
  filterForm.dateRange = []
  pagination.page = 1
  loadHistory()
}

/**
 * 查看详情
 */
const viewDetail = (row) => {
  currentRecord.value = row
  detailDialogVisible.value = true
}

/**
 * 跳转到商家详情
 */
const goToMerchant = (merchantId) => {
  router.push(`/admin/merchants/${merchantId}`)
}

/**
 * 格式化日期时间
 */
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 初始化
onMounted(() => {
  loadHistory()
})
</script>

<style scoped>
.history-page {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}

.card-header h2 {
  margin: 0;
  font-size: 20px;
  color: #303133;
  font-weight: 600;
}

.subtitle {
  margin: 5px 0 0 0;
  font-size: 14px;
  color: #909399;
}

.filter-form {
  margin-bottom: 0;
}

.highlight-text {
  font-weight: 600;
  color: #409eff;
  font-size: 16px;
}

.approval-reason {
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
  max-height: 200px;
  overflow-y: auto;
}

/* 响应式布局 */
@media (max-width: 768px) {
  .history-page {
    padding: 12px;
  }
}
</style>











