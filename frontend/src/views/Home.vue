<!--
 * @Author: lingbai
 * @Date: 2025-10-21 23:25:21
 * @LastEditTime: 2025-10-22 02:25:21
 * @Description: 商城首页 - 参考京东商城风格设计，使用本地矢量图标
 * @FilePath: /frontend/src/views/Home.vue
-->
<template>
  <div class="home-container">
    <!-- 顶部导航栏 -->
    <div class="top-bar">
      <div class="container">
        <div class="top-links">
          <span>您好，欢迎来到在线商城！</span>
          <div class="user-links">
            <a href="#" @click="handleLogin" v-if="!isLoggedIn">请登录</a>
            <!-- 用户欢迎下拉菜单 -->
            <div
              v-else
              class="welcome-dropdown"
              @mouseenter="logoutVisible = true"
              @mouseleave="logoutVisible = false"
            >
              <a href="#" class="welcome-trigger">
                欢迎,{{ displayUserName }}
                <el-icon class="arrow-icon"><ArrowDown /></el-icon>
              </a>
              <transition name="fade-slide">
                <div v-if="logoutVisible" class="welcome-menu">
                  <a @click="handleLogout" class="welcome-menu-item">
                    退出登录
                  </a>
                </div>
              </transition>
            </div>
            <a href="#" @click="goToOrders">我的订单</a>
            <a href="#" @click="goToProfile">会员中心</a>
            <!-- 商家服务下拉菜单 -->
            <div
              class="merchant-service-dropdown"
              @mouseenter="showMerchantMenu = true"
              @mouseleave="showMerchantMenu = false"
            >
              <a href="#" class="merchant-service-trigger">
                商家服务
                <el-icon class="arrow-icon"><ArrowDown /></el-icon>
              </a>
              <transition name="fade-slide">
                <div v-if="showMerchantMenu" class="merchant-menu">
                  <a @click="goToMerchantLogin" class="merchant-menu-item">
                    商家后台
                  </a>
                  <a @click="goToMerchantRegister" class="merchant-menu-item">
                    商家入驻
                  </a>
                </div>
              </transition>
            </div>
            <!-- 购物车 -->
            <div class="cart-link" @click="goToCart">
              <LocalIcon name="gouwuche" :size="16" />
              <span>购物车</span>
              <el-badge :value="cartCount" class="cart-badge" v-if="cartCount > 0 && isLoggedIn" />
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 主导航栏 -->
    <div class="main-header">
      <div class="container">
        <div class="header-content">
          <!-- Logo -->
          <div class="logo">
            <h1>在线商城</h1>
          </div>

          <!-- 搜索框 -->
          <div class="search-area">
            <div class="search-box">
              <el-input
                v-model="searchKeyword"
                placeholder="搜索商品、品牌、店铺..."
                class="search-input"
                @keyup.enter="handleSearch"
              >
                <template #append>
                  <el-button type="danger" @click="handleSearch">
                    <LocalIcon name="sousuo" :size="16" />
                    搜索
                  </el-button>
                </template>
              </el-input>
            </div>
            <div class="hot-keywords">
              <span>热门搜索：</span>
              <a v-for="keyword in hotKeywords" :key="keyword" @click="searchKeyword = keyword; handleSearch()">
                {{ keyword }}
              </a>
            </div>
          </div>


        </div>
      </div>
    </div>

    <!-- 导航菜单 -->
    <div class="nav-menu">
      <div class="container">
        <div class="nav-content">
          <!-- 全部商品分类 -->
          <div class="category-menu">
            <div class="category-trigger" @mouseenter="showCategories = true" @mouseleave="showCategories = false">
              <LocalIcon name="fenlei" :size="20" />
              全部商品分类
            </div>
            <!-- 分类下拉菜单 -->
            <div class="category-dropdown" v-show="showCategories" @mouseenter="showCategories = true" @mouseleave="showCategories = false">
              <div class="category-list">
                <div 
                  v-for="category in categories" 
                  :key="category.id"
                  class="category-item"
                  @click="goToCategory(category.id)"
                >
                  <LocalIcon :name="category.icon" :size="16" />
                  <span>{{ category.name }}</span>
                  <div class="sub-categories" v-if="category.children">
                    <a v-for="sub in category.children" :key="sub.id" @click.stop="goToCategory(sub.id)">
                      {{ sub.name }}
                    </a>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 导航链接 -->
          <div class="nav-links">
            <a href="#" class="nav-link active">首页</a>
            <a href="#" class="nav-link">服装城</a>
            <a href="#" class="nav-link">美妆馆</a>
            <a href="#" class="nav-link">超市</a>
            <a href="#" class="nav-link">生鲜</a>
            <a href="#" class="nav-link">全球购</a>
            <a href="#" class="nav-link">闪购</a>
          </div>
        </div>
      </div>
    </div>

    <!-- 主要内容区 -->
    <div class="main-content">
      <div class="container">
        <!-- 轮播图和侧边栏 -->
        <div class="banner-section">
          <div class="banner-carousel">
            <HomeBannerCarousel 
              height="400px"
              :autoplay-interval="5000"
              @click="handleBannerCarouselClick"
            />
          </div>

          <!-- 右侧快捷入口 -->
          <div class="quick-entries">
            <div class="user-info-card" v-if="isLoggedIn">
              <div class="user-avatar">
                <el-avatar :size="50" :src="userInfo.avatar || defaultAvatar">
                  <el-icon><User /></el-icon>
                </el-avatar>
              </div>
              <div class="user-details">
                <p class="username">{{ displayUserName }}</p>
                <p class="user-level">会员等级：{{ userInfo.level || '普通会员' }}</p>
                <!-- 移动端补充：在用户信息卡片中提供可见的注销入口 -->
                <button class="logout-option compact" @click="handleLogout" aria-label="退出登录">
                  <el-icon class="logout-icon"><SwitchButton /></el-icon>
                  <span class="logout-text">退出登录</span>
                </button>
              </div>
            </div>
            <div class="login-card" v-else>
              <p>Hi！欢迎来到在线商城</p>
              <div class="login-buttons">
                <el-button type="danger" @click="handleLogin">登录</el-button>
              </div>
            </div>

            <div class="quick-links">
              <div class="quick-link" v-for="link in quickLinks" :key="link.id" @click="goToLink(link.url)">
                <LocalIcon :name="link.icon" :size="20" />
                <span>{{ link.name }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 秒杀活动 -->
        <div class="seckill-section">
          <div class="section-header">
            <h2>
              <LocalIcon name="tixing" :size="20" color="#ff4142" />
              京东秒杀
            </h2>
            <div class="countdown">
              <span>距离结束：</span>
              <div class="time-box">{{ countdown.hours }}</div>
              <span>:</span>
              <div class="time-box">{{ countdown.minutes }}</div>
              <span>:</span>
              <div class="time-box">{{ countdown.seconds }}</div>
            </div>
            <el-button type="danger" size="small">更多秒杀</el-button>
          </div>
          <div class="seckill-products">
            <div 
              v-for="product in seckillProducts" 
              :key="product.id"
              class="seckill-item"
              @click="goToProduct(product.id)"
            >
              <img :src="product.image" :alt="product.name" />
              <div class="seckill-price">
                <span class="current-price">¥{{ product.seckillPrice }}</span>
                <span class="original-price">¥{{ product.originalPrice }}</span>
              </div>
              <div class="seckill-progress">
                <el-progress :percentage="product.progress" :show-text="false" />
                <span class="progress-text">已抢{{ product.progress }}%</span>
              </div>
            </div>
            <!-- 空数据提示 -->
            <div v-if="seckillProducts.length === 0" class="empty-products">
              <el-icon :size="60" color="#ccc"><Box /></el-icon>
              <p class="empty-text">商品正在来的路上</p>
            </div>
          </div>
        </div>

        <!-- 推荐商品 -->
        <div class="recommend-section">
          <div class="section-header">
            <h2>为你推荐</h2>
            <div class="recommend-tabs">
              <span 
                v-for="tab in recommendTabs" 
                :key="tab.id"
                :class="['tab', { active: activeTab === tab.id }]"
                @click="activeTab = tab.id"
              >
                {{ tab.name }}
              </span>
            </div>
          </div>
          <div class="recommend-products">
            <div 
              v-for="product in recommendProducts" 
              :key="product.id"
              class="product-card"
              @click="goToProduct(product.id)"
            >
              <div class="product-image">
                <img :src="product.image" :alt="product.name" />
                <div class="product-tags">
                  <span v-for="tag in product.tags" :key="tag" class="tag">{{ tag }}</span>
                </div>
              </div>
              <div class="product-info">
                <h3 class="product-name">{{ product.name }}</h3>
                <p class="product-desc">{{ product.description }}</p>
                <div class="product-price">
                  <span class="current-price">¥{{ product.price }}</span>
                  <span class="original-price" v-if="product.originalPrice">¥{{ product.originalPrice }}</span>
                </div>
                <div class="product-stats">
                  <span class="sales">已售{{ product.sales }}件</span>
                  <div class="rating">
                    <el-rate v-model="product.rating" disabled show-score text-color="#ff9900" />
                    <span class="reviews">({{ product.reviews }})</span>
                  </div>
                </div>
              </div>
            </div>
            <!-- 空数据提示 -->
            <div v-if="recommendProducts.length === 0" class="empty-products large">
              <el-icon :size="80" color="#ccc"><Box /></el-icon>
              <p class="empty-text">商品正在来的路上</p>
              <p class="empty-subtext">敬请期待更多精彩商品</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部信息 -->
    <div class="footer">
      <div class="container">
        <div class="footer-content">
          <div class="footer-section">
            <h4>购物指南</h4>
            <ul>
              <li><a href="#">购物流程</a></li>
              <li><a href="#">会员介绍</a></li>
              <li><a href="#">生活旅行</a></li>
              <li><a href="#">常见问题</a></li>
            </ul>
          </div>
          <div class="footer-section">
            <h4>配送方式</h4>
            <ul>
              <li><a href="#">上门自提</a></li>
              <li><a href="#">211限时达</a></li>
              <li><a href="#">配送服务查询</a></li>
              <li><a href="#">配送费收取标准</a></li>
            </ul>
          </div>
          <div class="footer-section">
            <h4>支付方式</h4>
            <ul>
              <li><a href="#">货到付款</a></li>
              <li><a href="#">在线支付</a></li>
              <li><a href="#">分期付款</a></li>
              <li><a href="#">邮局汇款</a></li>
            </ul>
          </div>
          <div class="footer-section">
            <h4>售后服务</h4>
            <ul>
              <li><a href="#">售后政策</a></li>
              <li><a href="#">价格保护</a></li>
              <li><a href="#">退款说明</a></li>
              <li><a href="#">返修/退换货</a></li>
            </ul>
          </div>
        </div>
        <div class="footer-bottom">
          <p>&copy; 2025 在线商城 版权所有 | 京ICP备12345678号</p>
        </div>
      </div>
    </div>

    <!-- 登录弹窗 -->
    <LoginModal 
      v-model="showLoginModal"
      @login-success="handleLoginSuccess"
    />
  </div>
</template>

<script setup>
/**
 * V1.0 2025-11-08T08:52:37+08:00 修改日志：
 * - 添加首页顶部欢迎区域的悬停交互与可访问性（键盘可聚焦）
 * - 集成统一注销流程（前端会话清理 + 后端登出 API）
 * - 增加移动端紧凑版注销入口与过渡动画
 * @author lingbai
 * @description 首页欢迎信息区域与注销逻辑的交互优化，提升安全与可用性
 */
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { logout as apiLogout } from '@/api/auth'
import { SwitchButton, ArrowDown, Box, User } from '@element-plus/icons-vue'
import * as logger from '@/utils/logger'
import LoginModal from '@/components/LoginModal.vue'
import LocalIcon from '@/components/LocalIcon.vue'
import HomeBannerCarousel from '@/components/HomeBannerCarousel.vue'
import { getCartCount } from '@/api/cart'
import { getHotProducts, searchProducts } from '@/api/product'

const router = useRouter()
const userStore = useUserStore()

// 响应式数据
const searchKeyword = ref('')
const showCategories = ref(false)
const showMerchantMenu = ref(false)
const isLoggedIn = computed(() => userStore.isLoggedIn)
const cartCount = ref(3)
const activeTab = ref(1)
// 欢迎区域注销显示状态与防抖
const logoutVisible = ref(false)
const loggingOut = ref(false)

// 弹窗状态
const showLoginModal = ref(false)

// 用户信息 - 直接使用 userStore 的数据保证实时同步
const userInfo = computed(() => userStore.userInfo)

// 默认头像
const defaultAvatar = '/images/default-avatar.png'

/**
 * 显示用户名称
 * 优先级：昵称 > 格式化的用户名 > 手机号隐藏中间四位 > 默认值
 */
const displayUserName = computed(() => {
  const info = userStore.userInfo
  if (!info) return '用户'
  
  // 优先使用昵称
  if (info.nickname && info.nickname.trim()) {
    return info.nickname
  }
  
  // 如果用户名不是 user_ 开头的自动生成格式，使用用户名
  if (info.username && !info.username.startsWith('user_')) {
    return info.username
  }
  
  // 如果有手机号，隐藏中间四位显示
  if (info.phone) {
    return info.phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
  }
  
  // 如果用户名是 user_手机号 格式，提取手机号并隐藏中间四位
  if (info.username && info.username.startsWith('user_')) {
    const phone = info.username.substring(5)
    if (phone.length === 11) {
      return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
    }
  }
  
  // 最后使用用户名或默认值
  return info.username || '用户'
})

// 热门搜索关键词
const hotKeywords = ref(['iPhone15', '笔记本电脑', '运动鞋', '连衣裙', '护肤品'])

// 商品分类数据
const categories = ref([
  {
    id: 1,
    name: '手机数码',
    icon: 'shumashouji',
    children: [
      { id: 11, name: '手机通讯' },
      { id: 12, name: '数码配件' },
      { id: 13, name: '智能设备' }
    ]
  },
  {
    id: 2,
    name: '电脑办公',
    icon: 'shumashouji',
    children: [
      { id: 21, name: '笔记本' },
      { id: 22, name: '台式机' },
      { id: 23, name: '办公设备' }
    ]
  },
  {
    id: 3,
    name: '服装服饰',
    icon: 'nanzhuang',
    children: [
      { id: 31, name: '男装' },
      { id: 32, name: '女装' },
      { id: 33, name: '内衣配饰' }
    ]
  },
  {
    id: 4,
    name: '家居家装',
    icon: 'jiajujiancai',
    children: [
      { id: 41, name: '家具' },
      { id: 42, name: '家装建材' },
      { id: 43, name: '厨具' }
    ]
  },
  {
    id: 5,
    name: '图书文娱',
    icon: 'tushuyinxiang',
    children: [
      { id: 51, name: '图书' },
      { id: 52, name: '文具' },
      { id: 53, name: '电子书' }
    ]
  }
])

// 轮播图数据 - 虚拟图片已移除
const banners = ref([])

// 快捷入口
const quickLinks = ref([
  { id: 1, name: '充值', icon: 'youhuiquan', url: '/recharge' },
  { id: 2, name: '优惠券', icon: 'youhuiquan', url: '/coupons' },
  { id: 3, name: '白条', icon: 'youhuiquan', url: '/credit' },
  { id: 4, name: '领券', icon: 'youhuiquan', url: '/get-coupons' }
])

// 秒杀倒计时
const countdown = reactive({
  hours: '02',
  minutes: '30',
  seconds: '45'
})

// 秒杀商品（已清空虚拟数据）
const seckillProducts = ref([])

// 推荐标签
const recommendTabs = ref([
  { id: 1, name: '猜你喜欢' },
  { id: 2, name: '京东超市' },
  { id: 3, name: '数码新品' },
  { id: 4, name: '时尚穿搭' }
])

// 推荐商品（已清空虚拟数据）
const recommendProducts = ref([])

// 倒计时定时器
let countdownTimer = null

/**
 * 处理搜索
 */
const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  
  // 直接跳转到搜索结果页面，带上搜索关键词
  // 搜索页面会自行调用搜索API获取结果
  router.push({
    path: '/search',
    query: { q: searchKeyword.value }
  })
}

