<!--
  修改密码页面
  
  @author lingbai
  @version 1.0
  @since 2025-10-21
-->
<template>
  <div class="password-page">
    <div class="page-header">
      <el-button 
        type="text" 
        @click="$router.back()"
        class="back-btn"
      >
        <LocalIcon name="fanhui" :size="16" />
        返回
      </el-button>
      <h1>修改密码</h1>
    </div>

    <el-card shadow="hover">
      <div class="password-tips">
        <el-alert
          title="密码安全提示"
          type="info"
          :closable="false"
          show-icon
        >
          <template #default>
            <ul class="tips-list">
              <li>密码长度为6-20位字符</li>
              <li>必须包含字母和数字</li>
              <li>建议使用字母、数字和特殊字符的组合</li>
              <li>不要使用过于简单或常见的密码</li>
            </ul>
          </template>
        </el-alert>
      </div>

      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        label-width="120px"
        class="password-form"
      >
        <!-- 原密码 -->
        <el-form-item label="原密码" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            placeholder="请输入原密码"
            show-password
            autocomplete="current-password"
          />
        </el-form-item>

        <!-- 新密码 -->
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password
            autocomplete="new-password"
            @input="handlePasswordInput"
          />
          <!-- 密码强度指示器 -->
          <div class="password-strength">
            <div class="strength-label">密码强度：</div>
            <div class="strength-bar">
              <div 
                class="strength-fill"
                :class="passwordStrengthClass"
                :style="{ width: passwordStrengthWidth }"
              ></div>
            </div>
            <div class="strength-text" :class="passwordStrengthClass">
              {{ passwordStrengthText }}
            </div>
          </div>
        </el-form-item>

        <!-- 确认新密码 -->
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
            autocomplete="new-password"
          />
        </el-form-item>

        <!-- 操作按钮 -->
        <el-form-item>
          <el-button 
            type="primary" 
            @click="handleSubmit"
            :loading="submitting"
            size="large"
          >
            修改密码
          </el-button>
          <el-button 
            @click="handleReset"
            size="large"
          >
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 修改成功提示 -->
    <el-dialog
      v-model="showSuccessDialog"
      title="密码修改成功"
      width="400px"
      :show-close="false"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
    >
      <div class="success-content">
        <LocalIcon name="chenggong" :size="48" color="green" class="success-icon" />
        <h2>密码修改成功！</h2>
        <p>您的密码已成功修改，请使用新密码登录。</p>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button type="primary" @click="handleReLogin">
            重新登录
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import LocalIcon from '@/components/LocalIcon.vue'

/**
 * 修改密码页面组件
 * 
 * 功能包括：
 * - 密码修改
 * - 密码强度检测
 * - 表单验证
 * - 修改成功后重新登录
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-10-21
 */

const router = useRouter()
const userStore = useUserStore()

// 表单引用
const passwordFormRef = ref()

// 响应式数据
const submitting = ref(false)
const showSuccessDialog = ref(false)

// 表单数据
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 密码强度
const passwordStrength = ref(0)

