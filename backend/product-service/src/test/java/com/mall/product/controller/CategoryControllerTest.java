package com.mall.product.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mall.product.domain.entity.Category;
import com.mall.product.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 分类控制器集成测试
 * 测试分类相关的REST API接口
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
@WebMvcTest(CategoryController.class)
@DisplayName("分类控制器测试")
public class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    @Autowired
    private ObjectMapper objectMapper;

    private Category testCategory;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("测试分类");
        testCategory.setParentId(null);
        testCategory.setLevel(1);
        testCategory.setSort(100);
        testCategory.setStatus(1);
        testCategory.setDescription("这是一个测试分类");
    }

    // ==================== 基础查询测试 ====================

    @Test
    @DisplayName("获取所有分类API测试")
    void testGetAllCategories() throws Exception {
        // Mock服务层
        List<Category> categories = Arrays.asList(testCategory);
        when(categoryService.getAllCategories()).thenReturn(categories);

        // 执行测试
        mockMvc.perform(get("/api/categories/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("测试分类"));

        // 验证服务层调用
        verify(categoryService, times(1)).getAllCategories();
    }

    @Test
    @DisplayName("分页查询分类API测试")
    void testGetCategories() throws Exception {
        // Mock服务层
        when(categoryService.getCategories(1L, 10L)).thenReturn("分页数据");

        // 执行测试
        mockMvc.perform(get("/api/categories/page")
                .param("current", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("分页数据"));

        // 验证服务层调用
        verify(categoryService, times(1)).getCategories(1L, 10L);
    }

    @Test
    @DisplayName("根据ID获取分类API测试")
    void testGetCategoryById() throws Exception {
        // Mock服务层
        when(categoryService.getCategoryById(1L)).thenReturn(testCategory);

        // 执行测试
        mockMvc.perform(get("/api/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("测试分类"));

        // 验证服务层调用
        verify(categoryService, times(1)).getCategoryById(1L);
    }

    @Test
    @DisplayName("根据父级ID获取子分类API测试")
    void testGetCategoriesByParentId() throws Exception {
        // Mock服务层
        List<Category> childCategories = Arrays.asList(testCategory);
        when(categoryService.getCategoriesByParentId(1L)).thenReturn(childCategories);

        // 执行测试
        mockMvc.perform(get("/api/categories/children")
                .param("parentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1));

        // 验证服务层调用
        verify(categoryService, times(1)).getCategoriesByParentId(1L);
    }

    @Test
    @DisplayName("获取分类树API测试")
    void testGetCategoryTree() throws Exception {
        // Mock服务层
        List<Category> categoryTree = Arrays.asList(testCategory);
        when(categoryService.buildCategoryTree()).thenReturn(categoryTree);

        // 执行测试
        mockMvc.perform(get("/api/categories/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1));

        // 验证服务层调用
        verify(categoryService, times(1)).buildCategoryTree();
    }

    // ==================== 分类管理测试 ====================

    @Test
    @DisplayName("创建分类API测试")
    void testCreateCategory() throws Exception {
        // Mock服务层
        when(categoryService.createCategory(any(Category.class))).thenReturn(true);

        // 执行测试
        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCategory)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("分类创建成功"));

        // 验证服务层调用
        verify(categoryService, times(1)).createCategory(any(Category.class));
    }

    @Test
    @DisplayName("更新分类API测试")
    void testUpdateCategory() throws Exception {
        // Mock服务层
        when(categoryService.updateCategory(any(Category.class))).thenReturn(true);

        // 执行测试
        mockMvc.perform(put("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCategory)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("分类更新成功"));

        // 验证服务层调用
        verify(categoryService, times(1)).updateCategory(any(Category.class));
    }

    @Test
    @DisplayName("删除分类API测试")
    void testDeleteCategory() throws Exception {
        // Mock服务层
        when(categoryService.deleteCategory(1L)).thenReturn(true);

        // 执行测试
        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("分类删除成功"));

        // 验证服务层调用
        verify(categoryService, times(1)).deleteCategory(1L);
    }

    @Test
    @DisplayName("批量删除分类API测试")
    void testBatchDeleteCategories() throws Exception {
        // Mock服务层
        List<Long> categoryIds = Arrays.asList(1L, 2L, 3L);
        when(categoryService.batchDeleteCategories(categoryIds)).thenReturn(true);

        // 执行测试
        mockMvc.perform(delete("/api/categories/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoryIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("批量删除分类成功"));

        // 验证服务层调用
        verify(categoryService, times(1)).batchDeleteCategories(categoryIds);
    }

    @Test
    @DisplayName("更新分类状态API测试")
    void testUpdateCategoryStatus() throws Exception {
        // Mock服务层
        when(categoryService.updateCategoryStatus(1L, 0)).thenReturn(true);

        // 执行测试
        mockMvc.perform(put("/api/categories/1/status")
                .param("status", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("分类状态更新成功"));

        // 验证服务层调用
        verify(categoryService, times(1)).updateCategoryStatus(1L, 0);
    }

    // ==================== 搜索优化测试 ====================

    @Test
    @DisplayName("搜索分类API测试")
    void testSearchCategories() throws Exception {
        // Mock服务层
        List<Category> searchResults = Arrays.asList(testCategory);
        when(categoryService.searchCategories("电子")).thenReturn(searchResults);

        // 执行测试
        mockMvc.perform(get("/api/categories/search")
                .param("keyword", "电子"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1));

        // 验证服务层调用
        verify(categoryService, times(1)).searchCategories("电子");
    }

    @Test
    @DisplayName("根据级别获取分类API测试")
    void testGetCategoriesByLevel() throws Exception {
        // Mock服务层
        List<Category> levelCategories = Arrays.asList(testCategory);
        when(categoryService.getCategoriesByLevel(1)).thenReturn(levelCategories);

        // 执行测试
        mockMvc.perform(get("/api/categories/level/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1));

        // 验证服务层调用
        verify(categoryService, times(1)).getCategoriesByLevel(1);
    }

    @Test
    @DisplayName("获取分类路径API测试")
    void testGetCategoryPath() throws Exception {
        // Mock服务层
        List<Category> path = Arrays.asList(testCategory);
        when(categoryService.getCategoryPath(1L)).thenReturn(path);

        // 执行测试
        mockMvc.perform(get("/api/categories/1/path"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1));

        // 验证服务层调用
        verify(categoryService, times(1)).getCategoryPath(1L);
    }

    @Test
    @DisplayName("获取子分类ID API测试")
    void testGetAllChildCategoryIds() throws Exception {
        // Mock服务层
        List<Long> childIds = Arrays.asList(2L, 3L, 4L);
        when(categoryService.getAllChildCategoryIds(1L)).thenReturn(childIds);

        // 执行测试
        mockMvc.perform(get("/api/categories/1/children/ids"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0]").value(2))
                .andExpect(jsonPath("$.data[1]").value(3))
                .andExpect(jsonPath("$.data[2]").value(4));

        // 验证服务层调用
        verify(categoryService, times(1)).getAllChildCategoryIds(1L);
    }

    // ==================== 缓存优化测试 ====================

    @Test
    @DisplayName("刷新分类缓存API测试")
    void testRefreshCategoryCache() throws Exception {
        // Mock服务层
        when(categoryService.refreshCategoryCache()).thenReturn(true);

        // 执行测试
        mockMvc.perform(post("/api/categories/cache/refresh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("分类缓存刷新成功"));

        // 验证服务层调用
        verify(categoryService, times(1)).refreshCategoryCache();
    }

    @Test
    @DisplayName("获取热门分类API测试")
    void testGetHotCategories() throws Exception {
        // Mock服务层
        List<Category> hotCategories = Arrays.asList(testCategory);
        when(categoryService.getHotCategories(10)).thenReturn(hotCategories);

        // 执行测试
        mockMvc.perform(get("/api/categories/hot")
                .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1));

        // 验证服务层调用
        verify(categoryService, times(1)).getHotCategories(10);
    }

    // ==================== 批量操作测试 ====================

    @Test
    @DisplayName("批量更新分类状态API测试")
    void testBatchUpdateCategoryStatus() throws Exception {
        // Mock服务层
        List<Long> categoryIds = Arrays.asList(1L, 2L, 3L);
        when(categoryService.batchUpdateCategoryStatus(categoryIds, 0)).thenReturn(true);

        // 执行测试
        mockMvc.perform(put("/api/categories/batch/status")
                .param("status", "0")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoryIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("批量更新分类状态成功"));

        // 验证服务层调用
        verify(categoryService, times(1)).batchUpdateCategoryStatus(categoryIds, 0);
    }

    @Test
    @DisplayName("批量移动分类API测试")
    void testBatchMoveCategories() throws Exception {
        // Mock服务层
        List<Long> categoryIds = Arrays.asList(4L, 5L);
        when(categoryService.batchMoveCategories(categoryIds, 2L)).thenReturn(true);

        // 执行测试
        mockMvc.perform(put("/api/categories/batch/move")
                .param("newParentId", "2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoryIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("批量移动分类成功"));

        // 验证服务层调用
        verify(categoryService, times(1)).batchMoveCategories(categoryIds, 2L);
    }

    // ==================== 统计分析测试 ====================

    @Test
    @DisplayName("获取分类统计信息API测试")
    void testGetCategoryStatistics() throws Exception {
        // Mock服务层
        Object statistics = "统计信息";
        when(categoryService.getCategoryStatistics(1L)).thenReturn(statistics);

        // 执行测试
        mockMvc.perform(get("/api/categories/1/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("统计信息"));

        // 验证服务层调用
        verify(categoryService, times(1)).getCategoryStatistics(1L);
    }

    @Test
    @DisplayName("验证分类层级API测试")
    void testValidateCategoryLevel() throws Exception {
        // Mock服务层
        when(categoryService.validateCategoryLevel(1L, 5)).thenReturn(true);

        // 执行测试
        mockMvc.perform(get("/api/categories/1/validate-level")
                .param("maxLevel", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));

        // 验证服务层调用
        verify(categoryService, times(1)).validateCategoryLevel(1L, 5);
    }

    // ==================== 异常情况测试 ====================

    @Test
    @DisplayName("获取不存在分类API测试")
    void testGetNonExistentCategory() throws Exception {
        // Mock服务层
        when(categoryService.getCategoryById(99999L)).thenReturn(null);

        // 执行测试
        mockMvc.perform(get("/api/categories/99999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("分类不存在"));

        // 验证服务层调用
        verify(categoryService, times(1)).getCategoryById(99999L);
    }

    @Test
    @DisplayName("创建分类失败API测试")
    void testCreateCategoryFailure() throws Exception {
        // Mock服务层
        when(categoryService.createCategory(any(Category.class))).thenReturn(false);

        // 执行测试
        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCategory)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("分类创建失败"));

        // 验证服务层调用
        verify(categoryService, times(1)).createCategory(any(Category.class));
    }

    @Test
    @DisplayName("删除有子分类的分类API测试")
    void testDeleteCategoryWithChildren() throws Exception {
        // Mock服务层
        when(categoryService.deleteCategory(1L)).thenReturn(false);

        // 执行测试
        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("分类删除失败，可能存在子分类"));

        // 验证服务层调用
        verify(categoryService, times(1)).deleteCategory(1L);
    }

    @Test
    @DisplayName("无效参数测试")
    void testInvalidParameters() throws Exception {
        // 测试无效的分类ID
        mockMvc.perform(get("/api/categories/invalid"))
                .andExpect(status().isBadRequest());

        // 测试无效的状态值
        mockMvc.perform(put("/api/categories/1/status")
                .param("status", "invalid"))
                .andExpect(status().isBadRequest());

        // 测试无效的级别值
        mockMvc.perform(get("/api/categories/level/invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("空请求体测试")
    void testEmptyRequestBody() throws Exception {
        // 测试创建分类时空请求体
        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isBadRequest());

        // 测试更新分类时空请求体
        mockMvc.perform(put("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("服务层异常处理测试")
    void testServiceException() throws Exception {
        // Mock服务层抛出异常
        when(categoryService.getCategoryById(1L)).thenThrow(new RuntimeException("数据库连接失败"));

        // 执行测试
        mockMvc.perform(get("/api/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("获取分类详情失败"));

        // 验证服务层调用
        verify(categoryService, times(1)).getCategoryById(1L);
    }

    @Test
    @DisplayName("搜索空关键词测试")
    void testSearchWithEmptyKeyword() throws Exception {
        // Mock服务层
        when(categoryService.searchCategories("")).thenReturn(Arrays.asList());

        // 执行测试
        mockMvc.perform(get("/api/categories/search")
                .param("keyword", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());

        // 验证服务层调用
        verify(categoryService, times(1)).searchCategories("");
    }

    @Test
    @DisplayName("缓存刷新失败测试")
    void testRefreshCacheFailure() throws Exception {
        // Mock服务层
        when(categoryService.refreshCategoryCache()).thenReturn(false);

        // 执行测试
        mockMvc.perform(post("/api/categories/cache/refresh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("分类缓存刷新失败"));

        // 验证服务层调用
        verify(categoryService, times(1)).refreshCategoryCache();
    }

    @Test
    @DisplayName("获取不存在分类的统计信息测试")
    void testGetStatisticsForNonExistentCategory() throws Exception {
        // Mock服务层
        when(categoryService.getCategoryStatistics(99999L)).thenReturn(null);

        // 执行测试
        mockMvc.perform(get("/api/categories/99999/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("分类不存在"));

        // 验证服务层调用
        verify(categoryService, times(1)).getCategoryStatistics(99999L);
    }
}