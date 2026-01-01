<template>
  <div class="review-page">
    <div class="page-header">
      <el-page-header @back="goBack" title="返回订单">
        <template #content>
          <span class="page-title">商品评价</span>
        </template>
      </el-page-header>
    </div>

    <div v-loading="loading" class="review-content">
      <!-- 订单商品列表 -->
      <div v-if="orderItems.length > 0" class="order-items">
        <div 
          v-for="(item, index) in orderItems" 
          :key="item.id" 
          class="review-item-card"
        >
          <!-- 商品信息 -->
          <div class="product-info">
            <div class="product-image">
              <img :src="item.image" :alt="item.name" />
            </div>
            <div class="product-detail">
              <h3 class="product-name">{{ item.name }}</h3>
              <p class="product-spec">{{ item.specification }}</p>
            </div>
            <div v-if="item.reviewed" class="reviewed-badge">
              <el-tag type="success">已评价</el-tag>
            </div>
          </div>

          <!-- 评价表单 -->
          <div v-if="!item.reviewed" class="review-form">
            <!-- 综合评分 -->
            <div class="rating-row">
              <StarRating 
                v-model="reviewForms[index].rating" 
                label="综合评分"
                :show-text="true"
              />
            </div>

            <!-- 描述相符 -->
            <div class="rating-row">
              <StarRating 
                v-model="reviewForms[index].descriptionRating" 
                label="描述相符"
                :show-text="true"
              />
            </div>

            <!-- 卖家服务 -->
            <div class="rating-row">
              <StarRating 
                v-model="reviewForms[index].serviceRating" 
                label="卖家服务"
                :show-text="true"
              />
            </div>

            <!-- 物流服务 -->
            <div class="rating-row">
              <StarRating 
                v-model="reviewForms[index].logisticsRating" 
                label="物流服务"
                :show-text="true"
              />
            </div>

            <!-- 评价内容 -->
            <div class="content-row">
              <label class="form-label">评价内容</label>
              <el-input
                v-model="reviewForms[index].content"
                type="textarea"
                :rows="4"
                placeholder="请分享您的购物体验（选填）"
                maxlength="500"
                show-word-limit
              />
            </div>

            <!-- 图片上传 -->
            <div class="upload-row">
              <label class="form-label">上传图片（最多9张）</label>
              <el-upload
                v-model:file-list="reviewForms[index].imageList"
                action="#"
                list-type="picture-card"
                :auto-upload="false"
                :limit="9"
                :on-exceed="handleExceed"
                :on-preview="handlePreview"
                :on-remove="(file) => handleRemove(file, index)"
                :before-upload="beforeUpload"
                accept="image/*"
              >
                <el-icon><Plus /></el-icon>
              </el-upload>
              <div class="upload-tip">支持jpg、png格式，单张不超过5MB</div>
            </div>

            <!-- 匿名评价 -->
            <div class="anonymous-row">
              <el-checkbox v-model="reviewForms[index].anonymous">
                匿名评价
              </el-checkbox>
              <span class="anonymous-tip">勾选后将以"匿名用户"身份发布评价</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <el-empty v-else-if="!loading" description="暂无待评价商品" />
    </div>

    <!-- 提交按钮 -->
    <div v-if="hasUnreviewedItems" class="submit-section">
      <el-button 
        type="primary" 
        size="large" 
        :loading="submitting"
        :disabled="!canSubmit"
        @click="submitReviews"
      >
        提交评价
      </el-button>
    </div>

    <!-- 图片预览 -->
    <el-dialog v-model="previewVisible" title="图片预览" width="600px">
      <img :src="previewUrl" alt="预览图片" style="width: 100%;" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import StarRating from '@/components/StarRating.vue'
