<!--
  用户中心主页面
  
  @author lingbai
  @version 1.0
  @since 2025-10-21
-->
<template>
  <div class="user-center">
    <!-- 用户信息卡片 -->
    <el-card class="user-info-card" shadow="hover">
      <div class="user-header">
        <div class="avatar-section">
          <el-avatar :size="80" :src="userInfo.avatar" class="user-avatar">
            {{ userInfo.nickname?.charAt(0) || 'U' }}
          </el-avatar>
          <el-button 
            type="text" 
            class="change-avatar-btn"
            @click="showAvatarUpload = true"
          >
            更换头像
          </el-button>
        </div>
        <div class="user-details">
          <h2 class="username">{{ userInfo.nickname || userInfo.username }}</h2>
          <p class="user-meta">
            <el-tag v-if="userInfo.gender" :type="getGenderTagType(userInfo.gender)">
              {{ getGenderText(userInfo.gender) }}
            </el-tag>
            <span v-if="userInfo.birthday" class="birthday">
              <LocalIcon name="rili" :size="16" />
              {{ userInfo.birthday }}
            </span>
          </p>
          <p v-if="userInfo.bio" class="user-bio">{{ userInfo.bio }}</p>
          <div class="user-stats">
            <div class="stat-item">
              <span class="stat-label">注册时间</span>
              <span class="stat-value">{{ formatDate(userInfo.createTime) }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">最后登录</span>
              <span class="stat-value">{{ formatDate(userInfo.lastLoginTime) }}</span>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 功能菜单 -->
    <div class="menu-grid">
      <el-card 
        v-for="menu in menuList" 
        :key="menu.key"
        class="menu-card"
        shadow="hover"
        @click="handleMenuClick(menu)"
      >
        <div class="menu-content">
          <LocalIcon :name="menu.icon" :size="32" :color="menu.color" />
          <h3>{{ menu.title }}</h3>
          <p>{{ menu.description }}</p>
        </div>
      </el-card>
    </div>

    <!-- 头像上传对话框 -->
    <el-dialog
      v-model="showAvatarUpload"
      title="更换头像"
      width="400px"
      :before-close="handleAvatarDialogClose"
    >
      <el-upload
        class="avatar-uploader"
        action="#"
        :show-file-list="false"
        :before-upload="beforeAvatarUpload"
        :http-request="handleAvatarUpload"
        accept="image/*"
      >
        <img v-if="previewAvatar" :src="previewAvatar" class="avatar-preview" />
        <LocalIcon v-else name="tianjia" :size="32" class="avatar-uploader-icon" />
        <div class="upload-tip">
          <p>点击上传头像</p>
          <p class="tip-text">支持 JPG、PNG 格式，文件大小不超过 2MB</p>
        </div>
      </el-upload>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showAvatarUpload = false">取消</el-button>
          <el-button type="primary" @click="confirmAvatarUpload" :loading="uploading">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import LocalIcon from '@/components/LocalIcon.vue'

/**
 * 用户中心主页面组件
 * 
 * 功能包括：
 * - 显示用户基本信息
 * - 提供功能菜单导航
 * - 头像上传功能
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-10-21
 */

const router = useRouter()
const userStore = useUserStore()

// 响应式数据
const userInfo = computed(() => userStore.userInfo || {})
const showAvatarUpload = ref(false)
const previewAvatar = ref('')
const uploading = ref(false)

// 功能菜单配置
const menuList = reactive([
  {
    key: 'profile',
    title: '个人资料',
    description: '编辑个人信息',
    icon: 'wo',
    color: '#409EFF',
    path: '/user/profile'
  },
  {
    key: 'security',
    title: '账户安全',
    description: '密码和安全设置',
    icon: 'anquan',
    color: '#F56C6C',
    path: '/user/security'
  },
  {
    key: 'orders',
    title: '我的订单',
    description: '查看订单状态',
    icon: 'quanbudingdan',
    color: '#E6A23C',
    path: '/user/orders'
  },
  {
    key: 'addresses',
    title: '收货地址',
    description: '管理收货地址',
    icon: 'dizhi',
    color: '#67C23A',
    path: '/user/addresses'
  },
  {
    key: 'favorites',
    title: '我的收藏',
    description: '收藏的商品',
    icon: 'shoucang',
    color: '#FF6B6B',
    path: '/user/favorites'
  },
  {
    key: 'settings',
    title: '系统设置',
    description: '个性化设置',
    icon: 'shezhi',
    color: '#9C88FF',
    path: '/user/settings'
  }
])

/**
 * 组件挂载时初始化
 */
onMounted(async () => {
  // 如果用户信息为空，尝试获取
  if (!userStore.userInfo) {
    try {
      await userStore.fetchUserInfo()
    } catch (error) {
      console.error('获取用户信息失败:', error)
      ElMessage.error('获取用户信息失败')
    }
  }
})

/**
 * 处理菜单点击
 */
const handleMenuClick = (menu) => {
  if (menu.path) {
    router.push(menu.path)
  }
}

/**
 * 获取性别标签类型
 */
const getGenderTagType = (gender) => {
  switch (gender) {
    case 1: return 'primary'
    case 2: return 'danger'
    default: return 'info'
  }
}

/**
 * 获取性别文本
 */
const getGenderText = (gender) => {
  switch (gender) {
    case 1: return '男'
    case 2: return '女'
    default: return '未知'
  }
}

/**
 * 格式化日期
 */
const formatDate = (dateStr) => {
  if (!dateStr) return '暂无'
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

/**
 * 头像上传前验证
 */
const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }

  // 预览图片
  const reader = new FileReader()
  reader.onload = (e) => {
    previewAvatar.value = e.target.result
  }
  reader.readAsDataURL(file)

  return false // 阻止自动上传
}

