<template>
  <div class="register-container">
    <div class="register-box">
      <!-- 步骤指示器 -->
      <div class="step-indicator">
        <div class="step" :class="{ active: currentStep >= 1, completed: currentStep > 1 }">
          <div class="step-number">1</div>
          <div class="step-title">手机验证</div>
        </div>
        <div class="step-line" :class="{ completed: currentStep > 1 }"></div>
        <div class="step" :class="{ active: currentStep >= 2, completed: currentStep > 2 }">
          <div class="step-number">2</div>
          <div class="step-title">设置密码</div>
        </div>
      </div>

      <!-- 注册标题 -->
      <div class="register-header">
        <h2 class="title">用户注册</h2>
        <p class="subtitle">{{ stepSubtitle }}</p>
      </div>

      <!-- 第一步：手机号验证 -->
      <div v-if="currentStep === 1" class="step-content">
        <el-form 
          :model="phoneForm" 
          :rules="phoneRules" 
          ref="phoneFormRef"
          class="register-form"
          @submit.prevent="handlePhoneVerification">
          
          <el-form-item prop="phone">
            <el-input
              v-model="phoneForm.phone"
              placeholder="请输入手机号"
              size="large"
              clearable>
              <template #prefix>
                <el-icon><Phone /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          
          <el-form-item prop="smsCode">
            <div class="sms-input">
              <el-input
                v-model="phoneForm.smsCode"
                placeholder="请输入验证码"
                size="large"
                clearable>
                <template #prefix>
                  <el-icon><Message /></el-icon>
                </template>
              </el-input>
              <el-button 
                :disabled="smsCountdown > 0 || !phoneForm.phone"
                @click="sendSmsCodeHandler"
                class="sms-btn"
                size="large">
                {{ smsCountdown > 0 ? `${smsCountdown}s后重发` : '获取验证码' }}
              </el-button>
            </div>
          </el-form-item>

          <el-form-item>
            <el-button 
              type="primary" 
              size="large"
              :loading="loading"
              @click="handlePhoneVerification"
              class="next-btn">
              下一步
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 第二步：密码设置 -->
      <div v-if="currentStep === 2" class="step-content">
        <el-form 
          :model="passwordForm" 
          :rules="passwordRules" 
          ref="passwordFormRef"
          class="register-form"
          @submit.prevent="handleRegister">
          
          <el-form-item prop="password">
            <el-input
              v-model="passwordForm.password"
              type="password"
              placeholder="请设置密码"
              size="large"
              show-password
              clearable>
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          
          <el-form-item prop="confirmPassword">
            <el-input
              v-model="passwordForm.confirmPassword"
              type="password"
              placeholder="请确认密码"
              size="large"
              show-password
              clearable
              @keyup.enter="handleRegister">
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <!-- 用户协议 -->
          <el-form-item prop="agreement">
            <el-checkbox v-model="passwordForm.agreement">
              我已阅读并同意
              <el-button type="text" @click="showUserAgreement">《用户协议》</el-button>
              和
              <el-button type="text" @click="showPrivacyPolicy">《隐私政策》</el-button>
            </el-checkbox>
          </el-form-item>

          <el-form-item>
            <div class="button-group">
              <el-button 
                size="large"
                @click="goToPreviousStep"
                class="back-btn">
                上一步
              </el-button>
              <el-button 
                type="primary" 
                size="large"
                :loading="loading"
                @click="handleRegister"
                class="register-btn">
                完成注册
              </el-button>
            </div>
          </el-form-item>
        </el-form>
      </div>

      <!-- 登录链接 -->
      <div class="login-link">
        <span>已有账号？</span>
        <el-button type="text" @click="goToLogin">立即登录</el-button>
      </div>
    </div>

    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
    </div>

    <!-- 用户协议对话框 -->
    <el-dialog v-model="agreementDialogVisible" title="用户协议" width="600px">
      <div class="agreement-content">
        <h3>用户协议</h3>
        <p>欢迎使用我们的在线商城服务。在使用本服务前，请仔细阅读以下条款：</p>
        
        <h4>1. 服务条款</h4>
        <p>用户在使用本服务时，必须遵守相关法律法规，不得进行违法违规活动。</p>
        
        <h4>2. 账户安全</h4>
        <p>用户有责任保护自己的账户安全，包括但不限于密码保护、及时更新个人信息等。</p>
        
        <h4>3. 购物规则</h4>
        <p>用户在购买商品时，应确保提供的信息真实有效，并按照平台规则完成交易。</p>
        
        <h4>4. 免责声明</h4>
        <p>平台对因用户违规使用服务而造成的损失不承担责任。</p>
        
        <p>本协议的最终解释权归平台所有。</p>
      </div>
      <template #footer>
        <el-button @click="agreementDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 隐私政策对话框 -->
    <el-dialog v-model="privacyDialogVisible" title="隐私政策" width="600px">
      <div class="privacy-content">
        <h3>隐私政策</h3>
        <p>我们非常重视您的隐私保护，本政策说明我们如何收集、使用和保护您的个人信息：</p>
        
        <h4>1. 信息收集</h4>
        <p>我们会收集您在注册、购物过程中提供的必要信息，如姓名、联系方式、收货地址等。</p>
        
        <h4>2. 信息使用</h4>
        <p>收集的信息仅用于提供服务、处理订单、客户服务等合法用途。</p>
        
        <h4>3. 信息保护</h4>
        <p>我们采用行业标准的安全措施保护您的个人信息，防止未经授权的访问、使用或泄露。</p>
        
        <h4>4. 信息共享</h4>
        <p>除法律要求外，我们不会向第三方分享您的个人信息。</p>
        
        <h4>5. 用户权利</h4>
        <p>您有权查看、修改或删除您的个人信息。</p>
        
        <p>如有疑问，请联系我们的客服团队。</p>
      </div>
      <template #footer>
        <el-button @click="privacyDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { sendSmsCode } from '@/api/auth'
