package com.mall.merchant.service;

import com.mall.merchant.domain.dto.BannerApplicationDTO;
import com.mall.merchant.domain.entity.BannerApplication;
import com.mall.merchant.domain.vo.BannerApplicationVO;
import com.mall.merchant.domain.vo.BannerStatisticsVO;
import com.mall.merchant.repository.BannerApplicationRepository;
import com.mall.merchant.repository.BannerStatisticsRepository;
import com.mall.merchant.service.impl.BannerApplicationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 轮播图申请服务测试类
 * 包含单元测试和属性测试
 * 
 * Feature: banner-promotion
 * 
 * @author system
 * @version 1.0
 * @since 2025-12-28
 */
@ExtendWith(MockitoExtension.class)
class BannerApplicationServiceTest {

    @Mock
    private BannerApplicationRepository bannerApplicationRepository;

    @Mock
    private BannerStatisticsRepository bannerStatisticsRepository;

    @InjectMocks
    private BannerApplicationServiceImpl bannerApplicationService;

    private BannerApplicationDTO validDTO;
    private BannerApplication savedApplication;
    private Long merchantId = 1L;

    @BeforeEach
    void setUp() {
        validDTO = new BannerApplicationDTO();
        validDTO.setImageUrl("https://example.com/banner.jpg");
        validDTO.setTitle("测试轮播图");
        validDTO.setDescription("测试描述");
        validDTO.setTargetUrl("https://example.com/product");
        validDTO.setStartDate(LocalDate.now().plusDays(1));
        validDTO.setEndDate(LocalDate.now().plusDays(30));

        savedApplication = new BannerApplication();
        savedApplication.setId(1L);
        savedApplication.setMerchantId(merchantId);
        savedApplication.setImageUrl(validDTO.getImageUrl());
        savedApplication.setTitle(validDTO.getTitle());
        savedApplication.setDescription(validDTO.getDescription());
        savedApplication.setTargetUrl(validDTO.getTargetUrl());
        savedApplication.setStartDate(validDTO.getStartDate());
        savedApplication.setEndDate(validDTO.getEndDate());
        savedApplication.setStatus(BannerApplication.STATUS_PENDING);
        savedApplication.setDeleted(0);
    }

    // ==================== Property 2: Application Creation Status ====================
    
    @Nested
    @DisplayName("Property 2: Application Creation Status - 申请创建后状态必须为PENDING")
    class ApplicationCreationStatusProperty {

        /**
         * Property 2: Application Creation Status
         * For any valid banner application submission, the created application record 
         * SHALL have status "PENDING" (待审核).
         * 
         * Validates: Requirements 1.6
         */
        @ParameterizedTest(name = "创建申请 - 标题: {0}, 图片: {1}")
        @MethodSource("com.mall.merchant.service.BannerApplicationServiceTest#generateValidApplicationData")
        @DisplayName("任何有效申请创建后状态都应为PENDING")
        void createApplication_shouldAlwaysSetStatusToPending(String title, String imageUrl) {
            // Arrange
            BannerApplicationDTO dto = new BannerApplicationDTO();
            dto.setTitle(title);
            dto.setImageUrl(imageUrl);
            dto.setTargetUrl("https://example.com/target");
            dto.setStartDate(LocalDate.now().plusDays(1));
            dto.setEndDate(LocalDate.now().plusDays(30));

            ArgumentCaptor<BannerApplication> captor = ArgumentCaptor.forClass(BannerApplication.class);
            
            BannerApplication mockSaved = new BannerApplication();
            mockSaved.setId(new Random().nextLong());
            mockSaved.setStatus(BannerApplication.STATUS_PENDING);
            
            when(bannerApplicationRepository.save(any(BannerApplication.class))).thenReturn(mockSaved);

            // Act
            Long id = bannerApplicationService.createApplication(dto, merchantId);

            // Assert
            verify(bannerApplicationRepository).save(captor.capture());
            BannerApplication captured = captor.getValue();
            
            assertEquals(BannerApplication.STATUS_PENDING, captured.getStatus(),
                    "新创建的申请状态必须为PENDING");
            assertNotNull(id, "返回的申请ID不能为空");
        }

        @Test
        @DisplayName("创建申请时不应设置审核相关字段")
        void createApplication_shouldNotSetReviewFields() {
            // Arrange
            ArgumentCaptor<BannerApplication> captor = ArgumentCaptor.forClass(BannerApplication.class);
            when(bannerApplicationRepository.save(any(BannerApplication.class))).thenReturn(savedApplication);

            // Act
            bannerApplicationService.createApplication(validDTO, merchantId);

            // Assert
            verify(bannerApplicationRepository).save(captor.capture());
            BannerApplication captured = captor.getValue();
            
            assertNull(captured.getReviewTime(), "新申请不应有审核时间");
            assertNull(captured.getReviewerId(), "新申请不应有审核人ID");
            assertNull(captured.getRejectReason(), "新申请不应有拒绝原因");
        }
    }

    // ==================== Property 20: CTR Calculation ====================

    @Nested
    @DisplayName("Property 20: CTR Calculation - 点击率计算正确性")
    class CtrCalculationProperty {

