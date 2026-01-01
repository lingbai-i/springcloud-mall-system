<template>
  <div class="admin-login">
    <div class="login-container">
      <div class="login-header">
        <!-- 修复：使用正确的商标图片路径，统一品牌形象 -->
        <img src="/商标png.png" alt="Logo" class="logo" />
        <h1 class="title">管理员登录</h1>
        <p class="subtitle">秉承匠心 · 诚就百物</p>
      </div>
      
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        size="large"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入管理员账号"
            prefix-icon="User"
            clearable
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            show-password
            clearable
          />
        </el-form-item>
        
        <el-form-item prop="captcha" v-if="showCaptcha">
          <div class="captcha-container">
            <el-input
              v-model="loginForm.captcha"
              placeholder="请输入验证码"
              prefix-icon="Key"
              clearable
            />
            <div class="captcha-image" @click="refreshCaptcha">
              <img :src="captchaUrl" alt="验证码" />
              <span class="refresh-text">点击刷新</span>
            </div>
          </div>
        </el-form-item>
        
        <el-form-item>
          <div class="login-options">
            <el-checkbox v-model="loginForm.remember">记住我</el-checkbox>
            <el-link type="primary" @click="handleForgotPassword">忘记密码？</el-link>
          </div>
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登录' }}
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="login-footer">
        <p class="tips">
          <el-icon><InfoFilled /></el-icon>
          请使用管理员账号登录系统
        </p>
        <div class="links">
          <el-link href="/" target="_blank">返回首页</el-link>
          <el-divider direction="vertical" />
          <el-link href="/merchant/login">商家登录</el-link>
        </div>
      </div>
    </div>
    
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { InfoFilled } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 表单引用
const loginFormRef = ref()

// 加载状态
const loading = ref(false)

// 是否显示验证码
const showCaptcha = ref(false)

// 验证码URL
const captchaUrl = ref('')

// 登录表单数据
const loginForm = reactive({
  username: '',
  password: '',
  captcha: '',
  remember: false
})

// 表单验证规则
const loginRules = {
  username: [
    { required: true, message: '请输入管理员账号', trigger: 'blur' },
    { min: 3, max: 20, message: '账号长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  captcha: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 4, message: '验证码长度为 4 位', trigger: 'blur' }
  ]
}

// 处理登录
const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  try {
    await loginFormRef.value.validate()
    
    loading.value = true
    
    // 调用登录API
    const loginData = {
      username: loginForm.username,
      password: loginForm.password,
      captcha: loginForm.captcha,
      remember: loginForm.remember,
      type: 'admin' // 标识为管理员登录
    }
    
    try {
      await userStore.login(loginData)
      
      ElMessage.success('登录成功')
      
      // 获取重定向路径
      const redirect = route.query.redirect || '/admin/dashboard'
      router.push(redirect)
      
    } catch (error) {
      console.error('登录失败:', error)
      
      // 根据错误类型显示不同的提示
      if (error.code === 'INVALID_CAPTCHA') {
        ElMessage.error('验证码错误')
        refreshCaptcha()
      } else if (error.code === 'ACCOUNT_LOCKED') {
        ElMessage.error('账号已被锁定，请联系系统管理员')
      } else if (error.code === 'INVALID_CREDENTIALS') {
        ElMessage.error('用户名或密码错误')
        // 连续失败3次后显示验证码
        if (error.failCount >= 3) {
          showCaptcha.value = true
          refreshCaptcha()
        }
      } else {
        ElMessage.error(error.message || '登录失败，请稍后重试')
      }
    }
    
  } catch (error) {
    console.error('表单验证失败:', error)
  } finally {
    loading.value = false
  }
}

// 刷新验证码
const refreshCaptcha = () => {
  captchaUrl.value = `/api/admin/captcha?t=${Date.now()}`
}

