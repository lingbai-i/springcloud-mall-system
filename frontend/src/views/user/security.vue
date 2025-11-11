<template>
  <div class="security-container">
    <el-card class="security-card">
      <template #header>
        <div class="card-header">
          <span>账户安全</span>
        </div>
      </template>
      
      <div class="security-items">
        <!-- 修改密码 -->
        <div class="security-item">
          <div class="item-info">
            <LocalIcon name="mima" :size="24" color="#409EFF" />
            <div class="item-detail">
              <h3>登录密码</h3>
              <p v-if="!userInfo.hasSetPassword" class="not-set">您还未设置登录密码，设置后可使用账号密码登录</p>
              <p v-else>定期修改密码可以提高账户安全性</p>
            </div>
          </div>
          <el-button type="primary" @click="showPasswordDialog = true">
            {{ userInfo.hasSetPassword ? '修改密码' : '设置密码' }}
          </el-button>
        </div>

        <!-- 手机绑定 -->
        <div class="security-item">
          <div class="item-info">
            <LocalIcon name="shouji" :size="24" color="#67C23A" />
            <div class="item-detail">
              <h3>手机号码</h3>
              <p v-if="userInfo.phone">已绑定: {{ formatPhone(userInfo.phone) }}</p>
              <p v-else class="not-set">未绑定</p>
            </div>
          </div>
          <el-button v-if="userInfo.phone" @click="showBindPhone = true">更换手机</el-button>
          <el-button v-else type="success" @click="showBindPhone = true">绑定手机</el-button>
        </div>

        <!-- 邮箱绑定 -->
        <div class="security-item">
          <div class="item-info">
            <LocalIcon name="youxiang" :size="24" color="#E6A23C" />
            <div class="item-detail">
              <h3>邮箱地址</h3>
              <p v-if="userInfo.email">已绑定: {{ formatEmail(userInfo.email) }}</p>
              <p v-else class="not-set">未绑定</p>
            </div>
          </div>
          <el-button v-if="userInfo.email" @click="showBindEmail = true">更换邮箱</el-button>
          <el-button v-else type="success" @click="showBindEmail = true">绑定邮箱</el-button>
        </div>

        <!-- 最后登录信息 -->
        <div class="security-item">
          <div class="item-info">
            <LocalIcon name="shizhong" :size="24" color="#909399" />
            <div class="item-detail">
              <h3>最后登录</h3>
              <p>{{ formatDate(userInfo.lastLoginTime) }}</p>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 绑定手机对话框 -->
    <el-dialog v-model="showBindPhone" title="绑定/更换手机" width="400px">
      <el-form :model="phoneForm" :rules="phoneRules" ref="phoneFormRef" label-width="80px">
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="phoneForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="验证码" prop="code">
          <div class="code-input">
            <el-input v-model="phoneForm.code" placeholder="请输入验证码" />
            <el-button :disabled="countdown > 0" @click="sendPhoneCode">
              {{ countdown > 0 ? `${countdown}s后重试` : '发送验证码' }}
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showBindPhone = false">取消</el-button>
        <el-button type="primary" @click="confirmBindPhone" :loading="binding">确定</el-button>
      </template>
    </el-dialog>

    <!-- 绑定邮箱对话框 -->
    <el-dialog v-model="showBindEmail" title="绑定/更换邮箱" width="400px">
      <el-form :model="emailForm" :rules="emailRules" ref="emailFormRef" label-width="80px">
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="emailForm.email" placeholder="请输入邮箱地址" />
        </el-form-item>
        <el-form-item label="验证码" prop="code">
          <div class="code-input">
            <el-input v-model="emailForm.code" placeholder="请输入验证码" />
            <el-button :disabled="emailCountdown > 0" @click="sendEmailCode">
              {{ emailCountdown > 0 ? `${emailCountdown}s后重试` : '发送验证码' }}
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showBindEmail = false">取消</el-button>
        <el-button type="primary" @click="confirmBindEmail" :loading="binding">确定</el-button>
      </template>
    </el-dialog>

    <!-- 修改密码对话框 -->
    <el-dialog v-model="showPasswordDialog" :title="userInfo.hasSetPassword ? '修改密码' : '设置登录密码'" width="400px">
      <el-alert 
        v-if="!userInfo.hasSetPassword"
        title="温馨提示"
        type="info"
        description="设置登录密码后，您可以使用账号密码登录系统"
        :closable="false"
        show-icon
        style="margin-bottom: 20px;"
      />
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
        <el-form-item v-if="userInfo.hasSetPassword" label="当前密码" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            placeholder="请输入当前密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPasswordDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmChangePassword" :loading="changing">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { changePassword } from '@/api/user'
import LocalIcon from '@/components/LocalIcon.vue'

const router = useRouter()
const userStore = useUserStore()

const userInfo = computed(() => userStore.userInfo || {})
const showBindPhone = ref(false)
const showBindEmail = ref(false)
const showPasswordDialog = ref(false)
const countdown = ref(0)
const emailCountdown = ref(0)
const binding = ref(false)
const changing = ref(false)
const phoneFormRef = ref()
const emailFormRef = ref()
const passwordFormRef = ref()

