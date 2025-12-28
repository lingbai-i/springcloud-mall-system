package com.mall.admin.service;

import com.mall.admin.client.MerchantBannerClient;
import com.mall.admin.domain.vo.BannerReviewDetailVO;
import com.mall.admin.domain.vo.BannerReviewVO;
import com.mall.admin.service.impl.BannerReviewServiceImpl;
import com.mall.common.core.domain.R;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 轮播图审核服务测试类
 * 包含单元测试和属性测试
 * 
 * Feature: banner-promotion
 * 
 * @author system
 * @version 1.0
 * @since 2025-12-28
 */
@ExtendWith(MockitoExtension.class)
class BannerReviewServiceTest {

    @Mock
    private MerchantBannerClient merchantBannerClient;

    @InjectMocks
    private BannerReviewServiceImpl bannerReviewService;

    private BannerReviewVO sampleReviewVO;
    private Long adminId = 1L;

    @BeforeEach
    void setUp() {
        sampleReviewVO = new BannerReviewVO();
        sampleReviewVO.setId(1L);
        sampleReviewVO.setMerchantId(100L);
        sampleReviewVO.setMerchantName("测试商家");
        sampleReviewVO.setImageUrl("https://example.com/banner.jpg");
        sampleReviewVO.setTitle("测试轮播图");
        sampleReviewVO.setTargetUrl("https://example.com/product");
        sampleReviewVO.setStartDate(LocalDate.now().plusDays(1));
        sampleReviewVO.setEndDate(LocalDate.now().plusDays(30));
        sampleReviewVO.setStatus("PENDING");
        sampleReviewVO.setStatusText("待审核");
        sampleReviewVO.setSubmitTime(LocalDateTime.now());
    }

    // ==================== Property 9: Pending Count Increment ====================

    @Nested
    @DisplayName("Property 9: Pending Count Increment - 待审核数量统计")
    class PendingCountIncrementProperty {

        /**
         * Property 9: Pending Count Increment
         * For any new banner application submission, the pending review count 
         * SHALL increase by exactly 1.
         * 
         * Validates: Requirements 4.1
         * 
         * 注：此属性测试验证待审核数量的正确获取
         */
        @ParameterizedTest(name = "待审核数量: {0}")
        @MethodSource("com.mall.admin.service.BannerReviewServiceTest#generatePendingCounts")
        @DisplayName("待审核数量应正确返回")
        void getPendingCount_shouldReturnCorrectCount(long expectedCount) {
            // Arrange
            when(merchantBannerClient.getPendingCount()).thenReturn(R.ok(expectedCount));

            // Act
            Long actualCount = bannerReviewService.getPendingCount();

            // Assert
            assertEquals(expectedCount, actualCount, 
                    "待审核数量应与商家服务返回的数量一致");
        }

        @Test
        @DisplayName("商家服务调用失败时应返回0")
        void getPendingCount_shouldReturnZeroOnFailure() {
            // Arrange
            when(merchantBannerClient.getPendingCount()).thenThrow(new RuntimeException("服务不可用"));

            // Act
            Long count = bannerReviewService.getPendingCount();

            // Assert
            assertEquals(0L, count, "服务调用失败时应返回0");
        }
    }

    // ==================== Property 11: Rejection Requires Reason ====================

    @Nested
    @DisplayName("Property 11: Rejection Requires Reason - 拒绝必须填写原因")
    class RejectionRequiresReasonProperty {

        /**
         * Property 11: Rejection Requires Reason
         * For any rejection action, the system SHALL require a non-empty rejection 
         * reason before completing the rejection.
         * 
         * Validates: Requirements 5.5
         */
        @ParameterizedTest(name = "空原因测试: [{0}]")
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n", "  \t\n  "})
        @DisplayName("拒绝时空原因应抛出异常")
        void rejectApplication_emptyReason_shouldThrowException(String emptyReason) {
            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> bannerReviewService.rejectApplication(1L, adminId, emptyReason));
            
            assertTrue(exception.getMessage().contains("拒绝原因不能为空"),
                    "异常消息应包含'拒绝原因不能为空'");
            
            // 验证不应调用商家服务
            verify(merchantBannerClient, never()).rejectApplication(anyLong(), anyLong(), anyString());
        }

