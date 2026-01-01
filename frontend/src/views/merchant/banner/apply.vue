<template>
  <div class="banner-apply-page">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>{{ isEdit ? '编辑轮播图申请' : '提交轮播图投流申请' }}</span>
          <el-button text @click="goBack">
            <el-icon><ArrowLeft /></el-icon>
            返回列表
          </el-button>
        </div>
      </template>
      
      <el-row :gutter="40">
        <!-- 左侧表单 -->
        <el-col :xs="24" :lg="12">
          <el-form
            ref="formRef"
            :model="formData"
            :rules="formRules"
            label-width="100px"
            label-position="top"
          >
            <!-- 轮播图图片 -->
            <el-form-item label="轮播图图片" prop="imageUrl" required>
              <div class="upload-container">
                <!-- 隐藏的文件输入 -->
                <input
                  ref="fileInputRef"
                  type="file"
                  accept="image/jpeg,image/png,image/gif,image/webp"
                  style="display: none;"
                  @change="handleFileSelect"
                />
                
                <div class="banner-uploader" @click="triggerFileSelect">
                  <div v-if="formData.imageUrl" class="uploaded-image">
                    <img :src="formData.imageUrl" alt="轮播图" />
                    <div class="image-actions">
                      <el-button type="primary" size="small" @click.stop="triggerFileSelect">
                        重新上传
                      </el-button>
                    </div>
                  </div>
                  <div v-else class="upload-placeholder">
                    <el-icon v-if="!uploading" :size="40"><Plus /></el-icon>
                    <el-progress
                      v-else
                      type="circle"
                      :percentage="uploadProgress"
                      :width="60"
                    />
                    <span v-if="!uploading">点击上传轮播图</span>
                    <span v-else>上传中...</span>
                  </div>
                </div>
                
                <div class="upload-tip">
                  支持 JPG、PNG、GIF、WebP 格式，文件大小不超过 10MB<br/>
                  上传后可手动裁剪，最终输出 1920×600 标准尺寸
                </div>
              </div>
            </el-form-item>
            
            <!-- 标题 -->
            <el-form-item label="标题" prop="title">
              <el-input
                v-model="formData.title"
                placeholder="请输入轮播图标题"
                maxlength="100"
                show-word-limit
              />
            </el-form-item>
            
            <!-- 描述 -->
            <el-form-item label="描述" prop="description">
              <el-input
                v-model="formData.description"
                type="textarea"
                placeholder="请输入轮播图描述（选填）"
                maxlength="500"
                show-word-limit
                :rows="3"
              />
            </el-form-item>
            
            <!-- 跳转商品 -->
            <el-form-item label="跳转商品" prop="productId" required>
              <el-select
                v-model="formData.productId"
                placeholder="请选择点击轮播图后跳转的商品"
                style="width: 100%;"
                filterable
                :loading="loadingProducts"
                @visible-change="handleProductDropdownOpen"
              >
                <el-option
                  v-for="product in productList"
                  :key="product.id"
                  :label="product.productName"
                  :value="product.id"
                >
                  <div class="product-option">
                    <img :src="getFirstImage(product.mainImage || product.images)" class="product-thumb" />
                    <div class="product-info">
                      <span class="product-name">{{ product.productName }}</span>
                      <span class="product-price">¥{{ product.price }}</span>
                    </div>
                  </div>
                </el-option>
              </el-select>
              <div class="form-tip">用户点击轮播图后将跳转到所选商品的详情页</div>
            </el-form-item>
            
            <!-- 展示日期 -->
            <el-form-item label="展示日期" required>
              <el-row :gutter="16" style="width: 100%;">
                <el-col :span="12">
                  <el-form-item prop="startDate" style="margin-bottom: 0;">
                    <el-date-picker
                      v-model="formData.startDate"
                      type="date"
                      placeholder="开始日期"
                      :disabled-date="disableStartDate"
                      value-format="YYYY-MM-DD"
                      style="width: 100%;"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item prop="endDate" style="margin-bottom: 0;">
                    <el-date-picker
                      v-model="formData.endDate"
                      type="date"
                      placeholder="结束日期"
                      :disabled-date="disableEndDate"
                      value-format="YYYY-MM-DD"
                      style="width: 100%;"
                    />
                  </el-form-item>
                </el-col>
              </el-row>
              <div class="form-tip">轮播图将在开始日期自动上架，结束日期自动下架</div>
            </el-form-item>
            
            <!-- 提交按钮 -->
            <el-form-item>
              <el-button
                type="primary"
                :loading="submitting"
                @click="handleSubmit"
              >
                {{ isEdit ? '保存修改' : '提交申请' }}
              </el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>
        </el-col>
        
        <!-- 右侧预览 -->
        <el-col :xs="24" :lg="12">
          <div class="preview-section">
            <BannerPreview
              :image-url="formData.imageUrl"
              :title="formData.title"
              :container-width="previewWidth"
            />
            
            <!-- 申请信息摘要 -->
            <div class="application-summary" v-if="formData.startDate && formData.endDate">
              <h4>申请信息</h4>
              <el-descriptions :column="1" border size="small">
                <el-descriptions-item label="展示周期">
                  {{ formData.startDate }} 至 {{ formData.endDate }}
                  <el-tag size="small" style="margin-left: 8px;">
                    {{ displayDays }} 天
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="跳转链接">
                  {{ formData.targetUrl || '未设置' }}
                </el-descriptions-item>
              </el-descriptions>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>
    
    <!-- 图片裁剪对话框 -->
    <ImageCropper
      v-model="cropperVisible"
      :image-src="cropperImageSrc"
      :output-width="1920"
      :output-height="600"
      @crop="handleCropComplete"
      @cancel="handleCropCancel"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, ArrowLeft } from '@element-plus/icons-vue'
