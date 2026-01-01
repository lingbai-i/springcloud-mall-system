/**
 * 服务器时间同步工具类
 * 统一使用北京时间（GMT+8）作为基准
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-01
 */
import { getServerTime } from '@/api/system'
import * as logger from '@/utils/logger'

class ServerTimeManager {
  constructor() {
    // 服务器时间与本地时间的差值（毫秒）
    this.timeDiff = 0
    
    // 是否已经同步过
    this.synced = false
    
    // 同步中标志
    this.syncing = false
    
    // 自动同步定时器
    this.syncTimer = null
    
    // 同步间隔（默认5分钟）
    this.syncInterval = 5 * 60 * 1000
  }
  
  /**
   * 同步服务器时间
   * @returns {Promise<boolean>} 是否同步成功
   */
  async sync() {
    if (this.syncing) {
      logger.debug('[服务器时间] 正在同步中，跳过')
      return false
    }
    
    this.syncing = true
    
    try {
      const startTime = Date.now()
      const response = await getServerTime()
      const endTime = Date.now()
      
      if (response && response.data && response.data.timestamp) {
        // 计算网络延迟
        const networkDelay = (endTime - startTime) / 2
        
        // 计算服务器时间（补偿网络延迟）
        const serverTime = response.data.timestamp + networkDelay
        
        // 计算时间差
        this.timeDiff = serverTime - endTime
        
        this.synced = true
        
        logger.info('[服务器时间] 同步成功', {
          serverTime: new Date(serverTime).toLocaleString('zh-CN', { timeZone: 'Asia/Shanghai' }),
          localTime: new Date(endTime).toLocaleString('zh-CN'),
          timeDiff: this.timeDiff,
          networkDelay,
          timezone: response.data.timezone
        })
        
        return true
      }
      
      logger.warn('[服务器时间] 同步失败：响应数据无效', response)
      return false
    } catch (error) {
      logger.error('[服务器时间] 同步失败', error)
      return false
    } finally {
      this.syncing = false
    }
  }
  
  /**
   * 获取当前服务器时间（毫秒时间戳）
   * @returns {number} 服务器时间戳
   */
  now() {
    if (!this.synced) {
      logger.warn('[服务器时间] 未同步，使用本地时间')
      return Date.now()
    }
    return Date.now() + this.timeDiff
  }
  
  /**
   * 获取当前服务器时间（Date对象）
   * @returns {Date} 服务器时间Date对象
   */
  nowDate() {
    return new Date(this.now())
  }
  
  /**
   * 格式化服务器时间
   * @param {string} format 格式字符串，默认 'YYYY-MM-DD HH:mm:ss'
   * @returns {string} 格式化后的时间字符串
   */
  format(format = 'YYYY-MM-DD HH:mm:ss') {
    const date = this.nowDate()
    
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    const seconds = String(date.getSeconds()).padStart(2, '0')
    
    const weekDays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
    const weekDay = weekDays[date.getDay()]
    
    return format
      .replace('YYYY', year)
      .replace('MM', month)
      .replace('DD', day)
      .replace('HH', hours)
      .replace('mm', minutes)
      .replace('ss', seconds)
      .replace('WW', weekDay)
  }
  
  /**
   * 启动自动同步
   * @param {number} interval 同步间隔（毫秒），默认5分钟
   */
  startAutoSync(interval = this.syncInterval) {
    // 先清除已有的定时器
    this.stopAutoSync()
    
    // 立即同步一次
    this.sync()
    
    // 设置定时同步
    this.syncTimer = setInterval(() => {
      this.sync()
    }, interval)
    
    logger.info('[服务器时间] 启动自动同步', { interval })
  }
  
  /**
   * 停止自动同步
   */
  stopAutoSync() {
    if (this.syncTimer) {
      clearInterval(this.syncTimer)
      this.syncTimer = null
      logger.info('[服务器时间] 停止自动同步')
    }
  }
  
  /**
   * 获取时间差（毫秒）
   * @returns {number} 服务器时间与本地时间的差值
   */
  getTimeDiff() {
    return this.timeDiff
  }
  
  /**
   * 是否已同步
   * @returns {boolean} 是否已同步
   */
  isSynced() {
    return this.synced
  }
}

// 创建单例
const serverTimeManager = new ServerTimeManager()

// 导出实例和类
export { serverTimeManager, ServerTimeManager }
export default serverTimeManager

