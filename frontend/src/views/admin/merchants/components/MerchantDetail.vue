<template>
  <el-dialog
    v-model="visible"
    title="商家详情"
    width="80%"
    :before-close="handleClose">
    <div v-if="merchant" class="merchant-detail">
      <!-- 基本信息 -->
      <el-card class="info-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span>基本信息</span>
            <el-tag :type="getStatusType(merchant.status)" size="large">
              {{ getStatusText(merchant.status) }}
            </el-tag>
          </div>
        </template>
        
        <div class="basic-info">
          <div class="merchant-profile">
            <div class="avatar-section">
              <img :src="merchant.avatar" :alt="merchant.shopName" class="merchant-avatar" />
              <div class="rating-section">
                <el-rate
                  v-model="merchant.rating"
                  disabled
                  show-score
                  text-color="#ff9900"
                  score-template="{value}">
                </el-rate>
              </div>
            </div>
            <div class="profile-info">
              <h3 class="shop-name">{{ merchant.shopName }}</h3>
              <div class="info-row">
                <span class="label">商家ID：</span>
                <span class="value">{{ merchant.id }}</span>
              </div>
              <div class="info-row">
                <span class="label">联系人：</span>
                <span class="value">{{ merchant.contactPerson }}</span>
              </div>
              <div class="info-row">
                <span class="label">手机号：</span>
                <span class="value">{{ merchant.phone }}</span>
              </div>
              <div class="info-row">
                <span class="label">邮箱：</span>
                <span class="value">{{ merchant.email }}</span>
              </div>
              <div class="info-row">
                <span class="label">经营类目：</span>
                <el-tag size="small">{{ getCategoryName(merchant.category) }}</el-tag>
              </div>
            </div>
          </div>
          
          <div class="stats-grid">
            <div class="stat-item">
              <div class="stat-number">{{ merchant.productCount }}</div>
              <div class="stat-label">商品数量</div>
            </div>
            <div class="stat-item">
              <div class="stat-number">{{ merchant.orderCount }}</div>
              <div class="stat-label">订单数量</div>
            </div>
            <div class="stat-item">
              <div class="stat-number">{{ formatDate(merchant.registerTime) }}</div>
              <div class="stat-label">注册时间</div>
            </div>
            <div class="stat-item">
              <div class="stat-number">{{ formatDate(merchant.lastLoginTime) }}</div>
              <div class="stat-label">最后登录</div>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 详细信息标签页 -->
      <el-card class="detail-tabs" shadow="never">
        <el-tabs v-model="activeTab" type="border-card">
          <!-- 店铺信息 -->
          <el-tab-pane label="店铺信息" name="shop">
            <div class="tab-content">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="店铺名称">{{ merchant.shopName }}</el-descriptions-item>
                <el-descriptions-item label="店铺类型">{{ getShopType(merchant.shopType) }}</el-descriptions-item>
                <el-descriptions-item label="经营类目">{{ getCategoryName(merchant.category) }}</el-descriptions-item>
                <el-descriptions-item label="店铺等级">{{ getShopLevel(merchant.shopLevel) }}</el-descriptions-item>
                <el-descriptions-item label="店铺地址" :span="2">
                  {{ merchant.shopAddress || '暂未设置' }}
                </el-descriptions-item>
                <el-descriptions-item label="店铺简介" :span="2">
                  {{ merchant.shopDescription || '暂未设置' }}
                </el-descriptions-item>
              </el-descriptions>
            </div>
          </el-tab-pane>

          <!-- 资质信息 -->
          <el-tab-pane label="资质信息" name="qualification">
            <div class="tab-content">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="营业执照号">{{ merchant.businessLicense || '暂未上传' }}</el-descriptions-item>
                <el-descriptions-item label="法人姓名">{{ merchant.legalPerson || '暂未填写' }}</el-descriptions-item>
                <el-descriptions-item label="法人身份证">{{ merchant.legalIdCard || '暂未填写' }}</el-descriptions-item>
                <el-descriptions-item label="注册资本">{{ merchant.registeredCapital || '暂未填写' }}</el-descriptions-item>
                <el-descriptions-item label="经营范围" :span="2">
                  {{ merchant.businessScope || '暂未填写' }}
                </el-descriptions-item>
              </el-descriptions>
              
              <div class="qualification-images" v-if="merchant.qualificationImages">
                <h4>资质证件</h4>
                <div class="image-grid">
                  <div
                    v-for="(image, index) in merchant.qualificationImages"
                    :key="index"
                    class="image-item">
                    <img :src="image" :alt="`资质证件${index + 1}`" @click="previewImage(image)" />
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <!-- 经营数据 -->
          <el-tab-pane label="经营数据" name="business">
            <div class="tab-content">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-card class="data-card">
                    <template #header>
                      <span>销售数据</span>
                    </template>
                    <div class="data-list">
                      <div class="data-item">
                        <span class="data-label">总销售额：</span>
                        <span class="data-value">¥{{ formatMoney(merchant.totalSales) }}</span>
                      </div>
                      <div class="data-item">
                        <span class="data-label">本月销售额：</span>
                        <span class="data-value">¥{{ formatMoney(merchant.monthlySales) }}</span>
                      </div>
                      <div class="data-item">
                        <span class="data-label">平均客单价：</span>
                        <span class="data-value">¥{{ formatMoney(merchant.avgOrderValue) }}</span>
                      </div>
                    </div>
                  </el-card>
                </el-col>
                <el-col :span="12">
                  <el-card class="data-card">
                    <template #header>
                      <span>服务数据</span>
                    </template>
                    <div class="data-list">
                      <div class="data-item">
                        <span class="data-label">服务评分：</span>
                        <span class="data-value">{{ merchant.serviceRating }}/5.0</span>
                      </div>
                      <div class="data-item">
                        <span class="data-label">发货速度：</span>
                        <span class="data-value">{{ merchant.shippingSpeed }}小时</span>
                      </div>
                      <div class="data-item">
                        <span class="data-label">退款率：</span>
                        <span class="data-value">{{ merchant.refundRate }}%</span>
                      </div>
                    </div>
                  </el-card>
                </el-col>
              </el-row>
            </div>
          </el-tab-pane>

          <!-- 操作记录 -->
          <el-tab-pane label="操作记录" name="logs">
            <div class="tab-content">
              <el-table :data="operationLogs" stripe>
                <el-table-column prop="time" label="操作时间" width="180">
                  <template #default="{ row }">
                    {{ formatDateTime(row.time) }}
                  </template>
                </el-table-column>
                <el-table-column prop="operator" label="操作人" width="120"></el-table-column>
                <el-table-column prop="action" label="操作类型" width="120">
                  <template #default="{ row }">
                    <el-tag :type="getActionType(row.action)" size="small">
                      {{ row.action }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="description" label="操作描述"></el-table-column>
                <el-table-column prop="result" label="结果" width="100">
                  <template #default="{ row }">
                    <el-tag :type="row.result === '成功' ? 'success' : 'danger'" size="small">
                      {{ row.result }}
                    </el-tag>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
        <el-button
          v-if="merchant && merchant.status === 'pending'"
          type="success"
          @click="approveMerchant">
          审核通过
        </el-button>
        <el-button
          v-if="merchant && merchant.status === 'pending'"
          type="danger"
          @click="rejectMerchant">
          审核拒绝
        </el-button>
        <el-button
          v-if="merchant && merchant.status === 'approved'"
          type="warning"
          @click="disableMerchant">
          禁用商家
        </el-button>
        <el-button
          v-if="merchant && merchant.status === 'disabled'"
          type="success"
          @click="enableMerchant">
          启用商家
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

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

const activeTab = ref('shop')

// 模拟操作记录数据
const operationLogs = ref([
  {
    time: new Date(Date.now() - 1000 * 60 * 60 * 24 * 1),
    operator: '管理员',
    action: '审核通过',
    description: '商家资质审核通过，允许正常经营',
    result: '成功'
  },
  {
    time: new Date(Date.now() - 1000 * 60 * 60 * 24 * 3),
    operator: '系统',
    action: '注册申请',
    description: '商家提交入驻申请',
    result: '成功'
  }
])

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
}

