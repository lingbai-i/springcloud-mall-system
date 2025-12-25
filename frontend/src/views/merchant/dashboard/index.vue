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
import { ref, reactive, onMounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getLowStockProducts } from '@/api/merchant/product'
import { getKeyMetrics } from '@/api/merchant/dashboard'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import {
  Plus, List, Setting, User, ShoppingBag, Money, TrendCharts, Star,
  ArrowUp, ArrowDown, Clock, Warning, Document, Van
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

// 加载状态
const loading = reactive({
  overview: false,
  salesTrend: false,
  hotProducts: false,
  recentOrders: false,
  pendingItems: false,
  analytics: false
})

// 响应式数据
const currentDate = ref('')
const salesPeriod = ref('7days')
const salesChartRef = ref()
const productChartRef = ref()
let salesChart = null
let productChart = null

// 数据概览
const overviewData = reactive([
  {
    key: 'todaySales',
    label: '今日销售额',
    value: '¥0',
    icon: 'Money',
    color: '#52c41a',
    trend: 0
  },
  {
    key: 'todayOrders',
    label: '今日订单',
    value: '0',
    icon: 'ShoppingBag',
    color: '#1890ff',
    trend: 0
  },
  {
    key: 'totalProducts',
    label: '商品总数',
    value: '0',
    icon: 'List',
    color: '#722ed1',
    trend: 0
  },
  {
    key: 'shopRating',
    label: '店铺评分',
    value: '0',
    icon: 'Star',
    color: '#fa8c16',
    trend: 0
  }
])

// 最新订单
const recentOrders = reactive([])

// 待处理事项
const pendingItems = reactive([
  {
    type: 'pending_orders',
    title: '待发货订单',
    count: 0,
    icon: 'Van',
    color: '#fa541c'
  },
  {
    type: 'low_stock',
    title: '库存不足',
    count: 0,
    icon: 'Warning',
    color: '#faad14'
  },
  {
    type: 'pending_reviews',
    title: '待回复评价',
    count: 0,
    icon: 'Document',
    color: '#13c2c2'
  },
  {
    type: 'expired_products',
    title: '即将下架',
    count: 0,
    icon: 'Clock',
    color: '#eb2f96'
  }
])

// 店铺数据分析
const analysisData = reactive([
  {
    key: 'conversion_rate',
    label: '转化率',
    value: '0%',
    desc: '暂无数据'
  },
  {
    key: 'avg_order_value',
    label: '客单价',
    value: '¥0',
    desc: '暂无数据'
  },
  {
    key: 'return_rate',
    label: '退货率',
    value: '0%',
    desc: '暂无数据'
  },
  {
    key: 'customer_satisfaction',
    label: '客户满意度',
    value: '0%',
    desc: '暂无数据'
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
  if (!salesChartRef.value) return
  
  salesChart = echarts.init(salesChartRef.value)
  
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
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: []
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
        data: [],
        itemStyle: {
          color: '#52c41a'
        }
      },
      {
        name: '订单量',
        type: 'bar',
        yAxisIndex: 1,
        data: [],
        itemStyle: {
          color: '#1890ff'
        }
      }
    ]
  }
  
  salesChart.setOption(option)
  
  // 响应式调整
  window.addEventListener('resize', () => {
    salesChart?.resize()
  })
}

