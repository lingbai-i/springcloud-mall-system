<template>
  <div class="admin-dashboard">
    <!-- 欢迎区域 -->
    <div class="welcome-section">
      <div class="welcome-content">
        <h1 class="welcome-title">欢迎回来，{{ userStore.username }}！</h1>
        <p class="welcome-subtitle">今天是 {{ currentDate }}，祝您工作愉快</p>
      </div>
      <div class="welcome-actions">
        <el-button type="primary" @click="handleQuickAction('users')">
          <el-icon><User /></el-icon>
          用户管理
        </el-button>
        <el-button type="success" @click="handleQuickAction('merchants')">
          <el-icon><Shop /></el-icon>
          商家管理
        </el-button>
        <el-button type="warning" @click="handleQuickAction('orders')">
          <el-icon><Document /></el-icon>
          订单管理
        </el-button>
      </div>
    </div>

    <!-- 数据概览卡片 -->
    <div class="stats-grid">
      <el-card class="stats-card" v-for="stat in statsData" :key="stat.key">
        <div class="stats-content">
          <div class="stats-icon" :style="{ backgroundColor: stat.color }">
            <component :is="stat.icon" />
          </div>
          <div class="stats-info">
            <div class="stats-value">{{ stat.value }}</div>
            <div class="stats-label">{{ stat.label }}</div>
            <div class="stats-trend" :class="stat.trend > 0 ? 'positive' : 'negative'">
              <el-icon>
                <ArrowUp v-if="stat.trend > 0" />
                <ArrowDown v-else />
              </el-icon>
              {{ Math.abs(stat.trend) }}%
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <el-row :gutter="24">
        <!-- 销售趋势图 -->
        <el-col :span="16">
          <el-card class="chart-card">
            <template #header>
              <div class="card-header">
                <span>销售趋势</span>
                <el-radio-group v-model="salesPeriod" size="small">
                  <el-radio-button label="7d">近7天</el-radio-button>
                  <el-radio-button label="30d">近30天</el-radio-button>
                  <el-radio-button label="90d">近90天</el-radio-button>
                </el-radio-group>
              </div>
            </template>
            <div class="chart-container" ref="salesChartRef"></div>
          </el-card>
        </el-col>
        
        <!-- 用户分布图 -->
        <el-col :span="8">
          <el-card class="chart-card">
            <template #header>
              <span>用户分布</span>
            </template>
            <div class="chart-container" ref="userChartRef"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 数据表格区域 -->
    <div class="tables-section">
      <el-row :gutter="24">
        <!-- 最新订单 -->
        <el-col :span="12">
          <el-card class="table-card">
            <template #header>
              <div class="card-header">
                <span>最新订单</span>
                <el-link type="primary" @click="handleViewMore('orders')">查看更多</el-link>
              </div>
            </template>
            <el-table :data="recentOrders" style="width: 100%" size="small">
              <el-table-column prop="orderNo" label="订单号" width="120" />
              <el-table-column prop="userName" label="用户" width="80" />
              <el-table-column prop="amount" label="金额" width="80">
                <template #default="{ row }">
                  <span class="amount">¥{{ row.amount }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="80">
                <template #default="{ row }">
                  <el-tag :type="getOrderStatusType(row.status)" size="small">
                    {{ getOrderStatusText(row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="时间" />
            </el-table>
          </el-card>
        </el-col>
        
        <!-- 待处理事项 -->
        <el-col :span="12">
          <el-card class="table-card">
            <template #header>
              <div class="card-header">
                <span>待处理事项</span>
                <el-badge :value="pendingCount" class="badge">
                  <el-icon><Bell /></el-icon>
                </el-badge>
              </div>
            </template>
            <div class="pending-list">
              <div 
                v-for="item in pendingItems" 
                :key="item.id"
                class="pending-item"
                @click="handlePendingItem(item)"
              >
                <div class="pending-icon" :style="{ backgroundColor: item.color }">
                  <component :is="item.icon" />
                </div>
                <div class="pending-content">
                  <div class="pending-title">{{ item.title }}</div>
                  <div class="pending-desc">{{ item.description }}</div>
                </div>
                <div class="pending-count">{{ item.count }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 系统状态 -->
    <div class="system-section">
      <el-card class="system-card">
        <template #header>
          <span>系统状态</span>
        </template>
        <el-row :gutter="24">
          <el-col :span="6" v-for="system in systemStatus" :key="system.name">
            <div class="system-item">
              <div class="system-status" :class="system.status">
                <div class="status-dot"></div>
                <span>{{ system.statusText }}</span>
              </div>
              <div class="system-name">{{ system.name }}</div>
              <div class="system-info">{{ system.info }}</div>
            </div>
          </el-col>
        </el-row>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import {
  User,
  Shop,
  Document,
  TrendCharts,
  Money,
  ArrowUp,
  ArrowDown,
  Bell,
  PictureFilled
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getDashboardStats, getSalesTrend, getUserDistribution, getRecentOrders, getPendingItems } from '@/api/admin'
import { getPendingBannerCount } from '@/api/admin/banner'

const router = useRouter()
const userStore = useUserStore()

// 图表引用
const salesChartRef = ref()
const userChartRef = ref()

// 销售趋势周期
const salesPeriod = ref('30d')

// 监听周期变化，重新加载数据
watch(salesPeriod, () => {
  loadSalesTrend()
})

// 当前日期
const currentDate = computed(() => {
  return new Date().toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  })
})

// 统计数据 - 虚拟数据已移除，待接入真实API
const statsData = reactive([
  {
    key: 'users',
    label: '总用户数',
    value: '0',
    trend: 0,
    color: '#1890ff',
    icon: User
  },
  {
    key: 'merchants',
    label: '商家数量',
    value: '0',
    trend: 0,
    color: '#52c41a',
    icon: Shop
  },
  {
    key: 'orders',
    label: '总订单数',
    value: '0',
    trend: 0,
    color: '#faad14',
    icon: Document
  },
  {
    key: 'transactionAmount',
    label: '交易额',
    value: '¥0',
    trend: 0,
    color: '#f5222d',
    icon: Money
  }
])

// 最新订单数据 - 虚拟数据已移除，待接入真实API
const recentOrders = reactive([])

// 待处理事项 - 虚拟数据已移除，待接入真实API
const pendingItems = reactive([])

// 待处理事项总数
const pendingCount = computed(() => {
  return pendingItems.reduce((sum, item) => sum + item.count, 0)
})

// 系统状态
const systemStatus = reactive([
  {
    name: '数据库',
    status: 'normal',
    statusText: '正常',
    info: 'MySQL 8.0'
  },
  {
    name: '缓存服务',
    status: 'normal',
    statusText: '正常',
    info: 'Redis 6.2'
  },
  {
    name: '消息队列',
    status: 'warning',
    statusText: '警告',
    info: 'RocketMQ'
  },
  {
    name: '文件存储',
    status: 'normal',
    statusText: '正常',
    info: 'MinIO'
  }
])

// 处理快速操作
const handleQuickAction = (action) => {
  switch (action) {
    case 'users':
      router.push('/admin/users')
      break
    case 'merchants':
      router.push('/admin/merchants')
      break
    case 'orders':
      router.push('/admin/orders')
      break
  }
}

// 处理查看更多
const handleViewMore = (type) => {
  router.push(`/admin/${type}`)
}

// 处理待处理事项点击
const handlePendingItem = (item) => {
  switch (item.action) {
    case 'merchant-audit':
      router.push('/admin/merchants/audit')
      break
    case 'product-audit':
      router.push('/admin/products/audit')
      break
    case 'refund-process':
      router.push('/admin/orders/refunds')
      break
    case 'banner-review':
      router.push('/admin/banner/review')
      break
    case 'user-report':
      ElMessage.info('用户举报功能开发中...')
      break
  }
}

// 获取订单状态类型
const getOrderStatusType = (status) => {
  const statusMap = {
    pending: 'warning',
    paid: 'success',
    shipped: 'primary',
    completed: 'success',
    cancelled: 'danger'
  }
  return statusMap[status] || 'info'
}

// 获取订单状态文本
const getOrderStatusText = (status) => {
  const statusMap = {
    pending: '待付款',
    paid: '已付款',
    shipped: '已发货',
    completed: '已完成',
    cancelled: '已取消'
  }
  return statusMap[status] || '未知'
}

// 初始化销售趋势图
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
      data: ['销售额', '订单数']
    },
    xAxis: {
      type: 'category',
      data: []
    },
    yAxis: [
      {
        type: 'value',
        name: '销售额(万元)',
        position: 'left'
      },
      {
        type: 'value',
        name: '订单数',
        position: 'right'
      }
    ],
    series: [
      {
        name: '销售额',
        type: 'line',
        smooth: true,
        data: [],
        itemStyle: {
          color: '#1890ff'
        }
      },
      {
        name: '订单数',
        type: 'bar',
        yAxisIndex: 1,
        data: [],
        itemStyle: {
          color: '#52c41a'
        }
      }
    ]
  }
  
  chart.setOption(option)
  
  // 响应式处理
  window.addEventListener('resize', () => {
    chart.resize()
  })
  
  return chart
}

