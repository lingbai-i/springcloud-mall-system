<template>
  <div class="admin-users">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h2 class="page-title">用户管理</h2>
        <p class="page-description">管理平台所有用户信息，包括用户状态、权限等</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="handleExport">
          <el-icon><Download /></el-icon>
          导出用户
        </el-button>
        <el-button type="success" @click="handleAddUser">
          <el-icon><Plus /></el-icon>
          添加用户
        </el-button>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="用户名">
          <el-input
            v-model="searchForm.username"
            placeholder="请输入用户名"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input
            v-model="searchForm.email"
            placeholder="请输入邮箱"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input
            v-model="searchForm.phone"
            placeholder="请输入手机号"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择状态"
            clearable
            style="width: 120px"
          >
            <el-option label="正常" value="active" />
            <el-option label="禁用" value="disabled" />
            <el-option label="待验证" value="pending" />
          </el-select>
        </el-form-item>
        <el-form-item label="注册时间">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 240px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 用户统计 -->
    <div class="stats-section">
      <el-row :gutter="24">
        <el-col :span="6" v-for="stat in userStats" :key="stat.key">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon" :style="{ backgroundColor: stat.color }">
                <component :is="stat.icon" />
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stat.value }}</div>
                <div class="stat-label">{{ stat.label }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 用户列表 -->
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>用户列表</span>
          <div class="header-tools">
            <el-tooltip content="刷新数据">
              <el-button circle @click="handleRefresh">
                <el-icon><Refresh /></el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip content="列设置">
              <el-button circle @click="handleColumnSetting">
                <el-icon><Setting /></el-icon>
              </el-button>
            </el-tooltip>
          </div>
        </div>
      </template>

      <el-table
        :data="userList"
        v-loading="loading"
        stripe
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="头像" width="80">
          <template #default="{ row }">
            <el-avatar :src="row.avatar" :alt="row.username">
              {{ row.username.charAt(0).toUpperCase() }}
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column label="性别" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.gender === 'male'" type="primary" size="small">男</el-tag>
            <el-tag v-else-if="row.gender === 'female'" type="danger" size="small">女</el-tag>
            <el-tag v-else type="info" size="small">未知</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getUserStatusType(row.status)" size="small">
              {{ getUserStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="最后登录" width="160">
          <template #default="{ row }">
            <div v-if="row.lastLoginTime">
              <div>{{ formatDate(row.lastLoginTime) }}</div>
              <div class="text-gray-500 text-xs">{{ formatTime(row.lastLoginTime) }}</div>
            </div>
            <span v-else class="text-gray-400">从未登录</span>
          </template>
        </el-table-column>
        <el-table-column prop="registerTime" label="注册时间" width="160">
          <template #default="{ row }">
            <div>
              <div>{{ formatDate(row.registerTime) }}</div>
              <div class="text-gray-500 text-xs">{{ formatTime(row.registerTime) }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleViewDetail(row)">
              <el-icon><View /></el-icon>
              详情
            </el-button>
            <el-button type="warning" link @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-dropdown @command="(command) => handleMoreAction(command, row)">
              <el-button type="info" link>
                更多
                <el-icon><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item 
                    :command="{ action: 'toggleStatus', row }"
                    :disabled="row.status === 'pending'"
                  >
                    {{ row.status === 'active' ? '禁用用户' : '启用用户' }}
                  </el-dropdown-item>
                  <el-dropdown-item :command="{ action: 'resetPassword', row }">
                    重置密码
                  </el-dropdown-item>
                  <el-dropdown-item :command="{ action: 'viewOrders', row }">
                    查看订单
                  </el-dropdown-item>
                  <el-dropdown-item 
                    :command="{ action: 'delete', row }"
                    divided
                    style="color: #f56c6c"
                  >
                    删除用户
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <!-- 批量操作 -->
      <div v-if="selectedUsers.length > 0" class="batch-actions">
        <span class="batch-info">已选择 {{ selectedUsers.length }} 个用户</span>
        <el-button type="warning" @click="handleBatchDisable">批量禁用</el-button>
        <el-button type="success" @click="handleBatchEnable">批量启用</el-button>
        <el-button type="danger" @click="handleBatchDelete">批量删除</el-button>
      </div>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 用户详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="用户详情"
      width="800px"
      :before-close="handleCloseDetail"
    >
      <UserDetail
        v-if="detailDialogVisible && currentUser"
        :user="currentUser"
        @refresh="handleRefresh"
      />
    </el-dialog>

    <!-- 编辑用户对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑用户"
      width="600px"
      :before-close="handleCloseEdit"
    >
      <UserEdit
        v-if="editDialogVisible && currentUser"
        :user="currentUser"
        @success="handleEditSuccess"
        @cancel="handleCloseEdit"
      />
    </el-dialog>

    <!-- 添加用户对话框 -->
    <el-dialog
      v-model="addDialogVisible"
      title="添加用户"
      width="600px"
      :before-close="handleCloseAdd"
    >
      <UserAdd
        v-if="addDialogVisible"
        @success="handleAddSuccess"
        @cancel="handleCloseAdd"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Refresh,
  Plus,
  Download,
  View,
  Edit,
  Setting,
  ArrowDown,
  User,
  UserFilled,
  Warning,
  CircleCheck
} from '@element-plus/icons-vue'
import { getUserList, getUserStats } from '@/api/admin'
import UserDetail from './components/UserDetail.vue'
import UserEdit from './components/UserEdit.vue'
import UserAdd from './components/UserAdd.vue'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const userList = ref([])
const selectedUsers = ref([])
const currentUser = ref(null)

// 对话框状态
const detailDialogVisible = ref(false)
const editDialogVisible = ref(false)
const addDialogVisible = ref(false)

// 搜索表单
const searchForm = reactive({
  username: '',
  email: '',
  phone: '',
  status: '',
  dateRange: []
})

// 分页信息
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 用户统计数据 - 虚拟数据已移除，待接入真实API
const userStats = reactive([
  {
    key: 'total',
    label: '总用户数',
    value: '0',
    color: '#1890ff',
    icon: UserFilled
  },
  {
    key: 'active',
    label: '活跃用户',
    value: '0',
    color: '#52c41a',
    icon: CircleCheck
  },
  {
    key: 'disabled',
    label: '禁用用户',
    value: '0',
    color: '#faad14',
    icon: Warning
  },
  {
    key: 'pending',
    label: '待验证',
    value: '0',
    color: '#f5222d',
    icon: User
  }
])

// 用户数据将从API获取

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

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

// 格式化时间
const formatTime = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleTimeString('zh-CN', { 
    hour12: false,
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 加载用户列表
const loadUserList = async () => {
  loading.value = true
  try {
    // 调用真实的API
    const queryParams = {
      page: pagination.page,
      size: pagination.size,
      username: searchForm.username,
      email: searchForm.email,
      phone: searchForm.phone,
      status: searchForm.status
    }
    
    const response = await getUserList(queryParams)
    userList.value = response.data.list || []
    pagination.total = response.data.total || 0
    
  } catch (error) {
    ElMessage.error('加载用户列表失败')
    console.error('Load user list error:', error)
  } finally {
    loading.value = false
  }
}

// 加载用户统计数据
const loadUserStats = async () => {
  try {
    const response = await getUserStats()
    if (response.data) {
      userStats[0].value = response.data.total || 0
      userStats[1].value = response.data.active || 0
      userStats[2].value = response.data.disabled || 0
      userStats[3].value = response.data.pending || 0
    }
  } catch (error) {
    console.error('Load user stats error:', error)
  }
}

// 搜索处理
const handleSearch = () => {
  pagination.page = 1
  loadUserList()
}

// 重置搜索
const handleReset = () => {
  Object.assign(searchForm, {
    username: '',
    email: '',
    phone: '',
    status: '',
    dateRange: []
  })
  pagination.page = 1
  loadUserList()
}

// 刷新数据
const handleRefresh = () => {
  loadUserList()
}

// 分页处理
const handlePageChange = (page) => {
  pagination.page = page
  loadUserList()
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  loadUserList()
}

// 选择处理
const handleSelectionChange = (selection) => {
  selectedUsers.value = selection
}

// 查看详情
const handleViewDetail = (user) => {
  currentUser.value = user
  detailDialogVisible.value = true
}

// 编辑用户
const handleEdit = (user) => {
  currentUser.value = user
  editDialogVisible.value = true
}

// 添加用户
const handleAddUser = () => {
  addDialogVisible.value = true
}

// 更多操作
const handleMoreAction = async ({ action, row }) => {
  switch (action) {
    case 'toggleStatus':
      await handleToggleStatus(row)
      break
    case 'resetPassword':
      await handleResetPassword(row)
      break
    case 'viewOrders':
      handleViewOrders(row)
      break
    case 'delete':
      await handleDeleteUser(row)
      break
  }
}

// 切换用户状态
const handleToggleStatus = async (user) => {
  const action = user.status === 'active' ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(
      `确定要${action}用户 "${user.username}" 吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500))
    
    user.status = user.status === 'active' ? 'disabled' : 'active'
    ElMessage.success(`${action}成功`)
    
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`${action}失败`)
    }
  }
}

// 重置密码
const handleResetPassword = async (user) => {
  try {
    await ElMessageBox.confirm(
      `确定要重置用户 "${user.username}" 的密码吗？`,
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

// 查看用户订单
const handleViewOrders = (user) => {
  router.push(`/admin/orders?userId=${user.id}`)
}

// 删除用户
const handleDeleteUser = async (user) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 "${user.username}" 吗？此操作不可恢复！`,
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
    loadUserList()
    
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 批量操作
const handleBatchDisable = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要禁用选中的 ${selectedUsers.value.length} 个用户吗？`,
      '批量操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500))
    
    ElMessage.success('批量禁用成功')
    loadUserList()
    
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量禁用失败')
    }
  }
}

const handleBatchEnable = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要启用选中的 ${selectedUsers.value.length} 个用户吗？`,
      '批量操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500))
    
    ElMessage.success('批量启用成功')
    loadUserList()
    
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量启用失败')
    }
  }
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedUsers.value.length} 个用户吗？此操作不可恢复！`,
      '危险操作',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'error'
      }
    )
    
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500))
    
    ElMessage.success('批量删除成功')
    loadUserList()
    
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}

// 导出用户
const handleExport = async () => {
  try {
    ElMessage.info('正在导出用户数据...')
    
    // 模拟导出
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    ElMessage.success('导出成功')
    
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

// 列设置
const handleColumnSetting = () => {
  ElMessage.info('列设置功能开发中...')
}

// 对话框关闭处理
const handleCloseDetail = () => {
  detailDialogVisible.value = false
  currentUser.value = null
}

const handleCloseEdit = () => {
  editDialogVisible.value = false
  currentUser.value = null
}

const handleCloseAdd = () => {
  addDialogVisible.value = false
}

// 编辑成功处理
const handleEditSuccess = () => {
  handleCloseEdit()
  loadUserList()
  ElMessage.success('编辑成功')
}

// 添加成功处理
const handleAddSuccess = () => {
  handleCloseAdd()
  loadUserList()
  ElMessage.success('添加成功')
}

// 组件挂载时加载数据
onMounted(() => {
  loadUserList()
  loadUserStats()
})
</script>

<style scoped>
.admin-users {
  padding: 0;
}

/* 页面头部 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  padding: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 8px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 8px 0;
}

.page-description {
  font-size: 14px;
  opacity: 0.9;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
}

/* 搜索卡片 */
.search-card {
  margin-bottom: 24px;
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 统计区域 */
.stats-section {
  margin-bottom: 24px;
}

.stat-card {
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 20px;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  line-height: 1;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

/* 表格卡片 */
.table-card {
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-tools {
  display: flex;
  gap: 8px;
}

/* 批量操作 */
.batch-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 0;
  border-top: 1px solid #ebeef5;
  margin-top: 16px;
}

.batch-info {
  color: #1890ff;
  font-weight: 500;
}

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

/* 工具类 */
.text-gray-500 {
  color: #6b7280;
}

.text-gray-400 {
  color: #9ca3af;
}

.text-xs {
  font-size: 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }
  
  .header-actions {
    justify-content: center;
  }
  
  .stats-section .el-col {
    margin-bottom: 16px;
  }
}
</style>