import { addProductReview, checkReviewed } from '@/api/product'
import { uploadImage } from '@/api/upload'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 获取第一张图片URL（处理逗号分隔的多图片URL）
const getFirstImage = (imageUrl) => {
  if (!imageUrl) return ''
  if (imageUrl.includes(',')) {
    return imageUrl.split(',')[0].trim()
  }
  return imageUrl
}

// 状态
const loading = ref(false)
const submitting = ref(false)
const orderItems = ref([])
const reviewForms = ref([])
const previewVisible = ref(false)
const previewUrl = ref('')

// 订单ID
const orderId = computed(() => route.params.orderId)

// 是否有未评价的商品
const hasUnreviewedItems = computed(() => {
  return orderItems.value.some(item => !item.reviewed)
})

// 是否可以提交
const canSubmit = computed(() => {
  return reviewForms.value.some((form, index) => {
    return !orderItems.value[index]?.reviewed && form.rating > 0
  })
})

// 初始化评价表单
const initReviewForm = () => ({
  rating: 0,
  descriptionRating: 0,
  serviceRating: 0,
  logisticsRating: 0,
  content: '',
  imageList: [],
  anonymous: false
})

// 获取订单商品
const fetchOrderItems = async () => {
  loading.value = true
  try {
    const userId = userStore.userId || localStorage.getItem('userId')
    if (!userId) {
      ElMessage.warning('请先登录')
      router.push('/auth/login')
      return
    }

    // 获取订单详情 - 添加userId参数避免403错误
    const response = await fetch(`/api/order-service/orders/${orderId.value}?userId=${userId}`, {
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })

    if (!response.ok) {
      throw new Error('获取订单失败')
    }

    const result = await response.json()
    if (result.success && result.data) {
      const order = result.data
      
      // 检查订单状态
      if (order.status !== 'COMPLETED') {
        ElMessage.warning('只有已完成的订单才能评价')
        router.push('/user/orders')
        return
      }

      // 转换订单商品数据
      const items = (order.orderItems || []).map(item => ({
        id: item.id,
        productId: item.productId,
        name: item.productName,
        image: getFirstImage(item.productImage),
        specification: item.productSpec,
        reviewed: false
      }))

      // 检查每个商品是否已评价
      for (const item of items) {
        try {
          const checkResult = await checkReviewed(item.productId, orderId.value, userId)
          item.reviewed = checkResult.data === true
        } catch (e) {
          console.warn('检查评价状态失败:', e)
        }
      }

      orderItems.value = items
      reviewForms.value = items.map(() => initReviewForm())
    } else {
      throw new Error(result.message || '获取订单失败')
    }
  } catch (error) {
    console.error('获取订单商品失败:', error)
    ElMessage.error('获取订单信息失败')
  } finally {
    loading.value = false
  }
}

