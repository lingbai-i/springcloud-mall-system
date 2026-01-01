<template>
  <div class="admin-layout">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '240px'" class="admin-sidebar">
      <div class="logo-container">
        <!-- 修复：使用正确的商标图片路径 -->
        <img src="/商标png.png" alt="Logo" :class="isCollapse ? 'logo-mini' : 'logo'" />
        <h1 v-if="!isCollapse" class="title">管理后台</h1>
      </div>
      
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :unique-opened="true"
        class="admin-menu"
        router
      >
        <el-menu-item index="/admin/dashboard">
          <el-icon><DataBoard /></el-icon>
          <template #title>仪表板</template>
        </el-menu-item>
        
        <el-sub-menu index="users">
          <template #title>
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </template>
          <el-menu-item index="/admin/users">用户列表</el-menu-item>
        </el-sub-menu>
        
        <el-sub-menu index="merchants">
          <template #title>
            <el-icon><Shop /></el-icon>
            <span>商家管理</span>
          </template>
          <el-menu-item index="/admin/merchants">商家列表</el-menu-item>
          <el-menu-item index="/admin/merchants/applications">商家审核</el-menu-item>
        </el-sub-menu>
        
        <el-sub-menu index="products">
          <template #title>
            <el-icon><Goods /></el-icon>
            <span>商品管理</span>
          </template>
          <el-menu-item index="/admin/products">商品列表</el-menu-item>
          <el-menu-item index="/admin/products/audit">商品审核</el-menu-item>
          <el-menu-item index="/admin/products/categories">分类管理</el-menu-item>
        </el-sub-menu>
        
        <el-sub-menu index="orders">
          <template #title>
            <el-icon><Document /></el-icon>
            <span>订单管理</span>
          </template>
          <el-menu-item index="/admin/orders">订单列表</el-menu-item>
          <el-menu-item index="/admin/orders/refunds">退款处理</el-menu-item>
        </el-sub-menu>
        
        <el-sub-menu index="banner">
          <template #title>
            <el-icon><PictureFilled /></el-icon>
            <span>轮播图管理</span>
          </template>
          <el-menu-item index="/admin/banner/review">轮播图审核</el-menu-item>
        </el-sub-menu>
        
        <el-menu-item index="/admin/statistics">
          <el-icon><TrendCharts /></el-icon>
          <template #title>数据统计</template>
        </el-menu-item>
        
        <el-menu-item index="/admin/settings">
          <el-icon><Setting /></el-icon>
          <template #title>系统设置</template>
        </el-menu-item>
      </el-menu>
    </el-aside>
    
    <!-- 主内容区域 -->
    <el-container class="admin-main">
      <!-- 顶部导航栏 -->
      <el-header class="admin-header">
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
          <!-- 全屏按钮 -->
          <el-tooltip content="全屏" placement="bottom">
            <el-button type="text" @click="toggleFullscreen" class="header-btn">
              <el-icon><FullScreen /></el-icon>
            </el-button>
          </el-tooltip>
          
          <!-- 用户信息 -->
          <el-dropdown @command="handleCommand" class="user-dropdown">
            <span class="user-info">
              <el-avatar :size="32" :src="userStore.avatar" />
              <span class="username">{{ userStore.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人资料</el-dropdown-item>
                <el-dropdown-item command="password">修改密码</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <!-- 主要内容区域 -->
      <el-main class="admin-content">
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
  User,
  Shop,
  Goods,
  Document,
  TrendCharts,
  Setting,
  Fold,
  Expand,
  FullScreen,
  ArrowDown,
  PictureFilled
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 侧边栏折叠状态
const isCollapse = ref(false)

// 当前激活的菜单
const activeMenu = computed(() => route.path)

// 面包屑导航
const breadcrumbList = computed(() => {
  const matched = route.matched.filter(item => item.meta && item.meta.title)
  const breadcrumbs = []
  
  // 添加首页
  breadcrumbs.push({
    path: '/admin/dashboard',
    title: '首页'
  })
  
  // 添加当前路由的面包屑
  matched.forEach(item => {
    if (item.path !== '/admin' && item.meta.title !== '首页') {
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
        router.push('/admin/login')
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
      document.title = `${title} - 管理后台`
    }
  },
  { immediate: true }
)
</script>

<style scoped>
.admin-layout {
  height: 100vh;
  display: flex;
  background-color: #f4f7f5;
}

.admin-sidebar {
  background-color: #e8eee9; /* 中度浅绿色，与商家后台一致 */
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
  justify-content: center;
  padding: 0 16px;
  background-color: #dce4de; /* 稍微深一点的绿色背景，增加层次感 */
  border-bottom: 1px solid #d1dbd3;
}

.logo {
  height: 32px;
  margin-right: 12px;
}

.logo-mini {
  height: 32px;
}

.title {
  color: #3a5044;
  font-size: 18px;
  font-weight: 600;
  margin: 0;
  letter-spacing: 0.5px;
}

.admin-menu {
  border: none;
  height: calc(100vh - 64px);
  overflow-y: auto;
  background-color: transparent !important;
  padding: 12px 0;
}

:deep(.el-menu) {
  background-color: transparent !important;
  border: none;
}

:deep(.el-menu-item),
:deep(.el-sub-menu__title) {
  height: 50px;
  line-height: 50px;
  margin: 4px 12px;
  border-radius: 8px;
  color: #3a5044 !important;
  display: flex;
  align-items: center;
  transition: all 0.2s;
}

:deep(.el-menu-item .el-icon),
:deep(.el-sub-menu__title .el-icon) {
  margin-right: 12px;
  width: 20px;
  text-align: center;
  font-size: 18px;
  color: #3a5044 !important;
}

:deep(.el-menu-item:hover),
:deep(.el-sub-menu__title:hover) {
  background-color: rgba(58, 80, 68, 0.08) !important;
  color: #2c3e50 !important;
}

:deep(.el-menu-item.is-active) {
  background-color: #3a5044 !important; /* 选中项改为深色背景，形成鲜明对比 */
  color: #ffffff !important;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(58, 80, 68, 0.2);
}

:deep(.el-menu-item.is-active .el-icon) {
  color: #ffffff !important;
}

.admin-menu:not(.el-menu--collapse) {
  width: 240px;
}

.admin-main {
  flex: 1;
  background: linear-gradient(135deg, #f4f7f5 0%, #d1dbd3 100%) !important; /* 统一绿色渐变背景 */
  display: flex;
  flex-direction: column;
  position: relative;
}

.admin-header {
  background-color: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid #d1dbd3;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 64px;
  z-index: 1000;
}

.header-left {
  display: flex;
  align-items: center;
}

.collapse-btn {
  margin-right: 16px;
  font-size: 20px;
  color: #3a5044;
  cursor: pointer;
}

.collapse-btn:hover {
  color: #4caf50;
}

.breadcrumb {
  font-size: 14px;
}

:deep(.el-breadcrumb__inner) {
  color: #5c7c6a !important;
}

:deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  color: #3a5044 !important;
  font-weight: 600;
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
}

.user-dropdown {
  cursor: pointer;
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

.admin-content {
  padding: 24px;
  overflow-y: auto;
  flex: 1;
}

/* 组件样式统一优化 */
:deep(.el-card) {
  border: 1px solid rgba(209, 219, 211, 0.5) !important;
  background-color: rgba(255, 255, 255, 0.9) !important;
  backdrop-filter: blur(10px);
  box-shadow: 0 8px 32px rgba(58, 80, 68, 0.08) !important;
  border-radius: 12px;
}

:deep(.el-card__header) {
  border-bottom: 1px solid rgba(209, 219, 211, 0.5) !important;
  color: #3a5044 !important;
  font-weight: 600;
}

:deep(.el-button--primary) {
  background: linear-gradient(135deg, #4caf50 0%, #388e3c 100%) !important;
  border: none !important;
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.2);
}

:deep(.el-button--primary:hover) {
  opacity: 0.9;
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(76, 175, 80, 0.3);
}

:deep(.el-table) {
  background-color: transparent !important;
  color: #3a5044 !important;
}

:deep(.el-table th.el-table__cell) {
  background-color: rgba(232, 238, 233, 0.5) !important;
  color: #3a5044 !important;
  border-bottom: 1px solid #d1dbd3 !important;
}

:deep(.el-table td.el-table__cell) {
  border-bottom: 1px solid rgba(209, 219, 211, 0.3) !important;
}

/* 滚动条样式 */
.admin-menu::-webkit-scrollbar,
.admin-content::-webkit-scrollbar {
  width: 6px;
}

.admin-menu::-webkit-scrollbar-track,
.admin-content::-webkit-scrollbar-track {
  background: transparent;
}

.admin-menu::-webkit-scrollbar-thumb {
  background: rgba(58, 80, 68, 0.1);
  border-radius: 3px;
}

.admin-menu::-webkit-scrollbar-thumb:hover {
  background: rgba(58, 80, 68, 0.2);
}

.admin-content::-webkit-scrollbar-thumb {
  background: rgba(58, 80, 68, 0.1);
  border-radius: 3px;
}

.admin-content::-webkit-scrollbar-thumb:hover {
  background: rgba(58, 80, 68, 0.2);
}
</style>