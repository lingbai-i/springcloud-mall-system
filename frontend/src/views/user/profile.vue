<template>
  <div class="profile-container">
    <!-- 返回按钮 -->
    <div class="page-header">
      <el-button type="text" @click="goBack" class="back-btn">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
    </div>

    <!-- 标签页 -->
    <el-tabs v-model="activeTab" class="profile-tabs">
      <!-- 头像设置 -->
      <el-tab-pane label="头像设置" name="avatar">
        <div class="avatar-section">
          <div class="avatar-preview">
            <el-avatar :size="150" :src="previewAvatar || userForm.avatar" />
          </div>
          <div class="avatar-upload">
            <input 
              type="file" 
              ref="fileInput" 
              accept="image/*" 
              style="display: none" 
              @change="handleFileSelect"
            />
            <el-button type="primary" @click="selectAvatar">选择头像</el-button>
            <el-button 
              type="success" 
              @click="saveAvatar" 
              :loading="uploadLoading"
              :disabled="!pendingFile"
            >
              保存头像
            </el-button>
            <div class="upload-tips">
              <p>支持 JPG、PNG 格式，文件大小不超过 2MB</p>
              <p v-if="pendingFile" class="file-info">已选择：{{ pendingFile.name }}</p>
            </div>
          </div>
        </div>
      </el-tab-pane>
      
      <!-- 基本信息 -->
      <el-tab-pane label="基本信息" name="basic">
        <el-form
          ref="formRef"
          :model="userForm"
          :rules="rules"
          label-width="100px"
          class="profile-form"
        >
          <!-- 昵称 -->
          <el-form-item label="昵称" prop="nickname" required>
            <el-input
              v-model="userForm.nickname"
              placeholder="请输入昵称"
              clearable
            />
          </el-form-item>

          <!-- 性别 -->
          <el-form-item label="性别">
            <el-radio-group v-model="userForm.gender">
              <el-radio :label="1">男</el-radio>
              <el-radio :label="2">女</el-radio>
              <el-radio :label="0">保密</el-radio>
            </el-radio-group>
          </el-form-item>

          <!-- 生日 -->
          <el-form-item label="生日">
            <el-date-picker
              v-model="userForm.birthday"
              type="date"
              placeholder="请选择"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              style="width: 100%"
            />
          </el-form-item>

          <!-- 邮箱 -->
          <el-form-item label="邮箱" prop="email">
            <el-input
              v-model="userForm.email"
              placeholder="请输入邮箱"
              clearable
            />
          </el-form-item>

          <!-- 手机号 -->
          <el-form-item label="手机号" prop="phone">
            <el-input
              v-model="userForm.phone"
              placeholder="请输入手机号"
              clearable
            />
          </el-form-item>

          <!-- 个人简介 -->
          <el-form-item label="个人简介">
            <el-input
              v-model="userForm.bio"
              type="textarea"
              :rows="4"
              placeholder="请输入个人简介"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>

          <!-- 提交按钮 -->
          <el-form-item>
            <el-button type="success" @click="updateProfile" :loading="loading">
              提交
            </el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type UploadProps, type UploadInstance } from 'element-plus'
import { Plus, ArrowLeft } from '@element-plus/icons-vue'
import { updateUserProfile, uploadAvatar } from '@/api/user'
import { useUserStore } from '@/stores/user'
import * as logger from '@/utils/logger'
import axios from 'axios'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const uploadRef = ref<UploadInstance>()
const fileInput = ref<HTMLInputElement>()
const activeTab = ref('avatar') // 默认显示头像标签页
const loading = ref(false)
const uploadLoading = ref(false)
const previewAvatar = ref('')
const uploadUrl = ref('/api/users/upload-avatar')
const pendingFile = ref<File | null>(null)

/**
 * 返回上一页
 */
const goBack = () => {
  router.back()
}

// 用户表单数据
const userForm = reactive({
  nickname: '',
  email: '',
  phone: '',
  gender: 0,
  birthday: '',
  avatar: '',
  bio: ''
})

// 表单验证规则
const rules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '昵称长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

/**
 * 获取用户信息
 */
