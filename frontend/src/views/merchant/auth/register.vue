<template>
  <div class="merchant-register">
    <div class="register-container">
      <div class="register-header">
        <!-- 修复：使用正确的商标图片路径，统一品牌形象 -->
        <img src="/商标png.png" alt="Logo" class="logo" />
        <h1 class="title">商家入驻申请</h1>
        <p class="subtitle">秉承匠心 · 诚就百物 · 开启您的电商之旅</p>
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
  /* 使用与后台主区域一致的浅绿色渐变 */
  background: linear-gradient(135deg, #f4f7f5 0%, #d1dbd3 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  position: relative;
  overflow: hidden;
}

.register-container {
  /* 玻璃拟态效果 */
  background: rgba(255, 255, 255, 0.8);
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(58, 80, 68, 0.1);
  backdrop-filter: blur(15px);
  border: 1px solid rgba(255, 255, 255, 0.5);
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
  height: 80px;
  margin-bottom: 20px;
  filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.05));
}

.title {
  font-size: 28px;
  /* 使用品牌深绿色 */
  color: #3a5044;
  margin: 0 0 10px 0;
  font-weight: bold;
}

.subtitle {
  font-size: 14px;
  color: #8a9b8e;
  margin: 0;
}

.steps {
  margin-bottom: 40px;
}

:deep(.el-step__title.is-success) {
  color: #3a5044;
}

:deep(.el-step__head.is-success) {
  color: #3a5044;
  border-color: #3a5044;
}

.step-content {
  min-height: 400px;
}

.step-title {
  text-align: center;
  font-size: 20px;
  color: #3a5044;
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
  border: 2px solid rgba(58, 80, 68, 0.1);
  background: rgba(255, 255, 255, 0.5);
  border-radius: 12px;
  padding: 30px 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.entity-card:hover {
  border-color: #3a5044;
  box-shadow: 0 4px 12px rgba(58, 80, 68, 0.1);
  transform: translateY(-2px);
}

.entity-card.active {
  border-color: #3a5044;
  background: rgba(58, 80, 68, 0.05);
  box-shadow: 0 4px 12px rgba(58, 80, 68, 0.2);
}

.entity-icon {
  font-size: 48px;
  color: #3a5044;
  margin-bottom: 15px;
}

.entity-card h3 {
  font-size: 20px;
  color: #3a5044;
  margin: 0 0 10px 0;
}

.entity-desc {
  font-size: 14px;
  color: #6e7d73;
  margin: 0 0 20px 0;
}

.entity-features {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.feature-item {
  font-size: 13px;
  color: #8a9b8e;
  text-align: left;
}

/* 表单样式 */
.register-form {
  margin-bottom: 30px;
}

:deep(.el-input__wrapper) {
  background-color: rgba(255, 255, 255, 0.5);
  box-shadow: 0 0 0 1px rgba(58, 80, 68, 0.1) inset;
  border-radius: 8px;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #3a5044 inset !important;
}

/* 表单区块样式 */
.form-section {
  background-color: rgba(58, 80, 68, 0.02);
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  border: 1px solid rgba(58, 80, 68, 0.05);
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #3a5044;
  margin: 0 0 20px 0;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(58, 80, 68, 0.1);
}

/* 店铺类型radio优化布局 */
.shop-type-radio {
  display: inline-flex !important;
  align-items: center !important;
  margin-right: 0 !important;
  margin-bottom: 0 !important;
  width: 100% !important;
  padding: 12px 16px;
  border: 1px solid rgba(58, 80, 68, 0.1);
  border-radius: 8px;
  transition: all 0.3s;
  background-color: rgba(255, 255, 255, 0.5);
}

.shop-type-radio:hover {
  background-color: #ffffff;
  border-color: #3a5044;
}

:deep(.el-radio__input.is-checked .el-radio__inner) {
  background-color: #3a5044;
  border-color: #3a5044;
}

:deep(.el-radio__input.is-checked + .el-radio__label) {
  color: #3a5044;
}

/* 步骤操作按钮 */
.step-actions .el-button--primary {
  background: linear-gradient(135deg, #4a6355, #3a5044);
  border: none;
  box-shadow: 0 4px 12px rgba(58, 80, 68, 0.2);
}

.step-actions .el-button--primary:hover {
  background: linear-gradient(135deg, #5a7566, #4a6355);
  transform: translateY(-1px);
}

/* 背景装饰 */
.bg-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
}

.circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(58, 80, 68, 0.03);
  animation: float 10s ease-in-out infinite;
}

.circle-1 {
  width: 400px;
  height: 400px;
  top: -150px;
  left: -150px;
}

.circle-2 {
  width: 300px;
  height: 300px;
  bottom: -100px;
  right: -100px;
  animation-delay: 2s;
}

.circle-3 {
  width: 200px;
  height: 200px;
  top: 20%;
  right: 10%;
  animation-delay: 4s;
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