/**
 * 处理登录
 */
const handleLogin = () => {
  showLoginModal.value = true
}

/**
 * 处理注册
 */
const handleRegister = () => {
  router.push('/auth/register')
}

/**
 * 登录成功处理
 * @param {Object} loginData - 登录数据
 */
const handleLoginSuccess = async (loginData) => {
  console.log('登录成功:', loginData)
  ElMessage.success('登录成功！')
  
  // 登录成功后获取购物车数量
  await loadCartCount()
}



/**
 * 跳转到购物车
 */
const goToCart = () => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    showLoginModal.value = true
    return
  }
  router.push('/cart')
}

/**
 * 跳转到个人中心
 */
const goToProfile = () => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    showLoginModal.value = true
    return
  }
  router.push('/user/profile')
}

/**
 * 跳转到订单页面
 */
const goToOrders = () => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    showLoginModal.value = true
    return
  }
  router.push('/user/orders')
}

/**
 * 跳转到分类页面
 */
const goToCategory = (categoryId) => {
  ElMessage.info(`跳转到分类页面: ${categoryId}`)
  // router.push(`/category/${categoryId}`)
}

/**
 * 跳转到商品详情（新窗口打开）
 */
const goToProduct = (productId) => {
  window.open(`/product/${productId}`, '_blank')
}