const loadUserInfo = async () => {
  try {
    // 先从后端拉取最新的用户信息
    try {
      await userStore.fetchUserInfo()
      console.log('已从后端刷新用户信息')
    } catch (err) {
      console.warn('从后端获取用户信息失败，使用缓存数据:', err)
    }
    
    // 从 store 读取用户信息
    if (userStore.userInfo) {
      Object.assign(userForm, {
        nickname: userStore.userInfo.nickname || userStore.userInfo.username || '',
        email: userStore.userInfo.email || '',
        phone: userStore.userInfo.phone || '',
        gender: userStore.userInfo.gender ?? 0,
        birthday: userStore.userInfo.birthday || '',
        avatar: userStore.userInfo.avatar || '',
        bio: userStore.userInfo.bio || ''
      })
      previewAvatar.value = userForm.avatar
      console.log('用户信息已加载，头像长度:', userForm.avatar?.length || 0)
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    ElMessage.error('获取用户信息失败')
  }
}

/**
 * 更新用户资料
 */
const updateProfile = async () => {
  if (!formRef.value) return
  
  // 调试：检查token状态
  console.log('当前token:', userStore.token)
  console.log('是否登录:', userStore.isLoggedIn)
  
  if (!userStore.token) {
    ElMessage.error('未登录或登录已过期，请重新登录')
    router.push('/home')
    return
  }
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        loading.value = true
        // 添加 username 字段
        const response = await updateUserProfile({
          ...userForm,
          username: userStore.userInfo?.username
        })
        if (response.success || response.code === 200) {
          ElMessage.success('更新成功')
          // 更新store中的用户信息
          userStore.updateUserInfo(userForm)
          // 刷新用户信息以确保后端数据同步
          try {
            await userStore.fetchUserInfo()
          } catch (err) {
            console.warn('刷新用户信息失败:', err)
          }
        } else {
          ElMessage.error(response.message || '更新失败')
        }
      } catch (error) {
        console.error('更新失败:', error)
        ElMessage.error('更新失败')
      } finally {
        loading.value = false
      }
    }
  })
}

/**
 * 重置表单
 */
const resetForm = () => {
  if (!formRef.value) return
  formRef.value.resetFields()
  loadUserInfo()
}

/**
 * 点击选择头像按钮
 */
const selectAvatar = () => {
  fileInput.value?.click()
}

/**
 * 文件选择回调
 */
const handleFileSelect = (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  
  if (!file) return
  
  // 验证文件类型
  const isImage = file.type === 'image/jpeg' || file.type === 'image/png'
  if (!isImage) {
    ElMessage.error('头像图片只能是 JPG/PNG 格式!')
    return
  }
  
  // 验证文件大小
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    ElMessage.error('头像图片大小不能超过 2MB!')
    return
  }
  
  // 保存文件
  pendingFile.value = file
  
  // 生成预览图
  const reader = new FileReader()
  reader.onload = (e) => {
    previewAvatar.value = e.target?.result as string
    console.log('已选择头像，预览图长度:', previewAvatar.value?.length)
  }
  reader.readAsDataURL(file)
}

/**
 * 头像上传成功回调
 */
const handleAvatarSuccess: UploadProps['onSuccess'] = (response) => {
  if (response.code === 200) {
    userForm.avatar = response.data
    previewAvatar.value = response.data
    ElMessage.success('头像上传成功')
  } else {
    ElMessage.error('头像上传失败')
  }
}

/**
 * 头像上传前验证
 */
const beforeAvatarUpload: UploadProps['beforeUpload'] = (rawFile) => {
  const isJPG = rawFile.type === 'image/jpeg' || rawFile.type === 'image/png'
  const isLt2M = rawFile.size / 1024 / 1024 < 2

  if (!isJPG) {
    ElMessage.error('头像图片只能是 JPG/PNG 格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('头像图片大小不能超过 2MB!')
    return false
  }
  
  // 保存待上传文件
  pendingFile.value = rawFile
  
  // 预览
  const reader = new FileReader()
  reader.onload = (e) => {
    previewAvatar.value = e.target?.result as string
  }
  reader.readAsDataURL(rawFile)
  
  return false // 阻止自动上传
}

/**
 * 保存头像（MinIO 文件上传）
 */
const saveAvatar = async () => {
  if (!pendingFile.value) {
    ElMessage.warning('请先选择头像')
    return
  }

  try {
    uploadLoading.value = true
    logger.info('=== 开始上传头像到 MinIO ===')
    logger.info('文件名:', pendingFile.value.name)
    logger.info('文件大小:', pendingFile.value.size, 'bytes')
    logger.info('文件类型:', pendingFile.value.type)
    logger.info('当前用户:', userStore.userInfo?.username)
    
    // 使用 FormData 上传文件
    const formData = new FormData()
    formData.append('file', pendingFile.value)
    
    // 调用新的上传接口
    const response = await axios.post('/api/users/upload-avatar', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
        'Authorization': `Bearer ${userStore.token}`
      }
    })
    
    logger.info('上传响应:', response.data)
    
    if (response.data.success) {
      const avatarUrl = response.data.data
      logger.info('✓ 头像上传成功')
      logger.info('新头像URL:', avatarUrl)
      
      // 更新本地数据
      userForm.avatar = avatarUrl
      previewAvatar.value = avatarUrl
      
      // 更新 store
      userStore.updateUserInfo({ avatar: avatarUrl })
      logger.info('✓ 已更新 store')
      
      // 强制更新 localStorage
      const currentUserInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      currentUserInfo.avatar = avatarUrl
      localStorage.setItem('userInfo', JSON.stringify(currentUserInfo))
      logger.info('✓ 已更新 localStorage')
      
      // 从后端刷新
      await userStore.fetchUserInfo()
      logger.info('✓ 后端数据已刷新')
      
      ElMessage.success('头像更新成功')
      
      // 清空文件选择
      pendingFile.value = null
      if (fileInput.value) {
        fileInput.value.value = ''
      }
      
      logger.info('=== 头像更新完成 ===')
    } else {
      logger.error('✗ 头像上传失败:', response.data.message)
      ElMessage.error(response.data.message || '头像上传失败')
    }
    
  } catch (error: any) {
    logger.error('✗ 头像上传异常:', error)
    if (error.response) {
      logger.error('响应状态:', error.response.status)
      logger.error('响应数据:', error.response.data)
    }
    ElMessage.error('头像上传失败，请重试')
  } finally {
    uploadLoading.value = false
  }
}
/**
 * 取消上传
 */