import BannerPreview from '@/components/BannerPreview.vue'
import ImageCropper from '@/components/ImageCropper.vue'
import { 
  submitBannerApplication, 
  getBannerApplicationDetail,
  updateBannerApplication 
} from '@/api/merchant/banner'
import { getProductList } from '@/api/merchant/product'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 是否编辑模式
const isEdit = computed(() => !!route.params.id)
const applicationId = computed(() => route.params.id)

// 表单引用
const formRef = ref(null)
const fileInputRef = ref(null)

// 上传状态
const uploading = ref(false)
const uploadProgress = ref(0)
const submitting = ref(false)

// 裁剪器状态
const cropperVisible = ref(false)
const cropperImageSrc = ref('')
const pendingFileName = ref('')

// 链接类型状态
const productList = ref([])
const loadingProducts = ref(false)

// 预览宽度
const previewWidth = ref(500)

// 表单数据
const formData = reactive({
  imageUrl: '',
  title: '',
  description: '',
  targetUrl: '',
  productId: '',
  startDate: '',
  endDate: ''
})

// 计算展示天数
const displayDays = computed(() => {
  if (!formData.startDate || !formData.endDate) return 0
  const start = new Date(formData.startDate)
  const end = new Date(formData.endDate)
  return Math.ceil((end - start) / (1000 * 60 * 60 * 24)) + 1
})

// URL验证正则（站内链接）
const internalPathPattern = /^\/[\w\-/]*$/

