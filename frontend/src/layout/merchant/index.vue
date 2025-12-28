<template>
  <div class="merchant-layout">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '240px'" class="merchant-sidebar">
      <div class="logo-container">
        <img v-if="!isCollapse" src="/logo.png" alt="Logo" class="logo" />
        <img v-else src="/logo.png" alt="Logo" class="logo-mini" />
        <h1 v-if="!isCollapse" class="title">商家中心</h1>
      </div>
      
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :unique-opened="true"
        class="merchant-menu"
        background-color="#001529"
        text-color="#ffffff"
        active-text-color="#52c41a"
        router
      >
        <el-menu-item index="/merchant/dashboard">
          <el-icon><DataBoard /></el-icon>
          <template #title>仪表板</template>
        </el-menu-item>
        
        <el-sub-menu index="products">
          <template #title>
            <el-icon><Goods /></el-icon>
            <span>商品管理</span>
          </template>
          <el-menu-item index="/merchant/products">商品列表</el-menu-item>
        </el-sub-menu>
        
        <el-sub-menu index="orders">
          <template #title>
            <el-icon><Document /></el-icon>
            <span>订单管理</span>
          </template>
          <el-menu-item index="/merchant/orders">订单列表</el-menu-item>
          <el-menu-item index="/merchant/orders/after-sales">售后处理</el-menu-item>
        </el-sub-menu>
        
        <el-sub-menu index="shop">
          <template #title>
            <el-icon><Shop /></el-icon>
            <span>店铺管理</span>
          </template>
          <el-menu-item index="/merchant/shop/info">店铺信息</el-menu-item>
          <el-menu-item index="/merchant/shop/decoration">店铺装修</el-menu-item>
        </el-sub-menu>
        
        <el-sub-menu index="marketing">
          <template #title>
            <el-icon><Present /></el-icon>
            <span>营销工具</span>
          </template>
          <el-menu-item index="/merchant/marketing">营销概览</el-menu-item>
          <el-menu-item index="/merchant/banner/list">轮播图投流</el-menu-item>
        </el-sub-menu>
        
        <el-menu-item index="/merchant/analytics">
          <el-icon><TrendCharts /></el-icon>
          <template #title>数据分析</template>
        </el-menu-item>
        
        <el-menu-item index="/merchant/finance">
          <el-icon><Money /></el-icon>
          <template #title>财务管理</template>
        </el-menu-item>
        
        <el-menu-item index="/merchant/settings">
          <el-icon><Setting /></el-icon>
          <template #title>设置中心</template>
        </el-menu-item>
      </el-menu>
    </el-aside>
    
    <!-- 主内容区域 -->
    <el-container class="merchant-main">
      <!-- 顶部导航栏 -->
      <el-header class="merchant-header">
        <div class="header-left">
          <el-button
            type="text"
            @click="toggleCollapse"
            class="collapse-btn"
          >
            <el-icon><Fold v-if="!isCollapse" /><Expand v-else /></el-icon>
          </el-button>
          
          <!-- 面包屑导航 -->
          <el-breadcrumb separator="/" class="breadcrumb">
            <el-breadcrumb-item
              v-for="item in breadcrumbList"
              :key="item.path"
              :to="item.path"
            >
              {{ item.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="header-right">
          <!-- 消息通知 -->
          <el-tooltip content="消息通知" placement="bottom">
            <el-badge :value="notificationCount" :hidden="notificationCount === 0">
              <el-button type="text" class="header-btn">
                <el-icon><Bell /></el-icon>
              </el-button>
            </el-badge>
          </el-tooltip>
          
          <!-- 全屏按钮 -->
          <el-tooltip content="全屏" placement="bottom">
            <el-button type="text" @click="toggleFullscreen" class="header-btn">
              <el-icon><FullScreen /></el-icon>
            </el-button>
          </el-tooltip>
          
          <!-- 用户信息 -->
          <el-dropdown @command="handleCommand" class="user-dropdown">
            <span class="user-info">
              <el-avatar :size="32" :src="userStore.userInfo?.avatar || userStore.userInfo?.logo" />
              <span class="username">{{ displayName }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人资料</el-dropdown-item>
                <el-dropdown-item command="shop">店铺设置</el-dropdown-item>
                <el-dropdown-item command="password">修改密码</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <!-- 主要内容区域 -->
      <el-main class="merchant-content">
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  DataBoard,
  Goods,
  Document,
  Shop,
  Present,
  TrendCharts,
  Money,
  Setting,
  Fold,
  Expand,
  Bell,
  FullScreen,
  ArrowDown
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 侧边栏折叠状态
const isCollapse = ref(false)

// 消息通知数量
const notificationCount = ref(0)

// 当前激活的菜单
const activeMenu = computed(() => route.path)

// 显示名称：优先显示店铺名称，其次昵称，最后用户名
const displayName = computed(() => {
  const info = userStore.userInfo
  return info.shopName || info.nickname || info.username || '商家'
})

// 面包屑导航
const breadcrumbList = computed(() => {
  const matched = route.matched.filter(item => item.meta && item.meta.title)
  const breadcrumbs = []
  
  // 添加首页
  breadcrumbs.push({
    path: '/merchant/dashboard',
    title: '首页'
  })
  
  // 添加当前路由的面包屑
  matched.forEach(item => {
    if (item.path !== '/merchant' && item.meta.title !== '首页') {
      breadcrumbs.push({
        path: item.path,
        title: item.meta.title
      })
    }
  })
  
  return breadcrumbs
})

// 切换侧边栏折叠状态
const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

// 切换全屏
const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
  } else {
    document.exitFullscreen()
  }
}

// 处理用户下拉菜单命令
const handleCommand = async (command) => {
  switch (command) {
    case 'profile':
      // 跳转到个人资料页面
      break
    case 'shop':
      router.push('/merchant/shop/info')
      break
    case 'password':
      // 跳转到修改密码页面
      break
    case 'logout':
      try {
        await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        await userStore.logout()
        ElMessage.success('退出登录成功')
        router.push('/merchant/login')
      } catch (error) {
        // 用户取消操作
      }
      break
  }
}

// 监听路由变化，更新页面标题
watch(
  () => route.meta.title,
  (title) => {
    if (title) {
      document.title = `${title} - 商家中心`
    }
  },
  { immediate: true }
)
</script>

<style scoped>
.merchant-layout {
  height: 100vh;
  display: flex;
}

.merchant-sidebar {
  background-color: #001529;
  transition: width 0.3s;
  overflow: hidden;
}

.logo-container {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 16px;
  border-bottom: 1px solid #1f1f1f;
}

.logo {
  height: 32px;
  margin-right: 12px;
}

.logo-mini {
  height: 32px;
}

.title {
  color: #ffffff;
  font-size: 18px;
  font-weight: 600;
  margin: 0;
}

.merchant-menu {
  border: none;
  height: calc(100vh - 64px);
  overflow-y: auto;
}

.merchant-menu:not(.el-menu--collapse) {
  width: 240px;
}

.merchant-main {
  flex: 1;
  background-color: #f0f2f5;
}

.merchant-header {
  background-color: #ffffff;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}

.header-left {
  display: flex;
  align-items: center;
}

.collapse-btn {
  margin-right: 16px;
  font-size: 18px;
  color: #666;
}

.breadcrumb {
  font-size: 14px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-btn {
  font-size: 16px;
  color: #666;
}

.user-dropdown {
  cursor: pointer;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
}

.username {
  font-size: 14px;
}

.merchant-content {
  padding: 24px;
  overflow-y: auto;
}

/* 滚动条样式 */
.merchant-menu::-webkit-scrollbar {
  width: 6px;
}

.merchant-menu::-webkit-scrollbar-track {
  background: #001529;
}

.merchant-menu::-webkit-scrollbar-thumb {
  background: #52c41a;
  border-radius: 3px;
}

.merchant-content::-webkit-scrollbar {
  width: 8px;
}

.merchant-content::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.merchant-content::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

.merchant-content::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>