const initProductChart = () => {
  if (!productChartRef.value) return
  
  productChart = echarts.init(productChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      top: 'center'
    },
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['60%', '50%'],
        avoidLabelOverlap: false,
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: '16',
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
  
  productChart.setOption(option)
  
  // 响应式调整
  window.addEventListener('resize', () => {
    productChart?.resize()
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
  router.push(`/merchant/orders/${orderId}`)
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
    router.push(routeMap[type])
  }
}

/**
 * 计算趋势百分比
 * @param {Number} today 今日值
 * @param {Number} yesterday 昨日值
 * @returns {Number} 趋势百分比
 */
const calculateTrend = (today, yesterday) => {
  if (!yesterday || yesterday === 0) {
    return today > 0 ? 100 : 0
  }
  return Math.round(((today - yesterday) / yesterday) * 100)
}

/**
 * 加载数据概览 - 直接从 order-service 获取数据
 */
const loadOverviewData = async () => {
  loading.overview = true
  const merchantId = userStore.merchantId || localStorage.getItem('merchantId')
  
  if (!merchantId) {
    console.warn('商家ID不存在，无法加载数据')
    loading.overview = false
    return
  }
  
  try {
    // 获取所有订单来计算统计数据
    const params = new URLSearchParams({
      merchantId: merchantId,
      page: 0,
      size: 1000
    })
    
    const response = await fetch(`/api/order-service/orders/merchant?${params.toString()}`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    
    const result = await response.json()
    
    let todaySales = 0, todayOrders = 0, totalOrders = 0, totalSales = 0
    let yesterdaySales = 0, yesterdayOrders = 0
    
    if (result.success && result.data) {
      const orders = result.data.content || []
      totalOrders = result.data.totalElements || orders.length
      
      // 获取今天和昨天的日期
      const today = new Date()
      today.setHours(0, 0, 0, 0)
      const yesterday = new Date(today)
      yesterday.setDate(yesterday.getDate() - 1)
      
      orders.forEach(order => {
        const orderDate = new Date(order.createTime)
        orderDate.setHours(0, 0, 0, 0)
        const amount = parseFloat(order.totalAmount) || 0
        
        totalSales += amount
        
        if (orderDate.getTime() === today.getTime()) {
          todaySales += amount
          todayOrders++
        } else if (orderDate.getTime() === yesterday.getTime()) {
          yesterdaySales += amount
          yesterdayOrders++
        }
      })
      
      console.log('仪表盘-订单统计:', { todaySales, todayOrders, yesterdaySales, yesterdayOrders, totalOrders })
    }
    
    // 获取商品总数 - 直接调用 merchant-service API
    let totalProducts = 0
    try {
      const productParams = new URLSearchParams({
        merchantId: merchantId,
        page: 1,
        size: 1
      })
      
      const productResponse = await fetch(`/api/merchant/products/list?${productParams.toString()}`, {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      })
      
      const productResult = await productResponse.json()
      console.log('仪表盘-商品列表响应:', productResult)
      
      if (productResult.success && productResult.data) {
        // PageResult 结构: { records, total, current, size, pages }
        totalProducts = productResult.data.total || productResult.data.records?.length || 0
      }
    } catch (e) {
      console.warn('获取商品总数失败:', e)
    }
    
    console.log('仪表盘-商品总数:', totalProducts)
    
    // 更新数据概览
    overviewData[0].value = `¥${todaySales.toFixed(2)}`
    overviewData[0].trend = calculateTrend(todaySales, yesterdaySales)
    
    overviewData[1].value = String(todayOrders)
    overviewData[1].trend = calculateTrend(todayOrders, yesterdayOrders)
    
    overviewData[2].value = String(totalProducts)
    overviewData[2].trend = 0
    
    // 店铺评分暂时显示为暂无
    overviewData[3].value = '暂无'
    overviewData[3].trend = 0
    
    console.log('数据概览加载成功')
  } catch (error) {
    console.error('加载数据概览失败:', error)
    overviewData[0].value = '¥0'
    overviewData[1].value = '0'
    overviewData[2].value = '0'
    overviewData[3].value = '暂无'
  } finally {
    loading.overview = false
  }
}

// 生命周期
onMounted(() => {
  initCurrentDate()
  
  nextTick(() => {
    initSalesChart()
    initProductChart()
    // 加载所有仪表盘数据
    loadDashboardData()
  })
})

// 监听销售周期变化
watch(salesPeriod, () => {
  loadSalesTrend()
})

/**
 * 加载所有仪表盘数据
 */
const loadDashboardData = async () => {
  // 并行加载所有数据
  await Promise.allSettled([
    loadOverviewData(),
    loadSalesTrend(),
    loadHotProducts(),
    loadRecentOrders(),
    loadPendingItems(),
    loadAnalyticsData()
  ])
}

/**
 * 获取销售周期对应的天数
 */
const getPeriodDays = () => {
  const periodMap = {
    '7days': 7,
    '30days': 30,
    '90days': 90
  }
  return periodMap[salesPeriod.value] || 7
}

/**
 * 加载销售趋势数据 - 从 order-service 获取订单并计算
 */
const loadSalesTrend = async () => {
  loading.salesTrend = true
  const merchantId = userStore.merchantId || localStorage.getItem('merchantId')
  
  if (!merchantId) {
    loading.salesTrend = false
    return
  }
  
  try {
    const days = getPeriodDays()
    
    // 获取所有订单
    const params = new URLSearchParams({
      merchantId: merchantId,
      page: 0,
      size: 1000
    })
    
    const response = await fetch(`/api/order-service/orders/merchant?${params.toString()}`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    
    const result = await response.json()
    
    if (result.success && result.data) {
      const orders = result.data.content || []
      
      // 计算日期范围
      const endDate = new Date()
      const startDate = new Date()
      startDate.setDate(startDate.getDate() - days + 1)
      
      // 初始化每日统计
      const dailyStats = {}
      for (let d = new Date(startDate); d <= endDate; d.setDate(d.getDate() + 1)) {
        const dateKey = d.toISOString().split('T')[0]
        dailyStats[dateKey] = { sales: 0, orders: 0 }
      }
      
      // 统计每日数据
      orders.forEach(order => {
        const orderDate = new Date(order.createTime)
        const dateKey = orderDate.toISOString().split('T')[0]
        
        if (dailyStats[dateKey]) {
          dailyStats[dateKey].sales += parseFloat(order.totalAmount) || 0
          dailyStats[dateKey].orders++
        }
      })
      
      // 转换为图表数据
      const dates = Object.keys(dailyStats).sort()
      const salesAmounts = dates.map(d => dailyStats[d].sales)
      const orderCounts = dates.map(d => dailyStats[d].orders)
      const displayDates = dates.map(d => d.substring(5)) // 只显示月-日
      
      updateSalesChart(displayDates, salesAmounts, orderCounts)
      console.log('仪表盘-销售趋势加载成功:', { days, dataPoints: dates.length })
    } else {
      updateSalesChart([], [], [])
    }
  } catch (error) {
    console.error('加载销售趋势失败:', error)
    updateSalesChart([], [], [])
  } finally {
    loading.salesTrend = false
  }
}

/**
 * 更新销售趋势图表
 */
const updateSalesChart = (dates, salesAmounts, orderCounts) => {
  if (!salesChart) return
  
  salesChart.setOption({
    xAxis: {
      data: dates
    },
    series: [
      { name: '销售额', data: salesAmounts },
      { name: '订单量', data: orderCounts }
    ]
  })
}

/**
 * 加载热销商品数据 - 从 order-service 获取订单并统计商品销量
 */
const loadHotProducts = async () => {
  loading.hotProducts = true
  const merchantId = userStore.merchantId || localStorage.getItem('merchantId')
  
  if (!merchantId) {
    loading.hotProducts = false
    return
  }
  
  try {
    // 获取所有订单
    const params = new URLSearchParams({
      merchantId: merchantId,
      page: 0,
      size: 1000
    })
    
    const response = await fetch(`/api/order-service/orders/merchant?${params.toString()}`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    
    const result = await response.json()
    
    if (result.success && result.data) {
      const orders = result.data.content || []
      
      // 统计商品销量
      const productSales = {}
      orders.forEach(order => {
        const items = order.orderItems || []
        items.forEach(item => {
          const productName = item.productName || '未知商品'
          const quantity = item.quantity || 1
          
          if (!productSales[productName]) {
            productSales[productName] = 0
          }
          productSales[productName] += quantity
        })
      })
      
      // 转换为饼图数据，取前10名
      const sortedProducts = Object.entries(productSales)
        .sort((a, b) => b[1] - a[1])
        .slice(0, 10)
        .map(([name, value]) => ({ name, value }))
      
      if (sortedProducts.length > 0) {
        updateProductChart(sortedProducts)
        console.log('仪表盘-热销商品加载成功:', sortedProducts.length, '个商品')
      } else {
        updateProductChart([{ name: '暂无销售数据', value: 1 }])
      }
    } else {
      updateProductChart([{ name: '暂无销售数据', value: 1 }])
    }
  } catch (error) {
    console.error('加载热销商品失败:', error)
    updateProductChart([{ name: '暂无销售数据', value: 1 }])
  } finally {
    loading.hotProducts = false
  }
}

/**
 * 更新商品销量排行图表
 */
const updateProductChart = (data) => {
  if (!productChart) return
  
  productChart.setOption({
    series: [{
      data: data
    }]
  })
}

/**
 * 加载最近订单 - 直接调用 order-service
 */
const loadRecentOrders = async () => {
  loading.recentOrders = true
  const merchantId = userStore.merchantId || localStorage.getItem('merchantId')
  
  if (!merchantId) {
    loading.recentOrders = false
    return
  }
  
  try {
    // 直接调用 order-service API，与 merchant/orders 页面保持一致
    const params = new URLSearchParams({
      merchantId: merchantId,
      page: 0,
      size: 10
    })
    
    const response = await fetch(`/api/order-service/orders/merchant?${params.toString()}`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    
    const result = await response.json()
    console.log('仪表盘-最近订单响应:', result)
    
    if (result.success && result.data) {
      const rawOrders = result.data.content || []
      
      // 清空并重新填充数组
      recentOrders.length = 0
      rawOrders.slice(0, 10).forEach(order => {
        recentOrders.push({
          id: order.id,
          orderNo: order.orderNo,
          customerName: order.receiverName || '匿名用户',
          totalAmount: order.totalAmount || 0,
          status: mapStatusToFrontend(order.status),
          createTime: order.createTime
        })
      })
      
      console.log('仪表盘-最近订单加载成功:', recentOrders.length, '条')
    }
  } catch (error) {
    console.error('加载最近订单失败:', error)
  } finally {
    loading.recentOrders = false
  }
}

/**
 * 映射后端状态到前端状态
 */
const mapStatusToFrontend = (status) => {
  const statusMap = {
    'PENDING': 'pending_payment',
    'PAID': 'pending_shipment',
    'SHIPPED': 'shipped',
    'COMPLETED': 'completed',
    'CANCELLED': 'cancelled',
    'REFUNDING': 'refunding',
    'REFUNDED': 'refunded'
  }
  return statusMap[status] || status?.toLowerCase() || 'pending_payment'
}

/**
 * 加载待处理事项 - 直接调用 order-service
 */
const loadPendingItems = async () => {
  loading.pendingItems = true
  const merchantId = userStore.merchantId || localStorage.getItem('merchantId')
  
  if (!merchantId) {
    loading.pendingItems = false
    return
  }
  
  try {
    // 获取所有订单来统计各状态数量
    const params = new URLSearchParams({
      merchantId: merchantId,
      page: 0,
      size: 1000 // 获取足够多的订单来统计
    })
    
    const response = await fetch(`/api/order-service/orders/merchant?${params.toString()}`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    
    const result = await response.json()
    
    if (result.success && result.data) {
      const orders = result.data.content || []
      
      // 统计各状态订单数量
      const statusCounts = {}
      orders.forEach(order => {
        const status = order.status
        statusCounts[status] = (statusCounts[status] || 0) + 1
      })
      
      // 待发货订单数 (状态: PAID)
      pendingItems[0].count = statusCounts['PAID'] || 0
      
      console.log('仪表盘-订单状态统计:', statusCounts)
    }
    
    // 获取库存不足商品数
    try {
      const lowStockRes = await getLowStockProducts({ merchantId, threshold: 10, page: 1, size: 1 })
      if (lowStockRes?.data) {
        pendingItems[1].count = lowStockRes.data.total || 0
      }
    } catch (e) {
      console.warn('获取库存不足商品失败:', e)
    }
    
    // 待回复评价和即将下架暂时设为0
    pendingItems[2].count = 0
    pendingItems[3].count = 0
    
  } catch (error) {
    console.error('加载待处理事项失败:', error)
  } finally {
    loading.pendingItems = false
  }
}

/**
 * 加载店铺数据分析
 */
const loadAnalyticsData = async () => {
  loading.analytics = true
  const merchantId = userStore.merchantId
  
  if (!merchantId) {
    loading.analytics = false
    return
  }
  
  try {
    const response = await getKeyMetrics(merchantId)
    
    if (response?.data) {
      const metrics = response.data
      
      // 转化率
      const conversionRate = metrics.conversionRate || metrics.conversion_rate || 0
      analysisData[0].value = `${(conversionRate * 100).toFixed(1)}%`
      analysisData[0].desc = conversionRate > 0 ? '较上月' : '暂无数据'
      
      // 客单价
      const avgOrderValue = metrics.avgOrderValue || metrics.avg_order_value || 0
      analysisData[1].value = `¥${avgOrderValue.toFixed(2)}`
      analysisData[1].desc = avgOrderValue > 0 ? '较上月' : '暂无数据'
      
      // 退货率
      const returnRate = metrics.returnRate || metrics.return_rate || 0
      analysisData[2].value = `${(returnRate * 100).toFixed(1)}%`
      analysisData[2].desc = returnRate > 0 ? '较上月' : '暂无数据'
      
      // 客户满意度
      const satisfaction = metrics.customerSatisfaction || metrics.customer_satisfaction || 0
      analysisData[3].value = `${(satisfaction * 100).toFixed(1)}%`
      analysisData[3].desc = satisfaction > 0 ? '较上月' : '暂无数据'
    }
  } catch (error) {
    console.error('加载店铺数据分析失败:', error)
    // 设置默认值
    analysisData.forEach(item => {
      item.value = item.key.includes('rate') || item.key.includes('satisfaction') ? '0%' : '¥0'
      item.desc = '暂无数据'
    })
  } finally {
    loading.analytics = false
  }
}
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