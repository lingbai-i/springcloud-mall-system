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
            <el-option label="待退款" value="refunding"></el-option>
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

    <!-- 发货对话框 -->
    <el-dialog
      v-model="showShipmentDialog"
      title="订单发货"
      width="500px"
      :close-on-click-modal="false">
      <el-form
        ref="shipmentFormRef"
        :model="shipmentForm"
        :rules="shipmentRules"
        label-width="100px">
        <el-form-item label="物流公司" prop="logisticsCompany">
          <el-select
            v-model="shipmentForm.logisticsCompany"
            placeholder="请选择物流公司"
            style="width: 100%">
            <el-option
              v-for="company in logisticsCompanies"
              :key="company.value"
              :label="company.label"
              :value="company.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="物流单号" prop="logisticsNo">
          <el-input
            v-model="shipmentForm.logisticsNo"
            placeholder="请输入物流单号"
            clearable />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showShipmentDialog = false">取消</el-button>
        <el-button type="primary" :loading="loading" @click="confirmShipment">
          确认发货
        </el-button>
      </template>
    </el-dialog>

    <!-- 订单详情对话框 -->
    <el-dialog
      v-model="showOrderDetailDialog"
      title="订单详情"
      width="700px"
      :close-on-click-modal="false">
      <div v-loading="orderDetailLoading">
        <template v-if="currentOrderDetail">
          <!-- 订单基本信息 -->
          <div class="detail-section">
            <h4>订单信息</h4>
            <el-descriptions :column="2" border size="small">
              <el-descriptions-item label="订单号">{{ currentOrderDetail.orderNo }}</el-descriptions-item>
              <el-descriptions-item label="订单状态">
                <el-tag :type="getStatusType(currentOrderDetail.status)" size="small">
                  {{ currentOrderDetail.statusText }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="下单时间">{{ formatTime(currentOrderDetail.createTime) }}</el-descriptions-item>
              <el-descriptions-item label="支付时间">{{ formatTime(currentOrderDetail.payTime) || '-' }}</el-descriptions-item>
              <el-descriptions-item label="支付方式">{{ currentOrderDetail.payMethod }}</el-descriptions-item>
              <el-descriptions-item label="发货时间">{{ formatTime(currentOrderDetail.shipTime) || '-' }}</el-descriptions-item>
            </el-descriptions>
          </div>

          <!-- 收货信息 -->
          <div class="detail-section">
            <h4>收货信息</h4>
            <el-descriptions :column="1" border size="small">
              <el-descriptions-item label="收货人">{{ currentOrderDetail.receiverName }}</el-descriptions-item>
              <el-descriptions-item label="联系电话">{{ currentOrderDetail.receiverPhone }}</el-descriptions-item>
              <el-descriptions-item label="收货地址">{{ currentOrderDetail.receiverAddress }}</el-descriptions-item>
            </el-descriptions>
          </div>

          <!-- 物流信息 -->
          <div class="detail-section" v-if="currentOrderDetail.logisticsNo">
            <h4>物流信息</h4>
            <el-descriptions :column="2" border size="small">
              <el-descriptions-item label="物流公司">{{ currentOrderDetail.logisticsCompany }}</el-descriptions-item>
              <el-descriptions-item label="物流单号">{{ currentOrderDetail.logisticsNo }}</el-descriptions-item>
            </el-descriptions>
          </div>

          <!-- 商品信息 -->
          <div class="detail-section">
            <h4>商品信息</h4>
            <div class="detail-products">
              <div v-for="item in currentOrderDetail.items" :key="item.id" class="detail-product-item">
                <img :src="item.image" class="product-img" />
                <div class="product-info">
                  <div class="product-name">{{ item.name }}</div>
                  <div class="product-spec">{{ item.spec }}</div>
                </div>
                <div class="product-price">{{ item.price }} 元 x {{ item.quantity }}</div>
                <div class="product-subtotal">{{ item.subtotal }} 元</div>
              </div>
            </div>
          </div>

          <!-- 费用信息 -->
          <div class="detail-section">
            <h4>费用明细</h4>
            <el-descriptions :column="2" border size="small">
              <el-descriptions-item label="商品金额">{{ currentOrderDetail.productAmount }} 元</el-descriptions-item>
              <el-descriptions-item label="运费">{{ currentOrderDetail.shippingFee }} 元</el-descriptions-item>
              <el-descriptions-item label="优惠金额">-{{ currentOrderDetail.discountAmount }} 元</el-descriptions-item>
              <el-descriptions-item label="实付金额">
                <span style="color: #f56c6c; font-weight: 600;">{{ currentOrderDetail.totalAmount }} 元</span>
              </el-descriptions-item>
            </el-descriptions>
          </div>

          <!-- 备注 -->
          <div class="detail-section" v-if="currentOrderDetail.remark">
            <h4>订单备注</h4>
            <p>{{ currentOrderDetail.remark }}</p>
          </div>
        </template>
      </div>
      <template #footer>
        <el-button @click="showOrderDetailDialog = false">关闭</el-button>
        <el-button 
          v-if="currentOrderDetail && currentOrderDetail.status === 'pending_shipment'" 
          type="primary" 
          @click="showOrderDetailDialog = false; shipOrder(currentOrderDetail.id)">
          去发货
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Download, Operation, Search, Refresh, ArrowDown,
  ShoppingBag, Clock, Van, CircleCheck, CircleClose, RefreshRight, Money
} from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const selectedOrders = ref([])
const showOrderDetail = ref(false)
const showShipmentDialog = ref(false)
const currentOrderId = ref(null)

