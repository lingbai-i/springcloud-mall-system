package com.mall.admin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mall.admin.domain.dto.BannerRejectDTO;
import com.mall.admin.domain.vo.BannerReviewDetailVO;
import com.mall.admin.domain.vo.BannerReviewVO;
import com.mall.admin.service.BannerReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 轮播图审核控制器测试类
 * 
 * Feature: banner-promotion
 * 
 * @author system
 * @version 1.0
 * @since 2025-12-28
 */
@ExtendWith(MockitoExtension.class)
class BannerReviewControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BannerReviewService bannerReviewService;

    @InjectMocks
    private BannerReviewController bannerReviewController;

    private ObjectMapper objectMapper;
    private BannerReviewVO sampleReviewVO;
    private BannerReviewDetailVO sampleDetailVO;
    private Long adminId = 1L;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(bannerReviewController)
                .setControllerAdvice(new TestExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        // 初始化测试数据
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

        sampleDetailVO = new BannerReviewDetailVO();
        sampleDetailVO.setId(1L);
        sampleDetailVO.setMerchantId(100L);
        sampleDetailVO.setMerchantName("测试商家");
        sampleDetailVO.setImageUrl("https://example.com/banner.jpg");
        sampleDetailVO.setTitle("测试轮播图");
        sampleDetailVO.setDescription("测试描述");
        sampleDetailVO.setTargetUrl("https://example.com/product");
        sampleDetailVO.setStartDate(LocalDate.now().plusDays(1));
        sampleDetailVO.setEndDate(LocalDate.now().plusDays(30));
        sampleDetailVO.setStatus("PENDING");
        sampleDetailVO.setStatusText("待审核");
        sampleDetailVO.setSubmitTime(LocalDateTime.now());
    }

    // ==================== 待审核列表API测试 ====================

    @Nested
    @DisplayName("待审核列表API测试")
    class GetPendingListTests {

        @Test
        @DisplayName("成功获取待审核列表")
        void getPendingList_success() throws Exception {
            // Arrange
            List<BannerReviewVO> list = Collections.singletonList(sampleReviewVO);
            Page<BannerReviewVO> mockPage = new PageImpl<>(list, PageRequest.of(0, 10), 1);
            when(bannerReviewService.getPendingList(anyInt(), anyInt())).thenReturn(mockPage);

            // Act & Assert
            mockMvc.perform(get("/api/admin/banner/pending")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.totalElements").value(1));

            verify(bannerReviewService).getPendingList(0, 10);
        }

        @Test
        @DisplayName("使用默认分页参数")
        void getPendingList_defaultParams() throws Exception {
            // Arrange
            Page<BannerReviewVO> mockPage = new PageImpl<>(Collections.emptyList());
            when(bannerReviewService.getPendingList(anyInt(), anyInt())).thenReturn(mockPage);

            // Act & Assert
            mockMvc.perform(get("/api/admin/banner/pending"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(bannerReviewService).getPendingList(0, 10);
        }
    }

    // ==================== 所有申请列表API测试 ====================

    @Nested
    @DisplayName("所有申请列表API测试")
    class GetAllApplicationsTests {

        @Test
        @DisplayName("成功获取所有申请列表")
        void getAllApplications_success() throws Exception {
            // Arrange
            List<BannerReviewVO> list = Collections.singletonList(sampleReviewVO);
            Page<BannerReviewVO> mockPage = new PageImpl<>(list, PageRequest.of(0, 10), 1);
            when(bannerReviewService.getAllApplications(any(), any(), any(), anyInt(), anyInt()))
                    .thenReturn(mockPage);

            // Act & Assert
            mockMvc.perform(get("/api/admin/banner/list")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.content").isArray());

            verify(bannerReviewService).getAllApplications(null, null, null, 0, 10);
        }

        @Test
        @DisplayName("按状态筛选申请列表")
        void getAllApplications_byStatus() throws Exception {
            // Arrange
            Page<BannerReviewVO> mockPage = new PageImpl<>(Collections.emptyList());
            when(bannerReviewService.getAllApplications(eq("PENDING"), any(), any(), anyInt(), anyInt()))
                    .thenReturn(mockPage);

            // Act & Assert
            mockMvc.perform(get("/api/admin/banner/list")
                            .param("status", "PENDING")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(bannerReviewService).getAllApplications(eq("PENDING"), any(), any(), eq(0), eq(10));
        }
    }

    // ==================== 申请详情API测试 ====================

    @Nested
    @DisplayName("申请详情API测试")
    class GetApplicationDetailTests {

        @Test
        @DisplayName("成功获取申请详情")
        void getApplicationDetail_success() throws Exception {
            // Arrange
            when(bannerReviewService.getApplicationDetail(1L)).thenReturn(sampleDetailVO);

            // Act & Assert
            mockMvc.perform(get("/api/admin/banner/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.title").value("测试轮播图"));

            verify(bannerReviewService).getApplicationDetail(1L);
        }

        @Test
        @DisplayName("申请不存在时返回错误")
        void getApplicationDetail_notFound() throws Exception {
            // Arrange
            when(bannerReviewService.getApplicationDetail(999L))
                    .thenThrow(new RuntimeException("申请不存在"));

            // Act & Assert
            mockMvc.perform(get("/api/admin/banner/999"))
                    .andExpect(status().isInternalServerError());
        }
    }

    // ==================== 审核通过API测试 ====================

    @Nested
    @DisplayName("审核通过API测试")
    class ApproveApplicationTests {

        @Test
        @DisplayName("成功审核通过申请")
        void approveApplication_success() throws Exception {
            // Arrange
            doNothing().when(bannerReviewService).approveApplication(1L, adminId);

            // Act & Assert
            mockMvc.perform(post("/api/admin/banner/1/approve")
                            .header("X-Admin-Id", adminId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("审核通过成功"));

            verify(bannerReviewService).approveApplication(1L, adminId);
        }

        @Test
        @DisplayName("缺少管理员ID时返回错误")
        void approveApplication_missingAdminId() throws Exception {
            // Act & Assert
            mockMvc.perform(post("/api/admin/banner/1/approve"))
                    .andExpect(status().isBadRequest());

            verify(bannerReviewService, never()).approveApplication(anyLong(), anyLong());
        }
    }

    // ==================== 审核拒绝API测试 ====================

    @Nested
    @DisplayName("审核拒绝API测试")
    class RejectApplicationTests {

        @Test
        @DisplayName("成功审核拒绝申请")
        void rejectApplication_success() throws Exception {
            // Arrange
            BannerRejectDTO dto = new BannerRejectDTO();
            dto.setReason("图片质量不符合要求");
            
            doNothing().when(bannerReviewService).rejectApplication(1L, adminId, dto.getReason());

            // Act & Assert
            mockMvc.perform(post("/api/admin/banner/1/reject")
                            .header("X-Admin-Id", adminId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("审核拒绝成功"));

            verify(bannerReviewService).rejectApplication(1L, adminId, dto.getReason());
        }

        @Test
        @DisplayName("拒绝原因为空时返回验证错误")
        void rejectApplication_emptyReason() throws Exception {
            // Arrange
            BannerRejectDTO dto = new BannerRejectDTO();
            dto.setReason("");

            // Act & Assert
            mockMvc.perform(post("/api/admin/banner/1/reject")
                            .header("X-Admin-Id", adminId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest());

            verify(bannerReviewService, never()).rejectApplication(anyLong(), anyLong(), anyString());
        }

        @Test
        @DisplayName("缺少管理员ID时返回错误")
        void rejectApplication_missingAdminId() throws Exception {
            // Arrange
            BannerRejectDTO dto = new BannerRejectDTO();
            dto.setReason("测试原因");

            // Act & Assert
            mockMvc.perform(post("/api/admin/banner/1/reject")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest());

            verify(bannerReviewService, never()).rejectApplication(anyLong(), anyLong(), anyString());
        }
    }

    // ==================== 待审核数量API测试 ====================

    @Nested
    @DisplayName("待审核数量API测试")
    class GetPendingCountTests {

        @Test
        @DisplayName("成功获取待审核数量")
        void getPendingCount_success() throws Exception {
            // Arrange
            when(bannerReviewService.getPendingCount()).thenReturn(5L);

            // Act & Assert
            mockMvc.perform(get("/api/admin/banner/pending-count"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(5));

            verify(bannerReviewService).getPendingCount();
        }

        @Test
        @DisplayName("无待审核申请时返回0")
        void getPendingCount_zero() throws Exception {
            // Arrange
            when(bannerReviewService.getPendingCount()).thenReturn(0L);

            // Act & Assert
            mockMvc.perform(get("/api/admin/banner/pending-count"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(0));
        }
    }

    /**
     * 测试用异常处理器
     */
    @ControllerAdvice
    static class TestExceptionHandler {
        @ExceptionHandler(RuntimeException.class)
        public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
}
