<template>
  <el-dialog
    v-model="visible"
    title="编辑商家信息"
    width="70%"
    :before-close="handleClose">
    <div v-if="merchant" class="edit-content">
      <el-form
        ref="formRef"
        :model="editForm"
        :rules="formRules"
        label-width="120px">
        
        <!-- 基本信息 -->
        <el-card class="form-section" shadow="never">
          <template #header>
            <span>基本信息</span>
          </template>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="店铺名称" prop="shopName">
                <el-input v-model="editForm.shopName" placeholder="请输入店铺名称"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="联系人" prop="contactPerson">
                <el-input v-model="editForm.contactPerson" placeholder="请输入联系人姓名"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="手机号" prop="phone">
                <el-input v-model="editForm.phone" placeholder="请输入手机号"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="邮箱" prop="email">
                <el-input v-model="editForm.email" placeholder="请输入邮箱地址"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="经营类目" prop="category">
                <el-select v-model="editForm.category" placeholder="请选择经营类目">
                  <el-option label="数码电子" value="electronics"></el-option>
                  <el-option label="服装鞋帽" value="clothing"></el-option>
                  <el-option label="家居用品" value="home"></el-option>
                  <el-option label="美妆护肤" value="beauty"></el-option>
                  <el-option label="食品饮料" value="food"></el-option>
                  <el-option label="运动户外" value="sports"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="商家状态" prop="status">
                <el-select v-model="editForm.status" placeholder="请选择商家状态">
                  <el-option label="待审核" value="pending"></el-option>
                  <el-option label="已通过" value="approved"></el-option>
                  <el-option label="已拒绝" value="rejected"></el-option>
                  <el-option label="已禁用" value="disabled"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-form-item label="店铺头像" prop="avatar">
            <el-upload
              class="avatar-uploader"
              :action="uploadAction"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
              :before-upload="beforeAvatarUpload">
              <img v-if="editForm.avatar" :src="editForm.avatar" class="avatar" />
              <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
            </el-upload>
          </el-form-item>
        </el-card>

        <!-- 店铺信息 -->
        <el-card class="form-section" shadow="never">
          <template #header>
            <span>店铺信息</span>
          </template>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="店铺类型" prop="shopType">
                <el-select v-model="editForm.shopType" placeholder="请选择店铺类型">
                  <el-option label="个人店铺" value="personal"></el-option>
                  <el-option label="企业店铺" value="enterprise"></el-option>
                  <el-option label="旗舰店" value="flagship"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="店铺等级" prop="shopLevel">
                <el-select v-model="editForm.shopLevel" placeholder="请选择店铺等级">
                  <el-option label="一星店铺" :value="1"></el-option>
                  <el-option label="二星店铺" :value="2"></el-option>
                  <el-option label="三星店铺" :value="3"></el-option>
                  <el-option label="四星店铺" :value="4"></el-option>
                  <el-option label="五星店铺" :value="5"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-form-item label="店铺地址" prop="shopAddress">
            <el-input v-model="editForm.shopAddress" placeholder="请输入店铺地址"></el-input>
          </el-form-item>
          
          <el-form-item label="店铺简介" prop="shopDescription">
            <el-input
              v-model="editForm.shopDescription"
              type="textarea"
              :rows="3"
              placeholder="请输入店铺简介"
              maxlength="200"
              show-word-limit>
            </el-input>
          </el-form-item>
        </el-card>

        <!-- 经营设置 -->
        <el-card class="form-section" shadow="never">
          <template #header>
            <span>经营设置</span>
          </template>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="佣金比例" prop="commissionRate">
                <el-input-number
                  v-model="editForm.commissionRate"
                  :min="0"
                  :max="20"
                  :precision="2"
                  :step="0.1"
                  placeholder="请输入佣金比例">
                </el-input-number>
                <span class="input-suffix">%</span>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="保证金" prop="deposit">
                <el-input-number
                  v-model="editForm.deposit"
                  :min="0"
                  :step="1000"
                  placeholder="请输入保证金金额">
                </el-input-number>
                <span class="input-suffix">元</span>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-form-item label="特殊权限">
            <el-checkbox-group v-model="editForm.permissions">
              <el-checkbox value="featured">推荐商家</el-checkbox>
              <el-checkbox value="priority">优先展示</el-checkbox>
              <el-checkbox value="bulk_upload">批量上传</el-checkbox>
              <el-checkbox value="advanced_analytics">高级分析</el-checkbox>
            </el-checkbox-group>
          </el-form-item>
        </el-card>

        <!-- 资质信息 -->
        <el-card class="form-section" shadow="never">
          <template #header>
            <span>资质信息</span>
          </template>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="营业执照号" prop="businessLicense">
                <el-input v-model="editForm.businessLicense" placeholder="请输入营业执照号"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="法人姓名" prop="legalPerson">
                <el-input v-model="editForm.legalPerson" placeholder="请输入法人姓名"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="法人身份证" prop="legalIdCard">
                <el-input v-model="editForm.legalIdCard" placeholder="请输入法人身份证号"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="注册资本" prop="registeredCapital">
                <el-input v-model="editForm.registeredCapital" placeholder="请输入注册资本"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-form-item label="经营范围" prop="businessScope">
            <el-input
              v-model="editForm.businessScope"
              type="textarea"
              :rows="3"
              placeholder="请输入经营范围"
              maxlength="500"
              show-word-limit>
            </el-input>
          </el-form-item>
        </el-card>
      </el-form>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button
          type="primary"
          @click="submitEdit"
          :loading="submitting">
          保存修改
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  merchant: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'refresh'])

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const formRef = ref()
const submitting = ref(false)
const uploadAction = '/api/upload/avatar' // 上传接口地址