/**
 * 跳转到轮播图链接
 */
const goToBanner = (link) => {
  ElMessage.info(`跳转到: ${link}`)
  // router.push(link)
}

/**
 * 处理轮播图点击事件
 * @param {Object} payload 点击数据 { banner, index }
 */
const handleBannerCarouselClick = (payload) => {
  console.log('轮播图点击:', payload)
}

/**
 * 跳转到快捷链接
 */
const goToLink = (url) => {
  ElMessage.info(`跳转到: ${url}`)
  // router.push(url)
}

/**
 * 跳转到商家登录页面（新窗口打开）
 */
const goToMerchantLogin = () => {
  showMerchantMenu.value = false
  window.open('/merchant/login', '_blank')
}

/**
 * 跳转到商家注册页面（新窗口打开）
 */
const goToMerchantRegister = () => {
  showMerchantMenu.value = false
  window.open('/merchant/register', '_blank')
}

/**
 * 跳转到商家后台
 */
const goToMerchant = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/auth/login')
    return
  }
  
  // 跳转到商家登录页面（如果已是商家会自动跳转到仪表板）
  router.push('/merchant/login')
}

/**
 * 更新倒计时
 */
const updateCountdown = () => {
  // 这里应该是真实的倒计时逻辑
  // 为了演示，我们只是简单地减少秒数
  let seconds = parseInt(countdown.seconds)
  if (seconds > 0) {
    countdown.seconds = (seconds - 1).toString().padStart(2, '0')
  } else {
    countdown.seconds = '59'
    let minutes = parseInt(countdown.minutes)
    if (minutes > 0) {
      countdown.minutes = (minutes - 1).toString().padStart(2, '0')
    } else {
      countdown.minutes = '59'
      let hours = parseInt(countdown.hours)
      if (hours > 0) {
        countdown.hours = (hours - 1).toString().padStart(2, '0')
      }
    }
  }
}

