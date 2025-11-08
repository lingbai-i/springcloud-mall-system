<template>
  <div class="merchant-products">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">商品管理</h2>
        <p class="page-desc">管理您的商品信息，包括上架、下架、编辑等操作</p>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="addProduct">
          <el-icon><Plus /></el-icon>
          添加商品
        </el-button>
        <el-button @click="handleBatchImport">
          <el-icon><Upload /></el-icon>
          批量导入
        </el-button>
        <el-button @click="handleExport">
          <el-icon><Download /></el-icon>
          导出商品
        </el-button>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" :inline="true" class="search-form">
        <el-form-item label="商品名称">
          <el-input
            v-model="searchForm.keyword"
            placeholder="请输入商品名称或关键词"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        
        <el-form-item label="商品分类">
          <el-select v-model="searchForm.category" placeholder="请选择分类" clearable style="width: 150px">
            <el-option label="全部分类" value=""></el-option>
            <el-option label="数码电子" value="electronics"></el-option>
            <el-option label="服装鞋帽" value="clothing"></el-option>
            <el-option label="家居用品" value="home"></el-option>
            <el-option label="美妆护肤" value="beauty"></el-option>
            <el-option label="食品饮料" value="food"></el-option>
            <el-option label="运动户外" value="sports"></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="商品状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 120px">
            <el-option label="全部状态" value=""></el-option>
            <el-option label="在售" value="on_sale"></el-option>
            <el-option label="下架" value="off_sale"></el-option>
            <el-option label="售罄" value="sold_out"></el-option>
            <el-option label="待审核" value="pending"></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="价格区间">
          <el-input
            v-model="searchForm.minPrice"
            placeholder="最低价"
            type="number"
            style="width: 100px"
          />
          <span style="margin: 0 8px">-</span>
          <el-input
            v-model="searchForm.maxPrice"
            placeholder="最高价"
            type="number"
            style="width: 100px"
          />
        </el-form-item>
        
        <el-form-item label="库存预警">
          <el-checkbox v-model="searchForm.lowStock">仅显示库存不足</el-checkbox>
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

    <!-- 商品统计 -->
    <div class="stats-section">
      <el-row :gutter="20">
        <el-col :xs="12" :sm="6" v-for="stat in productStats" :key="stat.key">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" :style="{ backgroundColor: stat.color }">
                <el-icon :size="20">
                  <component :is="stat.icon" />
                </el-icon>
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

    <!-- 商品列表 -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>商品列表</span>
          <div class="header-actions">
            <el-button
              v-if="selectedProducts.length > 0"
              type="danger"
              size="small"
              @click="handleBatchDelete">
              批量删除 ({{ selectedProducts.length }})
            </el-button>
            <el-button
              v-if="selectedProducts.length > 0"
              type="warning"
              size="small"
              @click="handleBatchOffSale">
              批量下架 ({{ selectedProducts.length }})
            </el-button>
            <el-button
              v-if="selectedProducts.length > 0"
              type="success"
              size="small"
              @click="handleBatchOnSale">
              批量上架 ({{ selectedProducts.length }})
            </el-button>
          </div>
        </div>
      </template>

      <el-table
        :data="productList"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        style="width: 100%">
        
        <el-table-column type="selection" width="55" />
        
        <el-table-column label="商品信息" min-width="300">
          <template #default="{ row }">
            <div class="product-info">
              <el-image
                :src="row.image"
                :preview-src-list="[row.image]"
                class="product-image"
                fit="cover"
              />
              <div class="product-details">
                <div class="product-name">{{ row.name }}</div>
                <div class="product-sku">SKU: {{ row.sku }}</div>
                <div class="product-category">{{ getCategoryName(row.category) }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="price" label="价格" width="100" sortable>
          <template #default="{ row }">
            <span class="price">¥{{ row.price }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="stock" label="库存" width="80" sortable>
          <template #default="{ row }">
            <span :class="{ 'low-stock': row.stock <= 10 }">{{ row.stock }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="sales" label="销量" width="80" sortable>
          <template #default="{ row }">
            {{ row.sales }}
          </template>
        </el-table-column>
        
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="createTime" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="viewProduct(row.id)">
              查看
            </el-button>
            <el-button text type="primary" size="small" @click="editProduct(row.id)">
              编辑
            </el-button>
            <el-dropdown @command="(command) => handleProductAction(command, row)">
              <el-button text type="primary" size="small">
                更多<el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item :command="`${row.status === 'on_sale' ? 'off_sale' : 'on_sale'}_${row.id}`">
                    {{ row.status === 'on_sale' ? '下架' : '上架' }}
                  </el-dropdown-item>
                  <el-dropdown-item :command="`copy_${row.id}`">复制商品</el-dropdown-item>
                  <el-dropdown-item :command="`stats_${row.id}`">查看统计</el-dropdown-item>
                  <el-dropdown-item :command="`delete_${row.id}`" divided>删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 商品详情对话框 -->
    <ProductDetail
      v-model="showProductDetail"
      :product-id="currentProductId"
      @refresh="loadProductList"
    />

    <!-- 商品表单对话框 -->
    <ProductForm
      v-model="showProductForm"
      :product-id="editingProductId"
      @success="handleFormSuccess"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Upload, Download, Search, Refresh, ArrowDown,
  ShoppingBag, TrendCharts, Warning, Star
} from '@element-plus/icons-vue'
import ProductDetail from './components/ProductDetail.vue'
import ProductForm from './components/ProductForm.vue'

// 响应式数据
const loading = ref(false)
const selectedProducts = ref([])
const showProductDetail = ref(false)
const currentProductId = ref(null)
const showProductForm = ref(false)
const editingProductId = ref(null)

// 搜索表单
const searchForm = reactive({
  keyword: '',
  category: '',
  status: '',
  minPrice: '',
  maxPrice: '',
  lowStock: false
})

// 分页信息
const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

// 商品统计
const productStats = reactive([
  {
    key: 'total',
    label: '商品总数',
    value: '1,248',
    icon: 'ShoppingBag',
    color: '#1890ff'
  },
  {
    key: 'on_sale',
    label: '在售商品',
    value: '1,156',
    icon: 'TrendCharts',
    color: '#52c41a'
  },
  {
    key: 'low_stock',
    label: '库存预警',
    value: '23',
    icon: 'Warning',
    color: '#faad14'
  },
  {
    key: 'hot_sale',
    label: '热销商品',
    value: '89',
    icon: 'Star',
    color: '#f5222d'
  }
])

// 商品列表
const productList = ref([
  {
    id: 1,
    name: 'iPhone 15 Pro Max 256GB 深空黑色',
    sku: 'IP15PM256BK',
    category: 'electronics',
    price: '9999.00',
    stock: 156,
    sales: 2580,
    status: 'on_sale',
    image: 'https://trae-api-us.mchost.guru/api/ide/v1/text_to_image?prompt=iPhone%2015%20Pro%20Max%20black%20smartphone%20product%20photo&image_size=square',
    createTime: '2024-01-10 10:30:00'
  },
  {
    id: 2,
    name: 'MacBook Pro 14英寸 M3芯片',
    sku: 'MBP14M3SL',
    category: 'electronics',
    price: '14999.00',
    stock: 8,
    sales: 456,
    status: 'on_sale',
    image: 'https://trae-api-us.mchost.guru/api/ide/v1/text_to_image?prompt=MacBook%20Pro%2014%20inch%20silver%20laptop%20product%20photo&image_size=square',
    createTime: '2024-01-08 14:20:00'
  },
  {
    id: 3,
    name: 'AirPods Pro 第三代 无线耳机',
    sku: 'APP3WH',
    category: 'electronics',
    price: '1899.00',
    stock: 234,
    sales: 1890,
    status: 'on_sale',
    image: 'https://trae-api-us.mchost.guru/api/ide/v1/text_to_image?prompt=AirPods%20Pro%20white%20wireless%20earbuds%20product%20photo&image_size=square',
    createTime: '2024-01-05 09:15:00'
  },
  {
    id: 4,
    name: 'iPad Air 第五代 64GB WiFi版',
    sku: 'IPA5W64BL',
    category: 'electronics',
    price: '4399.00',
    stock: 0,
    sales: 678,
    status: 'sold_out',
    image: 'https://trae-api-us.mchost.guru/api/ide/v1/text_to_image?prompt=iPad%20Air%20blue%20tablet%20product%20photo&image_size=square',
    createTime: '2024-01-03 16:45:00'
  },
  {
    id: 5,
    name: 'Apple Watch Series 9 GPS 45mm',
    sku: 'AWS9G45MN',
    category: 'electronics',
    price: '3199.00',
    stock: 89,
    sales: 1234,
    status: 'pending',
    image: 'https://trae-api-us.mchost.guru/api/ide/v1/text_to_image?prompt=Apple%20Watch%20Series%209%20midnight%20smartwatch%20product%20photo&image_size=square',
    createTime: '2024-01-01 11:30:00'
  }
])

// 方法
const getCategoryName = (category) => {
  const categoryMap = {
    'electronics': '数码电子',
    'clothing': '服装鞋帽',
    'home': '家居用品',
    'beauty': '美妆护肤',
    'food': '食品饮料',
    'sports': '运动户外'
  }
  return categoryMap[category] || '未知分类'
}

const getStatusType = (status) => {
  const statusMap = {
    'on_sale': 'success',
    'off_sale': 'info',
    'sold_out': 'warning',
    'pending': 'danger'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    'on_sale': '在售',
    'off_sale': '下架',
    'sold_out': '售罄',
    'pending': '待审核'
  }
  return statusMap[status] || '未知'
}

const formatTime = (time) => {
  return new Date(time).toLocaleString('zh-CN')
}

const handleSearch = () => {
  pagination.currentPage = 1
  loadProductList()
}

const handleReset = () => {
  Object.keys(searchForm).forEach(key => {
    if (typeof searchForm[key] === 'boolean') {
      searchForm[key] = false
    } else {
      searchForm[key] = ''
    }
  })
  handleSearch()
}

const handleSelectionChange = (selection) => {
  selectedProducts.value = selection
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  loadProductList()
}

const handleCurrentChange = (page) => {
  pagination.currentPage = page
  loadProductList()
}

const loadProductList = async () => {
  loading.value = true
  try {
    // 调用真实的API
    const response = await productApi.getProductList({
      ...searchForm,
      page: pagination.currentPage,
      pageSize: pagination.pageSize
    })
    
    productList.value = response.data.list || []
    pagination.total = response.data.total || 0
    
  } catch (error) {
    ElMessage.error('加载商品列表失败')
  } finally {
    loading.value = false
  }
}

const viewProduct = (productId) => {
  currentProductId.value = productId
  showProductDetail.value = true
}

const addProduct = () => {
  editingProductId.value = null
  showProductForm.value = true
}

const editProduct = (productId) => {
  editingProductId.value = productId
  showProductForm.value = true
}

const handleProductAction = async (command, row) => {
  const [action, id] = command.split('_')
  
  switch (action) {
    case 'on':
    case 'off':
      await handleToggleStatus(id, action === 'on' ? 'on_sale' : 'off_sale')
      break
    case 'copy':
      await handleCopyProduct(id)
      break
    case 'stats':
      handleViewStats(id)
      break
    case 'delete':
      await handleDeleteProduct(id)
      break
  }
}

const handleToggleStatus = async (productId, status) => {
  try {
    await ElMessageBox.confirm(
      `确定要${status === 'on_sale' ? '上架' : '下架'}该商品吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 调用API
    // await productApi.updateProductStatus(productId, status)
    
    ElMessage.success(`商品${status === 'on_sale' ? '上架' : '下架'}成功`)
    loadProductList()
    
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const handleCopyProduct = async (productId) => {
  try {
    // 调用API复制商品
    // await productApi.copyProduct(productId)
    
    ElMessage.success('商品复制成功')
    loadProductList()
    
  } catch (error) {
    ElMessage.error('复制商品失败')
  }
}

const handleViewStats = (productId) => {
  // 跳转到商品统计页面
  console.log('查看商品统计:', productId)
}

const handleDeleteProduct = async (productId) => {
  try {
    await ElMessageBox.confirm(
      '删除后不可恢复，确定要删除该商品吗？',
      '确认删除',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'error'
      }
    )
    
    // 调用API
    // await productApi.deleteProduct(productId)
    
    ElMessage.success('商品删除成功')
    loadProductList()
    
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除商品失败')
    }
  }
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedProducts.value.length} 个商品吗？删除后不可恢复。`,
      '批量删除',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'error'
      }
    )
    
    const productIds = selectedProducts.value.map(item => item.id)
    // await productApi.batchDeleteProducts(productIds)
    
    ElMessage.success('批量删除成功')
    selectedProducts.value = []
    loadProductList()
    
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}

const handleBatchOnSale = async () => {
  try {
    const productIds = selectedProducts.value.map(item => item.id)
    // await productApi.batchUpdateStatus(productIds, 'on_sale')
    
    ElMessage.success('批量上架成功')
    selectedProducts.value = []
    loadProductList()
    
  } catch (error) {
    ElMessage.error('批量上架失败')
  }
}

const handleBatchOffSale = async () => {
  try {
    const productIds = selectedProducts.value.map(item => item.id)
    // await productApi.batchUpdateStatus(productIds, 'off_sale')
    
    ElMessage.success('批量下架成功')
    selectedProducts.value = []
    loadProductList()
    
  } catch (error) {
    ElMessage.error('批量下架失败')
  }
}

const handleBatchImport = () => {
  ElMessage.info('批量导入功能开发中...')
}

const handleExport = () => {
  ElMessage.info('导出功能开发中...')
}

const handleFormSuccess = () => {
  loadProductList()
}

// 生命周期
onMounted(() => {
  loadProductList()
})
</script>

<style scoped>
.merchant-products {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: calc(100vh - 60px);
}

/* 页面头部 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.page-title {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #262626;
}

.page-desc {
  margin: 0;
  color: #8c8c8c;
  font-size: 14px;
}

.header-right {
  display: flex;
  gap: 12px;
}

/* 搜索区域 */
.search-card {
  margin-bottom: 20px;
}

.search-form {
  margin-bottom: -18px;
}

/* 统计区域 */
.stats-section {
  margin-bottom: 20px;
}

.stat-card {
  height: 80px;
  transition: transform 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-content {
  display: flex;
  align-items: center;
  height: 100%;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  margin-right: 12px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 20px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 2px;
}

.stat-label {
  font-size: 12px;
  color: #8c8c8c;
}

/* 表格区域 */
.table-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.product-info {
  display: flex;
  align-items: center;
}

.product-image {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  margin-right: 12px;
  flex-shrink: 0;
}

.product-details {
  flex: 1;
  min-width: 0;
}

.product-name {
  font-weight: 500;
  color: #262626;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-sku {
  font-size: 12px;
  color: #8c8c8c;
  margin-bottom: 2px;
}

.product-category {
  font-size: 12px;
  color: #1890ff;
}

.price {
  font-weight: 600;
  color: #f5222d;
}

.low-stock {
  color: #faad14;
  font-weight: 600;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .merchant-products {
    padding: 16px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 16px;
  }
  
  .header-right {
    width: 100%;
    justify-content: flex-start;
    flex-wrap: wrap;
  }
  
  .search-form {
    flex-direction: column;
  }
  
  .search-form .el-form-item {
    margin-right: 0;
    margin-bottom: 16px;
  }
  
  .stat-card {
    margin-bottom: 12px;
  }
  
  .product-info {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .product-image {
    margin-bottom: 8px;
  }
  
  .product-details {
    width: 100%;
  }
}
</style>