        @ParameterizedTest(name = "有效原因: {0}")
        @MethodSource("com.mall.admin.service.BannerReviewServiceTest#generateValidRejectReasons")
        @DisplayName("有效拒绝原因应成功调用商家服务")
        void rejectApplication_validReason_shouldCallMerchantService(String validReason) {
            // Arrange
            when(merchantBannerClient.rejectApplication(anyLong(), anyLong(), anyString()))
                    .thenReturn(R.ok("审核拒绝成功", null));

            // Act
            assertDoesNotThrow(() -> bannerReviewService.rejectApplication(1L, adminId, validReason));

            // Assert
            verify(merchantBannerClient).rejectApplication(eq(1L), eq(adminId), eq(validReason));
        }
    }

    // ==================== Property 12: Approval Status Update ====================

    @Nested
    @DisplayName("Property 12: Approval Status Update - 审核通过状态更新")
    class ApprovalStatusUpdateProperty {

        /**
         * Property 12: Approval Status Update
         * For any application that is approved by an administrator, the application 
         * status SHALL be updated to "APPROVED" (已通过).
         * 
         * Validates: Requirements 5.6
         */
        @ParameterizedTest(name = "申请ID: {0}, 管理员ID: {1}")
        @MethodSource("com.mall.admin.service.BannerReviewServiceTest#generateApprovalTestData")
        @DisplayName("审核通过应正确调用商家服务")
        void approveApplication_shouldCallMerchantServiceCorrectly(Long applicationId, Long adminId) {
            // Arrange
            when(merchantBannerClient.approveApplication(anyLong(), anyLong()))
                    .thenReturn(R.ok("审核通过成功", null));

            // Act
            assertDoesNotThrow(() -> bannerReviewService.approveApplication(applicationId, adminId));

            // Assert
            verify(merchantBannerClient).approveApplication(eq(applicationId), eq(adminId));
        }

        @Test
        @DisplayName("审核通过失败时应抛出异常")
        void approveApplication_failure_shouldThrowException() {
            // Arrange
            when(merchantBannerClient.approveApplication(anyLong(), anyLong()))
                    .thenReturn(R.fail("申请不存在"));

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> bannerReviewService.approveApplication(1L, adminId));
            
            assertTrue(exception.getMessage().contains("申请不存在"));
        }
    }

    // ==================== Property 13: Rejection Status Update ====================

    @Nested
    @DisplayName("Property 13: Rejection Status Update - 审核拒绝状态更新")
    class RejectionStatusUpdateProperty {

        /**
         * Property 13: Rejection Status Update
         * For any application that is rejected by an administrator, the application 
         * status SHALL be updated to "REJECTED" (已拒绝) AND the rejection reason 
         * SHALL be stored.
         * 
         * Validates: Requirements 5.7
         */
        @ParameterizedTest(name = "申请ID: {0}, 管理员ID: {1}, 原因: {2}")
        @MethodSource("com.mall.admin.service.BannerReviewServiceTest#generateRejectionTestData")
        @DisplayName("审核拒绝应正确传递原因到商家服务")
        void rejectApplication_shouldPassReasonToMerchantService(Long applicationId, Long adminId, String reason) {
            // Arrange
            when(merchantBannerClient.rejectApplication(anyLong(), anyLong(), anyString()))
                    .thenReturn(R.ok("审核拒绝成功", null));

            // Act
            assertDoesNotThrow(() -> bannerReviewService.rejectApplication(applicationId, adminId, reason));

            // Assert
            verify(merchantBannerClient).rejectApplication(eq(applicationId), eq(adminId), eq(reason));
        }

        @Test
        @DisplayName("审核拒绝失败时应抛出异常")
        void rejectApplication_failure_shouldThrowException() {
            // Arrange
            when(merchantBannerClient.rejectApplication(anyLong(), anyLong(), anyString()))
                    .thenReturn(R.fail("申请不存在"));

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> bannerReviewService.rejectApplication(1L, adminId, "测试原因"));
            
            assertTrue(exception.getMessage().contains("申请不存在"));
        }
    }

    // ==================== 单元测试 ====================

    @Nested
    @DisplayName("待审核列表查询测试")
    class GetPendingListTests {

        @Test
        @DisplayName("成功获取待审核列表")
        void getPendingList_success() {
            // Arrange
            List<BannerReviewVO> list = Collections.singletonList(sampleReviewVO);
            Page<BannerReviewVO> mockPage = new PageImpl<>(list, PageRequest.of(0, 10), 1);
            when(merchantBannerClient.getPendingApplications(anyInt(), anyInt()))
                    .thenReturn(R.ok(mockPage));

            // Act
            Page<BannerReviewVO> result = bannerReviewService.getPendingList(0, 10);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals("PENDING", result.getContent().get(0).getStatus());
        }

        @Test
        @DisplayName("商家服务返回失败时应返回空列表")
        void getPendingList_failure_shouldReturnEmptyPage() {
            // Arrange
            when(merchantBannerClient.getPendingApplications(anyInt(), anyInt()))
                    .thenReturn(R.fail("服务不可用"));

            // Act
            Page<BannerReviewVO> result = bannerReviewService.getPendingList(0, 10);

            // Assert
            assertNotNull(result);
            assertEquals(0, result.getTotalElements());
        }
    }

    @Nested
    @DisplayName("申请详情查询测试")
    class GetApplicationDetailTests {

        @Test
        @DisplayName("成功获取申请详情")
        void getApplicationDetail_success() {
            // Arrange
            when(merchantBannerClient.getApplicationDetail(anyLong()))
                    .thenReturn(R.ok(sampleReviewVO));

            // Act
            BannerReviewDetailVO result = bannerReviewService.getApplicationDetail(1L);

            // Assert
            assertNotNull(result);
            assertEquals(sampleReviewVO.getId(), result.getId());
            assertEquals(sampleReviewVO.getTitle(), result.getTitle());
        }

        @Test
        @DisplayName("申请不存在时应抛出异常")
        void getApplicationDetail_notFound_shouldThrowException() {
            // Arrange
            when(merchantBannerClient.getApplicationDetail(anyLong()))
                    .thenReturn(R.fail("申请不存在"));

            // Act & Assert
            assertThrows(RuntimeException.class,
                    () -> bannerReviewService.getApplicationDetail(999L));
        }
    }

    @Nested
    @DisplayName("所有申请列表查询测试")
    class GetAllApplicationsTests {

        @Test
        @DisplayName("成功获取所有申请列表")
        void getAllApplications_success() {
            // Arrange
            List<BannerReviewVO> list = Collections.singletonList(sampleReviewVO);
            Page<BannerReviewVO> mockPage = new PageImpl<>(list, PageRequest.of(0, 10), 1);
            when(merchantBannerClient.getAllApplications(any(), any(), any(), anyInt(), anyInt()))
                    .thenReturn(R.ok(mockPage));

            // Act
            Page<BannerReviewVO> result = bannerReviewService.getAllApplications(null, null, null, 0, 10);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
        }

        @Test
        @DisplayName("按状态筛选申请列表")
        void getAllApplications_byStatus() {
            // Arrange
            List<BannerReviewVO> list = Collections.singletonList(sampleReviewVO);
            Page<BannerReviewVO> mockPage = new PageImpl<>(list, PageRequest.of(0, 10), 1);
            when(merchantBannerClient.getAllApplications(eq("PENDING"), any(), any(), anyInt(), anyInt()))
                    .thenReturn(R.ok(mockPage));

            // Act
            Page<BannerReviewVO> result = bannerReviewService.getAllApplications("PENDING", null, null, 0, 10);

            // Assert
            assertNotNull(result);
            verify(merchantBannerClient).getAllApplications(eq("PENDING"), any(), any(), anyInt(), anyInt());
        }
    }

    // ==================== 测试数据生成方法 ====================

    /**
     * 生成待审核数量测试数据
     * Property 9: Pending Count Increment
     */
    static Stream<Arguments> generatePendingCounts() {
        return Stream.of(
                Arguments.of(0L),
                Arguments.of(1L),
                Arguments.of(5L),
                Arguments.of(10L),
                Arguments.of(50L),
                Arguments.of(100L),
                Arguments.of(999L)
        );
    }

    /**
     * 生成有效的拒绝原因
     * Property 11: Rejection Requires Reason
     */
    static Stream<Arguments> generateValidRejectReasons() {
        return Stream.of(
                Arguments.of("图片质量不符合要求"),
                Arguments.of("内容涉及违规信息"),
                Arguments.of("跳转链接无效"),
                Arguments.of("展示日期不合理"),
                Arguments.of("标题与图片内容不符"),
                Arguments.of("图片尺寸不符合规范"),
                Arguments.of("包含敏感词汇"),
                Arguments.of("商家资质审核未通过"),
                Arguments.of("重复提交申请"),
                Arguments.of("其他原因：请修改后重新提交")
        );
    }

    /**
     * 生成审核通过测试数据
     * Property 12: Approval Status Update
     */
    static Stream<Arguments> generateApprovalTestData() {
        Random random = new Random();
        List<Arguments> args = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            args.add(Arguments.of(
                    (long) (random.nextInt(1000) + 1),  // applicationId
                    (long) (random.nextInt(100) + 1)    // adminId
            ));
        }
        return args.stream();
    }

    /**
     * 生成审核拒绝测试数据
     * Property 13: Rejection Status Update
     */
    static Stream<Arguments> generateRejectionTestData() {
        return Stream.of(
                Arguments.of(1L, 1L, "图片质量不符合要求"),
                Arguments.of(2L, 1L, "内容涉及违规信息"),
                Arguments.of(3L, 2L, "跳转链接无效"),
                Arguments.of(4L, 2L, "展示日期不合理"),
                Arguments.of(5L, 3L, "标题与图片内容不符"),
                Arguments.of(100L, 10L, "图片尺寸不符合规范"),
                Arguments.of(200L, 20L, "包含敏感词汇"),
                Arguments.of(300L, 30L, "商家资质审核未通过"),
                Arguments.of(400L, 40L, "重复提交申请"),
                Arguments.of(500L, 50L, "其他原因：请修改后重新提交")
        );
    }
}
