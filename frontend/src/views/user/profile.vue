<template>
  <div class="profile-container">
    <!-- 返回按钮 -->
    <div class="page-header">
      <el-button type="text" @click="goBack" class="back-btn">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h2 class="page-title">个人资料</h2>
    </div>

    <div class="profile-content">
      <!-- 左侧：头像区域 -->
      <div class="avatar-section">
        <div class="section-title">头像</div>
        
        <!-- 当前头像 -->
        <div class="avatar-display">
          <div class="avatar-box" :class="{ 'has-preview': previewUrl }">
            <img v-if="previewUrl" :src="previewUrl" class="avatar-img" />
            <img v-else-if="userForm.avatar" :src="userForm.avatar" class="avatar-img" />
            <el-icon v-else class="avatar-placeholder"><User /></el-icon>
          </div>
          <div v-if="previewUrl" class="preview-badge">预览</div>
        </div>

        <!-- 选择图片按钮 -->
        <div class="avatar-actions">
          <input
            ref="fileInputRef"
            type="file"
            accept="image/jpeg,image/png,image/gif,image/webp"
            style="display: none"
            @change="handleFileSelect"
          />
          <el-button type="primary" @click="triggerFileSelect" size="small">
            <el-icon><Picture /></el-icon>
            选择图片
          </el-button>
          
          <el-button 
            v-if="selectedFile" 
            type="success" 
            @click="submitAvatar" 
            :loading="uploadLoading"
            size="small"
          >
            {{ uploadLoading ? '上传中...' : '提交头像' }}
          </el-button>

          <el-button v-if="selectedFile" @click="cancelSelect" size="small">
            取消
          </el-button>
        </div>

        <!-- 提示信息 -->
        <div class="avatar-tips">
          <p>支持 JPG、PNG、GIF、WebP 格式</p>
          <p>文件大小不超过 2MB</p>
        </div>
      </div>

      <!-- 右侧：基本信息表单 -->
      <div class="info-section">
        <div class="section-title">基本信息</div>
        
        <el-form
          ref="formRef"
          :model="userForm"
          :rules="rules"
          label-width="80px"
          class="profile-form"
        >
          <el-form-item label="昵称" prop="nickname" required>
            <el-input v-model="userForm.nickname" placeholder="请输入昵称" clearable />
          </el-form-item>

          <el-form-item label="性别">
            <el-radio-group v-model="userForm.gender">
              <el-radio :label="1">男</el-radio>
              <el-radio :label="2">女</el-radio>
              <el-radio :label="0">保密</el-radio>
            </el-radio-group>
          </el-form-item>

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

          <el-form-item label="邮箱" prop="email">
            <el-input v-model="userForm.email" placeholder="请输入邮箱" clearable />
          </el-form-item>

          <el-form-item label="手机号" prop="phone">
            <el-input v-model="userForm.phone" placeholder="请输入手机号" clearable />
          </el-form-item>

          <el-form-item label="个人简介">
            <el-input
              v-model="userForm.bio"
              type="textarea"
              :rows="3"
              placeholder="请输入个人简介"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>

          <el-form-item>
            <el-button type="success" @click="updateProfile" :loading="loading">
              保存信息
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance } from 'element-plus'
import { ArrowLeft, User, Picture } from '@element-plus/icons-vue'
import { updateUserProfile, uploadAvatarToMinio } from '@/api/user'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const fileInputRef = ref<HTMLInputElement>()
const loading = ref(false)
const uploadLoading = ref(false)

// 头像相关
const selectedFile = ref<File | null>(null)
const previewUrl = ref('')

const goBack = () => router.back()

const userForm = reactive({
  nickname: '',
  email: '',
  phone: '',
  gender: 0,
  birthday: '',
  avatar: '',
  bio: ''
})

const rules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '昵称长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  email: [{ type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }]
}

const loadUserInfo = async () => {
  try {
    try {
      await userStore.fetchUserInfo()
    } catch (err) {
      console.warn('从后端获取用户信息失败:', err)
    }
    
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
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    ElMessage.error('获取用户信息失败')
  }
}

