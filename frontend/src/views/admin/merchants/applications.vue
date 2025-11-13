<template>
  <div class="applications-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>商家入驻申请管理</h2>
          <p class="subtitle">审核商家入驻申请，管理商家资质信息</p>
        </div>
      </template>
      
      <!-- 筛选器 -->
      <el-form :inline="true" class="filter-form">
        <el-form-item label="审批状态">
          <el-select v-model="filterForm.status" placeholder="全部状态" clearable @change="loadApplications">
            <el-option label="待审核" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已拒绝" :value="2" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="搜索">
          <el-input
            v-model="filterForm.keyword"
            placeholder="店铺名称/联系人/电话"
            clearable
            @clear="loadApplications"
            @keyup.enter="loadApplications"
            style="width: 250px"
          >
            <template #append>
              <el-button icon="Search" @click="loadApplications">搜索</el-button>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="loadApplications" icon="Refresh">刷新</el-button>
        </el-form-item>
      </el-form>
      
      <!-- 统计卡片 -->
      <div class="stats-cards">
        <el-row :gutter="20">
          <el-col :span="6">
            <div class="stat-card pending" @click="filterByStatus(0)">
              <div class="stat-icon">
                <el-icon><Clock /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ stats.pending || 0 }}</div>
                <div class="stat-label">待审核</div>
              </div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card approved" @click="filterByStatus(1)">
              <div class="stat-icon">
                <el-icon><CircleCheck /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ stats.approved || 0 }}</div>
                <div class="stat-label">已通过</div>
              </div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card rejected" @click="filterByStatus(2)">
              <div class="stat-icon">
                <el-icon><CircleClose /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ stats.rejected || 0 }}</div>
                <div class="stat-label">已拒绝</div>
              </div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card total" @click="filterByStatus(null)">
              <div class="stat-icon">
                <el-icon><DataAnalysis /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ stats.total || 0 }}</div>
                <div class="stat-label">总申请数</div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
      
      <!-- 申请列表表格 -->
      <el-table
        :data="applications"
        :loading="loading"
        stripe
        border
        style="margin-top: 20px"
        @row-dblclick="viewDetail"
      >
        <el-table-column prop="id" label="申请ID" width="80" align="center" />
        <el-table-column prop="shopName" label="店铺名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="entityTypeText" label="主体类型" width="120" align="center" />
        <el-table-column prop="shopTypeText" label="店铺类型" width="120" align="center" />
        <el-table-column prop="contactName" label="联系人" width="100" />
        <el-table-column prop="contactPhoneMasked" label="联系电话" width="130" />
        <el-table-column prop="createdTime" label="申请时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdTime) }}
          </template>
        </el-table-column>
        <el-table-column label="审批状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.approvalStatus === 0" type="warning">待审核</el-tag>
            <el-tag v-else-if="row.approvalStatus === 1" type="success">已通过</el-tag>
            <el-tag v-else-if="row.approvalStatus === 2" type="danger">已拒绝</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="viewDetail(row)">
              <el-icon><View /></el-icon>
              查看
            </el-button>
            <el-button
              v-if="row.approvalStatus === 0"
              size="small"
              type="success"
              link
              @click="openApprovalDialog(row)"
            >
              <el-icon><CircleCheck /></el-icon>
              审批
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadApplications"
        @current-change="loadApplications"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>
    
    <!-- 审批对话框 -->
    <el-dialog
      v-model="approvalDialogVisible"
      :title="'审批申请 - ' + (currentApplication?.shopName || '')"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-scrollbar max-height="600px">
        <el-descriptions :column="2" border v-if="currentApplication">
          <el-descriptions-item label="店铺名称" :span="2">
            <span class="highlight-text">{{ currentApplication.shopName }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="主体类型">
            {{ currentApplication.entityTypeText }}
          </el-descriptions-item>
          <el-descriptions-item label="店铺类型">
            {{ currentApplication.shopTypeText || '未填写' }}
          </el-descriptions-item>
          <el-descriptions-item label="联系人">
            {{ currentApplication.contactName }}
          </el-descriptions-item>
          <el-descriptions-item label="联系电话">
            {{ currentApplication.contactPhone }}
          </el-descriptions-item>
          <el-descriptions-item label="邮箱" :span="2">
            {{ currentApplication.email || '未填写' }}
          </el-descriptions-item>
          
          <!-- 企业信息 -->
          <template v-if="currentApplication.companyName">
            <el-descriptions-item label="企业名称" :span="2">
              {{ currentApplication.companyName }}
            </el-descriptions-item>
            <el-descriptions-item label="信用代码" :span="2">
              {{ currentApplication.creditCode }}
            </el-descriptions-item>
            <el-descriptions-item label="法定代表人">
              {{ currentApplication.legalPerson }}
            </el-descriptions-item>
            <el-descriptions-item label="营业执照">
              <el-image 
                v-if="currentApplication.businessLicense" 
                :src="currentApplication.businessLicense" 
                style="width: 100px; height: 100px; cursor: pointer" 
                fit="cover"
                :preview-src-list="[currentApplication.businessLicense]"
                :preview-teleported="true"
              >
                <template #error>
                  <div class="image-error">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
              <span v-else class="text-muted">未上传</span>
            </el-descriptions-item>
          </template>
          
          <!-- 个人信息 -->
          <template v-if="currentApplication.idCard">
            <el-descriptions-item label="身份证号" :span="2">
              {{ currentApplication.idCard }}
            </el-descriptions-item>
            <el-descriptions-item label="身份证正面">
              <el-image 
                v-if="currentApplication.idCardFront" 
                :src="currentApplication.idCardFront" 
                style="width: 100px; height: 100px; cursor: pointer" 
                fit="cover"
                :preview-src-list="[currentApplication.idCardFront]"
                :preview-teleported="true"
              >
                <template #error>
                  <div class="image-error">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
              <span v-else class="text-muted">未上传</span>
            </el-descriptions-item>
            <el-descriptions-item label="身份证反面">
              <el-image 
                v-if="currentApplication.idCardBack" 
                :src="currentApplication.idCardBack" 
                style="width: 100px; height: 100px; cursor: pointer" 
                fit="cover"
                :preview-src-list="[currentApplication.idCardBack]"
                :preview-teleported="true"
              >
                <template #error>
                  <div class="image-error">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
              <span v-else class="text-muted">未上传</span>
            </el-descriptions-item>
          </template>
          
          <el-descriptions-item label="登录账号" :span="2">
            {{ currentApplication.username }}
          </el-descriptions-item>
          <el-descriptions-item label="申请时间" :span="2">
            {{ formatDateTime(currentApplication.createdTime) }}
          </el-descriptions-item>
        </el-descriptions>
        
        <el-divider />
        
        <el-form :model="approvalForm" label-width="100px" :rules="approvalRules" ref="approvalFormRef">
          <el-form-item label="审批结果" prop="approved" required>
            <el-radio-group v-model="approvalForm.approved">
              <el-radio :label="true">
                <el-icon color="#67c23a"><CircleCheck /></el-icon>
                通过审核
              </el-radio>
              <el-radio :label="false">
                <el-icon color="#f56c6c"><CircleClose /></el-icon>
                拒绝申请
              </el-radio>
            </el-radio-group>
          </el-form-item>
          
          <el-form-item 
            label="审批意见" 
            prop="reason"
          >
            <el-input
              v-model="approvalForm.reason"
              type="textarea"
              :rows="4"
              :placeholder="approvalForm.approved ? '选填：审批意见或建议' : '必填：请填写拒绝原因'"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
        </el-form>
      </el-scrollbar>
      
      <template #footer>
        <el-button @click="approvalDialogVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          :loading="approving" 
          @click="submitApproval"
          :icon="approvalForm.approved ? CircleCheck : CircleClose"
        >
          确认{{ approvalForm.approved ? '通过' : '拒绝' }}
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      :title="'申请详情 - ' + (currentApplication?.shopName || '')"
      width="900px"
      :close-on-click-modal="false"
    >
      <el-scrollbar max-height="700px">
        <el-tabs v-model="activeTab" v-if="currentApplication">
          <!-- 基本信息 -->
          <el-tab-pane label="基本信息" name="basic">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="申请ID">
                <el-tag>{{ currentApplication.id }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="申请时间">
                {{ formatDateTime(currentApplication.createdTime) }}
              </el-descriptions-item>
              <el-descriptions-item label="店铺名称" :span="2">
                <span class="highlight-text">{{ currentApplication.shopName }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="主体类型">
                {{ currentApplication.entityTypeText }}
              </el-descriptions-item>
              <el-descriptions-item label="店铺类型">
                {{ currentApplication.shopTypeText || '未填写' }}
              </el-descriptions-item>
              <el-descriptions-item label="联系人">
                {{ currentApplication.contactName }}
              </el-descriptions-item>
              <el-descriptions-item label="联系电话">
                {{ currentApplication.contactPhone }}
              </el-descriptions-item>
              <el-descriptions-item label="邮箱地址" :span="2">
                {{ currentApplication.email || '未填写' }}
              </el-descriptions-item>
              <el-descriptions-item label="登录账号" :span="2">
                {{ currentApplication.username }}
              </el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>
          
          <!-- 资质信息 -->
          <el-tab-pane label="资质信息" name="qualification">
            <!-- 企业资质 -->
            <div v-if="currentApplication.companyName" class="qualification-section">
              <h3>企业资质信息</h3>
              <el-descriptions :column="2" border>
                <el-descriptions-item label="企业名称" :span="2">
                  {{ currentApplication.companyName }}
                </el-descriptions-item>
                <el-descriptions-item label="统一社会信用代码" :span="2">
                  {{ currentApplication.creditCode }}
                </el-descriptions-item>
                <el-descriptions-item label="法定代表人">
                  {{ currentApplication.legalPerson }}
                </el-descriptions-item>
                <el-descriptions-item label="营业执照">
                  <el-button 
                    v-if="currentApplication.businessLicense" 
                    type="primary" 
                    size="small"
                    @click="previewImage(currentApplication.businessLicense)"
                  >
                    <el-icon><View /></el-icon>
                    查看营业执照
                  </el-button>
                  <span v-else class="text-muted">未上传</span>
                </el-descriptions-item>
              </el-descriptions>
              
              <div v-if="currentApplication.businessLicense" class="certificate-preview">
                <h4>营业执照预览</h4>
                <el-image 
                  :src="currentApplication.businessLicense" 
                  style="width: 100%; max-width: 600px" 
                  fit="contain"
                  :preview-src-list="[currentApplication.businessLicense]"
                  :preview-teleported="true"
                >
                  <template #error>
                    <div class="image-error-large">
                      <el-icon><Picture /></el-icon>
                      <p>图片加载失败</p>
                    </div>
                  </template>
                </el-image>
              </div>
            </div>
            
            <!-- 个人资质 -->
            <div v-if="currentApplication.idCard" class="qualification-section">
              <h3>个人身份信息</h3>
              <el-descriptions :column="2" border>
                <el-descriptions-item label="身份证号" :span="2">
                  {{ currentApplication.idCard }}
                </el-descriptions-item>
              </el-descriptions>
              
              <div class="certificate-preview">
                <h4>身份证照片</h4>
                <el-row :gutter="20">
                  <el-col :span="12">
                    <div class="id-card-item">
                      <p class="id-card-label">身份证正面</p>
                      <el-image 
                        v-if="currentApplication.idCardFront"
                        :src="currentApplication.idCardFront" 
                        style="width: 100%" 
                        fit="contain"
                        :preview-src-list="[currentApplication.idCardFront, currentApplication.idCardBack].filter(Boolean)"
                        :preview-teleported="true"
                      >
                        <template #error>
                          <div class="image-error-large">
                            <el-icon><Picture /></el-icon>
                            <p>图片加载失败</p>
                          </div>
                        </template>
                      </el-image>
                      <div v-else class="image-placeholder">未上传</div>
                    </div>
                  </el-col>
                  <el-col :span="12">
                    <div class="id-card-item">
                      <p class="id-card-label">身份证反面</p>
                      <el-image 
                        v-if="currentApplication.idCardBack"
                        :src="currentApplication.idCardBack" 
                        style="width: 100%" 
                        fit="contain"
                        :preview-src-list="[currentApplication.idCardFront, currentApplication.idCardBack].filter(Boolean)"
                        :initial-index="1"
                        :preview-teleported="true"
                      >
                        <template #error>
                          <div class="image-error-large">
                            <el-icon><Picture /></el-icon>
                            <p>图片加载失败</p>
                          </div>
                        </template>
                      </el-image>
                      <div v-else class="image-placeholder">未上传</div>
                    </div>
                  </el-col>
                </el-row>
              </div>
            </div>
          </el-tab-pane>
          
          <!-- 审批信息 -->
          <el-tab-pane label="审批信息" name="approval">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="审批状态" :span="2">
                <el-tag v-if="currentApplication.approvalStatus === 0" type="warning" size="large">待审核</el-tag>
                <el-tag v-else-if="currentApplication.approvalStatus === 1" type="success" size="large">已通过</el-tag>
                <el-tag v-else-if="currentApplication.approvalStatus === 2" type="danger" size="large">已拒绝</el-tag>
              </el-descriptions-item>
              
              <template v-if="currentApplication.approvalTime">
                <el-descriptions-item label="审批时间" :span="2">
                  {{ formatDateTime(currentApplication.approvalTime) }}
                </el-descriptions-item>
                <el-descriptions-item label="审批人">
                  {{ currentApplication.approvalByName || '未知' }}
                </el-descriptions-item>
                <el-descriptions-item label="审批人ID">
                  {{ currentApplication.approvalBy }}
                </el-descriptions-item>
                <el-descriptions-item label="审批意见" :span="2">
                  <div class="approval-reason">
                    {{ currentApplication.approvalReason || '无' }}
                  </div>
                </el-descriptions-item>
                <el-descriptions-item v-if="currentApplication.merchantId" label="关联商家ID" :span="2">
                  <el-tag type="success">{{ currentApplication.merchantId }}</el-tag>
                </el-descriptions-item>
              </template>
              <template v-else>
                <el-descriptions-item label="审批状态" :span="2">
                  <el-alert type="info" :closable="false">
                    该申请尚未审核
                  </el-alert>
                </el-descriptions-item>
              </template>
            </el-descriptions>
            
            <div v-if="currentApplication.approvalStatus === 0" style="margin-top: 20px; text-align: center">
              <el-button type="primary" size="large" @click="openApprovalDialog(currentApplication)">
                <el-icon><Edit /></el-icon>
                立即审批
              </el-button>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-scrollbar>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Clock, 
  CircleCheck, 
  CircleClose, 
  DataAnalysis, 
  View, 
  Search, 
  Refresh,
  Picture,
  Edit
} from '@element-plus/icons-vue'
import { getApplicationList, getApplicationStats, approveApplication } from '@/api/merchant'

// 数据
const loading = ref(false)
const applications = ref([])
const stats = reactive({
  pending: 0,
  approved: 0,
  rejected: 0,
  total: 0
})

// 筛选表单
const filterForm = reactive({
  status: null,
  keyword: ''
})

// 分页
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 对话框
const approvalDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentApplication = ref(null)
const approving = ref(false)
const activeTab = ref('basic')

// 审批表单
const approvalForm = reactive({
  approved: true,
  reason: ''
})

const approvalFormRef = ref(null)

// 表单验证规则
const approvalRules = computed(() => ({
  reason: [
    { 
      required: approvalForm.approved === false, 
      message: '拒绝申请时必须填写原因', 
      trigger: 'blur' 
    },
    { 
      min: approvalForm.approved === false ? 5 : 0, 
      message: '拒绝原因至少5个字符', 
      trigger: 'blur' 
    }
  ]
}))

/**
 * 加载申请列表
 */
const loadApplications = async () => {
  loading.value = true
  
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      status: filterForm.status,
      keyword: filterForm.keyword
    }
    
    const response = await getApplicationList(params)
    
    if (response.success || response.code === 200) {
      const data = response.data
      applications.value = data.records || []
      pagination.total = data.total || 0
      
      console.log('加载申请列表成功:', applications.value.length, '条')
    } else {
      ElMessage.error(response.message || '加载失败')
    }
  } catch (error) {
    console.error('加载申请列表失败:', error)
    ElMessage.error('加载失败: ' + (error.message || '网络错误'))
  } finally {
    loading.value = false
  }
}

