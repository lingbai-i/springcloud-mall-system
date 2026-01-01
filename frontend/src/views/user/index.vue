<!--
  用户中心主页面
  
  @author lingbai
  @version 1.0
  @since 2025-10-21
-->
<template>
  <div class="user-center">
    <!-- 侧边栏布局 -->
    <div class="user-layout">
      <!-- 左侧导航菜单 -->
      <div class="sidebar">
        <!-- 返回商城首页按钮 -->
        <el-button 
          type="primary" 
          plain 
          class="back-home-btn"
          @click="goToHome"
        >
          <el-icon><HomeFilled /></el-icon>
          返回商城首页
        </el-button>

        <!-- 用户信息卡片 -->
        <el-card class="user-info-card" shadow="hover">
          <div class="user-header">
            <div class="avatar-section">
              <el-avatar :size="60" :src="userInfo.avatar" class="user-avatar">
                {{ userInfo.nickname?.charAt(0) || 'U' }}
              </el-avatar>
            </div>
            <div class="user-details">
              <h3 class="username">{{ userInfo.nickname || userInfo.username }}</h3>
              <p v-if="userInfo.bio" class="user-bio">{{ userInfo.bio }}</p>
            </div>
          </div>
        </el-card>

        <!-- 功能菜单 -->
        <el-menu
          :default-active="activeMenu"
          class="user-menu"
          @select="handleMenuSelect"
        >
          <el-menu-item
            v-for="menu in menuList"
            :key="menu.key"
            :index="menu.key"
          >
            <LocalIcon :name="menu.icon" :size="20" :color="activeMenu === menu.key ? menu.color : '#606266'" />
            <span>{{ menu.title }}</span>
            <el-badge v-if="menu.badge && cartCount > 0" :value="cartCount" :max="99" class="menu-badge" />
          </el-menu-item>
        </el-menu>
      </div>

      <!-- 右侧内容区域 -->
      <div class="content-area">
        <!-- 个人资料 -->
        <div v-if="activeMenu === 'profile'" class="content-panel">
          <ProfileView />
        </div>

        <!-- 账户安全 -->
        <div v-if="activeMenu === 'security'" class="content-panel">
          <SecurityView />
        </div>

        <!-- 我的订单 -->
        <div v-if="activeMenu === 'orders'" class="content-panel">
          <OrdersView />
        </div>

        <!-- 收货地址 -->
        <div v-if="activeMenu === 'addresses'" class="content-panel">
          <AddressesView />
        </div>

        <!-- 我的收藏 -->
        <div v-if="activeMenu === 'favorites'" class="content-panel">
          <FavoritesView />
        </div>

        <!-- 购物车 -->
        <div v-if="activeMenu === 'cart'" class="content-panel">
          <CartView />
        </div>

        <!-- 系统设置 -->
        <div v-if="activeMenu === 'settings'" class="content-panel">
          <SettingsView />
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import { ElMessage, ElMessageBox } from 'element-plus'
import { HomeFilled } from '@element-plus/icons-vue'
import LocalIcon from '@/components/LocalIcon.vue'
import ProfileView from './profile.vue'
import SecurityView from './security.vue'
import OrdersView from './orders.vue'
import AddressesView from './addresses.vue'
import FavoritesView from './favorites.vue'
import SettingsView from './Settings.vue'
import CartView from './cart.vue'
import * as logger from '@/utils/logger'

/**
 * 用户中心主页面组件
 * 
 * 功能包括：
 * - 显示用户基本信息
 * - 提供功能菜单导航
 * - 头像上传功能
 * - 路由同步：支持直接访问子路由
 * 
 * @author lingbai
 * @version 1.1
 * @since 2025-10-21
 * @updated 2025-12-28 - 添加路由同步功能
 */

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const cartStore = useCartStore()

// 响应式数据
const userInfo = computed(() => userStore.userInfo || {})
const cartCount = computed(() => cartStore.totalCount || 0)


// 路由路径到菜单key的映射
const routeToMenuMap = {
  '/user/profile': 'profile',
  '/user/security': 'security',
  '/user/orders': 'orders',
  '/user/addresses': 'addresses',
  '/user/favorites': 'favorites',
  '/user/cart': 'cart',
  '/user/settings': 'settings',
  '/user/password': 'security'
}

// 菜单key到路由路径的映射
const menuToRouteMap = {
  'profile': '/user/profile',
  'security': '/user/security',
  'orders': '/user/orders',
  'addresses': '/user/addresses',
  'favorites': '/user/favorites',
  'cart': '/user/cart',
  'settings': '/user/settings'
}

// 根据当前路由初始化激活菜单
const getActiveMenuFromRoute = () => {
  const path = route.path
  return routeToMenuMap[path] || 'profile'
}

