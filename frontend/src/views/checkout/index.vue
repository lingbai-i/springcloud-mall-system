<template>
  <div class="checkout-container">
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
        <div class="goods-list">
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
      </div>

      <!-- 配送方式 -->
      <div class="delivery-section">
        <h3>配送方式</h3>
        <el-radio-group v-model="selectedDelivery">
          <el-radio :value="1" class="delivery-option">
            <div class="delivery-info">
              <div class="delivery-name">标准配送</div>
              <div class="delivery-desc">预计3-5个工作日送达</div>
            </div>
            <div class="delivery-fee">免运费</div>
          </el-radio>
          <el-radio :value="2" class="delivery-option">
            <div class="delivery-info">
              <div class="delivery-name">次日达</div>
              <div class="delivery-desc">次日送达，限部分地区</div>
            </div>
            <div class="delivery-fee">¥15</div>
          </el-radio>
        </el-radio-group>
      </div>

      <!-- 支付方式 -->
      <div class="payment-section">
        <h3>支付方式</h3>
        <el-radio-group v-model="selectedPayment">
          <el-radio :value="1" class="payment-option">
            <div class="payment-info">
              <div class="payment-name">在线支付</div>
              <div class="payment-desc">支持微信、支付宝、银行卡</div>
            </div>
          </el-radio>
          <el-radio :value="2" class="payment-option">
            <div class="payment-info">
              <div class="payment-name">货到付款</div>
              <div class="payment-desc">送货上门后付款</div>
            </div>
          </el-radio>
        </el-radio-group>
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
      <div class="price-summary">
        <div class="price-item">
          <span>商品总价：</span>
          <span>¥{{ goodsTotal.toFixed(2) }}</span>
        </div>
        <div class="price-item">
          <span>运费：</span>
          <span>¥{{ deliveryFee.toFixed(2) }}</span>
        </div>
        <div class="price-item total">
          <span>应付总额：</span>
          <span class="total-price">¥{{ totalPrice.toFixed(2) }}</span>
        </div>
      </div>
      <el-button 
        type="primary" 
        size="large"
        @click="submitOrder"
        :loading="submitting"
        :disabled="!canSubmit">
        提交订单
      </el-button>
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
import { useRouter, useRoute } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const cartStore = useCartStore()
const userStore = useUserStore()

// 响应式数据
const checkoutItems = ref([])
const addresses = ref([])
const selectedAddress = ref(null)
const selectedDelivery = ref(1)
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

// 地区选项（简化版，实际应该从接口获取）
const regionOptions = ref([
  {
    value: '110000',
    label: '北京市',
    children: [
      {
        value: '110100',
        label: '北京市',
        children: [
          { value: '110101', label: '东城区' },
          { value: '110102', label: '西城区' },
          { value: '110105', label: '朝阳区' },
          { value: '110106', label: '丰台区' }
        ]
      }
    ]
  },
  {
    value: '310000',
    label: '上海市',
    children: [
      {
        value: '310100',
        label: '上海市',
        children: [
          { value: '310101', label: '黄浦区' },
          { value: '310104', label: '徐汇区' },
          { value: '310105', label: '长宁区' },
          { value: '310106', label: '静安区' }
        ]
      }
    ]
  }
])

// 计算属性
const goodsTotal = computed(() => {
  return checkoutItems.value.reduce((total, item) => {
    return total + (item.price * item.quantity)
  }, 0)
})

const deliveryFee = computed(() => {
  return selectedDelivery.value === 2 ? 15 : 0
})

const totalPrice = computed(() => {
  return goodsTotal.value + deliveryFee.value
})

const canSubmit = computed(() => {
  return selectedAddress.value && checkoutItems.value.length > 0 && !submitting.value
})

// 方法
const loadCheckoutData = () => {
  // 从购物车获取选中的商品
  const selectedItems = cartStore.selectedItems
  if (selectedItems.length === 0) {
    ElMessage.warning('请先选择要结算的商品')
    router.push('/cart')
    return
  }
  
  checkoutItems.value = selectedItems.map(item => ({ ...item }))
}

const loadAddresses = async () => {
  try {
    // 从后端API获取用户地址列表
    const response = await getUserAddresses()
    addresses.value = response.data || []
    
    // 选择默认地址
    const defaultAddress = addresses.value.find(addr => addr.isDefault)
    if (defaultAddress) {
      selectedAddress.value = defaultAddress
    } else if (addresses.value.length > 0) {
      selectedAddress.value = addresses.value[0]
    }
  } catch (error) {
    ElMessage.error('加载地址失败')
  }
}

const selectAddress = (address) => {
  selectedAddress.value = address
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
  addressForm.value = {
    receiverName: address.receiverName,
    receiverPhone: address.receiverPhone,
    region: [address.province, address.city, address.district],
    detailAddress: address.detailAddress,
    isDefault: address.isDefault
  }
  addressDialogVisible.value = true
}

const saveAddress = async () => {
  try {
    await addressFormRef.value.validate()
    addressSaving.value = true
    
    // TODO: 调用后端API保存地址
    const addressData = {
      ...addressForm.value,
      province: addressForm.value.region[0],
      city: addressForm.value.region[1],
      district: addressForm.value.region[2]
    }
    
    if (editingAddress.value) {
      // 编辑地址
      const index = addresses.value.findIndex(addr => addr.id === editingAddress.value.id)
      if (index > -1) {
        addresses.value[index] = { ...editingAddress.value, ...addressData }
      }
      ElMessage.success('地址更新成功')
    } else {
      // 新增地址
      const newAddress = {
        id: Date.now(),
        ...addressData
      }
      addresses.value.push(newAddress)
      ElMessage.success('地址添加成功')
    }
    
    addressDialogVisible.value = false
  } catch (error) {
    console.error('保存地址失败:', error)
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
      items: checkoutItems.value,
      address: selectedAddress.value,
      deliveryType: selectedDelivery.value,
      paymentType: selectedPayment.value,
      remark: orderRemark.value,
      goodsTotal: goodsTotal.value,
      deliveryFee: deliveryFee.value,
      totalPrice: totalPrice.value
    }
    
    // TODO: 调用后端API提交订单
    console.log('提交订单数据:', orderData)
    
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    ElMessage.success('订单提交成功')
    
    // 清空购物车中的已结算商品
    cartStore.clearSelected()
    
    // 跳转到订单详情或支付页面
    router.push('/orders')
    
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('订单提交失败')
    }
  } finally {
    submitting.value = false
  }
}

// 生命周期
onMounted(() => {
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

/* 配送和支付方式样式 */
.delivery-option,
.payment-option {
  display: flex !important;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding: 15px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  margin-bottom: 10px;
}

.delivery-info,
.payment-info {
  flex: 1;
}

.delivery-name,
.payment-name {
  font-weight: 500;
  color: #303133;
  margin-bottom: 5px;
}

.delivery-desc,
.payment-desc {
  font-size: 12px;
  color: #909399;
}

.delivery-fee {
  color: #e6a23c;
  font-weight: 500;
}

/* 底部结算栏 */
.checkout-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  border-top: 1px solid #ebeef5;
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  z-index: 1000;
}

.price-summary {
  flex: 1;
}

.price-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
  color: #606266;
}

.price-item.total {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #ebeef5;
}

.total-price {
  color: #f56c6c;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .goods-item {
    grid-template-columns: 1fr;
    gap: 10px;
    text-align: center;
  }
  
  .checkout-footer {
    flex-direction: column;
    gap: 15px;
  }
  
  .price-summary {
    width: 100%;
  }
}
</style>