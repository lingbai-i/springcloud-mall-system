<template>
  <div class="user-detail">
    <el-tabs v-model="activeTab" type="border-card">
      <!-- 基本信息 -->
      <el-tab-pane label="基本信息" name="basic">
        <div class="detail-section">
          <div class="user-avatar-section">
            <el-avatar :size="80" :src="user.avatar">
              {{ user.username.charAt(0).toUpperCase() }}
            </el-avatar>
            <div class="user-basic-info">
              <h3>{{ user.username }}</h3>
              <p class="user-id">ID: {{ user.id }}</p>
              <el-tag :type="getUserStatusType(user.status)" size="small">
                {{ getUserStatusText(user.status) }}
              </el-tag>
            </div>
          </div>

          <el-descriptions :column="2" border>
            <el-descriptions-item label="用户名">
              {{ user.username }}
            </el-descriptions-item>
            <el-descriptions-item label="邮箱">
              {{ user.email }}
            </el-descriptions-item>
            <el-descriptions-item label="手机号">
              {{ user.phone || '未设置' }}
            </el-descriptions-item>
            <el-descriptions-item label="性别">
              <el-tag v-if="user.gender === 'male'" type="primary" size="small">男</el-tag>
              <el-tag v-else-if="user.gender === 'female'" type="danger" size="small">女</el-tag>
              <el-tag v-else type="info" size="small">未知</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="注册时间">
              {{ formatDateTime(user.registerTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="最后登录">
              {{ user.lastLoginTime ? formatDateTime(user.lastLoginTime) : '从未登录' }}
            </el-descriptions-item>
            <el-descriptions-item label="账户状态">
              <el-tag :type="getUserStatusType(user.status)">
                {{ getUserStatusText(user.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="邮箱验证">
              <el-tag :type="user.emailVerified ? 'success' : 'warning'" size="small">
                {{ user.emailVerified ? '已验证' : '未验证' }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </el-tab-pane>

      <!-- 个人资料 -->
      <el-tab-pane label="个人资料" name="profile">
        <div class="detail-section">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="真实姓名">
              {{ userProfile.realName || '未设置' }}
            </el-descriptions-item>
            <el-descriptions-item label="身份证号">
              {{ userProfile.idCard || '未设置' }}
            </el-descriptions-item>
            <el-descriptions-item label="生日">
              {{ userProfile.birthday || '未设置' }}
            </el-descriptions-item>
            <el-descriptions-item label="职业">
              {{ userProfile.occupation || '未设置' }}
            </el-descriptions-item>
            <el-descriptions-item label="公司">
              {{ userProfile.company || '未设置' }}
            </el-descriptions-item>
            <el-descriptions-item label="学历">
              {{ userProfile.education || '未设置' }}
            </el-descriptions-item>
            <el-descriptions-item label="个人简介" :span="2">
              {{ userProfile.bio || '未设置' }}
            </el-descriptions-item>
          </el-descriptions>

          <h4 style="margin: 24px 0 16px 0;">收货地址</h4>
          <el-table :data="userAddresses" style="width: 100%">
            <el-table-column prop="name" label="收货人" width="100" />
            <el-table-column prop="phone" label="联系电话" width="120" />
            <el-table-column prop="address" label="详细地址" min-width="200" />
            <el-table-column label="默认地址" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.isDefault" type="success" size="small">默认</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>

      <!-- 订单记录 -->
      <el-tab-pane label="订单记录" name="orders">
        <div class="detail-section">
          <div class="order-stats">
            <el-row :gutter="16">
              <el-col :span="6" v-for="stat in orderStats" :key="stat.key">
                <el-card class="stat-card" shadow="hover">
                  <div class="stat-content">
                    <div class="stat-value">{{ stat.value }}</div>
                    <div class="stat-label">{{ stat.label }}</div>
                  </div>
                </el-card>
              </el-col>
            </el-row>
          </div>

          <el-table :data="userOrders" style="width: 100%; margin-top: 16px;">
            <el-table-column prop="orderNo" label="订单号" width="150" />
            <el-table-column prop="amount" label="订单金额" width="100">
              <template #default="{ row }">
                <span class="amount">¥{{ row.amount }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="订单状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getOrderStatusType(row.status)" size="small">
                  {{ getOrderStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="下单时间" width="160">
              <template #default="{ row }">
                {{ formatDateTime(row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button type="primary" link @click="viewOrderDetail(row)">
                  查看详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>

      <!-- 操作日志 -->
      <el-tab-pane label="操作日志" name="logs">
        <div class="detail-section">
          <el-timeline>
            <el-timeline-item
              v-for="log in operationLogs"
              :key="log.id"
              :timestamp="formatDateTime(log.createTime)"
              :type="getLogType(log.type)"
            >
              <div class="log-content">
                <div class="log-title">{{ log.title }}</div>
                <div class="log-description">{{ log.description }}</div>
                <div class="log-meta">
                  <span>IP: {{ log.ip }}</span>
                  <span>设备: {{ log.device }}</span>
                </div>
              </div>
            </el-timeline-item>
          </el-timeline>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 操作按钮 -->
    <div class="action-buttons">
      <el-button type="primary" @click="handleEdit">
        <el-icon><Edit /></el-icon>
        编辑用户
      </el-button>
      <el-button 
        :type="user.status === 'active' ? 'warning' : 'success'" 
        @click="handleToggleStatus"
      >
        <el-icon><Switch /></el-icon>
        {{ user.status === 'active' ? '禁用用户' : '启用用户' }}
      </el-button>
      <el-button type="info" @click="handleResetPassword">
        <el-icon><Key /></el-icon>
        重置密码
      </el-button>
      <el-button type="danger" @click="handleDelete">
        <el-icon><Delete /></el-icon>
        删除用户
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Edit, Switch, Key, Delete } from '@element-plus/icons-vue'

const router = useRouter()

// Props
const props = defineProps({
  user: {
    type: Object,
    required: true
  }
})

// Emits
const emit = defineEmits(['refresh'])

// 当前激活的标签页
const activeTab = ref('basic')

// 用户详细数据将从API获取
const userProfile = reactive({})
const userAddresses = reactive([])
const orderStats = reactive([])
const userOrders = reactive([])
const operationLogs = reactive([
  {
    id: 1,
    type: 'login',
    title: '用户登录',
    description: '用户成功登录系统',
    ip: '192.168.1.100',
    device: 'Chrome 120.0 / Windows 10',
    createTime: '2024-01-15 10:30:00'
  },
  {
    id: 2,
    type: 'order',
    title: '创建订单',
    description: '用户创建了新订单 ORD20240115001',
    ip: '192.168.1.100',
    device: 'Chrome 120.0 / Windows 10',
    createTime: '2024-01-15 10:25:00'
  },
  {
    id: 3,
    type: 'profile',
    title: '更新资料',
    description: '用户更新了个人资料信息',
    ip: '192.168.1.100',
    device: 'Chrome 120.0 / Windows 10',
    createTime: '2024-01-14 16:20:00'
  }
])

// 获取用户状态类型
const getUserStatusType = (status) => {
  const statusMap = {
    active: 'success',
    disabled: 'warning',
    pending: 'danger'
  }
  return statusMap[status] || 'info'
}

// 获取用户状态文本
const getUserStatusText = (status) => {
  const statusMap = {
    active: '正常',
    disabled: '禁用',
    pending: '待验证'
  }
  return statusMap[status] || '未知'
}

// 获取订单状态类型
const getOrderStatusType = (status) => {
  const statusMap = {
    pending: 'warning',
    paid: 'primary',
    shipped: 'info',
    completed: 'success',
    cancelled: 'danger'
  }
  return statusMap[status] || 'info'
}

// 获取订单状态文本
const getOrderStatusText = (status) => {
  const statusMap = {
    pending: '待付款',
    paid: '已付款',
    shipped: '已发货',
    completed: '已完成',
    cancelled: '已取消'
  }
  return statusMap[status] || '未知'
}

// 获取日志类型
const getLogType = (type) => {
  const typeMap = {
    login: 'success',
    order: 'primary',
    profile: 'info',
    error: 'danger'
  }
  return typeMap[type] || 'info'
}

// 格式化日期时间
const formatDateTime = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 查看订单详情
const viewOrderDetail = (order) => {
  router.push(`/admin/orders/${order.id}`)
}

// 编辑用户
const handleEdit = () => {
  ElMessage.info('编辑功能将在父组件中处理')
}

// 切换用户状态
const handleToggleStatus = async () => {
  const action = props.user.status === 'active' ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(
      `确定要${action}用户 "${props.user.username}" 吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500))
    
    ElMessage.success(`${action}成功`)
    emit('refresh')
    
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`${action}失败`)
    }
  }
}

// 重置密码
const handleResetPassword = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要重置用户 "${props.user.username}" 的密码吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500))
    
    ElMessage.success('密码重置成功，新密码已发送到用户邮箱')
    
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('密码重置失败')
    }
  }
}

// 删除用户
const handleDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 "${props.user.username}" 吗？此操作不可恢复！`,
      '危险操作',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'error'
      }
    )
    
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500))
    
    ElMessage.success('删除成功')
    emit('refresh')
    
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}
</script>

<style scoped>
.user-detail {
  padding: 0;
}

/* 用户头像区域 */
.user-avatar-section {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  border-radius: 8px;
}

.user-basic-info h3 {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: 600;
  color: #333;
}

.user-id {
  margin: 0 0 8px 0;
  color: #666;
  font-size: 14px;
}

/* 详情区域 */
.detail-section {
  padding: 0;
}

/* 订单统计 */
.order-stats {
  margin-bottom: 16px;
}

.stat-card {
  text-align: center;
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.stat-content {
  padding: 8px 0;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #1890ff;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

/* 金额样式 */
.amount {
  font-weight: 600;
  color: #f5222d;
}

/* 日志内容 */
.log-content {
  padding-left: 8px;
}

.log-title {
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.log-description {
  color: #666;
  font-size: 14px;
  margin-bottom: 8px;
}

.log-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #999;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #ebeef5;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .user-avatar-section {
    flex-direction: column;
    text-align: center;
  }
  
  .action-buttons {
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .order-stats .el-col {
    margin-bottom: 12px;
  }
}
</style>