const cancelUpload = () => {
  previewAvatar.value = userForm.avatar
  pendingFile.value = null
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.profile-container {
  padding: 20px;
  background: #fff;
}

.page-header {
  margin-bottom: 20px;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 14px;
  color: #409EFF;
  padding: 0;
}

.back-btn:hover {
  color: #66b1ff;
}

.profile-tabs {
  max-width: 800px;
}

.profile-tabs :deep(.el-tabs__header) {
  margin-bottom: 30px;
  border-bottom: 2px solid #e4e7ed;
}

.profile-tabs :deep(.el-tabs__item) {
  font-size: 16px;
  padding: 0 30px;
  height: 50px;
  line-height: 50px;
}

.profile-tabs :deep(.el-tabs__item.is-active) {
  color: #67c23a;
  font-weight: 600;
}

.profile-tabs :deep(.el-tabs__active-bar) {
  background-color: #67c23a;
}

.profile-form {
  max-width: 600px;
  padding: 20px 0;
}

.profile-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #303133;
}

.profile-form :deep(.el-form-item.is-required .el-form-item__label)::before {
  content: '*';
  color: #f56c6c;
  margin-right: 4px;
}

.profile-form :deep(.el-input) {
  max-width: 400px;
}

.profile-form :deep(.el-date-picker) {
  max-width: 400px;
}

.profile-form :deep(.el-textarea) {
  max-width: 500px;
}

.profile-form :deep(.el-button) {
  min-width: 100px;
}

/* 头像设置区域 */
.avatar-section {
  max-width: 600px;
  padding: 30px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 30px;
}

.avatar-preview {
  text-align: center;
}

.avatar-upload {
  text-align: center;
  width: 100%;
  max-width: 400px;
}

.avatar-upload .el-button {
  margin: 0 5px;
  min-width: 100px;
}

.upload-tips {
  margin-top: 15px;
  font-size: 12px;
  color: #909399;
  line-height: 1.8;
}

.upload-tips p {
  margin: 5px 0;
}

.file-info {
  color: #67c23a;
  font-weight: 500;
}

/* 头像上传区域 */
.avatar-upload-section {
  max-width: 600px;
  padding: 20px 0;
}

.current-avatar {
  text-align: center;
  margin-bottom: 20px;
}

.current-avatar h3 {
  margin-bottom: 20px;
  font-size: 16px;
  color: #303133;
  font-weight: 500;
}

.upload-area {
  text-align: center;
}

.upload-area h3 {
  margin-bottom: 20px;
  font-size: 16px;
  color: #303133;
  font-weight: 500;
}

.avatar-uploader {
  display: inline-block;
}

.avatar-uploader :deep(.el-upload) {
  border: 2px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: 0.3s;
  width: 180px;
  height: 180px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-uploader :deep(.el-upload:hover) {
  border-color: #67c23a;
}

.avatar-preview {
  width: 180px;
  height: 180px;
  object-fit: cover;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #8c939d;
}

.upload-text {
  margin-top: 10px;
  font-size: 14px;
}

.upload-tips {
  margin-top: 15px;
  font-size: 12px;
  color: #909399;
  line-height: 1.8;
}

.upload-tips p {
  margin: 0;
}

.avatar-actions {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  gap: 10px;
}

.avatar-actions .el-button {
  min-width: 100px;
}

@media (max-width: 768px) {
  .profile-container {
    padding: 10px;
  }
  
  .profile-form,
  .avatar-upload-section {
    max-width: 100%;
  }
  
  .profile-form :deep(.el-input),
  .profile-form :deep(.el-date-picker),
  .profile-form :deep(.el-textarea) {
    max-width: 100%;
  }
}
</style>