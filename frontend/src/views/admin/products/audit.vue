<template>
  <div class="product-audit-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>商品审核管理</h2>
          <p class="subtitle">审核商家发布的商品信息</p>
        </div>
      </template>
      
      <!-- 筛选器 -->
      <el-form :inline="true" class="filter-form">
        <el-form-item label="审核状态">
          <el-select v-model="filterForm.status" placeholder="全部状态" clearable @change="loadProducts">
            <el-option label="待审核" value="pending" />
            <el-option label="已通过" value="approved" />
            <el-option label="已拒绝" value="rejected" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="商品分类">
          <el-select v-model="filterForm.category" placeholder="全部分类" clearable @change="loadProducts">
            <el-option label="数码电子" value="electronics" />
            <el-option label="服装鞋帽" value="clothing" />
            <el-option label="家居用品" value="home" />
            <el-option label="美妆护肤" value="beauty" />
            <el-option label="食品饮料" value="food" />
            <el-option label="运动户外" value="sports" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="搜索">
          <el-input
            v-model="filterForm.keyword"
            placeholder="商品名称/商家名称"
            clearable
            @clear="loadProducts"
            @keyup.enter="loadProducts"
            style="width: 250px"
          >
            <template #append>
              <el-button icon="Search" @click="loadProducts">搜索</el-button>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="loadProducts" icon="Refresh">刷新</el-button>
        </el-form-item>
      </el-form>
      
      <!-- 统计卡片 -->
      <div class="stats-cards">
        <el-row :gutter="20">
          <el-col :span="6">
            <div class="stat-card pending">
              <div class="stat-value">{{ stats.pending }}</div>
              <div class="stat-label">待审核</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card approved">
              <div class="stat-value">{{ stats.approved }}</div>
              <div class="stat-label">已通过</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card rejected">
              <div class="stat-value">{{ stats.rejected }}</div>
              <div class="stat-label">已拒绝</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card total">
              <div class="stat-value">{{ stats.total }}</div>
              <div class="stat-label">总商品数</div>
            </div>
          </el-col>
        </el-row>
      </div>
      
      <!-- 商品列表 -->
      <el-table
        :data="products"
        :loading="loading"
        stripe
        border
        style="margin-top: 20px"
      >
        <el-table-column prop="id" label="商品ID" width="80" align="center" />
        <el-table-column label="商品图片" width="100" align="center">
          <template #default="{ row }">
            <el-image
              :src="row.imageUrl || '/placeholder.png'"
              style="width: 60px; height: 60px"
              fit="cover"
            />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="merchantName" label="商家名称" width="150" show-overflow-tooltip />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="price" label="价格" width="100" align="right">
          <template #default="{ row }">
            ¥{{ row.price }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'pending'" type="warning">待审核</el-tag>
            <el-tag v-else-if="row.status === 'approved'" type="success">已通过</el-tag>
            <el-tag v-else-if="row.status === 'rejected'" type="danger">已拒绝</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="viewDetail(row)">查看</el-button>
            <el-button
              v-if="row.status === 'pending'"
              size="small"
              type="success"
              link
              @click="approveProduct(row)"
            >
              审核
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
        @size-change="loadProducts"
        @current-change="loadProducts"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'

// 数据
const loading = ref(false)
const products = ref([])
const stats = reactive({
  pending: 0,
  approved: 0,
  rejected: 0,
  total: 0
})

// 筛选表单
const filterForm = reactive({
  status: '',
  category: '',
  keyword: ''
})

// 分页
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

/**
 * 加载商品列表
 */
const loadProducts = async () => {
  loading.value = true
  
  try {
    // TODO: 接入真实API
    // const response = await getProductAuditList(params)
    
    // 模拟数据
    await new Promise(resolve => setTimeout(resolve, 500))
    products.value = []
    pagination.total = 0
    
    ElMessage.info('商品审核功能开发中，敬请期待')
  } catch (error) {
    console.error('加载商品列表失败:', error)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

/**
 * 查看详情
 */
const viewDetail = (row) => {
  ElMessage.info('查看商品详情功能开发中')
}

/**
 * 审核商品
 */
const approveProduct = (row) => {
  ElMessage.info('商品审核功能开发中')
}

// 初始化
onMounted(() => {
  loadProducts()
})
</script>

<style scoped>
.product-audit-page {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
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
  padding: 20px;
  border-radius: 8px;
  text-align: center;
  color: white;
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

.stat-value {
  font-size: 32px;
  font-weight: bold;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
}
</style>
