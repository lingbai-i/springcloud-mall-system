<template>
  <div class="merchant-register">
    <div class="register-container">
      <div class="register-header">
        <img src="/logo.png" alt="Logo" class="logo" />
        <h1 class="title">商家入驻申请</h1>
        <p class="subtitle">加入我们，开启您的电商之旅</p>
      </div>
      
      <el-steps :active="currentStep" finish-status="success" align-center class="steps">
        <el-step title="选择类型" />
        <el-step title="填写信息" />
        <el-step title="提交审核" />
      </el-steps>
      
      <!-- 第一步：选择主体类型 -->
      <div v-if="currentStep === 0" class="step-content">
        <h2 class="step-title">请选择您的主体类型</h2>
        <div class="entity-types">
          <div
            class="entity-card"
            :class="{ active: registerForm.entityType === 'enterprise' }"
            @click="selectEntityType('enterprise')"
          >
            <el-icon class="entity-icon"><OfficeBuilding /></el-icon>
            <h3>企业</h3>
            <p class="entity-desc">拥有营业执照的企业法人</p>
            <div class="entity-features">
              <div class="feature-item">✓ 旗舰店</div>
              <div class="feature-item">✓ 专卖店</div>
              <div class="feature-item">✓ 专营店</div>
              <div class="feature-item">✓ 普通企业店</div>
            </div>
          </div>
          
          <div
            class="entity-card"
            :class="{ active: registerForm.entityType === 'individual' }"
            @click="selectEntityType('individual')"
          >
            <el-icon class="entity-icon"><Shop /></el-icon>
            <h3>个体</h3>
            <p class="entity-desc">拥有个体工商户营业执照</p>
            <div class="entity-features">
              <div class="feature-item">✓ 小店</div>
              <div class="feature-item">✓ 本人开店</div>
            </div>
          </div>
          
          <div
            class="entity-card"
            :class="{ active: registerForm.entityType === 'personal' }"
            @click="selectEntityType('personal')"
          >
            <el-icon class="entity-icon"><User /></el-icon>
            <h3>个人</h3>
            <p class="entity-desc">使用身份证开店的个人</p>
            <div class="entity-features">
              <div class="feature-item">✓ 小店</div>
              <div class="feature-item">✓ 身份证开店</div>
            </div>
          </div>
        </div>
        
        <div class="step-actions">
          <el-button size="large" @click="goBack">返回</el-button>
          <el-button
            type="primary"
            size="large"
            :disabled="!registerForm.entityType"
            @click="nextStep"
          >
            下一步
          </el-button>
        </div>
      </div>
      
      <!-- 第二步：填写信息 -->
      <div v-if="currentStep === 1" class="step-content">
        <h2 class="step-title">填写{{ entityTypeText }}信息</h2>
        
        <el-form
          ref="registerFormRef"
          :model="registerForm"
          :rules="registerRules"
          label-width="120px"
          class="register-form"
          size="large"
        >
          <!-- 店铺类型选择（仅企业显示） -->
          <div v-if="registerForm.entityType === 'enterprise'" class="form-section">
            <h3 class="section-title">店铺类型</h3>
            <el-form-item label="店铺类型" prop="shopType" required>
              <el-radio-group v-model="registerForm.shopType" class="shop-type-radio-group">
                <el-radio label="flagship" class="shop-type-radio">
                  <span class="radio-label">旗舰店</span>
                  <el-tooltip
                    content="以自有品牌或品牌一级独占性授权书开店"
                    placement="top"
                    effect="light"
                  >
                    <el-icon class="info-icon"><QuestionFilled /></el-icon>
                  </el-tooltip>
                </el-radio>
                <el-radio label="specialty" class="shop-type-radio">
                  <span class="radio-label">专卖店</span>
                  <el-tooltip
                    content="以自有品牌或品牌授权书开店"
                    placement="top"
                    effect="light"
                  >
                    <el-icon class="info-icon"><QuestionFilled /></el-icon>
                  </el-tooltip>
                </el-radio>
                <el-radio label="franchise" class="shop-type-radio">
                  <span class="radio-label">专营店</span>
                  <el-tooltip
                    content="以自有品牌或品牌授权书开设的主要经营同一大类商品的店铺"
                    placement="top"
                    effect="light"
                  >
                    <el-icon class="info-icon"><QuestionFilled /></el-icon>
                  </el-tooltip>
                </el-radio>
                <el-radio label="ordinary" class="shop-type-radio">
                  <span class="radio-label">普通企业店</span>
                  <el-tooltip
                    content="以企业营业执照及法定代表人身份证件开店，部分类目开放"
                    placement="top"
                    effect="light"
                  >
                    <el-icon class="info-icon"><QuestionFilled /></el-icon>
                  </el-tooltip>
                </el-radio>
              </el-radio-group>
            </el-form-item>
          </div>
          
          <!-- 基本信息 -->
          <div class="form-section">
            <h3 class="section-title">基本信息</h3>
            <el-form-item label="店铺名称" prop="shopName" required>
            <el-input
              v-model="registerForm.shopName"
              placeholder="请输入店铺名称（2-20个字符）"
              clearable
            />
          </el-form-item>
          
            <el-form-item label="联系人" prop="contactName" required>
            <el-input
              v-model="registerForm.contactName"
              placeholder="请输入联系人姓名"
              clearable
            />
          </el-form-item>
          
            <el-form-item label="联系电话" prop="contactPhone" required>
            <el-input
              v-model="registerForm.contactPhone"
              placeholder="请输入联系电话"
              clearable
            />
          </el-form-item>
          
            <el-form-item label="邮箱" prop="email" required>
            <el-input
              v-model="registerForm.email"
              placeholder="请输入邮箱地址"
              clearable
            />
          </el-form-item>
          </div>
          
          <!-- 企业信息（企业类型显示） -->
          <div v-if="registerForm.entityType === 'enterprise'" class="form-section">
            <h3 class="section-title">企业信息</h3>
            <el-form-item label="企业名称" prop="companyName">
              <el-input
                v-model="registerForm.companyName"
                placeholder="请输入营业执照上的企业名称"
                clearable
              />
            </el-form-item>
            
            <el-form-item label="统一社会信用代码" prop="creditCode">
              <el-input
                v-model="registerForm.creditCode"
                placeholder="请输入18位统一社会信用代码"
                maxlength="18"
                clearable
              />
            </el-form-item>
            
            <el-form-item label="法定代表人" prop="legalPerson">
              <el-input
                v-model="registerForm.legalPerson"
                placeholder="请输入法定代表人姓名"
                clearable
              />
            </el-form-item>
            
            <el-form-item label="营业执照" prop="businessLicense">
              <el-upload
                class="license-uploader"
                action="/api/users/upload"
                :show-file-list="false"
                :on-success="(res) => handleUploadSuccess(res, 'businessLicense')"
                :before-upload="beforeFileUpload"
                name="file"
              >
                <img v-if="registerForm.businessLicense" :src="registerForm.businessLicense" class="license-image" />
                <el-icon v-else class="license-uploader-icon"><Plus /></el-icon>
              </el-upload>
              <div class="upload-tip">请上传营业执照照片，支持jpg/png格式，大小不超过2MB</div>
            </el-form-item>
          </div>
          
           <!-- 个体工商户信息（个体类型显示） -->
           <div v-if="registerForm.entityType === 'individual'" class="form-section">
             <h3 class="section-title">个体工商户信息</h3>
            <el-form-item label="个体户名称" prop="companyName">
              <el-input
                v-model="registerForm.companyName"
                placeholder="请输入个体工商户名称"
                clearable
              />
            </el-form-item>
            
            <el-form-item label="统一社会信用代码" prop="creditCode">
              <el-input
                v-model="registerForm.creditCode"
                placeholder="请输入统一社会信用代码或注册号"
                clearable
              />
            </el-form-item>
            
            <el-form-item label="经营者姓名" prop="legalPerson">
              <el-input
                v-model="registerForm.legalPerson"
                placeholder="请输入经营者姓名"
                clearable
              />
            </el-form-item>
            
             <el-form-item label="营业执照" prop="businessLicense">
              <el-upload
                class="license-uploader"
                action="/api/users/upload"
                :show-file-list="false"
                :on-success="(res) => handleUploadSuccess(res, 'businessLicense')"
                :before-upload="beforeFileUpload"
                name="file"
              >
                <img v-if="registerForm.businessLicense" :src="registerForm.businessLicense" class="license-image" />
                <el-icon v-else class="license-uploader-icon"><Plus /></el-icon>
              </el-upload>
               <div class="upload-tip">请上传个体工商户营业执照照片</div>
             </el-form-item>
           </div>
           
           <!-- 个人信息（个人类型显示） -->
           <div v-if="registerForm.entityType === 'personal'" class="form-section">
             <h3 class="section-title">个人身份信息</h3>
            <el-form-item label="真实姓名" prop="legalPerson">
              <el-input
                v-model="registerForm.legalPerson"
                placeholder="请输入真实姓名"
                clearable
              />
            </el-form-item>
            
            <el-form-item label="身份证号" prop="idCard">
              <el-input
                v-model="registerForm.idCard"
                placeholder="请输入身份证号"
                maxlength="18"
                clearable
              />
            </el-form-item>
            
            <el-form-item label="身份证正面" prop="idCardFront">
              <el-upload
                class="license-uploader"
                action="/api/users/upload"
                :show-file-list="false"
                :on-success="(res) => handleUploadSuccess(res, 'idCardFront')"
                :before-upload="beforeFileUpload"
                name="file"
              >
                <img v-if="registerForm.idCardFront" :src="registerForm.idCardFront" class="license-image" />
                <el-icon v-else class="license-uploader-icon"><Plus /></el-icon>
              </el-upload>
              <div class="upload-tip">请上传身份证人像面</div>
            </el-form-item>
            
            <el-form-item label="身份证反面" prop="idCardBack">
              <el-upload
                class="license-uploader"
                action="/api/users/upload"
                :show-file-list="false"
                :on-success="(res) => handleUploadSuccess(res, 'idCardBack')"
                :before-upload="beforeFileUpload"
                name="file"
              >
                <img v-if="registerForm.idCardBack" :src="registerForm.idCardBack" class="license-image" />
                <el-icon v-else class="license-uploader-icon"><Plus /></el-icon>
              </el-upload>
              <div class="upload-tip">请上传身份证国徽面</div>
            </el-form-item>
          </div>
          
           <!-- 账号设置 -->
           <div class="form-section">
             <h3 class="section-title">账号设置</h3>
           <el-form-item label="登录账号" prop="username" required>
            <el-input
              v-model="registerForm.username"
              placeholder="请输入登录账号（6-20个字符）"
              clearable
            />
          </el-form-item>
          
           <el-form-item label="登录密码" prop="password" required>
            <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="请输入登录密码（6-20个字符）"
              show-password
              clearable
            />
          </el-form-item>
          
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              show-password
              clearable
            />
          </el-form-item>
          
          <!-- 协议 -->
          <el-form-item prop="agreement">
            <el-checkbox v-model="registerForm.agreement">
              我已阅读并同意
              <el-link type="primary" @click="showAgreement">《商家入驻协议》</el-link>
              和
              <el-link type="primary" @click="showPrivacy">《隐私政策》</el-link>
             </el-checkbox>
           </el-form-item>
           </div>
         </el-form>
        
        <div class="step-actions">
          <el-button size="large" @click="prevStep">上一步</el-button>
          <el-button type="primary" size="large" @click="nextStep">下一步</el-button>
        </div>
      </div>
      
      <!-- 第三步：确认信息 -->
      <div v-if="currentStep === 2" class="step-content">
        <h2 class="step-title">请确认您的申请信息</h2>
        
        <div class="confirm-content">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="主体类型">
              {{ entityTypeText }}
            </el-descriptions-item>
            <el-descriptions-item label="店铺类型">
              {{ shopTypeText }}
            </el-descriptions-item>
            <el-descriptions-item label="店铺名称">
              {{ registerForm.shopName }}
            </el-descriptions-item>
            <el-descriptions-item label="联系人">
              {{ registerForm.contactName }}
            </el-descriptions-item>
            <el-descriptions-item label="联系电话">
              {{ registerForm.contactPhone }}
            </el-descriptions-item>
            <el-descriptions-item label="邮箱">
              {{ registerForm.email }}
            </el-descriptions-item>
            
            <template v-if="registerForm.entityType === 'enterprise' || registerForm.entityType === 'individual'">
              <el-descriptions-item label="企业/个体户名称">
                {{ registerForm.companyName }}
              </el-descriptions-item>
              <el-descriptions-item label="统一社会信用代码">
                {{ registerForm.creditCode }}
              </el-descriptions-item>
              <el-descriptions-item label="法定代表人/经营者">
                {{ registerForm.legalPerson }}
              </el-descriptions-item>
            </template>
            
            <template v-if="registerForm.entityType === 'personal'">
              <el-descriptions-item label="真实姓名">
                {{ registerForm.legalPerson }}
              </el-descriptions-item>
              <el-descriptions-item label="身份证号">
                {{ registerForm.idCard }}
              </el-descriptions-item>
            </template>
            
            <el-descriptions-item label="登录账号">
              {{ registerForm.username }}
            </el-descriptions-item>
          </el-descriptions>
          
          <el-alert
            title="提示"
            type="info"
            :closable="false"
            class="submit-tip"
            show-icon
          >
            提交申请后，我们将在3个工作日内完成审核。审核结果将通过邮箱和短信通知您。
          </el-alert>
        </div>
        
        <div class="step-actions">
          <el-button size="large" @click="prevStep">上一步</el-button>
          <el-button
            type="primary"
            size="large"
            :loading="submitting"
            @click="handleSubmit"
          >
            {{ submitting ? '提交中...' : '提交申请' }}
          </el-button>
        </div>
      </div>
    </div>
    
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { OfficeBuilding, Shop, User, Plus, QuestionFilled } from '@element-plus/icons-vue'

