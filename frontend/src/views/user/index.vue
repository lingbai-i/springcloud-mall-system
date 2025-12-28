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
              <el-button 
                type="text" 
                size="small"
                class="change-avatar-btn"
                @click="showAvatarUpload = true"
              >
                更换头像
              </el-button>
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
          </el-menu-item>
        </el-menu>
      </div>

      <!-- 右侧内容区域 -->
      <div class="content-area">
        <!-- 个人资料 -->
        <div v-show="activeMenu === 'profile'" class="content-panel">
          <ProfileView />
        </div>

        <!-- 账户安全 -->
        <div v-show="activeMenu === 'security'" class="content-panel">
          <SecurityView />
        </div>

        <!-- 我的订单 -->
        <div v-show="activeMenu === 'orders'" class="content-panel">
          <OrdersView />
        </div>

        <!-- 收货地址 -->
        <div v-show="activeMenu === 'addresses'" class="content-panel">
          <AddressesView />
        </div>

        <!-- 我的收藏 -->
        <div v-show="activeMenu === 'favorites'" class="content-panel">
          <FavoritesView />
        </div>

        <!-- 系统设置 -->
        <div v-show="activeMenu === 'settings'" class="content-panel">
          <SettingsView />
        </div>
      </div>
    </div>

    <!-- 头像上传对话框 -->
    <el-dialog
      v-model="showAvatarUpload"
      title="更换头像"
      width="400px"
      :before-close="handleAvatarDialogClose"
    >
      <el-upload
        class="avatar-uploader"
        action="#"
        :show-file-list="false"
        :before-upload="beforeAvatarUpload"
        :http-request="handleAvatarUpload"
        accept="image/*"
      >
        <img v-if="previewAvatar" :src="previewAvatar" class="avatar-preview" />
        <LocalIcon v-else name="tianjia" :size="32" class="avatar-uploader-icon" />
        <div class="upload-tip">
          <p>点击上传头像</p>
          <p class="tip-text">支持 JPG、PNG 格式，文件大小不超过 2MB</p>
        </div>
      </el-upload>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showAvatarUpload = false">取消</el-button>
          <el-button type="primary" @click="confirmAvatarUpload" :loading="uploading">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { HomeFilled } from '@element-plus/icons-vue'
import LocalIcon from '@/components/LocalIcon.vue'
import ProfileView from './profile.vue'
import SecurityView from './security.vue'
import OrdersView from './orders.vue'
import AddressesView from './addresses.vue'
import FavoritesView from './favorites.vue'
import SettingsView from './Settings.vue'
import { updateUserProfile } from '@/api/user'
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

// 响应式数据
const userInfo = computed(() => userStore.userInfo || {})
const showAvatarUpload = ref(false)
const previewAvatar = ref('')
const uploading = ref(false)

// 路由路径到菜单key的映射
const routeToMenuMap = {
  '/user/profile': 'profile',
  '/user/security': 'security',
  '/user/orders': 'orders',
  '/user/addresses': 'addresses',
  '/user/favorites': 'favorites',
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

/**
 * 头像上传前验证
 */
const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }

  // 预览图片
  const reader = new FileReader()
  reader.onload = (e) => {
    previewAvatar.value = e.target.result
  }
  reader.readAsDataURL(file)

  return false // 阻止自动上传
}

/**
 * 处理头像上传
 *
 * 修改日志：
 * V1.1 2025-11-09T20:51:23+08:00：保留占位实现，但实际上传在 confirmAvatarUpload 中走后端更新资料接口，保证与 /users/profile 一致。
 */
const handleAvatarUpload = async (options) => {
  // 这里暂时不实际上传，只是模拟
  console.log('上传文件:', options.file)
}

/**
 * 确认头像上传
 *
 * 修改日志：
 * V1.1 2025-11-09T20:51:23+08:00：接入后端 /users/profile 接口，提交 base64 头像数据至 avatar 字段；成功后刷新本地 store。
 * V1.2 2025-11-11：添加后端数据同步，确保头像更新后立即刷新用户信息。
 * @author lingbai
 * @returns {Promise<void>} 无返回值
 */
const confirmAvatarUpload = async () => {
  if (!previewAvatar.value) {
    ElMessage.warning('请先选择头像')
    return
  }

  try {
    uploading.value = true
    // 调用后端用户资料更新接口，仅更新 avatar 字段（后端会基于 token 识别用户）
    logger.info('开始调用后端更新头像接口 /users/profile')
    const resp = await updateUserProfile({ avatar: previewAvatar.value })
    if (resp && (resp.success === true || resp.code === 200)) {
      // 后端不返回最新用户对象，这里直接用本地预览值更新 store，保持 UI 同步
      await userStore.updateUserInfo({ avatar: previewAvatar.value })
      
      // 刷新用户信息以确保后端数据同步
      try {
        await userStore.fetchUserInfo()
        logger.info('头像更新成功，已同步到本地 store 并刷新后端数据')
      } catch (err) {
        logger.warn('刷新用户信息失败，但头像已更新', err)
      }
      
      ElMessage.success('头像更新成功')
      showAvatarUpload.value = false
      previewAvatar.value = ''
    } else {
      const message = (resp && resp.message) || '头像更新失败'
      logger.warn('后端返回失败，头像未更新', { message })
      ElMessage.error(message)
    }
  } catch (error) {
    console.error('头像上传失败:', error)
    ElMessage.error('头像上传失败')
  } finally {
    uploading.value = false
  }
}

/**
 * 头像对话框关闭处理
 */
const handleAvatarDialogClose = () => {
  previewAvatar.value = ''
  showAvatarUpload.value = false
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

.change-avatar-btn {
  font-size: 12px;
  color: #409EFF;
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

.avatar-uploader {
  display: flex;
  justify-content: center;
}

.avatar-uploader :deep(.el-upload) {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: 0.2s;
  width: 200px;
  height: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.avatar-uploader :deep(.el-upload:hover) {
  border-color: #409EFF;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  margin-bottom: 10px;
}

.avatar-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.upload-tip {
  text-align: center;
}

.upload-tip p {
  margin: 5px 0;
  color: #606266;
}

.tip-text {
  font-size: 12px;
  color: #909399;
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
