package com.mall.product.service;

import com.mall.product.domain.entity.Category;

import java.util.List;

/**
 * 分类服务接口
 * 提供商品分类相关的业务逻辑处理
 * 
 * @author lingbai
 * @version 2.0
 * @since 2025-10-22
 * 修改日志：V2.0 2025-10-22：添加分类搜索、缓存优化、批量操作等功能
 */
public interface CategoryService {
    
    // ==================== 基础查询 ====================
    
    /**
     * 获取所有分类列表
     * 
     * @return 分类列表
     */
    List<Category> getAllCategories();
    
    /**
     * 分页查询分类
     * 
     * @param current 当前页码
     * @param size 每页大小
     * @return 分类分页数据
     */
    Object getCategories(Long current, Long size);
    
    /**
     * 根据ID获取分类
     * 
     * @param id 分类ID
     * @return 分类信息
     */
    Category getCategoryById(Long id);
    
    /**
     * 根据父级ID获取子分类列表
     * 
     * @param parentId 父级分类ID，为0或null时获取顶级分类
     * @return 子分类列表
     */
    List<Category> getCategoriesByParentId(Long parentId);
    
    /**
     * 构建分类树
     * 
     * @return 分类树结构
     */
    List<Category> buildCategoryTree();
    
    // ==================== 分类管理 ====================
    
    /**
     * 创建新分类
     * 
     * @param category 分类信息
     * @return 是否创建成功
     */
    boolean createCategory(Category category);
    
    /**
     * 更新分类信息
     * 
     * @param category 分类信息
     * @return 是否更新成功
     */
    boolean updateCategory(Category category);
    
    /**
     * 删除分类
     * 
     * @param id 分类ID
     * @return 是否删除成功
     */
    boolean deleteCategory(Long id);
    
    /**
     * 批量删除分类
     * 
     * @param ids 分类ID列表
     * @return 是否删除成功
     */
    boolean batchDeleteCategories(List<Long> ids);
    
    /**
     * 更新分类状态
     * 
     * @param id 分类ID
     * @param status 状态（1-启用，0-禁用）
     * @return 是否更新成功
     */
    boolean updateCategoryStatus(Long id, Integer status);
    
    // ==================== 搜索优化 ====================
    
    /**
     * 搜索分类
     * 根据关键词搜索分类名称和描述
     * 
     * @param keyword 搜索关键词
     * @return 匹配的分类列表
     */
    List<Category> searchCategories(String keyword);
    
    /**
     * 根据级别获取分类
     * 
     * @param level 分类级别
     * @return 指定级别的分类列表
     */
    List<Category> getCategoriesByLevel(Integer level);
    
    /**
     * 获取分类路径
     * 从根分类到指定分类的完整路径
     * 
     * @param categoryId 分类ID
     * @return 分类路径列表
     */
    List<Category> getCategoryPath(Long categoryId);
    
    /**
     * 获取分类的所有子分类ID
     * 递归获取指定分类下的所有子分类ID
     * 
     * @param categoryId 分类ID
     * @return 子分类ID列表
     */
    List<Long> getAllChildCategoryIds(Long categoryId);
    
    // ==================== 缓存优化 ====================
    
    /**
     * 刷新分类缓存
     * 清空并重新加载分类缓存
     * 
     * @return 是否刷新成功
     */
    boolean refreshCategoryCache();
    
    /**
     * 获取热门分类
     * 根据商品数量或访问量排序
     * 
     * @param limit 限制数量
     * @return 热门分类列表
     */
    List<Category> getHotCategories(Integer limit);
    
    // ==================== 批量操作 ====================
    
    /**
     * 批量更新分类状态
     * 
     * @param ids 分类ID列表
     * @param status 状态
     * @return 是否更新成功
     */
    boolean batchUpdateCategoryStatus(List<Long> ids, Integer status);
    
    /**
     * 批量移动分类
     * 将多个分类移动到新的父分类下
     * 
     * @param categoryIds 分类ID列表
     * @param newParentId 新父分类ID
     * @return 是否移动成功
     */
    boolean batchMoveCategories(List<Long> categoryIds, Long newParentId);
    
    // ==================== 统计分析 ====================
    
    /**
     * 获取分类统计信息
     * 包括商品数量、子分类数量等
     * 
     * @param categoryId 分类ID
     * @return 统计信息
     */
    Object getCategoryStatistics(Long categoryId);
    
    /**
     * 验证分类层级
     * 检查分类层级是否合理，防止过深的嵌套
     * 
     * @param categoryId 分类ID
     * @param maxLevel 最大允许层级
     * @return 是否符合层级要求
     */
    boolean validateCategoryLevel(Long categoryId, Integer maxLevel);
}