const router = useRouter()

// 当前步骤
const currentStep = ref(0)

// 表单引用
const registerFormRef = ref()

// 提交状态
const submitting = ref(false)

// 注册表单数据
const registerForm = reactive({
  // 主体类型：enterprise-企业, individual-个体, personal-个人
  entityType: '',
  // 店铺类型：flagship-旗舰店, specialty-专卖店, franchise-专营店, ordinary-普通企业店, small-小店
  shopType: '',
  // 基本信息
  shopName: '',
  contactName: '',
  contactPhone: '',
  email: '',
  // 企业/个体户信息
  companyName: '',
  creditCode: '',
  legalPerson: '',
  businessLicense: '',
  // 个人信息
  idCard: '',
  idCardFront: '',
  idCardBack: '',
  // 账号设置
  username: '',
  password: '',
  confirmPassword: '',
  agreement: false
})

// 表单验证规则
const registerRules = computed(() => {
  const baseRules = {
    shopName: [
      { required: true, message: '请输入店铺名称', trigger: 'blur' },
      { min: 2, max: 20, message: '店铺名称长度在 2 到 20 个字符', trigger: 'blur' }
    ],
    contactName: [
      { required: true, message: '请输入联系人姓名', trigger: 'blur' }
    ],
    contactPhone: [
      { required: true, message: '请输入联系电话', trigger: 'blur' },
      { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
    ],
    email: [
      { required: true, message: '请输入邮箱地址', trigger: 'blur' },
      { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
    ],
    username: [
      { required: true, message: '请输入登录账号', trigger: 'blur' },
      { min: 6, max: 20, message: '账号长度在 6 到 20 个字符', trigger: 'blur' },
      { pattern: /^[a-zA-Z0-9_]+$/, message: '账号只能包含字母、数字和下划线', trigger: 'blur' }
    ],
    password: [
      { required: true, message: '请输入登录密码', trigger: 'blur' },
      { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
    ],
    confirmPassword: [
      { required: true, message: '请再次输入密码', trigger: 'blur' },
      {
        validator: (rule, value, callback) => {
          if (value !== registerForm.password) {
            callback(new Error('两次输入的密码不一致'))
          } else {
            callback()
          }
        },
        trigger: 'blur'
      }
    ],
    agreement: [
      {
        validator: (rule, value, callback) => {
          if (!value) {
            callback(new Error('请阅读并同意商家入驻协议和隐私政策'))
          } else {
            callback()
          }
        },
        trigger: 'change'
      }
    ]
  }
  
  // 根据主体类型添加不同的验证规则
  if (registerForm.entityType === 'enterprise' || registerForm.entityType === 'individual') {
    baseRules.companyName = [
      { required: true, message: '请输入企业/个体户名称', trigger: 'blur' }
    ]
    baseRules.creditCode = [
      { required: true, message: '请输入统一社会信用代码', trigger: 'blur' }
    ]
    baseRules.legalPerson = [
      { required: true, message: '请输入法定代表人/经营者姓名', trigger: 'blur' }
    ]
    baseRules.businessLicense = [
      { required: true, message: '请上传营业执照', trigger: 'change' }
    ]
  }
  
  if (registerForm.entityType === 'enterprise') {
    baseRules.shopType = [
      { required: true, message: '请选择店铺类型', trigger: 'change' }
    ]
  }
  
  if (registerForm.entityType === 'personal') {
    baseRules.legalPerson = [
      { required: true, message: '请输入真实姓名', trigger: 'blur' }
    ]
    baseRules.idCard = [
      { required: true, message: '请输入身份证号', trigger: 'blur' },
      { pattern: /^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/, message: '请输入正确的身份证号', trigger: 'blur' }
    ]
    baseRules.idCardFront = [
      { required: true, message: '请上传身份证正面', trigger: 'change' }
    ]
    baseRules.idCardBack = [
      { required: true, message: '请上传身份证反面', trigger: 'change' }
    ]
  }
  
  return baseRules
})

// 主体类型文本
const entityTypeText = computed(() => {
  const map = {
    enterprise: '企业',
    individual: '个体工商户',
    personal: '个人'
  }
  return map[registerForm.entityType] || ''
})

// 店铺类型文本
const shopTypeText = computed(() => {
  if (registerForm.entityType === 'enterprise') {
    const map = {
      flagship: '旗舰店',
      specialty: '专卖店',
      franchise: '专营店',
      ordinary: '普通企业店'
    }
    return map[registerForm.shopType] || ''
  } else {
    return '小店'
  }
})

// 选择主体类型
const selectEntityType = (type) => {
  registerForm.entityType = type
  // 个体和个人默认为小店
  if (type === 'individual' || type === 'personal') {
    registerForm.shopType = 'small'
  } else {
    registerForm.shopType = ''
  }
}

// 下一步
const nextStep = async () => {
  if (currentStep.value === 0) {
    // 第一步：检查是否选择了主体类型
    if (!registerForm.entityType) {
      ElMessage.warning('请选择主体类型')
      return
    }
  } else if (currentStep.value === 1) {
    // 第二步：验证表单
    if (!registerFormRef.value) return
    try {
      await registerFormRef.value.validate()
    } catch (error) {
      ElMessage.error('请完善表单信息')
      return
    }
  }
  
  currentStep.value++
}

// 上一步
const prevStep = () => {
  currentStep.value--
}

// 返回
const goBack = () => {
  router.back()
}

// 文件上传成功回调（MinIO）
const handleUploadSuccess = (response, fieldName) => {
  console.log('上传成功响应:', response)
  
  if (response.success || response.code === 200) {
    // 提取文件URL
    const fileUrl = response.data?.url || response.data
    registerForm[fieldName] = fileUrl
    ElMessage.success('文件上传成功')
  } else {
    ElMessage.error(response.message || '文件上传失败')
  }
}

// 文件上传前验证
const beforeFileUpload = (file) => {
  const isImage = file.type === 'image/jpeg' || file.type === 'image/png'
  if (!isImage) {
    ElMessage.error('只能上传 JPG 或 PNG 格式的图片!')
    return false
  }
  
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  
  return true
}

// 显示协议
const showAgreement = () => {
  ElMessageBox.alert(
    '这里是商家入驻协议内容...',
    '商家入驻协议',
    { confirmButtonText: '我知道了' }
  )
}

// 显示隐私政策
const showPrivacy = () => {
  ElMessageBox.alert(
    '这里是隐私政策内容...',
    '隐私政策',
    { confirmButtonText: '我知道了' }
  )
}

// 提交申请
const handleSubmit = async () => {
  submitting.value = true
  
  try {
    // 导入API
    const { submitMerchantApplication } = await import('@/api/merchant')
    
    // 调用API提交申请
    const response = await submitMerchantApplication(registerForm)
    
    if (response.success || response.code === 200) {
      ElMessage.success('申请提交成功！我们将在1个工作日内完成审核')
      
      // 跳转到成功页面或登录页面
      setTimeout(() => {
        router.push('/merchant/login')
      }, 1500)
    } else {
      ElMessage.error(response.message || '提交失败')
    }
    
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error(error.message || '提交失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.merchant-register {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  position: relative;
  overflow: hidden;
}

.register-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  padding: 40px;
  width: 100%;
  max-width: 900px;
  position: relative;
  z-index: 1;
}

.register-header {
  text-align: center;
  margin-bottom: 30px;
}

.logo {
  width: 60px;
  height: 60px;
  margin-bottom: 15px;
}

.title {
  font-size: 28px;
  color: #333;
  margin: 0 0 10px 0;
  font-weight: bold;
}

.subtitle {
  font-size: 14px;
  color: #666;
  margin: 0;
}

.steps {
  margin-bottom: 40px;
}

.step-content {
  min-height: 400px;
}

.step-title {
  text-align: center;
  font-size: 20px;
  color: #333;
  margin-bottom: 30px;
}

/* 主体类型选择卡片 */
.entity-types {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 40px;
}

.entity-card {
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  padding: 30px 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.entity-card:hover {
  border-color: #667eea;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
}

.entity-card.active {
  border-color: #667eea;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.entity-icon {
  font-size: 48px;
  color: #667eea;
  margin-bottom: 15px;
}

.entity-card h3 {
  font-size: 20px;
  color: #333;
  margin: 0 0 10px 0;
}

.entity-desc {
  font-size: 14px;
  color: #666;
  margin: 0 0 20px 0;
}

.entity-features {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.feature-item {
  font-size: 13px;
  color: #888;
  text-align: left;
}

/* 表单样式 */
.register-form {
  margin-bottom: 30px;
}

.radio-content {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  margin-left: 8px;
}

.radio-desc {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
  display: block;
  line-height: 1.5;
}

/* 表单区块样式 */
.form-section {
  background-color: #f8f9fa;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 24px;
  border: 2px solid #e0e0e0;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0 0 20px 0;
  padding-bottom: 12px;
  border-bottom: 2px solid #e0e0e0;
}

/* 店铺类型radio优化布局 */
.shop-type-radio-group {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.shop-type-radio {
  display: inline-flex !important;
  align-items: center !important;
  margin-right: 0 !important;
  margin-bottom: 0 !important;
  width: 100% !important;
  padding: 12px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  transition: all 0.3s;
  background-color: #ffffff;
}

.shop-type-radio:hover {
  background-color: #f5f7fa;
  border-color: #667eea;
}

.shop-type-radio .radio-label {
  margin-left: 8px;
  font-weight: 500;
  color: #333;
  flex: 1;
}

.shop-type-radio .info-icon {
  margin-left: 6px;
  font-size: 16px;
  color: #909399;
  cursor: help;
  transition: color 0.3s;
}

.shop-type-radio .info-icon:hover {
  color: #667eea;
}

/* Element Plus Radio组件样式调整 */
:deep(.el-radio) {
  display: inline-flex;
  align-items: center;
  margin-right: 0;
  white-space: normal;
}

:deep(.el-radio__label) {
  padding-left: 0;
  display: inline-flex;
  align-items: center;
  width: 100%;
}

:deep(.el-radio__input) {
  align-self: flex-start;
  margin-top: 2px;
}

/* Tooltip过渡动画 */
:deep(.el-tooltip__popper) {
  transition: opacity 0.3s ease-in-out;
}

/* 表单项间距 */
.register-form :deep(.el-form-item) {
  margin-bottom: 16px;
}

/* 必填字段标记 */
:deep(.el-form-item.is-required:not(.is-no-asterisk)) > .el-form-item__label:before {
  content: '*';
  color: #f56c6c;
  margin-right: 4px;
}

/* 上传组件 */
.license-uploader {
  width: 180px;
  height: 180px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  overflow: hidden;
  transition: border-color 0.3s;
}

.license-uploader:hover {
  border-color: #667eea;
}

.license-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 180px;
  height: 180px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.license-image {
  width: 180px;
  height: 180px;
  object-fit: cover;
}

.upload-tip {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}

/* 确认信息 */
.confirm-content {
  margin-bottom: 30px;
}

.submit-tip {
  margin-top: 20px;
}

/* 步骤操作按钮 */
.step-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 40px;
}

/* 背景装饰 */
.bg-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  overflow: hidden;
  z-index: 0;
}

.circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  animation: float 15s infinite ease-in-out;
}

.circle-1 {
  width: 300px;
  height: 300px;
  top: -100px;
  left: -100px;
}

.circle-2 {
  width: 200px;
  height: 200px;
  bottom: -50px;
  right: -50px;
  animation-delay: 5s;
}

.circle-3 {
  width: 150px;
  height: 150px;
  top: 50%;
  left: 50%;
  animation-delay: 10s;
}

@keyframes float {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  33% {
    transform: translate(30px, -30px) scale(1.1);
  }
  66% {
    transform: translate(-20px, 20px) scale(0.9);
  }
}

/* 响应式 */
@media (max-width: 768px) {
  .register-container {
    padding: 30px 20px;
  }
  
  .entity-types {
    grid-template-columns: 1fr;
  }
  
  .step-actions {
    flex-direction: column;
  }
  
  .step-actions .el-button {
    width: 100%;
  }
}
</style>
