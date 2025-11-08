<template>
  <div class="merchant-dashboard">
    <!-- 欢迎区域 -->
    <div class="welcome-section">
      <el-card shadow="never" class="welcome-card">
        <div class="welcome-content">
          <div class="welcome-info">
            <h2>欢迎回来，{{ userStore.userInfo?.shopName || '商家' }}！</h2>
            <p class="welcome-desc">今天是 {{ currentDate }}，祝您生意兴隆！</p>
            <div class="quick-actions">
              <el-button type="primary" @click="$router.push('/merchant/products/add')">
                <el-icon><Plus /></el-icon>
                添加商品
              </el-button>
              <el-button @click="$router.push('/merchant/orders')">
                <el-icon><List /></el-icon>
                查看订单
              </el-button>
              <el-button @click="$router.push('/merchant/shop/settings')">
                <el-icon><Setting /></el-icon>
                店铺设置
              </el-button>
            </div>
          </div>
          <div class="welcome-avatar">
            <el-avatar :size="80" :src="userStore.userInfo?.avatar">
              <el-icon><User /></el-icon>
            </el-avatar>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 数据概览 -->
    <div class="overview-section">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="6" v-for="item in overviewData" :key="item.key">
          <el-card shadow="hover" class="overview-card">
            <div class="overview-content">
              <div class="overview-icon" :style="{ backgroundColor: item.color }">
                <el-icon :size="24">
                  <component :is="item.icon" />
                </el-icon>
              </div>
              <div class="overview-info">
                <div class="overview-value">{{ item.value }}</div>
                <div class="overview-label">{{ item.label }}</div>
                <div class="overview-trend" :class="item.trend > 0 ? 'positive' : 'negative'">
                  <el-icon>
                    <component :is="item.trend > 0 ? 'ArrowUp' : 'ArrowDown'" />
                  </el-icon>
                  {{ Math.abs(item.trend) }}%
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <el-row :gutter="20">
        <!-- 销售趋势 -->
        <el-col :xs="24" :lg="12">
          <el-card shadow="never" class="chart-card">
            <template #header>
              <div class="card-header">
                <span>销售趋势</span>
                <el-radio-group v-model="salesPeriod" size="small">
                  <el-radio-button value="7days">近7天</el-radio-button>
                  <el-radio-button value="30days">近30天</el-radio-button>
                  <el-radio-button value="90days">近90天</el-radio-button>
                </el-radio-group>
              </div>
            </template>
            <div ref="salesChartRef" class="chart-container"></div>
          </el-card>
        </el-col>

        <!-- 商品销量排行 -->
        <el-col :xs="24" :lg="12">
          <el-card shadow="never" class="chart-card">
            <template #header>
              <span>商品销量排行</span>
            </template>
            <div ref="productChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 订单和商品状态 -->
    <div class="status-section">
      <el-row :gutter="20">
        <!-- 最新订单 -->
        <el-col :xs="24" :lg="14">
          <el-card shadow="never" class="status-card">
            <template #header>
              <div class="card-header">
                <span>最新订单</span>
                <el-button text type="primary" @click="$router.push('/merchant/orders')">
                  查看全部
                </el-button>
              </div>
            </template>
            <el-table :data="recentOrders" style="width: 100%" max-height="300">
              <el-table-column prop="orderNo" label="订单号" width="140">
                <template #default="{ row }">
                  <el-button text type="primary" @click="viewOrder(row.id)">
                    {{ row.orderNo }}
                  </el-button>
                </template>
              </el-table-column>
              <el-table-column prop="customerName" label="客户" width="100"></el-table-column>
              <el-table-column prop="totalAmount" label="金额" width="80">
                <template #default="{ row }">
                  <span class="amount">¥{{ row.totalAmount }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="80">
                <template #default="{ row }">
                  <el-tag :type="getOrderStatusType(row.status)" size="small">
                    {{ getOrderStatusText(row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="下单时间" min-width="120">
                <template #default="{ row }">
                  {{ formatTime(row.createTime) }}
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>

        <!-- 待处理事项 -->
        <el-col :xs="24" :lg="10">
          <el-card shadow="never" class="status-card">
            <template #header>
              <span>待处理事项</span>
            </template>
            <div class="pending-items">
              <div class="pending-item" v-for="item in pendingItems" :key="item.type">
                <div class="pending-icon" :style="{ backgroundColor: item.color }">
                  <el-icon>
                    <component :is="item.icon" />
                  </el-icon>
                </div>
                <div class="pending-content">
                  <div class="pending-title">{{ item.title }}</div>
                  <div class="pending-count">{{ item.count }} 项</div>
                </div>
                <el-button text type="primary" @click="handlePendingItem(item.type)">
                  处理
                </el-button>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 店铺数据分析 -->
    <div class="analysis-section">
      <el-card shadow="never" class="analysis-card">
        <template #header>
          <span>店铺数据分析</span>
        </template>
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :md="6" v-for="item in analysisData" :key="item.key">
            <div class="analysis-item">
              <div class="analysis-label">{{ item.label }}</div>
              <div class="analysis-value">{{ item.value }}</div>
              <div class="analysis-desc">{{ item.desc }}</div>
            </div>
          </el-col>
        </el-row>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useUserStore } from '@/stores/user'
import * as echarts from 'echarts'
import {
  Plus, List, Setting, User, ShoppingBag, Money, TrendCharts, Star,
  ArrowUp, ArrowDown, Clock, Warning, Document, Truck
} from '@element-plus/icons-vue'

const userStore = useUserStore()

// 响应式数据
const currentDate = ref('')
const salesPeriod = ref('7days')
const salesChartRef = ref()
const productChartRef = ref()

// 数据概览
const overviewData = reactive([
  {
    key: 'todaySales',
    label: '今日销售额',
    value: '¥12,580',
    icon: 'Money',
    color: '#52c41a',
    trend: 15.6
  },
  {
    key: 'todayOrders',
    label: '今日订单',
    value: '156',
    icon: 'ShoppingBag',
    color: '#1890ff',
    trend: 8.2
  },
  {
    key: 'totalProducts',
    label: '商品总数',
    value: '1,248',
    icon: 'List',
    color: '#722ed1',
    trend: 2.1
  },
  {
    key: 'shopRating',
    label: '店铺评分',
    value: '4.8',
    icon: 'Star',
    color: '#fa8c16',
    trend: 0.3
  }
])

// 最新订单
const recentOrders = reactive([
  {
    id: 1,
    orderNo: 'M202401150001',
    customerName: '张三',
    totalAmount: '299.00',
    status: 'pending_payment',
    createTime: '2024-01-15 14:30:25'
  },
  {
    id: 2,
    orderNo: 'M202401150002',
    customerName: '李四',
    totalAmount: '158.50',
    status: 'pending_shipment',
    createTime: '2024-01-15 13:45:12'
  },
  {
    id: 3,
    orderNo: 'M202401150003',
    customerName: '王五',
    totalAmount: '89.90',
    status: 'shipped',
    createTime: '2024-01-15 12:20:08'
  },
  {
    id: 4,
    orderNo: 'M202401150004',
    customerName: '赵六',
    totalAmount: '456.00',
    status: 'completed',
    createTime: '2024-01-15 11:15:33'
  }
])

// 待处理事项
const pendingItems = reactive([
  {
    type: 'pending_orders',
    title: '待发货订单',
    count: 23,
    icon: 'Truck',
    color: '#fa541c'
  },
  {
    type: 'low_stock',
    title: '库存不足',
    count: 8,
    icon: 'Warning',
    color: '#faad14'
  },
  {
    type: 'pending_reviews',
    title: '待回复评价',
    count: 12,
    icon: 'Document',
    color: '#13c2c2'
  },
  {
    type: 'expired_products',
    title: '即将下架',
    count: 5,
    icon: 'Clock',
    color: '#eb2f96'
  }
])

// 店铺数据分析
const analysisData = reactive([
  {
    key: 'conversion_rate',
    label: '转化率',
    value: '3.2%',
    desc: '较上月提升0.5%'
  },
  {
    key: 'avg_order_value',
    label: '客单价',
    value: '¥186',
    desc: '较上月提升¥12'
  },
  {
    key: 'return_rate',
    label: '退货率',
    value: '2.1%',
    desc: '较上月下降0.3%'
  },
  {
    key: 'customer_satisfaction',
    label: '客户满意度',
    value: '96.8%',
    desc: '较上月提升1.2%'
  }
])

// 方法
const initCurrentDate = () => {
  const now = new Date()
  const options = {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  }
  currentDate.value = now.toLocaleDateString('zh-CN', options)
}

const initSalesChart = () => {
  const chart = echarts.init(salesChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      }
    },
    legend: {
      data: ['销售额', '订单量']
    },
    xAxis: {
      type: 'category',
      data: ['01-09', '01-10', '01-11', '01-12', '01-13', '01-14', '01-15']
    },
    yAxis: [
      {
        type: 'value',
        name: '销售额(元)',
        position: 'left'
      },
      {
        type: 'value',
        name: '订单量',
        position: 'right'
      }
    ],
    series: [
      {
        name: '销售额',
        type: 'line',
        smooth: true,
        data: [8200, 9500, 7800, 11200, 13600, 10800, 12580],
        itemStyle: {
          color: '#52c41a'
        }
      },
      {
        name: '订单量',
        type: 'bar',
        yAxisIndex: 1,
        data: [82, 95, 78, 112, 136, 108, 156],
        itemStyle: {
          color: '#1890ff'
        }
      }
    ]
  }
  
  chart.setOption(option)
  
  // 响应式调整
  window.addEventListener('resize', () => {
    chart.resize()
  })
}

const initProductChart = () => {
  const chart = echarts.init(productChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'item'
    },
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: '18',
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: [
          { value: 335, name: '智能手机' },
          { value: 310, name: '笔记本电脑' },
          { value: 234, name: '平板电脑' },
          { value: 135, name: '智能手表' },
          { value: 148, name: '耳机音响' }
        ]
      }
    ]
  }
  
  chart.setOption(option)
  
  // 响应式调整
  window.addEventListener('resize', () => {
    chart.resize()
  })
}

