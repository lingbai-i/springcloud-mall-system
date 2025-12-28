package com.mall.product.controller;

import com.mall.product.domain.vo.ActiveBannerVO;
import com.mall.product.service.BannerDisplayService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 轮播图展示控制器测试类
 * 
 * Feature: banner-promotion
 * 
 * @author system
 * @version 1.0
 * @since 2025-12-28
 */
@ExtendWith(MockitoExtension.class)
class BannerControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BannerDisplayService bannerDisplayService;

    @InjectMocks
    private BannerController bannerController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(bannerController).build();
    }

    // ==================== 获取活跃轮播图API测试 ====================

    @Nested
    @DisplayName("获取活跃轮播图API测试")
    class GetActiveBannersTests {

        @Test
        @DisplayName("成功获取活跃轮播图列表")
        void getActiveBanners_success() throws Exception {
            // Arrange
            List<ActiveBannerVO> banners = createActiveBanners(3);
            when(bannerDisplayService.getActiveBanners(5)).thenReturn(banners);

            // Act & Assert
            mockMvc.perform(get("/api/banner/active")
                            .param("limit", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(3));

            verify(bannerDisplayService).getActiveBanners(5);
        }

        @Test
        @DisplayName("使用默认限制参数")
        void getActiveBanners_defaultLimit() throws Exception {
            // Arrange
            List<ActiveBannerVO> banners = createActiveBanners(5);
            when(bannerDisplayService.getActiveBanners(5)).thenReturn(banners);

            // Act & Assert
            mockMvc.perform(get("/api/banner/active"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(bannerDisplayService).getActiveBanners(5);
        }

        @Test
        @DisplayName("无活跃轮播图时返回空列表")
        void getActiveBanners_empty() throws Exception {
            // Arrange
            when(bannerDisplayService.getActiveBanners(anyInt()))
                    .thenReturn(Collections.emptyList());

            // Act & Assert
            mockMvc.perform(get("/api/banner/active"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("返回的轮播图包含必要字段")
        void getActiveBanners_containsRequiredFields() throws Exception {
            // Arrange
            List<ActiveBannerVO> banners = createActiveBanners(1);
            when(bannerDisplayService.getActiveBanners(anyInt())).thenReturn(banners);

            // Act & Assert
            mockMvc.perform(get("/api/banner/active"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].id").exists())
                    .andExpect(jsonPath("$.data[0].imageUrl").exists())
                    .andExpect(jsonPath("$.data[0].title").exists())
                    .andExpect(jsonPath("$.data[0].targetUrl").exists());
        }
    }

    // ==================== 记录曝光API测试 ====================

    @Nested
    @DisplayName("记录曝光API测试")
    class RecordImpressionTests {

        @Test
        @DisplayName("成功记录轮播图曝光")
        void recordImpression_success() throws Exception {
            // Arrange
            doNothing().when(bannerDisplayService).recordImpression(1L);

            // Act & Assert
            mockMvc.perform(post("/api/banner/1/impression"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("记录成功"));

            verify(bannerDisplayService).recordImpression(1L);
        }

        @Test
        @DisplayName("记录不同轮播图的曝光")
        void recordImpression_differentBanners() throws Exception {
            // Arrange
            doNothing().when(bannerDisplayService).recordImpression(anyLong());

            // Act & Assert
            mockMvc.perform(post("/api/banner/1/impression"))
                    .andExpect(status().isOk());
            mockMvc.perform(post("/api/banner/2/impression"))
                    .andExpect(status().isOk());

            verify(bannerDisplayService).recordImpression(1L);
            verify(bannerDisplayService).recordImpression(2L);
        }
    }

    // ==================== 记录点击API测试 ====================

    @Nested
    @DisplayName("记录点击API测试")
    class RecordClickTests {

        @Test
        @DisplayName("成功记录轮播图点击")
        void recordClick_success() throws Exception {
            // Arrange
            doNothing().when(bannerDisplayService).recordClick(1L);

            // Act & Assert
            mockMvc.perform(post("/api/banner/1/click"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("记录成功"));

            verify(bannerDisplayService).recordClick(1L);
        }

        @Test
        @DisplayName("记录不同轮播图的点击")
        void recordClick_differentBanners() throws Exception {
            // Arrange
            doNothing().when(bannerDisplayService).recordClick(anyLong());

            // Act & Assert
            mockMvc.perform(post("/api/banner/1/click"))
                    .andExpect(status().isOk());
            mockMvc.perform(post("/api/banner/2/click"))
                    .andExpect(status().isOk());

            verify(bannerDisplayService).recordClick(1L);
            verify(bannerDisplayService).recordClick(2L);
        }
    }

    // ==================== 刷新缓存API测试 ====================

    @Nested
    @DisplayName("刷新缓存API测试")
    class RefreshCacheTests {

        @Test
        @DisplayName("成功刷新轮播图缓存")
        void refreshCache_success() throws Exception {
            // Arrange
            doNothing().when(bannerDisplayService).refreshCache();

            // Act & Assert
            mockMvc.perform(post("/api/banner/refresh-cache"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("缓存已刷新"));

            verify(bannerDisplayService).refreshCache();
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
