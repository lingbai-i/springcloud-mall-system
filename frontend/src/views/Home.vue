<!--
 * @Author: lingbai
 * @Date: 2025-12-29
 * @Description: 百物语首页 - 融合传统美学与现代电商功能的响应式首页设计
 * @FilePath: /frontend/src/views/Home.vue
-->
<template>
  <div class="home-container">
    <!-- 主头部区域 -->
    <div class="home-main-content">
      <div class="main-header">
      <div class="container header-flex">
        <!-- Logo (Align with Category Sidebar) -->
        <div class="logo-area">
          <img src="/商标png.png" alt="百物语" class="brand-logo" />
        </div>

        <!-- 搜索框 (Align with Main Banner) -->
        <div class="search-area">
          <p class="brand-slogan">
            <img src="/主题/标语.png" alt="在万水千山中 领略独属于我们的国风之美" class="slogan-img" />
          </p>
          <div class="search-box-wrapper">
            <input 
              v-model="searchKeyword"
              placeholder="搜索商品、品牌..."
              class="custom-search-input"
              @keyup.enter="handleSearch"
            />
            <button class="search-btn" @click="handleSearch">搜索</button>
          </div>
          <div class="hot-keywords">
            <a v-for="keyword in hotKeywords" :key="keyword" @click="searchKeyword = keyword; handleSearch()">
              {{ keyword }}
            </a>
          </div>
        </div>

        <!-- Right Placeholder (Align with Right Sidebar) -->
        <div class="header-right-placeholder"></div>
      </div>
    </div>

    <!-- 导航与横幅区域 -->
    <div class="nav-banner-section">
      <div class="container flex-layout">
        <!-- 左侧分类菜单 -->
        <div class="category-sidebar">
          <div class="category-list">
            <div 
              v-for="category in categories" 
              :key="category.id"
              class="category-item"
              @mouseenter="showPopover(category, $event)"
              @mouseleave="hidePopover"
              @click="goToCategory(category.id)"
            >
              <div class="cat-content">
                <el-icon v-if="!category.isImageIcon" class="cat-icon"><component :is="category.icon" /></el-icon>
                <img v-else :src="category.icon" class="cat-image-icon" />
                <span class="cat-name">{{ category.name }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 中间轮播图与快捷栏 -->
        <div class="main-column">
          <transition name="fade">
            <div 
              class="sub-category-popover" 
              v-if="activeCategory"
              v-show="popoverVisible"
              :style="popoverStyle"
              @mouseenter="onPopoverMouseEnter"
              @mouseleave="onPopoverMouseLeave"
            >
              <!-- 独立背景装饰层：确保拉伸效果完全覆盖动态高度 -->
              <div class="popover-bg-wrapper"></div>
              
              <div class="popover-content-area">
                <div 
                  v-for="group in activeCategory.children" 
                  :key="group.id" 
                  class="sub-cat-group"
                >
                  <div class="sub-cat-title">
                    {{ group.name }}
                    <el-icon><ArrowRight /></el-icon>
                  </div>
                  <div class="sub-cat-links">
                    <a 
                      v-for="item in group.subItems" 
                      :key="item" 
                      @click.stop="goToCategory(group.id)"
                    >
                      {{ item }}
                    </a>
                    <!-- 兼容旧数据格式 -->
                    <a 
                      v-if="!group.subItems && group.name" 
                      @click.stop="goToCategory(group.id)"
                    >
                      {{ group.name }}
                    </a>
                  </div>
                </div>
              </div>
            </div>
          </transition>

          <div class="main-banner">
            <HomeBannerCarousel 
              height="350px"
              :autoplay-interval="5000"
              @click="handleBannerCarouselClick"
              class="custom-carousel"
            />
          </div>
          

        </div>

        <!-- 右侧用户/快捷区 -->
        <div class="right-sidebar guofeng-card">
          <div class="user-panel-new" v-if="isLoggedIn">
            <div class="user-top-info">
              <div class="avatar-wrapper">
                <img :src="userInfo.avatar || defaultAvatar" class="user-avatar-main" />
              </div>
              <div class="user-meta">
                <div class="user-nickname-main">{{ displayUserName }}</div>
                <div class="user-quick-links">
                  <a @click="goToProfile">关注店铺</a>
                  <span class="link-sep">|</span>
                  <a @click="goToProfile">收货地址</a>
                </div>
              </div>
            </div>
            <div class="user-stats-grid">
              <div class="stat-item" @click="goToCart">
                <div class="stat-value">{{ cartCount }}</div>
                <div class="stat-label">购物车</div>
              </div>
              <div class="stat-item" @click="goToOrders">
                <div class="stat-value">0</div>
                <div class="stat-label">待收货</div>
              </div>
              <div class="stat-item" @click="goToOrders">
                <div class="stat-value">0</div>
                <div class="stat-label">待发货</div>
              </div>
              <div class="stat-item" @click="goToOrders">
                <div class="stat-value">0</div>
                <div class="stat-label">待付款</div>
              </div>
              <div class="stat-item" @click="goToOrders">
                <div class="stat-value">0</div>
                <div class="stat-label">待评价</div>
              </div>
            </div>
          </div>
          <div class="user-panel guest" v-else>
            <div class="user-top-info">
              <el-avatar :size="48" :src="defaultAvatar" class="user-avatar-main">
                <el-icon><User /></el-icon>
              </el-avatar>
              <div class="user-meta">
                <div class="user-nickname-main">晚上好</div>
              </div>
            </div>
            
            <div class="guest-welcome-msg">
              <h3>登录百物语后更多精彩</h3>
              <p>嘿！更懂你的推荐，更便捷的搜索</p>
            </div>

            <div class="user-actions">
              <button class="action-btn login-full" @click="handleLogin">立即登录</button>
            </div>

            <div class="user-stats-grid guest-footer">
              <div class="stat-item">
                <div class="stat-value"><el-icon><Star /></el-icon></div>
                <div class="stat-label">宝贝收藏</div>
              </div>
              <div class="stat-item">
                <div class="stat-value"><el-icon><Shop /></el-icon></div>
                <div class="stat-label">买过的店</div>
              </div>
              <div class="stat-item">
                <div class="stat-value"><el-icon><Collection /></el-icon></div>
                <div class="stat-label">收藏的店</div>
              </div>
              <div class="stat-item">
                <div class="stat-value"><el-icon><Clock /></el-icon></div>
                <div class="stat-label">我的足迹</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 主要内容区 -->
    <div class="main-content">
      <div class="container">
        <!-- 推荐商品 -->
        <div class="recommend-section">
          <div class="section-header-styled">
            <div class="recommend-tab-main">
              <div class="recommend-label-tag">
                <img src="/主题/猜你喜欢.png" alt="猜你喜欢" class="label-icon" />
              </div>
              
              <div class="nav-arrow-btn prev" @click="scrollRecommendNav('prev')">
                <img src="/主题/向右.png" alt="向左" class="nav-icon mirror" />
              </div>
              
              <div class="recommend-nav-wrapper">
                <div class="nav-scroll-area">
                  <div 
                    v-for="cat in recommendCategories" 
                    :key="cat.id"
                    class="nav-item"
                    :class="{ active: activeRecommendCat === cat.id }"
                    @click="activeRecommendCat = cat.id"
                  >
                    {{ cat.name }}
                  </div>
                </div>
              </div>

              <div class="nav-arrow-btn next" @click="scrollRecommendNav('next')">
                <img src="/主题/向右.png" alt="向右" class="nav-icon" />
              </div>
            </div>

            <div class="recommend-refresh-btn" @click="refreshRecommend">
              <img src="/主题/刷新.png" alt="刷新" class="refresh-icon" :class="{ rotating: isRefreshing }" />
            </div>
          </div>
          
          <div class="product-grid" :style="{ height: totalGridHeight + 'px', position: 'relative' }">
            <div 
              v-for="product in visibleProducts" 
              :key="product.id"
              class="product-card"
              :style="product.style"
              @click="goToProduct(product.id)"
            >
              <div class="product-image-box">
                <img 
                  :src="product.image" 
                  :alt="product.name" 
                  loading="lazy" 
                  @error="handleImageError"
                />
              </div>
              <div class="product-info">
                <div class="product-title-row">
                  <h3 class="product-name" :title="product.name">{{ product.name }}</h3>
                </div>
                <div class="product-price-row">
                  <div class="price-wrapper">
                    <span class="currency">¥</span>
                    <span class="price-val">{{ product.price }}</span>
                  </div>
                  <span class="sales-count">{{ product.sales > 1000 ? (product.sales/1000).toFixed(1) + '万' : product.sales }}+人购买</span>
                </div>
              </div>
            </div>
            
             <!-- 空数据提示 -->
            <div v-if="recommendProducts.length === 0 && !isLoading" class="empty-products">
              <el-icon :size="40" color="#999"><Box /></el-icon>
              <p>暂无推荐商品</p>
            </div>
          </div>

          <!-- 加载状态与滚动锚点 -->
          <div class="scroll-footer">
            <div v-if="isLoading" class="loading-status">
              <el-icon class="is-loading"><Loading /></el-icon>
              <span>正在加载更多精选商品...</span>
            </div>
            
            <div v-if="isError" class="error-status">
              <span>加载失败，请检查网络</span>
              <el-button type="primary" size="small" @click="loadMoreProducts()" plain>重试</el-button>
            </div>
            
            <div v-if="!hasMore && recommendProducts.length > 0" class="no-more-status">
              <span class="line"></span>
              <span class="text">已加载全部内容</span>
              <span class="line"></span>
            </div>

            <!-- 滚动监听锚点 -->
            <div class="scroll-sentinel"></div>
          </div>
        </div>
      </div>
    </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { 
  ArrowDown, 
  ArrowRight, 
  ArrowLeft,
  Refresh,
  User, 
  Box,
  Loading,
  Monitor,
  Operation,
  Iphone,
  House,
  Trophy,
  Opportunity,
  Clock,
  Ticket,
  Food,
  Goods,
  List,
  SwitchButton
} from '@element-plus/icons-vue'
import * as logger from '@/utils/logger'
import HomeBannerCarousel from '@/components/HomeBannerCarousel.vue'
import { getRecommendProducts } from '@/api/product'

const router = useRouter()
const userStore = useUserStore()

// State
const searchKeyword = ref('')
const isLoggedIn = computed(() => userStore.isLoggedIn)
const cartCount = ref(0) // 首页右侧面板显示用，实际数量由导航栏显示

// 二级分类弹出层状态
const activeCategory = ref(null)
const isHoveringPopover = ref(false)
const isHoveringSidebar = ref(false)
const popoverVisible = ref(false)
let hoverTimer = null

const popoverStyle = reactive({
  top: '0px',
  left: '0px',
  width: '100%', // 宽度撑满 main-column
  height: 'auto', // 高度根据内容自动调整
  minHeight: '460px', // 最小高度与轮播图一致
  maxHeight: '800px' // 设置最大高度限制
})

/**
 * 显示二级分类弹出层
 * @param {Object} category 分类数据
 * @param {Event} event 鼠标事件
 */
const showPopover = (category, event) => {
  // 清除之前的定时器
  if (hoverTimer) clearTimeout(hoverTimer)
  
  isHoveringSidebar.value = true
  
  // 如果当前已经有显示的分类，则切换时可以稍微快一点
  const delay = popoverVisible.value ? 100 : 300
  
  hoverTimer = setTimeout(() => {
    activeCategory.value = category
    popoverVisible.value = true
    
    // 逻辑简化：不再需要复杂的 getBoundingClientRect 计算，
    // 因为我们改用了 absolute 布局，相对于 .flex-layout 定位
  }, delay)
}

/**
 * 隐藏二级分类弹出层
 */
const hidePopover = () => {
  isHoveringSidebar.value = false
  
  if (hoverTimer) clearTimeout(hoverTimer)
  
  hoverTimer = setTimeout(() => {
    // 只有当鼠标既不在侧边栏也不在弹出层时，才隐藏
    if (!isHoveringSidebar.value && !isHoveringPopover.value) {
      popoverVisible.value = false
      // 动画结束后清除数据
      setTimeout(() => {
        if (!popoverVisible.value) {
          activeCategory.value = null
        }
      }, 200) // 对应 CSS 过渡时间
    }
  }, 300) // 延迟隐藏，给用户移动鼠标的时间
}

/**
 * 鼠标进入弹出层
 */
const onPopoverMouseEnter = () => {
  isHoveringPopover.value = true
  if (hoverTimer) clearTimeout(hoverTimer)
}

/**
 * 跳转到卖家登录页面
 */
const goToSellerLogin = () => {
  router.push('/merchant/login')
}

/**
 * 跳转到商家入驻页面
 */
const goToSellerRegister = () => {
  router.push('/merchant/register')
}

/**
 * 鼠标离开弹出层
 */
const onPopoverMouseLeave = () => {
  isHoveringPopover.value = false
  hidePopover()
}

const displayUserName = computed(() => {
  const info = userStore.userInfo
  return info?.nickname || info?.username || '用户'
})

const userInfo = computed(() => userStore.userInfo)
const defaultAvatar = '/images/default-avatar.png'

const hotKeywords = ref(['汉服', '茶具', '文房四宝', '刺绣', '古琴'])

const categories = ref([
  {
    id: 1, name: '电脑 / 配件 / 办公 / 文具', icon: 'Monitor',
    children: [
      { id: 101, name: '电脑整机', subItems: ['笔记本电脑', '游戏本', '平板电脑', 'DIY电脑', '服务器/工作站', '一体机', '闺蜜机'] },
      { id: 102, name: '电脑配件', subItems: ['拒绝腱鞘炎电脑手托', '显示器', 'CPU', '主板', '显卡', '硬盘', '内存', '机箱', '电源', '散热器', '显示器支架', '光驱', '声卡', '装机配件', 'SSD固态硬盘', '组装电脑', 'USB分线器', '主板CPU'] },
      { id: 103, name: '外设产品', subItems: ['鼠标', '键盘', '网络仪表仪器', 'U盘', '移动硬盘', '摄像头', '手写板', 'UPS电源', '平板电脑配件', '笔记本配件', '投屏器', '扩展坞'] },
      { id: 104, name: '游戏设备', subItems: ['游戏机', '游戏耳机', '手柄/方向盘', '游戏软件', '游戏周边', '游戏手办'] },
      { id: 105, name: '网络产品', subItems: ['路由器', '网络机顶盒', '交换机', '网络存储', '网卡', '5G/4G上网', '网线', '网络配件'] },
      { id: 106, name: '办公设备', subItems: ['投影机', '打印机', '传真设备', '碎纸机', '考勤门禁', '收银机', '保险柜', '安防监控'] },
      { id: 107, name: '办公耗材', subItems: ['墨粉', '墨盒', '色带', '纸类', '刻录光盘', '电脑打印纸', '条码标签纸', '收银纸', '卡纸'] },
      { id: 108, name: '财务办公', subItems: ['订书机', '票夹', '大头针', '美工刀', '胶带', '复写纸', '号码机', '印泥', '账本', '计算器'] },
      { id: 109, name: '书写工具', subItems: ['钢笔', '记号笔', '荧光笔', '油漆笔', '秀丽笔', '圆珠笔', '中性笔', '笔芯', '铅笔'] },
      { id: 110, name: '本册纸签', subItems: ['记事本', '日记本', '作业本', '教学用本', '手帐', '纪念册', '索引纸', '文稿纸', '相册', '明信片'] },
      { id: 111, name: '学习用品', subItems: ['卷笔刀', '练字帖', '拼音机口算机', '橡皮擦', '修正带', '阅读架', '文具盒', '地球仪'] },
      { id: 112, name: '绘画书法', subItems: ['画纸', '颜料画笔', '勾线笔', '蜡笔', '水彩笔', '画架', '文房套装', '砚台', '毛笔字帖', '宣纸'] }
    ]
  },
  {
    id: 2, name: '工业品 / 商业 / 农业 / 定制', icon: 'Operation',
    children: [
      { id: 201, name: '零件耗材', subItems: ['螺丝', '螺母', '铆钉', '钻头', '铣刀', '丝锥', '密封圈', '轴承', '链条', '气缸', '软管', '气管', '弹簧', '滑轮', '管卡'] },
      { id: 202, name: '工控安防', subItems: ['变压器', '变频器', '断路器', '逆变器', '蓄电池', '发电机', '计数器', '定时器', '隔离网', '防护栏', '灭火器', '反光背心', '安全帽', '警戒带', '减速带'] },
      { id: 203, name: '实验化学', subItems: ['工业润滑油', '润滑脂', '切削液', '防锈剂', '化学试剂', '实验室试剂', '实验室器材', '实验室设备'] },
      { id: 204, name: '金属橡塑', subItems: ['铝型材', '不锈钢板', '钢管', '钢筋', '铁板', '铝板', '铝管', '彩钢瓦', 'POM板', '塑料板', '尼龙板', '橡胶垫', '橡胶气囊', '橡胶管', '石墨'] },
      { id: 205, name: '纺织面料', subItems: ['梭织面料', '纺织机械', '面料/布类', '土工布', '牛仔', '纱线', '面料样衣', '纺织原料'] },
      { id: 206, name: '包装印刷', subItems: ['塑料袋', '纸箱', '自封袋', '气泡膜', '快递袋', '搬家箱', '包装胶带', '编织袋', '缠绕膜', '泡沫箱'] },
      { id: 207, name: '商业家具', subItems: ['货架', '展柜', '广告牌', '易拉宝', '收银台', '灯箱', '服装展示架', '更衣柜', '酒店桌椅', '酒吧椅', '平板手推车', '超市货架', '摆摊车', '卡座', '火锅桌', '冷藏展示柜', '洗头床', '理发椅', '理发镜台', '寄存柜', '家具支架', '五金货架'] },
      { id: 208, name: '商业设备', subItems: ['商用洗碗机', '商用炉灶', '商用油烟机', '家禽脱毛机', '商用冷藏柜', '商用扫地机', '商用洗车机', '商用冷水机'] },
      { id: 209, name: '市政景观', subItems: ['雕塑', '花箱', '岗亭', '消防柜', '实验台', '防爆柜', '排椅', '坐便椅', '标志标识', '移动厕所', '工具房', '课桌椅', '旗杆', '讲台'] },
      { id: 210, name: '商务服务', subItems: ['工商注册', '代理记账', '短视频动画', '平面设计', '3D绘图', 'logo设计', '知识产权', 'PPT制作', '标书制作'] },
      { id: 211, name: '农用物资', subItems: ['农药', '肥料', '种子种苗', '驱鸟剂', '助剂/添加剂', '园林养护', '授粉蜂', '性诱剂/信息素', '卫生农药', '农技衣服'] },
      { id: 212, name: '畜牧养殖', subItems: ['饲料', '兽药', '养殖设备', '蚊香棒/蝇香', '养蜂物资', '水产养殖', '保温灯', '动物种苗'] },
      { id: 213, name: '农机百货', subItems: ['微耕机', '农膜篷布', '温室大棚', '刀/剪/锯', '喷雾器/打药机', '农用百货', '喷灌/滴灌', '农产品加工', '农业机械', '智慧农业'] },
      { id: 214, name: '商务定制', subItems: ['包装定制', '奖杯奖牌', '不干胶定制', '印章定制', '工牌定制', '工装定制', '证书奖状'] },
      { id: 215, name: '个人定制', subItems: ['照片冲印', '海报印刷', '饰品定制', '贺卡/卡贴', '手机壳定制', '西服定制'] }
    ]
  },
  {
    id: 3, name: '家电 / 手机 / 通信 / 数码', icon: 'Iphone',
    children: [
      { id: 301, name: '电视', subItems: ['4k超高清', '智能电视', '会议电视', '全面屏电视', '电视音响套装', '100寸电视', '电视挂架'] },
      { id: 302, name: '空调', subItems: ['挂式空调', '柜式空调', '中央空调', '新风空调', '移动空调', '挂机1.5匹', '柜机3匹'] },
      { id: 303, name: '洗衣机', subItems: ['滚筒洗衣机', '洗烘一体机', '波轮洗衣机', '洗烘套装', '迷你洗衣机', '洗衣机配件', '烘干机'] },
      { id: 304, name: '冰箱', subItems: ['多门', '对开门', '十字对开门', '双门', '冰洗套装', '冷柜/冰吧', '酒柜', '冰箱配件', '风冷无霜'] },
      { id: 305, name: '厨电', subItems: ['油烟机', '灶具', '集成灶', '集成水槽', '消毒柜', '洗碗机', '电热水器', '壁挂炉', '燃气热水器', '空气能热水器', '太阳能热水器', '破壁机', '咖啡机', '榨汁机', '烤箱', '厨师机', '电热水壶', '面包机', '空气炸锅', '养生壶', '电磁炉', '电饭煲', '电压力锅', '微波炉'] },
      { id: 306, name: '手机', subItems: ['iPhone6=CCD相机平替', '新品手机', '手机维修', 'AI手机', '5G手机', '游戏手机', '学习手机', '对讲机'] },
      { id: 307, name: '运营商', subItems: ['合约机', '手机卡', '宽带', '充话费/流量', '中国电信', '中国移动', '中国联通'] },
      { id: 308, name: '手机配件', subItems: ['手机壳', '贴膜', '手机存储卡', '数据线', '充电器', '创意配件', '手机饰品', '手机支架'] },
      { id: 309, name: '环境电器', subItems: ['取暖器', '空气净化器', '加湿器', '净水器', '饮水机', '除湿机', '电风扇', '冷风扇'] },
      { id: 310, name: '个护健康', subItems: ['剃须刀', '电动牙刷', '冲牙器', '电吹风', '卷/直发器', '理发器', '美容仪'] },
      { id: 311, name: '清洁电器', subItems: ['吸尘器', '扫地机器人', '擦窗机器人', '蒸汽/电动拖把', '除螨仪', '洗地机'] },
      { id: 312, name: '视听影音', subItems: ['家庭影院', 'KTV音响', '迷你音响', 'DVD功放', '回音壁', '麦克风'] },
      { id: 313, name: '摄影摄像', subItems: ['大疆pocket 3口袋云台相机', '数码相机', '微单相机', '单反相机', '拍立得', '运动相机', '胶卷相机', '摄像机', '镜头'] },
      { id: 314, name: '影音娱乐', subItems: ['无线耳机', '音箱', '收音机', '麦克风', '音频线', '有线耳机', '直播设备', '头戴式耳机'] },
      { id: 315, name: '智能设备', subItems: ['智能手表', '智能手环', '监控摄像', 'XR设备', '智能家居', '健康监测', '无人机'] },
      { id: 316, name: '电子教育', subItems: ['学习机', '点读机/笔', '早教益智', '录音笔', '电纸书', '电子词典', '复读机', '翻译机'] },
      { id: 317, name: '家电配件', subItems: ['电视配件', '空调配件', '洗衣机配件', '冰箱配件', '厨卫大电配件', '厨房小电配件'] },
      { id: 318, name: '家电服务', subItems: ['空调清洗', '家电维修', '家电清洗'] }
    ]
  },
  {
    id: 4, name: '家具 / 家装 / 家居 / 厨具', icon: 'House',
    children: [
      { id: 401, name: '全屋家具', subItems: ['小卧室飘窗拼接床', '极有家', '床', '床垫', '沙发', '茶几', '电视柜', '休闲椅', '书架', '鞋柜', '餐桌', '餐椅', '餐边柜', '酒柜', '厨房岛台', '床头柜', '五斗柜', '衣柜', '梳妆台', '穿衣镜'] },
      { id: 402, name: '儿童家具', subItems: ['儿童床', '儿童桌椅', '儿童衣柜', '学习桌', '玩具收纳', '儿童书架'] },
      { id: 403, name: '办公家具', subItems: ['办公桌', '办公椅', '办公沙发', '办公柜类', '人体工学椅', '办公升降桌', '静音仓', '大班台', '会议桌', '会议椅', '培训桌', '屏风隔断', '接待台', '演讲台', '大板桌', '报刊架', '密集柜', '零件柜'] },
      { id: 404, name: '全屋定制', subItems: ['定制衣柜', '榻榻米', '橱柜', '门', '地暖', '室内门', '防盗门窗', '淋浴房', '壁挂炉', '散热器'] },
      { id: 405, name: '建筑材料', subItems: ['油漆涂料', '刷刷辅料', '瓷砖', '地板', '壁纸', '强化地板', '美缝剂', '防水涂料', '木材/板材'] },
      { id: 406, name: '厨房卫浴', subItems: ['水槽', '龙头', '淋浴花洒', '马桶', '智能马桶', '智能马桶盖', '厨卫挂件', '浴室柜', '浴霸'] },
      { id: 407, name: '五金电工', subItems: ['指纹锁', '电动工具', '手动工具', '测量工具', '工具箱', '开关插座', '配电箱', '机械锁', '拉手'] },
      { id: 408, name: '装修设计', subItems: ['全包装修', '半包装修', '家装设计', '局部装修', '安装服务', '装修公司', '旧房翻新'] },
      { id: 409, name: '全屋灯具', subItems: ['吸顶灯', '吊灯', '台灯', '筒灯', '庭院灯', '装饰灯', 'LED灯', '氛围照明', '落地灯', '手电'] },
      { id: 410, name: '床上用品', subItems: ['床品四件套', '婚庆用品', '被子', '枕头', '毛毯', '电热毯', '凉席', '蚊帐'] },
      { id: 411, name: '居家布艺', subItems: ['睡在春天上泡泡纱被套', '窗帘窗纱', '门帘', '桌布', '抱枕靠垫', '沙发垫', '飘窗垫', '地毯', '地垫', '蒲团'] },
      { id: 412, name: '生活日用', subItems: ['衣架', '化妆镜', '洗漱杯', '香薰', '火机烟具', '马桶刷', '净化除味', '雨伞雨具'] },
      { id: 413, name: '收纳用品', subItems: ['真空收纳袋', '化妆品收纳', '内衣收纳盒', '手办收纳', '洞洞板', '鞋子收纳', '收纳柜'] },
      { id: 414, name: '家居饰品', subItems: ['现代装饰画', '油画', '桌面摆件', '落地摆件', '电表箱装饰画', '钟饰壁饰', '节庆用品'] },
      { id: 415, name: '厨房厨具', subItems: ['酒具', '锅具', '炒锅', '碗碟', '刀剪菜板', '茶具', '咖啡具', '置物架'] },
      { id: 416, name: '厨房储物', subItems: ['调味品罐', '油壶', '密封罐', '米桶', '料理盒', '泡酒瓶'] },
      { id: 417, name: '烧烤用具', subItems: ['烧烤架', '烧烤炉', '烧烤刷', '烧烤网', '烧烤夹', '烧烤盘', '锡纸', '烧烤炭'] },
      { id: 418, name: '餐饮用具', subItems: ['餐具', '水杯', '水壶', '酒具', '保鲜容器', '一次性餐桌用品'] }
    ]
  },
  {
    id: 5, name: '女装 / 男装 / 内衣 / 配饰', icon: '/icons/nanzhuang.png', isImageIcon: true,
    children: [
      { id: 501, name: '流行女装', subItems: ['当季热卖', '新品推荐', '商场同款', '连衣裙', 'T恤', '衬衫', '外套', '针织衫', '风衣', '西服', '卫衣', '马甲', '大衣', '皮衣/皮草', '毛衣', '羽绒服', '休闲裤', '牛仔裤', '短裤', '直筒裤', '工装裤', '西裤', '运动裤', '半身裙'] },
      { id: 502, name: '流行男装', subItems: ['当季热卖', '衬衫', '卫衣', 'POLO衫', 'T恤', '针织衫', '毛衣', '羊绒衫', '休闲裤', '牛仔裤', '西裤', '外套', '风衣', '大衣', '牛仔外套', '棉衣', '羽绒服', '西装', '皮衣', '马甲', '棒球服'] },
      { id: 503, name: '女士内衣', subItems: ['女士内裤', '无钢圈', '聚拢', '打底背心', '文胸套装', '塑身内衣', '家居服', '丝袜裤袜'] },
      { id: 504, name: '男士内衣', subItems: ['男士内裤', '男袜', '男士背心', '男士家居服', '平角裤', '组合内裤'] },
      { id: 505, name: '服饰配件', subItems: ['渔夫帽', '棒球帽', '贝雷帽', '毛线帽', '礼帽', '遮阳帽', '雷锋帽', '女士腰带', '男士腰带', '裤链', '丝巾', '围巾', '手套', '耳罩', '防晒袖'] },
      { id: 506, name: '眼镜专区', subItems: ['太阳眼镜', '眼镜框', '防蓝光', '偏光', '蛤蟆镜'] },
      { id: 507, name: '饰品专区', subItems: ['项链', '手饰脚饰', '耳饰', '戒指', '发饰', '胸针', '打火机', '领带', '袖扣'] },
      { id: 508, name: '钟表专区', subItems: ['DW', '天梭', '浪琴', '欧米茄', '萧邦', '卡西欧', '西铁城', '天王', '阿玛尼', '国表', '日韩表', '儿童手表', '闹钟', '挂钟', '座钟', '钟表配件', '钟表维修'] }
    ]
  },
  {
    id: 6, name: '女鞋 / 男鞋 / 运动 / 户外', icon: '/icons/nvxie.png', isImageIcon: true,
    children: [
      { id: 601, name: '女休闲鞋', subItems: ['新品推荐', '老爹鞋', '内增高鞋', '帆布鞋', '小白鞋', '板鞋', '网面鞋', '椰子鞋', '高帮鞋'] },
      { id: 602, name: '女单鞋', subItems: ['深口单鞋', '尖头鞋', '浅口单鞋', '乐福鞋', '女高跟鞋', '玛丽珍鞋', '平底鞋', '粗跟鞋'] },
      { id: 603, name: '时尚女靴', subItems: ['马丁靴', '切尔西/烟筒靴', '雪地靴', '高筒靴', '袜靴/弹力靴', '过膝靴', '休闲靴', '短筒靴', '雨靴', '裸靴'] },
      { id: 604, name: '男休闲鞋', subItems: ['新品推荐', '休闲皮鞋', '豆豆鞋', '椰子鞋', '网面鞋', '高帮鞋', '小白鞋', '老爹鞋', '帆布鞋'] },
      { id: 605, name: '时尚男靴', subItems: ['男马丁靴', '男雪地靴', '男工装靴', '男商务靴', '男切尔西靴'] },
      { id: 606, name: '男商务鞋', subItems: ['正装鞋', '套脚商务鞋', '系带商务鞋', '休闲皮鞋', '英伦鞋', '布洛克鞋', '德比鞋', '冲孔鞋'] },
      { id: 607, name: '运动鞋包', subItems: ['健身包都智能化了', '跑步鞋', '休闲鞋', '篮球鞋', '运动包', '足球鞋', '专项运动鞋', '训练鞋'] },
      { id: 608, name: '运动服饰', subItems: ['T恤', '速干衣', '瑜伽裤', '运动裤', '运动套装', '夹克/风衣', '卫衣/套头衫', '运动背心', '健身服', '运动内衣'] },
      { id: 609, name: '健身训练', subItems: ['跑步机', '动感单车', '健身车', '椭圆机', '划船机', '甩脂机', '倒立机', '踏步机', '哑铃', '收腹机', '瑜伽用品', '舞蹈用品'] },
      { id: 610, name: '体育用品', subItems: ['乒乓球', '羽毛球', '篮球', '足球', '轮滑滑板', '网球', '高尔夫', '台球', '排球', '田径鞋'] },
      { id: 611, name: '游泳用品', subItems: ['女士泳衣', '比基尼', '男士泳衣', '泳镜', '游泳圈', '游泳包防水包', '泳帽', '游泳配件'] },
      { id: 612, name: '户外服装', subItems: ['冲锋衣', '冲锋裤', '速干衣', '速干裤', '一次性内衣裤', '防晒衣', '防晒裤'] },
      { id: 613, name: '户外装备', subItems: ['背包', '帐篷/垫子', '望远镜', '烧烤用具', '便携桌椅床', '户外配饰', '军迷用品', '野餐用品', '睡袋/吊床', '户外照明', '户外工具', '登山攀岩', '极限户外', '冲浪潜水', '滑雪装备'] },
      { id: 614, name: '垂钓用品', subItems: ['钓竿', '鱼线', '浮漂', '鱼饵', '钓鱼配件', '渔具包', '钓箱钓椅', '鱼线轮', '钓鱼灯', '辅助装备'] }
    ]
  },
  {
    id: 7, name: '汽车 / 珠宝 / 文玩 / 箱包', icon: '/icons/zhubaoshipin.png', isImageIcon: true,
    children: [
      { id: 701, name: '汽车装饰', subItems: ['座垫座套', '脚垫', '头枕腰靠', '安全座椅', '方向盘套', '后备箱垫', '车载支架', '车钥匙扣', '炭包/净化剂', '遮阳/雪挡', '车衣', '贴膜', '汽车窗帘', '导航/中控膜', '漆面保护膜', '汽车脚垫'] },
      { id: 702, name: '汽车配件', subItems: ['轮胎', '机油', '雨刮器', '车灯', '火花塞', '滤芯器', '刹车盘', '轮毂', '车顶架', '保险杠'] },
      { id: 703, name: '电动车', subItems: ['电动自行车', '电动摩托车', '平衡车', '电动滑板车', '电动三轮车', '老年代步车', '电动四轮车', '电助力车', '电动车头盔', '电动车挡风被'] },
      { id: 704, name: '摩托车', subItems: ['摩托车', '摩托车养护', '摩托车护具', '摩托车骑行服', '摩托车头盔', '摩托车骑行鞋', '摩托车手套', '摩托车风镜', '头盔耳机'] },
      { id: 705, name: '车载电器', subItems: ['行车记录仪', '智能后视镜', '车载充电器', '车机导航', '车载蓝牙', '智能驾驶', '对讲电台', '倒车雷达', '导航仪', '安全预警仪', '车载净化器', '车载吸尘器', '洗车机', '新能源充电桩'] },
      { id: 706, name: '汽车拍卖', subItems: ['新车特卖', '二手车', '低价起拍', '法拍车', '公车处置', '新能源车', 'SUV', 'MPV', '货卡'] },
      { id: 707, name: '珠宝首饰', subItems: ['黄金项链/吊坠', '黄金手饰', '黄金戒指', 'K金项链', '铂金首饰', '钻石戒指', '玉手镯', '玉吊坠', '彩宝首饰', '珍珠项链'] },
      { id: 708, name: '古董文玩', subItems: ['钱币', '邮品', '书画', '器具', '玉石', '手工艺', '紫砂', '古玩杂项'] },
      { id: 709, name: '潮流女包', subItems: ['单肩包', '手提包', '斜挎包', '双肩包', '钱包', '手拿包', '卡包/零钱包', '钥匙包'] },
      { id: 710, name: '精品男包', subItems: ['男士钱包', '双肩包', '单肩/斜挎包', '商务公文包', '男士手包', '卡包名片夹', '钥匙包'] },
      { id: 711, name: '功能箱包', subItems: ['行李箱', '拉杆包', '旅行包', '电脑包', '休闲运动包', '书包', '登山包', '腰包/胸包', '旅行配件'] },
      { id: 712, name: '奢侈品', subItems: ['LV/路易威登', 'Chanel/香奈儿', 'PRADA', 'GUCCI', 'COACH', 'BALLY', 'YSL/圣罗兰'] }
    ]
  },
  {
    id: 8, name: '食品 / 生鲜 / 酒类 / 健康', icon: 'Food',
    children: [
      { id: 801, name: '粮油调味', subItems: ['大米', '食用油', '面', '杂粮', '调味品', '南北干货', '方便食品', '烘焙原料', '有机食品'] },
      { id: 802, name: '休闲食品', subItems: ['够麻够辣钵钵鸡拌面', '饼干蛋糕', '糖/巧克力', '方便食品', '肉干肉脯', '营养零食', '休闲零食', '坚果炒货', '蜜饯果干'] },
      { id: 803, name: '饮料冲调', subItems: ['牛奶', '茶', '饮料', '酸奶', '饮用水', '咖啡', '蜂蜜/蜂产品', '冲饮谷物', '成人奶粉'] },
      { id: 804, name: '新鲜水果', subItems: ['热带“甜蜜霸主”金煌芒', '苹果', '橙子', '奇异果/猕猴桃', '火龙果', '榴莲', '芒果', '椰子', '车厘子', '百香果', '柚子', '国产水果', '进口水果'] },
      { id: 805, name: '蔬菜蛋品', subItems: ['蛋品', '叶菜类', '根茎类', '葱姜蒜椒', '鲜菌菇', '半加工豆制品', '玉米', '山药', '地瓜/红薯'] },
      { id: 806, name: '精选肉类', subItems: ['猪肉', '牛肉', '羊肉', '鸡肉', '鸭肉', '冷鲜肉', '内脏类', '冷藏熟食', '牛排', '牛腩', '鸡翅'] },
      { id: 807, name: '海鲜水产', subItems: ['鱼类', '虾类', '蟹类', '贝类', '海参', '鱿鱼/章鱼', '活鲜', '三文鱼', '大闸蟹', '小龙虾', '海产干货'] },
      { id: 808, name: '冷饮冻食', subItems: ['水饺/馄饨', '汤圆/元宵', '面点', '奶酪/黄油', '方便速食', '火锅丸串', '冰淇淋', '低温奶', '生日蛋糕'] },
      { id: 809, name: '中外名酒', subItems: ['白酒', '葡萄酒', '洋酒', '啤酒', '黄酒/养生酒', '收藏酒/陈年老酒', '果酒'] },
      { id: 810, name: '中西药品', subItems: ['感冒咳嗽', '补肾壮阳', '补气养血', '止痛镇痛', '耳鼻喉用药', '眼科用药', '口腔用药', '皮肤用药', '肠胃消化', '风湿骨外伤', '维生素/钙', '心脑血管', '避孕药', '肝胆用药'] },
      { id: 811, name: '营养健康', subItems: ['增强免疫', '骨骼健康', '补肾强身', '肠胃养护', '调节三高', '缓解疲劳', '养肝护肝', '改善贫血', '清咽利喉', '美容养颜', '改善睡眠', '明目益智'] },
      { id: 812, name: '保健器械', subItems: ['血压计', '血糖仪', '心电仪', '体温计', '胎心仪', '制氧机', '呼吸机', '雾化器', '助听器', '轮椅', '拐杖', '养生器械', '理疗仪', '智能健康'] }
    ]
  },
  {
    id: 9, name: '母婴 / 童装 / 玩具 / 宠物', icon: 'Goods',
    children: [
      { id: 901, name: '婴儿奶粉', subItems: ['1段', '2段', '3段', '4段', '孕妈奶粉', '特殊配方奶粉', '有机奶粉'] },
      { id: 902, name: '尿裤湿巾', subItems: ['纸尿裤', '拉拉裤', '成人尿裤', '婴儿湿巾'] },
      { id: 903, name: '喂养用品', subItems: ['奶瓶奶嘴', '吸奶器', '暖奶消毒', '辅食料理机', '牙胶安抚', '儿童餐具', '水壶/水杯', '围兜'] },
      { id: 904, name: '宝宝辅食', subItems: ['米粉/米糊', '菜粉/水果粉', '果泥', '面条', '磨牙棒', '宝宝米/宝宝粥', '肉松/鱼松'] },
      { id: 905, name: '孕产妇用品', subItems: ['孕妇装', '孕妇裤', '孕妇文胸', '孕妇内裤', '防辐射服', '孕妇背带', '孕妇袜', '孕妇帽', '妈咪包', '孕妇彩妆'] },
      { id: 906, name: '孕产妇营养品', subItems: ['叶酸', 'DHA', '维生素', '钙铁锌', '牛初乳', '益生菌', '鱼肝油', '辅酶Q10', '男士叶酸'] },
      { id: 907, name: '哺乳用品', subItems: ['哺乳衣', '哺乳文胸', '防溢乳垫', '母乳储存袋', '哺乳枕'] },
      { id: 908, name: '乳房护理', subItems: ['乳头矫正', '乳房精油', '乳房乳霜/羊脂膏', '乳晕护理', '乳房护理工具'] },
      { id: 909, name: '童车童床', subItems: ['安全座椅', '婴儿推车', '婴儿床', '餐椅', '自行车', '扭扭车', '滑板车', '电动车'] },
      { id: 910, name: '童装童鞋', subItems: ['套装', '卫衣', '裤子', '外套', '毛衣', '衬衫/针织衫', '户外运动服', 'T恤', '裙子', '亲子装', '演出服', '羽绒服', '棉服', '内衣裤', '配饰', '家居服', '马甲', '袜子', '民族服装', '婴儿礼盒', '连体衣/爬服', '运动鞋', '靴子', '帆布鞋', '皮鞋', '棉鞋', '凉鞋', '拖鞋'] },
      { id: 911, name: '玩具潮流', subItems: ['买个Jellycat哄自己上班', '活人感很重的包包挂件', '益智玩具', '积木拼装', '毛绒玩具', '娃娃玩具', '动漫玩具', '模型玩具', '潮流盲盒', '高达模型'] },
      { id: 912, name: '宠物食品', subItems: ['狗粮', '猫粮', '宠物零食', '猫咪罐头', '狗狗罐头', '小宠食品', '水族食品'] },
      { id: 913, name: '宠物玩具', subItems: ['狗玩具', '猫玩具', '鸟类玩具', '小宠玩具', '智能玩具', '训练用具'] },
      { id: 914, name: '宠物日用', subItems: ['猫砂盆', '狗厕所', '猫砂', '尿垫', '食具水具', '鱼缸/水族箱', '猫狗窝'] },
      { id: 915, name: '宠物服饰', subItems: ['狗界“没朋友”项圈', '衣服', '鞋子', '颈圈/项圈', '帽子/领结'] }
    ]
  }
])

const recommendProducts = ref([])
const currentPage = ref(1)
const pageSize = ref(20)
const isLoading = ref(false)
const hasMore = ref(true)
const isError = ref(false)
const isRefreshing = ref(false)

// 虚拟滚动状态
const viewportHeight = ref(window.innerHeight)
const scrollTop = ref(0)
const rowHeight = 330 // 微调行高，增加上下间距舒适度
const itemsPerRow = 6
const bufferRows = 3 // 上下缓冲区行数

// 计算当前应该显示的商品切片
const visibleProducts = computed(() => {
  if (recommendProducts.value.length === 0) return []
  
  // 计算起始行和结束行
  const startRow = Math.max(0, Math.floor(scrollTop.value / rowHeight) - bufferRows)
  const visibleRows = Math.ceil(viewportHeight.value / rowHeight)
  const endRow = Math.min(
    Math.ceil(recommendProducts.value.length / itemsPerRow),
    startRow + visibleRows + bufferRows * 2
  )
  
  const startIdx = startRow * itemsPerRow
  const endIdx = endRow * itemsPerRow
  
  // 为了满足 DOM 节点数量控制在 100 以内的要求（6列 * 16行 = 96个节点）
  // 我们确保切片长度不超过 100
  const limitedEndIdx = Math.min(endIdx, startIdx + 96)
  
  return recommendProducts.value.slice(startIdx, limitedEndIdx).map((p, index) => ({
    ...p,
    // 用于绝对定位的坐标计算
    style: {
        position: 'absolute',
        top: `${Math.floor((startIdx + index) / itemsPerRow) * rowHeight}px`,
        left: `${((startIdx + index) % itemsPerRow) * (100 / itemsPerRow)}%`,
        width: `${100 / itemsPerRow}%`,
        padding: '5px',
        boxSizing: 'border-box'
      }
  }))
})

// 计算容器总高度
const totalGridHeight = computed(() => {
  const rows = Math.ceil(recommendProducts.value.length / itemsPerRow)
  return rows * rowHeight
})

// 加载更多商品
const loadMoreProducts = async (isInitial = false) => {
  if (isLoading.value || (!hasMore.value && !isInitial)) return
  
  isLoading.value = true
  isError.value = false
  
  try {
    const res = await getRecommendProducts({ 
      page: isInitial ? 1 : currentPage.value + 1,
      size: pageSize.value,
      limit: pageSize.value // 兼容旧接口
    })
    
    if (res.code === 200) {
      // 兼容分页对象和数组两种返回格式
      const productList = Array.isArray(res.data) ? res.data : (res.data?.records || [])
      const newProducts = productList.map(p => {
        // mainImage 可能是逗号分隔的多个URL，只取第一个作为列表展示图
        const mainImageUrl = p.mainImage?.split(',')[0]?.trim() || ''
        return {
          id: p.id,
          name: p.name || p.productName,
          image: mainImageUrl || p.imageUrl || 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIyMDAiIGhlaWdodD0iMjAwIiB2aWV3Qm94PSIwIDAgMjAwIDIwMCI+PHJlY3Qgd2lkdGg9IjIwMCIgaGVpZ2h0PSIyMDAiIGZpbGw9IiNmNWY1ZjUiLz48cGF0aCBkPSJNODAgNzBoNDBjNS41IDAgMTAgNC41IDEwIDEwdjQwYzAgNS41LTQuNSAxMC0xMCAxMEg4MGMtNS41IDAtMTAtNC41LTEwLTEwVjgwYzAtNS41IDQuNS0xMCAxMC0xMHptMzUgMTVhNSA1IDAgMTEtMTAgMCA1IDUgMCAwMTEwIDB6bS0zMCAzNWwyMC0yMCAxNSAxNS0zNSAzNXYtMzB6IiBmaWxsPSIjY2NjIi8+PC9zdmc+',
          price: p.price,
          sales: p.salesCount || p.sales || 0
        }
      })
      
      if (isInitial) {
        recommendProducts.value = newProducts
        currentPage.value = 1
      } else {
        recommendProducts.value = [...recommendProducts.value, ...newProducts]
        currentPage.value++
      }
      
      hasMore.value = newProducts.length === pageSize.value
    } else {
      isError.value = true
    }
  } catch (e) {
    console.error('Failed to load products', e)
    isError.value = true
  } finally {
    isLoading.value = false
  }
}

// 处理轮播图点击
const handleBannerCarouselClick = (banner) => {
  if (banner?.linkUrl) {
    window.open(banner.linkUrl, '_blank')
  } else if (banner?.productId) {
    router.push(`/product/${banner.productId}`)
  }
}

// 处理图片加载错误
const handleImageError = (event) => {
  const img = event.target
  // 使用 data URI 作为默认占位图（灰色背景 + 图片图标）
  img.src = 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIyMDAiIGhlaWdodD0iMjAwIiB2aWV3Qm94PSIwIDAgMjAwIDIwMCI+PHJlY3Qgd2lkdGg9IjIwMCIgaGVpZ2h0PSIyMDAiIGZpbGw9IiNmNWY1ZjUiLz48cGF0aCBkPSJNODAgNzBoNDBjNS41IDAgMTAgNC41IDEwIDEwdjQwYzAgNS41LTQuNSAxMC0xMCAxMEg4MGMtNS41IDAtMTAtNC41LTEwLTEwVjgwYzAtNS41IDQuNS0xMCAxMC0xMHptMzUgMTVhNSA1IDAgMTEtMTAgMCA1IDUgMCAwMTEwIDB6bS0zMCAzNWwyMC0yMCAxNSAxNS0zNSAzNXYtMzB6IiBmaWxsPSIjY2NjIi8+PC9zdmc+'
  // 防止无限循环
  img.onerror = null
}

// 刷新推荐
const refreshRecommend = async () => {
  if (isRefreshing.value) return
  isRefreshing.value = true
  hasMore.value = true
  await loadMoreProducts(true)
  ElMessage.success('已刷新推荐商品')
  setTimeout(() => {
    isRefreshing.value = false
  }, 1000)
}

// 滚动处理（带防抖）
let scrollTimer = null
const handleScroll = () => {
  const grid = document.querySelector('.product-grid')
  if (grid) {
    const rect = grid.getBoundingClientRect()
    // 计算相对于 grid 顶部的滚动偏移量
    // 当 grid 顶部到达视口顶部时，rect.top 为 0，此时 scrollTop 应为 0
    // 当继续向上滚动时，rect.top 为负数，scrollTop 变为正数
    scrollTop.value = Math.max(0, -rect.top)
  }
  
  if (scrollTimer) return
  scrollTimer = setTimeout(() => {
    scrollTimer = null
  }, 100)
}

// 监听窗口大小变化
const handleResize = () => {
  viewportHeight.value = window.innerHeight
}

const recommendCategories = ref([
  { id: 1, name: '运动户外' },
  { id: 2, name: '食品生鲜' },
  { id: 3, name: '日用百货' },
  { id: 4, name: '服饰时尚' },
  { id: 5, name: '3c数码' },
  { id: 6, name: '智能家电' },
  { id: 7, name: '家用电器' },
  { id: 8, name: '珠宝饰品' },
  { id: 9, name: '玩具潮玩' },
  { id: 10, name: '鲜花园艺' },
  { id: 11, name: '数字生活' },
  { id: 12, name: '家享家居' },
  { id: 13, name: '家装建材' },
  { id: 14, name: '宠物生活' },
  { id: 15, name: '商业农业' },
  { id: 16, name: '工业用品' },
  { id: 17, name: '汽车用品' }
])
const activeRecommendCat = ref(1)

const scrollRecommendNav = (direction) => {
  const scrollArea = document.querySelector('.nav-scroll-area')
  if (scrollArea) {
    const scrollAmount = 200
    scrollArea.scrollBy({
      left: direction === 'next' ? scrollAmount : -scrollAmount,
      behavior: 'smooth'
    })
  }
}

// Intersection Observer 用于触发加载更多
const sentinelObserver = ref(null)
const setupSentinel = () => {
  const sentinel = document.querySelector('.scroll-sentinel')
  if (!sentinel) return
  
  sentinelObserver.value = new IntersectionObserver((entries) => {
    if (entries[0].isIntersecting && hasMore.value && !isLoading.value) {
      loadMoreProducts()
    }
  }, {
    rootMargin: '200px' // 提前 200px 触发加载
  })
  
  sentinelObserver.value.observe(sentinel)
}
const handleSearch = () => {
  if (!searchKeyword.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  router.push({ path: '/search', query: { q: searchKeyword.value } })
}

const handleLogin = () => {
  router.push('/auth/login')
}

const handleRegister = () => {
  router.push('/auth/register')
}

const goToCart = () => {
  if (!isLoggedIn.value) {
    router.push('/auth/login')
    return
  }
  router.push('/user/cart')
}

const goToProfile = () => {
  if (!isLoggedIn.value) {
    router.push('/auth/login')
    return
  }
  router.push('/user/profile')
}

const goToOrders = () => {
  if (!isLoggedIn.value) {
    router.push('/auth/login')
    return
  }
  router.push('/user/orders')
}

const goToCategory = (id) => {
  ElMessage.info(`查看分类: ${id}`)
}

const goToProduct = (id) => {
  window.open(`/product/${id}`, '_blank')
}

onMounted(async () => {
  window.addEventListener('scroll', handleScroll, { passive: true })
  window.addEventListener('resize', handleResize)
  
  await loadMoreProducts(true)
  setupSentinel()
  
  if (isLoggedIn.value) {
    await userStore.fetchUserInfo(true).catch(() => {})
  }
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
  window.removeEventListener('resize', handleResize)
  if (sentinelObserver.value) {
    sentinelObserver.value.disconnect()
  }
})
</script>

<style scoped>
/* 核心布局变量 */
.home-container {
  font-family: "PingFang SC", "Microsoft YaHei", sans-serif;
  color: #333;
}

.home-main-content {
  flex: 1;
}

.container {
  max-width: none;
  width: 65%;
  min-width: 1200px;
  margin: 0 auto;
  padding: 0 15px;
  position: relative;
}

/* 导航栏样式已移至 UserLayout */

.top-links {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.top-left-section {
  display: flex;
  align-items: center;
}

/* 时间显示 */
.time-display {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #2c3e50;
  font-size: 13px;
  font-weight: 500;
  padding-right: 20px;
  border-right: 1px solid #e0e0e0;
  margin-right: 15px;
}

.time-icon {
  font-size: 14px;
  color: #409eff;
}

/* 顶部欢迎Logo */
.welcome-logo {
  height: 18px;
  width: auto;
  vertical-align: middle;
  margin-right: 8px;
  filter: none; /* 移除滤镜，确保清晰可见 */
}

.welcome-area {
  color: #2c3e50; /* 墨绿色 */
  display: flex;
  align-items: center;
}

.user-nickname {
  color: #2c3e50; /* 墨绿色 */
  font-weight: 500;
  margin: 0 2px;
}

.user-links {
  display: flex;
  align-items: center;
  gap: 15px;
}

.user-links a {
  color: #2c3e50; /* 墨绿色 */
  text-decoration: none;
  transition: color 0.2s;
}

.user-links a:hover {
  color: #5d7e68; /* 悬停改为主题绿色，不再变白 */
  text-decoration: underline;
}

/* 用户下拉菜单 */
.welcome-dropdown {
  position: relative;
  display: inline-block;
  z-index: 1002;
}

/* 用户卡片弹出层 (新版样式) */
.user-card-popover {
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%); /* 居中定位 */
  width: 220px; /* 缩小宽度 */
  background: #e8f5e9; /* 浅绿色背景 */
  border-radius: 10px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
  padding: 12px;
  margin-top: 10px;
  z-index: 2001;
  border: 1px solid #c8e6c9;
  backdrop-filter: blur(8px);
  color: #000; /* 确保下拉栏字体颜色为黑色 */
}

/* 增加透明桥接层，防止鼠标移动到间隙时消失 */
.user-card-popover::before {
  content: '';
  position: absolute;
  top: -12px;
  left: 0;
  right: 0;
  height: 12px;
  background: transparent;
}

.user-info-section {
  display: flex;
  gap: 10px;
}

.avatar-wrapper {
  position: relative;
  width: 44px; /* 缩小头像 */
  height: 44px;
}

.user-avatar {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
  border: 1.5px solid #fff;
  box-shadow: 0 1px 4px rgba(0,0,0,0.1);
}

.vip-tag {
  position: absolute;
  bottom: -3px;
  left: 50%;
  transform: translateX(-50%);
  background: #e6c17a;
  color: #333;
  font-size: 8px; /* 缩小标签 */
  padding: 0px 4px;
  border-radius: 6px;
  white-space: nowrap;
  font-weight: bold;
}

.user-detail-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 2px;
}

.nickname-row {
  display: flex;
  align-items: center;
  gap: 6px;
}

.popover-nickname {
  color: #333;
  font-size: 14px; /* 缩小字号 */
  font-weight: 600;
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.action-links {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px; /* 缩小链接字号 */
}

.action-link {
  color: #666;
  text-decoration: none;
  cursor: pointer;
  transition: color 0.2s;
}

.action-link:hover {
  color: var(--theme-primary);
}

.logout-link:hover {
  color: #ff4d4f;
}

.link-divider {
  color: #ddd;
  font-size: 12px;
}

.popover-footer {
  border-top: 1px solid #f0ede4;
  padding-top: 12px;
}

.equity-btn {
  width: 100%;
  padding: 10px;
  background: #fff;
  border: 1px solid #f0ede4;
  border-radius: 8px;
  color: #333;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  justify-content: center;
  align-items: center;
}

.equity-btn:hover {
  background: #fdfcf8;
  border-color: #e6c17a;
  color: #b8924a;
  box-shadow: 0 2px 8px rgba(230, 193, 122, 0.2);
}

.welcome-trigger {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #2c3e50; /* 统一使用墨绿色 */
  text-decoration: none;
  padding: 4px 0;
  cursor: pointer;
}

.welcome-trigger:hover {
  color: #5d7e68; /* 悬停改为主题绿色 */
}

.arrow-icon {
  font-size: 12px;
  transition: transform 0.3s;
}

.arrow-icon.is-active {
  transform: rotate(180deg);
}

/* 移除旧的 welcome-menu 样式，改用 user-card-popover */
.welcome-menu {
  display: none;
}

.welcome-menu-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  color: #e8f5e9; /* 浅绿色文字 */
  font-size: 13px;
  text-decoration: none;
  cursor: pointer;
  transition: all 0.2s;
}

.welcome-menu-item:hover {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
}

.welcome-menu-item .el-icon {
  font-size: 16px;
  color: #e8f5e9;
}

.welcome-menu-item:hover .el-icon {
  color: #fff;
}

.welcome-menu-item.logout {
  color: #ff8a80; /* 柔和的红色 */
}

.welcome-menu-item.logout .el-icon {
  color: #ff8a80;
}

.welcome-menu-item.logout:hover {
  background: rgba(255, 82, 82, 0.1);
  color: #ff5252;
}

.welcome-menu-item.logout:hover .el-icon {
  color: #ff5252;
}

.menu-divider {
  height: 1px;
  background: #f0f0f0;
  margin: 4px 0;
}

/* 下拉菜单动画 */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease !important;
}

