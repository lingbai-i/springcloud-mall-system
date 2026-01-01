package com.mall.user.service;

import com.mall.common.core.domain.R;
import com.mall.user.domain.vo.UserFavoriteVO;

import java.util.List;
import java.util.Map;

/**
 * 用户收藏Service接口
 * 
 * @author lingbai
 * @version 1.0
 * P25-09-20
 */
public interface UserFavoriteService {

    /**
     * 添加收藏
     * 
     * @param userId 用户ID
     * @param productId 商品ID
     * @return 操作结果
     */
    R<Void> addFavorite(Long userId, Long productId);

    /**
     * 取消收藏
     * 
     * @param userId 用户ID
     * @param productId 商品ID
     * @return 操作结果
     */
    R<Void> removeFavorite(Long userId, Long productId);

    /**
     * 批量取消收藏
     * 
     * @param userId 用户ID
     * @param productIds 商品ID列表
     * @return 操作结果
     */
    R<Void> batchRemoveFavorites(Long userId, List<Long> productIds);

    /**
     * 获取收藏列表（分页）
     * 
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 收藏列表
     */
    R<Map<String, Object>> getFavorites(Long userId, int page, int size);

    /**
     * 检查是否已收藏
     * 
     * @param userId 用户ID
     * @param productId 商品ID
     * @return 是否已收藏
     */
    R<Boolean> isFavorited(Long userId, Long productId);

    /**
     * 获取用户收藏数量
     * 
     * @param userId 用户ID
     * @return 收藏数量
     */
    R<Long> getFavoriteCount(Long userId);
}