/**
 * 加载购物车数量
 */
const loadCartCount = async () => {
  if (!userStore.isLoggedIn || !userStore.userInfo?.id) {
    cartCount.value = 0
    return
  }
  
  try {
    const response = await getCartCount(userStore.userInfo.id)
    if (response && response.code === 200) {
      cartCount.value = response.data || 0
    } else {
      cartCount.value = 0
    }
  } catch (error) {
    // 购物车服务可能未启动或不可用，静默处理错误
    // 避免因购物车API失败而触发“登录过期”弹窗
    console.warn('获取购物车数量失败（购物车服务可能未启动）:', error.message)
    cartCount.value = 0
  }
}

/**
 * 加载首页推荐商品
 */
const loadRecommendProducts = async () => {
  try {
    const response = await getHotProducts({ limit: 12 })
    
    if (response.code === 200 && response.data) {
      // 将后端数据转换为前端需要的格式
      recommendProducts.value = response.data.map(product => ({
        id: product.id,
        name: product.name || product.productName,
        image: product.mainImage || product.imageUrl || '/images/placeholder.png',
        description: product.description?.substring(0, 50) || '暂无描述',
        price: product.price || 0,
        originalPrice: product.marketPrice || product.originalPrice,
        sales: product.salesCount || product.sales || 0,
        rating: product.rating || 5,
        reviews: product.reviewCount || product.reviews || 0,
        tags: product.isNew ? ['新品'] : []
      }))
    }
  } catch (error) {
    console.error('加载推荐商品失败:', error)
    // 静默失败，不打断用户体验
  }
}

