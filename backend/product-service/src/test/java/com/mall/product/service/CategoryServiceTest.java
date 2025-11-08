package com.mall.product.service;

import com.mall.product.domain.entity.Category;
import com.mall.product.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 分类服务单元测试
 * 测试分类管理、搜索优化、缓存管理、批量操作等核心功能
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
@DisplayName("分类服务测试")
public class CategoryServiceTest {

    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        categoryService = new CategoryServiceImpl();
    }

    // ==================== 基础查询测试 ====================

    @Test
    @DisplayName("获取所有分类测试")
    void testGetAllCategories() {
        // 执行测试
        List<Category> categories = categoryService.getAllCategories();

        // 验证结果
        assertNotNull(categories, "分类列表不应该为空");
        assertFalse(categories.isEmpty(), "应该有分类数据");
        
        for (Category category : categories) {
            assertNotNull(category.getId(), "分类ID不应该为空");
            assertNotNull(category.getName(), "分类名称不应该为空");
        }
    }

    @Test
    @DisplayName("分页查询分类测试")
    void testGetCategories() {
        // 执行测试
        Object pageData = categoryService.getCategories(1L, 5L);

        // 验证结果
        assertNotNull(pageData, "分页数据不应该为空");
    }

    @Test
    @DisplayName("根据ID获取分类测试")
    void testGetCategoryById() {
        // 执行测试
        Category category = categoryService.getCategoryById(1L);

        // 验证结果
        assertNotNull(category, "应该能获取到分类");
        assertEquals(1L, category.getId(), "分类ID应该正确");
        assertNotNull(category.getName(), "分类名称不应该为空");
    }

    @Test
    @DisplayName("根据父级ID获取子分类测试")
    void testGetCategoriesByParentId() {
        // 测试获取顶级分类
        List<Category> topCategories = categoryService.getCategoriesByParentId(null);
        assertNotNull(topCategories, "顶级分类列表不应该为空");

        // 测试获取子分类
        List<Category> childCategories = categoryService.getCategoriesByParentId(1L);
        assertNotNull(childCategories, "子分类列表不应该为空");
        
        for (Category category : childCategories) {
            assertEquals(1L, category.getParentId(), "所有子分类的父级ID应该正确");
        }
    }

    @Test
    @DisplayName("构建分类树测试")
    void testBuildCategoryTree() {
        // 执行测试
        List<Category> categoryTree = categoryService.buildCategoryTree();

        // 验证结果
        assertNotNull(categoryTree, "分类树不应该为空");
        assertFalse(categoryTree.isEmpty(), "分类树应该有数据");
        
        // 验证树形结构
        for (Category rootCategory : categoryTree) {
            assertNull(rootCategory.getParentId(), "根分类的父级ID应该为空");
            
            if (rootCategory.getChildren() != null && !rootCategory.getChildren().isEmpty()) {
                for (Category child : rootCategory.getChildren()) {
                    assertEquals(rootCategory.getId(), child.getParentId(), "子分类的父级ID应该正确");
                }
            }
        }
    }

    // ==================== 分类管理测试 ====================

    @Test
    @DisplayName("创建分类测试")
    void testCreateCategory() {
        // 准备测试数据
        Category category = new Category();
        category.setName("测试分类");
        category.setParentId(1L);
        category.setLevel(2);
        category.setSort(100);
        category.setStatus(1);
        category.setDescription("这是一个测试分类");

        // 执行测试
        boolean result = categoryService.createCategory(category);

        // 验证结果
        assertTrue(result, "分类创建应该成功");
        assertNotNull(category.getId(), "分类ID应该被设置");
    }

    @Test
    @DisplayName("更新分类测试")
    void testUpdateCategory() {
        // 准备测试数据
        Category category = categoryService.getCategoryById(1L);
        assertNotNull(category, "分类应该存在");

        String originalName = category.getName();
        category.setName("更新后的分类名称");

        // 执行测试
        boolean result = categoryService.updateCategory(category);

        // 验证结果
        assertTrue(result, "分类更新应该成功");
        
        Category updatedCategory = categoryService.getCategoryById(1L);
        assertEquals("更新后的分类名称", updatedCategory.getName(), "分类名称应该被更新");
    }

    @Test
    @DisplayName("删除分类测试")
    void testDeleteCategory() {
        // 先创建一个分类
        Category category = new Category();
        category.setName("待删除分类");
        category.setLevel(1);
        category.setSort(999);
        category.setStatus(1);
        categoryService.createCategory(category);

        Long categoryId = category.getId();
        assertNotNull(categoryId, "分类ID应该存在");

        // 执行删除
        boolean result = categoryService.deleteCategory(categoryId);

        // 验证结果
        assertTrue(result, "分类删除应该成功");
        
        Category deletedCategory = categoryService.getCategoryById(categoryId);
        assertNull(deletedCategory, "删除后应该无法获取到分类");
    }

    @Test
    @DisplayName("批量删除分类测试")
    void testBatchDeleteCategories() {
        // 准备测试数据
        List<Long> categoryIds = Arrays.asList(10L, 11L, 12L);

        // 执行测试
        boolean result = categoryService.batchDeleteCategories(categoryIds);

        // 验证结果
        assertTrue(result, "批量删除应该成功");
    }

    @Test
    @DisplayName("更新分类状态测试")
    void testUpdateCategoryStatus() {
        // 执行测试
        boolean result = categoryService.updateCategoryStatus(1L, 0);

        // 验证结果
        assertTrue(result, "状态更新应该成功");
        
        Category category = categoryService.getCategoryById(1L);
        assertEquals(0, category.getStatus(), "分类状态应该被更新");
    }

    // ==================== 搜索优化测试 ====================

    @Test
    @DisplayName("搜索分类测试")
    void testSearchCategories() {
        // 执行测试
        List<Category> categories = categoryService.searchCategories("电子");

        // 验证结果
        assertNotNull(categories, "搜索结果不应该为空");
        
        for (Category category : categories) {
            assertTrue(category.getName().contains("电子") || 
                      (category.getDescription() != null && category.getDescription().contains("电子")),
                      "搜索结果应该包含关键词");
        }
    }

    @Test
    @DisplayName("根据级别获取分类测试")
    void testGetCategoriesByLevel() {
        // 执行测试
        List<Category> level1Categories = categoryService.getCategoriesByLevel(1);
        List<Category> level2Categories = categoryService.getCategoriesByLevel(2);

        // 验证结果
        assertNotNull(level1Categories, "一级分类列表不应该为空");
        assertNotNull(level2Categories, "二级分类列表不应该为空");
        
        for (Category category : level1Categories) {
            assertEquals(1, category.getLevel(), "所有分类应该是一级分类");
        }
        
        for (Category category : level2Categories) {
            assertEquals(2, category.getLevel(), "所有分类应该是二级分类");
        }
    }

    @Test
    @DisplayName("获取分类路径测试")
    void testGetCategoryPath() {
        // 执行测试
        List<Category> path = categoryService.getCategoryPath(3L);

        // 验证结果
        assertNotNull(path, "分类路径不应该为空");
        assertFalse(path.isEmpty(), "分类路径应该有数据");
        
        // 验证路径顺序（从根到叶子）
        for (int i = 0; i < path.size() - 1; i++) {
            Category current = path.get(i);
            Category next = path.get(i + 1);
            assertEquals(current.getId(), next.getParentId(), "路径中的分类应该有正确的父子关系");
        }
    }

    @Test
    @DisplayName("获取所有子分类ID测试")
    void testGetAllChildCategoryIds() {
        // 执行测试
        List<Long> childIds = categoryService.getAllChildCategoryIds(1L);

        // 验证结果
        assertNotNull(childIds, "子分类ID列表不应该为空");
        
        // 验证所有子分类确实属于指定分类
        for (Long childId : childIds) {
            Category child = categoryService.getCategoryById(childId);
            assertNotNull(child, "子分类应该存在");
            
            // 验证是直接子分类或间接子分类
            List<Category> path = categoryService.getCategoryPath(childId);
            boolean isChild = path.stream().anyMatch(c -> c.getId().equals(1L));
            assertTrue(isChild, "应该是指定分类的子分类");
        }
    }

    // ==================== 缓存优化测试 ====================

    @Test
    @DisplayName("刷新分类缓存测试")
    void testRefreshCategoryCache() {
        // 执行测试
        boolean result = categoryService.refreshCategoryCache();

        // 验证结果
        assertTrue(result, "缓存刷新应该成功");
    }

    @Test
    @DisplayName("获取热门分类测试")
    void testGetHotCategories() {
        // 执行测试
        List<Category> hotCategories = categoryService.getHotCategories(5);

        // 验证结果
        assertNotNull(hotCategories, "热门分类列表不应该为空");
        assertTrue(hotCategories.size() <= 5, "热门分类数量不应该超过限制");
        
        for (Category category : hotCategories) {
            assertNotNull(category.getId(), "分类ID不应该为空");
            assertNotNull(category.getName(), "分类名称不应该为空");
        }
    }

    // ==================== 批量操作测试 ====================

    @Test
    @DisplayName("批量更新分类状态测试")
    void testBatchUpdateCategoryStatus() {
        // 准备测试数据
        List<Long> categoryIds = Arrays.asList(1L, 2L, 3L);

        // 执行测试
        boolean result = categoryService.batchUpdateCategoryStatus(categoryIds, 0);

        // 验证结果
        assertTrue(result, "批量状态更新应该成功");
        
        // 验证状态是否更新
        for (Long categoryId : categoryIds) {
            Category category = categoryService.getCategoryById(categoryId);
            if (category != null) {
                assertEquals(0, category.getStatus(), "分类状态应该被更新");
            }
        }
    }

    @Test
    @DisplayName("批量移动分类测试")
    void testBatchMoveCategories() {
        // 准备测试数据
        List<Long> categoryIds = Arrays.asList(4L, 5L);
        Long newParentId = 2L;

        // 执行测试
        boolean result = categoryService.batchMoveCategories(categoryIds, newParentId);

        // 验证结果
        assertTrue(result, "批量移动分类应该成功");
        
        // 验证分类是否移动
        for (Long categoryId : categoryIds) {
            Category category = categoryService.getCategoryById(categoryId);
            if (category != null) {
                assertEquals(newParentId, category.getParentId(), "分类应该被移动到新的父分类下");
            }
        }
    }

    // ==================== 统计分析测试 ====================

    @Test
    @DisplayName("获取分类统计信息测试")
    void testGetCategoryStatistics() {
        // 执行测试
        Object statistics = categoryService.getCategoryStatistics(1L);

        // 验证结果
        assertNotNull(statistics, "统计信息不应该为空");
    }

    @Test
    @DisplayName("验证分类层级测试")
    void testValidateCategoryLevel() {
        // 执行测试
        boolean valid1 = categoryService.validateCategoryLevel(1L, 5);
        boolean valid2 = categoryService.validateCategoryLevel(1L, 1);

        // 验证结果
        assertTrue(valid1, "层级验证应该通过（层级小于限制）");
        assertFalse(valid2, "层级验证应该失败（层级等于限制但有子分类）");
    }

    // ==================== 异常情况测试 ====================

    @Test
    @DisplayName("获取不存在的分类测试")
    void testGetNonExistentCategory() {
        // 执行测试
        Category category = categoryService.getCategoryById(99999L);

        // 验证结果
        assertNull(category, "不存在的分类应该返回null");
    }

    @Test
    @DisplayName("搜索空关键词测试")
    void testSearchWithEmptyKeyword() {
        // 执行测试
        List<Category> categories = categoryService.searchCategories("");

        // 验证结果
        assertNotNull(categories, "搜索结果不应该为空");
        assertTrue(categories.isEmpty(), "空关键词搜索应该返回空列表");
    }

    @Test
    @DisplayName("创建空分类测试")
    void testCreateNullCategory() {
        // 执行测试并验证异常
        assertThrows(IllegalArgumentException.class, () -> {
            categoryService.createCategory(null);
        }, "创建空分类应该抛出异常");
    }

    @Test
    @DisplayName("删除有子分类的分类测试")
    void testDeleteCategoryWithChildren() {
        // 执行测试（尝试删除有子分类的分类）
        boolean result = categoryService.deleteCategory(1L);

        // 验证结果
        assertFalse(result, "删除有子分类的分类应该失败");
    }

    @Test
    @DisplayName("获取无效级别的分类测试")
    void testGetCategoriesByInvalidLevel() {
        // 执行测试
        List<Category> categories = categoryService.getCategoriesByLevel(-1);

        // 验证结果
        assertNotNull(categories, "结果不应该为空");
        assertTrue(categories.isEmpty(), "无效级别应该返回空列表");
    }

    @Test
    @DisplayName("获取不存在分类的路径测试")
    void testGetPathForNonExistentCategory() {
        // 执行测试
        List<Category> path = categoryService.getCategoryPath(99999L);

        // 验证结果
        assertNotNull(path, "路径不应该为空");
        assertTrue(path.isEmpty(), "不存在分类的路径应该为空列表");
    }

    @Test
    @DisplayName("批量操作空列表测试")
    void testBatchOperationsWithEmptyList() {
        // 执行测试
        boolean deleteResult = categoryService.batchDeleteCategories(Arrays.asList());
        boolean statusResult = categoryService.batchUpdateCategoryStatus(Arrays.asList(), 1);
        boolean moveResult = categoryService.batchMoveCategories(Arrays.asList(), 1L);

        // 验证结果
        assertTrue(deleteResult, "空列表批量删除应该成功");
        assertTrue(statusResult, "空列表批量状态更新应该成功");
        assertTrue(moveResult, "空列表批量移动应该成功");
    }
}