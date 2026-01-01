<template>
  <div class="merchant-layout">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '240px'" class="merchant-sidebar">
      <div class="logo-container">
        <img src="/商标png.png" alt="Logo" :class="isCollapse ? 'logo-mini' : 'logo'" />
        <h1 v-if="!isCollapse" class="title">商家中心</h1>
      </div>
      
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :unique-opened="true"
        class="merchant-menu"
        background-color="#e8eee9"
        text-color="#3a5044"
        active-text-color="#2c3e34"
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
              <el-avatar :size="32" :src="userStore.userInfo?.avatar || userStore.userInfo?.logo || userStore.userInfo?.shopLogo">
                <el-icon><Shop /></el-icon>
              </el-avatar>
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
  background-color: #e8eee9; /* 中度浅绿色，比之前更沉稳，不刺眼 */
  color: #3a5044;
  height: 100vh;
  transition: width 0.3s;
  box-shadow: 2px 0 12px rgba(58, 80, 68, 0.12);
  z-index: 1001;
  overflow: hidden;
  border-right: 1px solid #d1dbd3;
}

.logo-container {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  background-color: #dce4de; /* 稍深一点的顶栏，增加稳重感 */
  overflow: hidden;
  gap: 12px;
  border-bottom: 1px solid #c4cfc6;
}

.logo {
  height: 40px;
  width: auto;
  object-fit: contain;
}

.logo-mini {
  height: 32px;
  width: auto;
  margin: 0 auto;
}

.title {
  color: #3a5044;
  font-size: 18px;
  margin: 0;
  font-weight: 600;
  white-space: nowrap;
  font-family: "PingFang SC", "Microsoft YaHei", sans-serif;
  letter-spacing: 1px;
}

.merchant-menu {
  border-right: none;
}

/* 深度选择器修改 Element Plus 菜单样式 */
:deep(.el-menu) {
  border-right: none;
}

:deep(.el-menu-item),
:deep(.el-sub-menu__title) {
  height: 50px;
  line-height: 50px;
  margin: 4px 12px;
  border-radius: 8px;
  display: flex;
  align-items: center;
}

/* 统一图标与文字的对齐 */
:deep(.el-menu-item .el-icon),
:deep(.el-sub-menu__title .el-icon) {
  margin-right: 12px;
  width: 20px;
  text-align: center;
  vertical-align: middle;
}

:deep(.el-menu-item.is-active) {
  background-color: #3a5044 !important; /* 选中项改为深色背景，形成鲜明对比 */
  color: #ffffff !important;
  font-weight: 600;
  border-right: none;
  box-shadow: 0 4px 12px rgba(58, 80, 68, 0.2);
}

:deep(.el-menu-item:hover),
:deep(.el-sub-menu__title:hover) {
  background-color: rgba(58, 80, 68, 0.08) !important;
  color: #3a5044 !important;
}

:deep(.el-sub-menu.is-active .el-sub-menu__title) {
  color: #3a5044 !important;
  font-weight: 600;
}

.merchant-menu:not(.el-menu--collapse) {
  width: 240px;
}

.merchant-main {
  flex: 1;
  background: linear-gradient(135deg, #f4f7f5 0%, #d1dbd3 100%) !important; /* 加深一点点终点色，让渐变更明显 */
  display: flex;
  flex-direction: column;
  position: relative;
}

.merchant-header {
  background-color: rgba(255, 255, 255, 0.8); /* 半透明背景，透出下方渐变 */
  backdrop-filter: blur(10px); /* 磨砂玻璃效果 */
  border-bottom: 1px solid rgba(209, 219, 211, 0.5);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 1px 4px rgba(58, 80, 68, 0.05);
  z-index: 1000;
}

.header-left {
  display: flex;
  align-items: center;
}

.collapse-btn {
  margin-right: 16px;
  font-size: 18px;
  color: #3a5044;
  cursor: pointer;
  transition: color 0.3s;
}

.collapse-btn:hover {
  color: #5a7a68;
}

.breadcrumb {
  font-size: 14px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.header-btn {
  font-size: 18px;
  color: #3a5044;
  cursor: pointer;
  transition: transform 0.2s;
}

.header-btn:hover {
  transform: scale(1.1);
}

.user-dropdown {
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background 0.3s;
}

.user-dropdown:hover {
  background: #f0f4f1;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #3a5044;
}

.username {
  font-size: 14px;
  font-weight: 500;
}

.merchant-content {
  padding: 24px;
  overflow-y: auto;
  flex: 1;
  background-color: transparent !important; /* 强制透明，确保透出父级的渐变 */
}

/* 统一修改主区域内卡片的样式，使其适应渐变背景 */
:deep(.el-card) {
  border: 1px solid rgba(255, 255, 255, 0.6) !important;
  background-color: rgba(255, 255, 255, 0.7) !important;
  backdrop-filter: blur(8px);
  box-shadow: 0 4px 12px rgba(58, 80, 68, 0.04) !important;
  border-radius: 12px;
  transition: all 0.3s ease;
}

:deep(.el-card:hover) {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(58, 80, 68, 0.08) !important;
  background-color: rgba(255, 255, 255, 0.9) !important;
}

/* 滚动条样式 */
.merchant-menu::-webkit-scrollbar {
  width: 4px;
}

.merchant-menu::-webkit-scrollbar-track {
    background: #e8eee9;
  }
  
  .merchant-menu::-webkit-scrollbar-thumb {
    background: #c4cfc6;
    border-radius: 2px;
  }

.merchant-content::-webkit-scrollbar {
  width: 6px;
}

.merchant-content::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.merchant-content::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.merchant-content::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>