// 提交评价
const submitReviews = async () => {
  // 验证必填字段
  const unreviewedIndexes = orderItems.value
    .map((item, index) => ({ item, index }))
    .filter(({ item }) => !item.reviewed)

  const invalidItems = unreviewedIndexes.filter(({ index }) => {
    return reviewForms.value[index].rating === 0
  })

  if (invalidItems.length > 0) {
    ElMessage.warning('请为所有商品选择综合评分')
    return
  }

  submitting.value = true
  try {
    const userId = userStore.userId || localStorage.getItem('userId')
    // 优先使用昵称，其次用户名，最后使用默认值
    const userName = userStore.userInfo?.nickname || userStore.username || localStorage.getItem('username') || '用户'
    const userAvatar = userStore.avatar || localStorage.getItem('avatar') || ''

    let successCount = 0
    let failCount = 0

    for (const { item, index } of unreviewedIndexes) {
      const form = reviewForms.value[index]
      if (form.rating === 0) continue

      try {
        // 先上传图片到MinIO，获取永久URL
        const imageUrls = []
        for (const file of form.imageList) {
          if (file.url && file.url.startsWith('http')) {
            // 已经是URL，直接使用
            imageUrls.push(file.url)
          } else if (file.raw) {
            // 需要上传的文件
            try {
              const res = await uploadImage(file.raw)
              if (res.data?.url) {
                imageUrls.push(res.data.url)
              }
            } catch (uploadErr) {
              console.warn('图片上传失败:', uploadErr)
            }
          }
        }

        await addProductReview(item.productId, {
          orderId: Number(orderId.value),
          userId: Number(userId),
          userName: form.anonymous ? '匿名用户' : userName,
          userAvatar: form.anonymous ? '' : userAvatar,
          rating: form.rating,
          descriptionRating: form.descriptionRating || form.rating,
          serviceRating: form.serviceRating || form.rating,
          logisticsRating: form.logisticsRating || form.rating,
          content: form.content || '',
          images: imageUrls.join(','),
          anonymous: form.anonymous ? 1 : 0
        })
        successCount++
        item.reviewed = true
      } catch (e) {
        console.error('提交评价失败:', e)
        failCount++
      }
    }

    if (successCount > 0) {
      ElMessage.success(`成功提交 ${successCount} 条评价`)
      if (failCount === 0) {
        // 全部成功，关闭窗口或返回订单列表
        setTimeout(() => {
          if (window.opener) {
            window.close()
          } else {
            router.push('/user/orders')
          }
        }, 1500)
      }
    }
    if (failCount > 0) {
      ElMessage.warning(`${failCount} 条评价提交失败，请重试`)
    }
  } catch (error) {
    console.error('提交评价失败:', error)
    ElMessage.error('提交评价失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

// 图片上传相关
const handleExceed = () => {
  ElMessage.warning('最多只能上传9张图片')
}

const handlePreview = (file) => {
  previewUrl.value = file.url || URL.createObjectURL(file.raw)
  previewVisible.value = true
}

const handleRemove = (file, index) => {
  const fileList = reviewForms.value[index].imageList
  const idx = fileList.indexOf(file)
  if (idx > -1) {
    fileList.splice(idx, 1)
  }
}

const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  return true
}

// 返回 - 如果是新窗口打开则关闭窗口，否则跳转
const goBack = () => {
  // 检查是否是新窗口打开的（通过 window.opener 判断）
  if (window.opener) {
    window.close()
  } else {
    router.push('/user/orders')
  }
}

onMounted(() => {
  if (!orderId.value) {
    ElMessage.error('订单ID无效')
    router.push('/user/orders')
    return
  }
  fetchOrderItems()
})
</script>

<style scoped>
.review-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  min-height: 100vh;
  background: #f5f5f5;
}

.page-header {
  background: white;
  padding: 16px 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.review-content {
  min-height: 300px;
}

.order-items {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.review-item-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.product-info {
  display: flex;
  gap: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 20px;
}

.product-image {
  width: 80px;
  height: 80px;
  border-radius: 6px;
  overflow: hidden;
  flex-shrink: 0;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-detail {
  flex: 1;
}

.product-name {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  line-height: 1.4;
}

.product-spec {
  margin: 0;
  font-size: 14px;
  color: #909399;
}

.reviewed-badge {
  flex-shrink: 0;
}

.review-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.rating-row {
  display: flex;
  align-items: center;
}

.content-row,
.upload-row {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.anonymous-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.anonymous-tip {
  font-size: 12px;
  color: #909399;
}

.submit-section {
  position: sticky;
  bottom: 0;
  background: white;
  padding: 16px 20px;
  margin-top: 20px;
  border-radius: 8px;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.06);
  display: flex;
  justify-content: center;
}

.submit-section .el-button {
  width: 200px;
}

/* 响应式 */
@media (max-width: 768px) {
  .review-page {
    padding: 12px;
  }

  .product-info {
    flex-wrap: wrap;
  }

  .product-image {
    width: 60px;
    height: 60px;
  }

  .rating-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .submit-section .el-button {
    width: 100%;
  }
}
</style>
