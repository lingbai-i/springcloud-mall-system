<!--
 * @Author: lingbai
 * @Date: 2025-01-27 15:30:00
 * @LastEditTime: 2025-11-08 09:13:29 +08:00
 * @Description: 登录弹窗组件 - 参考京东登录弹窗设计，支持密码登录和验证码登录
 * @FilePath: /frontend/src/components/LoginModal.vue
-->
<template>
  <el-dialog
    v-model="visible"
    title=""
    width="29%"
    :show-close="false"
    :close-on-click-modal="true"
    class="login-modal"
    @close="handleClose"
  >


    <div class="login-content">
      <!-- 登录方式切换 -->
      <div class="login-tabs">
        <div 
          class="tab-item"
          :class="{ active: loginType === 'password' }"
          @click="switchLoginType('password')"
        >
          密码登录
        </div>
        <div 
          class="tab-item"
          :class="{ active: loginType === 'sms' }"
          @click="switchLoginType('sms')"
        >
          验证码登录
        </div>
      </div>

      <!-- 登录表单 -->
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        @submit.prevent="handleSubmit"
      >
        <!-- 密码登录表单 -->
        <template v-if="loginType === 'password'">
          <el-form-item prop="account">
            <el-input
              v-model="loginForm.account"
              placeholder="请输入账号名/手机号/邮箱"
              size="large"
              clearable
              @keyup.enter="handleSubmit"
            />
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              show-password
              clearable
              @keyup.enter="handleSubmit"
            />
          </el-form-item>
        </template>

        <!-- 验证码登录表单 -->
        <template v-else>
          <el-form-item prop="phone">
            <el-input
              v-model="loginForm.phone"
              placeholder="请输入手机号"
              size="large"
              clearable
              @keyup.enter="handleSubmit"
            />
          </el-form-item>
          
          <el-form-item prop="smsCode">
            <div class="sms-input-group">
              <el-input
                v-model="loginForm.smsCode"
                placeholder="请输入验证码"
                size="large"
                clearable
                @keyup.enter="handleSubmit"
              />
              <el-button
                type="text"
                class="sms-btn"
                :disabled="smsCountdown > 0"
                @click="sendSmsCode"
              >
                {{ smsCountdown > 0 ? `${smsCountdown}s后重发` : '获取验证码' }}
              </el-button>
            </div>
          </el-form-item>
        </template>

        <!-- 记住登录状态 -->
        <div class="login-options">
          <el-checkbox v-model="rememberMe">记住登录状态</el-checkbox>
          <a href="#" class="forgot-password" @click="handleForgotPassword">忘记密码？</a>
        </div>

        <!-- 登录按钮 -->
        <el-form-item>
          <el-button
            type="danger"
            size="large"
            class="login-btn"
            :loading="loginLoading"
            @click="handleSubmit"
          >
            {{ loginLoading ? '登录中...' : '登录' }}
          </el-button>
        </el-form-item>

        <!-- 注册链接 -->
        <div class="register-link">
          <span>还没有账号？</span>
          <a href="#" @click.prevent="handleGoRegister">立即注册</a>
        </div>
      </el-form>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
// 统一日志接口（可平滑替换为 Winston），用于结构化调试与错误记录
import * as logger from '@/utils/logger'

/**
 * 组件属性定义
 */
const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
})

/**
 * 组件事件定义
 */
const emit = defineEmits(['update:modelValue', 'login-success'])

/**
 * 路由实例
 */
const router = useRouter()

/**
 * 弹窗显示状态
 */
const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

/**
 * 登录类型：password-密码登录，sms-验证码登录
 */
const loginType = ref('password')

/**
 * 登录表单引用
 */
const loginFormRef = ref()

/**
 * 登录表单数据
 */
const loginForm = reactive({
  account: '', // 账号（用户名/手机号/邮箱）
  password: '', // 密码
  phone: '', // 手机号
  smsCode: '' // 短信验证码
})

