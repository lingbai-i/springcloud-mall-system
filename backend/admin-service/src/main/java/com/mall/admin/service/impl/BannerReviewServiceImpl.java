package com.mall.admin.service.impl;

import com.mall.admin.client.MerchantBannerClient;
import com.mall.admin.domain.vo.BannerReviewDetailVO;
import com.mall.admin.domain.vo.BannerReviewVO;
import com.mall.admin.service.BannerReviewService;
import com.mall.common.core.domain.R;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * 轮播图审核服务实现类
 * 
 * @author system
 * @version 1.0
 * @since 2025-12-28
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BannerReviewServiceImpl implements BannerReviewService {

    private final MerchantBannerClient merchantBannerClient;

    @Override
    public Page<BannerReviewVO> getPendingList(int page, int size) {
        log.info("获取待审核申请列表，页码：{}，大小：{}", page, size);
        try {
            R<Page<BannerReviewVO>> response = merchantBannerClient.getPendingApplications(page, size);
            if (response.isSuccess() && response.getData() != null) {
                return response.getData();
            }
            log.warn("获取待审核列表失败：{}", response.getMessage());
            return new PageImpl<>(Collections.emptyList(), PageRequest.of(page, size), 0);
        } catch (Exception e) {
            log.error("调用商家服务获取待审核列表失败", e);
            throw new RuntimeException("获取待审核列表失败：" + e.getMessage());
        }
    }

    @Override
    public Page<BannerReviewVO> getAllApplications(String status, String startDate, String endDate, int page, int size) {
        log.info("获取所有申请列表，状态：{}，页码：{}，大小：{}", status, page, size);
        try {
            R<Page<BannerReviewVO>> response = merchantBannerClient.getAllApplications(status, startDate, endDate, page, size);
            if (response.isSuccess() && response.getData() != null) {
                return response.getData();
            }
            log.warn("获取申请列表失败：{}", response.getMessage());
            return new PageImpl<>(Collections.emptyList(), PageRequest.of(page, size), 0);
        } catch (Exception e) {
            log.error("调用商家服务获取申请列表失败", e);
            throw new RuntimeException("获取申请列表失败：" + e.getMessage());
        }
    }

    @Override
    public BannerReviewDetailVO getApplicationDetail(Long id) {
        log.info("获取申请详情：{}", id);
        try {
            R<BannerReviewVO> response = merchantBannerClient.getApplicationDetail(id);
            if (response.isSuccess() && response.getData() != null) {
                return convertToDetailVO(response.getData());
            }
            throw new RuntimeException("申请不存在");
        } catch (Exception e) {
            log.error("调用商家服务获取申请详情失败", e);
            throw new RuntimeException("获取申请详情失败：" + e.getMessage());
        }
    }

    @Override
    public void approveApplication(Long id, Long adminId) {
        log.info("管理员 {} 审核通过申请 {}", adminId, id);
        try {
            R<String> response = merchantBannerClient.approveApplication(id, adminId);
            if (!response.isSuccess()) {
                throw new RuntimeException(response.getMessage());
            }
            log.info("申请 {} 审核通过成功", id);
        } catch (Exception e) {
            log.error("调用商家服务审核通过失败", e);
            throw new RuntimeException("审核通过失败：" + e.getMessage());
        }
    }

    @Override
    public void rejectApplication(Long id, Long adminId, String reason) {
        log.info("管理员 {} 审核拒绝申请 {}，原因：{}", adminId, id, reason);
        
        // 验证拒绝原因不能为空
        if (reason == null || reason.trim().isEmpty()) {
            throw new RuntimeException("拒绝原因不能为空");
        }
        
        try {
            R<String> response = merchantBannerClient.rejectApplication(id, adminId, reason);
            if (!response.isSuccess()) {
                throw new RuntimeException(response.getMessage());
            }
            log.info("申请 {} 审核拒绝成功", id);
        } catch (Exception e) {
            log.error("调用商家服务审核拒绝失败", e);
            throw new RuntimeException("审核拒绝失败：" + e.getMessage());
        }
    }

    @Override
    public Long getPendingCount() {
        log.debug("获取待审核申请数量");
        try {
            R<Long> response = merchantBannerClient.getPendingCount();
            if (response.isSuccess() && response.getData() != null) {
                return response.getData();
            }
            return 0L;
        } catch (Exception e) {
            log.error("调用商家服务获取待审核数量失败", e);
            return 0L;
        }
    }

    /**
     * 将BannerReviewVO转换为BannerReviewDetailVO
     */
    private BannerReviewDetailVO convertToDetailVO(BannerReviewVO vo) {
        BannerReviewDetailVO detailVO = new BannerReviewDetailVO();
        detailVO.setId(vo.getId());
        detailVO.setMerchantId(vo.getMerchantId());
        detailVO.setMerchantName(vo.getMerchantName());
        detailVO.setImageUrl(vo.getImageUrl());
        detailVO.setTitle(vo.getTitle());
        detailVO.setTargetUrl(vo.getTargetUrl());
        detailVO.setStartDate(vo.getStartDate());
        detailVO.setEndDate(vo.getEndDate());
        detailVO.setStatus(vo.getStatus());
        detailVO.setStatusText(vo.getStatusText());
        detailVO.setSubmitTime(vo.getSubmitTime());
        detailVO.setReviewTime(vo.getReviewTime());
        return detailVO;
    }
}