/**
 * 加载秒杀商品（使用热销商品代替）
 */
const loadSeckillProducts = async () => {
  try {
    const response = await getHotProducts({ limit: 5 })
    
    if (response.code === 200 && response.data) {
      seckillProducts.value = response.data.map(product => ({
        id: product.id,
        name: product.name || product.productName,
        image: product.mainImage || product.imageUrl || '/images/placeholder.png',
        seckillPrice: (product.price * 0.8).toFixed(2), // 假设秒杀价是原价的8折
        originalPrice: product.price || 0,
        progress: Math.floor(Math.random() * 50) + 30 // 随机进度30-80%
      }))
    }
  } catch (error) {
    console.error('加载秒杀商品失败:', error)
  }
}

/**
 * 组件挂载时的初始化
 */
onMounted(async () => {
  // 启动倒计时
  countdownTimer = setInterval(updateCountdown, 1000)
  
  // 加载首页商品数据
  await Promise.all([
    loadRecommendProducts(),
    loadSeckillProducts()
  ])
  
  // 如果用户已登录，加载购物车数量并刷新用户信息
  if (userStore.isLoggedIn) {
    logger.debug('初始化本地用户会话', userStore.userInfo)
    
    // 强制刷新用户信息以确保显示最新数据（普通用户首页需要最新的昵称和头像）
    // 使用 forceRefresh=true 确保即使有 merchantId 也会刷新
    try {
      await userStore.fetchUserInfo(true)
      logger.info('用户信息已刷新', userStore.userInfo)
    } catch (error) {
      logger.warn('刷新用户信息失败', error)
    }
    
    await loadCartCount()
  }
})

/**
 * 组件卸载时的清理
 */
onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
})

/**
 * 执行注销流程并重定向到登录页
 * @author lingbai
 * @version V1.0 2025-11-08T08:52:37+08:00
 * V1.1 2025-11-08T09:01:16+08:00：移入 script setup 以修复 SFC 解析错误
 * @returns {Promise<void>} 无返回值，完成后跳转到登录页
 * @description 调用后端登出 API（尽力而为），随后统一清理本地会话数据，
 * 并通过路由重定向至登录页。Axios 已启用 `withCredentials` 且自动注入 CSRF 头，
 * 以降低跨站请求风险。即使后端失败也强制前端清理，避免残留会话导致安全问题。
 */
const handleLogout = async () => {
  if (loggingOut.value) { return }
  loggingOut.value = true
  logger.info('触发用户注销流程')
  try {
    await apiLogout()
    logger.info('后端登出 API 调用成功')
  } catch (e) {
    // 即便后端失败也要清理前端会话，避免风险
    logger.warn('后端登出 API 调用失败，继续本地会话清理', e)
  }
  // 统一清理会话与本地存储
  userStore.userLogout()
  // 反馈与重定向到主页
  ElMessage.success('您已退出登录')
  router.replace('/home')
  loggingOut.value = false
}
</script>

<style scoped>
/* 全局样式 */
.home-container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 15px;
}

/* 顶部导航栏 */
.top-bar {
  background-color: #e3101e;
  color: white;
  font-size: 12px;
  padding: 8px 0;
}