// 表单验证规则
const formRules = {
  imageUrl: [
    { required: true, message: '请上传轮播图图片', trigger: 'change' }
  ],
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { max: 100, message: '标题最多100个字符', trigger: 'blur' }
  ],
  description: [
    { max: 500, message: '描述最多500个字符', trigger: 'blur' }
  ],
  productId: [
    { required: true, message: '请选择跳转商品', trigger: 'change' }
  ],
  targetUrl: [
    { required: true, message: '请选择跳转链接', trigger: 'change' },
    {
      validator: (_, value, callback) => {
        if (!value) {
          callback(new Error('请选择跳转链接'))
        } else if (value === '/' || value.startsWith('/')) {
          callback()
        } else {
          callback(new Error('站内链接格式错误'))
        }
      },
      trigger: 'change'
    }
  ],
  startDate: [
    { required: true, message: '请选择开始日期', trigger: 'change' }
  ],
  endDate: [
    { required: true, message: '请选择结束日期', trigger: 'change' },
    {
      validator: (_, value, callback) => {
        if (value && formData.startDate && value < formData.startDate) {
          callback(new Error('结束日期不能早于开始日期'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}

/**
 * 获取第一张图片URL（处理逗号分隔的多图片URL）
 * @param imageUrl 图片URL字符串，可能包含逗号分隔的多个URL
 * @returns 第一张图片的URL
 */
const getFirstImage = (imageUrl) => {
  if (!imageUrl) return ''
  if (imageUrl.includes(',')) {
    return imageUrl.split(',')[0].trim()
  }
  return imageUrl
}

// 处理商品下拉框打开
const handleProductDropdownOpen = async (visible) => {
  if (visible && productList.value.length === 0) {
    await loadProducts()
  }
}

// 加载商品列表
const loadProducts = async () => {
  loadingProducts.value = true
  try {
    const merchantId = userStore.merchantId || userStore.userInfo?.merchantId || localStorage.getItem('merchantId')
    console.log('加载商品列表，merchantId:', merchantId)
    const response = await getProductList({ 
      merchantId,
      status: 1, // 只获取上架商品
      page: 1,
      size: 100 
    })
    console.log('商品列表响应:', response)
    if (response.success && response.data) {
      // 兼容分页和非分页响应
      const data = response.data
      productList.value = data.records || data.list || data.content || (Array.isArray(data) ? data : [])
      console.log('商品列表:', productList.value)
    } else if (response.code === 200 && response.data) {
      // 兼容 code: 200 格式
      const data = response.data
      productList.value = data.records || data.list || data.content || (Array.isArray(data) ? data : [])
      console.log('商品列表(code 200):', productList.value)
    }
  } catch (error) {
    console.error('加载商品列表失败:', error)
    ElMessage.error('加载商品列表失败')
  } finally {
    loadingProducts.value = false
  }
}

// 监听商品选择变化，更新 targetUrl
watch(() => formData.productId, (newProductId) => {
  if (newProductId) {
    formData.targetUrl = `/product/${newProductId}`
  } else {
    formData.targetUrl = ''
  }
})

// 禁用开始日期（今天之前的日期）
const disableStartDate = (date) => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return date.getTime() < today.getTime()
}

// 禁用结束日期（开始日期之前的日期）
const disableEndDate = (date) => {
  if (!formData.startDate) {
    return disableStartDate(date)
  }
  const startDate = new Date(formData.startDate)
  startDate.setHours(0, 0, 0, 0)
  return date.getTime() < startDate.getTime()
}

// 触发文件选择
const triggerFileSelect = () => {
  fileInputRef.value?.click()
}

// 处理文件选择
const handleFileSelect = async (event) => {
  const file = event.target.files?.[0]
  if (!file) return
  
  // 重置input以便可以选择相同文件
  event.target.value = ''
  
  // 验证文件类型
  const validTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  if (!validTypes.includes(file.type)) {
    ElMessage.error('仅支持 JPG、PNG、GIF、WebP 格式的图片')
    return
  }
  
  // 验证文件大小 (10MB)
  const maxSize = 10 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('图片大小不能超过 10MB')
    return
  }
  
  // 保存文件名
  pendingFileName.value = file.name
  
  // 读取文件并打开裁剪器
  const reader = new FileReader()
  reader.onload = (e) => {
    cropperImageSrc.value = e.target.result
    cropperVisible.value = true
  }
  reader.onerror = () => {
    ElMessage.error('读取图片失败，请重试')
  }
  reader.readAsDataURL(file)
}

// 裁剪完成
const handleCropComplete = async (blob) => {
  uploading.value = true
  uploadProgress.value = 0
  
  try {
    // 创建FormData
    const formDataObj = new FormData()
    const fileName = pendingFileName.value.replace(/\.[^.]+$/, '') + '_banner.jpg'
    formDataObj.append('file', blob, fileName)
    
    // 模拟上传进度
    const progressInterval = setInterval(() => {
      if (uploadProgress.value < 90) {
        uploadProgress.value += 10
      }
    }, 100)
    
    // 上传到服务器
    const response = await fetch('/api/product-service/files/upload', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`,
        'X-Merchant-Id': userStore.merchantId || userStore.userInfo?.merchantId || '1'
      },
      body: formDataObj
    })
    
    clearInterval(progressInterval)
    uploadProgress.value = 100
    
    const result = await response.json()
    
    if (result.success && result.data) {
      formData.imageUrl = result.data.url || result.data
      ElMessage.success('图片上传成功')
      formRef.value?.validateField('imageUrl')
    } else {
      ElMessage.error(result.message || '上传失败')
    }
  } catch (error) {
    console.error('Upload error:', error)
    ElMessage.error('图片上传失败，请重试')
  } finally {
    uploading.value = false
    uploadProgress.value = 0
    cropperImageSrc.value = ''
    pendingFileName.value = ''
  }
}

// 取消裁剪
const handleCropCancel = () => {
  cropperImageSrc.value = ''
  pendingFileName.value = ''
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    
    submitting.value = true
    
    const submitData = {
      imageUrl: formData.imageUrl,
      title: formData.title,
      description: formData.description || '',
      targetUrl: formData.targetUrl,  // 站内链接直接使用
      startDate: formData.startDate,
      endDate: formData.endDate
    }
    
    if (isEdit.value) {
      await updateBannerApplication(applicationId.value, submitData)
      ElMessage.success('申请修改成功')
    } else {
      await submitBannerApplication(submitData)
      ElMessage.success('申请提交成功，请等待审核')
    }
    
    router.push('/merchant/banner/list')
  } catch (error) {
    if (error !== false) {
      console.error('Submit error:', error)
      ElMessage.error(error.message || '提交失败，请重试')
    }
  } finally {
    submitting.value = false
  }
}

// 重置表单
const handleReset = () => {
  formRef.value?.resetFields()
  formData.imageUrl = ''
  formData.title = ''
  formData.description = ''
  formData.targetUrl = ''
  formData.productId = ''
  formData.startDate = ''
  formData.endDate = ''
}

// 返回列表
const goBack = () => {
  router.push('/merchant/banner/list')
}

// 加载申请详情（编辑模式）
const loadApplicationDetail = async () => {
  if (!isEdit.value) return
  
  try {
    const response = await getBannerApplicationDetail(applicationId.value)
    if (response.success && response.data) {
      const data = response.data
      formData.imageUrl = data.imageUrl
      formData.title = data.title
      formData.description = data.description || ''
      formData.targetUrl = data.targetUrl || ''
      
      // 从 targetUrl 解析 productId（兼容新旧格式）
      if (data.targetUrl) {
        if (data.targetUrl.startsWith('/product/detail/')) {
          formData.productId = data.targetUrl.replace('/product/detail/', '')
        } else if (data.targetUrl.startsWith('/product/')) {
          formData.productId = data.targetUrl.replace('/product/', '')
        }
      }
      
      formData.startDate = data.startDate
      formData.endDate = data.endDate
    }
  } catch (error) {
    console.error('Load detail error:', error)
    ElMessage.error('加载申请详情失败')
    router.push('/merchant/banner/list')
  }
}

onMounted(() => {
  loadApplicationDetail()
})
</script>

<style scoped>
.banner-apply-page {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.upload-container {
  width: 100%;
}

.banner-uploader {
  width: 100%;
  border: 1px dashed #d9d9d9;
  border-radius: 8px;
  cursor: pointer;
  transition: border-color 0.3s;
}

.banner-uploader:hover {
  border-color: #52c41a;
}

.upload-placeholder {
  width: 100%;
  height: 180px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
  gap: 8px;
}

.uploaded-image {
  width: 100%;
  height: 180px;
  position: relative;
  overflow: hidden;
  border-radius: 8px;
}

.uploaded-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-actions {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 8px;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.6));
  display: flex;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.uploaded-image:hover .image-actions {
  opacity: 1;
}

.upload-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
  line-height: 1.6;
}

.form-tip {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
}

.preview-section {
  position: sticky;
  top: 20px;
}

.application-summary {
  margin-top: 20px;
  padding: 16px;
  background-color: #fafafa;
  border-radius: 8px;
}

.application-summary h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #303133;
}

@media (max-width: 992px) {
  .preview-section {
    margin-top: 24px;
    position: static;
  }
}

.product-option {
  display: flex;
  align-items: center;
  padding: 4px 0;
}

.product-thumb {
  width: 40px;
  height: 40px;
  object-fit: cover;
  border-radius: 4px;
  margin-right: 10px;
  background-color: #f5f5f5;
}

.product-info {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-width: 0;
}

.product-name {
  font-size: 14px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-price {
  font-size: 12px;
  color: #f56c6c;
  margin-top: 2px;
}
</style>
