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
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
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
  Bell
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const router = useRouter()
const userStore = useUserStore()

// 图表引用
const salesChartRef = ref()
const userChartRef = ref()

// 销售趋势周期
const salesPeriod = ref('30d')

// 当前日期
const currentDate = computed(() => {
  return new Date().toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  })
})

// 统计数据
const statsData = reactive([
  {
    key: 'users',
    label: '总用户数',
    value: '12,345',
    trend: 12.5,
    color: '#1890ff',
    icon: User
  },
  {
    key: 'merchants',
    label: '商家数量',
    value: '1,234',
    trend: 8.2,
    color: '#52c41a',
    icon: Shop
  },
  {
    key: 'orders',
    label: '总订单数',
    value: '45,678',
    trend: 15.3,
    color: '#faad14',
    icon: Document
  },
  {
    key: 'revenue',
    label: '总收入',
    value: '¥2,345,678',
    trend: 23.1,
    color: '#f5222d',
    icon: Money
  }
])

// 最新订单数据
const recentOrders = reactive([
  {
    orderNo: 'ORD001',
    userName: '张三',
    amount: '299.00',
    status: 'paid',
    createTime: '2024-01-15 10:30'
  },
  {
    orderNo: 'ORD002',
    userName: '李四',
    amount: '599.00',
    status: 'pending',
    createTime: '2024-01-15 09:45'
  },
  {
    orderNo: 'ORD003',
    userName: '王五',
    amount: '199.00',
    status: 'shipped',
    createTime: '2024-01-15 08:20'
  },
  {
    orderNo: 'ORD004',
    userName: '赵六',
    amount: '899.00',
    status: 'completed',
    createTime: '2024-01-14 16:15'
  }
])

// 待处理事项
const pendingItems = reactive([
  {
    id: 1,
    title: '商家审核',
    description: '有新的商家申请待审核',
    count: 5,
    color: '#1890ff',
    icon: Shop,
    action: 'merchant-audit'
  },
  {
    id: 2,
    title: '商品审核',
    description: '有商品待审核上架',
    count: 12,
    color: '#52c41a',
    icon: Document,
    action: 'product-audit'
  },
  {
    id: 3,
    title: '退款处理',
    description: '有退款申请待处理',
    count: 3,
    color: '#faad14',
    icon: Money,
    action: 'refund-process'
  },
  {
    id: 4,
    title: '用户举报',
    description: '有用户举报待处理',
    count: 2,
    color: '#f5222d',
    icon: User,
    action: 'user-report'
  }
])

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
      data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
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
        data: [120, 132, 101, 134, 90, 230, 210, 182, 191, 234, 290, 330],
        itemStyle: {
          color: '#1890ff'
        }
      },
      {
        name: '订单数',
        type: 'bar',
        yAxisIndex: 1,
        data: [2200, 1800, 1900, 2100, 1500, 2800, 2600, 2400, 2300, 2900, 3200, 3500],
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
      data: ['新用户', '活跃用户', '沉睡用户', '流失用户']
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
        data: [
          { value: 335, name: '新用户', itemStyle: { color: '#1890ff' } },
          { value: 310, name: '活跃用户', itemStyle: { color: '#52c41a' } },
          { value: 234, name: '沉睡用户', itemStyle: { color: '#faad14' } },
          { value: 135, name: '流失用户', itemStyle: { color: '#f5222d' } }
        ]
      }
    ]
  }
  
  chart.setOption(option)
  
  // 响应式处理
  window.addEventListener('resize', () => {
    chart.resize()
  })
}

// 组件挂载后初始化
onMounted(async () => {
  await nextTick()
  initSalesChart()
  initUserChart()
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