<template>
  <div class="merchants-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">商家管理</h2>
        <p class="page-desc">管理平台所有商家信息，包括商家审核、状态管理等</p>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="exportMerchants" :loading="exporting">
          <el-icon><Download /></el-icon>
          导出商家
        </el-button>
      </div>
    </div>

    <!-- 搜索和筛选区域 -->
    <div class="search-section">
      <el-card>
        <div class="search-form">
          <el-row :gutter="20">
            <el-col :span="6">
              <el-input
                v-model="searchForm.keyword"
                placeholder="搜索商家名称、联系人、手机号"
                clearable
                @keyup.enter="handleSearch">
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
            </el-col>
            <el-col :span="4">
              <el-select v-model="searchForm.status" placeholder="商家状态" clearable>
                <el-option label="全部状态" value=""></el-option>
                <el-option label="待审核" value="pending"></el-option>
                <el-option label="已通过" value="approved"></el-option>
                <el-option label="已拒绝" value="rejected"></el-option>
                <el-option label="已禁用" value="disabled"></el-option>
              </el-select>
            </el-col>
            <el-col :span="4">
              <el-select v-model="searchForm.category" placeholder="经营类目" clearable>
                <el-option label="全部类目" value=""></el-option>
                <el-option label="数码电子" value="electronics"></el-option>
                <el-option label="服装鞋帽" value="clothing"></el-option>
                <el-option label="家居用品" value="home"></el-option>
                <el-option label="美妆护肤" value="beauty"></el-option>
                <el-option label="食品饮料" value="food"></el-option>
                <el-option label="运动户外" value="sports"></el-option>
              </el-select>
            </el-col>
            <el-col :span="6">
              <el-date-picker
                v-model="searchForm.dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="注册开始日期"
                end-placeholder="注册结束日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD">
              </el-date-picker>
            </el-col>
            <el-col :span="4">
              <el-button type="primary" @click="handleSearch" :loading="loading">
                <el-icon><Search /></el-icon>
                搜索
              </el-button>
              <el-button @click="resetSearch">重置</el-button>
            </el-col>
          </el-row>
        </div>
      </el-card>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-section">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-icon total">
                <el-icon><Shop /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-number">{{ stats.total }}</div>
                <div class="stats-label">总商家数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-icon pending">
                <el-icon><Clock /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-number">{{ stats.pending }}</div>
                <div class="stats-label">待审核</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-icon approved">
                <el-icon><Check /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-number">{{ stats.approved }}</div>
                <div class="stats-label">已通过</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-icon active">
                <el-icon><TrendCharts /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-number">{{ stats.active }}</div>
                <div class="stats-label">活跃商家</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 商家列表 -->
    <div class="table-section">
      <el-card>
        <template #header>
          <div class="table-header">
            <span>商家列表</span>
            <div class="table-actions">
              <el-button
                type="danger"
                size="small"
                :disabled="selectedMerchants.length === 0"
                @click="batchDisable">
                批量禁用
              </el-button>
              <el-button
                type="success"
                size="small"
                :disabled="selectedMerchants.length === 0"
                @click="batchApprove">
                批量通过
              </el-button>
            </div>
          </div>
        </template>

        <el-table
          :data="merchants"
          v-loading="loading"
          @selection-change="handleSelectionChange"
          stripe
          style="width: 100%">
          <el-table-column type="selection" width="55"></el-table-column>
          <el-table-column prop="id" label="商家ID" width="80"></el-table-column>
          <el-table-column label="商家信息" min-width="200">
            <template #default="{ row }">
              <div class="merchant-info">
                <div class="merchant-avatar" v-if="row.avatar || row.shopLogo">
                  <img :src="row.avatar || row.shopLogo" :alt="row.shopName" />
                </div>
                <div class="merchant-avatar default-avatar" v-else>
                  <el-icon :size="24"><Shop /></el-icon>
                </div>
                <div class="merchant-details">
                  <div class="shop-name">{{ row.shopName }}</div>
                  <div class="contact-info">{{ row.contactName || row.contactPerson || '未设置' }} | {{ row.contactPhone || row.phone || '未设置' }}</div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="邮箱" width="180">
            <template #default="{ row }">
              {{ row.contactEmail || row.email }}
            </template>
          </el-table-column>
          <el-table-column label="经营类目" width="120">
            <template #default="{ row }">
              <el-tag size="small">{{ getCategoryName(row.businessCategory || row.category) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)" size="small">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="productCount" label="商品数" width="80"></el-table-column>
          <el-table-column prop="orderCount" label="订单数" width="80"></el-table-column>
          <el-table-column prop="rating" label="评分" width="100">
            <template #default="{ row }">
              <el-rate
                v-model="row.rating"
                disabled
                show-score
                text-color="#ff9900"
                score-template="{value}"
                size="small">
              </el-rate>
            </template>
          </el-table-column>
          <el-table-column label="注册时间" width="120">
            <template #default="{ row }">
              {{ formatDate(row.createTime || row.registerTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button
                type="primary"
                size="small"
                @click="viewDetail(row)">
                查看详情
              </el-button>
              <el-button
                v-if="isPendingStatus(row)"
                type="success"
                size="small"
                @click="approveMerchant(row)">
                审核
              </el-button>
              <el-dropdown @command="handleCommand" trigger="click">
                <el-button size="small">
                  更多<el-icon class="el-icon--right"><arrow-down /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item :command="`edit-${row.id}`">编辑信息</el-dropdown-item>
                    <el-dropdown-item
                      v-if="isApprovedStatus(row)"
                      :command="`disable-${row.id}`">
                      禁用商家
                    </el-dropdown-item>
                    <el-dropdown-item
                      v-if="isDisabledStatus(row)"
                      :command="`enable-${row.id}`">
                      启用商家
                    </el-dropdown-item>
                    <el-dropdown-item
                      :command="`reset-password-${row.id}`"
                      divided>
                      重置密码
                    </el-dropdown-item>
                    <el-dropdown-item
                      :command="`delete-${row.id}`"
                      style="color: #f56c6c">
                      删除商家
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="pagination.current"
            v-model:page-size="pagination.size"
            :page-sizes="[10, 20, 50, 100]"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange">
          </el-pagination>
        </div>
      </el-card>
    </div>

    <!-- 商家详情对话框 -->
    <MerchantDetail
      v-model="detailVisible"
      :merchant="currentMerchant"
      @refresh="loadMerchants" />

    <!-- 商家审核对话框 -->
    <MerchantApproval
      v-model="approvalVisible"
      :merchant="currentMerchant"
      @refresh="loadMerchants" />

    <!-- 商家编辑对话框 -->
    <MerchantEdit
      v-model="editVisible"
      :merchant="currentMerchant"
      @refresh="loadMerchants" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Download,
  Search,
  Shop,
  Clock,
  Check,
  TrendCharts,
  ArrowDown
} from '@element-plus/icons-vue'
import { getMerchantList } from '@/api/admin'
import MerchantDetail from './components/MerchantDetail.vue'
import MerchantApproval from './components/MerchantApproval.vue'
import MerchantEdit from './components/MerchantEdit.vue'

// 响应式数据
const loading = ref(false)
const exporting = ref(false)
const merchants = ref([])
const selectedMerchants = ref([])

// 搜索表单
const searchForm = reactive({
  keyword: '',
  status: '',
  category: '',
  dateRange: []
})

// 分页信息
const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

// 统计数据
const stats = reactive({
  total: 0,
  pending: 0,
  approved: 0,
  active: 0
})

// 对话框控制
const detailVisible = ref(false)
const approvalVisible = ref(false)
const editVisible = ref(false)
const currentMerchant = ref(null)

// 类目映射
const categoryMap = {
  electronics: '数码电子',
  clothing: '服装鞋帽',
  home: '家居用品',
  beauty: '美妆护肤',
  food: '食品饮料',
  sports: '运动户外'
}

// 方法
const loadMerchants = async () => {
  loading.value = true
  try {
    // 准备查询参数
    const queryParams = {
      page: pagination.current,
      size: pagination.size,
      keyword: searchForm.keyword,
      status: searchForm.status,
      category: searchForm.category
    }
    
    // 调用真实的API
    const response = await getMerchantList(queryParams)
    // 适配后端返回格式：PageResult 使用 records 字段，merchant-service 直接返回使用 list 字段
    merchants.value = response.data.records || response.data.list || []
    pagination.total = response.data.total || 0
    
    // 更新统计数据
    stats.total = response.data.stats?.total || pagination.total || 0
    stats.pending = response.data.stats?.pending || 0
    stats.approved = response.data.stats?.approved || 0
    stats.active = response.data.stats?.active || 0
    
  } catch (error) {
    console.error('Load merchants error:', error)
    ElMessage.error('获取商家列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadMerchants()
}

const resetSearch = () => {
  Object.assign(searchForm, {
    keyword: '',
    status: '',
    category: '',
    dateRange: []
  })
  handleSearch()
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.current = 1
  loadMerchants()
}

const handleCurrentChange = (page) => {
  pagination.current = page
  loadMerchants()
}

const handleSelectionChange = (selection) => {
  selectedMerchants.value = selection
}

const viewDetail = (merchant) => {
  currentMerchant.value = merchant
  detailVisible.value = true
}

const approveMerchant = (merchant) => {
  currentMerchant.value = merchant
  approvalVisible.value = true
}

const handleCommand = (command) => {
  const [action, id] = command.split('-')
  const merchant = merchants.value.find(m => m.id === parseInt(id))
  
  switch (action) {
    case 'edit':
      currentMerchant.value = merchant
      editVisible.value = true
      break
    case 'disable':
      disableMerchant(merchant)
      break
    case 'enable':
      enableMerchant(merchant)
      break
    case 'reset':
      resetPassword(merchant)
      break
    case 'delete':
      deleteMerchant(merchant)
      break
  }
}

const disableMerchant = async (merchant) => {
  try {
    await ElMessageBox.confirm(
      `确定要禁用商家"${merchant.shopName}"吗？禁用后该商家将无法正常经营。`,
      '确认禁用',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 调用API禁用商家
    // await disableMerchantApi(merchant.id)
    
    merchant.status = 'disabled'
    ElMessage.success('商家已禁用')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('禁用商家失败')
    }
  }
}

const enableMerchant = async (merchant) => {
  try {
    // 调用API启用商家
    // await enableMerchantApi(merchant.id)
    
    merchant.status = 'approved'
    ElMessage.success('商家已启用')
  } catch (error) {
    ElMessage.error('启用商家失败')
  }
}

const resetPassword = async (merchant) => {
  try {
    await ElMessageBox.confirm(
      `确定要重置商家"${merchant.shopName}"的密码吗？新密码将通过短信发送给商家。`,
      '确认重置密码',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 调用API重置密码
    // await resetMerchantPasswordApi(merchant.id)
    
    ElMessage.success('密码重置成功，新密码已发送至商家手机')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('重置密码失败')
    }
  }
}

const deleteMerchant = async (merchant) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除商家"${merchant.shopName}"吗？此操作不可恢复！`,
      '确认删除',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'error'
      }
    )
    
    // 调用API删除商家
    // await deleteMerchantApi(merchant.id)
    
    const index = merchants.value.findIndex(m => m.id === merchant.id)
    if (index > -1) {
      merchants.value.splice(index, 1)
      pagination.total--
    }
    
    ElMessage.success('商家已删除')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除商家失败')
    }
  }
}

const batchApprove = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要批量通过选中的 ${selectedMerchants.value.length} 个商家吗？`,
      '批量审核通过',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )
    
    // 调用API批量通过
    // await batchApproveMerchantsApi(selectedMerchants.value.map(m => m.id))
    
    selectedMerchants.value.forEach(merchant => {
      merchant.status = 'approved'
    })
    
    ElMessage.success('批量审核通过成功')
    selectedMerchants.value = []
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量审核失败')
    }
  }
}

const batchDisable = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要批量禁用选中的 ${selectedMerchants.value.length} 个商家吗？`,
      '批量禁用',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 调用API批量禁用
    // await batchDisableMerchantsApi(selectedMerchants.value.map(m => m.id))
    
    selectedMerchants.value.forEach(merchant => {
      merchant.status = 'disabled'
    })
    
    ElMessage.success('批量禁用成功')
    selectedMerchants.value = []
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量禁用失败')
    }
  }
}

const exportMerchants = async () => {
  exporting.value = true
  try {
    // 调用API导出商家数据
    // await exportMerchantsApi(searchForm)
    
    // 模拟导出
    await new Promise(resolve => setTimeout(resolve, 2000))
    ElMessage.success('商家数据导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  } finally {
    exporting.value = false
  }
}

// 工具方法
const getCategoryName = (category) => {
  return categoryMap[category] || category || '未设置'
}

// 状态判断辅助函数（支持数字和字符串状态）
const isPendingStatus = (row) => {
  return row.approvalStatus === 0 || row.status === 'pending'
}

const isApprovedStatus = (row) => {
  return row.status === 1 || row.status === 'approved'
}

const isDisabledStatus = (row) => {
  return row.status === 0 || row.status === 'disabled'
}

const getStatusType = (status) => {
  // 支持数字状态：0=禁用, 1=正常
  // 支持字符串状态：pending, approved, rejected, disabled
  if (status === 1 || status === 'approved') return 'success'
  if (status === 0 || status === 'disabled') return 'info'
  if (status === 'pending') return 'warning'
  if (status === 'rejected') return 'danger'
  return 'info'
}

const getStatusText = (status) => {
  // 支持数字状态：0=禁用, 1=正常
  // 支持字符串状态：pending, approved, rejected, disabled
  if (status === 1 || status === 'approved') return '已通过'
  if (status === 0 || status === 'disabled') return '已禁用'
  if (status === 'pending') return '待审核'
  if (status === 'rejected') return '已拒绝'
  return String(status)
}

const formatDate = (dateString) => {
  return new Date(dateString).toLocaleDateString('zh-CN')
}

// 生命周期
onMounted(() => {
  loadMerchants()
})
</script>

<style scoped>
.merchants-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.page-title {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
}

.page-desc {
  margin: 0;
  color: #6b7280;
  font-size: 14px;
}

.search-section {
  margin-bottom: 20px;
}

.search-form {
  padding: 20px;
}

.stats-section {
  margin-bottom: 20px;
}

.stats-card {
  border: none;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1);
}

.stats-content {
  display: flex;
  align-items: center;
}

.stats-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  font-size: 24px;
  color: white;
}

.stats-icon.total {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stats-icon.pending {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stats-icon.approved {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stats-icon.active {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stats-info {
  flex: 1;
}

.stats-number {
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
  line-height: 1;
  margin-bottom: 4px;
}

.stats-label {
  font-size: 14px;
  color: #6b7280;
}

.table-section {
  margin-bottom: 20px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.merchant-info {
  display: flex;
  align-items: center;
}

.merchant-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  margin-right: 12px;
}

.merchant-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.merchant-avatar.default-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.merchant-details {
  flex: 1;
}

.shop-name {
  font-weight: 500;
  color: #1f2937;
  margin-bottom: 4px;
}

.contact-info {
  font-size: 12px;
  color: #6b7280;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

@media (max-width: 768px) {
  .merchants-container {
    padding: 10px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 16px;
  }
  
  .search-form .el-row {
    flex-direction: column;
  }
  
  .search-form .el-col {
    width: 100% !important;
    margin-bottom: 16px;
  }
  
  .stats-section .el-row {
    flex-direction: column;
  }
  
  .stats-section .el-col {
    width: 100% !important;
    margin-bottom: 16px;
  }
}
</style>