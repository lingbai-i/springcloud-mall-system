<template>
  <div class="checkout-container">
    <!-- 返回导航 -->
    <div class="page-navigation">
      <el-button 
        type="text" 
        @click="goBack"
        class="back-btn">
        <el-icon><ArrowLeft /></el-icon>
        <span>返回</span>
      </el-button>
      <el-button 
        type="text" 
        @click="goHome"
        class="home-btn">
        <el-icon><House /></el-icon>
        <span>首页</span>
      </el-button>
    </div>

    <!-- 页面标题 -->
    <div class="page-header">
      <h2>确认订单</h2>
    </div>

    <div class="checkout-content">
      <!-- 收货地址 -->
      <div class="address-section">
        <h3>收货地址</h3>
        <div class="address-list" v-if="addresses.length > 0">
          <div 
            v-for="address in addresses" 
            :key="address.id"
            class="address-item"
            :class="{ active: selectedAddress?.id === address.id }"
            @click="selectAddress(address)">
            <div class="address-info">
              <div class="receiver-info">
                <span class="name">{{ address.receiverName }}</span>
                <span class="phone">{{ address.receiverPhone }}</span>
                <el-tag v-if="address.isDefault" type="primary" size="small">默认</el-tag>
              </div>
              <div class="address-detail">
                {{ address.province }} {{ address.city }} {{ address.district }} {{ address.detailAddress }}
              </div>
            </div>
            <div class="address-actions">
              <el-button type="text" @click.stop="editAddress(address)">编辑</el-button>
            </div>
          </div>
        </div>
        <div class="no-address" v-else>
          <el-empty description="暂无收货地址">
            <el-button type="primary" @click="addAddress">添加收货地址</el-button>
          </el-empty>
        </div>
        <el-button v-if="addresses.length > 0" type="text" @click="addAddress" class="add-address-btn">
          + 添加新地址
        </el-button>
      </div>

      <!-- 商品清单 -->
      <div class="goods-section">
        <h3>商品清单</h3>
        <div v-if="checkoutItems.length > 0" class="goods-list">
          <div v-for="item in checkoutItems" :key="item.productId" class="goods-item">
            <img :src="item.productImage" :alt="item.productName" class="product-image">
            <div class="product-info">
              <h4 class="product-name">{{ item.productName }}</h4>
              <p class="product-specs" v-if="item.specifications">{{ item.specifications }}</p>
            </div>
            <div class="product-price">¥{{ item.price }}</div>
            <div class="product-quantity">x{{ item.quantity }}</div>
            <div class="product-total">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
          </div>
        </div>
        <div v-else class="no-goods">
          <el-empty description="暂无商品">
            <el-button type="primary" @click="goBack">返回购物车</el-button>
          </el-empty>
        </div>
      </div>

      <!-- 支付方式 -->
      <div class="payment-section">
        <h3>支付方式</h3>
        <div class="payment-options">
          <div 
            class="payment-card"
            :class="{ active: selectedPayment === 1 }"
            @click="selectPayment(1)">
            <div class="radio-indicator">
              <el-icon v-if="selectedPayment === 1" color="#409eff"><Check /></el-icon>
            </div>
            <div class="option-content">
              <div class="option-main">
                <div class="payment-name">在线支付</div>
                <div class="payment-desc">支持微信、支付宝、银行卡</div>
              </div>
            </div>
          </div>
          <div 
            class="payment-card"
            :class="{ active: selectedPayment === 2 }"
            @click="selectPayment(2)">
            <div class="radio-indicator">
              <el-icon v-if="selectedPayment === 2" color="#409eff"><Check /></el-icon>
            </div>
            <div class="option-content">
              <div class="option-main">
                <div class="payment-name">货到付款</div>
                <div class="payment-desc">送货上门后付款</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 订单备注 -->
      <div class="remark-section">
        <h3>订单备注</h3>
        <el-input
          v-model="orderRemark"
          type="textarea"
          :rows="3"
          placeholder="请输入订单备注（选填）"
          maxlength="200"
          show-word-limit>
        </el-input>
      </div>
    </div>

    <!-- 底部结算栏 -->
    <div class="checkout-footer">
      <div class="footer-container">
        <div class="price-summary">
          <div class="price-row">
            <span class="price-label">商品总价：</span>
            <span class="price-value">¥{{ goodsTotal.toFixed(2) }}</span>
          </div>
          <div class="price-row total-row">
            <span class="price-label">应付总额：</span>
            <span class="total-price">¥{{ goodsTotal.toFixed(2) }}</span>
          </div>
        </div>
        <el-button 
          type="primary" 
          size="large"
          @click="submitOrder"
          :loading="submitting"
          :disabled="!canSubmit"
          class="submit-btn">
          提交订单
        </el-button>
      </div>
    </div>

    <!-- 地址编辑对话框 -->
    <el-dialog 
      v-model="addressDialogVisible" 
      :title="editingAddress ? '编辑地址' : '添加地址'"
      width="500px">
      <el-form :model="addressForm" :rules="addressRules" ref="addressFormRef" label-width="80px">
        <el-form-item label="收货人" prop="receiverName">
          <el-input v-model="addressForm.receiverName" placeholder="请输入收货人姓名"></el-input>
        </el-form-item>
        <el-form-item label="手机号" prop="receiverPhone">
          <el-input v-model="addressForm.receiverPhone" placeholder="请输入手机号"></el-input>
        </el-form-item>
        <el-form-item label="所在地区" prop="region">
          <el-cascader
            v-model="addressForm.region"
            :options="regionOptions"
            placeholder="请选择省市区"
            style="width: 100%">
          </el-cascader>
        </el-form-item>
        <el-form-item label="详细地址" prop="detailAddress">
          <el-input 
            v-model="addressForm.detailAddress" 
            type="textarea"
            :rows="2"
            placeholder="请输入详细地址">
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="addressForm.isDefault">设为默认地址</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addressDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveAddress" :loading="addressSaving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, House, Check } from '@element-plus/icons-vue'
import { useRouter, useRoute } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'
import { getAddressList, addAddress as apiAddAddress, updateAddress as apiUpdateAddress } from '@/api/address'
import { createOrder } from '@/api/order'
import regionData, { getRegionName } from '@/utils/region-data'

