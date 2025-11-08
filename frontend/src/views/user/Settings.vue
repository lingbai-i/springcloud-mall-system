<template>
  <div class="settings-container">
    <el-card class="settings-card">
      <template #header>
        <div class="card-header">
          <span>账户设置</span>
        </div>
      </template>
      
      <el-tabs v-model="activeTab" class="settings-tabs">
        <!-- 安全设置 -->
        <el-tab-pane label="安全设置" name="security">
          <el-form :model="securityForm" :rules="securityRules" ref="securityFormRef" label-width="120px">
            <el-form-item label="当前密码" prop="currentPassword">
              <el-input
                v-model="securityForm.currentPassword"
                type="password"
                placeholder="请输入当前密码"
                show-password
              />
            </el-form-item>
            
            <el-form-item label="新密码" prop="newPassword">
              <el-input
                v-model="securityForm.newPassword"
                type="password"
                placeholder="请输入新密码"
                show-password
              />
            </el-form-item>
            
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input
                v-model="securityForm.confirmPassword"
                type="password"
                placeholder="请再次输入新密码"
                show-password
              />
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="changePassword">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <!-- 通知设置 -->
        <el-tab-pane label="通知设置" name="notification">
          <el-form :model="notificationForm" label-width="120px">
            <el-form-item label="邮件通知">
              <el-switch v-model="notificationForm.emailNotification" />
              <div class="setting-desc">接收订单状态、促销活动等邮件通知</div>
            </el-form-item>
            
            <el-form-item label="短信通知">
              <el-switch v-model="notificationForm.smsNotification" />
              <div class="setting-desc">接收订单状态、物流信息等短信通知</div>
            </el-form-item>
            
            <el-form-item label="系统消息">
              <el-switch v-model="notificationForm.systemNotification" />
              <div class="setting-desc">接收系统公告、活动消息等通知</div>
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="saveNotificationSettings">保存设置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <!-- 隐私设置 -->
        <el-tab-pane label="隐私设置" name="privacy">
          <el-form :model="privacyForm" label-width="120px">
            <el-form-item label="个人资料可见性">
              <el-radio-group v-model="privacyForm.profileVisibility">
                <el-radio label="public">公开</el-radio>
                <el-radio label="friends">仅好友可见</el-radio>
                <el-radio label="private">私密</el-radio>
              </el-radio-group>
            </el-form-item>
            
            <el-form-item label="购买记录">
              <el-switch v-model="privacyForm.showPurchaseHistory" />
              <div class="setting-desc">是否允许他人查看您的购买记录</div>
            </el-form-item>
            
            <el-form-item label="在线状态">
              <el-switch v-model="privacyForm.showOnlineStatus" />
              <div class="setting-desc">是否显示您的在线状态</div>
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="savePrivacySettings">保存设置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <!-- 账户管理 -->
        <el-tab-pane label="账户管理" name="account">
          <div class="account-management">
            <el-alert
              title="危险操作"
              type="warning"
              description="以下操作不可逆，请谨慎操作"
              show-icon
              :closable="false"
            />
            
            <div class="danger-zone">
              <el-button type="danger" @click="showDeleteDialog = true">
                删除账户
              </el-button>
              <div class="setting-desc">
                删除账户将永久删除您的所有数据，包括订单记录、收藏等信息
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
    
    <!-- 删除账户确认对话框 -->
    <el-dialog
      v-model="showDeleteDialog"
      title="删除账户"
      width="400px"
      :before-close="handleDeleteDialogClose"
    >
      <div class="delete-confirm">
        <el-alert
          title="警告"
          type="error"
          description="此操作不可逆，将永久删除您的账户和所有相关数据"
          show-icon
          :closable="false"
        />
        
        <el-form :model="deleteForm" :rules="deleteRules" ref="deleteFormRef" style="margin-top: 20px;">
          <el-form-item label="输入密码确认" prop="password">
            <el-input
              v-model="deleteForm.password"
              type="password"
              placeholder="请输入您的密码"
              show-password
            />
          </el-form-item>
        </el-form>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showDeleteDialog = false">取消</el-button>
          <el-button type="danger" @click="confirmDeleteAccount">确认删除</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { userApi } from '@/api/user'

const activeTab = ref('security')
const showDeleteDialog = ref(false)

const securityFormRef = ref<FormInstance>()
const deleteFormRef = ref<FormInstance>()

// 安全设置表单
const securityForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 通知设置表单
const notificationForm = reactive({
  emailNotification: true,
  smsNotification: true,
  systemNotification: true
})

// 隐私设置表单
const privacyForm = reactive({
  profileVisibility: 'public',
  showPurchaseHistory: true,
  showOnlineStatus: true
})

// 删除账户表单
const deleteForm = reactive({
  password: ''
})

// 安全设置验证规则
const securityRules = {
  currentPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule: any, value: string, callback: Function) => {
        if (value !== securityForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 删除账户验证规则
const deleteRules = {
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

/**
 * 获取用户设置
 */
const getUserSettings = async () => {
  try {
    // 这里应该调用API获取用户设置
    // const response = await userApi.getUserSettings()
    // 暂时使用默认值
  } catch (error) {
    ElMessage.error('获取设置失败')
  }
}

/**
 * 修改密码
 */
const changePassword = async () => {
  if (!securityFormRef.value) return
  
  await securityFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const response = await userApi.changePassword({
          currentPassword: securityForm.currentPassword,
          newPassword: securityForm.newPassword
        })
        
        if (response.code === 200) {
          ElMessage.success('密码修改成功')
          // 重置表单
          securityFormRef.value?.resetFields()
        } else {
          ElMessage.error(response.message || '密码修改失败')
        }
      } catch (error) {
        ElMessage.error('密码修改失败')
      }
    }
  })
}

/**
 * 保存通知设置
 */
const saveNotificationSettings = async () => {
  try {
    // 这里应该调用API保存通知设置
    // await userApi.updateNotificationSettings(notificationForm)
    ElMessage.success('通知设置保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

/**
 * 保存隐私设置
 */
const savePrivacySettings = async () => {
  try {
    // 这里应该调用API保存隐私设置
    // await userApi.updatePrivacySettings(privacyForm)
    ElMessage.success('隐私设置保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

/**
 * 确认删除账户
 */
const confirmDeleteAccount = async () => {
  if (!deleteFormRef.value) return
  
  await deleteFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await ElMessageBox.confirm(
          '确定要删除账户吗？此操作不可逆！',
          '最终确认',
          {
            confirmButtonText: '确定删除',
            cancelButtonText: '取消',
            type: 'error'
          }
        )
        
        // 这里应该调用API删除账户
        // await userApi.deleteAccount(deleteForm.password)
        
        ElMessage.success('账户删除成功')
        showDeleteDialog.value = false
        
        // 跳转到登录页
        // router.push('/login')
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('删除失败')
        }
      }
    }
  })
}

/**
 * 处理删除对话框关闭
 */
const handleDeleteDialogClose = () => {
  deleteFormRef.value?.resetFields()
  showDeleteDialog.value = false
}

onMounted(() => {
  getUserSettings()
})
</script>

<style scoped>
.settings-container {
  padding: 20px;
}

.settings-card {
  max-width: 800px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}

.settings-tabs {
  margin-top: 20px;
}

.setting-desc {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.account-management {
  padding: 20px 0;
}

.danger-zone {
  margin-top: 30px;
  padding: 20px;
  border: 1px solid #f56c6c;
  border-radius: 4px;
  background-color: #fef0f0;
}

.delete-confirm {
  text-align: left;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>