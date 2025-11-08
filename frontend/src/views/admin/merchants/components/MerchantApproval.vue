<template>
  <el-dialog
    v-model="visible"
    title="商家审核"
    width="60%"
    :before-close="handleClose">
    <div v-if="merchant" class="approval-content">
      <!-- 商家基本信息 -->
      <el-card class="merchant-summary" shadow="never">
        <template #header>
          <span>商家基本信息</span>
        </template>
        <div class="summary-content">
          <div class="merchant-info">
            <img :src="merchant.avatar" :alt="merchant.shopName" class="merchant-avatar" />
            <div class="info-details">
              <h3 class="shop-name">{{ merchant.shopName }}</h3>
              <div class="info-item">
                <span class="label">联系人：</span>
                <span class="value">{{ merchant.contactPerson }}</span>
              </div>
              <div class="info-item">
                <span class="label">手机号：</span>
                <span class="value">{{ merchant.phone }}</span>
              </div>
              <div class="info-item">
                <span class="label">邮箱：</span>
                <span class="value">{{ merchant.email }}</span>
              </div>
              <div class="info-item">
                <span class="label">经营类目：</span>
                <el-tag size="small">{{ getCategoryName(merchant.category) }}</el-tag>
              </div>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 审核表单 -->
      <el-card class="approval-form" shadow="never">
        <template #header>
          <span>审核信息</span>
        </template>
        
        <el-form
          ref="formRef"
          :model="approvalForm"
          :rules="formRules"
          label-width="120px">
          
          <el-form-item label="审核结果" prop="result" required>
            <el-radio-group v-model="approvalForm.result">
              <el-radio value="approved">
                <el-icon color="#67c23a"><Check /></el-icon>
                审核通过
              </el-radio>
              <el-radio value="rejected">
                <el-icon color="#f56c6c"><Close /></el-icon>
                审核拒绝
              </el-radio>
            </el-radio-group>
          </el-form-item>

          <!-- 通过时的设置 -->
          <template v-if="approvalForm.result === 'approved'">
            <el-form-item label="店铺等级" prop="shopLevel">
              <el-select v-model="approvalForm.shopLevel" placeholder="请选择店铺等级">
                <el-option label="一星店铺" :value="1"></el-option>
                <el-option label="二星店铺" :value="2"></el-option>
                <el-option label="三星店铺" :value="3"></el-option>
                <el-option label="四星店铺" :value="4"></el-option>
                <el-option label="五星店铺" :value="5"></el-option>
              </el-select>
            </el-form-item>

            <el-form-item label="佣金比例" prop="commissionRate">
              <el-input-number
                v-model="approvalForm.commissionRate"
                :min="0"
                :max="20"
                :precision="2"
                :step="0.1"
                placeholder="请输入佣金比例">
              </el-input-number>
              <span class="input-suffix">%</span>
            </el-form-item>

            <el-form-item label="保证金" prop="deposit">
              <el-input-number
                v-model="approvalForm.deposit"
                :min="0"
                :step="1000"
                placeholder="请输入保证金金额">
              </el-input-number>
              <span class="input-suffix">元</span>
            </el-form-item>

            <el-form-item label="特殊权限">
              <el-checkbox-group v-model="approvalForm.permissions">
                <el-checkbox value="featured">推荐商家</el-checkbox>
                <el-checkbox value="priority">优先展示</el-checkbox>
                <el-checkbox value="bulk_upload">批量上传</el-checkbox>
                <el-checkbox value="advanced_analytics">高级分析</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
          </template>

          <el-form-item label="审核备注" prop="remark">
            <el-input
              v-model="approvalForm.remark"
              type="textarea"
              :rows="4"
              :placeholder="approvalForm.result === 'approved' ? '请输入通过备注（可选）' : '请输入拒绝原因'"
              maxlength="500"
              show-word-limit>
            </el-input>
          </el-form-item>

          <el-form-item label="通知方式">
            <el-checkbox-group v-model="approvalForm.notifyMethods">
              <el-checkbox value="email">邮件通知</el-checkbox>
              <el-checkbox value="sms">短信通知</el-checkbox>
              <el-checkbox value="system">站内消息</el-checkbox>
            </el-checkbox-group>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 审核历史 -->
      <el-card class="approval-history" shadow="never" v-if="approvalHistory.length > 0">
        <template #header>
          <span>审核历史</span>
        </template>
        
        <el-timeline>
          <el-timeline-item
            v-for="(item, index) in approvalHistory"
            :key="index"
            :timestamp="formatDateTime(item.time)"
            :type="getTimelineType(item.result)">
            <div class="history-item">
              <div class="history-header">
                <span class="operator">{{ item.operator }}</span>
                <el-tag :type="getResultType(item.result)" size="small">
                  {{ getResultText(item.result) }}
                </el-tag>
              </div>
              <div class="history-content" v-if="item.remark">
                {{ item.remark }}
              </div>
            </div>
          </el-timeline-item>
        </el-timeline>
      </el-card>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button
          type="primary"
          @click="submitApproval"
          :loading="submitting">
          提交审核
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Check, Close } from '@element-plus/icons-vue'

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