const router = useRouter()
const route = useRoute()
const cartStore = useCartStore()
const userStore = useUserStore()

// 响应式数据
const checkoutItems = ref([])
const addresses = ref([])
const selectedAddress = ref(null)
const selectedPayment = ref(1)
const orderRemark = ref('')
const submitting = ref(false)

// 地址相关
const addressDialogVisible = ref(false)
const editingAddress = ref(null)
const addressSaving = ref(false)
const addressFormRef = ref()
const addressForm = ref({
  receiverName: '',
  receiverPhone: '',
  region: [],
  detailAddress: '',
  isDefault: false
})

// 地址验证规则
const addressRules = {
  receiverName: [
    { required: true, message: '请输入收货人姓名', trigger: 'blur' }
  ],
  receiverPhone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  region: [
    { required: true, message: '请选择所在地区', trigger: 'change' }
  ],
  detailAddress: [
    { required: true, message: '请输入详细地址', trigger: 'blur' }
  ]
}

// 地区选项
 const regionOptions = ref(regionData)

// 计算属性
const goodsTotal = computed(() => {
  return checkoutItems.value.reduce((total, item) => {
    return total + (item.price * item.quantity)
  }, 0)
})

const canSubmit = computed(() => {
  return selectedAddress.value && checkoutItems.value.length > 0 && !submitting.value
})

// 方法
const loadCheckoutData = () => {
  console.log('=== 开始加载结算数据 ===')
  console.log('购物车Store:', cartStore)
  console.log('购物车所有商品:', cartStore.cartItems)
  
  // 从购物车获取选中的商品
  const selectedItems = cartStore.selectedItems
  console.log('选中的商品:', selectedItems)
  console.log('选中商品数量:', selectedItems ? selectedItems.length : 0)
  
  if (!selectedItems || selectedItems.length === 0) {
    ElMessage.warning('请先选择要结算的商品')
    router.push('/cart')
    return
  }
  
  console.log('第一个商品数据:', selectedItems[0])
  
  // 复制商品数据，确保包含所有必要字段
  checkoutItems.value = selectedItems.map(item => ({
    productId: item.productId,
    productName: item.productName,
    productImage: item.productImage,
    price: item.price,
    quantity: item.quantity,
    specifications: item.specifications || ''
  }))
  
  console.log('结算商品列表:', checkoutItems.value)
  console.log('=== 结算数据加载完成 ===')
}

const loadAddresses = async () => {
  try {
    // 从后端API获取用户地址列表
    const response = await getAddressList()
    console.log('地址列表响应:', response)
    
    if (response.success) {
      addresses.value = response.data || []
      
      // 选择默认地址
      const defaultAddress = addresses.value.find(addr => addr.isDefault)
      if (defaultAddress) {
        selectedAddress.value = defaultAddress
      } else if (addresses.value.length > 0) {
        selectedAddress.value = addresses.value[0]
      }
    }
  } catch (error) {
    console.error('加载地址失败:', error)
    // 如果后端未实现，使用空数组
    addresses.value = []
  }
}