// 初始化用户分布图
const initUserChart = () => {
  const chart = echarts.init(userChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 10,
      data: []
    },
    series: [
      {
        name: '用户分布',
        type: 'pie',
        radius: ['50%', '70%'],
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
        data: []
      }
    ]
  }
  
  chart.setOption(option)
  
  // 响应式处理
  window.addEventListener('resize', () => {
    chart.resize()
  })
  
  return chart
}

// 图表实例
let salesChart = null
let userChart = null

// 加载Dashboard统计数据
const loadDashboardStats = async () => {
  try {
    const response = await getDashboardStats()
    if (response.data) {
      statsData[0].value = response.data.totalUsers || 0
      statsData[0].trend = response.data.usersTrend || 0
      statsData[1].value = response.data.totalMerchants || 0
      statsData[1].trend = response.data.merchantsTrend || 0
      statsData[2].value = response.data.totalOrders || 0
      statsData[2].trend = response.data.ordersTrend || 0
      statsData[3].value = `¥${response.data.totalTransactionAmount || 0}`
      statsData[3].trend = response.data.transactionTrend || 0
    }
  } catch (error) {
    console.error('Load dashboard stats error:', error)
  }
}

// 加载销售趋势数据
const loadSalesTrend = async () => {
  try {
    const response = await getSalesTrend({ period: salesPeriod.value })
    if (response.data && salesChart) {
      salesChart.setOption({
        xAxis: {
          data: response.data.dates || []
        },
        series: [
          {
            data: response.data.sales || []
          },
          {
            data: response.data.orders || []
          }
        ]
      })
    }
  } catch (error) {
    console.error('Load sales trend error:', error)
  }
}