// 处理忘记密码
const handleForgotPassword = async () => {
  try {
    const { value: email } = await ElMessageBox.prompt(
      '请输入您的邮箱地址，我们将发送重置密码的链接',
      '忘记密码',
      {
        confirmButtonText: '发送',
        cancelButtonText: '取消',
        inputPattern: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
        inputErrorMessage: '请输入有效的邮箱地址'
      }
    )
    
    // 发送重置密码邮件
    // await adminApi.sendResetPasswordEmail(email)
    
    ElMessage.success('重置密码链接已发送到您的邮箱')
    
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('发送失败，请稍后重试')
    }
  }
}

// 组件挂载时的初始化
onMounted(() => {
  // 如果已经登录且是管理员，直接跳转到仪表板
  if (userStore.isLoggedIn && userStore.isAdmin) {
    router.push('/admin/dashboard')
    return
  }
  
  // 检查是否需要显示验证码
  const failCount = localStorage.getItem('admin_login_fail_count') || 0
  if (failCount >= 3) {
    showCaptcha.value = true
    refreshCaptcha()
  }
})
</script>

<style scoped>
.admin-login {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f4f7f5 0%, #d1dbd3 100%);
  position: relative;
  overflow: hidden;
}

.login-container {
  width: 400px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 20px;
  box-shadow: 0 20px 50px rgba(58, 80, 68, 0.1);
  backdrop-filter: blur(15px);
  position: relative;
  z-index: 1;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.logo {
  height: 64px;
  margin-bottom: 16px;
  filter: drop-shadow(0 4px 8px rgba(58, 80, 68, 0.1));
}

.title {
  font-size: 28px;
  font-weight: 600;
  color: #3a5044;
  margin: 0 0 8px 0;
  letter-spacing: 2px;
}

.subtitle {
  font-size: 14px;
  color: #5c7c6a;
  margin: 0;
  font-family: "STKaiti", "Kaiti SC", serif;
}

.login-form {
  margin-bottom: 24px;
}

/* 深度选择器修改 Element Plus 样式 */
:deep(.el-input__wrapper) {
  background-color: rgba(255, 255, 255, 0.5);
  box-shadow: 0 0 0 1px #dcdfe6 inset;
  transition: all 0.3s;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #5c7c6a inset !important;
  background-color: #fff;
}

:deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #5c7c6a;
  border-color: #5c7c6a;
}

:deep(.el-checkbox__input.is-checked + .el-checkbox__label) {
  color: #3a5044;
}

:deep(.el-link.el-link--primary) {
  color: #5c7c6a;
}

:deep(.el-link.el-link--primary:hover) {
  color: #3a5044;
}

.captcha-container {
  display: flex;
  gap: 12px;
}

.captcha-image {
  width: 120px;
  height: 40px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
}

.captcha-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.refresh-text {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(58, 80, 68, 0.8);
  color: white;
  font-size: 10px;
  text-align: center;
  padding: 2px;
  transform: translateY(100%);
  transition: transform 0.3s;
}

.captcha-image:hover .refresh-text {
  transform: translateY(0);
}

.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.login-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 500;
  background: linear-gradient(135deg, #5c7c6a, #3a5044);
  border: none;
  border-radius: 8px;
  transition: all 0.3s;
}

.login-btn:hover {
  background: linear-gradient(135deg, #6d8e7b, #4a6355);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(58, 80, 68, 0.2);
}

.login-footer {
  text-align: center;
}

.tips {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 14px;
  color: #5c7c6a;
  margin: 0 0 16px 0;
}

.links {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
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

.circle {
  position: absolute;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(92, 124, 106, 0.1), rgba(58, 80, 68, 0.05));
  animation: float 6s ease-in-out infinite;
}

.circle-1 {
  width: 300px;
  height: 300px;
  top: -100px;
  left: -100px;
  animation-delay: 0s;
}

.circle-2 {
  width: 200px;
  height: 200px;
  bottom: -50px;
  right: -50px;
  animation-delay: 2s;
}

.circle-3 {
  width: 150px;
  height: 150px;
  top: 20%;
  right: 15%;
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
  .login-container {
    width: 90%;
    padding: 24px;
    margin: 20px;
  }
  
  .title {
    font-size: 24px;
  }
  
  .captcha-container {
    flex-direction: column;
  }
  
  .captcha-image {
    width: 100%;
  }
}
</style>