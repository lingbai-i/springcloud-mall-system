<template>
  <div class="admin-orders">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">订单管理</h2>
      <p class="page-desc">管理所有订单，包括查看、处理退款等操作</p>
    </div>

    <!-- 订单状态统计 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :xs="12" :sm="6" :md="4" v-for="stat in orderStats" :key="stat.key">
        <el-card shadow="hover" class="stat-card" @click="filterByStatus(stat.status)">
          <div class="stat-value">{{ stat.value }}</div>
          <div class="stat-label">{{ stat.label }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索筛选 -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="订单号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 140px">
            <el-option label="待付款" value="PENDING" />
            <el-option label="待发货" value="PAID" />
            <el-option label="已发货" value="SHIPPED" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
            <el-option label="退款中" value="REFUND_PENDING" />
            <el-option label="已退款" value="REFUNDED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 订单列表 -->
    <el-card shadow="never" class="table-card">
      <el-table :data="orderList" v-loading="loading" style="width: 100%">
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column label="买家信息" width="150">
          <template #default="{ row }">
            <div>{{ row.receiverName }}</div>
            <div class="text-gray">{{ row.receiverPhone }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="订单金额" width="100">
          <template #default="{ row }">
            <span class="amount">¥{{ row.totalAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="订单状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" width="170">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="viewOrder(row.id)">查看</el-button>
            <el-button v-if="row.status === 'REFUND_PENDING'" text type="success" size="small" @click="handleRefund(row.id, true)">
              同意退款
            </el-button>
            <el-button v-if="row.status === 'REFUND_PENDING'" text type="danger" size="small" @click="handleRefund(row.id, false)">
              拒绝退款
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next"
          @size-change="loadOrderList"
          @current-change="loadOrderList"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
/**
 * 管理员订单管理页面
 * @author lingbai
 * @version 1.0
 * @since 2025-12-02
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const orderList = ref([])

// 搜索表单
const searchForm = reactive({
  orderNo: '',
  status: ''
})

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

// 订单统计
const orderStats = reactive([
  { key: 'pending', label: '待付款', value: 0, status: 'PENDING' },
  { key: 'paid', label: '待发货', value: 0, status: 'PAID' },
  { key: 'shipped', label: '已发货', value: 0, status: 'SHIPPED' },
  { key: 'completed', label: '已完成', value: 0, status: 'COMPLETED' },
  { key: 'refund', label: '退款中', value: 0, status: 'REFUND_PENDING' }
])

// 状态映射
const statusMap = {
  'PENDING': { text: '待付款', type: 'warning' },
  'PAID': { text: '待发货', type: 'primary' },
  'SHIPPED': { text: '已发货', type: 'success' },
  'COMPLETED': { text: '已完成', type: 'success' },
  'CANCELLED': { text: '已取消', type: 'info' },
  'REFUND_PENDING': { text: '退款中', type: 'warning' },
  'REFUNDED': { text: '已退款', type: 'info' }
}

const getStatusText = (status) => statusMap[status]?.text || status
const getStatusType = (status) => statusMap[status]?.type || 'info'
const formatTime = (time) => time ? new Date(time).toLocaleString('zh-CN') : '-'

const filterByStatus = (status) => {
  searchForm.status = status
  handleSearch()
}

const handleSearch = () => {
  pagination.currentPage = 1
  loadOrderList()
}

const handleReset = () => {
  searchForm.orderNo = ''
  searchForm.status = ''
  handleSearch()
}

/**
 * 加载订单列表
 */
const loadOrderList = async () => {
  loading.value = true
  try {
    const params = new URLSearchParams({
      page: pagination.currentPage - 1,
      size: pagination.pageSize
    })
    if (searchForm.status) params.append('status', searchForm.status)
    if (searchForm.orderNo) params.append('orderNo', searchForm.orderNo)

    const response = await fetch(`/api/order-service/orders/admin?${params.toString()}`, {
      headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
    })
    const result = await response.json()
    
    if (result.success && result.data) {
      orderList.value = result.data.content || []
      pagination.total = result.data.totalElements || 0
    }
  } catch (error) {
    console.error('加载订单失败:', error)
    ElMessage.error('加载订单列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 加载订单统计
 */
const loadOrderStats = async () => {
  try {
    const response = await fetch('/api/order-service/orders/admin/stats', {
      headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
    })
    const result = await response.json()
    if (result.success && result.data) {
      orderStats.forEach(stat => {
        stat.value = result.data[stat.status.toLowerCase()] || 0
      })
    }
  } catch (error) {
    console.error('加载统计失败:', error)
  }
}

/**
 * 查看订单详情
 */
const viewOrder = (orderId) => {
  window.open(`/order/${orderId}`, '_blank')
}

/**
 * 处理退款
 */
const handleRefund = async (orderId, approved) => {
  const action = approved ? '同意' : '拒绝'
  try {
    await ElMessageBox.confirm(`确定要${action}该退款申请吗？`, '确认操作', { type: 'warning' })
    
    const response = await fetch(`/api/order-service/orders/${orderId}/refund/handle?approved=${approved}`, {
      method: 'PUT',
      headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
    })
    const result = await response.json()
    
    if (result.success || result.code === 200) {
      ElMessage.success(`${action}退款成功`)
      loadOrderList()
      loadOrderStats()
    } else {
      throw new Error(result.message)
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || `${action}退款失败`)
    }
  }
}

onMounted(() => {
  loadOrderList()
  loadOrderStats()
})
</script>

<style scoped>
.admin-orders { padding: 20px; background: #f5f5f5; min-height: 100vh; }
.page-header { margin-bottom: 20px; }
.page-title { margin: 0 0 8px; font-size: 24px; color: #262626; }
.page-desc { margin: 0; color: #8c8c8c; font-size: 14px; }
.stats-row { margin-bottom: 20px; }
.stat-card { text-align: center; cursor: pointer; }
.stat-value { font-size: 28px; font-weight: 600; color: #1890ff; }
.stat-label { font-size: 14px; color: #8c8c8c; margin-top: 8px; }
.search-card { margin-bottom: 20px; }
.table-card { margin-bottom: 20px; }
.pagination-wrapper { display: flex; justify-content: center; margin-top: 20px; }
.amount { color: #f5222d; font-weight: 500; }
.text-gray { color: #8c8c8c; font-size: 12px; }
</style>
