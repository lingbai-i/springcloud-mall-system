<template>
  <div class="merchant-login">
    <div class="login-container">
      <div class="login-header">
        <img src="/logo.png" alt="Logo" class="logo" />
        <h1 class="title">商家登录</h1>
        <p class="subtitle">欢迎使用商家管理中心</p>
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
            placeholder="请输入商家账号/手机号"
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
          没有店铺？
          <el-link type="primary" @click="handleRegister">点击注册申请</el-link>
        </p>
        <div class="links">
          <el-link href="/" target="_blank">返回首页</el-link>
          <el-divider direction="vertical" />
          <el-link href="/admin/login">管理员登录</el-link>
          <el-divider direction="vertical" />
          <el-link @click="handleHelp">帮助中心</el-link>
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
    { required: true, message: '请输入商家账号或手机号', trigger: 'blur' },
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
      type: 'merchant' // 标识为商家登录
    }
    
    try {
      await userStore.login(loginData)
      
      ElMessage.success('登录成功')
      
      // 获取重定向路径
      const redirect = route.query.redirect || '/merchant/dashboard'
      router.push(redirect)
      
    } catch (error) {
      console.error('登录失败:', error)
      
      // 根据错误类型显示不同的提示
      if (error.code === 'INVALID_CAPTCHA') {
        ElMessage.error('验证码错误')
        refreshCaptcha()
      } else if (error.code === 'ACCOUNT_LOCKED') {
        ElMessage.error('账号已被锁定，请联系客服')
      } else if (error.code === 'ACCOUNT_PENDING') {
        ElMessage.warning('您的商家账号正在审核中，请耐心等待')
      } else if (error.code === 'ACCOUNT_REJECTED') {
        ElMessage.error('您的商家申请已被拒绝，请重新申请')
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
  captchaUrl.value = `/api/merchant/captcha?t=${Date.now()}`
}

// 处理忘记密码
const handleForgotPassword = async () => {
  try {
    const { value: phone } = await ElMessageBox.prompt(
      '请输入您的注册手机号，我们将发送验证码',
      '忘记密码',
      {
        confirmButtonText: '发送验证码',
        cancelButtonText: '取消',
        inputPattern: /^1[3-9]\d{9}$/,
        inputErrorMessage: '请输入有效的手机号'
      }
    )
    
    // 发送重置密码短信
    // await merchantApi.sendResetPasswordSms(phone)
    
    ElMessage.success('验证码已发送到您的手机')
    
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('发送失败，请稍后重试')
    }
  }
}

// 处理注册
const handleRegister = () => {
  ElMessageBox.confirm(
    '商家注册需要提供营业执照等资质文件，是否继续？',
    '商家注册',
    {
      confirmButtonText: '继续注册',
      cancelButtonText: '取消',
      type: 'info'
    }
  ).then(() => {
    // 跳转到商家注册页面
    router.push('/merchant/register')
  }).catch(() => {
    // 用户取消
  })
}

// 处理帮助
const handleHelp = () => {
  ElMessageBox.alert(
    '如有问题，请联系客服：\n电话：400-123-4567\n邮箱：merchant@mall.com\n工作时间：9:00-18:00',
    '帮助中心',
    {
      confirmButtonText: '知道了'
    }
  )
}

// 组件挂载时的初始化
onMounted(() => {
  // 如果已经登录且是商家，直接跳转到仪表板
  if (userStore.isLoggedIn && userStore.isMerchant) {
    router.push('/merchant/dashboard')
    return
  }
  
  // 检查是否需要显示验证码
  const failCount = localStorage.getItem('merchant_login_fail_count') || 0
  if (failCount >= 3) {
    showCaptcha.value = true
    refreshCaptcha()
  }
})
</script>

<style scoped>
.merchant-login {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
  position: relative;
  overflow: hidden;
}

.login-container {
  width: 400px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  position: relative;
  z-index: 1;
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.logo {
  height: 64px;
  margin-bottom: 16px;
}

.title {
  font-size: 28px;
  font-weight: 600;
  color: #52c41a;
  margin: 0 0 8px 0;
}

.subtitle {
  font-size: 14px;
  color: #666;
  margin: 0;
}

.login-form {
  margin-bottom: 24px;
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
  background: #f5f7fa;
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
  background: rgba(0, 0, 0, 0.7);
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
  background: linear-gradient(135deg, #52c41a, #389e0d);
  border: none;
}

.login-btn:hover {
  background: linear-gradient(135deg, #73d13d, #52c41a);
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
  color: #666;
  margin: 0 0 16px 0;
}

.links {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  flex-wrap: wrap;
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
  background: rgba(255, 255, 255, 0.1);
  animation: float 6s ease-in-out infinite;
}

.circle-1 {
  width: 200px;
  height: 200px;
  top: 10%;
  left: 10%;
  animation-delay: 0s;
}

.circle-2 {
  width: 150px;
  height: 150px;
  top: 60%;
  right: 10%;
  animation-delay: 2s;
}

.circle-3 {
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
  
  .links {
    font-size: 12px;
  }
}
</style>