/**
 * 记住登录状态
 */
const rememberMe = ref(false)

/**
 * 登录加载状态
 */
const loginLoading = ref(false)

/**
 * 短信验证码倒计时
 */
const smsCountdown = ref(0)

/**
 * 表单验证规则
 */
const loginRules = computed(() => {
  const rules = {}
  
  if (loginType.value === 'password') {
    rules.account = [
      { required: true, message: '请输入账号名/手机号/邮箱', trigger: 'blur' },
      { min: 2, max: 50, message: '账号长度在 2 到 50 个字符', trigger: 'blur' }
    ]
    rules.password = [
      { required: true, message: '请输入密码', trigger: 'blur' },
      { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
    ]
  } else {
    rules.phone = [
      { required: true, message: '请输入手机号', trigger: 'blur' },
      { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
    ]
    rules.smsCode = [
      { required: true, message: '请输入验证码', trigger: 'blur' },
      { pattern: /^\d{6}$/, message: '验证码为6位数字', trigger: 'blur' }
    ]
  }
  
  return rules
})

/**
 * 切换登录方式
 * @param {string} type - 登录类型
 */
const switchLoginType = (type) => {
  loginType.value = type
  // 清空表单数据
  Object.keys(loginForm).forEach(key => {
    loginForm[key] = ''
  })
  // 清除表单验证
  if (loginFormRef.value) {
    loginFormRef.value.clearValidate()
  }
}

/**
 * 发送短信验证码
 * 说明：
 * - 后端接口期望位置参数：`sendSmsCode(phoneNumber, purpose)`；此前误传对象导致服务端解析失败。
 * - 这里遵循防御式校验，确保手机号与目的有效。
 * @author lingbai
 * @version 1.1 2025-11-08T09:13:29+08:00：修正参数为位置传递并添加统一日志
 */
const sendSmsCode = async () => {
  if (!loginForm.phone) {
    ElMessage.warning('请先输入手机号')
    return
  }
  
  if (!/^1[3-9]\d{9}$/.test(loginForm.phone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  
  try {
    // 调用发送短信验证码API（位置参数：手机号 + 目的）
    const { sendSmsCode: sendCode } = await import('@/api/auth')
    await sendCode(loginForm.phone, 'LOGIN')
    
    ElMessage.success('验证码已发送')
    
    // 开始倒计时
    smsCountdown.value = 60
    const timer = setInterval(() => {
      smsCountdown.value--
      if (smsCountdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (error) {
    // 统一错误日志，便于与请求拦截器日志关联分析
    logger.error('发送验证码失败:', error)
    ElMessage.error('验证码发送失败，请重试')
  }
}

/**
 * 处理登录提交
 */
const handleSubmit = async () => {
  if (!loginFormRef.value) return
  
  try {
    await loginFormRef.value.validate()
    loginLoading.value = true
    
    // 调用登录API
    const { login, loginBySms } = await import('@/api/auth')
    const userStore = useUserStore()
    
    if (loginType.value === 'password') {
      // 密码登录
      const loginData = {
        username: loginForm.account,
        password: loginForm.password
      }
      await userStore.login(loginData)
    } else {
      // 验证码登录
      const response = await loginBySms({
        phone: loginForm.phone,
        code: loginForm.smsCode
      })
      
      // 处理验证码登录响应
      if (response.success && response.data) {
        const { accessToken, userInfo: userData } = response.data
        // 保存token到localStorage
        localStorage.setItem('token', accessToken)
        localStorage.setItem('userInfo', JSON.stringify(userData))
        // 更新store
        userStore.token = accessToken
        userStore.userInfo = userData
      }
    }
    
    ElMessage.success('登录成功')
    emit('login-success', {
      type: loginType.value,
      data: { ...loginForm },
      rememberMe: rememberMe.value
    })
    
    handleClose()
  } catch (error) {
    console.error('登录失败:', error)
    if (error !== false) { // 不是表单验证错误
      ElMessage.error(error.response?.data?.message || error.message || '登录失败，请检查账号密码')
    }
  } finally {
    loginLoading.value = false
  }
}

/**
 * 处理忘记密码
 */
const handleForgotPassword = () => {
  ElMessage.info('忘记密码功能开发中...')
}

/**
 * 跳转到注册
 */
const handleGoRegister = () => {
  handleClose()
  router.push('/auth/register')
}



/**
 * 关闭弹窗
 */
const handleClose = () => {
  visible.value = false
  // 重置表单
  Object.keys(loginForm).forEach(key => {
    loginForm[key] = ''
  })
  rememberMe.value = false
  loginType.value = 'password'
  if (loginFormRef.value) {
    loginFormRef.value.clearValidate()
  }
}

/**
 * 监听弹窗显示状态，重置表单
 */
watch(visible, (newVal) => {
  if (!newVal) {
    handleClose()
  }
})
</script>

<style scoped>
/* 弹窗样式 */
.login-modal {
  --el-dialog-border-radius: 8px;
}

.login-modal :deep(.el-dialog) {
  border-radius: 8px;
  overflow: hidden;
}

.login-modal :deep(.el-dialog__header) {
  padding: 0;
  margin: 0;
}

.login-modal :deep(.el-dialog__body) {
  padding: 0;
}



/* 内容区域 */
.login-content {
  padding: 24px 16px 16px 16px;
}

/* 登录方式切换 */
.login-tabs {
  display: flex;
  margin-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.tab-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 8px;
  cursor: pointer;
  color: #666;
  border-bottom: 2px solid transparent;
  transition: all 0.3s;
  font-size: 14px;
}

.tab-item:hover {
  color: #ff4142;
}

.tab-item.active {
  color: #ff4142;
  border-bottom-color: #ff4142;
}

.tab-item .local-icon {
  margin-right: 6px;
}

/* 表单样式 */
.login-form {
  margin-top: 16px;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 16px;
}

.login-form :deep(.el-input__wrapper) {
  border-radius: 6px;
  box-shadow: 0 0 0 1px #dcdfe6 inset;
}

.login-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #c0c4cc inset;
}

.login-form :deep(.el-input.is-focus .el-input__wrapper) {
  box-shadow: 0 0 0 1px #ff4142 inset;
}

/* 短信验证码输入组 */
.sms-input-group {
  display: flex;
  gap: 12px;
}

.sms-input-group .el-input {
  flex: 1;
}

.sms-btn {
  white-space: nowrap;
  color: #ff4142;
  font-size: 14px;
  padding: 0 12px;
}

.sms-btn:hover:not(:disabled) {
  color: #ff6b6c;
}

.sms-btn:disabled {
  color: #c0c4cc;
}

/* 登录选项 */
.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.forgot-password {
  color: #ff4142;
  text-decoration: none;
  font-size: 14px;
}

.forgot-password:hover {
  color: #ff6b6c;
}

/* 登录按钮 */
.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 6px;
  background: linear-gradient(135deg, #ff4142 0%, #ff6b6c 100%);
  border: none;
}

.login-btn:hover {
  background: linear-gradient(135deg, #ff6b6c 0%, #ff4142 100%);
}

/* 注册链接 */
.register-link {
  text-align: center;
  margin: 16px 0;
  font-size: 13px;
  color: #666;
}

.register-link a {
  color: #ff4142;
  text-decoration: none;
  margin-left: 4px;
}

.register-link a:hover {
  color: #ff6b6c;
}

/* 桌面端优化样式 */
@media (min-width: 1024px) {
  .login-modal :deep(.el-dialog) {
    max-width: 260px;
    width: 29%;
  }
  
  .login-content {
    padding: 32px 20px 20px 20px;
  }
  
  .login-form {
    max-width: 100%;
    margin: 0 auto;
  }
}
</style>