.top-links {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-links {
  display: flex;
  gap: 15px;
}

.user-links a {
  color: white;
  text-decoration: none;
}

.cart-link {
  display: flex;
  align-items: center;
  gap: 5px;
  cursor: pointer;
  color: white;
  text-decoration: none;
  position: relative;
  padding: 5px 8px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.cart-link:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.user-links a:hover {
  text-decoration: underline;
}

/* 商家服务下拉菜单 */
.merchant-service-dropdown {
  position: relative;
  display: inline-flex;
  align-items: center;
}

.merchant-service-trigger {
  display: flex;
  align-items: center;
  gap: 4px;
  color: white;
  text-decoration: none;
  cursor: pointer;
  padding: 5px 8px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.merchant-service-trigger:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.merchant-service-trigger .arrow-icon {
  font-size: 12px;
  transition: transform 0.3s;
}

.merchant-service-dropdown:hover .merchant-service-trigger .arrow-icon {
  transform: rotate(180deg);
}

.merchant-menu {
  position: absolute;
  top: 100%;
  left: 0;
  margin-top: 8px;
  background-color: #ffffff !important;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  z-index: 1000;
  min-width: 120px;
  overflow: hidden;
}

.merchant-menu-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 15px;
  color: #333333 !important;
  background-color: #ffffff;
  text-decoration: none;
  cursor: pointer;
  transition: background-color 0.3s, color 0.3s;
  white-space: nowrap;
  border-bottom: 1px solid #f0f0f0;
  font-size: 14px;
}

.merchant-menu-item:last-child {
  border-bottom: none;
}

.merchant-menu-item:hover {
  background-color: #f5f5f5 !important;
  color: #e3101e !important;
}

/* 用户欢迎下拉菜单 */
.welcome-dropdown {
  position: relative;
  display: inline-flex;
  align-items: center;
}

.welcome-trigger {
  display: flex;
  align-items: center;
  gap: 4px;
  color: white;
  text-decoration: none;
  cursor: pointer;
  padding: 5px 8px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.welcome-trigger:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.welcome-trigger .arrow-icon {
  font-size: 12px;
  transition: transform 0.3s;
}

.welcome-dropdown:hover .welcome-trigger .arrow-icon {
  transform: rotate(180deg);
}

.welcome-menu {
  position: absolute;
  top: 100%;
  left: 0;
  margin-top: 8px;
  background-color: #ffffff !important;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  z-index: 1000;
  min-width: 120px;
  overflow: hidden;
}

.welcome-menu-item {
  display: flex;
  align-items: center;
  padding: 10px 15px;
  color: #333333 !important;
  background-color: #ffffff;
  text-decoration: none;
  cursor: pointer;
  transition: background-color 0.3s, color 0.3s;
  white-space: nowrap;
  font-size: 14px;
}

.welcome-menu-item:hover {
  background-color: #f5f5f5 !important;
  color: #e3101e !important;
}

/* 动效：淡入 + 上下滑动 */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.2s ease;
}
.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

/* 移动端：在用户信息卡片中显示紧凑的注销按钮 */
.logout-option.compact {
  display: none;
  margin-top: 8px;
  font-size: 12px;
  padding: 3px 6px;
}

/* 主导航栏 */
.main-header {
  background-color: white;
  padding: 20px 0;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
}

.logo h1 {
  color: #e3101e;
  font-size: 28px;
  font-weight: bold;
  margin: 0;
}

.search-area {
  flex: 1;
  max-width: 600px;
  margin: 0 40px;
}

.search-input {
  width: 100%;
}

.hot-keywords {
  margin-top: 8px;
  font-size: 12px;
  color: #666;
}

.hot-keywords span {
  margin-right: 10px;
}

.hot-keywords a {
  color: #666;
  text-decoration: none;
  margin-right: 15px;
}

.hot-keywords a:hover {
  color: #e3101e;
}

.cart-area {
  display: flex;
  align-items: center;
}

.cart-icon {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 10px;
  border-radius: 4px;
  transition: background-color 0.3s;
  position: relative;
}

.cart-icon:hover {
  background-color: #f5f5f5;
}

.cart-badge {
  position: absolute;
  top: 5px;
  right: 5px;
}

/* 导航菜单 */
.nav-menu {
  background-color: white;
  border-bottom: 1px solid #e0e0e0;
}

.nav-content {
  display: flex;
  align-items: center;
  height: 50px;
}

.category-menu {
  position: relative;
  margin-right: 30px;
}

.category-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  background-color: #e3101e;
  color: white;
  cursor: pointer;
  border-radius: 4px;
  font-weight: 500;
}

.category-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  width: 200px;
  background-color: white;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  z-index: 1000;
}

.category-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 15px;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
  position: relative;
}

.category-item:hover {
  background-color: #f5f5f5;
}

.category-item:last-child {
  border-bottom: none;
}