.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0 !important;
  transform: translateY(10px) !important;
}

.divider {
  color: rgba(255, 255, 255, 0.3);
}

.icon-sm {
  width: 16px;
  height: 16px;
  vertical-align: middle;
  margin-right: 4px;
  filter: none; /* 移除滤镜，确保图标清晰可见 */
}

/* 顶部标语Logo */
.slogan-logo {
  height: 24px;
  margin-right: 10px;
  vertical-align: middle;
}

.cart-link {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  background: transparent;
  transition: opacity 0.3s;
}

.cart-link:hover {
  opacity: 0.8;
}

.seller-center-dropdown {
  position: relative;
  height: 100%;
  display: flex;
  align-items: center;
}

.seller-link {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: all 0.3s;
  color: #2c3e50; /* 墨绿色 */
  font-weight: 500;
}

.seller-link:hover {
  background: rgba(255, 255, 255, 0.2);
}

.seller-popover {
  position: absolute;
  top: 100%;
  right: 0;
  width: 150px;
  background: #e8f5e9; /* 浅绿色背景 */
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  padding: 8px 0;
  z-index: 1000;
  margin-top: 5px;
  backdrop-filter: blur(8px);
  border: 1px solid #c8e6c9;
  color: #000 !important; /* 强制内容文字为纯黑色 */
}

