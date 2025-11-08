<template>
  <div class="addresses-page">
    <div class="page-header">
      <h1>收货地址管理</h1>
      <el-button type="primary" @click="showAddDialog = true">
        <LocalIcon name="tianjia" :size="16" />
        添加新地址
      </el-button>
    </div>

    <div class="addresses-list">
      <div
        v-for="address in addresses"
        :key="address.id"
        class="address-card"
        :class="{ 'is-default': address.isDefault }"
      >
        <div class="address-info">
          <div class="address-header">
            <span class="recipient">{{ address.recipient }}</span>
            <span class="phone">{{ address.phone }}</span>
            <el-tag v-if="address.isDefault" type="success" size="small">默认</el-tag>
          </div>
          <div class="address-detail">
            {{ address.province }} {{ address.city }} {{ address.district }} {{ address.detail }}
          </div>
        </div>
        
        <div class="address-actions">
          <el-button text @click="editAddress(address)">编辑</el-button>
          <el-button text @click="deleteAddress(address.id)">删除</el-button>
          <el-button 
            v-if="!address.isDefault" 
            text 
            type="primary" 
            @click="setDefault(address.id)"
          >
            设为默认
          </el-button>
        </div>
      </div>
    </div>

    <!-- 添加/编辑地址对话框 -->
    <el-dialog
      v-model="showAddDialog"
      :title="editingAddress ? '编辑地址' : '添加地址'"
      width="500px"
      @close="resetForm"
    >
      <el-form
        ref="addressFormRef"
        :model="addressForm"
        :rules="addressRules"
        label-width="80px"
      >
        <el-form-item label="收货人" prop="recipient">
          <el-input v-model="addressForm.recipient" placeholder="请输入收货人姓名" />
        </el-form-item>
        
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="addressForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        
        <el-form-item label="所在地区" prop="region">
          <el-cascader
            v-model="addressForm.region"
            :options="regionOptions"
            placeholder="请选择省市区"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="详细地址" prop="detail">
          <el-input
            v-model="addressForm.detail"
            type="textarea"
            :rows="3"
            placeholder="请输入详细地址"
          />
        </el-form-item>
        
        <el-form-item>
          <el-checkbox v-model="addressForm.isDefault">设为默认地址</el-checkbox>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showAddDialog = false">取消</el-button>
          <el-button type="primary" @click="saveAddress">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import LocalIcon from '@/components/LocalIcon.vue'

const addressFormRef = ref<FormInstance>()
const showAddDialog = ref(false)
const editingAddress = ref(null)

// 地址列表
const addresses = ref([
  {
    id: 1,
    recipient: '张三',
    phone: '13800138000',
    province: '北京市',
    city: '北京市',
    district: '朝阳区',
    detail: '三里屯街道工体北路8号院',
    isDefault: true
  },
  {
    id: 2,
    recipient: '李四',
    phone: '13900139000',
    province: '上海市',
    city: '上海市',
    district: '浦东新区',
    detail: '陆家嘴金融贸易区世纪大道100号',
    isDefault: false
  }
])

// 地址表单
const addressForm = reactive({
  recipient: '',
  phone: '',
  region: [],
  detail: '',
  isDefault: false
})

// 表单验证规则
const addressRules = {
  recipient: [
    { required: true, message: '请输入收货人姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  region: [
    { required: true, message: '请选择所在地区', trigger: 'change' }
  ],
  detail: [
    { required: true, message: '请输入详细地址', trigger: 'blur' }
  ]
}

// 地区选项将从API获取
const regionOptions = ref([])

/**
 * 编辑地址
 */
const editAddress = (address: any) => {
  editingAddress.value = address
  Object.assign(addressForm, {
    recipient: address.recipient,
    phone: address.phone,
    region: [address.province, address.city, address.district],
    detail: address.detail,
    isDefault: address.isDefault
  })
  showAddDialog.value = true
}

/**
 * 删除地址
 */
const deleteAddress = async (addressId: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这个地址吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const index = addresses.value.findIndex(addr => addr.id === addressId)
    if (index > -1) {
      addresses.value.splice(index, 1)
      ElMessage.success('地址删除成功')
    }
  } catch (error) {
    // 用户取消删除
  }
}

/**
 * 设为默认地址
 */
const setDefault = (addressId: number) => {
  addresses.value.forEach(addr => {
    addr.isDefault = addr.id === addressId
  })
  ElMessage.success('默认地址设置成功')
}

/**
 * 保存地址
 */
const saveAddress = async () => {
  if (!addressFormRef.value) return
  
  await addressFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const [province, city, district] = addressForm.region
        
        if (editingAddress.value) {
          // 编辑模式
          const index = addresses.value.findIndex(addr => addr.id === editingAddress.value.id)
          if (index > -1) {
            addresses.value[index] = {
              ...addresses.value[index],
              recipient: addressForm.recipient,
              phone: addressForm.phone,
              province,
              city,
              district,
              detail: addressForm.detail,
              isDefault: addressForm.isDefault
            }
          }
          ElMessage.success('地址更新成功')
        } else {
          // 新增模式
          const newAddress = {
            id: Date.now(),
            recipient: addressForm.recipient,
            phone: addressForm.phone,
            province,
            city,
            district,
            detail: addressForm.detail,
            isDefault: addressForm.isDefault
          }
          
          // 如果设为默认，取消其他默认地址
          if (addressForm.isDefault) {
            addresses.value.forEach(addr => {
              addr.isDefault = false
            })
          }
          
          addresses.value.push(newAddress)
          ElMessage.success('地址添加成功')
        }
        
        showAddDialog.value = false
        resetForm()
      } catch (error) {
        ElMessage.error('保存失败')
      }
    }
  })
}

/**
 * 重置表单
 */
const resetForm = () => {
  editingAddress.value = null
  Object.assign(addressForm, {
    recipient: '',
    phone: '',
    region: [],
    detail: '',
    isDefault: false
  })
  addressFormRef.value?.resetFields()
}

onMounted(() => {
  // 初始化数据
})
</script>

<style scoped>
.addresses-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.page-header h1 {
  margin: 0;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.addresses-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.address-card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 20px;
  background: white;
  transition: all 0.3s ease;
}

.address-card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.address-card.is-default {
  border-color: #409eff;
  background: #f0f9ff;
}

.address-info {
  margin-bottom: 15px;
}

.address-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 8px;
}

.recipient {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.phone {
  color: #606266;
}

.address-detail {
  color: #606266;
  line-height: 1.5;
}

.address-actions {
  display: flex;
  gap: 10px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 本地图标样式 */
.local-icon {
  display: inline-block;
  vertical-align: middle;
  object-fit: contain;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .addresses-container {
    padding: 15px;
  }
  
  .address-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .address-actions {
    align-self: flex-end;
  }
}
</style>