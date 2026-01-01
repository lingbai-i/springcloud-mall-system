<!--
 * @Author: lingbai
 * @Date: 2025-01-01
 * @Description: 用户端顶部导航栏组件 - 固定在所有用户端页面显示
-->
<template>
  <!-- 顶部导航栏 -->
  <div class="top-bar">
    <div class="container">
      <div class="top-links">
        <div class="top-left-section">
          <div class="time-display">
            <el-icon class="time-icon"><Clock /></el-icon>
            <span>{{ currentTime }}</span>
          </div>
          <div class="welcome-area">
            <template v-if="!isLoggedIn">
              您好，欢迎来到百物语！
            </template>
            <template v-else>
              欢迎 
              <div
                class="welcome-dropdown"
                @mouseenter="showUserPopover"
                @mouseleave="hideUserPopover"
              >
                <span class="user-nickname">
                  {{ displayUserName }}
                  <el-icon class="arrow-icon" :class="{ 'is-active': logoutVisible }"><ArrowDown /></el-icon>
                </span>
                <transition name="fade-pure">
                  <div v-if="logoutVisible" class="user-card-popover">
                    <div class="user-info-section">
                      <div class="avatar-wrapper">
                        <img :src="userInfo?.avatar || defaultAvatar" alt="用户头像" class="user-avatar" />
                        <div class="vip-tag" v-if="userInfo?.isVip">88VIP</div>
                      </div>
                      <div class="user-detail-info">
                        <div class="nickname-row">
                          <span class="popover-nickname">{{ displayUserName }}</span>
                        </div>
                        <div class="action-links">
                          <a @click="goToProfile" class="action-link">账号管理</a>
                          <span class="link-divider">|</span>
                          <a @click="handleLogout" class="action-link logout-link">退出</a>
                        </div>
                      </div>
                    </div>
                  </div>
                </transition>
              </div>
              来到百物语！
            </template>
          </div>
        </div>
        <div class="user-links">
          <template v-if="!isLoggedIn">
            <a href="#" @click.prevent="handleLogin">登录</a>
          </template>
          <template v-else>
            <a href="#" @click.prevent="goToOrders">我的订单</a>
            <a href="#" @click.prevent="goToProfile">个人中心</a>
          </template>
          <div class="cart-link" @click="goToCart">
            <img src="/主题/购物车.png" class="icon-sm" alt="购物车" />
            <span>购物车</span>
            <el-badge :value="cartCount" class="cart-badge" v-if="cartCount > 0 && isLoggedIn" />
          </div>
          <div 
            class="seller-center-dropdown"
            @mouseenter="showSellerPopover"
            @mouseleave="hideSellerPopover"
          >
            <div class="seller-link" @click="goToSellerLogin">
              <span>卖家中心</span>
              <el-icon class="arrow-icon" :class="{ 'is-active': sellerVisible }"><ArrowDown /></el-icon>
            </div>
            <transition name="fade-pure">
              <div v-if="sellerVisible" class="seller-popover">
                <div class="seller-menu">
                  <a class="seller-menu-item" @click.stop="goToSellerRegister">开店入驻</a>
                  <a class="seller-menu-item">卖家服务市场</a>
                  <a class="seller-menu-item">卖家培训中心</a>
                  <a class="seller-menu-item">电商学习中心</a>
                </div>
              </div>
            </transition>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { logout as apiLogout } from '@/api/auth'
import { getCartCount } from '@/api/cart'
import { ArrowDown, Clock } from '@element-plus/icons-vue'
import serverTimeManager from '@/utils/serverTime'

const router = useRouter()
const userStore = useUserStore()

// Emits
const emit = defineEmits(['show-login-modal'])

// 时间显示（使用服务器时间）
const currentTime = ref('')
let timeTimer = null

// 格式化时间（使用服务器时间）
const formatTime = () => {
  return serverTimeManager.format('YYYY-MM-DD WW HH:mm:ss')
}

