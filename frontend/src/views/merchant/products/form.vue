<template>
  <div class="product-form-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-button @click="goBack" class="back-btn">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <div class="header-info">
          <h2 class="page-title">{{ isEdit ? '编辑商品' : '添加商品' }}</h2>
          <p class="page-desc">{{ isEdit ? '修改商品信息、价格、库存等' : '发布新商品到您的店铺' }}</p>
        </div>
      </div>
      <div class="header-right">
        <el-button @click="handleSaveDraft">
          <el-icon><Document /></el-icon>
          保存草稿
        </el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
          <el-icon><Check /></el-icon>
          {{ isEdit ? '保存修改' : '发布商品' }}
        </el-button>
      </div>
    </div>

    <!-- 表单内容 -->
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="120px"
      class="product-form"
    >
      <!-- 基本信息 -->
      <el-card shadow="never" class="form-section">
        <template #header>
          <div class="section-header">
            <el-icon><InfoFilled /></el-icon>
            <span>基本信息</span>
          </div>
        </template>

        <el-form-item label="商品名称" prop="name" required>
          <el-input
            v-model="formData.name"
            placeholder="请输入商品名称"
            maxlength="100"
            show-word-limit
            clearable
          />
        </el-form-item>

        <el-form-item label="商品分类" prop="category" required>
          <el-select v-model="formData.category" placeholder="请选择商品分类" clearable style="width: 300px">
            <el-option label="数码电子" value="1"></el-option>
            <el-option label="服装鞋帽" value="2"></el-option>
            <el-option label="家居用品" value="3"></el-option>
            <el-option label="美妆护肤" value="4"></el-option>
            <el-option label="食品饮料" value="5"></el-option>
            <el-option label="运动户外" value="6"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="商品编码" prop="sku">
          <el-input
            v-model="formData.sku"
            placeholder="请输入商品编码/SKU"
            maxlength="50"
            style="width: 300px"
            clearable
          />
          <span class="form-tip">商品的唯一标识码，用于库存管理</span>
        </el-form-item>

        <el-form-item label="主图" prop="mainImage">
          <el-upload
            v-model:file-list="mainImageFileList"
            action="/api/merchant/products/upload-image"
            list-type="picture-card"
            :multiple="true"
            :auto-upload="true"
            :on-success="handleMainImageUploadSuccess"
            :on-error="handleUploadError"
            :on-remove="handleMainImageRemove"
            :before-upload="beforeImageUpload"
            accept="image/*"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">主图用于列表展示，可上传多张，建议尺寸800x800，单张不超过5MB</div>
        </el-form-item>

        <el-form-item label="详情图片" prop="images">
          <el-upload
            v-model:file-list="detailImageFileList"
            action="/api/merchant/products/upload-image"
            list-type="picture-card"
            :multiple="true"
            :auto-upload="true"
            :on-success="handleDetailImageUploadSuccess"
            :on-error="handleUploadError"
            :on-remove="handleDetailImageRemove"
            :before-upload="beforeImageUpload"
            accept="image/*"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">详情图片用于商品详情页展示，建议尺寸800x800，单张不超过5MB</div>
        </el-form-item>

        <el-form-item label="商品描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="4"
            placeholder="请输入商品描述"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-card>

      <!-- 价格库存 -->
      <el-card shadow="never" class="form-section">
        <template #header>
          <div class="section-header">
            <el-icon><Money /></el-icon>
            <span>价格与库存</span>
          </div>
        </template>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="销售价格" prop="price" required>
              <el-input
                v-model="formData.price"
                placeholder="请输入销售价格"
                type="number"
                min="0"
                step="0.01"
              >
                <template #prefix>¥</template>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="市场价格" prop="marketPrice">
              <el-input
                v-model="formData.marketPrice"
                placeholder="请输入市场价格"
                type="number"
                min="0"
                step="0.01"
              >
                <template #prefix>¥</template>
              </el-input>
              <span class="form-tip">用于显示对比价格</span>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="库存数量" prop="stock" required>
              <el-input
                v-model="formData.stock"
                placeholder="请输入库存数量"
                type="number"
                min="0"
              >
                <template #suffix>件</template>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="库存预警" prop="lowStockThreshold">
              <el-input
                v-model="formData.lowStockThreshold"
                placeholder="请输入预警值"
                type="number"
                min="0"
              >
                <template #suffix>件</template>
              </el-input>
              <span class="form-tip">库存低于此值时预警</span>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="重量" prop="weight">
          <el-input
            v-model="formData.weight"
            placeholder="请输入商品重量"
            type="number"
            min="0"
            step="0.01"
            style="width: 300px"
          >
            <template #suffix>kg</template>
          </el-input>
          <span class="form-tip">用于计算运费</span>
        </el-form-item>
      </el-card>

      <!-- 商品规格（可选） -->
      <el-card shadow="never" class="form-section">
        <template #header>
          <div class="section-header">
            <el-icon><Grid /></el-icon>
            <span>商品规格</span>
            <span class="section-tip">（可选）如果商品有多种规格，可添加SKU</span>
          </div>
        </template>

        <el-form-item label="启用规格">
          <el-switch v-model="enableSpecs" />
          <span class="form-tip">开启后可添加多规格商品，如颜色、尺寸等</span>
        </el-form-item>

        <div v-if="enableSpecs" class="specs-container">
          <el-button type="primary" plain @click="handleAddSpec">
            <el-icon><Plus /></el-icon>
            添加规格
          </el-button>
          <div class="specs-tip">规格功能开发中...</div>
        </div>
      </el-card>

      <!-- 其他设置 -->
      <el-card shadow="never" class="form-section">
        <template #header>
          <div class="section-header">
            <el-icon><Setting /></el-icon>
            <span>其他设置</span>
          </div>
        </template>

        <el-form-item label="商品状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio value="on_sale">立即上架</el-radio>
            <el-radio value="off_sale">暂不上架</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="是否推荐">
          <el-switch v-model="formData.isRecommended" />
          <span class="form-tip">推荐商品会在首页展示</span>
        </el-form-item>

        <el-form-item label="是否新品">
          <el-switch v-model="formData.isNew" />
          <span class="form-tip">新品标签会吸引更多关注</span>
        </el-form-item>

        <el-form-item label="限购数量" prop="limitBuy">
          <el-input
            v-model="formData.limitBuy"
            placeholder="不限购请填0"
            type="number"
            min="0"
            style="width: 200px"
          >
            <template #suffix>件/人</template>
          </el-input>
          <span class="form-tip">0表示不限购</span>
        </el-form-item>
      </el-card>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft, Document, Check, InfoFilled, Money, Grid, Setting, Plus
} from '@element-plus/icons-vue'
import {
  createProduct,
  updateProduct,
  getProductById
} from '@/api/merchant/product'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 响应式数据
const formRef = ref(null)
const submitLoading = ref(false)
const enableSpecs = ref(false)
const mainImageFileList = ref([])  // 主图文件列表
const mainImageUrls = ref([])  // 主图URL列表（支持多张）
const detailImageFileList = ref([])  // 详情图片文件列表
const detailImageUrls = ref([])  // 详情图片URL列表