const selectAddress = (address) => {
  selectedAddress.value = address
}

// 选择支付方式
const selectPayment = (paymentType) => {
  selectedPayment.value = paymentType
  console.log('选择支付方式:', paymentType)
}

const addAddress = () => {
  editingAddress.value = null
  addressForm.value = {
    receiverName: '',
    receiverPhone: '',
    region: [],
    detailAddress: '',
    isDefault: false
  }
  addressDialogVisible.value = true
}

const editAddress = (address) => {
  editingAddress.value = address
  
  // 根据地址数据格式设置表单值
  let region = []
  if (address.provinceCode && address.cityCode && address.districtCode) {
    region = [address.provinceCode, address.cityCode, address.districtCode]
  } else if (address.province && address.city && address.district) {
    region = [address.province, address.city, address.district]
  }
  
  addressForm.value = {
    receiverName: address.receiverName,
    receiverPhone: address.receiverPhone,
    region: region,
    detailAddress: address.detailAddress,
    isDefault: address.isDefault
  }
  addressDialogVisible.value = true
}

const saveAddress = async () => {
  try {
    await addressFormRef.value.validate()
    addressSaving.value = true
    
    // 构建地址数据
    const addressData = {
      receiverName: addressForm.value.receiverName,
      receiverPhone: addressForm.value.receiverPhone,
      provinceCode: addressForm.value.region[0],
      cityCode: addressForm.value.region[1],
      districtCode: addressForm.value.region[2],
      province: getRegionName(addressForm.value.region[0]),
      city: getRegionName(addressForm.value.region[1]),
      district: getRegionName(addressForm.value.region[2]),
      detailAddress: addressForm.value.detailAddress,
      isDefault: addressForm.value.isDefault
    }
    
    console.log('保存地址数据:', addressData)
    
    let response
    if (editingAddress.value) {
      // 编辑地址
      response = await apiUpdateAddress(editingAddress.value.id, addressData)
      if (response.success) {
        ElMessage.success('地址更新成功')
        // 重新加载地址列表
        await loadAddresses()
      } else {
        ElMessage.error(response.message || '地址更新失败')
      }
    } else {
      // 新增地址
      response = await apiAddAddress(addressData)
      if (response.success) {
        ElMessage.success('地址添加成功')
        // 重新加载地址列表
        await loadAddresses()
      } else {
        ElMessage.error(response.message || '地址添加失败')
      }
    }
    
    if (response.success) {
      addressDialogVisible.value = false
    }
  } catch (error) {
    console.error('保存地址失败:', error)
    if (error !== 'cancel') {
      ElMessage.error('保存地址失败')
    }
  } finally {
    addressSaving.value = false
  }
}

const submitOrder = async () => {
  if (!canSubmit.value) return
  
  try {
    await ElMessageBox.confirm('确认提交订单？', '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    submitting.value = true
    
    // 构建订单数据
    const orderData = {
      userId: userStore.userId,
      // 地址信息
      receiverName: selectedAddress.value.receiverName,
      receiverPhone: selectedAddress.value.receiverPhone,
      receiverAddress: `${selectedAddress.value.province}${selectedAddress.value.city}${selectedAddress.value.district}${selectedAddress.value.detailAddress}`,
      // 商品信息 - 后端期望orderItems字段
      orderItems: checkoutItems.value.map(item => ({
        productId: item.productId,
        productSpec: item.specifications || '',
        quantity: item.quantity
      })),
      // 订单备注
      remark: orderRemark.value
    }
    
    console.log('提交订单数据:', orderData)
    
    // 调用后端API创建订单
    const response = await createOrder(orderData)
    console.log('订单创建响应:', response)
    
    if (response.success) {
      ElMessage.success('订单提交成功')
      
      // 清空购物车中的已结算商品
      await cartStore.clearSelected()
      
      // 跳转到订单详情或支付页面
      if (selectedPayment.value === 1) {
        // 在线支付，跳转到支付页面
        router.push(`/payment/${response.data.orderId}`)
      } else {
        // 货到付款，跳转到订单列表
        router.push('/user/orders')
      }
    } else {
      ElMessage.error(response.message || '订单提交失败')
    }
    
  } catch (error) {
    if (error !== 'cancel') {
      console.error('订单提交失败:', error)
      ElMessage.error('订单提交失败')
    }
  } finally {
    submitting.value = false
  }
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 返回首页
const goHome = () => {
  router.push('/')
}

// 生命周期
onMounted(async () => {
  console.log('=== 结算页面 onMounted ===')
  
  // 先加载购物车数据
  if (cartStore.cartItems.length === 0) {
    console.log('购物车为空，先加载购物车数据')
    await cartStore.fetchCartItems()
  }
  
  loadCheckoutData()
  loadAddresses()
})
</script>

<style scoped>
.checkout-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  padding-bottom: 120px; /* 为底部结算栏留出空间 */
}

/* 页面导航 */
.page-navigation {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
}

.back-btn,
.home-btn {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 8px 16px;
  color: #606266;
  font-size: 14px;
  transition: all 0.3s;
}

.back-btn:hover,
.home-btn:hover {
  color: #409eff;
  background-color: #ecf5ff;
}

.page-header {
  margin-bottom: 30px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e4e7ed;
}

.page-header h2 {
  margin: 0;
  color: #303133;
}

.checkout-content > div {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
}

.checkout-content h3 {
  margin: 0 0 20px 0;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
}

/* 地址样式 */
.address-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  border: 2px solid #ebeef5;
  border-radius: 8px;
  margin-bottom: 10px;
  cursor: pointer;
  transition: all 0.3s;
}