// 更新时间
const updateTime = () => {
  currentTime.value = formatTime()
}

// State
const isLoggedIn = computed(() => userStore.isLoggedIn)
const cartCount = ref(0)
const logoutVisible = ref(false)
const sellerVisible = ref(false)
const userInfo = computed(() => userStore.userInfo)
const defaultAvatar = '/images/default-avatar.png'

// 下拉菜单控制逻辑
let userHoverTimer = null
let sellerHoverTimer = null

const showUserPopover = () => {
  if (userHoverTimer) clearTimeout(userHoverTimer)
  logoutVisible.value = true
}

const hideUserPopover = () => {
  if (userHoverTimer) clearTimeout(userHoverTimer)
  userHoverTimer = setTimeout(() => {
    logoutVisible.value = false
  }, 150)
}

const showSellerPopover = () => {
  if (sellerHoverTimer) clearTimeout(sellerHoverTimer)
  sellerVisible.value = true
}

const hideSellerPopover = () => {
  if (sellerHoverTimer) clearTimeout(sellerHoverTimer)
  sellerHoverTimer = setTimeout(() => {
    sellerVisible.value = false
  }, 150)
}

const displayUserName = computed(() => {
  const info = userStore.userInfo
  return info?.nickname || info?.username || '用户'
})

const handleLogin = () => {
  emit('show-login-modal')
}

const handleLogout = async () => {
  try {
    await apiLogout()
  } catch (e) {
    console.error(e)
  }
  userStore.userLogout()
  ElMessage.success('您已退出登录')
  router.replace('/home')
}

const goToCart = () => {
  if (!isLoggedIn.value) {
    emit('show-login-modal')
    return
  }
  router.push('/user/cart')
}

const goToProfile = () => {
  if (!isLoggedIn.value) {
    emit('show-login-modal')
    return
  }
  router.push('/user/profile')
}

const goToOrders = () => {
  if (!isLoggedIn.value) {
    emit('show-login-modal')
    return
  }
  router.push('/user/orders')
}

const goToSellerLogin = () => {
  router.push('/merchant/login')
}

const goToSellerRegister = () => {
  router.push('/merchant/register')
}

const loadCartCount = async () => {
  if (!userStore.isLoggedIn || !userStore.userInfo?.id) return
  try {
    const res = await getCartCount(userStore.userInfo.id)
    if (res?.code === 200) cartCount.value = res.data
  } catch (e) {
    console.warn('Cart error', e)
  }
}

onMounted(() => {
  // 初始化时间显示
  updateTime()
  timeTimer = setInterval(updateTime, 1000)
  
  if (isLoggedIn.value) {
    loadCartCount()
  }
})

onUnmounted(() => {
  // 清理时间定时器
  if (timeTimer) {
    clearInterval(timeTimer)
  }
})
</script>

<style scoped>
/* 顶部导航栏 */
.top-bar {
  background-color: transparent;
  color: #2c3e50;
  font-size: 13px;
  padding: 4px 0;
  position: relative;
  z-index: 2000;
}

.container {
  max-width: none;
  width: 65%;
  min-width: 1200px;
  margin: 0 auto;
  padding: 0 15px;
  position: relative;
}

