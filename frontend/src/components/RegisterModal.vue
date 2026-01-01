<!--
 * @Author: lingbai
 * @Date: 2025-01-27 15:35:00
 * @LastEditTime: 2025-01-27 15:35:00
 * @Description: 注册弹窗组件 - 参考京东注册弹窗设计
 * @FilePath: /frontend/src/components/RegisterModal.vue
-->
<template>
  <el-dialog
    v-model="visible"
    title=""
    width="400px"
    :show-close="false"
    :close-on-click-modal="false"
    class="register-modal"
    @close="handleClose"
  >
    <!-- 自定义头部 -->
    <template #header>
      <div class="modal-header">
        <div class="logo-section">
          <h2>百物语</h2>
          <span class="welcome-text">欢迎注册</span>
        </div>
        <el-button
          type="text"
          class="close-btn"
          @click="handleClose"
        >
          <LocalIcon name="shanchu" :size="16" />
        </el-button>
      </div>
    </template>

    <div class="register-content">
      <!-- 注册表单 -->
      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        class="register-form"
        @submit.prevent="handleSubmit"
      >
        <!-- 用户名 -->
        <el-form-item prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="请输入用户名"
            size="large"
            clearable
            @keyup.enter="handleSubmit"
          >
            <template #prefix>
              <LocalIcon name="zhanghao" :size="16" />
            </template>
          </el-input>
        </el-form-item>

        <!-- 手机号 -->
        <el-form-item prop="phone">
          <el-input
            v-model="registerForm.phone"
            placeholder="请输入手机号"
            size="large"
            clearable
            @keyup.enter="handleSubmit"
          >
            <template #prefix>
              <LocalIcon name="shoujihao" :size="16" />
            </template>
          </el-input>
        </el-form-item>

        <!-- 短信验证码 -->
        <el-form-item prop="smsCode">
          <div class="sms-input-group">
            <el-input
              v-model="registerForm.smsCode"
              placeholder="请输入验证码"
              size="large"
              clearable
              @keyup.enter="handleSubmit"
            >
              <template #prefix>
                <LocalIcon name="tishi" :size="16" />
              </template>
            </el-input>
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

        <!-- 密码 -->
        <el-form-item prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            show-password
            clearable
            @keyup.enter="handleSubmit"
          >
            <template #prefix>
              <LocalIcon name="mima" :size="16" />
            </template>
          </el-input>
        </el-form-item>

        <!-- 确认密码 -->
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请确认密码"
            size="large"
            show-password
            clearable
            @keyup.enter="handleSubmit"
          >
            <template #prefix>
              <LocalIcon name="mima" :size="16" />
            </template>
          </el-input>
        </el-form-item>

        <!-- 邮箱（可选） -->
        <el-form-item prop="email">
          <el-input
            v-model="registerForm.email"
            placeholder="请输入邮箱（可选）"
            size="large"
            clearable
            @keyup.enter="handleSubmit"
          >
            <template #prefix>
              <LocalIcon name="gonggao" :size="16" />
            </template>
          </el-input>
        </el-form-item>

        <!-- 用户协议 -->
        <div class="agreement-section">
          <el-checkbox v-model="agreeTerms">
            我已阅读并同意
            <a href="#" class="agreement-link" @click="showAgreement">《用户协议》</a>
            和
            <a href="#" class="agreement-link" @click="showPrivacy">《隐私政策》</a>
          </el-checkbox>
        </div>

        <!-- 注册按钮 -->
        <el-form-item>
          <el-button
            type="danger"
            size="large"
            class="register-btn"
            :loading="registerLoading"
            :disabled="!agreeTerms"
            @click="handleSubmit"
          >
            {{ registerLoading ? '注册中...' : '立即注册' }}
          </el-button>
        </el-form-item>

        <!-- 登录链接 -->
        <div class="login-link">
          <span>已有账号？</span>
          <a href="#" @click="handleGoLogin">立即登录</a>
        </div>
      </el-form>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import LocalIcon from './LocalIcon.vue'

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
const emit = defineEmits(['update:modelValue', 'register-success', 'go-login'])

/**
 * 弹窗显示状态
 */
const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

/**
 * 注册表单引用
 */
const registerFormRef = ref()

/**
 * 注册表单数据
 */
const registerForm = reactive({
  username: '', // 用户名
  phone: '', // 手机号
  smsCode: '', // 短信验证码
  password: '', // 密码
  confirmPassword: '', // 确认密码
  email: '' // 邮箱（可选）
})

/**
 * 同意用户协议
 */
const agreeTerms = ref(false)

/**
 * 注册加载状态
 */
const registerLoading = ref(false)

/**
 * 短信验证码倒计时
 */
const smsCountdown = ref(0)

/**
 * 表单验证规则
 */