const phoneForm = reactive({
  phone: '',
  code: ''
})

const emailForm = reactive({
  email: '',
  code: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const phoneRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ]
}

const emailRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ]
}

// 密码规则 - 根据hasSetPassword动态设置
const passwordRules = computed(() => ({
  oldPassword: userInfo.value.hasSetPassword ? [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ] : [],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}))

const formatPhone = (phone) => {
  if (!phone) return ''
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

const formatEmail = (email) => {
  if (!email) return ''
  const [user, domain] = email.split('@')
  if (user.length <= 3) return email
  return `${user.substring(0, 3)}***@${domain}`
}

const formatDate = (dateStr) => {
  if (!dateStr) return '暂无记录'
  return new Date(dateStr).toLocaleString('zh-CN')
}

const sendPhoneCode = () => {
  ElMessage.info('验证码发送功能待实现')
  countdown.value = 60
  const timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)
}

const sendEmailCode = () => {
  ElMessage.info('验证码发送功能待实现')
  emailCountdown.value = 60
  const timer = setInterval(() => {
    emailCountdown.value--
    if (emailCountdown.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)
}

const confirmBindPhone = async () => {
  if (!phoneFormRef.value) return
  
  await phoneFormRef.value.validate(async (valid) => {
    if (valid) {
      binding.value = true
      try {
        // TODO: 调用绑定手机接口
        await new Promise(resolve => setTimeout(resolve, 1000))
        ElMessage.success('手机绑定成功')
        showBindPhone.value = false
      } catch (error) {
        ElMessage.error('绑定失败')
      } finally {
        binding.value = false
      }
    }
  })
}

const confirmBindEmail = async () => {
  if (!emailFormRef.value) return
  
  await emailFormRef.value.validate(async (valid) => {
    if (valid) {
      binding.value = true
      try {
        // TODO: 调用绑定邮箱接口
        await new Promise(resolve => setTimeout(resolve, 1000))
        ElMessage.success('邮箱绑定成功')
        showBindEmail.value = false
      } catch (error) {
        ElMessage.error('绑定失败')
      } finally {
        binding.value = false
      }
    }
  })
}

const confirmChangePassword = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      changing.value = true
      try {
        console.log(userInfo.value.hasSetPassword ? '开始修改密码...' : '开始设置密码...')
        console.log('当前token:', userStore.token)
        
        let response
        
        // 根据hasSetPassword状态调用不同的API
        if (userInfo.value.hasSetPassword) {
          // 已设置过密码，使用修改密码API
          response = await changePassword({
            oldPassword: passwordForm.oldPassword,
            newPassword: passwordForm.newPassword,
            confirmPassword: passwordForm.confirmPassword
          })
        } else {
          // 首次设置密码，使用设置密码API（不需要旧密码）
          const { setPassword } = await import('@/api/user')
          response = await setPassword({
            newPassword: passwordForm.newPassword,
            confirmPassword: passwordForm.confirmPassword
          })
        }
        
        console.log('密码操作响应:', response)
        
        if (response.success || response.code === 200) {
          const successMsg = userInfo.value.hasSetPassword 
            ? '密码修改成功，请重新登录' 
            : '密码设置成功！现在可以使用账号密码登录了'
          ElMessage.success(successMsg)
          showPasswordDialog.value = false
          passwordFormRef.value?.resetFields()
          
          // 如果是修改密码，需要重新登录
          if (userInfo.value.hasSetPassword) {
            setTimeout(() => {
              userStore.logout()
              router.push('/home')
            }, 1500)
          } else {
            // 首次设置密码，刷新用户信息（更新hasSetPassword状态）
            try {
              await userStore.fetchUserInfo()
              console.log('用户信息已刷新，hasSetPassword:', userStore.userInfo?.hasSetPassword)
            } catch (err) {
              // 静默处理fetchUserInfo错误，不影响设置密码成功的提示
              console.warn('刷新用户信息失败（不影响密码设置）:', err.message)
            }
          }
        } else {
          ElMessage.error(response.message || '操作失败')
        }
      } catch (error) {
        console.error('密码操作失败:', error)
        if (error.response) {
          console.error('错误响应:', error.response.data)
          ElMessage.error(error.response.data.message || '操作失败')
        } else {
          ElMessage.error(error.message || '操作失败')
        }
      } finally {
        changing.value = false
      }
    }
  })
}
</script>

<style scoped>
.security-container {
  padding: 20px;
}

.security-card {
  max-width: 800px;
  margin: 0 auto;
}

.card-header {
  font-size: 18px;
  font-weight: bold;
}

.security-items {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.security-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border: 1px solid #eee;
  border-radius: 8px;
  transition: all 0.3s;
}

.security-item:hover {
  border-color: #409EFF;
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.1);
}

.item-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.item-detail h3 {
  margin: 0 0 5px 0;
  font-size: 16px;
  color: #303133;
}

.item-detail p {
  margin: 0;
  font-size: 14px;
  color: #606266;
}

.not-set {
  color: #F56C6C;
}

.code-input {
  display: flex;
  gap: 10px;
}

.code-input .el-input {
  flex: 1;
}
</style>
