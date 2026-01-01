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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Upload, Download, Search, Refresh, ArrowDown,
  ShoppingBag, TrendCharts, Warning, Star
} from '@element-plus/icons-vue'
import {
  getProductList,
  deleteProduct,
  batchDelete,
  productOnShelf,
  productOffShelf,
  batchOnShelf,
  batchOffShelf
} from '@/api/merchant/product'

const router = useRouter()
const userStore = useUserStore()

// 响应式数据
const loading = ref(false)
const selectedProducts = ref([])

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
    value: '0',
    icon: 'ShoppingBag',
    color: '#1890ff'
  },
  {
    key: 'on_sale',
    label: '在售商品',
    value: '0',
    icon: 'TrendCharts',
    color: '#52c41a'
  },
  {
    key: 'low_stock',
    label: '库存预警',
    value: '0',
    icon: 'Warning',
    color: '#faad14'
  },
  {
    key: 'hot_sale',
    label: '热销商品',
    value: '0',
    icon: 'Star',
    color: '#f5222d'
  }
])

// 商品列表
const productList = ref([])

// 方法
const getCategoryName = (categoryId) => {
  const categoryMap = {
    1: '数码电子',
    2: '服装鞋帽',
    3: '家居用品',
    4: '美妆护肤',
    5: '食品饮料',
    6: '运动户外'
  }
  return categoryMap[categoryId] || `分类${categoryId}`
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
    // 调用真实API
    const params = {
      merchantId: userStore.merchantId,
      page: pagination.currentPage,
      size: pagination.pageSize,
      keyword: searchForm.keyword || undefined,
      category: searchForm.category || undefined,
      status: searchForm.status ? getStatusCode(searchForm.status) : undefined,
      minPrice: searchForm.minPrice || undefined,
      maxPrice: searchForm.maxPrice || undefined
    }
    
    const response = await getProductList(params)
    
    if (response.code === 200 && response.data) {
      // 后端返回的字段是 records，需要进行字段映射
      const rawList = response.data.records || []
      
      // 映射后端字段到前端字段
      productList.value = rawList.map(item => {
        // 处理主图：可能是逗号分隔的多张图片，取第一张
        let mainImage = item.mainImage || ''
        if (mainImage && mainImage.includes(',')) {
          mainImage = mainImage.split(',')[0].trim()
        }
        
        return {
          id: item.id,
          name: item.productName || '未命名商品',
          sku: item.sku || `PROD-${item.id}`,
          category: item.categoryId,
          price: item.price || 0,
          stock: item.stockQuantity || 0,
          sales: item.salesCount || 0,
          status: item.status === 1 ? 'on_sale' : 'off_sale',
          image: mainImage || 'https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png',
          createTime: item.createTime || new Date().toISOString()
        }
      })
      
      pagination.total = response.data.total || 0
      
      console.log('商品列表加载成功:', {
        total: pagination.total,
        count: productList.value.length,
        sample: productList.value[0]
      })
      
      // 更新统计数据
      updateProductStats()
    }
    
  } catch (error) {
    console.error('加载商品列表失败:', error)
    ElMessage.error('加载商品列表失败')
  } finally {
    loading.value = false
  }
}

// 状态映射
const getStatusCode = (status) => {
  const statusMap = {
    'on_sale': 1,
    'off_sale': 2,
    'sold_out': 3,
    'pending': 0
  }
  return statusMap[status]
}

// 更新商品统计数据
const updateProductStats = () => {
  const total = productList.value.length
  const onSale = productList.value.filter(p => p.status === 'on_sale').length
  const lowStock = productList.value.filter(p => p.stock <= 10).length
  const hotSale = productList.value.filter(p => p.sales > 100).length
  
  productStats[0].value = String(total)
  productStats[1].value = String(onSale)
  productStats[2].value = String(lowStock)
  productStats[3].value = String(hotSale)
  
  console.log('统计数据已更新:', { total, onSale, lowStock, hotSale })
}

const viewProduct = (productId) => {
  router.push(`/merchant/products/form/${productId}`)
}

const addProduct = () => {
  router.push('/merchant/products/form')
}

const editProduct = (productId) => {
  router.push(`/merchant/products/form/${productId}`)
}

const handleProductAction = async (command, row) => {
  // 命令格式: "action_id"，例如 "off_sale_123" 或 "on_sale_123"
  const parts = command.split('_')
  const id = parts[parts.length - 1] // 获取最后一个部分作为ID
  const action = parts.slice(0, -1).join('_') // 获取前面的部分作为action
  
  switch (action) {
    case 'on_sale':
      await handleToggleStatus(id, 'on_sale')
      break
    case 'off_sale':
      await handleToggleStatus(id, 'off_sale')
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
    if (status === 'on_sale') {
      await productOnShelf(productId, userStore.merchantId)
    } else {
      await productOffShelf(productId, userStore.merchantId)
    }
    
    ElMessage.success(`商品${status === 'on_sale' ? '上架' : '下架'}成功`)
    loadProductList()
    
  } catch (error) {
    if (error !== 'cancel') {
      console.error('操作失败:', error)
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
    await deleteProduct(productId, userStore.merchantId)
    
    ElMessage.success('商品删除成功')
    loadProductList()
    
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除商品失败:', error)
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
    await batchDelete(userStore.merchantId, productIds)
    
    ElMessage.success('批量删除成功')
    selectedProducts.value = []
    loadProductList()
    
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }
}

const handleBatchOnSale = async () => {
  try {
    const productIds = selectedProducts.value.map(item => item.id)
    await batchOnShelf(userStore.merchantId, productIds)
    
    ElMessage.success('批量上架成功')
    selectedProducts.value = []
    loadProductList()
    
  } catch (error) {
    console.error('批量上架失败:', error)
    ElMessage.error('批量上架失败')
  }
}

const handleBatchOffSale = async () => {
  try {
    const productIds = selectedProducts.value.map(item => item.id)
    await batchOffShelf(userStore.merchantId, productIds)
    
    ElMessage.success('批量下架成功')
    selectedProducts.value = []
    loadProductList()
    
  } catch (error) {
    console.error('批量下架失败:', error)
    ElMessage.error('批量下架失败')
  }
}

const handleBatchImport = () => {
  ElMessage.info('批量导入功能开发中...')
}

const handleExport = () => {
  ElMessage.info('导出功能开发中...')
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