/**
 * 加载统计数据
 */
const loadStats = async () => {
  try {
    const response = await getApplicationStats()
    
    if (response.success || response.code === 200) {
      Object.assign(stats, response.data)
      console.log('加载统计成功:', stats)
    }
  } catch (error) {
    console.error('加载统计失败:', error)
  }
}

/**
 * 按状态筛选
 */
const filterByStatus = (status) => {
  filterForm.status = status
  pagination.page = 1
  loadApplications()
}

/**
 * 查看详情
 */
const viewDetail = (row) => {
  currentApplication.value = row
  activeTab.value = 'basic'
  detailDialogVisible.value = true
}

/**
 * 打开审批对话框
 */
const openApprovalDialog = (row) => {
  currentApplication.value = row
  approvalForm.approved = true
  approvalForm.reason = ''
  approvalDialogVisible.value = true
  detailDialogVisible.value = false
}

/**
 * 提交审批
 */
const submitApproval = async () => {
  // 验证表单
  if (approvalForm.approved === false && !approvalForm.reason.trim()) {
    ElMessage.warning('拒绝申请时必须填写原因')
    return
  }
  
  if (approvalForm.approved === false && approvalForm.reason.trim().length < 5) {
    ElMessage.warning('拒绝原因至少需要5个字符')
    return
  }
  
  // 二次确认
  try {
    await ElMessageBox.confirm(
      approvalForm.approved 
        ? `确认通过 "${currentApplication.value.shopName}" 的入驻申请吗？通过后将自动创建商家账号。`
        : `确认拒绝 "${currentApplication.value.shopName}" 的入驻申请吗？`,
      '确认审批',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: approvalForm.approved ? 'success' : 'warning',
      }
    )
  } catch {
    return // 用户取消
  }
  
  approving.value = true
  
  try {
    const response = await approveApplication(currentApplication.value.id, approvalForm)
    
    if (response.success || response.code === 200) {
      ElMessage.success({
        message: approvalForm.approved ? '审批通过成功！商家账号已创建' : '已拒绝该申请',
        duration: 3000
      })
      approvalDialogVisible.value = false
      
      // 刷新列表和统计
      await Promise.all([loadApplications(), loadStats()])
    } else {
      ElMessage.error(response.message || '审批失败')
    }
  } catch (error) {
    console.error('审批失败:', error)
    ElMessage.error('审批操作失败: ' + (error.message || '网络错误'))
  } finally {
    approving.value = false
  }
}