// 审核表单
const approvalForm = reactive({
  result: 'approved',
  shopLevel: 2,
  commissionRate: 5.0,
  deposit: 10000,
  permissions: [],
  remark: '',
  notifyMethods: ['email', 'system']
})

// 表单验证规则
const formRules = {
  result: [
    { required: true, message: '请选择审核结果', trigger: 'change' }
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
  remark: [
    {
      validator: (rule, value, callback) => {
        if (approvalForm.result === 'rejected' && !value) {
          callback(new Error('审核拒绝时必须填写拒绝原因'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 审核历史将从API获取
const approvalHistory = ref([])

// 类目映射
const categoryMap = {
  electronics: '数码电子',
  clothing: '服装鞋帽',
  home: '家居用品',
  beauty: '美妆护肤',
  food: '食品饮料',
  sports: '运动户外'
}

// 方法
const handleClose = () => {
  visible.value = false
  resetForm()
}

const resetForm = () => {
  Object.assign(approvalForm, {
    result: 'approved',
    shopLevel: 2,
    commissionRate: 5.0,
    deposit: 10000,
    permissions: [],
    remark: '',
    notifyMethods: ['email', 'system']
  })
}

const getCategoryName = (category) => {
  return categoryMap[category] || category
}

const getResultType = (result) => {
  const typeMap = {
    submitted: 'info',
    approved: 'success',
    rejected: 'danger',
    pending: 'warning'
  }
  return typeMap[result] || 'info'
}

const getResultText = (result) => {
  const textMap = {
    submitted: '提交申请',
    approved: '审核通过',
    rejected: '审核拒绝',
    pending: '待审核'
  }
  return textMap[result] || result
}

const getTimelineType = (result) => {
  const typeMap = {
    submitted: 'primary',
    approved: 'success',
    rejected: 'danger',
    pending: 'warning'
  }
  return typeMap[result] || 'primary'
}

const formatDateTime = (dateString) => {
  return new Date(dateString).toLocaleString('zh-CN')
}

const submitApproval = async () => {
  try {
    // 表单验证
    await formRef.value.validate()
    
    submitting.value = true
    
    // 构建提交数据
    const submitData = {
      merchantId: props.merchant.id,
      result: approvalForm.result,
      remark: approvalForm.remark,
      notifyMethods: approvalForm.notifyMethods
    }
    
    // 如果是通过，添加额外配置
    if (approvalForm.result === 'approved') {
      Object.assign(submitData, {
        shopLevel: approvalForm.shopLevel,
        commissionRate: approvalForm.commissionRate,
        deposit: approvalForm.deposit,
        permissions: approvalForm.permissions
      })
    }
    
    // 调用API提交审核
    // await submitMerchantApprovalApi(submitData)
    
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    ElMessage.success(
      approvalForm.result === 'approved' ? '审核通过成功' : '审核拒绝成功'
    )
    
    emit('refresh')
    handleClose()
    
  } catch (error) {
    if (error !== 'validation failed') {
      ElMessage.error('提交审核失败')
    }
  } finally {
    submitting.value = false
  }
}

// 监听审核结果变化，重置相关字段
watch(() => approvalForm.result, (newResult) => {
  if (newResult === 'rejected') {
    approvalForm.shopLevel = null
    approvalForm.commissionRate = null
    approvalForm.deposit = null
    approvalForm.permissions = []
  } else if (newResult === 'approved') {
    approvalForm.shopLevel = 2
    approvalForm.commissionRate = 5.0
    approvalForm.deposit = 10000
  }
})

// 监听商家变化，重置表单
watch(() => props.merchant, () => {
  resetForm()
}, { immediate: true })
</script>

<style scoped>
.approval-content {
  max-height: 70vh;
  overflow-y: auto;
}

.merchant-summary {
  margin-bottom: 20px;
}

.summary-content {
  padding: 10px 0;
}

.merchant-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.merchant-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  object-fit: cover;
}

.info-details {
  flex: 1;
}

.shop-name {
  margin: 0 0 12px 0;
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 6px;
}

.label {
  width: 70px;
  color: #6b7280;
  font-size: 14px;
}

.value {
  color: #1f2937;
  font-size: 14px;
}

.approval-form {
  margin-bottom: 20px;
}

.input-suffix {
  margin-left: 8px;
  color: #6b7280;
  font-size: 14px;
}

.approval-history {
  margin-bottom: 20px;
}

.history-item {
  padding: 8px 0;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.operator {
  font-weight: 500;
  color: #1f2937;
}

.history-content {
  color: #6b7280;
  font-size: 14px;
  line-height: 1.5;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

:deep(.el-radio) {
  display: flex;
  align-items: center;
  margin-right: 30px;
  margin-bottom: 12px;
}

:deep(.el-radio__label) {
  display: flex;
  align-items: center;
  gap: 6px;
}

:deep(.el-checkbox) {
  margin-right: 20px;
  margin-bottom: 8px;
}

@media (max-width: 768px) {
  .merchant-info {
    flex-direction: column;
    text-align: center;
  }
  
  .info-item {
    justify-content: center;
  }
  
  .label {
    width: auto;
    margin-right: 8px;
  }
}
</style>