// 编辑表单
const editForm = reactive({
  shopName: '',
  contactPerson: '',
  phone: '',
  email: '',
  category: '',
  status: '',
  avatar: '',
  shopType: '',
  shopLevel: null,
  shopAddress: '',
  shopDescription: '',
  commissionRate: null,
  deposit: null,
  permissions: [],
  businessLicense: '',
  legalPerson: '',
  legalIdCard: '',
  registeredCapital: '',
  businessScope: ''
})

// 表单验证规则
const formRules = {
  shopName: [
    { required: true, message: '请输入店铺名称', trigger: 'blur' },
    { min: 2, max: 50, message: '店铺名称长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  contactPerson: [
    { required: true, message: '请输入联系人姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '联系人姓名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择经营类目', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择商家状态', trigger: 'change' }
  ],
  shopType: [
    { required: true, message: '请选择店铺类型', trigger: 'change' }
  ],
  shopLevel: [
    { required: true, message: '请选择店铺等级', trigger: 'change' }
  ],
  commissionRate: [
    { required: true, message: '请输入佣金比例', trigger: 'blur' },
    { type: 'number', min: 0, max: 20, message: '佣金比例应在0-20%之间', trigger: 'blur' }
  ],
  deposit: [
    { required: true, message: '请输入保证金金额', trigger: 'blur' },
    { type: 'number', min: 0, message: '保证金金额不能为负数', trigger: 'blur' }
  ],
  businessLicense: [
    { pattern: /^[0-9A-Z]{18}$/, message: '请输入正确的营业执照号', trigger: 'blur' }
  ],
  legalIdCard: [
    { pattern: /^[1-9]\d{5}(18|19|20)\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\d{3}[0-9Xx]$/, message: '请输入正确的身份证号', trigger: 'blur' }
  ]
}

// 方法
const handleClose = () => {
  visible.value = false
}

const initForm = () => {
  if (props.merchant) {
    // 复制商家数据到表单
    Object.keys(editForm).forEach(key => {
      if (props.merchant[key] !== undefined) {
        editForm[key] = props.merchant[key]
      }
    })
    
    // 补充一些可能缺失的字段
    if (!editForm.shopType) editForm.shopType = 'enterprise'
    if (!editForm.shopLevel) editForm.shopLevel = 2
    if (!editForm.commissionRate) editForm.commissionRate = 5.0
    if (!editForm.deposit) editForm.deposit = 10000
    if (!editForm.permissions) editForm.permissions = []
  }
}

const handleAvatarSuccess = (response, file) => {
  // 处理头像上传成功
  if (response.code === 200) {
    editForm.avatar = response.data.url
    ElMessage.success('头像上传成功')
  } else {
    ElMessage.error('头像上传失败')
  }
}

const beforeAvatarUpload = (file) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG) {
    ElMessage.error('头像图片只能是 JPG/PNG 格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('头像图片大小不能超过 2MB!')
    return false
  }
  return true
}

const submitEdit = async () => {
  try {
    // 表单验证
    await formRef.value.validate()
    
    submitting.value = true
    
    // 构建提交数据
    const submitData = {
      id: props.merchant.id,
      ...editForm
    }
    
    // 调用API更新商家信息
    // await updateMerchantApi(submitData)
    
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    ElMessage.success('商家信息更新成功')
    emit('refresh')
    handleClose()
    
  } catch (error) {
    if (error !== 'validation failed') {
      ElMessage.error('更新商家信息失败')
    }
  } finally {
    submitting.value = false
  }
}

// 监听商家变化，初始化表单
watch(() => props.merchant, () => {
  initForm()
}, { immediate: true })

// 监听对话框显示状态
watch(visible, (newVisible) => {
  if (newVisible) {
    initForm()
  }
})
</script>

<style scoped>
.edit-content {
  max-height: 70vh;
  overflow-y: auto;
}

.form-section {
  margin-bottom: 20px;
}

.form-section:last-child {
  margin-bottom: 0;
}

.input-suffix {
  margin-left: 8px;
  color: #6b7280;
  font-size: 14px;
}

.avatar-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: border-color 0.2s;
}

.avatar-uploader:hover {
  border-color: #409eff;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  text-align: center;
  line-height: 100px;
}

.avatar {
  width: 100px;
  height: 100px;
  display: block;
  object-fit: cover;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

:deep(.el-card__header) {
  background-color: #f8f9fa;
  border-bottom: 1px solid #e9ecef;
  font-weight: 600;
  color: #495057;
}

:deep(.el-checkbox) {
  margin-right: 20px;
  margin-bottom: 8px;
}

@media (max-width: 768px) {
  .edit-content {
    max-height: 60vh;
  }
  
  .el-row .el-col {
    width: 100% !important;
    margin-bottom: 16px;
  }
}
</style>