const getCategoryName = (category) => {
  return categoryMap[category] || category
}

const getStatusType = (status) => {
  const typeMap = {
    pending: 'warning',
    approved: 'success',
    rejected: 'danger',
    disabled: 'info'
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status) => {
  const textMap = {
    pending: '待审核',
    approved: '已通过',
    rejected: '已拒绝',
    disabled: '已禁用'
  }
  return textMap[status] || status
}

const getShopType = (type) => {
  const typeMap = {
    personal: '个人店铺',
    enterprise: '企业店铺',
    flagship: '旗舰店'
  }
  return typeMap[type] || '普通店铺'
}

const getShopLevel = (level) => {
  const levelMap = {
    1: '一星店铺',
    2: '二星店铺',
    3: '三星店铺',
    4: '四星店铺',
    5: '五星店铺'
  }
  return levelMap[level] || '普通店铺'
}

const getActionType = (action) => {
  const typeMap = {
    '审核通过': 'success',
    '审核拒绝': 'danger',
    '禁用': 'warning',
    '启用': 'success',
    '注册申请': 'info'
  }
  return typeMap[action] || 'info'
}

const formatDate = (dateString) => {
  return new Date(dateString).toLocaleDateString('zh-CN')
}

const formatDateTime = (dateString) => {
  return new Date(dateString).toLocaleString('zh-CN')
}

const formatMoney = (amount) => {
  return (amount || 0).toLocaleString('zh-CN')
}

const previewImage = (imageUrl) => {
  // 这里可以实现图片预览功能
  window.open(imageUrl, '_blank')
}

const approveMerchant = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要审核通过商家"${props.merchant.shopName}"吗？`,
      '确认审核通过',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'success'
      }
    )
    
    // 调用API审核通过
    // await approveMerchantApi(props.merchant.id)
    
    ElMessage.success('审核通过成功')
    emit('refresh')
    handleClose()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('审核通过失败')
    }
  }
}

const rejectMerchant = async () => {
  try {
    const { value: reason } = await ElMessageBox.prompt(
      '请输入拒绝原因：',
      '审核拒绝',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPattern: /.+/,
        inputErrorMessage: '请输入拒绝原因'
      }
    )
    
    // 调用API审核拒绝
    // await rejectMerchantApi(props.merchant.id, reason)
    
    ElMessage.success('审核拒绝成功')
    emit('refresh')
    handleClose()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('审核拒绝失败')
    }
  }
}

const disableMerchant = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要禁用商家"${props.merchant.shopName}"吗？`,
      '确认禁用',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 调用API禁用商家
    // await disableMerchantApi(props.merchant.id)
    
    ElMessage.success('商家已禁用')
    emit('refresh')
    handleClose()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('禁用商家失败')
    }
  }
}

