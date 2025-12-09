import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import * as cartApi from '@/api/cart'

export const useCartStore = defineStore('cart', () => {
  const cartItems = ref([])
  const loading = ref(false)

  // 计算购物车总数量
  const totalCount = computed(() => {
    return cartItems.value.reduce((sum, item) => sum + item.quantity, 0)
  })

  // 计算购物车总价
  const totalPrice = computed(() => {
    return cartItems.value.reduce((sum, item) => {
      if (item.selected) {
        return sum + item.price * item.quantity
      }
      return sum
    }, 0)
  })

  // 获取选中的商品列表
  const selectedItems = computed(() => {
    return cartItems.value.filter(item => item.selected)
  })

  // 获取购物车列表
  async function fetchCartItems() {
    try {
      loading.value = true
      const response = await cartApi.getCartList()
      if (response.success) {
        cartItems.value = response.data || []
      }
    } catch (error) {
      console.error('获取购物车失败:', error)
    } finally {
      loading.value = false
    }
  }

  // 添加商品到购物车
  // 支持两种调用方式：addToCart(productId, quantity, specifications) 或 addToCart({productId, quantity, specifications})
  async function addToCart(productIdOrData, quantity = 1, specifications = '') {
    try {
      let data
      // 判断第一个参数是对象还是productId
      if (typeof productIdOrData === 'object' && productIdOrData !== null) {
        data = {
          productId: productIdOrData.productId,
          quantity: productIdOrData.quantity || 1,
          specifications: productIdOrData.specifications || ''
        }
      } else {
        data = { productId: productIdOrData, quantity, specifications }
      }
      
      const response = await cartApi.addToCart(data)
      if (response.success) {
        await fetchCartItems()
        return true
      }
      return false
    } catch (error) {
      console.error('添加购物车失败:', error)
      return false
    }
  }

  // 更新购物车数量
  async function updateQuantity(productId, quantity) {
    try {
      const response = await cartApi.updateCartItem({ productId, quantity })
      if (response.success) {
        await fetchCartItems()
        return true
      }
      return false
    } catch (error) {
      console.error('更新购物车失败:', error)
      return false
    }
  }

  // 删除购物车商品
  async function removeFromCart(productId) {
    try {
      const response = await cartApi.removeCartItem(productId)
      if (response.success) {
        await fetchCartItems()
        return true
      }
      return false
    } catch (error) {
      console.error('删除购物车失败:', error)
      return false
    }
  }

  // 选中/取消选中商品
  async function toggleSelect(productId, selected) {
    try {
      const response = await cartApi.toggleCartItemSelect(productId, selected)
      if (response.success) {
        await fetchCartItems()
        return true
      }
      return false
    } catch (error) {
      console.error('选中购物车商品失败:', error)
      return false
    }
  }

  // 清空购物车
  async function clearCart() {
    try {
      const response = await cartApi.clearCart()
      if (response.success) {
        cartItems.value = []
        return true
      }
      return false
    } catch (error) {
      console.error('清空购物车失败:', error)
      return false
    }
  }

  // 切换单个商品选中状态
  async function toggleItemSelected(productId) {
    const item = cartItems.value.find(i => i.productId === productId)
    if (item) {
      return await toggleSelect(productId, !item.selected)
    }
    return false
  }

  // 批量切换选中状态
  async function toggleAllSelected(selected) {
    try {
      // 批量更新所有商品的选中状态
      const promises = cartItems.value.map(item => 
        toggleSelect(item.productId, selected)
      )
      await Promise.all(promises)
      return true
    } catch (error) {
      console.error('批量选中失败:', error)
      return false
    }
  }

  // 删除购物车商品
  async function removeItem(productId) {
    return await removeFromCart(productId)
  }

  // 清空选中的商品(结算后调用)
  async function clearSelected() {
    try {
      const selectedProductIds = selectedItems.value.map(item => item.productId)
      const promises = selectedProductIds.map(productId => removeFromCart(productId))
      await Promise.all(promises)
      return true
    } catch (error) {
      console.error('清空选中商品失败:', error)
      return false
    }
  }

  return {
    cartItems,
    items: cartItems, // 别名,兼容旧代码
    loading,
    totalCount,
    totalPrice,
    selectedItems,
    fetchCartItems,
    loadCartItems: fetchCartItems, // 别名,兼容旧代码
    addToCart,
    addItem: addToCart, // 别名,兼容旧代码
    updateQuantity,
    removeFromCart,
    removeItem,
    toggleSelect,
    toggleItemSelected,
    toggleAllSelected,
    clearCart,
    clearSelected
  }
})
