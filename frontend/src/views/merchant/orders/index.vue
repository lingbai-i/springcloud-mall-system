<template>
  <div class="merchant-orders">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">订单管理</h2>
        <p class="page-desc">管理您的订单信息，包括订单处理、发货、退款等操作</p>
      </div>
      <div class="header-right">
        <el-button @click="handleExport">
          <el-icon><Download /></el-icon>
          导出订单
        </el-button>
        <el-button type="primary" @click="handleBatchProcess">
          <el-icon><Operation /></el-icon>
          批量处理
        </el-button>
      </div>
    </div>

    <!-- 订单状态统计 -->
    <div class="stats-section">
      <el-row :gutter="20">
        <el-col :xs="12" :sm="6" v-for="stat in orderStats" :key="stat.key">
          <el-card shadow="hover" class="stat-card" @click="filterByStatus(stat.status)">
            <div class="stat-content">
              <div class="stat-icon" :style="{ backgroundColor: stat.color }">
                <el-icon :size="20">
                  <component :is="stat.icon" />
                </el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stat.value }}</div>
                <div class="stat-label">{{ stat.label }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 搜索和筛选 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" :inline="true" class="search-form">
        <el-form-item label="订单号">
          <el-input
            v-model="searchForm.orderNo"
            placeholder="请输入订单号"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        
        <el-form-item label="买家信息">
          <el-input
            v-model="searchForm.buyer"
            placeholder="买家姓名/手机号"
            clearable
            style="width: 180px"
          />
        </el-form-item>
        
        <el-form-item label="订单状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 150px">
            <el-option label="全部状态" value=""></el-option>
            <el-option label="待付款" value="pending_payment"></el-option>
            <el-option label="待发货" value="pending_shipment"></el-option>
            <el-option label="已发货" value="shipped"></el-option>
            <el-option label="已完成" value="completed"></el-option>
            <el-option label="已取消" value="cancelled"></el-option>
            <el-option label="退款中" value="refunding"></el-option>
            <el-option label="已退款" value="refunded"></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="支付方式">
          <el-select v-model="searchForm.paymentMethod" placeholder="支付方式" clearable style="width: 120px">
            <el-option label="全部" value=""></el-option>
            <el-option label="微信支付" value="wechat"></el-option>
            <el-option label="支付宝" value="alipay"></el-option>
            <el-option label="银行卡" value="bank_card"></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="下单时间">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 240px"
          />
        </el-form-item>
        
        <el-form-item label="金额范围">
          <el-input
            v-model="searchForm.minAmount"
            placeholder="最低金额"
            type="number"
            style="width: 100px"
          />
          <span style="margin: 0 8px">-</span>
          <el-input
            v-model="searchForm.maxAmount"
            placeholder="最高金额"
            type="number"
            style="width: 100px"
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
    </el-card>

    <!-- 订单列表 -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>订单列表</span>
          <div class="header-actions">
            <el-button
              v-if="selectedOrders.length > 0"
              type="primary"
              size="small"
              @click="handleBatchShip">
              批量发货 ({{ selectedOrders.length }})
            </el-button>
            <el-button
              v-if="selectedOrders.length > 0"
              type="warning"
              size="small"
              @click="handleBatchCancel">
              批量取消 ({{ selectedOrders.length }})
            </el-button>
          </div>
        </div>
      </template>

      <el-table
        :data="orderList"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        style="width: 100%">
        
        <el-table-column type="selection" width="55" />
        
        <el-table-column label="订单信息" min-width="280">
          <template #default="{ row }">
            <div class="order-info">
              <div class="order-header">
                <span class="order-no">{{ row.orderNo }}</span>
                <el-tag :type="getStatusType(row.status)" size="small">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </div>
              <div class="order-time">{{ formatTime(row.createTime) }}</div>
              <div class="buyer-info">
                <span>买家：{{ row.buyerName }}</span>
                <span class="phone">{{ row.buyerPhone }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="商品信息" min-width="300">
          <template #default="{ row }">
            <div class="product-list">
              <div
                v-for="(item, index) in row.items"
                :key="index"
                class="product-item">
                <el-image
                  :src="item.image"
                  class="product-image"
                  fit="cover"
                />
                <div class="product-details">
                  <div class="product-name">{{ item.name }}</div>
                  <div class="product-spec">{{ item.spec }}</div>
                  <div class="product-price">
                    ¥{{ item.price }} × {{ item.quantity }}
                  </div>
                </div>
              </div>
              <div v-if="row.items.length > 2" class="more-items">
                还有 {{ row.items.length - 2 }} 件商品...
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="totalAmount" label="订单金额" width="120" sortable>
          <template #default="{ row }">
            <div class="amount-info">
              <div class="total-amount">¥{{ row.totalAmount }}</div>
              <div class="payment-method">{{ getPaymentMethodText(row.paymentMethod) }}</div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="收货信息" width="200">
          <template #default="{ row }">
            <div class="shipping-info">
              <div class="receiver">{{ row.receiverName }}</div>
              <div class="phone">{{ row.receiverPhone }}</div>
              <div class="address" :title="row.shippingAddress">
                {{ row.shippingAddress }}
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="物流信息" width="150">
          <template #default="{ row }">
            <div class="logistics-info" v-if="row.trackingNo">
              <div class="tracking-no">{{ row.trackingNo }}</div>
              <div class="logistics-company">{{ row.logisticsCompany }}</div>
              <el-button text type="primary" size="small" @click="trackOrder(row.trackingNo)">
                查看物流
              </el-button>
            </div>
            <span v-else class="no-logistics">-</span>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button text type="primary" size="small" @click="viewOrder(row.id)">
                查看
              </el-button>
              
              <el-button
                v-if="row.status === 'pending_shipment'"
                text
                type="success"
                size="small"
                @click="shipOrder(row.id)">
                发货
              </el-button>
              
              <el-button
                v-if="['pending_payment', 'pending_shipment'].includes(row.status)"
                text
                type="warning"
                size="small"
                @click="cancelOrder(row.id)">
                取消
              </el-button>
              
              <el-dropdown @command="(command) => handleOrderAction(command, row)">
                <el-button text type="primary" size="small">
                  更多<el-icon class="el-icon--right"><ArrowDown /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item :command="`print_${row.id}`">打印订单</el-dropdown-item>
                    <el-dropdown-item :command="`contact_${row.id}`">联系买家</el-dropdown-item>
                    <el-dropdown-item :command="`refund_${row.id}`" v-if="['completed', 'shipped'].includes(row.status)">
                      处理退款
                    </el-dropdown-item>
                    <el-dropdown-item :command="`remark_${row.id}`" divided>添加备注</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Download, Operation, Search, Refresh, ArrowDown,
  ShoppingBag, Clock, Van, CheckCircle, XCircle, RefreshRight, Money
} from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const selectedOrders = ref([])
const showOrderDetail = ref(false)
const showShipmentDialog = ref(false)
const currentOrderId = ref(null)

// 搜索表单
const searchForm = reactive({
  orderNo: '',
  buyer: '',
  status: '',
  paymentMethod: '',
  dateRange: [],
  minAmount: '',
  maxAmount: ''
})

// 分页信息
const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

// 订单统计
const orderStats = reactive([
  {
    key: 'pending_payment',
    label: '待付款',
    value: '0',
    icon: 'Clock',
    color: '#faad14',
    status: 'pending_payment'
  },
  {
    key: 'pending_shipment',
    label: '待发货',
    value: '0',
    icon: 'ShoppingBag',
    color: '#1890ff',
    status: 'pending_shipment'
  },
  {
    key: 'shipped',
    label: '已发货',
    value: '0',
    icon: 'Van',
    color: '#52c41a',
    status: 'shipped'
  },
  {
    key: 'completed',
    label: '已完成',
    value: '0',
    icon: 'CheckCircle',
    color: '#13c2c2',
    status: 'completed'
  }
])

// 订单列表
const orderList = ref([])

// 方法
const getStatusType = (status) => {
  const statusMap = {
    'pending_payment': 'warning',
    'pending_shipment': 'primary',
    'shipped': 'success',
    'completed': 'success',
    'cancelled': 'info',
    'refunding': 'warning',
    'refunded': 'danger'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    'pending_payment': '待付款',
    'pending_shipment': '待发货',
    'shipped': '已发货',
    'completed': '已完成',
    'cancelled': '已取消',
    'refunding': '退款中',
    'refunded': '已退款'
  }
  return statusMap[status] || '未知'
}

const getPaymentMethodText = (method) => {
  const methodMap = {
    'wechat': '微信支付',
    'alipay': '支付宝',
    'bank_card': '银行卡'
  }
  return methodMap[method] || '未知'
}

const formatTime = (time) => {
  return new Date(time).toLocaleString('zh-CN')
}

const filterByStatus = (status) => {
  searchForm.status = status
  handleSearch()
}

const handleSearch = () => {
  pagination.currentPage = 1
  loadOrderList()
}

const handleReset = () => {
  Object.keys(searchForm).forEach(key => {
    if (Array.isArray(searchForm[key])) {
      searchForm[key] = []
    } else {
      searchForm[key] = ''
    }
  })
  handleSearch()
}

const handleSelectionChange = (selection) => {
  selectedOrders.value = selection
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  loadOrderList()
}

const handleCurrentChange = (page) => {
  pagination.currentPage = page
  loadOrderList()
}

const loadOrderList = async () => {
  loading.value = true
  try {
    // TODO: 调用真实的API
    // const response = await orderApi.getOrderList({
    //   ...searchForm,
    //   page: pagination.currentPage,
    //   pageSize: pagination.pageSize
    // })
    // orderList.value = response.data.list || []
    // pagination.total = response.data.total || 0
    
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500))
    orderList.value = []
    pagination.total = 0
    
  } catch (error) {
    ElMessage.error('加载订单列表失败')
  } finally {
    loading.value = false
  }
}