/**
 * 处理头像上传
 */
const handleAvatarUpload = async (options) => {
  // 这里暂时不实际上传，只是模拟
  console.log('上传文件:', options.file)
}

/**
 * 确认头像上传
 */
const confirmAvatarUpload = async () => {
  if (!previewAvatar.value) {
    ElMessage.warning('请先选择头像')
    return
  }

  try {
    uploading.value = true
    
    // TODO: 实际的头像上传逻辑
    // 这里模拟上传成功
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    // 更新用户头像
    await userStore.updateUserInfo({
      avatar: previewAvatar.value
    })
    
    ElMessage.success('头像更新成功')
    showAvatarUpload.value = false
    previewAvatar.value = ''
  } catch (error) {
    console.error('头像上传失败:', error)
    ElMessage.error('头像上传失败')
  } finally {
    uploading.value = false
  }
}

/**
 * 头像对话框关闭处理
 */
const handleAvatarDialogClose = () => {
  previewAvatar.value = ''
  showAvatarUpload.value = false
}
</script>

<style scoped>
.user-center {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.user-info-card {
  margin-bottom: 30px;
}

.user-header {
  display: flex;
  align-items: flex-start;
  gap: 20px;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.user-avatar {
  border: 3px solid #f0f0f0;
}

.change-avatar-btn {
  font-size: 12px;
  color: #409EFF;
}

.user-details {
  flex: 1;
}

.username {
  margin: 0 0 10px 0;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.user-meta {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 10px;
}

.birthday {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #606266;
  font-size: 14px;
}

.user-bio {
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
  margin-bottom: 15px;
}

.user-stats {
  display: flex;
  gap: 30px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.stat-label {
  font-size: 12px;
  color: #909399;
}

.stat-value {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

.menu-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
}

.menu-card {
  cursor: pointer;
  transition: all 0.3s ease;
}

.menu-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.menu-content {
  text-align: center;
  padding: 20px;
}

.menu-content h3 {
  margin: 15px 0 10px 0;
  color: #303133;
  font-size: 18px;
  font-weight: 600;
}

.menu-content p {
  color: #606266;
  font-size: 14px;
  margin: 0;
}

.avatar-uploader {
  display: flex;
  justify-content: center;
}

.avatar-uploader :deep(.el-upload) {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: 0.2s;
  width: 200px;
  height: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.avatar-uploader :deep(.el-upload:hover) {
  border-color: #409EFF;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  margin-bottom: 10px;
}

.avatar-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.upload-tip {
  text-align: center;
}

.upload-tip p {
  margin: 5px 0;
  color: #606266;
}

.tip-text {
  font-size: 12px;
  color: #909399;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .user-center {
    padding: 15px;
  }
  
  .user-header {
    flex-direction: column;
    text-align: center;
  }
  
  .menu-grid {
    grid-template-columns: 1fr;
  }
  
  .user-stats {
    justify-content: center;
  }
}
</style>