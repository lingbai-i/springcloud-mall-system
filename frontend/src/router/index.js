import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

/**
 * Vue Router路由配置
 * 定义应用程序的路由规则和导航守卫
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */

// 路由配置
const routes = [
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/layout/index.vue'),
    redirect: '/home',
    children: [
      {
        path: '/home',
        name: 'Home',
        component: () => import('@/views/Home.vue'),
        meta: {
          title: '首页',
          keepAlive: true
        }
      },
      {
        path: '/category/:id?',
        name: 'Category',
        component: () => import('@/views/category/index.vue'),
        meta: {
          title: '商品分类',
          keepAlive: true
        }
      },
      {
        path: '/products',
        name: 'ProductList',
        component: () => import('@/views/product/list.vue'),
        meta: {
          title: '商品列表',
          keepAlive: true
        }
      },
      {
        path: '/products/category/:categoryId',
        name: 'ProductListByCategory',
        component: () => import('@/views/product/list.vue'),
        meta: {
          title: '商品列表',
          keepAlive: true
        }
      },
      {
        path: '/product/:id',
        name: 'ProductDetail',
        component: () => import('@/views/product/detail.vue'),
        meta: {
          title: '商品详情'
        }
      },
      {
        path: '/search',
        name: 'Search',
        component: () => import('@/views/search/index.vue'),
        meta: {
          title: '搜索结果'
        }
      },
      {
        path: '/cart',
        name: 'Cart',
        component: () => import('@/views/cart/index.vue'),
        meta: {
          title: '购物车',
          requiresAuth: true
        }
      },
      {
        path: '/user',
        name: 'User',
        component: () => import('@/views/user/index.vue'),
        meta: {
          title: '个人中心',
          requiresAuth: true
        },
        children: [
          {
            path: 'profile',
            name: 'UserProfile',
            component: () => import('@/views/user/profile.vue'),
            meta: {
              title: '个人信息',
              requiresAuth: true
            }
          },
          {
            path: 'security',
            name: 'UserSecurity',
            component: () => import('@/views/user/security.vue'),
            meta: {
              title: '账户安全',
              requiresAuth: true
            }
          },
          {
            path: 'orders',
            name: 'UserOrders',
            component: () => import('@/views/user/orders.vue'),
            meta: {
              title: '我的订单',
              requiresAuth: true
            }
          },
          {
            path: 'password',
            name: 'UserPassword',
            component: () => import('@/views/user/password.vue'),
            meta: {
              title: '修改密码',
              requiresAuth: true
            }
          },
          {
            path: 'addresses',
            name: 'UserAddresses',
            component: () => import('@/views/user/addresses.vue'),
            meta: {
              title: '收货地址',
              requiresAuth: true
            }
          },
          {
            path: 'favorites',
            name: 'UserFavorites',
            component: () => import('@/views/user/favorites.vue'),
            meta: {
              title: '我的收藏',
              requiresAuth: true
            }
          },
          {
            path: 'settings',
            name: 'UserSettings',
            component: () => import('@/views/user/Settings.vue'),
            meta: {
              title: '系统设置',
              requiresAuth: true
            }
          }
        ]
      },
      {
        path: '/order/:id',
        name: 'OrderDetail',
        component: () => import('@/views/order/detail.vue'),
        meta: {
          title: '订单详情',
          requiresAuth: true
        }
      }
    ]
  },
  {
    path: '/auth/login',
    name: 'Login',
    component: () => import('@/views/auth/login.vue'),
    meta: {
      title: '登录',
      hideLayout: true
    }
  },
  {
    path: '/auth/register',
    name: 'Register',
    component: () => import('@/views/auth/register.vue'),
    meta: {
      title: '注册',
      hideLayout: true
    }
  },
  // 兼容旧路径
  {
    path: '/login',
    redirect: '/auth/login'
  },
  {
    path: '/register',
    redirect: '/auth/register'
  },
  {
    path: '/checkout',
    name: 'Checkout',
    component: () => import('@/views/checkout/index.vue'),
    meta: {
      title: '结算',
      requiresAuth: true,
      hideLayout: true
    }
  },
  // 管理员端路由
  {
    path: '/admin',
    name: 'AdminLayout',
    component: () => import('@/layout/admin/index.vue'),
    redirect: '/admin/dashboard',
    meta: {
      requiresAuth: true,
      requiresAdmin: true,
      hideLayout: true
    },
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/dashboard/index.vue'),
        meta: {
          title: '管理员仪表板',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/users/index.vue'),
        meta: {
          title: '用户管理',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'users/:id',
        name: 'AdminUserDetail',
        component: () => import('@/views/admin/users/detail.vue'),
        meta: {
          title: '用户详情',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'merchants',
        name: 'AdminMerchants',
        component: () => import('@/views/admin/merchants/index.vue'),
        meta: {
          title: '商家管理',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'merchants/audit',
        name: 'AdminMerchantAudit',
        component: () => import('@/views/admin/merchants/audit.vue'),
        meta: {
          title: '商家审核',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'merchants/:id',
        name: 'AdminMerchantDetail',
        component: () => import('@/views/admin/merchants/detail.vue'),
        meta: {
          title: '商家详情',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'products',
        name: 'AdminProducts',
        component: () => import('@/views/admin/products/index.vue'),
        meta: {
          title: '商品管理',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'products/audit',
        name: 'AdminProductAudit',
        component: () => import('@/views/admin/products/audit.vue'),
        meta: {
          title: '商品审核',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'products/categories',
        name: 'AdminProductCategories',
        component: () => import('@/views/admin/products/categories.vue'),
        meta: {
          title: '分类管理',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'orders',
        name: 'AdminOrders',
        component: () => import('@/views/admin/orders/index.vue'),
        meta: {
          title: '订单管理',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'orders/refunds',
        name: 'AdminOrderRefunds',
        component: () => import('@/views/admin/orders/refunds.vue'),
        meta: {
          title: '退款处理',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'statistics',
        name: 'AdminStatistics',
        component: () => import('@/views/admin/statistics/index.vue'),
        meta: {
          title: '数据统计',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'settings',
        name: 'AdminSettings',
        component: () => import('@/views/admin/settings/index.vue'),
        meta: {
          title: '系统设置',
          requiresAuth: true,
          requiresAdmin: true
        }
      }
    ]
  },
  // 商家端路由
  {
    path: '/merchant',
    name: 'MerchantLayout',
    component: () => import('@/layout/merchant/index.vue'),
    redirect: '/merchant/dashboard',
    meta: {
      requiresAuth: true,
      requiresMerchant: true,
      hideLayout: true
    },
    children: [
      {
        path: 'dashboard',
        name: 'MerchantDashboard',
        component: () => import('@/views/merchant/dashboard/index.vue'),
        meta: {
          title: '商家仪表板',
          requiresAuth: true,
          requiresMerchant: true
        }
      },
      {
        path: 'products',
        name: 'MerchantProducts',
        component: () => import('@/views/merchant/products/index.vue'),
        meta: {
          title: '商品管理',
          requiresAuth: true,
          requiresMerchant: true
        }
      },
      {
        path: 'products/add',
        name: 'MerchantProductAdd',
        component: () => import('@/views/merchant/products/add.vue'),
        meta: {
          title: '添加商品',
          requiresAuth: true,
          requiresMerchant: true
        }
      },
      {
        path: 'products/edit/:id',
        name: 'MerchantProductEdit',
        component: () => import('@/views/merchant/products/edit.vue'),
        meta: {
          title: '编辑商品',
          requiresAuth: true,
          requiresMerchant: true
        }
      },
      {
        path: 'products/inventory',
        name: 'MerchantProductInventory',
        component: () => import('@/views/merchant/products/inventory.vue'),
        meta: {
          title: '库存管理',
          requiresAuth: true,
          requiresMerchant: true
        }
      },
      {
        path: 'orders',
        name: 'MerchantOrders',
        component: () => import('@/views/merchant/orders/index.vue'),
        meta: {
          title: '订单管理',
          requiresAuth: true,
          requiresMerchant: true
        }
      },
      {
        path: 'orders/process/:id',
        name: 'MerchantOrderProcess',
        component: () => import('@/views/merchant/orders/process.vue'),
        meta: {
          title: '订单处理',
          requiresAuth: true,
          requiresMerchant: true
        }
      },
      {
        path: 'orders/after-sales',
        name: 'MerchantOrderAfterSales',
        component: () => import('@/views/merchant/orders/after-sales.vue'),
        meta: {
          title: '售后处理',
          requiresAuth: true,
          requiresMerchant: true
        }
      },
      {
        path: 'shop',
        name: 'MerchantShop',
        component: () => import('@/views/merchant/shop/index.vue'),
        meta: {
          title: '店铺管理',
          requiresAuth: true,
          requiresMerchant: true
        }
      },
      {
        path: 'shop/info',
        name: 'MerchantShopInfo',
        component: () => import('@/views/merchant/shop/info.vue'),
        meta: {
          title: '店铺信息',
          requiresAuth: true,
          requiresMerchant: true
        }
      },
      {
        path: 'shop/decoration',
        name: 'MerchantShopDecoration',
        component: () => import('@/views/merchant/shop/decoration.vue'),
        meta: {
          title: '店铺装修',
          requiresAuth: true,
          requiresMerchant: true
        }
      },
      {
        path: 'marketing',
        name: 'MerchantMarketing',
        component: () => import('@/views/merchant/marketing/index.vue'),
        meta: {
          title: '营销工具',
          requiresAuth: true,
          requiresMerchant: true
        }
      },
      {
        path: 'analytics',
        name: 'MerchantAnalytics',
        component: () => import('@/views/merchant/analytics/index.vue'),
        meta: {
          title: '数据分析',
          requiresAuth: true,
          requiresMerchant: true
        }
      },
      {
        path: 'finance',
        name: 'MerchantFinance',
        component: () => import('@/views/merchant/finance/index.vue'),
        meta: {
          title: '财务管理',
          requiresAuth: true,
          requiresMerchant: true
        }
      },
      {
        path: 'settings',
        name: 'MerchantSettings',
        component: () => import('@/views/merchant/settings/index.vue'),
        meta: {
          title: '设置中心',
          requiresAuth: true,
          requiresMerchant: true
        }
      }
    ]
  },
  // 管理端登录页面
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: () => import('@/views/admin/auth/login.vue'),
    meta: {
      title: '管理员登录',
      hideLayout: true
    }
  },
  // 商家端登录页面
  {
    path: '/merchant/login',
    name: 'MerchantLogin',
    component: () => import('@/views/merchant/auth/login.vue'),
    meta: {
      title: '商家登录',
      hideLayout: true
    }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: {
      title: '页面不存在'
    }
  }
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior(to, from, savedPosition) {
    // 路由切换时的滚动行为
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

/**
 * 全局前置守卫
 * 处理路由权限验证和页面标题设置
 */
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 在线商城`
  }
  
  // 检查是否需要登录
  if (to.meta.requiresAuth) {
    if (!userStore.isLoggedIn) {
      ElMessage.warning('请先登录')
      // 根据路由类型重定向到对应的登录页面
      if (to.path.startsWith('/admin')) {
        next({
          path: '/admin/login',
          query: { redirect: to.fullPath }
        })
      } else if (to.path.startsWith('/merchant')) {
        next({
          path: '/merchant/login',
          query: { redirect: to.fullPath }
        })
      } else {
        next({
          path: '/auth/login',
          query: { redirect: to.fullPath }
        })
      }
      return
    }
  }
  
  // 检查管理员权限
  if (to.meta.requiresAdmin) {
    if (!userStore.isAdmin) {
      ElMessage.error('您没有管理员权限')
      next({ path: '/' })
      return
    }
  }
  
  // 检查商家权限
  if (to.meta.requiresMerchant) {
    if (!userStore.isMerchant) {
      ElMessage.error('您没有商家权限')
      next({ path: '/' })
      return
    }
  }
  
  // 已登录用户访问登录页面的处理
  if (userStore.isLoggedIn) {
    // 管理员访问管理员登录页面，重定向到管理员仪表板
    if (to.name === 'AdminLogin' && userStore.isAdmin) {
      next({ path: '/admin/dashboard' })
      return
    }
    // 商家访问商家登录页面，重定向到商家仪表板
    if (to.name === 'MerchantLogin' && userStore.isMerchant) {
      next({ path: '/merchant/dashboard' })
      return
    }
    // 普通用户访问用户登录/注册页面，重定向到首页
    if ((to.name === 'Login' || to.name === 'Register') && !userStore.isAdmin && !userStore.isMerchant) {
      next({ path: '/' })
      return
    }
  }
  
  next()
})

/**
 * 全局后置守卫
 * 处理路由切换后的逻辑
 */
router.afterEach((to, from) => {
  // 路由切换完成后的处理
  console.log(`路由切换: ${from.path} -> ${to.path}`)
})

export default router