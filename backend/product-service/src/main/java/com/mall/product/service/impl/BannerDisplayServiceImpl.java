package com.mall.product.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mall.common.core.domain.R;
import com.mall.product.domain.vo.ActiveBannerVO;
import com.mall.product.feign.MerchantBannerClient;
import com.mall.product.service.BannerDisplayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 轮播图展示服务实现类
 * 
 * @author system
 * @version 1.0
 * @since 2025-12-28
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BannerDisplayServiceImpl implements BannerDisplayService {

    private final MerchantBannerClient merchantBannerClient;
    private final ObjectMapper objectMapper;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 缓存键前缀
     */
    private static final String CACHE_KEY_PREFIX = "banner:active:";
    
    /**
     * 缓存过期时间（分钟）
     */
    private static final long CACHE_EXPIRE_MINUTES = 5;

    /**
     * 最大活跃轮播图数量
     */
    private static final int MAX_ACTIVE_BANNERS = 10;

    @Override
    public List<ActiveBannerVO> getActiveBanners(int limit) {
        // 限制最大数量
        int actualLimit = Math.min(limit, MAX_ACTIVE_BANNERS);
        String cacheKey = CACHE_KEY_PREFIX + actualLimit;
        
        // 尝试从缓存获取
        if (redisTemplate != null) {
            try {
                Object cached = redisTemplate.opsForValue().get(cacheKey);
                if (cached != null) {
                    log.debug("从缓存获取活跃轮播图列表");
                    return convertFromCache(cached);
                }
            } catch (Exception e) {
                log.warn("从缓存获取轮播图失败，将从服务获取", e);
            }
        }
        
        // 从商家服务获取
        List<ActiveBannerVO> banners = fetchFromMerchantService(actualLimit);
        
        // 存入缓存
        if (redisTemplate != null && !banners.isEmpty()) {
            try {
                redisTemplate.opsForValue().set(cacheKey, banners, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
                log.debug("轮播图列表已缓存，数量：{}", banners.size());
            } catch (Exception e) {
                log.warn("缓存轮播图列表失败", e);
            }
        }
        
        return banners;
    }

    @Override
    public void recordImpression(Long bannerId) {
        log.debug("记录轮播图曝光：{}", bannerId);
        try {
            R<String> response = merchantBannerClient.recordImpression(bannerId);
            if (!response.isSuccess()) {
                log.warn("记录轮播图曝光失败：{}", response.getMessage());
            }
        } catch (Exception e) {
            log.error("调用商家服务记录曝光失败", e);
        }
    }

    @Override
    public void recordClick(Long bannerId) {
        log.debug("记录轮播图点击：{}", bannerId);
        try {
            R<String> response = merchantBannerClient.recordClick(bannerId);
            if (!response.isSuccess()) {
                log.warn("记录轮播图点击失败：{}", response.getMessage());
            }
        } catch (Exception e) {
            log.error("调用商家服务记录点击失败", e);
        }
    }

    @Override
    public void refreshCache() {
        log.info("刷新轮播图缓存");
        if (redisTemplate != null) {
            try {
                // 删除所有轮播图缓存
                for (int i = 1; i <= MAX_ACTIVE_BANNERS; i++) {
                    redisTemplate.delete(CACHE_KEY_PREFIX + i);
                }
                log.info("轮播图缓存已清除");
            } catch (Exception e) {
                log.error("清除轮播图缓存失败", e);
            }
        }
    }

    /**
     * 从商家服务获取活跃轮播图
     */
    private List<ActiveBannerVO> fetchFromMerchantService(int limit) {
        try {
            R<List<ActiveBannerVO>> response = merchantBannerClient.getActiveBanners(limit);
            if (response.isSuccess() && response.getData() != null) {
                return response.getData();
            }
            log.warn("获取活跃轮播图失败：{}", response.getMessage());
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("调用商家服务获取活跃轮播图失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 从缓存转换为列表
     */
    @SuppressWarnings("unchecked")
    private List<ActiveBannerVO> convertFromCache(Object cached) {
        try {
            if (cached instanceof List) {
                return (List<ActiveBannerVO>) cached;
            }
            // 如果是JSON字符串，需要反序列化
            if (cached instanceof String) {
                return objectMapper.readValue((String) cached, 
                        new TypeReference<List<ActiveBannerVO>>() {});
            }
            return Collections.emptyList();
        } catch (Exception e) {
            log.warn("转换缓存数据失败", e);
            return Collections.emptyList();
        }
    }
}