const getOrderStatusType = (status) => {
  const statusMap = {
    'pending_payment': 'warning',
    'pending_shipment': 'primary',
    'shipped': 'info',
    'completed': 'success',
    'cancelled': 'danger'
  }
  return statusMap[status] || 'info'
}

const getOrderStatusText = (status) => {
  const statusMap = {
    'pending_payment': '待付款',
    'pending_shipment': '待发货',
    'shipped': '已发货',
    'completed': '已完成',
    'cancelled': '已取消'
  }
  return statusMap[status] || '未知'
}

const formatTime = (time) => {
  return new Date(time).toLocaleString('zh-CN')
}

const viewOrder = (orderId) => {
  // 跳转到订单详情页
  console.log('查看订单:', orderId)
}

const handlePendingItem = (type) => {
  // 处理待办事项
  const routeMap = {
    'pending_orders': '/merchant/orders?status=pending_shipment',
    'low_stock': '/merchant/products?filter=low_stock',
    'pending_reviews': '/merchant/reviews?status=pending',
    'expired_products': '/merchant/products?filter=expiring'
  }
  
  if (routeMap[type]) {
    // this.$router.push(routeMap[type])
    console.log('跳转到:', routeMap[type])
  }
}

// 生命周期
onMounted(() => {
  initCurrentDate()
  
  nextTick(() => {
    initSalesChart()
    initProductChart()
  })
})
</script>

