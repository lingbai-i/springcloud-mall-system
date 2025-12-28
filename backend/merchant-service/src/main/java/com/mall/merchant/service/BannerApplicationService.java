package com.mall.merchant.service;

import com.mall.merchant.domain.dto.BannerApplicationDTO;
import com.mall.merchant.domain.vo.BannerApplicationVO;
import com.mall.merchant.domain.vo.BannerStatisticsVO;
import org.springframework.data.domain.Page;

/**
 * 轮播图申请服务接口
 * 提供商家端轮播图申请相关的业务操作
 * 
 * @author system
 * @version 1.0
 * @since 2025-12-28
 */
public interface BannerApplicationService {

    /**
     * 创建轮播图申请
     * 
     * @param dto 申请数据
     * @param merchantId 商家ID
     * @return 申请ID
     */
    Long createApplication(BannerApplicationDTO dto, Long merchantId);

    /**
     * 更新轮播图申请
     * 只有待审核和已拒绝状态的申请可以更新
     * 
     * @param id 申请ID
     * @param dto 申请数据
     * @param merchantId 商家ID
     */
    void updateApplication(Long id, BannerApplicationDTO dto, Long merchantId);

    /**
     * 取消轮播图申请
     * 只有待审核状态的申请可以取消
     * 
     * @param id 申请ID
     * @param merchantId 商家ID
     */
    void cancelApplication(Long id, Long merchantId);

    /**
     * 获取申请详情
     * 
     * @param id 申请ID
     * @param merchantId 商家ID
     * @return 申请详情
     */
    BannerApplicationVO getApplicationDetail(Long id, Long merchantId);

    /**
     * 获取商家的申请列表（分页）
     * 
     * @param merchantId 商家ID
     * @param status 状态筛选（可选）
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 申请分页列表
     */
    Page<BannerApplicationVO> getApplicationList(Long merchantId, String status, int page, int size);

    /**
     * 获取轮播图统计数据
     * 
     * @param id 申请ID
     * @param merchantId 商家ID
     * @return 统计数据
     */
    BannerStatisticsVO getStatistics(Long id, Long merchantId);

    /**
     * 统计商家的申请数量
     * 
     * @param merchantId 商家ID
     * @param status 状态（可选）
     * @return 申请数量
     */
    Long countApplications(Long merchantId, String status);

    // ==================== 管理员端方法 ====================

    /**
     * 获取待审核申请列表（管理员）
     * 
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 待审核申请分页列表
     */
    Page<BannerApplicationVO> getPendingApplications(int page, int size);

    /**
     * 获取所有申请列表（管理员）
     * 
     * @param status 状态筛选（可选）
     * @param startDate 开始日期筛选（可选）
     * @param endDate 结束日期筛选（可选）
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 申请分页列表
     */
    Page<BannerApplicationVO> getAllApplications(String status, String startDate, String endDate, int page, int size);

    /**
     * 获取申请详情（管理员，不校验商家ID）
     * 
     * @param id 申请ID
     * @return 申请详情
     */
    BannerApplicationVO getApplicationDetailForAdmin(Long id);

    /**
     * 审核通过申请
     * 
     * @param id 申请ID
     * @param adminId 管理员ID
     */
    void approveApplication(Long id, Long adminId);

    /**
     * 审核拒绝申请
     * 
     * @param id 申请ID
     * @param adminId 管理员ID
     * @param reason 拒绝原因
     */
    void rejectApplication(Long id, Long adminId, String reason);

    /**
     * 获取待审核申请数量
     * 
     * @return 待审核数量
     */
    Long getPendingCount();

    // ==================== 展示端方法 ====================

    /**
     * 获取活跃轮播图列表（用于首页展示）
     * 返回已审核通过且在展示期内的轮播图，按排序权重和审核时间排序
     * 
     * @param limit 最大返回数量
     * @return 活跃轮播图列表
     */
    java.util.List<BannerApplicationVO> getActiveBanners(int limit);

    /**
     * 记录轮播图曝光
     * 
     * @param bannerId 轮播图ID
     */
    void recordImpression(Long bannerId);

    /**
     * 记录轮播图点击
     * 
     * @param bannerId 轮播图ID
     */
    void recordClick(Long bannerId);
}
