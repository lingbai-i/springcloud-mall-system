package com.mall.merchant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mall.merchant.domain.dto.BannerApplicationDTO;
import com.mall.merchant.domain.vo.BannerApplicationVO;
import com.mall.merchant.domain.vo.BannerStatisticsVO;
import com.mall.merchant.service.BannerApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 轮播图申请控制器单元测试
 * 使用MockMvc standalone模式测试控制器
 * 
 * @author system
 * @version 1.0
 * @since 2025-12-28
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("轮播图申请控制器测试")
public class BannerApplicationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BannerApplicationService bannerApplicationService;

    @InjectMocks
    private BannerApplicationController bannerApplicationController;

    private ObjectMapper objectMapper;
    private BannerApplicationDTO testDto;
    private BannerApplicationVO testVo;
    private BannerStatisticsVO testStatisticsVo;
    private static final Long TEST_MERCHANT_ID = 1001L;
    private static final Long TEST_APPLICATION_ID = 1L;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(bannerApplicationController).build();
        
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // 准备测试DTO
        testDto = new BannerApplicationDTO();
        testDto.setImageUrl("https://example.com/banner.jpg");
        testDto.setTitle("测试轮播图");
        testDto.setDescription("这是一个测试轮播图描述");
        testDto.setTargetUrl("https://example.com/product/1");
        testDto.setStartDate(LocalDate.now().plusDays(1));
        testDto.setEndDate(LocalDate.now().plusDays(30));

        // 准备测试VO
        testVo = new BannerApplicationVO();
        testVo.setId(TEST_APPLICATION_ID);
        testVo.setMerchantId(TEST_MERCHANT_ID);
        testVo.setImageUrl("https://example.com/banner.jpg");
        testVo.setTitle("测试轮播图");
        testVo.setDescription("这是一个测试轮播图描述");
        testVo.setTargetUrl("https://example.com/product/1");
        testVo.setStartDate(LocalDate.now().plusDays(1));
        testVo.setEndDate(LocalDate.now().plusDays(30));
        testVo.setStatus("PENDING");
        testVo.setStatusText("待审核");
        testVo.setCreateTime(LocalDateTime.now());
        testVo.setCanCancel(true);
        testVo.setCanEdit(true);
        testVo.setIsActive(false);
        testVo.setTotalImpressions(0L);
        testVo.setTotalClicks(0L);
        testVo.setCtr(0.0);

        // 准备测试统计VO
        testStatisticsVo = new BannerStatisticsVO();
        testStatisticsVo.setBannerId(TEST_APPLICATION_ID);
        testStatisticsVo.setBannerTitle("测试轮播图");
        testStatisticsVo.setTotalImpressions(1000L);
        testStatisticsVo.setTotalClicks(50L);
        testStatisticsVo.setCtr(5.0);
    }


    // ==================== 申请提交测试 ====================

    @Test
    @DisplayName("提交轮播图申请成功")
    void testCreateApplicationSuccess() throws Exception {
        // Mock服务层
        when(bannerApplicationService.createApplication(any(BannerApplicationDTO.class), eq(TEST_MERCHANT_ID)))
                .thenReturn(TEST_APPLICATION_ID);

        // 执行测试
        mockMvc.perform(post("/api/merchant/banner/apply")
                .header("X-Merchant-Id", TEST_MERCHANT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(TEST_APPLICATION_ID))
                .andExpect(jsonPath("$.message").value("申请提交成功"));

        // 验证服务层调用
        verify(bannerApplicationService, times(1)).createApplication(any(BannerApplicationDTO.class), eq(TEST_MERCHANT_ID));
    }

    // ==================== 申请更新测试 ====================

    @Test
    @DisplayName("更新轮播图申请成功")
    void testUpdateApplicationSuccess() throws Exception {
        // Mock服务层
        doNothing().when(bannerApplicationService).updateApplication(eq(TEST_APPLICATION_ID), any(BannerApplicationDTO.class), eq(TEST_MERCHANT_ID));

        // 执行测试
        mockMvc.perform(put("/api/merchant/banner/{id}", TEST_APPLICATION_ID)
                .header("X-Merchant-Id", TEST_MERCHANT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("申请更新成功"));

        // 验证服务层调用
        verify(bannerApplicationService, times(1)).updateApplication(eq(TEST_APPLICATION_ID), any(BannerApplicationDTO.class), eq(TEST_MERCHANT_ID));
    }

    // ==================== 申请取消测试 ====================

    @Test
    @DisplayName("取消轮播图申请成功")
    void testCancelApplicationSuccess() throws Exception {
        // Mock服务层
        doNothing().when(bannerApplicationService).cancelApplication(eq(TEST_APPLICATION_ID), eq(TEST_MERCHANT_ID));

        // 执行测试
        mockMvc.perform(delete("/api/merchant/banner/{id}", TEST_APPLICATION_ID)
                .header("X-Merchant-Id", TEST_MERCHANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("申请已取消"));

        // 验证服务层调用
        verify(bannerApplicationService, times(1)).cancelApplication(eq(TEST_APPLICATION_ID), eq(TEST_MERCHANT_ID));
    }

    // ==================== 申请详情测试 ====================

    @Test
    @DisplayName("获取轮播图申请详情成功")
    void testGetApplicationDetailSuccess() throws Exception {
        // Mock服务层
        when(bannerApplicationService.getApplicationDetail(eq(TEST_APPLICATION_ID), eq(TEST_MERCHANT_ID)))
                .thenReturn(testVo);

        // 执行测试
        mockMvc.perform(get("/api/merchant/banner/{id}", TEST_APPLICATION_ID)
                .header("X-Merchant-Id", TEST_MERCHANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(TEST_APPLICATION_ID))
                .andExpect(jsonPath("$.data.title").value("测试轮播图"))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.canCancel").value(true));

        // 验证服务层调用
        verify(bannerApplicationService, times(1)).getApplicationDetail(eq(TEST_APPLICATION_ID), eq(TEST_MERCHANT_ID));
    }

    // ==================== 申请列表测试 ====================

    @Test
    @DisplayName("获取轮播图申请列表成功")
    void testGetApplicationListSuccess() throws Exception {
        // Mock服务层
        List<BannerApplicationVO> voList = Arrays.asList(testVo);
        Page<BannerApplicationVO> page = new PageImpl<>(voList);
        when(bannerApplicationService.getApplicationList(eq(TEST_MERCHANT_ID), isNull(), eq(0), eq(10)))
                .thenReturn(page);

        // 执行测试
        mockMvc.perform(get("/api/merchant/banner/list")
                .header("X-Merchant-Id", TEST_MERCHANT_ID)
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].id").value(TEST_APPLICATION_ID));

        // 验证服务层调用
        verify(bannerApplicationService, times(1)).getApplicationList(eq(TEST_MERCHANT_ID), isNull(), eq(0), eq(10));
    }

    @Test
    @DisplayName("按状态筛选申请列表")
    void testGetApplicationListByStatus() throws Exception {
        // Mock服务层
        testVo.setStatus("APPROVED");
        testVo.setStatusText("已通过");
        List<BannerApplicationVO> voList = Arrays.asList(testVo);
        Page<BannerApplicationVO> page = new PageImpl<>(voList);
        when(bannerApplicationService.getApplicationList(eq(TEST_MERCHANT_ID), eq("APPROVED"), eq(0), eq(10)))
                .thenReturn(page);

        // 执行测试
        mockMvc.perform(get("/api/merchant/banner/list")
                .header("X-Merchant-Id", TEST_MERCHANT_ID)
                .param("status", "APPROVED")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content[0].status").value("APPROVED"));

        // 验证服务层调用
        verify(bannerApplicationService, times(1)).getApplicationList(eq(TEST_MERCHANT_ID), eq("APPROVED"), eq(0), eq(10));
    }


    // ==================== 统计数据测试 ====================

    @Test
    @DisplayName("获取轮播图统计数据成功")
    void testGetStatisticsSuccess() throws Exception {
        // Mock服务层
        when(bannerApplicationService.getStatistics(eq(TEST_APPLICATION_ID), eq(TEST_MERCHANT_ID)))
                .thenReturn(testStatisticsVo);

        // 执行测试
        mockMvc.perform(get("/api/merchant/banner/{id}/statistics", TEST_APPLICATION_ID)
                .header("X-Merchant-Id", TEST_MERCHANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.bannerId").value(TEST_APPLICATION_ID))
                .andExpect(jsonPath("$.data.totalImpressions").value(1000))
                .andExpect(jsonPath("$.data.totalClicks").value(50))
                .andExpect(jsonPath("$.data.ctr").value(5.0));

        // 验证服务层调用
        verify(bannerApplicationService, times(1)).getStatistics(eq(TEST_APPLICATION_ID), eq(TEST_MERCHANT_ID));
    }

    // ==================== 申请数量统计测试 ====================

    @Test
    @DisplayName("获取申请数量统计成功")
    void testGetApplicationCountSuccess() throws Exception {
        // Mock服务层
        when(bannerApplicationService.countApplications(eq(TEST_MERCHANT_ID), isNull()))
                .thenReturn(5L);

        // 执行测试
        mockMvc.perform(get("/api/merchant/banner/count")
                .header("X-Merchant-Id", TEST_MERCHANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(5));

        // 验证服务层调用
        verify(bannerApplicationService, times(1)).countApplications(eq(TEST_MERCHANT_ID), isNull());
    }

    @Test
    @DisplayName("按状态获取申请数量")
    void testGetApplicationCountByStatus() throws Exception {
        // Mock服务层
        when(bannerApplicationService.countApplications(eq(TEST_MERCHANT_ID), eq("PENDING")))
                .thenReturn(3L);

        // 执行测试
        mockMvc.perform(get("/api/merchant/banner/count")
                .header("X-Merchant-Id", TEST_MERCHANT_ID)
                .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(3));

        // 验证服务层调用
        verify(bannerApplicationService, times(1)).countApplications(eq(TEST_MERCHANT_ID), eq("PENDING"));
    }
}
