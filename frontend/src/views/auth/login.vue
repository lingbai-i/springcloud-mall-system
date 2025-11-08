<template>
  <div class="login-container">
    <div class="login-box">
      <!-- Logo和标题 -->
      <div class="login-header">
        <div class="logo">
          <img src="/logo.png" alt="商城Logo" class="logo-img">
        </div>
        <h2 class="title">欢迎登录</h2>
        <p class="subtitle">在线商城 - 您的购物首选</p>
      </div>

      <!-- 登录表单 -->
      <el-form 
        :model="loginForm" 
        :rules="loginRules" 
        ref="loginFormRef"
        class="login-form"
        @submit.prevent="handleLogin">
        
        <!-- 登录方式切换 -->
        <el-tabs v-model="loginType" class="login-tabs">
          <el-tab-pane label="密码登录" name="password">
            <el-form-item prop="username">
              <el-input
                v-model="loginForm.username"
                placeholder="请输入用户名/手机号/邮箱"
                size="large"
                clearable>
              </el-input>
            </el-form-item>
            
            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                size="large"
                show-password
                clearable
                @keyup.enter="handleLogin">
              </el-input>
            </el-form-item>
          </el-tab-pane>
          
          <el-tab-pane label="验证码登录" name="sms">
            <el-form-item prop="phone">
              <el-input
                v-model="loginForm.phone"
                placeholder="请输入手机号"
                size="large"
                clearable>
              </el-input>
            </el-form-item>
            
            <el-form-item prop="smsCode">
              <div class="sms-input">
                <el-input
                  v-model="loginForm.smsCode"
                  placeholder="请输入验证码"
                  size="large"
                  clearable
                  @keyup.enter="handleLogin">
                </el-input>
                <el-button 
                  :disabled="smsCountdown > 0"
                  @click="sendSmsCodeHandler"
                  class="sms-btn">
                  {{ smsCountdown > 0 ? `${smsCountdown}s后重发` : '获取验证码' }}
                </el-button>
              </div>
            </el-form-item>
          </el-tab-pane>
        </el-tabs>

        <!-- 记住我和忘记密码 -->
        <div class="login-options">
          <el-checkbox v-model="loginForm.rememberMe">记住我</el-checkbox>
          <el-button type="text" @click="handleForgotPassword">忘记密码？</el-button>
        </div>

        <!-- 登录按钮 -->
        <el-form-item>
          <el-button 
            type="primary" 
            size="large"
            :loading="loading"
            @click="handleLogin"
            class="login-btn">
            登录
          </el-button>
        </el-form-item>

        <!-- 第三方登录 -->
        <div class="third-party-login">
          <div class="divider">
            <span>其他登录方式</span>
          </div>
          <div class="third-party-buttons">
            <el-button circle @click="handleThirdPartyLogin('wechat')">
              微信
            </el-button>
            <el-button circle @click="handleThirdPartyLogin('qq')">
              QQ
            </el-button>
            <el-button circle @click="handleThirdPartyLogin('weibo')">
              微博
            </el-button>
          </div>
        </div>

        <!-- 注册链接 -->
        <div class="register-link">
          <span>还没有账号？</span>
          <el-button type="text" @click="goToRegister">立即注册</el-button>
        </div>
      </el-form>
    </div>

    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { sendSmsCode } from '@/api/auth'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 响应式数据
const loginType = ref('password')
const loading = ref(false)
const smsCountdown = ref(0)
const loginFormRef = ref()

// 登录表单数据
const loginForm = reactive({
  username: '',
  password: '',
  phone: '',
  smsCode: '',
  rememberMe: false
})

// 表单验证规则 - 根据登录类型动态设置
const loginRules = computed(() => {
  if (loginType.value === 'password') {
    return {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
      ]
    }
  } else {
    return {
      phone: [
        { required: true, message: '请输入手机号', trigger: 'blur' },
        { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
      ],
      smsCode: [
        { required: true, message: '请输入验证码', trigger: 'blur' },
        { len: 6, message: '验证码为6位数字', trigger: 'blur' }
      ]
    }
  }
})

let smsTimer = null