const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在 2 到 20 个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_\u4e00-\u9fa5]+$/, message: '用户名只能包含字母、数字、下划线和中文', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  smsCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '验证码为6位数字', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' },
    { pattern: /^(?=.*[a-zA-Z])(?=.*\d)[a-zA-Z\d@$!%*?&]{6,}$/, message: '密码必须包含字母和数字', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  email: [
    { pattern: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/, message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

/**
 * 发送短信验证码
 */
const sendSmsCode = async () => {
  if (!registerForm.phone) {
    ElMessage.warning('请先输入手机号')
    return
  }
  
  if (!/^1[3-9]\d{9}$/.test(registerForm.phone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  
  try {
    // TODO: 调用发送短信验证码API
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
    ElMessage.error('验证码发送失败，请重试')
  }
}

/**
 * 处理注册提交
 */
const handleSubmit = async () => {
  if (!registerFormRef.value) return
  
  if (!agreeTerms.value) {
    ElMessage.warning('请先同意用户协议和隐私政策')
    return
  }
  
  try {
    await registerFormRef.value.validate()
    registerLoading.value = true
    
    // TODO: 调用注册API
    await new Promise(resolve => setTimeout(resolve, 1000)) // 模拟API调用
    
    ElMessage.success('注册成功')
    emit('register-success', { ...registerForm })
    
    handleClose()
  } catch (error) {
    console.error('注册失败:', error)
    if (error !== false) { // 不是表单验证错误
      ElMessage.error('注册失败，请重试')
    }
  } finally {
    registerLoading.value = false
  }
}

/**
 * 跳转到登录
 */
const handleGoLogin = () => {
  emit('go-login')
  handleClose()
}

/**
 * 显示用户协议
 */
const showAgreement = () => {
  ElMessageBox.alert('用户协议内容...', '用户协议', {
    confirmButtonText: '确定'
  })
}

/**
 * 显示隐私政策
 */
const showPrivacy = () => {
  ElMessageBox.alert('隐私政策内容...', '隐私政策', {
    confirmButtonText: '确定'
  })
}

/**
 * 关闭弹窗
 */
const handleClose = () => {
  visible.value = false
  // 重置表单
  Object.keys(registerForm).forEach(key => {
    registerForm[key] = ''
  })
  agreeTerms.value = false
  if (registerFormRef.value) {
    registerFormRef.value.clearValidate()
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
.register-modal {
  --el-dialog-border-radius: 12px;
}

.register-modal :deep(.el-dialog) {
  border-radius: 12px;
  overflow: hidden;
  background: #f0f9f4; /* 适配主题的浅绿色背景 */
}

.register-modal :deep(.el-dialog__header) {
  padding: 0;
  margin: 0;
}

.register-modal :deep(.el-dialog__body) {
  padding: 0;
}

/* 头部样式 */
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  background: linear-gradient(135deg, #5d7e68 0%, #7da38a 100%);
  color: white;
}

.logo-section h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.welcome-text {
  font-size: 14px;
  opacity: 0.9;
  margin-left: 8px;
}

.close-btn {
  color: white !important;
  font-size: 16px;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.1) !important;
}

/* 内容区域 */
.register-content {
  padding: 24px;
  background: #f0f9f4;
}

/* 表单样式 */
.register-form {
  margin-top: 20px;
}

.register-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.register-form :deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 0 0 1px #dcdfe6 inset;
  background-color: #fff;
}

.register-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #5d7e68 inset;
}

.register-form :deep(.el-input.is-focus .el-input__wrapper) {
  box-shadow: 0 0 0 1px #5d7e68 inset !important;
}

/* 复选框主题化 */
.register-form :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #5d7e68;
  border-color: #5d7e68;
}

.register-form :deep(.el-checkbox__input.is-checked + .el-checkbox__label) {
  color: #5d7e68;
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
  color: #5d7e68;
  font-size: 14px;
  padding: 0 12px;
}

.sms-btn:hover:not(:disabled) {
  color: #4a6654;
}

.sms-btn:disabled {
  color: #c0c4cc;
}

/* 用户协议 */
.agreement-section {
  margin-bottom: 24px;
  font-size: 14px;
}

.agreement-link {
  color: #5d7e68;
  text-decoration: none;
}

.agreement-link:hover {
  color: #4a6654;
  text-decoration: underline;
}

/* 注册按钮 */
.register-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 8px;
  background: linear-gradient(135deg, #5d7e68 0%, #7da38a 100%) !important;
  border: none;
  color: white !important;
}

.register-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #4a6654 0%, #5d7e68 100%) !important;
  opacity: 0.9;
}

.register-btn:disabled {
  background: #c0c4cc !important;
  cursor: not-allowed;
}

/* 登录链接 */
.login-link {
  text-align: center;
  margin: 20px 0;
  font-size: 14px;
  color: #666;
}

.login-link a {
  color: #5d7e68;
  text-decoration: none;
  margin-left: 4px;
}

.login-link a:hover {
  color: #4a6654;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .register-modal :deep(.el-dialog) {
    width: 90% !important;
    margin: 5vh auto;
  }
  
  .modal-header {
    padding: 16px 20px;
  }
  
  .register-content {
    padding: 20px;
  }
}
</style>