import { Phone, Message, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

// 响应式数据
const currentStep = ref(1)
const loading = ref(false)
const smsCountdown = ref(0)
const phoneFormRef = ref()
const passwordFormRef = ref()
const agreementDialogVisible = ref(false)
const privacyDialogVisible = ref(false)

// 第一步表单数据
const phoneForm = reactive({
  phone: '',
  smsCode: ''
})

// 第二步表单数据
const passwordForm = reactive({
  password: '',
  confirmPassword: '',
  agreement: false
})

// 计算属性
const stepSubtitle = computed(() => {
  switch (currentStep.value) {
    case 1:
      return '请输入手机号并获取验证码'
    case 2:
      return '请设置您的登录密码'
    default:
      return '加入我们，开启购物之旅'
  }
})

// 生成用户名的函数
const generateUsername = () => {
  const chars = 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789'
  let result = 'bc'
  for (let i = 0; i < 6; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length))
  }
  return result
}

// 验证规则
const phoneRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  smsCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位数字', trigger: 'blur' }
  ]
}

const validatePassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入密码'))
  } else if (value.length < 6) {
    callback(new Error('密码长度不能少于6位'))
  } else if (!/(?=.*[a-zA-Z])(?=.*\d)/.test(value)) {
    callback(new Error('密码必须包含字母和数字'))
  } else {
    callback()
  }
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请确认密码'))
  } else if (value !== passwordForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const validateAgreement = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请阅读并同意用户协议和隐私政策'))
  } else {
    callback()
  }
}

