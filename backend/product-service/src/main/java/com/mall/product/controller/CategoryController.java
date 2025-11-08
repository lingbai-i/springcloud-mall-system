package com.mall.product.controller;

import com.mall.common.core.domain.R;
import com.mall.product.domain.entity.Category;
import com.mall.product.service.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类控制器
 * 提供商品分类相关的REST API接口
 * 
 * @author lingbai
 * @version 2.0
 * @since 2025-10-22
 * 修改日志：V2.0 2025-10-22：添加搜索、缓存、批量操作和统计功能
 */
@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
public class CategoryController {

    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);

    @Autowired
    private CategoryService categoryService;

    // ==================== 基础查询 ====================

    /**
     * 获取所有分类列表
     */
    @GetMapping("/all")
    public R<List<Category>> getAllCategories() {
        logger.info("接收到获取所有分类列表的请求");
        
        try {
            List<Category> categories = categoryService.getAllCategories();
            logger.info("成功获取分类列表 - 数量: {}", categories.size());
            return R.ok(categories);
        } catch (Exception e) {
            logger.error("获取分类列表失败", e);
            return R.fail("获取分类列表失败");
        }
    }

    /**
     * 分页查询分类
     */
    @GetMapping("/page")
    public R<Object> getCategories(@RequestParam(defaultValue = "1") Long current,
                                   @RequestParam(defaultValue = "10") Long size) {
        logger.info("接收到分页查询分类的请求 - 页码: {}, 大小: {}", current, size);
        
        try {
            Object pageData = categoryService.getCategories(current, size);
            logger.info("成功获取分页分类数据");
            return R.ok(pageData);
        } catch (Exception e) {
            logger.error("分页查询分类失败", e);
            return R.fail("分页查询分类失败");
        }
    }

    /**
     * 根据ID获取分类详情
     */
    @GetMapping("/{id}")
    public R<Category> getCategoryById(@PathVariable Long id) {
        logger.info("接收到根据ID获取分类的请求 - ID: {}", id);
        
        try {
            Category category = categoryService.getCategoryById(id);
            if (category != null) {
                logger.info("成功获取分类详情 - ID: {}, 名称: {}", category.getId(), category.getName());
                return R.ok(category);
            } else {
                logger.warn("未找到指定分类 - ID: {}", id);
                return R.fail("分类不存在");
            }
        } catch (Exception e) {
            logger.error("根据ID获取分类失败 - ID: {}", id, e);
            return R.fail("获取分类详情失败");
        }
    }

    /**
     * 根据父级ID获取子分类列表
     */
    @GetMapping("/children")
    public R<List<Category>> getCategoriesByParentId(@RequestParam(required = false) Long parentId) {
        logger.info("接收到根据父级ID获取子分类的请求 - 父级ID: {}", parentId);
        
        try {
            List<Category> childCategories = categoryService.getCategoriesByParentId(parentId);
            logger.info("成功获取子分类列表 - 父级ID: {}, 数量: {}", parentId, childCategories.size());
            return R.ok(childCategories);
        } catch (Exception e) {
            logger.error("根据父级ID获取子分类失败 - 父级ID: {}", parentId, e);
            return R.fail("获取子分类列表失败");
        }
    }

    /**
     * 获取分类树
     */
    @GetMapping("/tree")
    public R<List<Category>> getCategoryTree() {
        logger.info("接收到获取分类树的请求");
        
        try {
            List<Category> categoryTree = categoryService.buildCategoryTree();
            logger.info("成功构建分类树 - 根分类数量: {}", categoryTree.size());
            return R.ok(categoryTree);
        } catch (Exception e) {
            logger.error("获取分类树失败", e);
            return R.fail("获取分类树失败");
        }
    }

    // ==================== 分类管理 ====================

    /**
     * 创建新分类
     */
    @PostMapping
    public R<String> createCategory(@RequestBody Category category) {
        logger.info("接收到创建分类的请求 - 名称: {}", category != null ? category.getName() : "null");
        
        try {
            boolean success = categoryService.createCategory(category);
            if (success) {
                logger.info("分类创建成功 - 名称: {}", category.getName());
                return R.ok("分类创建成功");
            } else {
                logger.warn("分类创建失败 - 名称: {}", category != null ? category.getName() : "null");
                return R.fail("分类创建失败");
            }
        } catch (Exception e) {
            logger.error("创建分类时发生异常", e);
            return R.fail("分类创建失败");
        }
    }

    /**
     * 更新分类信息
     */
    @PutMapping
    public R<String> updateCategory(@RequestBody Category category) {
        logger.info("接收到更新分类的请求 - ID: {}", category != null ? category.getId() : "null");
        
        try {
            boolean success = categoryService.updateCategory(category);
            if (success) {
                logger.info("分类更新成功 - ID: {}, 名称: {}", category.getId(), category.getName());
                return R.ok("分类更新成功");
            } else {
                logger.warn("分类更新失败 - ID: {}", category != null ? category.getId() : "null");
                return R.fail("分类更新失败");
            }
        } catch (Exception e) {
            logger.error("更新分类时发生异常", e);
            return R.fail("分类更新失败");
        }
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/{id}")
    public R<String> deleteCategory(@PathVariable Long id) {
        logger.info("接收到删除分类的请求 - ID: {}", id);
        
        try {
            boolean success = categoryService.deleteCategory(id);
            if (success) {
                logger.info("分类删除成功 - ID: {}", id);
                return R.ok("分类删除成功");
            } else {
                logger.warn("分类删除失败 - ID: {}", id);
                return R.fail("分类删除失败，可能存在子分类");
            }
        } catch (Exception e) {
            logger.error("删除分类时发生异常 - ID: {}", id, e);
            return R.fail("分类删除失败");
        }
    }

    /**
     * 批量删除分类
     */
    @DeleteMapping("/batch")
    public R<String> batchDeleteCategories(@RequestBody List<Long> ids) {
        logger.info("接收到批量删除分类的请求 - 数量: {}", ids != null ? ids.size() : 0);
        
        try {
            boolean success = categoryService.batchDeleteCategories(ids);
            if (success) {
                logger.info("批量删除分类成功 - 数量: {}", ids.size());
                return R.ok("批量删除分类成功");
            } else {
                logger.warn("批量删除分类失败");
                return R.fail("批量删除分类失败");
            }
        } catch (Exception e) {
            logger.error("批量删除分类时发生异常", e);
            return R.fail("批量删除分类失败");
        }
    }

    /**
     * 更新分类状态
     */
    @PutMapping("/{id}/status")
    public R<String> updateCategoryStatus(@PathVariable Long id, @RequestParam Integer status) {
        logger.info("接收到更新分类状态的请求 - ID: {}, 状态: {}", id, status);
        
        try {
            boolean success = categoryService.updateCategoryStatus(id, status);
            if (success) {
                logger.info("分类状态更新成功 - ID: {}, 状态: {}", id, status);
                return R.ok("分类状态更新成功");
            } else {
                logger.warn("分类状态更新失败 - ID: {}, 状态: {}", id, status);
                return R.fail("分类状态更新失败");
            }
        } catch (Exception e) {
            logger.error("更新分类状态时发生异常 - ID: {}, 状态: {}", id, status, e);
            return R.fail("分类状态更新失败");
        }
    }

    // ==================== 搜索优化 ====================

    /**
     * 搜索分类
     */
    @GetMapping("/search")
    public R<List<Category>> searchCategories(@RequestParam String keyword) {
        logger.info("接收到搜索分类的请求 - 关键词: {}", keyword);
        
        try {
            List<Category> categories = categoryService.searchCategories(keyword);
            logger.info("搜索分类成功 - 关键词: {}, 结果数量: {}", keyword, categories.size());
            return R.ok(categories);
        } catch (Exception e) {
            logger.error("搜索分类失败 - 关键词: {}", keyword, e);
            return R.fail("搜索分类失败");
        }
    }

    /**
     * 根据级别获取分类
     */
    @GetMapping("/level/{level}")
    public R<List<Category>> getCategoriesByLevel(@PathVariable Integer level) {
        logger.info("接收到根据级别获取分类的请求 - 级别: {}", level);
        
        try {
            List<Category> categories = categoryService.getCategoriesByLevel(level);
            logger.info("根据级别获取分类成功 - 级别: {}, 数量: {}", level, categories.size());
            return R.ok(categories);
        } catch (Exception e) {
            logger.error("根据级别获取分类失败 - 级别: {}", level, e);
            return R.fail("根据级别获取分类失败");
        }
    }

    /**
     * 获取分类路径
     */
    @GetMapping("/{id}/path")
    public R<List<Category>> getCategoryPath(@PathVariable Long id) {
        logger.info("接收到获取分类路径的请求 - ID: {}", id);
        
        try {
            List<Category> path = categoryService.getCategoryPath(id);
            logger.info("获取分类路径成功 - ID: {}, 路径长度: {}", id, path.size());
            return R.ok(path);
        } catch (Exception e) {
            logger.error("获取分类路径失败 - ID: {}", id, e);
            return R.fail("获取分类路径失败");
        }
    }

    /**
     * 获取分类的所有子分类ID
     */
    @GetMapping("/{id}/children/ids")
    public R<List<Long>> getAllChildCategoryIds(@PathVariable Long id) {
        logger.info("接收到获取子分类ID的请求 - ID: {}", id);
        
        try {
            List<Long> childIds = categoryService.getAllChildCategoryIds(id);
            logger.info("获取子分类ID成功 - ID: {}, 子分类数量: {}", id, childIds.size());
            return R.ok(childIds);
        } catch (Exception e) {
            logger.error("获取子分类ID失败 - ID: {}", id, e);
            return R.fail("获取子分类ID失败");
        }
    }

    // ==================== 缓存优化 ====================

    /**
     * 刷新分类缓存
     */
    @PostMapping("/cache/refresh")
    public R<String> refreshCategoryCache() {
        logger.info("接收到刷新分类缓存的请求");
        
        try {
            boolean success = categoryService.refreshCategoryCache();
            if (success) {
                logger.info("分类缓存刷新成功");
                return R.ok("分类缓存刷新成功");
            } else {
                logger.warn("分类缓存刷新失败");
                return R.fail("分类缓存刷新失败");
            }
        } catch (Exception e) {
            logger.error("刷新分类缓存时发生异常", e);
            return R.fail("分类缓存刷新失败");
        }
    }

    /**
     * 获取热门分类
     */
    @GetMapping("/hot")
    public R<List<Category>> getHotCategories(@RequestParam(defaultValue = "10") Integer limit) {
        logger.info("接收到获取热门分类的请求 - 限制数量: {}", limit);
        
        try {
            List<Category> hotCategories = categoryService.getHotCategories(limit);
            logger.info("获取热门分类成功 - 数量: {}", hotCategories.size());
            return R.ok(hotCategories);
        } catch (Exception e) {
            logger.error("获取热门分类失败 - 限制数量: {}", limit, e);
            return R.fail("获取热门分类失败");
        }
    }

    // ==================== 批量操作 ====================

    /**
     * 批量更新分类状态
     */
    @PutMapping("/batch/status")
    public R<String> batchUpdateCategoryStatus(@RequestBody List<Long> ids, @RequestParam Integer status) {
        logger.info("接收到批量更新分类状态的请求 - 数量: {}, 状态: {}", ids != null ? ids.size() : 0, status);
        
        try {
            boolean success = categoryService.batchUpdateCategoryStatus(ids, status);
            if (success) {
                logger.info("批量更新分类状态成功 - 数量: {}, 状态: {}", ids.size(), status);
                return R.ok("批量更新分类状态成功");
            } else {
                logger.warn("批量更新分类状态失败");
                return R.fail("批量更新分类状态失败");
            }
        } catch (Exception e) {
            logger.error("批量更新分类状态时发生异常", e);
            return R.fail("批量更新分类状态失败");
        }
    }

    /**
     * 批量移动分类
     */
    @PutMapping("/batch/move")
    public R<String> batchMoveCategories(@RequestBody List<Long> categoryIds, @RequestParam Long newParentId) {
        logger.info("接收到批量移动分类的请求 - 数量: {}, 新父分类ID: {}", categoryIds != null ? categoryIds.size() : 0, newParentId);
        
        try {
            boolean success = categoryService.batchMoveCategories(categoryIds, newParentId);
            if (success) {
                logger.info("批量移动分类成功 - 数量: {}, 新父分类ID: {}", categoryIds.size(), newParentId);
                return R.ok("批量移动分类成功");
            } else {
                logger.warn("批量移动分类失败");
                return R.fail("批量移动分类失败");
            }
        } catch (Exception e) {
            logger.error("批量移动分类时发生异常", e);
            return R.fail("批量移动分类失败");
        }
    }

    // ==================== 统计分析 ====================

    /**
     * 获取分类统计信息
     */
    @GetMapping("/{id}/statistics")
    public R<Object> getCategoryStatistics(@PathVariable Long id) {
        logger.info("接收到获取分类统计信息的请求 - ID: {}", id);
        
        try {
            Object statistics = categoryService.getCategoryStatistics(id);
            if (statistics != null) {
                logger.info("获取分类统计信息成功 - ID: {}", id);
                return R.ok(statistics);
            } else {
                logger.warn("分类不存在 - ID: {}", id);
                return R.fail("分类不存在");
            }
        } catch (Exception e) {
            logger.error("获取分类统计信息失败 - ID: {}", id, e);
            return R.fail("获取分类统计信息失败");
        }
    }

    /**
     * 验证分类层级
     */
    @GetMapping("/{id}/validate-level")
    public R<Boolean> validateCategoryLevel(@PathVariable Long id, @RequestParam Integer maxLevel) {
        logger.info("接收到验证分类层级的请求 - ID: {}, 最大层级: {}", id, maxLevel);
        
        try {
            boolean valid = categoryService.validateCategoryLevel(id, maxLevel);
            logger.info("验证分类层级完成 - ID: {}, 最大层级: {}, 结果: {}", id, maxLevel, valid);
            return R.ok(valid);
        } catch (Exception e) {
            logger.error("验证分类层级失败 - ID: {}, 最大层级: {}", id, maxLevel, e);
            return R.fail("验证分类层级失败");
        }
    }
}