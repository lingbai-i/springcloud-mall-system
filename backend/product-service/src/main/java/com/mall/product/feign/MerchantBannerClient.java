package com.mall.product.feign;

import com.mall.common.core.domain.R;
import com.mall.product.domain.vo.ActiveBannerVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 商家轮播图服务Feign客户端
 * 用于从merchant-service获取轮播图数据和记录统计
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-12-28
 */
@FeignClient(name = "merchant-service", contextId = "merchantBanner")
public interface MerchantBannerClient {

    /**
     * 获取活跃轮播图列表
     * 
     * @param limit 最大返回数量
     * @return 活跃轮播图列表
     */
    @GetMapping("/api/internal/banner/active")
    R<List<ActiveBannerVO>> getActiveBanners(@RequestParam(name = "limit", defaultValue = "5") int limit);

    /**
     * 记录轮播图曝光
     * 
     * @param id 轮播图ID
     * @return 操作结果
     */
    @PostMapping("/api/internal/banner/{id}/impression")
    R<String> recordImpression(@PathVariable("id") Long id);

    /**
     * 记录轮播图点击
     * 
     * @param id 轮播图ID
     * @return 操作结果
     */
    @PostMapping("/api/internal/banner/{id}/click")
    R<String> recordClick(@PathVariable("id") Long id);
}