.sub-categories {
  position: absolute;
  left: 100%;
  top: 0;
  width: 150px;
  background-color: white;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  display: none;
}

.category-item:hover .sub-categories {
  display: block;
}

.sub-categories a {
  display: block;
  padding: 8px 12px;
  color: #333;
  text-decoration: none;
  border-bottom: 1px solid #f0f0f0;
}

.sub-categories a:hover {
  background-color: #f5f5f5;
  color: #e3101e;
}

.nav-links {
  display: flex;
  gap: 30px;
}

.nav-link {
  color: #333;
  text-decoration: none;
  font-weight: 500;
  padding: 10px 0;
  border-bottom: 2px solid transparent;
  transition: all 0.3s;
}

.nav-link:hover,
.nav-link.active {
  color: #e3101e;
  border-bottom-color: #e3101e;
}

.nav-link.merchant-entry {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #ff6700;
  font-weight: 600;
}

.nav-link.merchant-entry:hover {
  color: #ff8533;
  border-bottom-color: #ff6700;
  transform: translateY(-2px);
}

/* 主要内容区 */
.main-content {
  padding: 20px 0;
}

.banner-section {
  display: flex;
  gap: 20px;
  margin-bottom: 30px;
}

.banner-carousel {
  flex: 1;
}

.banner-item {
  position: relative;
  cursor: pointer;
  border-radius: 8px;
  overflow: hidden;
}

.banner-item img {
  width: 100%;
  height: 400px;
  object-fit: cover;
}

.banner-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(transparent, rgba(0,0,0,0.7));
  color: white;
  padding: 30px;
}

.banner-overlay h3 {
  font-size: 24px;
  margin: 0 0 10px 0;
}

.banner-overlay p {
  font-size: 16px;
  margin: 0;
}