// 判断是否为编辑模式
const isEdit = computed(() => !!route.params.id)

// 表单数据
const formData = reactive({
  name: '',
  category: '',
  sku: '',
  images: [],
  description: '',
  price: '',
  marketPrice: '',
  stock: '',
  lowStockThreshold: '10',
  weight: '',
  status: 'on_sale',
  isRecommended: false,
  isNew: false,
  limitBuy: '0'
})

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入商品名称', trigger: 'blur' },
    { min: 2, max: 100, message: '商品名称长度在2-100个字符', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择商品分类', trigger: 'change' }
  ],
  price: [
    { required: true, message: '请输入销售价格', trigger: 'blur' },
    { pattern: /^\d+(\.\d{1,2})?$/, message: '价格格式不正确', trigger: 'blur' }
  ],
  stock: [
    { required: true, message: '请输入库存数量', trigger: 'blur' },
    { pattern: /^\d+$/, message: '库存必须为整数', trigger: 'blur' }
  ]
}

// 方法
const goBack = () => {
  router.back()
}

const handleAddSpec = () => {
  ElMessage.info('规格管理功能开发中...')
}

// 图片上传前校验
const beforeImageUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }
  return true
}

// 主图上传成功
const handleMainImageUploadSuccess = (response, file, fileList) => {
  console.log('主图上传成功:', response)
  if (response.code === 200 && response.data && response.data.url) {
    mainImageUrls.value.push(response.data.url)
    ElMessage.success('主图上传成功')
  } else {
    ElMessage.error(response.msg || '主图上传失败')
  }
}

