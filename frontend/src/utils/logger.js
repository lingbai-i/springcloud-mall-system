/**
 * 轻量级日志模块（Winston 风格接口）
 * 提供 debug/info/warn/error 四种级别，在前端以 console 作为后端实现。
 * 设计意图：在浏览器环境中复用统一日志接口，后续可透明替换为 Winston。
 * @author lingbai
 * @version 1.0 2025-11-08T08:50:53+08:00：首次添加，提供统一日志接口
 */

// 当前日志级别（可通过环境变量或运行时修改）
let levelOrder = ['debug', 'info', 'warn', 'error']
let currentLevel = 'debug'

/**
 * 设置日志级别
 * @param {('debug'|'info'|'warn'|'error')} level - 日志级别
 */
export function setLevel(level) {
  if (levelOrder.includes(level)) {
    currentLevel = level
  }
}

/**
 * 判断级别是否允许输出
 * @param {string} target - 目标级别
 * @returns {boolean}
 */
function canLog(target) {
  return levelOrder.indexOf(target) >= levelOrder.indexOf(currentLevel)
}

/**
 * 生成带时间戳的日志前缀
 * @param {string} lvl - 日志级别
 * @returns {string}
 */
function prefix(lvl) {
  const ts = new Date().toISOString()
  return `[${ts}] [${lvl.toUpperCase()}]`
}

/**
 * 调试日志
 * @param {string} message - 日志消息
 * @param {any} payload - 结构化上下文
 */
export function debug(message, payload) {
  if (!canLog('debug')) return
  try { console.debug(prefix('debug'), message, payload ?? '') } catch (_) {}
}

/**
 * 信息日志
 * @param {string} message - 日志消息
 * @param {any} payload - 结构化上下文
 */
export function info(message, payload) {
  if (!canLog('info')) return
  try { console.info(prefix('info'), message, payload ?? '') } catch (_) {}
}

/**
 * 警告日志
 * @param {string} message - 日志消息
 * @param {any} payload - 结构化上下文或 Error
 */
export function warn(message, payload) {
  if (!canLog('warn')) return
  try { console.warn(prefix('warn'), message, payload ?? '') } catch (_) {}
}

/**
 * 错误日志
 * @param {string} message - 日志消息
 * @param {any} error - 错误对象或结构化上下文
 */
export function error(message, error) {
  try { console.error(prefix('error'), message, error ?? '') } catch (_) {}
}

export default { setLevel, debug, info, warn, error }

