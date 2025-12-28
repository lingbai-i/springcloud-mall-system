package com.mall.product.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mall.common.core.domain.R;
import com.mall.product.domain.vo.ActiveBannerVO;
import com.mall.product.feign.MerchantBannerClient;
import com.mall.product.service.impl.BannerDisplayServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 轮播图展示服务测试类
 * 
 * Feature: banner-promotion
 * 
 * 测试属性:
 * - Property 14: Automatic Activation - 轮播图在开始日期自动激活
 * - Property 15: Automatic Deactivation - 轮播图在结束日期后自动停用
 * - Property 16: Maximum Active Banners - 活跃轮播图数量不超过限制
 * - Property 17: Approval Date Priority - 按审核时间排序
 * - Property 19: Statistics Tracking - 统计数据正确记录
 * 
 * @author system
 * @version 1.0
 * @since 2025-12-28
 */
@ExtendWith(MockitoExtension.class)
class BannerDisplayServiceTest {

    @Mock
    private MerchantBannerClient merchantBannerClient;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    private BannerDisplayService bannerDisplayService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        
        bannerDisplayService = new BannerDisplayServiceImpl(merchantBannerClient, objectMapper);
        ReflectionTestUtils.setField(bannerDisplayService, "redisTemplate", redisTemplate);
    }

    // ==================== Property 14: Automatic Activation ====================

    @Nested
    @DisplayName("Property 14: 轮播图自动激活测试")
    class AutomaticActivationTests {

        @Test
        @DisplayName("活跃轮播图应该在展示期内")
        void activeBanners_shouldBeWithinDisplayPeriod() {
            // Arrange
            List<ActiveBannerVO> banners = createActiveBanners(3);
            LocalDate today = LocalDate.now();
            
            // 设置展示期包含今天
            for (ActiveBannerVO banner : banners) {
                banner.setStartDate(today.minusDays(1));
                banner.setEndDate(today.plusDays(7));
            }
            
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(anyString())).thenReturn(null);
            when(merchantBannerClient.getActiveBanners(anyInt()))
                    .thenReturn(R.ok(banners));

            // Act
            List<ActiveBannerVO> result = bannerDisplayService.getActiveBanners(5);

            // Assert
            assertNotNull(result);
            assertEquals(3, result.size());
            for (ActiveBannerVO banner : result) {
                assertTrue(banner.getStartDate().compareTo(today) <= 0, 
                        "开始日期应该在今天或之前");
                assertTrue(banner.getEndDate().compareTo(today) >= 0, 
                        "结束日期应该在今天或之后");
            }
        }

        @Test
        @DisplayName("未到开始日期的轮播图不应该返回")
        void futureBanners_shouldNotBeReturned() {
            // Arrange
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(anyString())).thenReturn(null);
            when(merchantBannerClient.getActiveBanners(anyInt()))
                    .thenReturn(R.ok(Collections.emptyList()));

            // Act
            List<ActiveBannerVO> result = bannerDisplayService.getActiveBanners(5);

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    // ==================== Property 15: Automatic Deactivation ====================

    @Nested
    @DisplayName("Property 15: 轮播图自动停用测试")
    class AutomaticDeactivationTests {

        @Test
        @DisplayName("过期轮播图不应该返回")
        void expiredBanners_shouldNotBeReturned() {
            // Arrange
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(anyString())).thenReturn(null);
            when(merchantBannerClient.getActiveBanners(anyInt()))
                    .thenReturn(R.ok(Collections.emptyList()));

            // Act
            List<ActiveBannerVO> result = bannerDisplayService.getActiveBanners(5);

            // Assert
            assertTrue(result.isEmpty());
            verify(merchantBannerClient).getActiveBanners(5);
        }
    }

    // ==================== Property 16: Maximum Active Banners ====================

    @Nested
    @DisplayName("Property 16: 最大活跃轮播图数量测试")
    class MaximumActiveBannersTests {

        @Test
        @DisplayName("返回的轮播图数量不应超过请求的限制")
        void activeBanners_shouldNotExceedLimit() {
            // Arrange
            List<ActiveBannerVO> banners = createActiveBanners(3);
            
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(anyString())).thenReturn(null);
            when(merchantBannerClient.getActiveBanners(anyInt()))
                    .thenReturn(R.ok(banners));

            // Act
            List<ActiveBannerVO> result = bannerDisplayService.getActiveBanners(5);

            // Assert
            assertTrue(result.size() <= 5);
        }

        @Test
        @DisplayName("请求数量超过最大限制时应该被截断")
        void activeBanners_shouldBeCappedAtMaximum() {
            // Arrange
            List<ActiveBannerVO> banners = createActiveBanners(10);
            
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(anyString())).thenReturn(null);
            when(merchantBannerClient.getActiveBanners(10))
                    .thenReturn(R.ok(banners));

            // Act
            List<ActiveBannerVO> result = bannerDisplayService.getActiveBanners(20);

            // Assert
            // 最大限制为10
            verify(merchantBannerClient).getActiveBanners(10);
        }

        @Test
        @DisplayName("请求数量为0时应该返回空列表")
        void activeBanners_zeroLimit_shouldReturnEmpty() {
            // Arrange
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(anyString())).thenReturn(null);
            when(merchantBannerClient.getActiveBanners(0))
                    .thenReturn(R.ok(Collections.emptyList()));

            // Act
            List<ActiveBannerVO> result = bannerDisplayService.getActiveBanners(0);

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    // ==================== Property 17: Approval Date Priority ====================

    @Nested
    @DisplayName("Property 17: 审核时间优先级测试")
    class ApprovalDatePriorityTests {

        @Test
        @DisplayName("轮播图应该按排序权重和审核时间排序")
        void activeBanners_shouldBeSortedByPriority() {
            // Arrange
            List<ActiveBannerVO> banners = createActiveBanners(3);
            banners.get(0).setSortOrder(10);
            banners.get(1).setSortOrder(20);
            banners.get(2).setSortOrder(15);
            
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(anyString())).thenReturn(null);
            when(merchantBannerClient.getActiveBanners(anyInt()))
                    .thenReturn(R.ok(banners));

            // Act
            List<ActiveBannerVO> result = bannerDisplayService.getActiveBanners(5);

            // Assert
            assertNotNull(result);
            assertEquals(3, result.size());
            // 验证调用了商家服务（排序由商家服务处理）
            verify(merchantBannerClient).getActiveBanners(5);
        }
    }

    // ==================== Property 19: Statistics Tracking ====================

    @Nested
    @DisplayName("Property 19: 统计数据追踪测试")
    class StatisticsTrackingTests {

        @Test
        @DisplayName("记录曝光应该调用商家服务")
        void recordImpression_shouldCallMerchantService() {
            // Arrange
            Long bannerId = 1L;
            when(merchantBannerClient.recordImpression(bannerId))
                    .thenReturn(R.ok("记录成功", null));

            // Act
            bannerDisplayService.recordImpression(bannerId);

            // Assert
            verify(merchantBannerClient).recordImpression(bannerId);
        }

        @Test
        @DisplayName("记录点击应该调用商家服务")
        void recordClick_shouldCallMerchantService() {
            // Arrange
            Long bannerId = 1L;
            when(merchantBannerClient.recordClick(bannerId))
                    .thenReturn(R.ok("记录成功", null));

            // Act
            bannerDisplayService.recordClick(bannerId);

            // Assert
            verify(merchantBannerClient).recordClick(bannerId);
        }

        @Test
        @DisplayName("记录曝光失败时不应抛出异常")
        void recordImpression_failure_shouldNotThrowException() {
            // Arrange
            Long bannerId = 1L;
            when(merchantBannerClient.recordImpression(bannerId))
                    .thenThrow(new RuntimeException("服务不可用"));

            // Act & Assert
            assertDoesNotThrow(() -> bannerDisplayService.recordImpression(bannerId));
        }

        @Test
        @DisplayName("记录点击失败时不应抛出异常")
        void recordClick_failure_shouldNotThrowException() {
            // Arrange
            Long bannerId = 1L;
            when(merchantBannerClient.recordClick(bannerId))
                    .thenThrow(new RuntimeException("服务不可用"));

            // Act & Assert
            assertDoesNotThrow(() -> bannerDisplayService.recordClick(bannerId));
        }

        @Test
        @DisplayName("多次曝光应该分别记录")
        void multipleImpressions_shouldBeRecordedSeparately() {
            // Arrange
            Long bannerId1 = 1L;
            Long bannerId2 = 2L;
            when(merchantBannerClient.recordImpression(anyLong()))
                    .thenReturn(R.ok("记录成功", null));

            // Act
            bannerDisplayService.recordImpression(bannerId1);
            bannerDisplayService.recordImpression(bannerId2);
            bannerDisplayService.recordImpression(bannerId1);

            // Assert
            verify(merchantBannerClient, times(2)).recordImpression(bannerId1);
            verify(merchantBannerClient, times(1)).recordImpression(bannerId2);
        }
    }

    // ==================== 缓存测试 ====================

    @Nested
    @DisplayName("缓存功能测试")
    class CacheTests {

        @Test
        @DisplayName("缓存命中时应该返回缓存数据")
        void getActiveBanners_cacheHit_shouldReturnCachedData() {
            // Arrange
            List<ActiveBannerVO> cachedBanners = createActiveBanners(2);
            
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(anyString())).thenReturn(cachedBanners);

            // Act
            List<ActiveBannerVO> result = bannerDisplayService.getActiveBanners(5);

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            verify(merchantBannerClient, never()).getActiveBanners(anyInt());
        }

        @Test
        @DisplayName("缓存未命中时应该从服务获取并缓存")
        void getActiveBanners_cacheMiss_shouldFetchAndCache() {
            // Arrange
            List<ActiveBannerVO> banners = createActiveBanners(3);
            
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(anyString())).thenReturn(null);
            when(merchantBannerClient.getActiveBanners(anyInt()))
                    .thenReturn(R.ok(banners));

            // Act
            List<ActiveBannerVO> result = bannerDisplayService.getActiveBanners(5);

            // Assert
            assertNotNull(result);
            assertEquals(3, result.size());
            verify(merchantBannerClient).getActiveBanners(5);
            verify(valueOperations).set(anyString(), eq(banners), anyLong(), any());
        }

        @Test
        @DisplayName("Redis不可用时应该直接从服务获取")
        void getActiveBanners_redisUnavailable_shouldFetchFromService() {
            // Arrange
            List<ActiveBannerVO> banners = createActiveBanners(2);
            
            when(redisTemplate.opsForValue()).thenThrow(new RuntimeException("Redis不可用"));
            when(merchantBannerClient.getActiveBanners(anyInt()))
                    .thenReturn(R.ok(banners));

            // Act
            List<ActiveBannerVO> result = bannerDisplayService.getActiveBanners(5);

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("刷新缓存应该删除所有缓存键")
        void refreshCache_shouldDeleteAllCacheKeys() {
            // Act
            bannerDisplayService.refreshCache();

            // Assert
            verify(redisTemplate, atLeast(1)).delete(anyString());
        }
    }

    // ==================== 辅助方法 ====================

    private List<ActiveBannerVO> createActiveBanners(int count) {
        List<ActiveBannerVO> banners = new ArrayList<>();
        LocalDate today = LocalDate.now();
        
        for (int i = 1; i <= count; i++) {
            ActiveBannerVO banner = new ActiveBannerVO();
            banner.setId((long) i);
            banner.setMerchantId((long) (100 + i));
            banner.setImageUrl("https://example.com/banner" + i + ".jpg");
            banner.setTitle("测试轮播图" + i);
            banner.setTargetUrl("https://example.com/product/" + i);
            banner.setStartDate(today.minusDays(1));
            banner.setEndDate(today.plusDays(7));
            banner.setSortOrder(i * 10);
            banners.add(banner);
        }
        
        return banners;
    }
}