const activeMenu = ref(getActiveMenuFromRoute())

// 监听路由变化，同步更新激活菜单
watch(() => route.path, (newPath) => {
  const menuKey = routeToMenuMap[newPath]
  if (menuKey && menuKey !== activeMenu.value) {
    activeMenu.value = menuKey
  }
}, { immediate: true })

// 功能菜单配置
const menuList = reactive([
  {
    key: 'profile',
    title: '个人资料',
    description: '编辑个人信息',
    icon: 'wo',
    color: '#409EFF'
  },
  {
    key: 'security',
    title: '账户安全',
    description: '密码和安全设置',
    icon: 'anquan',
    color: '#F56C6C'
  },
  {
    key: 'orders',
    title: '我的订单',
    description: '查看订单状态',
    icon: 'quanbudingdan',
    color: '#E6A23C'
  },
  {
    key: 'addresses',
    title: '收货地址',
    description: '管理收货地址',
    icon: 'dizhi',
    color: '#67C23A'
  },
  {
    key: 'favorites',
    title: '我的收藏',
    description: '收藏的商品',
    icon: 'shoucang',
    color: '#FF6B6B'
  },
  {
    key: 'cart',
    title: '购物车',
    description: '购物车商品',
    icon: 'gouwuche',
    color: '#409EFF'
  },
  {
    key: 'settings',
    title: '系统设置',
    description: '个性化设置',
    icon: 'shezhi',
    color: '#9C88FF'
  }
])

/**
 * 组件挂载时初始化
 */
onMounted(async () => {
  // 如果用户信息为空，尝试获取
  if (!userStore.userInfo || !userStore.userInfo.userId) {
    try {
      await userStore.fetchUserInfo()
      logger.info('用户中心页面加载，用户信息已刷新')
    } catch (error) {
      console.error('获取用户信息失败:', error)
      ElMessage.error('获取用户信息失败')
    }
  }
  
  // 如果当前路由是 /user，重定向到 /user/profile
  if (route.path === '/user' || route.path === '/user/') {
    router.replace('/user/profile')
  }
})

/**
 * 处理菜单选择 - 同时更新路由
 */
const handleMenuSelect = (key) => {
  activeMenu.value = key
  const targetRoute = menuToRouteMap[key]
  if (targetRoute && route.path !== targetRoute) {
    router.push(targetRoute)
  }
}

/**
 * 返回商城首页
 */
const goToHome = () => {
  router.push('/home')
}

/**
 * 获取性别标签类型
 */
const getGenderTagType = (gender) => {
  switch (gender) {
    case 1: return 'primary'
    case 2: return 'danger'
    default: return 'info'
  }
}

/**
 * 获取性别文本
 */
const getGenderText = (gender) => {
  switch (gender) {
    case 1: return '男'
    case 2: return '女'
    default: return '未知'
  }
}

/**
 * 格式化日期
 */
const formatDate = (dateStr) => {
  if (!dateStr) return '暂无'
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}
</script>

<style scoped>
.user-center {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}

.user-layout {
  display: flex;
  gap: 20px;
  min-height: calc(100vh - 200px);
}

/* 侧边栏样式 */
.sidebar {
  width: 260px;
  flex-shrink: 0;
}

.back-home-btn {
  width: 100%;
  margin-bottom: 16px;
  height: 44px;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.user-info-card {
  margin-bottom: 20px;
}

.user-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.user-avatar {
  border: 2px solid #f0f0f0;
}



.user-details {
  width: 100%;
  text-align: center;
}

.username {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 18px;
  font-weight: 600;
}

.user-bio {
  color: #606266;
  font-size: 12px;
  line-height: 1.5;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-menu {
  border: none;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}

.user-menu .el-menu-item {
  height: 50px;
  line-height: 50px;
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 0;
  padding: 0 20px;
}

.user-menu .el-menu-item:hover {
  background-color: #f5f7fa;
}

.user-menu .el-menu-item.is-active {
  background-color: #ecf5ff;
  color: #409EFF;
}

.menu-badge {
  margin-left: auto;
}

.menu-badge :deep(.el-badge__content) {
  transform: scale(0.8);
}

/* 内容区域样式 */
.content-area {
  flex: 1;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}

.content-panel {
  width: 100%;
  height: 100%;
}



/* 响应式设计 */
@media (max-width: 768px) {
  .user-center {
    padding: 10px;
  }
  
  .user-layout {
    flex-direction: column;
  }
  
  .sidebar {
    width: 100%;
  }
  
  .user-menu .el-menu-item {
    height: 45px;
    line-height: 45px;
  }
}
</style>