        /**
         * Property 20: CTR Calculation
         * For any banner with impressions > 0, the CTR SHALL be calculated as 
         * (clicks / impressions) * 100, rounded to 2 decimal places.
         * 
         * Validates: Requirements 8.4
         */
        @ParameterizedTest(name = "曝光: {0}, 点击: {1}, 期望CTR: {2}")
        @MethodSource("com.mall.merchant.service.BannerApplicationServiceTest#generateCtrTestData")
        @DisplayName("CTR计算应符合公式: (clicks/impressions)*100")
        void getStatistics_shouldCalculateCtrCorrectly(long impressions, long clicks, double expectedCtr) {
            // Arrange
            when(bannerApplicationRepository.findByIdAndMerchantIdAndDeleted(anyLong(), anyLong(), anyInt()))
                    .thenReturn(Optional.of(savedApplication));
            
            List<Object[]> mockStats = Collections.singletonList(
                    new Object[]{impressions, clicks, 0L, 0L}
            );
            when(bannerStatisticsRepository.sumStatisticsByBannerId(anyLong())).thenReturn(mockStats);
            when(bannerStatisticsRepository.findByBannerIdAndStatDateBetweenOrderByStatDateAsc(
                    anyLong(), any(), any())).thenReturn(Collections.emptyList());

            // Act
            BannerStatisticsVO result = bannerApplicationService.getStatistics(1L, merchantId);

            // Assert
            assertEquals(impressions, result.getTotalImpressions());
            assertEquals(clicks, result.getTotalClicks());
            assertEquals(expectedCtr, result.getCtr(), 0.01,
                    String.format("CTR计算错误: 期望 %.2f, 实际 %.2f", expectedCtr, result.getCtr()));
        }

        @Test
        @DisplayName("曝光为0时CTR应为0")
        void getStatistics_shouldReturnZeroCtrWhenNoImpressions() {
            // Arrange
            when(bannerApplicationRepository.findByIdAndMerchantIdAndDeleted(anyLong(), anyLong(), anyInt()))
                    .thenReturn(Optional.of(savedApplication));
            
            List<Object[]> mockStats = Collections.singletonList(new Object[]{0L, 0L, 0L, 0L});
            when(bannerStatisticsRepository.sumStatisticsByBannerId(anyLong())).thenReturn(mockStats);
            when(bannerStatisticsRepository.findByBannerIdAndStatDateBetweenOrderByStatDateAsc(
                    anyLong(), any(), any())).thenReturn(Collections.emptyList());

            // Act
            BannerStatisticsVO result = bannerApplicationService.getStatistics(1L, merchantId);

            // Assert
            assertEquals(0.0, result.getCtr(), "曝光为0时CTR应为0");
        }
    }

    // ==================== 单元测试 ====================

    @Nested
    @DisplayName("申请创建测试")
    class CreateApplicationTests {

        @Test
        @DisplayName("成功创建申请")
        void createApplication_success() {
            // Arrange
            when(bannerApplicationRepository.save(any(BannerApplication.class))).thenReturn(savedApplication);

            // Act
            Long id = bannerApplicationService.createApplication(validDTO, merchantId);

            // Assert
            assertNotNull(id);
            assertEquals(1L, id);
            verify(bannerApplicationRepository, times(1)).save(any(BannerApplication.class));
        }

        @Test
        @DisplayName("创建申请时应正确设置商家ID")
        void createApplication_shouldSetMerchantId() {
            // Arrange
            ArgumentCaptor<BannerApplication> captor = ArgumentCaptor.forClass(BannerApplication.class);
            when(bannerApplicationRepository.save(any(BannerApplication.class))).thenReturn(savedApplication);

            // Act
            bannerApplicationService.createApplication(validDTO, merchantId);

            // Assert
            verify(bannerApplicationRepository).save(captor.capture());
            assertEquals(merchantId, captor.getValue().getMerchantId());
        }
    }

    @Nested
    @DisplayName("申请取消测试")
    class CancelApplicationTests {

        @Test
        @DisplayName("成功取消待审核申请")
        void cancelApplication_success() {
            // Arrange
            when(bannerApplicationRepository.cancelApplication(anyLong(), anyLong(), anyString()))
                    .thenReturn(1);

            // Act & Assert
            assertDoesNotThrow(() -> bannerApplicationService.cancelApplication(1L, merchantId));
            verify(bannerApplicationRepository).cancelApplication(1L, merchantId, BannerApplication.STATUS_CANCELLED);
        }

        @Test
        @DisplayName("取消不存在的申请应抛出异常")
        void cancelApplication_notFound_shouldThrowException() {
            // Arrange
            when(bannerApplicationRepository.cancelApplication(anyLong(), anyLong(), anyString()))
                    .thenReturn(0);

            // Act & Assert
            assertThrows(RuntimeException.class, 
                    () -> bannerApplicationService.cancelApplication(1L, merchantId));
        }
    }

    @Nested
    @DisplayName("申请更新测试")
    class UpdateApplicationTests {

