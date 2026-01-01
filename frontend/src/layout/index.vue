<!--
 * @Author: lingbai
 * @Date: 2025-01-01
 * @Description: 用户端统一布局组件 - 包含固定导航栏和底部
 * 更新日期：2025-01-01 - 集成固定导航栏
-->
<template>
  <div class="user-layout">
    <!-- 顶部导航栏 -->
    <UserNavbar @show-login-modal="showLoginModal = true" />
    
    <!-- 页面内容区域 -->
    <div class="main-content">
      <router-view />
    </div>
    
    <!-- 底部 -->
    <div class="site-footer">
      <div class="container">
        <div class="footer-links">
           <a href="#">关于我们</a>
           <a href="#">联系客服</a>
           <a href="#">商家入驻</a>
           <a href="#">营销中心</a>
        </div>
        <p class="copyright">&copy; 2025 百物语 版权所有</p>
      </div>
    </div>

    <!-- 悬浮工具栏 -->
    <div class="fixed-tool-bar">
      <div class="tool-item">
        <img src="/主题/消息.png" alt="消息" />
        <span>消息</span>
      </div>
      <div class="tool-item" @click="goToCart">
        <img src="/主题/购物车.png" alt="购物车" />
        <span>购物车</span>
      </div>
      <div class="tool-item research-item">
        <img src="/主题/用户调研.gif" alt="用户调研" class="research-gif" />
        <span>用户调研</span>
      </div>
      <div class="tool-item">
        <img src="/主题/官方客服.png" alt="客服" />
        <span>官方客服</span>
      </div>
      <div class="tool-item">
        <img src="/主题/反馈.png" alt="反馈" />
        <span>反馈</span>
      </div>
      <div class="tool-item back-top" @click="scrollToTop">
        <img src="/主题/回顶部.png" alt="回顶部" />
        <span>顶部</span>
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import UserNavbar from '@/components/UserNavbar.vue'
import LoginModal from '@/components/LoginModal.vue'
import serverTimeManager from '@/utils/serverTime'

const router = useRouter()
const userStore = useUserStore()
const showLoginModal = ref(false)

const goToCart = () => {
  if (!userStore.isLoggedIn) {
    showLoginModal.value = true
    return
  }
  router.push('/user/cart')
}

const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const handleLoginSuccess = async () => {
  ElMessage.success('登录成功')
}

onMounted(() => {
  // 启动服务器时间同步（如果还未同步）
  if (!serverTimeManager.isSynced()) {
    serverTimeManager.startAutoSync()
  }
})
</script>

<style scoped>
.user-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-image: url('/主题/背景.png');
  background-size: cover;
  background-position: center top;
  background-attachment: fixed;
  background-repeat: no-repeat;
}

.main-content {
  flex: 1;
  width: 100%;
}

.container {
  max-width: none;
  width: 65%;
  min-width: 1200px;
  margin: 0 auto;
  padding: 0 15px;
}

/* 底部 */
.site-footer {
  background: rgba(58, 80, 68, 0.95);
  color: #e8f5e9;
  padding: 40px 0;
  text-align: center;
  margin-top: 40px;
}

.footer-links a {
  color: #e8f5e9;
  margin: 0 15px;
  text-decoration: none;
}

.footer-links a:hover {
  text-decoration: underline;
}

.copyright {
  margin-top: 20px;
  font-size: 12px;
  opacity: 0.7;
}

/* 悬浮工具栏 */
.fixed-tool-bar {
  position: fixed;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  flex-direction: column;
  background: white;
  border-radius: 8px 0 0 8px;
  box-shadow: -2px 0 12px rgba(0, 0, 0, 0.08);
  z-index: 2000;
  padding: 4px 0;
}

.tool-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px 4px;
  cursor: pointer;
  transition: all 0.2s;
  color: #666;
  width: 50px;
}

.tool-item:hover {
  background: #f8f8f8;
  color: var(--theme-primary);
}

.tool-item img {
  width: 22px;
  height: 22px;
  margin-bottom: 2px;
  object-fit: contain;
}

.tool-item span {
  font-size: 10px;
  line-height: 1.2;
  text-align: center;
}

.research-item {
  background: #fcfcfc;
  margin: 2px 0;
}

.research-gif {
  width: 26px !important;
  height: 26px !important;
}

.back-top {
  border-top: 1px solid #f5f5f5;
  margin-top: 2px;
  padding-top: 8px;
}
</style>