.top-links {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.top-left-section {
  display: flex;
  align-items: center;
}

/* 时间显示 */
.time-display {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #2c3e50;
  font-size: 13px;
  font-weight: 500;
  padding-right: 20px;
  border-right: 1px solid #e0e0e0;
  margin-right: 15px;
}

.time-icon {
  font-size: 14px;
  color: #409eff;
}

.welcome-area {
  color: #2c3e50;
  display: flex;
  align-items: center;
}

.user-nickname {
  color: #2c3e50;
  font-weight: 500;
  margin: 0 2px;
}

.user-links {
  display: flex;
  align-items: center;
  gap: 15px;
}

.user-links a {
  color: #2c3e50;
  text-decoration: none;
  transition: color 0.2s;
}

.user-links a:hover {
  color: #5d7e68;
  text-decoration: underline;
}

/* 用户下拉菜单 */
.welcome-dropdown {
  position: relative;
  display: inline-block;
  z-index: 1002;
}

/* 用户卡片弹出层 */
.user-card-popover {
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  width: 220px;
  background: #e8f5e9;
  border-radius: 10px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
  padding: 12px;
  margin-top: 10px;
  z-index: 2001;
  border: 1px solid #c8e6c9;
  backdrop-filter: blur(8px);
  color: #000;
}

.user-card-popover::before {
  content: '';
  position: absolute;
  top: -12px;
  left: 0;
  right: 0;
  height: 12px;
  background: transparent;
}

.user-info-section {
  display: flex;
  gap: 10px;
}

.avatar-wrapper {
  position: relative;
  width: 44px;
  height: 44px;
}

.user-avatar {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
  border: 1.5px solid #fff;
  box-shadow: 0 1px 4px rgba(0,0,0,0.1);
}

.vip-tag {
  position: absolute;
  bottom: -3px;
  left: 50%;
  transform: translateX(-50%);
  background: #e6c17a;
  color: #333;
  font-size: 8px;
  padding: 0px 4px;
  border-radius: 6px;
  white-space: nowrap;
  font-weight: bold;
}

.user-detail-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 2px;
}

.nickname-row {
  display: flex;
  align-items: center;
  gap: 6px;
}

.popover-nickname {
  color: #333;
  font-size: 14px;
  font-weight: 600;
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.action-links {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
}

.action-link {
  color: #666;
  text-decoration: none;
  cursor: pointer;
  transition: color 0.2s;
}

.action-link:hover {
  color: var(--theme-primary);
}

.logout-link:hover {
  color: #ff4d4f;
}

.link-divider {
  color: #ddd;
  font-size: 12px;
}

.icon-sm {
  width: 16px;
  height: 16px;
  vertical-align: middle;
  margin-right: 4px;
  filter: none;
}

.cart-link {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  background: transparent;
  transition: opacity 0.3s;
}

.cart-link:hover {
  opacity: 0.8;
}

.seller-center-dropdown {
  position: relative;
  height: 100%;
  display: flex;
  align-items: center;
}

.seller-link {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: all 0.3s;
  color: #2c3e50;
  font-weight: 500;
}

.seller-link:hover {
  background: rgba(255, 255, 255, 0.2);
}

.seller-popover {
  position: absolute;
  top: 100%;
  right: 0;
  width: 150px;
  background: #e8f5e9;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  padding: 8px 0;
  z-index: 1000;
  margin-top: 5px;
  backdrop-filter: blur(8px);
  border: 1px solid #c8e6c9;
  color: #000 !important;
}

.seller-popover::before {
  content: '';
  position: absolute;
  top: -8px;
  left: 0;
  right: 0;
  height: 8px;
  background: transparent;
}

.seller-menu {
  display: flex;
  flex-direction: column;
}

.seller-menu-item {
  padding: 10px 20px;
  font-size: 13px;
  color: #000 !important;
  text-decoration: none;
  transition: all 0.2s;
  cursor: pointer;
  font-weight: 500;
}

.seller-menu-item:hover {
  background: rgba(93, 126, 104, 0.1);
  color: var(--theme-primary);
}

.arrow-icon {
  margin-left: 4px;
  transition: transform 0.3s;
  font-size: 12px;
}

.arrow-icon.is-active {
  transform: rotate(180deg);
}

/* 强制垂直淡入动画 */
.fade-pure-enter-active,
.fade-pure-leave-active {
  transition: opacity 0.25s ease-out, transform 0.25s ease-out !important;
}

.fade-pure-enter-from,
.fade-pure-leave-to {
  opacity: 0 !important;
  transform: translateX(-50%) translateY(15px) !important;
}
</style>