        @Test
        @DisplayName("成功更新待审核申请")
        void updateApplication_pending_success() {
            // Arrange
            savedApplication.setStatus(BannerApplication.STATUS_PENDING);
            when(bannerApplicationRepository.findByIdAndMerchantIdAndDeleted(anyLong(), anyLong(), anyInt()))
                    .thenReturn(Optional.of(savedApplication));
            when(bannerApplicationRepository.save(any(BannerApplication.class))).thenReturn(savedApplication);

            // Act & Assert
            assertDoesNotThrow(() -> bannerApplicationService.updateApplication(1L, validDTO, merchantId));
        }

        @Test
        @DisplayName("更新已拒绝申请后状态应变为待审核")
        void updateApplication_rejected_shouldChangeToPending() {
            // Arrange
            savedApplication.setStatus(BannerApplication.STATUS_REJECTED);
            savedApplication.setRejectReason("测试拒绝原因");
            
            ArgumentCaptor<BannerApplication> captor = ArgumentCaptor.forClass(BannerApplication.class);
            when(bannerApplicationRepository.findByIdAndMerchantIdAndDeleted(anyLong(), anyLong(), anyInt()))
                    .thenReturn(Optional.of(savedApplication));
            when(bannerApplicationRepository.save(any(BannerApplication.class))).thenReturn(savedApplication);

            // Act
            bannerApplicationService.updateApplication(1L, validDTO, merchantId);

            // Assert
            verify(bannerApplicationRepository).save(captor.capture());
            assertEquals(BannerApplication.STATUS_PENDING, captor.getValue().getStatus());
            assertNull(captor.getValue().getRejectReason());
        }

        @Test
        @DisplayName("更新已通过申请应抛出异常")
        void updateApplication_approved_shouldThrowException() {
            // Arrange
            savedApplication.setStatus(BannerApplication.STATUS_APPROVED);
            when(bannerApplicationRepository.findByIdAndMerchantIdAndDeleted(anyLong(), anyLong(), anyInt()))
                    .thenReturn(Optional.of(savedApplication));

            // Act & Assert
            assertThrows(RuntimeException.class, 
                    () -> bannerApplicationService.updateApplication(1L, validDTO, merchantId));
        }
    }

    @Nested
    @DisplayName("申请列表查询测试")
    class GetApplicationListTests {

        @Test
        @DisplayName("查询所有申请列表")
        void getApplicationList_all() {
            // Arrange
            Page<BannerApplication> mockPage = new PageImpl<>(Collections.singletonList(savedApplication));
            when(bannerApplicationRepository.findByMerchantIdAndDeletedOrderByCreateTimeDesc(
                    anyLong(), anyInt(), any(Pageable.class))).thenReturn(mockPage);

            // Act
            Page<BannerApplicationVO> result = bannerApplicationService.getApplicationList(merchantId, null, 0, 10);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
        }

        @Test
        @DisplayName("按状态筛选申请列表")
        void getApplicationList_byStatus() {
            // Arrange
            Page<BannerApplication> mockPage = new PageImpl<>(Collections.singletonList(savedApplication));
            when(bannerApplicationRepository.findByMerchantIdAndStatusAndDeletedOrderByCreateTimeDesc(
                    anyLong(), anyString(), anyInt(), any(Pageable.class))).thenReturn(mockPage);

            // Act
            Page<BannerApplicationVO> result = bannerApplicationService.getApplicationList(
                    merchantId, BannerApplication.STATUS_PENDING, 0, 10);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
        }
    }

    // ==================== 测试数据生成方法 ====================

    /**
     * 生成有效的申请数据用于属性测试
     * Property 2: Application Creation Status
     */
    static Stream<Arguments> generateValidApplicationData() {
        return Stream.of(
                Arguments.of("促销活动", "https://example.com/promo.jpg"),
                Arguments.of("新品上市", "https://example.com/new.png"),
                Arguments.of("限时特惠", "https://example.com/sale.gif"),
                Arguments.of("品牌推广", "https://example.com/brand.jpg"),
                Arguments.of("节日活动", "https://example.com/holiday.png"),
                Arguments.of("会员专享", "https://example.com/vip.jpg"),
                Arguments.of("清仓甩卖", "https://example.com/clearance.png"),
                Arguments.of("新店开业", "https://example.com/opening.jpg"),
                Arguments.of("周年庆典", "https://example.com/anniversary.png"),
                Arguments.of("双十一", "https://example.com/1111.jpg")
        );
    }

    /**
     * 生成CTR测试数据
     * Property 20: CTR Calculation
     */
    static Stream<Arguments> generateCtrTestData() {
        return Stream.of(
                // impressions, clicks, expectedCtr
                Arguments.of(100L, 5L, 5.0),
                Arguments.of(1000L, 25L, 2.5),
                Arguments.of(500L, 50L, 10.0),
                Arguments.of(200L, 1L, 0.5),
                Arguments.of(10000L, 333L, 3.33),
                Arguments.of(1L, 1L, 100.0),
                Arguments.of(100L, 0L, 0.0),
                Arguments.of(999L, 99L, 9.91),
                Arguments.of(50L, 25L, 50.0),
                Arguments.of(1000000L, 10000L, 1.0)
        );
    }
}
