<template>
  <el-dialog
    v-model="visible"
    :title="isEdit ? '编辑商品' : '添加商品'"
    width="90%"
    :before-close="handleClose"
    :close-on-click-modal="false">
    
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="120px"
      class="product-form">
      
      <!-- 基本信息 -->
      <el-card shadow="never" class="form-section">
        <template #header>
          <span>基本信息</span>
        </template>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="商品名称" prop="name">
              <el-input
                v-model="form.name"
                placeholder="请输入商品名称"
                maxlength="100"
                show-word-limit
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="商品编号" prop="sku">
              <el-input
                v-model="form.sku"
                placeholder="请输入商品编号"
                maxlength="50"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="商品分类" prop="category">
              <el-select
                v-model="form.category"
                placeholder="请选择商品分类"
                style="width: 100%">
                <el-option
                  v-for="category in categories"
                  :key="category.value"
                  :label="category.label"
                  :value="category.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="商品状态" prop="status">
              <el-select
                v-model="form.status"
                placeholder="请选择商品状态"
                style="width: 100%">
                <el-option label="在售" value="on_sale" />
                <el-option label="下架" value="off_sale" />
                <el-option label="售罄" value="sold_out" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="销售价格" prop="price">
              <el-input-number
                v-model="form.price"
                :min="0"
                :precision="2"
                placeholder="0.00"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="8">
            <el-form-item label="市场价格" prop="marketPrice">
              <el-input-number
                v-model="form.marketPrice"
                :min="0"
                :precision="2"
                placeholder="0.00"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="8">
            <el-form-item label="商品重量" prop="weight">
              <el-input-number
                v-model="form.weight"
                :min="0"
                :precision="3"
                placeholder="0.000"
                style="width: 100%"
              >
                <template #append>kg</template>
              </el-input-number>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="库存数量" prop="stock">
          <el-input-number
            v-model="form.stock"
            :min="0"
            placeholder="0"
            style="width: 200px"
          />
          <span class="form-tip">库存不足10件时会显示低库存提醒</span>
        </el-form-item>
      </el-card>

      <!-- 商品图片 -->
      <el-card shadow="never" class="form-section">
        <template #header>
          <span>商品图片</span>
        </template>
        
        <el-form-item label="商品图片" prop="images">
          <div class="image-upload-container">
            <el-upload
              v-model:file-list="imageList"
              action="#"
              list-type="picture-card"
              :auto-upload="false"
              :on-change="handleImageChange"
              :on-remove="handleImageRemove"
              :before-upload="beforeImageUpload"
              multiple
              :limit="8">
              <el-icon><Plus /></el-icon>
              <template #tip>
                <div class="el-upload__tip">
                  支持jpg/png格式，单张图片不超过2MB，最多上传8张
                </div>
              </template>
            </el-upload>
          </div>
        </el-form-item>
      </el-card>

      <!-- 商品描述 -->
      <el-card shadow="never" class="form-section">
        <template #header>
          <span>商品描述</span>
        </template>
        
        <el-form-item label="商品描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="8"
            placeholder="请输入商品详细描述"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>
      </el-card>

      <!-- 规格参数 -->
      <el-card shadow="never" class="form-section">
        <template #header>
          <div class="section-header">
            <span>规格参数</span>
            <el-button type="primary" size="small" @click="addSpecification">
              添加参数
            </el-button>
          </div>
        </template>
        
        <div class="specifications-container">
          <div
            v-for="(spec, index) in form.specifications"
            :key="index"
            class="specification-item">
            <el-input
              v-model="spec.name"
              placeholder="参数名称"
              style="width: 200px; margin-right: 12px"
            />
            <el-input
              v-model="spec.value"
              placeholder="参数值"
              style="width: 300px; margin-right: 12px"
            />
            <el-button
              type="danger"
              size="small"
              @click="removeSpecification(index)">
              删除
            </el-button>
          </div>
          
          <div v-if="form.specifications.length === 0" class="empty-specifications">
            <span>暂无规格参数，点击"添加参数"按钮添加</span>
          </div>
        </div>
      </el-card>

      <!-- 物流信息 -->
      <el-card shadow="never" class="form-section">
        <template #header>
          <span>物流信息</span>
        </template>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="发货地址" prop="shipFrom">
              <el-input
                v-model="form.shipFrom"
                placeholder="请输入发货地址"
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="运费模板" prop="shippingTemplate">
              <el-select
                v-model="form.shippingTemplate"
                placeholder="请选择运费模板"
                style="width: 100%">
                <el-option label="包邮" value="free" />
                <el-option label="按重量计费" value="weight" />
                <el-option label="按件数计费" value="quantity" />
                <el-option label="自定义运费" value="custom" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-card>

      <!-- SEO设置 -->
      <el-card shadow="never" class="form-section">
        <template #header>
          <span>SEO设置</span>
        </template>
        
        <el-form-item label="SEO标题" prop="seoTitle">
          <el-input
            v-model="form.seoTitle"
            placeholder="请输入SEO标题"
            maxlength="60"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="SEO关键词" prop="seoKeywords">
          <el-input
            v-model="form.seoKeywords"
            placeholder="请输入SEO关键词，多个关键词用逗号分隔"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="SEO描述" prop="seoDescription">
          <el-input
            v-model="form.seoDescription"
            type="textarea"
            :rows="3"
            placeholder="请输入SEO描述"
            maxlength="160"
            show-word-limit
          />
        </el-form-item>
      </el-card>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button @click="saveDraft">保存草稿</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">
          {{ isEdit ? '更新商品' : '发布商品' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  productId: {
    type: [String, Number],
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const isEdit = computed(() => !!props.productId)

// 表单引用
const formRef = ref()

// 表单数据
const form = reactive({
  name: '',
  sku: '',
  category: '',
  status: 'on_sale',
  price: 0,
  marketPrice: 0,
  weight: 0,
  stock: 0,
  images: [],
  description: '',
  specifications: [],
  shipFrom: '',
  shippingTemplate: 'free',
  seoTitle: '',
  seoKeywords: '',
  seoDescription: ''
})

// 图片列表
const imageList = ref([])

// 提交状态
const submitting = ref(false)

// 商品分类选项
const categories = [
  { label: '数码电子', value: 'electronics' },
  { label: '服装鞋帽', value: 'clothing' },
  { label: '家居用品', value: 'home' },
  { label: '美妆护肤', value: 'beauty' },
  { label: '食品饮料', value: 'food' },
  { label: '运动户外', value: 'sports' }
]

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入商品名称', trigger: 'blur' },
    { min: 2, max: 100, message: '商品名称长度在2到100个字符', trigger: 'blur' }
  ],
  sku: [
    { required: true, message: '请输入商品编号', trigger: 'blur' },
    { pattern: /^[A-Za-z0-9]+$/, message: '商品编号只能包含字母和数字', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择商品分类', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择商品状态', trigger: 'change' }
  ],
  price: [
    { required: true, message: '请输入销售价格', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '销售价格必须大于0', trigger: 'blur' }
  ],
  stock: [
    { required: true, message: '请输入库存数量', trigger: 'blur' },
    { type: 'number', min: 0, message: '库存数量不能小于0', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入商品描述', trigger: 'blur' },
    { min: 10, max: 2000, message: '商品描述长度在10到2000个字符', trigger: 'blur' }
  ]
}

// 方法
const loadProductData = async () => {
  if (!props.productId) return
  
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500))
    
    // 这里应该调用真实的API
    // const response = await productApi.getProduct(props.productId)
    // const product = response.data
    
    // 使用模拟数据
    const product = {
      name: 'iPhone 15 Pro Max 256GB 深空黑色',
      sku: 'IP15PM256BK',
      category: 'electronics',
      status: 'on_sale',
      price: 9999.00,
      marketPrice: 10999.00,
      weight: 0.221,
      stock: 156,
      images: [
        'https://trae-api-us.mchost.guru/api/ide/v1/text_to_image?prompt=iPhone%2015%20Pro%20Max%20black%20smartphone%20product%20photo&image_size=square'
      ],
      description: 'iPhone 15 Pro Max 采用钛金属设计，搭载 A17 Pro 芯片，配备专业级摄像头系统。',
      specifications: [
        { name: '屏幕尺寸', value: '6.7 英寸' },
        { name: '处理器', value: 'A17 Pro 芯片' },
        { name: '存储容量', value: '256GB' }
      ],
      shipFrom: '深圳市南山区',
      shippingTemplate: 'free',
      seoTitle: 'iPhone 15 Pro Max 256GB 深空黑色 - 官方正品',
      seoKeywords: 'iPhone,苹果手机,iPhone15,Pro Max',
      seoDescription: 'iPhone 15 Pro Max 256GB 深空黑色，钛金属设计，A17 Pro芯片，专业摄像头系统。'
    }
    
    // 填充表单数据
    Object.assign(form, product)
    
    // 处理图片列表
    imageList.value = product.images.map((url, index) => ({
      uid: index,
      name: `image-${index}.jpg`,
      status: 'success',
      url: url
    }))
    
  } catch (error) {
    ElMessage.error('加载商品数据失败')
  }
}

