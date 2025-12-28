package com.mall.merchant.service.impl;

import com.mall.merchant.domain.dto.BannerApplicationDTO;
import com.mall.merchant.domain.entity.BannerApplication;
import com.mall.merchant.domain.entity.BannerStatistics;
import com.mall.merchant.domain.entity.Merchant;
import com.mall.merchant.domain.vo.BannerApplicationVO;
import com.mall.merchant.domain.vo.BannerStatisticsVO;
import com.mall.merchant.repository.BannerApplicationRepository;
import com.mall.merchant.repository.BannerStatisticsRepository;
import com.mall.merchant.repository.MerchantRepository;
import com.mall.merchant.service.BannerApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 轮播图申请服务实现类
 * 
 * @author system
 * @version 1.0
 * @since 2025-12-28
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BannerApplicationServiceImpl implements BannerApplicationService {

    private final BannerApplicationRepository bannerApplicationRepository;
    private final BannerStatisticsRepository bannerStatisticsRepository;
    private final MerchantRepository merchantRepository;

    @Override
    @Transactional
    public Long createApplication(BannerApplicationDTO dto, Long merchantId) {
        log.info("Creating banner application for merchant: {}", merchantId);
        
        BannerApplication application = new BannerApplication();
        application.setMerchantId(merchantId);
        application.setImageUrl(dto.getImageUrl());
        application.setTitle(dto.getTitle());
        application.setDescription(dto.getDescription());
        application.setTargetUrl(dto.getTargetUrl());
        application.setStartDate(dto.getStartDate());
        application.setEndDate(dto.getEndDate());
        application.setStatus(BannerApplication.STATUS_PENDING);
        application.setDeleted(0);
        
        BannerApplication saved = bannerApplicationRepository.save(application);
        log.info("Banner application created with id: {}", saved.getId());
        
        return saved.getId();
    }

    @Override
    @Transactional
    public void updateApplication(Long id, BannerApplicationDTO dto, Long merchantId) {
        log.info("Updating banner application: {} for merchant: {}", id, merchantId);
        
        BannerApplication application = bannerApplicationRepository
                .findByIdAndMerchantIdAndDeleted(id, merchantId, 0)
                .orElseThrow(() -> new RuntimeException("申请不存在或无权限访问"));
        
        if (!application.canEdit()) {
            throw new RuntimeException("当前状态不允许编辑");
        }
        
        application.setImageUrl(dto.getImageUrl());
        application.setTitle(dto.getTitle());
        application.setDescription(dto.getDescription());
        application.setTargetUrl(dto.getTargetUrl());
        application.setStartDate(dto.getStartDate());
        application.setEndDate(dto.getEndDate());
        
        // 如果是已拒绝状态，重新提交后变为待审核
        if (BannerApplication.STATUS_REJECTED.equals(application.getStatus())) {
            application.setStatus(BannerApplication.STATUS_PENDING);
            application.setRejectReason(null);
        }
        
        bannerApplicationRepository.save(application);
        log.info("Banner application updated: {}", id);
    }

    @Override
    @Transactional
    public void cancelApplication(Long id, Long merchantId) {
        log.info("Cancelling banner application: {} for merchant: {}", id, merchantId);
        
        int updated = bannerApplicationRepository.cancelApplication(
                id, merchantId, BannerApplication.STATUS_CANCELLED);
        
        if (updated == 0) {
            throw new RuntimeException("申请不存在、无权限或当前状态不允许取消");
        }
        
        log.info("Banner application cancelled: {}", id);
    }

    @Override
    public BannerApplicationVO getApplicationDetail(Long id, Long merchantId) {
        BannerApplication application = bannerApplicationRepository
                .findByIdAndMerchantIdAndDeleted(id, merchantId, 0)
                .orElseThrow(() -> new RuntimeException("申请不存在或无权限访问"));
        
        BannerApplicationVO vo = convertToVO(application);
        
        // 填充统计数据
        fillStatistics(vo);
        
        return vo;
    }

    @Override
    public Page<BannerApplicationVO> getApplicationList(Long merchantId, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BannerApplication> applications;
        
        if (status != null && !status.isEmpty()) {
            applications = bannerApplicationRepository
                    .findByMerchantIdAndStatusAndDeletedOrderByCreateTimeDesc(merchantId, status, 0, pageable);
        } else {
            applications = bannerApplicationRepository
                    .findByMerchantIdAndDeletedOrderByCreateTimeDesc(merchantId, 0, pageable);
        }
        
        return applications.map(this::convertToVO);
    }

    @Override
    public BannerStatisticsVO getStatistics(Long id, Long merchantId) {
        BannerApplication application = bannerApplicationRepository
                .findByIdAndMerchantIdAndDeleted(id, merchantId, 0)
                .orElseThrow(() -> new RuntimeException("申请不存在或无权限访问"));
        
        BannerStatisticsVO vo = new BannerStatisticsVO();
        vo.setBannerId(id);
        vo.setBannerTitle(application.getTitle());
        vo.setStartDate(application.getStartDate());
        vo.setEndDate(application.getEndDate());
        
        // 获取汇总统计
        List<Object[]> sumResult = bannerStatisticsRepository.sumStatisticsByBannerId(id);
        if (!sumResult.isEmpty() && sumResult.get(0) != null) {
            Object[] row = sumResult.get(0);
            vo.setTotalImpressions(((Number) row[0]).longValue());
            vo.setTotalClicks(((Number) row[1]).longValue());
            vo.setUniqueImpressions(((Number) row[2]).longValue());
            vo.setUniqueClicks(((Number) row[3]).longValue());
        } else {
            vo.setTotalImpressions(0L);
            vo.setTotalClicks(0L);
            vo.setUniqueImpressions(0L);
            vo.setUniqueClicks(0L);
        }
        
        vo.calculateCtr();
        
        // 获取每日统计
        List<BannerStatistics> dailyStats = bannerStatisticsRepository
                .findByBannerIdAndStatDateBetweenOrderByStatDateAsc(
                        id, application.getStartDate(), LocalDate.now());
        
        vo.setDailyStats(dailyStats.stream().map(stat -> {
            BannerStatisticsVO.DailyStatistics daily = new BannerStatisticsVO.DailyStatistics();
            daily.setDate(stat.getStatDate());
            daily.setImpressions(stat.getImpressions());
            daily.setClicks(stat.getClicks());
            daily.setCtr(stat.getCtr());
            return daily;
        }).collect(Collectors.toList()));
        
        return vo;
    }

    @Override
    public Long countApplications(Long merchantId, String status) {
        if (status != null && !status.isEmpty()) {
            return bannerApplicationRepository.countByMerchantIdAndStatusAndDeleted(merchantId, status, 0);
        }
        return bannerApplicationRepository.countByMerchantIdAndStatusAndDeleted(merchantId, null, 0);
    }

    // ==================== 管理员端方法实现 ====================

    @Override
    public Page<BannerApplicationVO> getPendingApplications(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BannerApplication> applications = bannerApplicationRepository
                .findByStatusAndDeletedOrderByCreateTimeAsc(BannerApplication.STATUS_PENDING, 0, pageable);
        return applications.map(this::convertToVO);
    }

    @Override
    public Page<BannerApplicationVO> getAllApplications(String status, String startDate, String endDate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BannerApplication> applications;
        
        if (status != null && !status.isEmpty()) {
            applications = bannerApplicationRepository
                    .findByStatusAndDeletedOrderByCreateTimeDesc(status, 0, pageable);
        } else {
            applications = bannerApplicationRepository
                    .findByDeletedOrderByCreateTimeDesc(0, pageable);
        }
        
        return applications.map(this::convertToVO);
    }

    @Override
    public BannerApplicationVO getApplicationDetailForAdmin(Long id) {
        BannerApplication application = bannerApplicationRepository
                .findByIdAndDeleted(id, 0)
                .orElseThrow(() -> new RuntimeException("申请不存在"));
        
        BannerApplicationVO vo = convertToVO(application);
        fillStatistics(vo);
        return vo;
    }

    @Override
    @Transactional
    public void approveApplication(Long id, Long adminId) {
        log.info("Admin {} approving banner application: {}", adminId, id);
        
        BannerApplication application = bannerApplicationRepository
                .findByIdAndDeleted(id, 0)
                .orElseThrow(() -> new RuntimeException("申请不存在"));
        
        if (!BannerApplication.STATUS_PENDING.equals(application.getStatus())) {
            throw new RuntimeException("只有待审核状态的申请可以审核");
        }
        
        application.setStatus(BannerApplication.STATUS_APPROVED);
        application.setReviewerId(adminId);
        application.setReviewTime(java.time.LocalDateTime.now());
        
        bannerApplicationRepository.save(application);
        log.info("Banner application approved: {}", id);
    }

    @Override
    @Transactional
    public void rejectApplication(Long id, Long adminId, String reason) {
        log.info("Admin {} rejecting banner application: {} with reason: {}", adminId, id, reason);
        
        if (reason == null || reason.trim().isEmpty()) {
            throw new RuntimeException("拒绝原因不能为空");
        }
        
        BannerApplication application = bannerApplicationRepository
                .findByIdAndDeleted(id, 0)
                .orElseThrow(() -> new RuntimeException("申请不存在"));
        
        if (!BannerApplication.STATUS_PENDING.equals(application.getStatus())) {
            throw new RuntimeException("只有待审核状态的申请可以审核");
        }
        
        application.setStatus(BannerApplication.STATUS_REJECTED);
        application.setRejectReason(reason);
        application.setReviewerId(adminId);
        application.setReviewTime(java.time.LocalDateTime.now());
        
        bannerApplicationRepository.save(application);
        log.info("Banner application rejected: {}", id);
    }

    @Override
    public Long getPendingCount() {
        return bannerApplicationRepository.countByStatusAndDeleted(BannerApplication.STATUS_PENDING, 0);
    }

    // ==================== 展示端方法实现 ====================

    @Override
    public java.util.List<BannerApplicationVO> getActiveBanners(int limit) {
        log.debug("获取活跃轮播图列表，最大数量：{}", limit);
        LocalDate today = LocalDate.now();
        
        List<BannerApplication> activeBanners = bannerApplicationRepository
                .findActiveBanners(BannerApplication.STATUS_APPROVED, today, 0, PageRequest.of(0, limit));
        
        return activeBanners.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void recordImpression(Long bannerId) {
        log.debug("记录轮播图曝光：{}", bannerId);
        LocalDate today = LocalDate.now();
        
        BannerStatistics stats = bannerStatisticsRepository
                .findByBannerIdAndStatDate(bannerId, today)
                .orElseGet(() -> {
                    BannerStatistics newStats = new BannerStatistics();
                    newStats.setBannerId(bannerId);
                    newStats.setStatDate(today);
                    newStats.setImpressions(0);
                    newStats.setClicks(0);
                    newStats.setUniqueImpressions(0);
                    newStats.setUniqueClicks(0);
                    return newStats;
                });
        
        stats.incrementImpressions();
        stats.setUniqueImpressions(stats.getUniqueImpressions() + 1);
        
        bannerStatisticsRepository.save(stats);
    }

    @Override
    @Transactional
    public void recordClick(Long bannerId) {
        log.debug("记录轮播图点击：{}", bannerId);
        LocalDate today = LocalDate.now();
        
        BannerStatistics stats = bannerStatisticsRepository
                .findByBannerIdAndStatDate(bannerId, today)
                .orElseGet(() -> {
                    BannerStatistics newStats = new BannerStatistics();
                    newStats.setBannerId(bannerId);
                    newStats.setStatDate(today);
                    newStats.setImpressions(0);
                    newStats.setClicks(0);
                    newStats.setUniqueImpressions(0);
                    newStats.setUniqueClicks(0);
                    return newStats;
                });
        
        stats.incrementClicks();
        stats.setUniqueClicks(stats.getUniqueClicks() + 1);
        
        bannerStatisticsRepository.save(stats);
    }

    /**
     * 转换实体为VO
     */
    private BannerApplicationVO convertToVO(BannerApplication application) {
        BannerApplicationVO vo = new BannerApplicationVO();
        vo.setId(application.getId());
        vo.setMerchantId(application.getMerchantId());
        
        // 填充商家名称
        merchantRepository.findById(application.getMerchantId())
                .ifPresent(merchant -> vo.setMerchantName(merchant.getShopName()));
        
        vo.setImageUrl(application.getImageUrl());
        vo.setTitle(application.getTitle());
        vo.setDescription(application.getDescription());
        vo.setTargetUrl(application.getTargetUrl());
        vo.setStartDate(application.getStartDate());
        vo.setEndDate(application.getEndDate());
        vo.setStatus(application.getStatus());
        vo.setStatusText(application.getStatusText());
        vo.setRejectReason(application.getRejectReason());
        vo.setReviewTime(application.getReviewTime());
        vo.setReviewerId(application.getReviewerId());
        vo.setSortOrder(application.getSortOrder());
        vo.setCreateTime(application.getCreateTime());
        vo.setUpdateTime(application.getUpdateTime());
        vo.setCanCancel(application.canCancel());
        vo.setCanEdit(application.canEdit());
        vo.setIsActive(application.isActive());
        return vo;
    }

    /**
     * 填充统计数据
     */
    private void fillStatistics(BannerApplicationVO vo) {
        List<Object[]> sumResult = bannerStatisticsRepository.sumStatisticsByBannerId(vo.getId());
        if (!sumResult.isEmpty() && sumResult.get(0) != null) {
            Object[] row = sumResult.get(0);
            vo.setTotalImpressions(((Number) row[0]).longValue());
            vo.setTotalClicks(((Number) row[1]).longValue());
            
            if (vo.getTotalImpressions() > 0) {
                vo.setCtr(Math.round((vo.getTotalClicks() * 100.0 / vo.getTotalImpressions()) * 100.0) / 100.0);
            } else {
                vo.setCtr(0.0);
            }
        } else {
            vo.setTotalImpressions(0L);
            vo.setTotalClicks(0L);
            vo.setCtr(0.0);
        }
    }
}