const enableMerchant = async () => {
  try {
    // 调用API启用商家
    // await enableMerchantApi(props.merchant.id)
    
    ElMessage.success('商家已启用')
    emit('refresh')
    handleClose()
  } catch (error) {
    ElMessage.error('启用商家失败')
  }
}

// 监听商家数据变化
watch(() => props.merchant, (newMerchant) => {
  if (newMerchant) {
    /**
     * 修改日志 V1.0 2025-11-08T08:46:54+08:00：
     * 修复无效对象字面量导致的语法错误，避免构建失败。
     * 说明：商家详细数据由后端接口提供，此处仅监听并确保对话框展示逻辑正常。
     * 为避免修改 props（Vue 约定不在子组件中直接修改 props），未在此处填充缺省字段。
     * 如需展示缺省值（例如 shippingSpeed/refundRate），建议在模板中提供兜底显示或使用 computed 包装。
     * @author lingbai
     */
    // 此 watcher 保持最小职责：存在商家数据时不做额外变更，避免副作用。
  }
}, { immediate: true })
</script>

<style scoped>
.merchant-detail {
  max-height: 70vh;
  overflow-y: auto;
}

.info-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.basic-info {
  display: flex;
  gap: 30px;
}

.merchant-profile {
  flex: 1;
  display: flex;
  gap: 20px;
}

.avatar-section {
  text-align: center;
}

.merchant-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  margin-bottom: 10px;
}

.rating-section {
  margin-top: 10px;
}

.profile-info {
  flex: 1;
}

.shop-name {
  margin: 0 0 16px 0;
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
}

.info-row {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.label {
  width: 80px;
  color: #6b7280;
  font-size: 14px;
}

.value {
  color: #1f2937;
  font-size: 14px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  min-width: 200px;
}

.stat-item {
  text-align: center;
  padding: 16px;
  background: #f9fafb;
  border-radius: 8px;
}

.stat-number {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  color: #6b7280;
}

.detail-tabs {
  margin-bottom: 20px;
}

.tab-content {
  padding: 20px 0;
}

.data-card {
  height: 100%;
}

.data-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.data-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f3f4f6;
}

.data-item:last-child {
  border-bottom: none;
}

.data-label {
  color: #6b7280;
  font-size: 14px;
}

.data-value {
  color: #1f2937;
  font-weight: 500;
  font-size: 14px;
}

.qualification-images {
  margin-top: 20px;
}

.qualification-images h4 {
  margin-bottom: 16px;
  color: #1f2937;
}

.image-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.image-item {
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e5e7eb;
}

.image-item img {
  width: 100%;
  height: 150px;
  object-fit: cover;
  cursor: pointer;
  transition: transform 0.2s;
}

.image-item img:hover {
  transform: scale(1.05);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

@media (max-width: 768px) {
  .basic-info {
    flex-direction: column;
  }
  
  .merchant-profile {
    flex-direction: column;
    text-align: center;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .image-grid {
    grid-template-columns: 1fr;
  }
}
</style>