.address-item:hover {
  border-color: #409eff;
}

.address-item.active {
  border-color: #409eff;
  background-color: #f0f9ff;
}

.receiver-info {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 5px;
}

.receiver-info .name {
  font-weight: 600;
  color: #303133;
}

.receiver-info .phone {
  color: #606266;
}

.address-detail {
  color: #606266;
  font-size: 14px;
}

.add-address-btn {
  margin-top: 10px;
}

/* 商品清单样式 */
.goods-item {
  display: grid;
  grid-template-columns: 80px 1fr 100px 80px 100px;
  align-items: center;
  gap: 15px;
  padding: 15px 0;
  border-bottom: 1px solid #f5f7fa;
}

.goods-item:last-child {
  border-bottom: none;
}

.product-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #ebeef5;
}

.product-name {
  margin: 0 0 5px 0;
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

.product-specs {
  margin: 0;
  font-size: 12px;
  color: #909399;
}

.product-price {
  color: #e6a23c;
  font-weight: 500;
}

.product-quantity {
  color: #606266;
  text-align: center;
}

.product-total {
  color: #f56c6c;
  font-weight: 600;
  text-align: right;
}

.no-goods {
  padding: 40px 20px;
  text-align: center;
}

/* 配送和支付方式样式 */
.payment-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.payment-card {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  border: 2px solid #dcdfe6;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  background: #fff;
}

.payment-card:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.15);
}

.payment-card.active {
  border-color: #409eff;
  background-color: #f0f9ff;
}

.radio-indicator {
  flex-shrink: 0;
  width: 20px;
  height: 20px;
  border: 2px solid #dcdfe6;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 2px;
  transition: all 0.3s;
}

.payment-card.active .radio-indicator {
  border-color: #409eff;
  background-color: #fff;
}

/* 选项内容布局 */
.option-content {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
}

.option-main {
  flex: 1;
}

.payment-name {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 6px;
  line-height: 1.4;
}

.payment-desc {
  font-size: 13px;
  color: #909399;
  line-height: 1.5;
}

/* 底部结算栏 */
.checkout-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  border-top: 2px solid #ebeef5;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.08);
  z-index: 1000;
}

.footer-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 30px;
}

.price-summary {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
}

.price-label {
  color: #606266;
  font-weight: 500;
}

.price-value {
  color: #303133;
  font-weight: 500;
}

.total-row {
  font-size: 18px;
  font-weight: 600;
  padding-top: 12px;
  margin-top: 8px;
  border-top: 1px solid #ebeef5;
}

.total-row .price-label {
  color: #303133;
}

.total-price {
  color: #f56c6c;
  font-size: 24px;
  font-weight: 700;
}

.submit-btn {
  min-width: 160px;
  height: 50px;
  font-size: 16px;
  font-weight: 600;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .goods-item {
    grid-template-columns: 60px 1fr;
    gap: 10px;
  }
  
  .product-price,
  .product-quantity,
  .product-total {
    grid-column: 2;
    text-align: left;
  }
  
  .footer-container {
    flex-direction: column;
    gap: 15px;
  }
  
  .price-summary {
    width: 100%;
  }
  
  .submit-btn {
    width: 100%;
  }
  
  .option-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .delivery-fee {
    margin-left: 0;
  }
}
</style>