const passwordRules = {
  password: [
    { validator: validatePassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  agreement: [
    { validator: validateAgreement, trigger: 'change' }
  ]
}

let smsTimer = null

// 方法
const sendSmsCodeHandler = async () => {
  if (!phoneForm.phone) {
    ElMessage.warning('请先输入手机号')
    return
  }
  
  if (!/^1[3-9]\d{9}$/.test(phoneForm.phone)) {
    ElMessage.error('请输入正确的手机号')
    return
  }
  
  try {
    await sendSmsCode(phoneForm.phone, 'REGISTER')
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

const handlePhoneVerification = async () => {
  try {
    await phoneFormRef.value.validate()
    loading.value = true
    
    // 这里应该调用API验证手机号和验证码
    // 模拟验证过程
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 验证成功，进入下一步
    currentStep.value = 2
    ElMessage.success('手机验证成功')
    
  } catch (error) {
    console.error('手机验证失败:', error)
  } finally {
    loading.value = false
  }
}

const goToPreviousStep = () => {
  if (currentStep.value > 1) {
    currentStep.value--
  }
}

const handleRegister = async () => {
  try {
    await passwordFormRef.value.validate()
    loading.value = true
    
    // 生成用户名
    const username = generateUsername()
    
    const registerData = {
      username: username,
      phone: phoneForm.phone,
      captcha: phoneForm.smsCode,  // 修改字段名从smsCode到captcha
      password: passwordForm.password,
      confirmPassword: passwordForm.password,  // 添加确认密码字段
      uuid: 'register-' + Date.now()  // 添加uuid字段
    }
    
    // 调用用户store的注册方法
    const success = await userStore.userRegister(registerData)
    
    if (success) {
      ElMessage.success('注册成功！欢迎加入我们')
      // 注册成功后跳转到首页
      router.push('/')
    }
    
  } catch (error) {
    console.error('注册失败:', error)
    ElMessage.error('注册失败，请重试')
  } finally {
    loading.value = false
  }
}

const showUserAgreement = () => {
  agreementDialogVisible.value = true
}

const showPrivacyPolicy = () => {
  privacyDialogVisible.value = true
}

const goToLogin = () => {
  router.push('/login')
}

// 生命周期
onMounted(() => {
  // 如果已经登录，直接跳转到首页
  if (userStore.isLoggedIn) {
    router.push('/')
  }
})

onUnmounted(() => {
  if (smsTimer) {
    clearInterval(smsTimer)
  }
})
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}

.register-box {
  width: 450px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  position: relative;
  z-index: 10;
  max-height: 90vh;
  overflow-y: auto;
}

/* 步骤指示器 */
.step-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 30px;
}

.step {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
}

.step-number {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #e4e7ed;
  color: #909399;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 16px;
  transition: all 0.3s;
}

.step.active .step-number {
  background: #409eff;
  color: white;
}

.step.completed .step-number {
  background: #67c23a;
  color: white;
}

.step-title {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
  transition: all 0.3s;
}

.step.active .step-title {
  color: #409eff;
  font-weight: 600;
}

.step.completed .step-title {
  color: #67c23a;
  font-weight: 600;
}

.step-line {
  width: 80px;
  height: 2px;
  background: #e4e7ed;
  margin: 0 20px;
  margin-top: -20px;
  transition: all 0.3s;
}

.step-line.completed {
  background: #67c23a;
}

.register-header {
  text-align: center;
  margin-bottom: 30px;
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

.step-content {
  min-height: 200px;
}

.register-form {
  margin-top: 20px;
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

.next-btn,
.register-btn {
  width: 100%;
  height: 45px;
  font-size: 16px;
  font-weight: 600;
}

.button-group {
  display: flex;
  gap: 15px;
}

.back-btn {
  flex: 1;
  height: 45px;
  font-size: 16px;
}

.register-btn {
  flex: 2;
  height: 45px;
  font-size: 16px;
  font-weight: 600;
}

.login-link {
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

/* 协议内容样式 */
.agreement-content,
.privacy-content {
  line-height: 1.6;
  color: #606266;
}

.agreement-content h3,
.privacy-content h3 {
  color: #303133;
  margin-bottom: 15px;
}

.agreement-content h4,
.privacy-content h4 {
  color: #409eff;
  margin: 15px 0 10px 0;
  font-size: 14px;
}

.agreement-content p,
.privacy-content p {
  margin-bottom: 10px;
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .register-box {
    width: 90%;
    padding: 30px 20px;
  }
  
  .sms-input {
    flex-direction: column;
  }
  
  .sms-btn {
    min-width: auto;
  }
  
  .button-group {
    flex-direction: column;
  }
  
  .step-indicator {
    transform: scale(0.9);
  }
}
</style>