<style scoped>
.merchant-dashboard {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: calc(100vh - 60px);
}

/* 欢迎区域 */
.welcome-section {
  margin-bottom: 20px;
}

.welcome-card {
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
  color: white;
}

.welcome-card :deep(.el-card__body) {
  padding: 30px;
}

.welcome-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.welcome-info h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
}

.welcome-desc {
  margin: 0 0 20px 0;
  opacity: 0.9;
  font-size: 14px;
}

.quick-actions {
  display: flex;
  gap: 12px;
}

.quick-actions .el-button {
  background-color: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.3);
  color: white;
}

.quick-actions .el-button:hover {
  background-color: rgba(255, 255, 255, 0.3);
  border-color: rgba(255, 255, 255, 0.4);
}

.welcome-avatar {
  flex-shrink: 0;
}

/* 数据概览 */
.overview-section {
  margin-bottom: 20px;
}

.overview-card {
  height: 120px;
  transition: transform 0.2s;
}

.overview-card:hover {
  transform: translateY(-2px);
}

.overview-content {
  display: flex;
  align-items: center;
  height: 100%;
}

.overview-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  margin-right: 16px;
}

.overview-info {
  flex: 1;
}

.overview-value {
  font-size: 24px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 4px;
}

.overview-label {
  font-size: 14px;
  color: #8c8c8c;
  margin-bottom: 4px;
}

.overview-trend {
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 2px;
}

.overview-trend.positive {
  color: #52c41a;
}

.overview-trend.negative {
  color: #ff4d4f;
}

/* 图表区域 */
.charts-section {
  margin-bottom: 20px;
}

.chart-card {
  height: 400px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  height: 320px;
  width: 100%;
}

/* 状态区域 */
.status-section {
  margin-bottom: 20px;
}

.status-card .el-table {
  font-size: 14px;
}

.amount {
  font-weight: 600;
  color: #52c41a;
}

.pending-items {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.pending-item {
  display: flex;
  align-items: center;
  padding: 12px;
  background-color: #fafafa;
  border-radius: 8px;
  transition: background-color 0.2s;
}

.pending-item:hover {
  background-color: #f0f0f0;
}

.pending-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  margin-right: 12px;
}

.pending-content {
  flex: 1;
}

.pending-title {
  font-size: 14px;
  color: #262626;
  margin-bottom: 2px;
}

.pending-count {
  font-size: 12px;
  color: #8c8c8c;
}

/* 分析区域 */
.analysis-section {
  margin-bottom: 20px;
}

.analysis-item {
  text-align: center;
  padding: 20px;
  border-right: 1px solid #f0f0f0;
}

.analysis-item:last-child {
  border-right: none;
}

.analysis-label {
  font-size: 14px;
  color: #8c8c8c;
  margin-bottom: 8px;
}

.analysis-value {
  font-size: 24px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 4px;
}

.analysis-desc {
  font-size: 12px;
  color: #52c41a;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .merchant-dashboard {
    padding: 16px;
  }
  
  .welcome-content {
    flex-direction: column;
    text-align: center;
    gap: 20px;
  }
  
  .quick-actions {
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .overview-card {
    margin-bottom: 16px;
  }
  
  .chart-card {
    height: 300px;
    margin-bottom: 16px;
  }
  
  .chart-container {
    height: 220px;
  }
  
  .analysis-item {
    border-right: none;
    border-bottom: 1px solid #f0f0f0;
    margin-bottom: 16px;
  }
  
  .analysis-item:last-child {
    border-bottom: none;
    margin-bottom: 0;
  }
}
</style>