const viewOrder = (orderId) => {
  ElMessage.info('订单详情功能开发中...')
  console.log('查看订单:', orderId)
}

const shipOrder = (orderId) => {
  ElMessage.info('发货功能开发中...')
  console.log('发货订单:', orderId)
}

const cancelOrder = async (orderId) => {
  try {
    await ElMessageBox.confirm(
      '确定要取消该订单吗？取消后不可恢复。',
      '确认取消',
      {
        confirmButtonText: '确定取消',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    ElMessage.info('取消订单功能开发中...')
    console.log('取消订单:', orderId)
    
  } catch (error) {
    // 用户取消操作
  }
}

const trackOrder = (trackingNo) => {
  // 打开物流跟踪页面
  window.open(`https://www.kuaidi100.com/chaxun?com=auto&nu=${trackingNo}`, '_blank')
}

const handleOrderAction = async (command, row) => {
  const [action, id] = command.split('_')
  
  switch (action) {
    case 'print':
      handlePrintOrder(id)
      break
    case 'contact':
      handleContactBuyer(id)
      break
    case 'refund':
      handleRefund(id)
      break
    case 'remark':
      handleAddRemark(id)
      break
  }
}

const handlePrintOrder = (orderId) => {
  ElMessage.info('打印订单功能开发中...')
}

const handleContactBuyer = (orderId) => {
  ElMessage.info('联系买家功能开发中...')
}

const handleRefund = (orderId) => {
  ElMessage.info('退款处理功能开发中...')
}

const handleAddRemark = (orderId) => {
  ElMessage.info('添加备注功能开发中...')
}

const handleBatchShip = () => {
  ElMessage.info('批量发货功能开发中...')
}

const handleBatchCancel = () => {
  ElMessage.info('批量取消功能开发中...')
}

const handleBatchProcess = () => {
  ElMessage.info('批量处理功能开发中...')
}

const handleExport = () => {
  ElMessage.info('导出订单功能开发中...')
}

const handleShipmentSuccess = () => {
  loadOrderList()
}

// 生命周期
onMounted(() => {
  loadOrderList()
})
</script>

<style scoped>
.merchant-orders {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: calc(100vh - 60px);
}

/* 页面头部 */
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
  color: #262626;
}

.page-desc {
  margin: 0;
  color: #8c8c8c;
  font-size: 14px;
}

.header-right {
  display: flex;
  gap: 12px;
}

/* 统计区域 */
.stats-section {
  margin-bottom: 20px;
}

.stat-card {
  height: 80px;
  cursor: pointer;
  transition: transform 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-content {
  display: flex;
  align-items: center;
  height: 100%;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  margin-right: 12px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 20px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 2px;
}

.stat-label {
  font-size: 12px;
  color: #8c8c8c;
}

/* 搜索区域 */
.search-card {
  margin-bottom: 20px;
}

.search-form {
  margin-bottom: -18px;
}

/* 表格区域 */
.table-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 8px;
}

/* 订单信息 */
.order-info {
  padding: 8px 0;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.order-no {
  font-weight: 600;
  color: #262626;
  font-size: 14px;
}

.order-time {
  font-size: 12px;
  color: #8c8c8c;
  margin-bottom: 4px;
}

.buyer-info {
  font-size: 12px;
  color: #595959;
}

.phone {
  margin-left: 8px;
  color: #8c8c8c;
}

/* 商品信息 */
.product-list {
  padding: 8px 0;
}

.product-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.product-item:last-child {
  margin-bottom: 0;
}

.product-image {
  width: 40px;
  height: 40px;
  border-radius: 4px;
  margin-right: 8px;
  flex-shrink: 0;
}

.product-details {
  flex: 1;
  min-width: 0;
}

.product-name {
  font-size: 13px;
  color: #262626;
  margin-bottom: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-spec {
  font-size: 11px;
  color: #8c8c8c;
  margin-bottom: 2px;
}

.product-price {
  font-size: 12px;
  color: #f5222d;
  font-weight: 500;
}

.more-items {
  font-size: 12px;
  color: #8c8c8c;
  text-align: center;
  padding: 4px 0;
}

/* 金额信息 */
.amount-info {
  text-align: right;
}

.total-amount {
  font-size: 16px;
  font-weight: 600;
  color: #f5222d;
  margin-bottom: 4px;
}

.payment-method {
  font-size: 12px;
  color: #8c8c8c;
}

/* 收货信息 */
.shipping-info {
  font-size: 12px;
  line-height: 1.4;
}

.receiver {
  font-weight: 500;
  color: #262626;
  margin-bottom: 2px;
}

.address {
  color: #595959;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-top: 2px;
}

/* 物流信息 */
.logistics-info {
  font-size: 12px;
  text-align: center;
}

.tracking-no {
  font-weight: 500;
  color: #262626;
  margin-bottom: 2px;
}

.logistics-company {
  color: #8c8c8c;
  margin-bottom: 4px;
}

.no-logistics {
  color: #d9d9d9;
  font-size: 14px;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .merchant-orders {
    padding: 16px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 16px;
  }
  
  .header-right {
    width: 100%;
    justify-content: flex-start;
    flex-wrap: wrap;
  }
  
  .search-form {
    flex-direction: column;
  }
  
  .search-form .el-form-item {
    margin-right: 0;
    margin-bottom: 16px;
  }
  
  .stat-card {
    margin-bottom: 12px;
  }
  
  .product-item {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .product-image {
    margin-bottom: 4px;
  }
  
  .action-buttons {
    flex-direction: row;
    flex-wrap: wrap;
  }
}
</style>