// 方法
const handleLogin = async () => {
  try {
    await loginFormRef.value.validate()
    loading.value = true
    
    let loginData = {}
    
    if (loginType.value === 'password') {
      loginData = {
        username: loginForm.username,
        password: loginForm.password,
        rememberMe: loginForm.rememberMe
      }
    } else {
      loginData = {
        phone: loginForm.phone,
        smsCode: loginForm.smsCode,
        rememberMe: loginForm.rememberMe
      }
    }
    
    // 调用用户store的登录方法
    const success = await userStore.login(loginData)
    
    if (success) {
      ElMessage.success('登录成功')
      
      // 获取重定向地址
      const redirect = route.query.redirect || '/'
      router.push(redirect)
    } else {
      ElMessage.error('登录失败,请检查用户名和密码')
    }
    
  } catch (error) {
    console.error('登录失败:', error)
    // 表单验证失败时不显示错误消息,Element Plus会自动显示验证错误
    if (error?.message && !error.message.includes('validation')) {
      ElMessage.error(error.message || '登录失败,请稍后重试')
    }
  } finally {
    loading.value = false
  }
}

const sendSmsCodeHandler = async () => {
  if (!loginForm.phone) {
    ElMessage.warning('请先输入手机号')
    return
  }
  
  if (!/^1[3-9]\d{9}$/.test(loginForm.phone)) {
    ElMessage.error('请输入正确的手机号')
    return
  }
  
  try {
    // 调用真实的发送验证码API
    console.log('发送验证码到:', loginForm.phone)
    
    await sendSmsCode(loginForm.phone, 'LOGIN')
    
    ElMessage.success('验证码已发送')
    
    // 开始倒计时
    smsCountdown.value = 60
    smsTimer = setInterval(() => {
      smsCountdown.value--
      if (smsCountdown.value <= 0) {
        clearInterval(smsTimer)
        smsTimer = null
      }
    }, 1000)
    
  } catch (error) {
    ElMessage.error('发送验证码失败')
  }
}

const handleForgotPassword = () => {
  ElMessage.info('请联系客服找回密码')
}

const handleThirdPartyLogin = (type) => {
  ElMessage.info(`${type}登录功能开发中`)
}

const goToRegister = () => {
  router.push('/register')
}

// 生命周期
onMounted(() => {
  // 如果已经登录，直接跳转
  if (userStore.isLoggedIn) {
    const redirect = route.query.redirect || '/'
    router.push(redirect)
  }
})

onUnmounted(() => {
  if (smsTimer) {
    clearInterval(smsTimer)
  }
})
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}

.login-box {
  width: 400px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  position: relative;
  z-index: 10;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.logo-img {
  width: 60px;
  height: 60px;
  margin-bottom: 15px;
}

.title {
  margin: 0 0 10px 0;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.subtitle {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.login-form {
  margin-top: 20px;
}

.login-tabs {
  margin-bottom: 20px;
}

.login-tabs :deep(.el-tabs__header) {
  margin: 0 0 20px 0;
}

.login-tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.sms-input {
  display: flex;
  gap: 10px;
}

.sms-input .el-input {
  flex: 1;
}

.sms-btn {
  white-space: nowrap;
  min-width: 100px;
}

.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.login-btn {
  width: 100%;
  height: 45px;
  font-size: 16px;
  font-weight: 600;
}

.third-party-login {
  margin-top: 30px;
}

.divider {
  text-align: center;
  margin-bottom: 20px;
  position: relative;
}

.divider::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background: #e4e7ed;
}

.divider span {
  background: rgba(255, 255, 255, 0.95);
  padding: 0 15px;
  color: #909399;
  font-size: 12px;
}

.third-party-buttons {
  display: flex;
  justify-content: center;
  gap: 15px;
}

.third-party-buttons .el-button {
  width: 45px;
  height: 45px;
  border-radius: 50%;
}

.icon {
  width: 20px;
  height: 20px;
  fill: currentColor;
}

.register-link {
  text-align: center;
  margin-top: 20px;
  color: #909399;
  font-size: 14px;
}

/* 背景装饰 */
.bg-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
}

.shape {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  animation: float 6s ease-in-out infinite;
}

.shape-1 {
  width: 200px;
  height: 200px;
  top: 10%;
  left: 10%;
  animation-delay: 0s;
}

.shape-2 {
  width: 150px;
  height: 150px;
  top: 60%;
  right: 10%;
  animation-delay: 2s;
}

.shape-3 {
  width: 100px;
  height: 100px;
  bottom: 20%;
  left: 20%;
  animation-delay: 4s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-20px);
  }
}

/* 响应式设计 */
@media (max-width: 480px) {
  .login-box {
    width: 90%;
    padding: 30px 20px;
  }
  
  .sms-input {
    flex-direction: column;
  }
  
  .sms-btn {
    min-width: auto;
  }
}
</style>