/**
 * 预览图片
 */
const previewImage = (url) => {
  // Element Plus 的 el-image 组件会自动处理预览
  console.log('预览图片:', url)
}

/**
 * 格式化日期时间
 */
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 初始化
onMounted(() => {
  loadApplications()
  loadStats()
})
</script>

<style scoped>
.applications-page {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  font-size: 20px;
  color: #303133;
  font-weight: 600;
}

.subtitle {
  margin: 5px 0 0 0;
  font-size: 14px;
  color: #909399;
}

.filter-form {
  margin-bottom: 20px;
}

.stats-cards {
  margin-bottom: 20px;
}

.stat-card {
  padding: 24px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 16px;
  color: white;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.stat-card.pending {
  background: linear-gradient(135deg, #ffa726 0%, #fb8c00 100%);
}

.stat-card.approved {
  background: linear-gradient(135deg, #66bb6a 0%, #43a047 100%);
}

.stat-card.rejected {
  background: linear-gradient(135deg, #ef5350 0%, #e53935 100%);
}

.stat-card.total {
  background: linear-gradient(135deg, #42a5f5 0%, #1e88e5 100%);
}

.stat-icon {
  font-size: 48px;
  opacity: 0.9;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 36px;
  font-weight: bold;
  margin-bottom: 4px;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
}

.highlight-text {
  font-weight: 600;
  color: #409eff;
  font-size: 16px;
}

.text-muted {
  color: #909399;
  font-style: italic;
}

.qualification-section {
  margin-bottom: 30px;
}

.qualification-section h3 {
  margin: 0 0 16px 0;
  font-size: 16px;
  color: #303133;
  font-weight: 600;
  padding-bottom: 8px;
  border-bottom: 2px solid #409eff;
}

.qualification-section h4 {
  margin: 20px 0 12px 0;
  font-size: 14px;
  color: #606266;
}

.certificate-preview {
  margin-top: 20px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.id-card-item {
  text-align: center;
}

.id-card-label {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.image-error {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  color: #c0c4cc;
  font-size: 24px;
}

.image-error-large {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 200px;
  background: #f5f7fa;
  color: #c0c4cc;
  font-size: 48px;
}

.image-error-large p {
  margin-top: 12px;
  font-size: 14px;
}

.image-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 200px;
  background: #f5f7fa;
  color: #c0c4cc;
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  font-size: 14px;
}

.approval-reason {
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

/* 响应式布局 */
@media (max-width: 768px) {
  .applications-page {
    padding: 12px;
  }
  
  .stat-card {
    padding: 16px;
  }
  
  .stat-value {
    font-size: 28px;
  }
  
  .stat-icon {
    font-size: 36px;
  }
}
</style>
