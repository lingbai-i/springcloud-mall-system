import request from '@/utils/request'

/**
 * 文件上传API
 * 使用product-service的MinIO上传功能（各服务独立上传，无跨服务调用）
 */

/**
 * 上传图片文件
 * @param {File} file 图片文件
 * @param {string} folder 存储文件夹（默认reviews）
 * @returns {Promise<{url: string}>} 返回图片URL
 */
export function uploadImage(file, folder = 'reviews') {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('folder', folder)
  
  return request({
    url: '/product-service/files/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 批量上传图片
 * @param {File[]} files 图片文件数组
 * @param {string} folder 存储文件夹
 * @returns {Promise<string[]>} 返回图片URL数组
 */
export async function uploadImages(files, folder = 'reviews') {
  const urls = []
  for (const file of files) {
    try {
      const res = await uploadImage(file, folder)
      if (res.data?.url) {
        urls.push(res.data.url)
      }
    } catch (e) {
      console.error('图片上传失败:', e)
    }
  }
  return urls
}