// 详情图片上传成功
const handleDetailImageUploadSuccess = (response, file, fileList) => {
  console.log('详情图片上传成功:', response)
  if (response.code === 200 && response.data && response.data.url) {
    detailImageUrls.value.push(response.data.url)
    ElMessage.success('详情图片上传成功')
  } else {
    ElMessage.error(response.msg || '详情图片上传失败')
  }
}

// 图片上传失败
const handleUploadError = (error, file, fileList) => {
  console.error('图片上传失败:', error)
  ElMessage.error('图片上传失败，请重试')
}

// 删除主图
const handleMainImageRemove = (file, fileList) => {
  // 从已上传列表中移除
  if (file.response && file.response.data && file.response.data.url) {
    const index = mainImageUrls.value.indexOf(file.response.data.url)
    if (index > -1) {
      mainImageUrls.value.splice(index, 1)
    }
  } else if (file.url) {
    // 编辑模式下，已有图片的url直接在file.url中
    const index = mainImageUrls.value.indexOf(file.url)
    if (index > -1) {
      mainImageUrls.value.splice(index, 1)
    }
  }
}

// 删除详情图片
const handleDetailImageRemove = (file, fileList) => {
  // 从已上传列表中移除
  if (file.response && file.response.data && file.response.data.url) {
    const index = detailImageUrls.value.indexOf(file.response.data.url)
    if (index > -1) {
      detailImageUrls.value.splice(index, 1)
    }
  }
}

const handleSaveDraft = async () => {
  try {
    // TODO: 保存草稿API
    ElMessage.success('草稿保存成功')
  } catch (error) {
    ElMessage.error('保存草稿失败')
  }
}

const handleSubmit = async () => {
  try {
    // 验证表单
    await formRef.value.validate()
    
    submitLoading.value = true
    
    // 准备提交数据
    const submitData = {
      merchantId: userStore.merchantId,
      productName: formData.name,  // 字段映射: name -> productName
      categoryId: parseInt(formData.category),  // 字段映射: category -> categoryId（必填）
      sku: formData.sku,
      description: formData.description,
      price: parseFloat(formData.price),
      marketPrice: formData.marketPrice ? parseFloat(formData.marketPrice) : null,
      stockQuantity: parseInt(formData.stock),  // 字段映射: stock -> stockQuantity
      warningStock: formData.lowStockThreshold ? parseInt(formData.lowStockThreshold) : 10,  // 字段映射: lowStockThreshold -> warningStock
      weight: formData.weight ? parseFloat(formData.weight) : null,
      status: formData.status === 'on_sale' ? 1 : 0,  // 状态转换: on_sale/off_sale -> 1/0
      isRecommended: formData.isRecommended ? 1 : 0,  // boolean -> Integer
      isNew: formData.isNew ? 1 : 0,  // boolean -> Integer
      limitBuy: parseInt(formData.limitBuy) || 0,
      mainImage: mainImageUrls.value.length > 0 ? mainImageUrls.value.join(',') : null,  // 主图（支持多张，用逗号分隔）
      images: detailImageUrls.value.length > 0 ? detailImageUrls.value.join(',') : null  // 详情图片用逗号分隔
    }
    
    // 调用真实API
    if (isEdit.value) {
      await updateProduct(route.params.id, submitData)
    } else {
      await createProduct(submitData)
    }
    
    ElMessage.success(isEdit.value ? '商品修改成功' : '商品发布成功')
    router.push('/merchant/products')
    
  } catch (error) {
    console.error('提交失败:', error)
    if (error !== 'cancel') {
      ElMessage.error(error.message || (isEdit.value ? '商品修改失败' : '商品发布失败'))
    }
  } finally {
    submitLoading.value = false
  }
}

