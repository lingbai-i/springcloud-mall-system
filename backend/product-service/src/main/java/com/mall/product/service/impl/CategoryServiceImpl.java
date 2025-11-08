package com.mall.product.service.impl;

import com.mall.product.domain.entity.Category;
import com.mall.product.service.CategoryService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * 分类服务实现类
 * 提供商品分类相关的业务逻辑处理
 * 
 * @author lingbai
 * @version 2.0
 * @since 2025-10-22
 * 修改日志：V2.0 2025-10-22：优化分类树结构、添加缓存机制、增强搜索功能
 */
@Service
public class CategoryServiceImpl implements CategoryService {
    
    // ==================== 存储和缓存 ====================
    private static final Map<Long, Category> categoryStorage = new ConcurrentHashMap<>();
    
    // 缓存相关
    private static final Map<String, Object> categoryCache = new ConcurrentHashMap<>();
    private static final String CACHE_KEY_ALL = "all_categories";
    private static final String CACHE_KEY_TREE = "category_tree";
    private static final String CACHE_KEY_HOT = "hot_categories";
    private static final String CACHE_KEY_SEARCH = "search_";
    private static final String CACHE_KEY_LEVEL = "level_";
    private static final String CACHE_KEY_PATH = "path_";
    private static final String CACHE_KEY_CHILDREN = "children_";
    
    // 缓存过期时间
    private static final long CACHE_EXPIRE_TIME = 30 * 60 * 1000; // 30分钟
    
    // 读写锁，提升并发性能
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private static final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
    private static final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
    
    // 异步执行器
    private static final ExecutorService asyncExecutor = Executors.newFixedThreadPool(4);
    
    // 搜索索引，提升搜索效率
    private static final Map<String, Set<Long>> searchIndex = new ConcurrentHashMap<>();
    
    static {
        initMockData();
        buildSearchIndex();
    }
    
    // ==================== 初始化数据 ====================
    
    private static void initMockData() {
        List<Category> mockCategories = createMockCategories();
        for (Category category : mockCategories) {
            categoryStorage.put(category.getId(), category);
        }
    }
    
    /**
     * 构建搜索索引，提升搜索效率
     */
    private static void buildSearchIndex() {
        searchIndex.clear();
        for (Category category : categoryStorage.values()) {
            if (category.getName() != null) {
                // 按字符分词建立索引
                String name = category.getName().toLowerCase();
                for (int i = 0; i < name.length(); i++) {
                    for (int j = i + 1; j <= name.length(); j++) {
                        String substring = name.substring(i, j);
                        searchIndex.computeIfAbsent(substring, k -> ConcurrentHashMap.newKeySet())
                                  .add(category.getId());
                    }
                }
            }
        }
    }
    
    /**
     * 创建模拟分类数据
     */
    private static List<Category> createMockCategories() {
        List<Category> categories = new ArrayList<>();
        
        // 一级分类
        categories.add(createCategory(1L, "电子产品", 0L, 1, 1, "📱", "各类电子产品", 1));
        categories.add(createCategory(2L, "服装鞋帽", 0L, 1, 2, "👕", "时尚服装配饰", 1));
        categories.add(createCategory(3L, "家居用品", 0L, 1, 3, "🏠", "家庭生活用品", 1));
        categories.add(createCategory(4L, "图书音像", 0L, 1, 4, "📚", "图书音像制品", 1));
        categories.add(createCategory(5L, "运动户外", 0L, 1, 5, "⚽", "运动健身用品", 1));
        
        // 二级分类 - 电子产品
        categories.add(createCategory(11L, "手机通讯", 1L, 2, 1, "📱", "智能手机及配件", 1));
        categories.add(createCategory(12L, "电脑办公", 1L, 2, 2, "💻", "电脑及办公设备", 1));
        categories.add(createCategory(13L, "数码影音", 1L, 2, 3, "📷", "数码相机音响等", 1));
        categories.add(createCategory(14L, "智能设备", 1L, 2, 4, "🤖", "智能家居设备", 1));
        
        // 二级分类 - 服装鞋帽
        categories.add(createCategory(21L, "男装", 2L, 2, 1, "👔", "男士服装", 1));
        categories.add(createCategory(22L, "女装", 2L, 2, 2, "👗", "女士服装", 1));
        categories.add(createCategory(23L, "鞋靴", 2L, 2, 3, "👟", "各类鞋靴", 1));
        categories.add(createCategory(24L, "箱包", 2L, 2, 4, "👜", "箱包配饰", 1));
        
        // 三级分类 - 手机通讯
        categories.add(createCategory(111L, "智能手机", 11L, 3, 1, "📱", "各品牌智能手机", 1));
        categories.add(createCategory(112L, "手机配件", 11L, 3, 2, "🔌", "手机壳充电器等", 1));
        categories.add(createCategory(113L, "对讲设备", 11L, 3, 3, "📻", "对讲机等通讯设备", 1));
        
        // 三级分类 - 电脑办公
        categories.add(createCategory(121L, "笔记本", 12L, 3, 1, "💻", "笔记本电脑", 1));
        categories.add(createCategory(122L, "台式机", 12L, 3, 2, "🖥️", "台式电脑", 1));
        categories.add(createCategory(123L, "办公设备", 12L, 3, 3, "🖨️", "打印机扫描仪等", 1));
        
        return categories;
    }
    