/* 增加透明桥接层 */
.seller-popover::before {
  content: '';
  position: absolute;
  top: -8px;
  left: 0;
  right: 0;
  height: 8px;
  background: transparent;
}

.seller-menu {
  display: flex;
  flex-direction: column;
}

.seller-menu-item {
  padding: 10px 20px;
  font-size: 13px;
  color: #000 !important; /* 强制链接文字为纯黑色 */
  text-decoration: none;
  transition: all 0.2s;
  cursor: pointer;
  font-weight: 500;
}

.seller-menu-item:hover {
  background: rgba(93, 126, 104, 0.1);
  color: var(--theme-primary);
}

.arrow-icon {
  margin-left: 4px;
  transition: transform 0.3s;
  font-size: 12px;
}

.arrow-icon.is-active {
  transform: rotate(180deg);
}

/* 主头部 */
.main-header {
  padding: 20px 0 5px;
}

.header-flex {
  display: flex;
  align-items: flex-end; /* 统一改为底部对齐，这通常是 Logo 和搜索框最稳固的对齐基准 */
  gap: 10px;
}

.logo-area {
  width: 230px;
  display: flex;
  flex-direction: column;
}

.search-area {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.header-right-placeholder {
  width: 240px;
}

.logo-area .brand-logo {
  height: 80px;
  width: auto;
  object-fit: contain;
  transform: translateY(-10px); /* 再次向下移动 5px (在 -15px 基础上回退 5px) */
  position: relative;
  z-index: 10;
}

.search-area .brand-slogan {
  margin-top: 0;
  margin-bottom: 20px;
  text-align: center;
  width: 100%;
}

.search-area .slogan-img {
  width: 100%;
  height: auto;
  opacity: 0.8;
  display: block;
}
  .search-box-wrapper {
  display: flex;
  background: rgba(255,255,255,0.9);
  border: 2px solid var(--theme-primary);
  border-radius: 12px; /* 调大圆角 */
  padding: 2px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

.custom-search-input {
  flex: 1;
  border: none;
  background: transparent;
  padding: 10px 20px;
  font-size: 14px;
  outline: none;
}

.search-btn {
  background: var(--theme-primary);
  color: white;
  border: none;
  border-radius: 10px; /* 调大圆角 */
  padding: 0 30px;
  font-weight: bold;
  cursor: pointer;
  transition: background 0.3s;
}

.search-btn:hover {
  background: #4a6b5a;
}

.hot-keywords {
  margin-top: 10px;
  font-size: 11px;
  color: #666;
}

.hot-keywords a {
  margin-right: 12px;
  cursor: pointer;
  color: #555;
  transition: color 0.2s;
}

.hot-keywords a:hover {
  color: var(--theme-accent);
}

/* 导航与横幅 */
.nav-banner-section {
  margin-top: 0px;
}

.flex-layout {
  display: flex;
  gap: 10px; /* 改为10px */
  height: 370px; /* 350px banner + 20px padding (top+bottom) */
  border: 3px solid rgba(100, 142, 118, 0.2); /* 颜色再减淡，更透明 */
  border-radius: 16px; /* 调大圆角，更圆润 */
  padding: 10px; /* 间隔改为10px */
  align-items: stretch;
  position: relative; /* 基础定位参考 */
  overflow: visible; /* 必须为 visible 以允许背景装饰向外溢出 */
  box-sizing: border-box; /* 显式设置 box-sizing */
  margin-bottom: 10px; /* 进一步缩小间距 */
}

.category-sidebar {
  width: 230px; /* 调大宽度以容纳多分类文本 */
  background-color: rgba(255, 255, 255, 0.07); /* 不透明度调整为7% */
  border-radius: 12px; /* 调大圆角 */
  overflow: visible; /* 允许子元素溢出显示 */
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  height: 350px; /* 强制与轮播图高度一致 */
}

.category-list {
  padding: 5px 0;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: space-between; /* 均匀分布分类项 */
}

.category-item {
  padding: 0 15px;
  height: 38px; /* 压缩高度，减少约20% */
  cursor: pointer;
  display: flex;
  align-items: center;
  transition: all 0.2s;
  position: relative;
  font-size: 13px;
  color: #444;
  z-index: 10;
}

.cat-content {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
}

.cat-icon {
  font-size: 16px;
  color: #666;
  flex-shrink: 0;
}

.cat-image-icon {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
  object-fit: contain;
}

.cat-name {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.category-item:hover {
  background: rgba(100, 142, 118, 0.1);
  color: var(--theme-primary);
  z-index: 20;
}

.category-item:hover .cat-icon {
  color: var(--theme-primary);
}

/* 动画过渡 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease !important;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0 !important;
  transform: translateY(10px) !important;
}

/* 强制垂直淡入动画 - 彻底隔离所有滑入可能 */
.fade-pure-enter-active,
.fade-pure-leave-active {
  transition: opacity 0.25s ease-out, transform 0.25s ease-out !important;
}

.fade-pure-enter-from,
.fade-pure-leave-to {
  opacity: 0 !important;
  transform: translateX(-50%) translateY(15px) !important; /* 关键：必须显式包含 translateX(-50%) 否则会被动画重置回 0 */
}

/* 清理所有可能残留的带有 translateX 的动画类 */
.fade-slide-enter-from,
.fade-slide-leave-to,
.fade-enter-from,
.fade-leave-to {
  opacity: 0 !important;
  transform: translateX(-50%) translateY(10px) !important;
  transition: none !important;
}

/* 子分类悬浮层 (绝对定位，高度自适应) */
.sub-category-popover {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  min-height: 460px;
  max-height: 900px; /* 适当调大最大高度 */
  z-index: 3000;
  background-color: transparent;
  padding: 0; /* padding 移至内容区，防止干扰背景 */
  border-radius: 0 16px 16px 16px;
  border: none;
  overflow: visible; /* 允许内部背景溢出 */
  pointer-events: auto;
  transition: height 0.3s ease;
  box-shadow: 0 10px 30px rgba(0,0,0,0.15);
  display: flex;
  flex-direction: column;
}

.popover-content-area {
  margin-bottom: 80px; /* 核心修改：将内容区域的物理边界向上移动 80px */
  padding: 30px 60px 0 40px;
  position: relative;
  z-index: 2;
  flex: 1;
  overflow-y: auto;
  overflow-x: visible;
  /* 渐隐遮罩：在内容物理边界结束前 20px 开始渐隐 */
  -webkit-mask-image: linear-gradient(to bottom, black 0%, black calc(100% - 20px), transparent 100%);
  mask-image: linear-gradient(to bottom, black 0%, black calc(100% - 20px), transparent 100%);
}

/* 移除不再需要的物理占位符样式 */
.popover-footer-spacer {
  display: none;
}

/* 滚动条美化 - 契合国风主题色 */
.popover-content-area::-webkit-scrollbar {
  width: 6px; /* 稍微加宽一点，方便点击 */
}

.popover-content-area::-webkit-scrollbar-track {
  background: rgba(100, 142, 118, 0.05); /* 极淡的青色背景 */
  border-radius: 3px;
}

.popover-content-area::-webkit-scrollbar-thumb {
  /* 使用主题色（青色）的渐变效果 */
  background: linear-gradient(
    to bottom,
    rgba(100, 142, 118, 0.4),
    rgba(100, 142, 118, 0.6)
  );
  border-radius: 3px;
  border: 1px solid rgba(255, 255, 255, 0.3); /* 增加一点亮边，更有质感 */
}

.popover-content-area::-webkit-scrollbar-thumb:hover {
  background: rgba(100, 142, 118, 0.8); /* 悬停时加深 */
}

/* 独立背景装饰层 */
.popover-bg-wrapper {
  position: absolute;
  top: 0;
  left: 0;
  width: calc(100% + 200px); /* 宽度溢出 */
  height: 100%; /* 高度强制设为容器 100% */
  background-image: url('/主题/分类背景.png');
  background-repeat: no-repeat;
  background-position: left top;
  background-size: 100% 100%; /* 强制背景图在整个动态高度内拉伸 */
  z-index: 1;
  pointer-events: none;
  border-radius: 0 16px 16px 16px;
  filter: drop-shadow(0 5px 15px rgba(0,0,0,0.1));
}

/* 移除之前的伪元素背景，改用独立的 .popover-bg-wrapper */
.sub-category-popover::before {
  display: none;
}

.sub-cat-group {
  position: relative;
  z-index: 1;
  margin-bottom: 20px;
  display: flex;
  align-items: flex-start;
  /* 内容向右微调，确保不与背景左侧深色装饰重合 */
  transform: translateX(10px); 
}

.sub-cat-title {
  width: 110px; /* 稍微加宽一点标题区 */
  font-size: 13px;
  font-weight: bold;
  color: var(--theme-primary); /* 使用主题色强调标题 */
  padding-right: 15px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  text-align: right;
  flex-shrink: 0;
  height: 24px;
  line-height: 24px;
}

.sub-cat-title .el-icon {
  font-size: 12px;
  margin-left: 4px;
  color: #999;
}

.sub-cat-links {
  flex: 1;
  display: flex;
  flex-wrap: wrap;
  gap: 0 15px;
  padding-top: 2px;
}

.sub-cat-links a {
  font-size: 13px;
  color: #666;
  line-height: 24px;
  white-space: nowrap;
  cursor: pointer;
  transition: color 0.2s;
}

.sub-cat-links a:hover {
  color: var(--theme-primary);
}

.main-column {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px; /* 改为10px */
  position: relative; /* 必须为 relative 以支持二级菜单的绝对定位 */
  overflow: visible; /* 必须为 visible 以允许二级菜单背景溢出 */
}

.main-banner {
  width: 100%;
  border-radius: 12px; /* 调大圆角 */
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.quick-nav-bar {
  width: 100%;
}

.quick-nav-list {
  display: flex;
  background: transparent;
  border-radius: 12px; /* 调大圆角 */
  padding: 15px;
  justify-content: space-around;
  backdrop-filter: none;
  box-shadow: none;
}

.quick-nav-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: bold;
  color: #444;
  cursor: pointer;
  transition: transform 0.2s;
}

.quick-nav-item:hover {
  transform: translateY(-2px);
  color: var(--theme-primary);
}

.quick-nav-item img {
  width: 36px;
  height: 36px;
}

/* 右侧新版用户面板 */
.user-panel-new {
  padding: 15px 5px;
}

.user-top-info {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 15px; /* 压缩间距 */
}

.user-avatar-main {
  width: 48px; /* 缩小头像 */
  height: 48px;
  border-radius: 50%;
  border: 2px solid #fff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  object-fit: cover;
  display: block;
}

.user-meta {
  flex: 1;
}

.user-nickname-main {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
  text-align: left;
}

.user-quick-links {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #666;
}

.user-quick-links a {
  color: #5d7e6a;
  text-decoration: none;
  cursor: pointer;
}

.user-quick-links a:hover {
  color: var(--theme-primary);
  text-decoration: underline;
}

.link-sep {
  color: #ccc;
}

.user-stats-grid {
  display: flex;
  justify-content: space-between;
  text-align: center;
}

.stat-item {
  cursor: pointer;
  transition: transform 0.2s;
  flex: 1;
}

.stat-item:hover {
  transform: translateY(-2px);
}

.stat-value {
  font-size: 16px; /* 缩小字号 */
  font-weight: bold;
  color: #3a5044;
  margin-bottom: 2px;
}

.stat-label {
  font-size: 12px;
  color: #666;
}

.right-sidebar {
  width: 250px;
  display: flex;
  flex-direction: column;
  padding: 15px;
  /* 调整为由上到下青黄渐变色，30%不透明度，适配国风主题 */
  background: linear-gradient(
    to bottom,
    rgba(100, 142, 118, 0.3),
    rgba(226, 173, 92, 0.3)
  );
  border-radius: 12px;
  height: 350px; /* 强制与轮播图高度一致 */
}

.guest-welcome-msg {
  margin: 20px 0;
  text-align: center;
}

.guest-welcome-msg h3 {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.guest-welcome-msg p {
  font-size: 12px;
  color: #999;
}

.action-btn.login-full {
  width: 100%;
  height: 40px;
  background: linear-gradient(135deg, #5d7e68 0%, #7da38a 100%);
  color: white;
  border: none;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 4px 12px rgba(93, 126, 104, 0.2);
}

.action-btn.login-full:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(93, 126, 104, 0.3);
  opacity: 0.9;
}

.guest-footer {
  margin-top: 25px;
  padding-top: 15px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
}

.guest-footer .stat-value {
  font-size: 20px;
  color: #5d7e68;
  margin-bottom: 5px;
}

.user-panel {
  text-align: center;
  padding-bottom: 15px;
}

.avatar-wrapper {
  margin-bottom: 10px;
}

.user-welcome p {
  font-size: 14px;
  color: #555;
  margin-bottom: 10px;
}

.user-actions {
  display: flex;
  justify-content: center;
  gap: 10px;
}

.action-btn {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  cursor: pointer;
  border: 1px solid transparent;
  transition: all 0.2s;
}

.action-btn.login, .action-btn.vip {
  background: var(--theme-primary);
  color: white;
}

.action-btn.register, .action-btn.logout {
  background: white;
  border-color: var(--theme-primary);
  color: var(--theme-primary);
}

.quick-service {
  padding: 20px 0;
  display: flex;
  justify-content: space-around;
}

.service-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: #666;
  cursor: pointer;
}

.service-item img {
  width: 32px;
  height: 32px;
  transition: transform 0.3s;
}

.service-item:hover img {
  transform: scale(1.1);
}

.promo-img {
  margin-top: auto;
  text-align: center;
}

.promo-img img {
  max-width: 100%;
  border-radius: 4px;
}

/* 内容区 */
.main-content {
  padding: 10px 0; /* 调整为 10px，使各区块间距与内部间距保持一致 */
}

.recommend-section {
  width: calc(100% + 30px); /* 补偿父容器 container 的 padding (左右各 15px) */
  margin-left: -15px; /* 向左偏移 15px 以抵消父容器内边距 */
  box-sizing: border-box; /* 确保 padding 不增加总宽度 */
  padding: 10px; /* 内部间距统一为 10px */
  /* 调整为由上到下青黄渐变色，30%不透明度，适配国风主题 */
  background: linear-gradient(
    to bottom,
    rgba(100, 142, 118, 0.3),
    rgba(226, 173, 92, 0.3)
  );
  border-radius: 16px;
  border: 3px solid rgba(100, 142, 118, 0.2);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  backdrop-filter: blur(10px);
  margin-top: 10px; /* 进一步缩小间距 */
}

.section-header-styled {
  height: 46px; /* 盒子总高度 46px */
  padding: 8px 0; /* 上下内边距 8px */
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
  box-sizing: border-box;
}

.recommend-tab-main {
  flex: 1;
  display: flex;
  align-items: center;
  overflow: hidden;
  gap: 12px;
}

.recommend-label-tag {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  background: rgba(100, 142, 118, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 12px;
  color: white;
  font-size: 14px;
  font-weight: bold;
  white-space: nowrap;
  box-shadow: 0 2px 6px rgba(0,0,0,0.1);
}

.label-icon {
  height: 24px;
}

.recommend-nav-wrapper {
  flex: 1;
  display: flex;
  align-items: center;
  overflow: hidden;
  position: relative;
  min-width: 0;
}

.nav-scroll-area {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 22px;
  overflow-x: auto;
  scrollbar-width: none;
  min-width: 0;
  padding: 0 15px; /* 增加内边距，配合渐变 */
  /* 使用遮罩实现左右边缘淡出效果，解决文字被生硬截断的问题 */
  -webkit-mask-image: linear-gradient(to right, transparent, black 15px, black calc(100% - 15px), transparent);
  mask-image: linear-gradient(to right, transparent, black 15px, black calc(100% - 15px), transparent);
}

.nav-scroll-area::-webkit-scrollbar {
  display: none;
}

.nav-item {
  font-size: 14px;
  color: #444;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.3s;
  font-weight: 500;
}

.nav-item:hover {
  color: var(--theme-primary);
}

.nav-item.active {
  color: #d35400; /* 类似图中的深橘色/红棕色 */
  font-weight: bold;
}

.nav-arrow-btn {
  width: 32px; /* 与刷新按钮大小一致 */
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: 1px solid rgba(100, 142, 118, 0.4); /* 与刷新按钮边框一致 */
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  flex-shrink: 0;
  padding: 6px;
}

.nav-arrow-btn:hover {
  background: rgba(100, 142, 118, 0.1);
}

.nav-icon {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.nav-icon.mirror {
  transform: scaleX(-1); /* 水平翻转图标作为向左按钮 */
}

.recommend-refresh-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  background: transparent; /* 背景透明 */
  border: 1px solid rgba(100, 142, 118, 0.4); /* 添加合适的边框颜色 */
  border-radius: 8px;
  padding: 4px;
  margin-left: 15px; /* 调整与导航区的间距 */
}

.recommend-refresh-btn:hover {
  background: rgba(100, 142, 118, 0.1);
}

.refresh-icon {
  width: 100%;
  height: 100%;
  object-fit: contain;
  transition: transform 0.8s cubic-bezier(0.4, 0, 0.2, 1);
}

.refresh-icon.rotating {
  animation: rotate-once 0.8s linear;
}

@keyframes rotate-once {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.product-grid {
  display: block; /* 虚拟滚动使用绝对定位，不需要 grid 布局 */
  padding: 5px;
}

.product-card {
  /* 背景设置为完全透明，使其融入父容器的渐变色中 */
  background: transparent;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  /* 移除背景过渡，因为背景加深现在在 info 区域 */
  transition: transform 0.3s ease;
  /* 完全移除边框线 */
  border: none;
  display: flex;
  flex-direction: column;
  position: relative;
  /* 确保在桌面端和移动端聚焦时也有反馈 */
  outline: none;
}

.product-card::before {
  display: none;
}

.product-card:hover,
.product-card:focus {
  /* 取消平移和阴影动画 */
  transform: none;
  box-shadow: none;
}

/* 仅在悬停或聚焦时加深图片区域的背景 */
.product-card:hover .product-image-box::after,
.product-card:focus .product-image-box::after {
  opacity: 1;
}

/* 针对聚焦状态提供清晰的轮廓反馈，提升可访问性 */
.product-card:focus-visible {
  box-shadow: 0 0 0 2px rgba(100, 142, 118, 0.4);
}

.product-image-box {
  position: relative;
  width: 100%;
  padding-top: 100%;
  overflow: hidden;
  /* 恢复纯白色背景，以便清晰展示商品图片 */
  background: #ffffff;
  border-radius: 12px;
}

/* 添加图片加深遮罩层 */
.product-image-box::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.12);
  opacity: 0;
  transition: opacity 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  pointer-events: none;
  z-index: 1;
}

.product-image-box img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: none;
}

.product-card:hover .product-image-box img {
  /* 保持静态，不进行缩放 */
  transform: none;
}

.product-info {
  padding: 8px 10px 10px; /* 减少底部内边距 */
  height: 60px; /* 固定信息区域高度，确保价格对齐 */
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  z-index: 1;
}

.product-title-row {
  display: flex;
  align-items: center; /* 改为垂直居中 */
  gap: 4px;
  margin-bottom: 0;
  height: 24px; /* 固定高度为 24px */
  overflow: hidden;
}

.platform-tag {
  /* 标签颜色调整为更沉稳的朱红色，符合国风 */
  background: linear-gradient(135deg, #a63e3e, #c04848);
  color: #fff;
  font-size: 10px;
  padding: 0px 3px;
  border-radius: 2px;
  font-weight: bold;
  flex-shrink: 0;
  /* 移除 margin-top 以便在 24px 容器中居中 */
}

.product-name {
  font-size: 12px;
  /* 调整为更深沉、更有质感的墨色，融入微弱的青绿调 */
  color: #34495e; 
  line-height: 24px;
  height: 24px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin: 0;
  font-weight: 500;
}

.product-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 3px;
  margin-bottom: 2px;
}

.tag-item {
  font-size: 9px;
  /* 标签文字改用主题色 */
  color: #648e76;
  background: rgba(100, 142, 118, 0.08);
  border: 1px solid rgba(100, 142, 118, 0.2);
  padding: 0px 3px;
  border-radius: 2px;
}

.product-price-row {
  margin-top: 2px;
  display: flex;
  justify-content: space-between;
  align-items: center; /* 确保垂直居中 */
  height: 24px; /* 固定高度为 24px */
  gap: 4px;
}

.price-wrapper {
  /* 价格使用朱红色 */
  color: #a63e3e;
  font-weight: bold;
}

.currency {
  font-size: 11px;
  margin-right: 1px;
}

.price-val {
  font-size: 18px;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  line-height: 1;
}

.sales-count {
  /* 销量文字颜色调淡 */
  color: #7f8c8d;
  font-size: 10px;
}

/* 底部和工具栏样式已移至 UserLayout */

/* 滚动加载相关样式 */
.scroll-footer {
  padding: 30px 0;
  text-align: center;
  position: relative;
  min-height: 80px;
}

.loading-status {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #648e76; /* 主题色 */
  font-size: 14px;
}

.is-loading {
  animation: rotate 1s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.error-status {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  color: #ff4d4f;
  font-size: 14px;
}

.no-more-status {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
  padding: 0 40px;
}

.no-more-status .line {
  flex: 1;
  height: 1px;
  background: linear-gradient(to right, transparent, rgba(100, 142, 118, 0.2), transparent);
}

.no-more-status .text {
  color: #999;
  font-size: 13px;
  letter-spacing: 1px;
}

.scroll-sentinel {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 50px;
  pointer-events: none;
}

.empty-products {
  position: absolute;
  top: 100px;
  left: 0;
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #999;
  gap: 15px;
}

/* 响应式适配 */
@media (max-width: 992px) {
  .category-sidebar, .right-sidebar {
    display: none;
  }
  
  .flex-layout {
    height: auto;
  }
  
  .product-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