const resetForm = () => {
  Object.assign(form, {
    name: '',
    sku: '',
    category: '',
    status: 'on_sale',
    price: 0,
    marketPrice: 0,
    weight: 0,
    stock: 0,
    images: [],
    description: '',
    specifications: [],
    shipFrom: '',
    shippingTemplate: 'free',
    seoTitle: '',
    seoKeywords: '',
    seoDescription: ''
  })
  imageList.value = []
  
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

const handleClose = () => {
  visible.value = false
}

// 图片上传处理
const handleImageChange = (file, fileList) => {
  // 更新图片列表
  form.images = fileList.map(item => item.url || item.raw)
}

const handleImageRemove = (file, fileList) => {
  form.images = fileList.map(item => item.url || item.raw)
}

const beforeImageUpload = (file) => {
  const isJPGOrPNG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPGOrPNG) {
    ElMessage.error('只能上传 JPG/PNG 格式的图片!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

// 规格参数管理
const addSpecification = () => {
  form.specifications.push({
    name: '',
    value: ''
  })
}

const removeSpecification = (index) => {
  form.specifications.splice(index, 1)
}

// 保存草稿
const saveDraft = async () => {
  try {
    // 调用API保存草稿
    // await productApi.saveDraft(form)
    
    ElMessage.success('草稿保存成功')
  } catch (error) {
    ElMessage.error('保存草稿失败')
  }
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    submitting.value = true
    
    // 准备提交数据
    const submitData = {
      ...form,
      specifications: form.specifications.filter(spec => spec.name && spec.value)
    }
    
    // 调用API
    if (isEdit.value) {
      // await productApi.updateProduct(props.productId, submitData)
      ElMessage.success('商品更新成功')
    } else {
      // await productApi.createProduct(submitData)
      ElMessage.success('商品发布成功')
    }
    
    emit('success')
    handleClose()
    
  } catch (error) {
    if (error !== 'validation failed') {
      ElMessage.error(isEdit.value ? '更新商品失败' : '发布商品失败')
    }
  } finally {
    submitting.value = false
  }
}

// 监听对话框显示状态
watch(visible, (newVisible) => {
  if (newVisible) {
    if (isEdit.value) {
      loadProductData()
    } else {
      resetForm()
    }
  }
})
</script>

<style scoped>
.product-form {
  max-height: 70vh;
  overflow-y: auto;
  padding-right: 12px;
}

.form-section {
  margin-bottom: 20px;
}

.form-section:last-child {
  margin-bottom: 0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.form-tip {
  margin-left: 12px;
  font-size: 12px;
  color: #8c8c8c;
}

/* 图片上传 */
.image-upload-container {
  width: 100%;
}

:deep(.el-upload--picture-card) {
  width: 100px;
  height: 100px;
}

:deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 100px;
  height: 100px;
}

/* 规格参数 */
.specifications-container {
  min-height: 60px;
}

.specification-item {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.empty-specifications {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 60px;
  color: #8c8c8c;
  background-color: #fafafa;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .product-form {
    max-height: 60vh;
  }
  
  .specification-item {
    flex-direction: column;
    align-items: stretch;
    gap: 8px;
  }
  
  .specification-item .el-input {
    width: 100% !important;
    margin-right: 0 !important;
  }
}
</style>