    /**
     * 创建分类对象
     */
    private static Category createCategory(Long id, String name, Long parentId, Integer level, 
                                         Integer sort, String icon, String description, Integer status) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        category.setParentId(parentId);
        category.setLevel(level);
        category.setSort(sort);
        category.setIcon(icon);
        category.setDescription(description);
        category.setStatus(status);
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        return category;
    }
    
    // ==================== 基础查询（优化版） ====================
    
    @Override
    public List<Category> getAllCategories() {
        return getCachedData(CACHE_KEY_ALL, () -> {
            readLock.lock();
            try {
                return new ArrayList<>(categoryStorage.values());
            } finally {
                readLock.unlock();
            }
        });
    }

    @Override
    public Object getCategories(Long current, Long size) {
        List<Category> allCategories = getAllCategories();
        
        // 分页计算
        int start = (int) ((current - 1) * size);
        int end = Math.min(start + size.intValue(), allCategories.size());
        
        List<Category> pageData = allCategories.subList(start, end);
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", pageData);
        result.put("total", allCategories.size());
        result.put("current", current);
        result.put("size", size);
        result.put("pages", (allCategories.size() + size - 1) / size);
        
        return result;
    }

    @Override
    public Category getCategoryById(Long id) {
        readLock.lock();
        try {
            return categoryStorage.get(id);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public List<Category> getCategoriesByParentId(Long parentId) {
        String cacheKey = CACHE_KEY_CHILDREN + (parentId != null ? parentId : 0);
        return getCachedData(cacheKey, () -> {
            readLock.lock();
            try {
                return categoryStorage.values().stream()
                        .filter(category -> Objects.equals(category.getParentId(), parentId))
                        .sorted(Comparator.comparing(Category::getSort))
                        .collect(Collectors.toList());
            } finally {
                readLock.unlock();
            }
        });
    }

    @Override
    public List<Category> buildCategoryTree() {
        return getCachedData(CACHE_KEY_TREE, () -> {
            List<Category> allCategories = getAllCategories();
            return buildTreeRecursive(allCategories);
        });
    }

    /**
     * 递归构建分类树（优化版）
     */
    private List<Category> buildTreeRecursive(List<Category> categories) {
        Map<Long, List<Category>> parentChildMap = categories.stream()
                .collect(Collectors.groupingBy(
                    category -> category.getParentId() != null ? category.getParentId() : 0L
                ));
        
        List<Category> rootCategories = parentChildMap.getOrDefault(0L, new ArrayList<>());
        
        // 递归设置子分类
        for (Category root : rootCategories) {
            setChildren(root, parentChildMap);
        }
        
        return rootCategories.stream()
                .sorted(Comparator.comparing(Category::getSort))
                .collect(Collectors.toList());
    }
    
    /**
     * 递归设置子分类
     */
    private void setChildren(Category parent, Map<Long, List<Category>> parentChildMap) {
        List<Category> children = parentChildMap.getOrDefault(parent.getId(), new ArrayList<>());
        children.sort(Comparator.comparing(Category::getSort));
        parent.setChildren(children);
        
        for (Category child : children) {
            setChildren(child, parentChildMap);
        }
    }

    // ==================== 分类管理（优化版） ====================
    
    @Override
    public boolean createCategory(Category category) {
        if (category == null || category.getName() == null || category.getName().trim().isEmpty()) {
            return false;
        }
        
        writeLock.lock();
        try {
            // 生成ID
            if (category.getId() == null) {
                category.setId(System.currentTimeMillis());
            }
            
            // 设置层级
            if (category.getLevel() == null) {
                category.setLevel(calculateLevel(category.getParentId()));
            }
            
            // 设置排序
            if (category.getSort() == null) {
                category.setSort(getNextSort(category.getParentId()));
            }
            
            // 设置默认值
            if (category.getStatus() == null) {
                category.setStatus(1);
            }
            
            category.setCreateTime(LocalDateTime.now());
            category.setUpdateTime(LocalDateTime.now());
            
            categoryStorage.put(category.getId(), category);
            
            // 异步更新搜索索引和清空缓存
            CompletableFuture.runAsync(() -> {
                updateSearchIndex(category);
                clearCache();
            }, asyncExecutor);
            
            return true;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean updateCategory(Category category) {
        if (category == null || category.getId() == null) {
            return false;
        }
        
        writeLock.lock();
        try {
            Category existing = categoryStorage.get(category.getId());
            if (existing == null) {
                return false;
            }
            
            // 更新字段
            if (category.getName() != null) existing.setName(category.getName());
            if (category.getParentId() != null) {
                existing.setParentId(category.getParentId());
                existing.setLevel(calculateLevel(category.getParentId()));
            }
            if (category.getSort() != null) existing.setSort(category.getSort());
            if (category.getIcon() != null) existing.setIcon(category.getIcon());
            if (category.getDescription() != null) existing.setDescription(category.getDescription());
            if (category.getStatus() != null) existing.setStatus(category.getStatus());
            
            existing.setUpdateTime(LocalDateTime.now());
            
            // 异步更新搜索索引和清空缓存
            CompletableFuture.runAsync(() -> {
                updateSearchIndex(existing);
                clearCache();
            }, asyncExecutor);
            
            return true;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean deleteCategory(Long id) {
        if (id == null) {
            return false;
        }
        
        writeLock.lock();
        try {
            // 检查是否有子分类
            List<Category> children = getCategoriesByParentId(id);
            if (!children.isEmpty()) {
                return false; // 有子分类，不能删除
            }
            
            Category removed = categoryStorage.remove(id);
            if (removed != null) {
                // 异步清理搜索索引和缓存
                CompletableFuture.runAsync(() -> {
                    removeFromSearchIndex(removed);
                    clearCache();
                }, asyncExecutor);
                return true;
            }
            return false;
        } finally {
            writeLock.unlock();
        }
    }

    // ==================== 搜索优化 ====================
    
    @Override
    public List<Category> searchCategories(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String cacheKey = CACHE_KEY_SEARCH + keyword.toLowerCase();
        return getCachedData(cacheKey, () -> {
            String searchKey = keyword.toLowerCase().trim();
            Set<Long> matchedIds = new HashSet<>();
            
            // 使用搜索索引快速查找
            for (Map.Entry<String, Set<Long>> entry : searchIndex.entrySet()) {
                if (entry.getKey().contains(searchKey)) {
                    matchedIds.addAll(entry.getValue());
                }
            }
            
            readLock.lock();
            try {
                return matchedIds.stream()
                        .map(categoryStorage::get)
                        .filter(Objects::nonNull)
                        .filter(category -> category.getStatus() == 1) // 只返回启用的分类
                        .sorted(Comparator.comparing(Category::getSort))
                        .collect(Collectors.toList());
            } finally {
                readLock.unlock();
            }
        });
    }

    @Override
    public List<Category> getCategoriesByLevel(Integer level) {
        if (level == null || level <= 0) {
            return new ArrayList<>();
        }
        
        String cacheKey = CACHE_KEY_LEVEL + level;
        return getCachedData(cacheKey, () -> {
            readLock.lock();
            try {
                return categoryStorage.values().stream()
                        .filter(category -> Objects.equals(category.getLevel(), level))
                        .filter(category -> category.getStatus() == 1)
                        .sorted(Comparator.comparing(Category::getSort))
                        .collect(Collectors.toList());
            } finally {
                readLock.unlock();
            }
        });
    }

    @Override
    public List<Category> getCategoryPath(Long categoryId) {
        if (categoryId == null) {
            return new ArrayList<>();
        }
        
        String cacheKey = CACHE_KEY_PATH + categoryId;
        return getCachedData(cacheKey, () -> {
            List<Category> path = new ArrayList<>();
            Category current = getCategoryById(categoryId);
            
            while (current != null) {
                path.add(0, current); // 添加到开头
                current = current.getParentId() != null ? getCategoryById(current.getParentId()) : null;
            }
            
            return path;
        });
    }

    // ==================== 统计分析（增强版） ====================
    
    @Override
    public Object getCategoryStatistics(Long categoryId) {
        Category category = getCategoryById(categoryId);
        if (category == null) {
            return null;
        }
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("categoryId", categoryId);
        statistics.put("categoryName", category.getName());
        statistics.put("level", category.getLevel());
        statistics.put("status", category.getStatus());
        statistics.put("createTime", category.getCreateTime());
        statistics.put("updateTime", category.getUpdateTime());
        
        // 统计子分类数量（直接子分类和所有子分类）
        List<Category> directChildren = getCategoriesByParentId(categoryId);
        List<Long> allChildIds = getAllChildCategoryIds(categoryId);
        
        statistics.put("directChildCount", directChildren.size());
        statistics.put("totalChildCount", allChildIds.size());
        
        // 统计各层级子分类数量
        Map<Integer, Long> levelCount = allChildIds.stream()
                .map(this::getCategoryById)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Category::getLevel, Collectors.counting()));
        statistics.put("childrenByLevel", levelCount);
        
        // 模拟商品数量统计（实际应该从商品服务获取）
        int productCount = (int) (Math.random() * 500) + allChildIds.size() * 10;
        statistics.put("productCount", productCount);
        
        // 模拟访问量统计
        statistics.put("viewCount", (int) (Math.random() * 10000) + productCount * 2);
        
        // 计算分类深度
        statistics.put("maxDepth", calculateMaxDepth(categoryId));
        
        return statistics;
    }
    
    /**
     * 计算分类的最大深度
     */
    private int calculateMaxDepth(Long categoryId) {
        List<Long> childIds = getAllChildCategoryIds(categoryId);
        if (childIds.isEmpty()) {
            return 0;
        }
        
        int maxDepth = 0;
        for (Long childId : childIds) {
            Category child = getCategoryById(childId);
            if (child != null) {
                int depth = child.getLevel() - getCategoryById(categoryId).getLevel();
                maxDepth = Math.max(maxDepth, depth);
            }
        }
        return maxDepth;
    }

    // ==================== 搜索索引管理 ====================
    
    /**
     * 更新搜索索引
     */
    private void updateSearchIndex(Category category) {
        if (category.getName() != null) {
            String name = category.getName().toLowerCase();
            for (int i = 0; i < name.length(); i++) {
                for (int j = i + 1; j <= name.length(); j++) {
                    String substring = name.substring(i, j);
                    searchIndex.computeIfAbsent(substring, k -> ConcurrentHashMap.newKeySet())
                              .add(category.getId());
                }
            }
        }
    }
    
    /**
     * 从搜索索引中移除
     */
    private void removeFromSearchIndex(Category category) {
        for (Set<Long> ids : searchIndex.values()) {
            ids.remove(category.getId());
        }
    }

    @Override
    public boolean batchDeleteCategories(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        
        boolean allSuccess = true;
        for (Long id : ids) {
            if (!deleteCategory(id)) {
                allSuccess = false;
            }
        }
        
        return allSuccess;
    }
    
    @Override
    public boolean batchUpdateCategoryStatus(List<Long> ids, Integer status) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        
        boolean allSuccess = true;
        for (Long id : ids) {
            if (!updateCategoryStatus(id, status)) {
                allSuccess = false;
            }
        }
        
        return allSuccess;
    }
    
    @Override
    public boolean updateCategoryStatus(Long id, Integer status) {
        if (id == null || status == null) {
            return false;
        }
        
        writeLock.lock();
        try {
            Category category = categoryStorage.get(id);
            if (category == null) {
                return false;
            }
            
            category.setStatus(status);
            category.setUpdateTime(LocalDateTime.now());
            
            // 异步清空缓存
            CompletableFuture.runAsync(() -> clearCache(), asyncExecutor);
            
            return true;
        } finally {
            writeLock.unlock();
        }
    }
    
    @Override
    public List<Long> getAllChildCategoryIds(Long categoryId) {
        if (categoryId == null) {
            return new ArrayList<>();
        }
        
        List<Long> childIds = new ArrayList<>();
        collectChildIds(categoryId, childIds);
        return childIds;
    }
    
    /**
     * 递归收集子分类ID
     */
    private void collectChildIds(Long parentId, List<Long> childIds) {
        List<Category> children = getCategoriesByParentId(parentId);
        for (Category child : children) {
            childIds.add(child.getId());
            collectChildIds(child.getId(), childIds);
        }
    }

    @Override
    public boolean batchMoveCategories(List<Long> categoryIds, Long newParentId) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return false;
        }
        
        Integer newLevel = calculateLevel(newParentId);
        boolean allSuccess = true;
        
        for (Long categoryId : categoryIds) {
            Category category = categoryStorage.get(categoryId);
            if (category != null) {
                category.setParentId(newParentId);
                category.setLevel(newLevel);
                category.setUpdateTime(LocalDateTime.now());
            } else {
                allSuccess = false;
            }
        }
        
        if (allSuccess) {
            clearCache();
        }
        
        return allSuccess;
    }
    
    // ==================== 缓存优化 ====================
    
    @Override
    public boolean refreshCategoryCache() {
        clearCache();
        // 重建搜索索引
        buildSearchIndex();
        // 预热缓存
        CompletableFuture.runAsync(() -> {
            getAllCategories();
            buildCategoryTree();
            getHotCategories(10);
        }, asyncExecutor);
        return true;
    }
    
    @Override
    public List<Category> getHotCategories(Integer limit) {
        return getCachedData(CACHE_KEY_HOT + "_" + limit, () -> {
            // 模拟热门分类逻辑：按商品数量和访问量排序
            List<Category> allCategories = getAllCategories();
            return allCategories.stream()
                    .filter(category -> category.getStatus() == 1)
                    .sorted((c1, c2) -> {
                        // 模拟综合热度排序（实际应该从数据库查询）
                        int score1 = (int) (Math.random() * 1000) + c1.getLevel() * 100;
                        int score2 = (int) (Math.random() * 1000) + c2.getLevel() * 100;
                        return Integer.compare(score2, score1);
                    })
                    .limit(limit)
                    .collect(Collectors.toList());
        });
    }

    @Override
    public boolean validateCategoryLevel(Long categoryId, Integer maxLevel) {
        if (maxLevel == null || maxLevel <= 0) {
            return false;
        }
        
        Category category = getCategoryById(categoryId);
        if (category == null) {
            return false;
        }
        
        return category.getLevel() <= maxLevel;
    }

    // ==================== 辅助方法 ====================
    
    /**
     * 计算分类层级
     */
    private Integer calculateLevel(Long parentId) {
        if (parentId == null || parentId == 0) {
            return 1;
        }
        
        Category parent = getCategoryById(parentId);
        return parent != null ? parent.getLevel() + 1 : 1;
    }
    
    /**
     * 获取下一个排序号
     */
    private Integer getNextSort(Long parentId) {
        List<Category> siblings = getCategoriesByParentId(parentId);
        return siblings.stream()
                .mapToInt(Category::getSort)
                .max()
                .orElse(0) + 1;
    }
    
    /**
     * 获取缓存数据
     */
    @SuppressWarnings("unchecked")
    private <T> T getCachedData(String key, java.util.function.Supplier<T> supplier) {
        CacheItem<T> cacheItem = (CacheItem<T>) categoryCache.get(key);
        
        if (cacheItem == null || cacheItem.isExpired()) {
            T data = supplier.get();
            categoryCache.put(key, new CacheItem<>(data));
            return data;
        }
        
        return cacheItem.getData();
    }
    
    /**
     * 清空缓存
     */
    private void clearCache() {
        categoryCache.clear();
    }
    
    /**
     * 缓存项
     */
    private static class CacheItem<T> {
        private final T data;
        private final long timestamp;
        
        public CacheItem(T data) {
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }
        
        public T getData() {
            return data;
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_EXPIRE_TIME;
        }
    }
}