// 加载用户分布数据
const loadUserDistribution = async () => {
  try {
    const response = await getUserDistribution()
    if (response.data && userChart) {
      const data = response.data.distribution || []
      userChart.setOption({
        legend: {
          data: data.map(item => item.name)
        },
        series: [
          {
            data: data.map(item => ({
              value: item.value,
              name: item.name,
              itemStyle: { color: item.color || '#1890ff' }
            }))
          }
        ]
      })
    }
  } catch (error) {
    console.error('Load user distribution error:', error)
  }
}

// 加载最近订单数据
const loadRecentOrders = async () => {
  try {
    const response = await getRecentOrders({ limit: 10 })
    if (response.data) {
      recentOrders.length = 0
      response.data.forEach(order => {
        recentOrders.push(order)
      })
    }
  } catch (error) {
    console.error('Load recent orders error:', error)
  }
}

// 加载待处理事项数据
const loadPendingItems = async () => {
  try {
    const response = await getPendingItems()
    if (response.data) {
      pendingItems.length = 0
      
      // 待审核商家
      if (response.data.pendingMerchants > 0) {
        pendingItems.push({
          id: 1,
          title: '待审核商家',
          description: '有新的商家入驻申请需要审核',
          count: response.data.pendingMerchants,
          color: '#1890ff',
          icon: Shop,
          action: 'merchant-audit'
        })
      }
      
      // 待审核商品
      if (response.data.pendingProducts > 0) {
        pendingItems.push({
          id: 2,
          title: '待审核商品',
          description: '有商品需要审核上架',
          count: response.data.pendingProducts,
          color: '#52c41a',
          icon: Document,
          action: 'product-audit'
        })
      }
      
      // 待处理退款（需要客服介入的退款纠纷）
      if (response.data.pendingRefunds > 0) {
        pendingItems.push({
          id: 3,
          title: '待处理退款',
          description: '有退款申请需要客服介入处理',
          count: response.data.pendingRefunds,
          color: '#faad14',
          icon: Money,
          action: 'refund-process'
        })
      }
      
      // 用户举报/投诉
      if (response.data.userReports > 0) {
        pendingItems.push({
          id: 4,
          title: '用户投诉',
          description: '有用户投诉需要处理',
          count: response.data.userReports,
          color: '#f5222d',
          icon: Bell,
          action: 'user-report'
        })
      }
    }
    
    // 加载待审核轮播图数量
    await loadPendingBanners()
  } catch (error) {
    console.error('Load pending items error:', error)
  }
}