// 更新基本信息
const updateProfile = async () => {
  if (!formRef.value) return
  
  if (!userStore.token) {
    ElMessage.error('未登录或登录已过期，请重新登录')
    router.push('/home')
    return
  }
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        loading.value = true
        const response = await updateUserProfile({
          ...userForm,
          username: userStore.userInfo?.username
        })
        if (response.success || response.code === 200) {
          ElMessage.success('信息更新成功')
          userStore.updateUserInfo(userForm)
          await userStore.fetchUserInfo()
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

// 触发文件选择
const triggerFileSelect = () => {
  fileInputRef.value?.click()
}

// 处理文件选择
const handleFileSelect = (event: Event) => {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  
  if (!file) return
  
  const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  if (!allowedTypes.includes(file.type)) {
    ElMessage.error('请选择 JPG、PNG、GIF 或 WebP 格式的图片')
    return
  }
  
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过 2MB')
    return
  }
  
  selectedFile.value = file
  
  const reader = new FileReader()
  reader.onload = (e) => {
    previewUrl.value = e.target?.result as string
  }
  reader.readAsDataURL(file)
  
  input.value = ''
}

// 取消选择
const cancelSelect = () => {
  selectedFile.value = null
  previewUrl.value = ''
}

// 提交头像
const submitAvatar = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择图片')
    return
  }
  
  try {
    uploadLoading.value = true
    
    const response = await uploadAvatarToMinio(selectedFile.value)
    
    if (response.success || response.code === 200) {
      const avatarUrl = response.data
      userForm.avatar = avatarUrl
      userStore.updateUserInfo({ ...userForm, avatar: avatarUrl })
      await userStore.fetchUserInfo()
      
      selectedFile.value = null
      previewUrl.value = ''
      
      ElMessage.success('头像更换成功')
    } else {
      ElMessage.error(response.message || '头像上传失败')
    }
  } catch (error: any) {
    console.error('上传头像失败:', error)
    ElMessage.error(error.message || '头像上传失败，请稍后重试')
  } finally {
    uploadLoading.value = false
  }
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.profile-container {
  padding: 20px;
  background: #fff;
  min-height: 100vh;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 14px;
  color: #409EFF;
  padding: 0;
}

.page-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.profile-content {
  display: flex;
  gap: 40px;
  max-width: 900px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 2px solid #67c23a;
}

/* 头像区域 */
.avatar-section {
  width: 200px;
  flex-shrink: 0;
}

.avatar-display {
  position: relative;
  display: flex;
  justify-content: center;
  margin-bottom: 16px;
}

.avatar-box {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  overflow: hidden;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 3px solid #e4e7ed;
  transition: border-color 0.3s;
}

.avatar-box.has-preview {
  border-color: #67c23a;
  box-shadow: 0 0 12px rgba(103, 194, 58, 0.3);
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  font-size: 48px;
  color: #c0c4cc;
}

.preview-badge {
  position: absolute;
  bottom: -5px;
  left: 50%;
  transform: translateX(-50%);
  background: #67c23a;
  color: #fff;
  font-size: 12px;
  padding: 2px 10px;
  border-radius: 10px;
}

.avatar-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: center;
}

.avatar-tips {
  margin-top: 16px;
  color: #909399;
  font-size: 12px;
  text-align: center;
}

.avatar-tips p {
  margin: 4px 0;
}

/* 信息区域 */
.info-section {
  flex: 1;
  max-width: 500px;
}

.profile-form {
  padding-top: 10px;
}

.profile-form :deep(.el-input),
.profile-form :deep(.el-textarea) {
  max-width: 350px;
}

.profile-form :deep(.el-form-item) {
  margin-bottom: 18px;
}

/* 响应式 */
@media (max-width: 768px) {
  .profile-content {
    flex-direction: column;
    gap: 30px;
  }
  
  .avatar-section {
    width: 100%;
  }
  
  .avatar-actions {
    flex-direction: row;
    flex-wrap: wrap;
    justify-content: center;
  }
}
</style>
