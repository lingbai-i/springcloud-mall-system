package com.mall.product.service;

import com.mall.product.domain.vo.ActiveBannerVO;

import java.util.List;

/**
 * 轮播图展示服务接口
 * 提供首页轮播图展示相关的业务操作
 * 
 * @author system
 * @version 1.0
 * @since 2025-12-28
 */
public interface BannerDisplayService {

    /**
     * 获取活跃轮播图列表（带缓存）
     * 返回当前活跃的轮播图，优先从Redis缓存获取
     * 
     * @param limit 最大返回数量
     * @return 活跃轮播图列表
     */
    List<ActiveBannerVO> getActiveBanners(int limit);

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

    /**
     * 刷新轮播图缓存
     */
    void refreshCache();
}