// 加载待审核轮播图数量
const loadPendingBanners = async () => {
  try {
    const response = await getPendingBannerCount()
    if (response.success && response.data > 0) {
      pendingItems.push({
        id: 5,
        title: '待审核轮播图',
        description: '有商家轮播图申请需要审核',
        count: response.data,
        color: '#722ed1',
        icon: PictureFilled,
        action: 'banner-review'
      })
    }
  } catch (error) {
    console.error('Load pending banners error:', error)
  }
}

// 组件挂载后初始化
onMounted(async () => {
  await nextTick()
  // 初始化图表
  salesChart = initSalesChart()
  userChart = initUserChart()
  
  // 加载数据
  loadDashboardStats()
  loadSalesTrend()
  loadUserDistribution()
  loadRecentOrders()
  loadPendingItems()
})
</script>

<style scoped>
.admin-dashboard {
  padding: 0;
}

/* 欢迎区域 */
.welcome-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 32px;
  border-radius: 8px;
  margin-bottom: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.welcome-title {
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 8px 0;
}

.welcome-subtitle {
  font-size: 16px;
  opacity: 0.9;
  margin: 0;
}

.welcome-actions {
  display: flex;
  gap: 12px;
}

/* 统计卡片网格 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 24px;
  margin-bottom: 24px;
}

.stats-card {
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s;
}

.stats-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.stats-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stats-icon {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
}

.stats-info {
  flex: 1;
}

.stats-value {
  font-size: 32px;
  font-weight: 600;
  color: #333;
  line-height: 1;
  margin-bottom: 4px;
}

.stats-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.stats-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  font-weight: 500;
}

.stats-trend.positive {
  color: #52c41a;
}

.stats-trend.negative {
  color: #f5222d;
}

/* 图表区域 */
.charts-section {
  margin-bottom: 24px;
}

.chart-card {
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.chart-container {
  height: 300px;
}

/* 表格区域 */
.tables-section {
  margin-bottom: 24px;
}

.table-card {
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.amount {
  font-weight: 600;
  color: #f5222d;
}

/* 待处理事项 */
.pending-list {
  max-height: 300px;
  overflow-y: auto;
}

.pending-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.pending-item:hover {
  background-color: #f5f5f5;
}

.pending-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 16px;
}

.pending-content {
  flex: 1;
}

.pending-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.pending-desc {
  font-size: 12px;
  color: #666;
}

.pending-count {
  background-color: #f5222d;
  color: white;
  font-size: 12px;
  font-weight: 500;
  padding: 2px 8px;
  border-radius: 12px;
  min-width: 20px;
  text-align: center;
}

.badge {
  cursor: pointer;
}

/* 系统状态 */
.system-section {
  margin-bottom: 24px;
}

.system-card {
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.system-item {
  text-align: center;
  padding: 16px;
}

.system-status {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 500;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.system-status.normal {
  color: #52c41a;
}

.system-status.normal .status-dot {
  background-color: #52c41a;
}

.system-status.warning {
  color: #faad14;
}

.system-status.warning .status-dot {
  background-color: #faad14;
}

.system-status.error {
  color: #f5222d;
}

.system-status.error .status-dot {
  background-color: #f5222d;
}

.system-name {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.system-info {
  font-size: 12px;
  color: #666;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .welcome-section {
    flex-direction: column;
    text-align: center;
    gap: 16px;
  }
  
  .welcome-actions {
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .chart-container {
    height: 250px;
  }
}
</style>