// 表单验证规则
const passwordRules = reactive({
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' },
    { 
      pattern: /^(?=.*[a-zA-Z])(?=.*\d)[a-zA-Z\d@$!%*?&]{6,20}$/, 
      message: '密码必须包含字母和数字', 
      trigger: 'blur' 
    },
    { validator: validateNewPassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
})

/**
 * 验证新密码不能与原密码相同
 */
function validateNewPassword(rule, value, callback) {
  if (value && value === passwordForm.oldPassword) {
    callback(new Error('新密码不能与原密码相同'))
  } else {
    callback()
  }
}

/**
 * 验证确认密码
 */
function validateConfirmPassword(rule, value, callback) {
  if (value && value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

/**
 * 密码强度计算
 */
const calculatePasswordStrength = (password) => {
  if (!password) return 0
  
  let strength = 0
  
  // 长度检查
  if (password.length >= 6) strength += 1
  if (password.length >= 8) strength += 1
  
  // 字符类型检查
  if (/[a-z]/.test(password)) strength += 1
  if (/[A-Z]/.test(password)) strength += 1
  if (/\d/.test(password)) strength += 1
  if (/[@$!%*?&]/.test(password)) strength += 1
  
  return Math.min(strength, 4)
}

/**
 * 密码强度样式类
 */
const passwordStrengthClass = computed(() => {
  switch (passwordStrength.value) {
    case 0:
    case 1: return 'weak'
    case 2: return 'medium'
    case 3: return 'strong'
    case 4: return 'very-strong'
    default: return 'weak'
  }
})

/**
 * 密码强度宽度
 */
const passwordStrengthWidth = computed(() => {
  return `${(passwordStrength.value / 4) * 100}%`
})

/**
 * 密码强度文本
 */
const passwordStrengthText = computed(() => {
  switch (passwordStrength.value) {
    case 0: return '无'
    case 1: return '弱'
    case 2: return '中等'
    case 3: return '强'
    case 4: return '很强'
    default: return '无'
  }
})

/**
 * 处理密码输入
 */
const handlePasswordInput = () => {
  passwordStrength.value = calculatePasswordStrength(passwordForm.newPassword)
  
  // 如果确认密码已输入，重新验证
  if (passwordForm.confirmPassword) {
    passwordFormRef.value?.validateField('confirmPassword')
  }
}

/**
 * 提交表单
 */
const handleSubmit = async () => {
  try {
    // 表单验证
    const valid = await passwordFormRef.value.validate()
    if (!valid) return

    submitting.value = true

    // 调用修改密码API
    await userStore.changeUserPassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
      confirmPassword: passwordForm.confirmPassword
    })

    // 显示成功对话框
    showSuccessDialog.value = true
    
  } catch (error) {
    console.error('修改密码失败:', error)
    ElMessage.error(error.message || '修改密码失败，请重试')
  } finally {
    submitting.value = false
  }
}

/**
 * 重置表单
 */
const handleReset = () => {
  passwordFormRef.value?.resetFields()
  passwordStrength.value = 0
}

/**
 * 重新登录
 */
const handleReLogin = async () => {
  try {
    // 登出当前用户
    await userStore.userLogout()
    
    // 跳转到登录页面
    router.push('/auth/login')
    
    ElMessage.success('请使用新密码重新登录')
  } catch (error) {
    console.error('登出失败:', error)
    // 即使登出失败也要跳转到登录页
    router.push('/auth/login')
  }
}
</script>

<style scoped>
.password-page {
  max-width: 600px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  margin-bottom: 30px;
  gap: 15px;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #409EFF;
  font-size: 14px;
}

.page-header h1 {
  margin: 0;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.password-tips {
  margin-bottom: 30px;
}

.tips-list {
  margin: 10px 0 0 0;
  padding-left: 20px;
}

.tips-list li {
  margin-bottom: 5px;
  color: #606266;
  font-size: 14px;
}

.password-form {
  padding: 20px;
}

.password-strength {
  display: flex;
  align-items: center;
  margin-top: 8px;
  gap: 10px;
}

.strength-label {
  font-size: 12px;
  color: #606266;
  white-space: nowrap;
}

.strength-bar {
  flex: 1;
  height: 4px;
  background-color: #f0f0f0;
  border-radius: 2px;
  overflow: hidden;
}

.strength-fill {
  height: 100%;
  transition: all 0.3s ease;
  border-radius: 2px;
}

.strength-fill.weak {
  background-color: #F56C6C;
}

.strength-fill.medium {
  background-color: #E6A23C;
}

.strength-fill.strong {
  background-color: #409EFF;
}

.strength-fill.very-strong {
  background-color: #67C23A;
}

.strength-text {
  font-size: 12px;
  font-weight: 500;
  white-space: nowrap;
}

.strength-text.weak {
  color: #F56C6C;
}

.strength-text.medium {
  color: #E6A23C;
}

.strength-text.strong {
  color: #409EFF;
}

.strength-text.very-strong {
  color: #67C23A;
}

.success-content {
  text-align: center;
  padding: 20px;
}

/* 本地图标样式 */
.local-icon {
  display: inline-block;
  vertical-align: middle;
  object-fit: contain;
}

.success-icon {
  margin-bottom: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .password-container {
    padding: 15px;
  }
  
  .password-form {
    padding: 20px;
  }
  
  .back-btn {
    margin-bottom: 15px;
  }
}

.password-form :deep(.el-form-item__label) {
  width: 100px !important;
}

.password-strength {
  flex-direction: column;
  align-items: flex-start;
  gap: 5px;
}

.strength-bar {
  width: 100%;
}
</style>