.quick-entries {
  width: 300px;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.user-info-card,
.login-card {
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.user-info-card {
  display: flex;
  align-items: center;
  gap: 15px;
}

.user-details .username {
  font-weight: bold;
  margin: 0 0 5px 0;
}

.user-details .user-level {
  color: #666;
  font-size: 12px;
  margin: 0;
}

.login-card {
  text-align: center;
}

.login-card p {
  margin: 0 0 15px 0;
  color: #666;
}

.login-buttons {
  display: flex;
  gap: 10px;
  justify-content: center;
}

.quick-links {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  overflow: hidden;
}

.quick-link {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 15px 20px;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.3s;
}

.quick-link:hover {
  background-color: #f5f5f5;
}

.quick-link:last-child {
  border-bottom: none;
}

/* 秒杀活动 */
.seckill-section {
  background-color: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 30px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.section-header h2 {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 0;
  color: #333;
  font-size: 20px;
}

.countdown {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 14px;
  color: #666;
}

.time-box {
  background-color: #333;
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-weight: bold;
  min-width: 30px;
  text-align: center;
}

.seckill-products {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.seckill-item {
  text-align: center;
  cursor: pointer;
  padding: 15px;
  border-radius: 8px;
  transition: transform 0.3s;
}

.seckill-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.seckill-item img {
  width: 100%;
  height: 150px;
  object-fit: cover;
  border-radius: 4px;
  margin-bottom: 10px;
}

.seckill-price {
  margin-bottom: 10px;
}

.current-price {
  color: #e3101e;
  font-size: 18px;
  font-weight: bold;
}

.original-price {
  color: #999;
  font-size: 14px;
  text-decoration: line-through;
  margin-left: 10px;
}

.seckill-progress {
  text-align: center;
}

.progress-text {
  font-size: 12px;
  color: #666;
  margin-top: 5px;
}

/* 空数据提示 */
.empty-products {
  grid-column: 1 / -1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #999;
}

.empty-products.large {
  padding: 100px 20px;
}

.empty-text {
  margin-top: 20px;
  font-size: 16px;
  color: #666;
  font-weight: 500;
}

.empty-subtext {
  margin-top: 10px;
  font-size: 14px;
  color: #999;
}

/* 推荐商品 */
.recommend-section {
  background-color: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.recommend-tabs {
  display: flex;
  gap: 20px;
}

.tab {
  padding: 8px 16px;
  cursor: pointer;
  border-radius: 20px;
  transition: all 0.3s;
  font-size: 14px;
}

.tab:hover,
.tab.active {
  background-color: #e3101e;
  color: white;
}

.recommend-products {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-top: 20px;
}

.product-card {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
}

.product-card:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  transform: translateY(-2px);
}

.product-image {
  position: relative;
  overflow: hidden;
}

.product-image img {
  width: 100%;
  height: 200px;
  object-fit: cover;
  transition: transform 0.3s;
}

.product-card:hover .product-image img {
  transform: scale(1.05);
}

.product-tags {
  position: absolute;
  top: 10px;
  left: 10px;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.tag {
  background-color: #e3101e;
  color: white;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 10px;
}

.product-info {
  padding: 15px;
}

.product-name {
  font-size: 14px;
  font-weight: 500;
  margin: 0 0 8px 0;
  color: #333;
  line-height: 1.4;
  height: 40px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.product-desc {
  font-size: 12px;
  color: #666;
  margin: 0 0 10px 0;
  line-height: 1.4;
  height: 32px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.product-price {
  margin-bottom: 10px;
}

.product-price .current-price {
  color: #e3101e;
  font-size: 16px;
  font-weight: bold;
}

.product-price .original-price {
  color: #999;
  font-size: 12px;
  text-decoration: line-through;
  margin-left: 8px;
}

.product-stats {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #666;
}

.rating {
  display: flex;
  align-items: center;
  gap: 5px;
}

/* 底部信息 */
.footer {
  background-color: #333;
  color: white;
  padding: 40px 0 20px;
  margin-top: 50px;
}

.footer-content {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 40px;
  margin-bottom: 30px;
}

.footer-section h4 {
  color: white;
  margin: 0 0 20px 0;
  font-size: 16px;
}

.footer-section ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.footer-section li {
  margin-bottom: 8px;
}

.footer-section a {
  color: #ccc;
  text-decoration: none;
  font-size: 14px;
}

.footer-section a:hover {
  color: #e3101e;
}

.footer-bottom {
  text-align: center;
  padding-top: 20px;
  border-top: 1px solid #555;
  color: #999;
  font-size: 12px;
}

/* 本地图标样式 */
.local-icon {
  display: inline-block;
  vertical-align: middle;
  object-fit: contain;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .seckill-products {
    grid-template-columns: repeat(3, 1fr);
  }
  
  .recommend-products {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 992px) {
  .container {
    padding: 0 20px;
  }
  
  .banner-section {
    flex-direction: column;
  }
  
  .quick-entries {
    width: 100%;
  }
  
  .seckill-products {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .recommend-products {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .container {
    padding: 0 15px;
  }

  .top-bar {
    display: none; /* 移动端隐藏顶部导航 */
  }

  .header-content {
    flex-direction: column;
    height: auto;
    gap: 15px;
    padding: 15px 0;
  }

  .logo h1 {
    font-size: 24px;
  }

  .search-area {
    margin: 0;
    max-width: none;
    width: 100%;
  }

  .cart-area {
    position: fixed;
    bottom: 20px;
    right: 20px;
    z-index: 1000;
    background-color: #e3101e;
    border-radius: 50%;
    width: 60px;
    height: 60px;
    justify-content: center;
    box-shadow: 0 4px 12px rgba(227, 16, 30, 0.3);
  }

  .cart-icon {
    color: white;
    flex-direction: column;
    gap: 2px;
    font-size: 12px;
  }

  .nav-content {
    flex-direction: column;
    height: auto;
    gap: 15px;
    padding: 10px 0;
  }

  .category-menu {
    margin-right: 0;
    width: 100%;
  }

  .category-trigger {
    width: 100%;
    justify-content: center;
  }

  .nav-links {
    flex-wrap: wrap;
    gap: 15px;
    justify-content: center;
  }

  .nav-link {
    font-size: 14px;
  }

  .banner-carousel {
    margin-bottom: 20px;
  }

  .banner-item img {
    height: 250px;
  }

  .banner-overlay {
    padding: 20px;
  }

  .banner-overlay h3 {
    font-size: 18px;
  }

  .banner-overlay p {
    font-size: 14px;
  }

  .banner-section {
    flex-direction: column;
  }
  
  .quick-entries {
    width: 100%;
    margin-top: 20px;
  }

  /* 移动端始终显示用户卡片中的注销按钮 */
  .logout-option.compact {
    display: inline-flex;
  }

  .seckill-products {
    grid-template-columns: repeat(2, 1fr);
    gap: 15px;
  }

  .seckill-item {
    padding: 10px;
  }

  .seckill-item img {
    height: 120px;
  }

  .recommend-products {
    grid-template-columns: repeat(2, 1fr);
    gap: 15px;
  }

  .product-card {
    padding: 10px;
  }

  .product-image img {
    height: 150px;
  }

  .footer-content {
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;
  }

  .footer-section h4 {
    font-size: 14px;
  }

  .footer-section ul li a {
    font-size: 12px;
  }
}

@media (max-width: 480px) {
  .container {
    padding: 0 10px;
  }

  .logo h1 {
    font-size: 20px;
  }

  .seckill-products,
  .recommend-products {
    grid-template-columns: 1fr;
  }

  .footer-content {
    grid-template-columns: 1fr;
  }

  .category-dropdown {
    width: 100%;
    left: 0;
    right: 0;
  }

  .sub-categories {
    position: static;
    width: 100%;
    margin-top: 5px;
    border: none;
    box-shadow: none;
    background-color: #f5f5f5;
  }

  .hot-keywords {
    display: none; /* 移动端隐藏热门搜索 */
  }
}
</style>
