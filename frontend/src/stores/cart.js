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
  async function addToCart(productId, quantity = 1, specifications = '') {
    try {
      const response = await cartApi.addToCart({ productId, quantity, specifications })
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
      const response = await cartApi.selectCartItem({ productId, selected })
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

  return {
    cartItems,
    loading,
    totalCount,
    totalPrice,
    fetchCartItems,
    addToCart,
    updateQuantity,
    removeFromCart,
    toggleSelect,
    clearCart
  }
})