// 订单详情对话框
const showOrderDetailDialog = ref(false)
const orderDetailLoading = ref(false)
const currentOrderDetail = ref(null)

// 发货对话框数据
const shipmentForm = reactive({
  logisticsCompany: '',
  logisticsNo: ''
})
const shipmentRules = {
  logisticsCompany: [{ required: true, message: '请选择物流公司', trigger: 'change' }],
  logisticsNo: [{ required: true, message: '请输入物流单号', trigger: 'blur' }]
}
const shipmentFormRef = ref(null)

// 物流公司选项
const logisticsCompanies = [
  { label: '顺丰速运', value: 'SF' },
  { label: '中通快递', value: 'ZTO' },
  { label: '圆通速递', value: 'YTO' },
  { label: '韵达快递', value: 'YD' },
  { label: '申通快递', value: 'STO' },
  { label: '极兔速递', value: 'JTSD' },
  { label: '邮政快递包裹', value: 'YZPY' },
  { label: 'EMS', value: 'EMS' },
  { label: '京东物流', value: 'JD' },
  { label: '德邦快递', value: 'DBL' }
]

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
    icon: 'CircleCheck',
    color: '#13c2c2',
    status: 'completed'
  }
])

// 订单列表
const orderList = ref([])

// 方法
const getStatusType = (status) => {
  // 订单状态颜色映射 - 使用不同颜色区分各种状态
  const statusMap = {
    // 前端标准状态
    'pending_payment': 'warning',    // 橙色 - 待付款
    'pending_shipment': 'primary',   // 蓝色 - 待发货
    'shipped': '',                   // 青色 - 已发货 (使用自定义样式)
    'completed': 'success',          // 绿色 - 已完成
    'cancelled': 'danger',           // 红色 - 已取消
    'refunding': 'warning',          // 橙色 - 待退款
    'refunded': 'info',              // 灰色 - 已退款
    // 后端枚举状态（大写）
    'PENDING': 'warning',
    'PENDING_PAYMENT': 'warning',
    'PAID': 'primary',
    'SHIPPED': '',
    'COMPLETED': 'success',
    'CANCELLED': 'danger',
    'REFUNDING': 'warning',
    'REFUNDED': 'info'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  // 订单状态文本映射
  const statusMap = {
    // 前端标准状态
    'pending_payment': '待付款',
    'pending_shipment': '待发货',
    'shipped': '已发货',
    'completed': '已完成',
    'cancelled': '已取消',
    'refunding': '待退款',
    'refunded': '已退款',
    // 后端枚举状态（大写）
    'PENDING': '待付款',
    'PENDING_PAYMENT': '待付款',
    'PAID': '待发货',
    'SHIPPED': '已发货',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消',
    'REFUNDING': '待退款',
    'REFUNDED': '已退款'
  }
  
  // 记录未知状态用于调试
  if (!statusMap[status]) {
    console.warn('未知订单状态:', status, typeof status)
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
    // 获取商家ID
    const merchantId = localStorage.getItem('merchantId')
    if (!merchantId) {
      ElMessage.warning('请先登录商家账号')
      return
    }
    
    // 构建查询参数
    const params = new URLSearchParams({
      merchantId: merchantId,
      page: pagination.currentPage - 1,
      size: pagination.pageSize
    })
    
    // 添加状态筛选
    if (searchForm.status) {
      params.append('status', mapStatusToBackend(searchForm.status))
    }
    
    // 调用后端API获取商家订单
    const response = await fetch(`/api/order-service/orders/merchant?${params.toString()}`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    
    const result = await response.json()
    console.log('商家订单列表响应:', result)
    
    if (result.success && result.data) {
      // 转换后端数据格式
      const rawOrders = result.data.content || []
      orderList.value = rawOrders.map(order => ({
        id: order.id,
        orderNo: order.orderNo,
        status: mapStatusToFrontend(order.status),
        createTime: order.createTime,
        buyerName: order.receiverName,
        buyerPhone: order.receiverPhone,
        totalAmount: order.totalAmount,
        paymentMethod: order.payMethod || 'alipay',
        receiverName: order.receiverName,
        receiverPhone: order.receiverPhone,
        shippingAddress: order.receiverAddress,
        trackingNo: order.logisticsNo,
        logisticsCompany: order.logisticsCompany,
        items: (order.orderItems || []).map(item => ({
          id: item.id,
          name: item.productName,
          image: item.productImage,
          spec: item.productSpec,
          price: item.productPrice,
          quantity: item.quantity
        }))
      }))
      
      pagination.total = result.data.totalElements || 0
      
      // 更新统计数据
      updateOrderStats()
    } else {
      throw new Error(result.message || '获取订单失败')
    }
    
  } catch (error) {
    console.error('加载订单列表失败:', error)
    ElMessage.error('加载订单列表失败')
    orderList.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

// 映射前端状态到后端状态
const mapStatusToBackend = (status) => {
  const statusMap = {
    'pending_payment': 'PENDING',
    'pending_shipment': 'PAID',
    'shipped': 'SHIPPED',
    'completed': 'COMPLETED',
    'cancelled': 'CANCELLED',
    'refunding': 'REFUNDING',
    'refunded': 'REFUNDED'
  }
  return statusMap[status] || status
}

// 映射后端状态到前端状态
const mapStatusToFrontend = (status) => {
  // 处理 null/undefined
  if (status === null || status === undefined) {
    console.warn('订单状态为空，使用默认值')
    return 'pending_payment'
  }
  
  // 处理数字状态码
  // 注意：这些数字来自 merchant-service 的 MerchantOrderServiceImpl.convertStatusToInt 方法
  // 映射关系：PENDING→1, PAID→2, SHIPPED→3, COMPLETED→5, CANCELLED→6, REFUNDING→7, REFUNDED→8
  if (typeof status === 'number') {
    const numericStatusMap = {
      0: 'pending_payment',    // 默认/未知状态
      1: 'pending_payment',    // PENDING - 待付款
      2: 'pending_shipment',   // PAID - 待发货（已付款）
      3: 'shipped',            // SHIPPED - 已发货
      4: 'completed',          // 已收货（兼容旧系统）
      5: 'completed',          // COMPLETED - 已完成
      6: 'cancelled',          // CANCELLED - 已取消
      7: 'refunding',          // REFUNDING - 待退款
      8: 'refunded'            // REFUNDED - 已退款
    }
    return numericStatusMap[status] || 'pending_payment'
  }
  
  // 处理字符串状态
  const statusMap = {
    // 后端枚举名称（大写）
    'PENDING': 'pending_payment',
    'PENDING_PAYMENT': 'pending_payment',
    'PAID': 'pending_shipment',
    'SHIPPED': 'shipped',
    'COMPLETED': 'completed',
    'CANCELLED': 'cancelled',
    'REFUNDING': 'refunding',
    'REFUNDED': 'refunded',
    // 后端枚举 code 值（小写）
    'pending': 'pending_payment',
    'paid': 'pending_shipment',
    'shipped': 'shipped',
    'completed': 'completed',
    'cancelled': 'cancelled',
    'refunding': 'refunding',
    'refunded': 'refunded',
    // 前端状态（直接返回）
    'pending_payment': 'pending_payment',
    'pending_shipment': 'pending_shipment'
  }
  
  if (typeof status === 'string') {
    // 先尝试直接匹配
    if (statusMap[status]) {
      return statusMap[status]
    }
    // 尝试大写匹配
    if (statusMap[status.toUpperCase()]) {
      return statusMap[status.toUpperCase()]
    }
    // 尝试小写匹配
    if (statusMap[status.toLowerCase()]) {
      return statusMap[status.toLowerCase()]
    }
    console.warn('未知订单状态:', status)
  }
  
  return 'pending_payment'
}

// 更新订单统计
const updateOrderStats = () => {
  const statusCounts = {}
  orderList.value.forEach(order => {
    statusCounts[order.status] = (statusCounts[order.status] || 0) + 1
  })
  
  orderStats.forEach(stat => {
    stat.value = String(statusCounts[stat.status] || 0)
  })
}

/**
 * 查看订单详情
 * @param {number} orderId - 订单ID
 */
const viewOrder = async (orderId) => {
  currentOrderId.value = orderId
  orderDetailLoading.value = true
  showOrderDetailDialog.value = true
  
  try {
    const merchantId = localStorage.getItem('merchantId')
    const response = await fetch(`/api/order-service/orders/${orderId}?merchantId=${merchantId}`, {
      headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
    })
    const result = await response.json()
    console.log('订单详情响应:', result)
    
    if (result.success && result.data) {
      const data = result.data
      currentOrderDetail.value = {
        id: data.id,
        orderNo: data.orderNo,
        status: mapStatusToFrontend(data.status),
        statusText: getStatusText(mapStatusToFrontend(data.status)),
        createTime: data.createTime,
        payTime: data.payTime,
        shipTime: data.shipTime,
        totalAmount: data.totalAmount,
        productAmount: data.productAmount || data.totalAmount,
        shippingFee: data.shippingFee || 0,
        discountAmount: data.discountAmount || 0,
        payMethod: data.payMethod || '-',
        receiverName: data.receiverName,
        receiverPhone: data.receiverPhone,
        receiverAddress: data.receiverAddress,
        logisticsCompany: data.logisticsCompany,
        logisticsNo: data.logisticsNo || data.trackingNo,
        remark: data.remark,
        items: (data.orderItems || []).map(item => ({
          id: item.id,
          name: item.productName,
          image: item.productImage || 'https://via.placeholder.com/80',
          spec: item.productSpec,
          price: item.productPrice,
          quantity: item.quantity,
          subtotal: item.subtotal || (item.productPrice * item.quantity)
        }))
      }
    } else {
      throw new Error(result.message || '获取订单详情失败')
    }
  } catch (error) {
    console.error('获取订单详情失败:', error)
    ElMessage.error('获取订单详情失败')
    showOrderDetailDialog.value = false
  } finally {
    orderDetailLoading.value = false
  }
}

/**
 * 打开发货对话框
 * @param {number} orderId - 订单ID
 */
const shipOrder = (orderId) => {
  currentOrderId.value = orderId
  // 重置表单
  shipmentForm.logisticsCompany = ''
  shipmentForm.logisticsNo = ''
  showShipmentDialog.value = true
}

/**
 * 确认发货
 * 调用后端API标记订单为已发货状态
 */
const confirmShipment = async () => {
  // 表单验证
  if (!shipmentFormRef.value) return
  
  try {
    await shipmentFormRef.value.validate()
  } catch (error) {
    return
  }
  
  const merchantId = localStorage.getItem('merchantId')
  if (!merchantId) {
    ElMessage.warning('请先登录商家账号')
    return
  }
  
  loading.value = true
  try {
    // 调用后端发货API
    const params = new URLSearchParams({
      merchantId: merchantId,
      logisticsCompany: getLogisticsCompanyName(shipmentForm.logisticsCompany),
      logisticsNo: shipmentForm.logisticsNo
    })
    
    const response = await fetch(`/api/order-service/orders/${currentOrderId.value}/ship?${params.toString()}`, {
      method: 'PUT',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`,
        'Content-Type': 'application/json'
      }
    })
    
    const result = await response.json()
    console.log('发货响应:', result)
    
    if (result.success || result.code === 200) {
      ElMessage.success('发货成功，订单状态已更新')
      showShipmentDialog.value = false
      // 刷新订单列表
      loadOrderList()
    } else {
      throw new Error(result.message || '发货失败')
    }
  } catch (error) {
    console.error('发货失败:', error)
    ElMessage.error(error.message || '发货失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

/**
 * 获取物流公司名称
 * @param {string} code - 物流公司代码
 * @returns {string} 物流公司名称
 */
const getLogisticsCompanyName = (code) => {
  const company = logisticsCompanies.find(c => c.value === code)
  return company ? company.label : code
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

/* 订单详情对话框样式 */
.detail-section {
  margin-bottom: 20px;
}

.detail-section h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}

.detail-products {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-product-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #fafafa;
  border-radius: 6px;
}

.detail-product-item .product-img {
  width: 60px;
  height: 60px;
  border-radius: 4px;
  object-fit: cover;
}

.detail-product-item .product-info {
  flex: 1;
}

.detail-product-item .product-name {
  font-size: 14px;
  color: #303133;
  margin-bottom: 4px;
}

.detail-product-item .product-spec {
  font-size: 12px;
  color: #909399;
}

.detail-product-item .product-price {
  font-size: 13px;
  color: #606266;
  width: 100px;
  text-align: right;
}

.detail-product-item .product-subtotal {
  font-size: 14px;
  font-weight: 500;
  color: #f56c6c;
  width: 80px;
  text-align: right;
}

/* 订单状态标签颜色 - 使用不同颜色区分各种状态 */
.merchant-orders :deep(.el-tag) {
  font-weight: 500;
  border-radius: 4px;
}

/* 待付款状态 - 橙色 */
.merchant-orders :deep(.el-tag--warning) {
  background-color: #fff7e6 !important;
  border-color: #ffd591 !important;
  color: #fa8c16 !important;
}

/* 待发货状态 - 蓝色 */
.merchant-orders :deep(.el-tag--primary) {
  background-color: #e6f7ff !important;
  border-color: #91d5ff !important;
  color: #1890ff !important;
}

/* 已发货状态 - 青色 */
.merchant-orders :deep(.el-tag:not([class*="el-tag--"])) {
  background-color: #e6fffb !important;
  border-color: #87e8de !important;
  color: #13c2c2 !important;
}

/* 已完成状态 - 绿色 */
.merchant-orders :deep(.el-tag--success) {
  background-color: #f6ffed !important;
  border-color: #b7eb8f !important;
  color: #52c41a !important;
}

/* 已取消状态 - 红色 */
.merchant-orders :deep(.el-tag--danger) {
  background-color: #fff2f0 !important;
  border-color: #ffccc7 !important;
  color: #ff4d4f !important;
}

/* 已退款状态 - 灰色 */
.merchant-orders :deep(.el-tag--info) {
  background-color: #fafafa !important;
  border-color: #d9d9d9 !important;
  color: #8c8c8c !important;
}
</style>