const loadProductData = async () => {
  if (!isEdit.value) return
  
  try {
    // 加载商品数据
    const response = await getProductById(route.params.id, userStore.merchantId)
    
    if (response.code === 200 && response.data) {
      // 填充表单数据
      Object.assign(formData, {
        name: response.data.productName || '',
        category: response.data.category || '',
        sku: response.data.sku || '',
        description: response.data.description || '',
        price: response.data.price ? response.data.price.toString() : '',
        marketPrice: response.data.marketPrice ? response.data.marketPrice.toString() : '',
        stock: response.data.stock ? response.data.stock.toString() : '',
        lowStockThreshold: response.data.lowStockThreshold ? response.data.lowStockThreshold.toString() : '10',
        weight: response.data.weight ? response.data.weight.toString() : '',
        status: response.data.status === 1 ? 'on_sale' : 'off_sale',
        isRecommended: response.data.isRecommended || false,
        isNew: response.data.isNew || false,
        limitBuy: response.data.limitBuy ? response.data.limitBuy.toString() : '0'
      })
      
      // 加载主图（支持多张）
      if (response.data.mainImage) {
        const mainImages = response.data.mainImage.split(',')
        mainImageUrls.value = mainImages
        mainImageFileList.value = mainImages.map((url, index) => ({
          name: `main_image_${index}`,
          url: url
        }))
      }
      
      // 加载详情图片
      if (response.data.images) {
        const imageUrls = response.data.images.split(',')
        detailImageUrls.value = imageUrls
        detailImageFileList.value = imageUrls.map((url, index) => ({
          name: `detail_image_${index}`,
          url: url
        }))
      }
    }
  } catch (error) {
    console.error('加载商品数据失败:', error)
    ElMessage.error('加载商品数据失败')
    router.back()
  }
}

// 生命周期
onMounted(() => {
  loadProductData()
})
</script>

<style scoped>
.product-form-page {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: calc(100vh - 60px);
}

/* 页面头部 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
  padding: 20px;
  background: white;
  border-radius: 4px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.header-left {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.back-btn {
  margin-top: 4px;
}

.header-info {
  flex: 1;
}

.page-title {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #262626;
}

.page-desc {
  margin: 0;
  color: #8c8c8c;
  font-size: 14px;
}

.header-right {
  display: flex;
  gap: 12px;
}

/* 表单样式 */
.product-form {
  max-width: 1200px;
}

.form-section {
  margin-bottom: 20px;
}

.form-section:last-child {
  margin-bottom: 0;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #262626;
}

.section-tip {
  margin-left: auto;
  font-size: 12px;
  font-weight: normal;
  color: #8c8c8c;
}

.form-tip {
  margin-left: 12px;
  font-size: 12px;
  color: #8c8c8c;
}

.upload-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #8c8c8c;
}

.specs-container {
  padding: 20px;
  background: #fafafa;
  border-radius: 4px;
  text-align: center;
}

.specs-tip {
  margin-top: 12px;
  color: #8c8c8c;
  font-size: 12px;
}

/* 表单项样式 */
.product-form :deep(.el-form-item) {
  margin-bottom: 24px;
}

.product-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #262626;
}

.product-form :deep(.el-input),
.product-form :deep(.el-textarea) {
  width: 100%;
  max-width: 600px;
}

.product-form :deep(.el-upload--picture-card) {
  width: 120px;
  height: 120px;
}

/* 响应式 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 16px;
  }

  .header-left {
    width: 100%;
    flex-direction: column;
  }

  .header-right {
    width: 100%;
  }

  .product-form {
    padding: 0 16px;
  }
}
</style>
