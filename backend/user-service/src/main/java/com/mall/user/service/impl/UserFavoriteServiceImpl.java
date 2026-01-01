package com.mall.user.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.core.domain.R;
import com.mall.user.client.ProductClient;
import com.mall.user.domain.entity.UserFavorite;
import com.mall.user.domain.vo.UserFavoriteVO;
import com.mall.user.mapper.UserFavoriteMapper;
import com.mall.user.service.UserFavoriteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户收藏Service实现类
 * 
 * @author lingbai
 * @version 1.0
 * P25-09-20
 */
@Service
public class UserFavoriteServiceImpl implements UserFavoriteService {

    private static final Logger logger = LoggerFactory.getLogger(UserFavoriteServiceImpl.class);

    @Autowired
    private UserFavoriteMapper userFavoriteMapper;

    @Autowired
    private ProductClient productClient;

    @Override
    @Transactional
    public R<Void> addFavorite(Long userId, Long productId) {
        logger.info("添加收藏 - 用户ID: {}, 商品ID: {}", userId, productId);

        try {
            // 检查是否已收藏
            if (userFavoriteMapper.existsByUserIdAndProductId(userId, productId)) {
                logger.warn("商品已收藏 - 用户ID: {}, 商品ID: {}", userId, productId);
                return R.fail("商品已收藏");
            }

            // 获取商品信息
            R<Map<String, Object>> productResult = productClient.getProductById(productId);
            if (productResult == null || productResult.getCode() != 200 || productResult.getData() == null) {
                logger.warn("商品不存在 - 商品ID: {}", productId);
                return R.fail("商品不存在");
            }

            Map<String, Object> productData = productResult.getData();

            // 创建收藏记录
            UserFavorite favorite = new UserFavorite();
            favorite.setUserId(userId);
            favorite.setProductId(productId);
            favorite.setProductName((String) productData.get("name"));
            favorite.setProductImage((String) productData.get("mainImage"));
            favorite.setProductDesc((String) productData.get("description"));
            
            // 处理价格
            Object priceObj = productData.get("price");
            if (priceObj != null) {
                favorite.setProductPrice(new BigDecimal(priceObj.toString()));
            }
            Object originalPriceObj = productData.get("originalPrice");
            if (originalPriceObj != null) {
                favorite.setOriginalPrice(new BigDecimal(originalPriceObj.toString()));
            }
            
            favorite.setCreateTime(LocalDateTime.now());
            favorite.setUpdateTime(LocalDateTime.now());

            // 保存收藏记录
            userFavoriteMapper.insert(favorite);

            // 增加商品收藏计数
            try {
                productClient.increaseFavoriteCount(productId);
            } catch (Exception e) {
                logger.warn("增加商品收藏计数失败，但收藏记录已保存 - 商品ID: {}", productId, e);
            }

            logger.info("收藏成功 - 用户ID: {}, 商品ID: {}", userId, productId);
            return R.ok();
        } catch (Exception e) {
            logger.error("添加收藏失败 - 用户ID: {}, 商品ID: {}", userId, productId, e);
            return R.fail("添加收藏失败");
        }
    }

    @Override
    @Transactional
    public R<Void> removeFavorite(Long userId, Long productId) {
        logger.info("取消收藏 - 用户ID: {}, 商品ID: {}", userId, productId);

        try {
            // 检查是否已收藏
            if (!userFavoriteMapper.existsByUserIdAndProductId(userId, productId)) {
                logger.warn("商品未收藏 - 用户ID: {}, 商品ID: {}", userId, productId);
                return R.fail("商品未收藏");
            }

            // 删除收藏记录
            int deleted = userFavoriteMapper.deleteByUserIdAndProductId(userId, productId);
            if (deleted > 0) {
                // 减少商品收藏计数
                try {
                    productClient.decreaseFavoriteCount(productId);
                } catch (Exception e) {
                    logger.warn("减少商品收藏计数失败 - 商品ID: {}", productId, e);
                }
                
                logger.info("取消收藏成功 - 用户ID: {}, 商品ID: {}", userId, productId);
                return R.ok();
            } else {
                return R.fail("取消收藏失败");
            }
        } catch (Exception e) {
            logger.error("取消收藏失败 - 用户ID: {}, 商品ID: {}", userId, productId, e);
            return R.fail("取消收藏失败");
        }
    }

    @Override
    @Transactional
    public R<Void> batchRemoveFavorites(Long userId, List<Long> productIds) {
        logger.info("批量取消收藏 - 用户ID: {}, 商品数量: {}", userId, productIds.size());

        try {
            if (productIds == null || productIds.isEmpty()) {
                return R.fail("请选择要取消收藏的商品");
            }

            // 批量删除收藏记录
            int deleted = userFavoriteMapper.deleteByUserIdAndProductIdIn(userId, productIds);
            
            // 批量减少商品收藏计数
            for (Long productId : productIds) {
                try {
                    productClient.decreaseFavoriteCount(productId);
                } catch (Exception e) {
                    logger.warn("减少商品收藏计数失败 - 商品ID: {}", productId, e);
                }
            }

            logger.info("批量取消收藏成功 - 用户ID: {}, 删除数量: {}", userId, deleted);
            return R.ok();
        } catch (Exception e) {
            logger.error("批量取消收藏失败 - 用户ID: {}", userId, e);
            return R.fail("批量取消收藏失败");
        }
    }

    @Override
    public R<Map<String, Object>> getFavorites(Long userId, int page, int size) {
        logger.info("获取收藏列表 - 用户ID: {}, 页码: {}, 每页大小: {}", userId, page, size);

        try {
            Page<UserFavorite> pageParam = new Page<>(page, size);
            IPage<UserFavorite> favoritePage = userFavoriteMapper.findByUserIdOrderByCreateTimeDesc(pageParam, userId);

            // 转换为VO
            List<UserFavoriteVO> voList = favoritePage.getRecords().stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());

            Map<String, Object> result = new HashMap<>();
            result.put("records", voList);
            result.put("total", favoritePage.getTotal());
            result.put("current", favoritePage.getCurrent());
            result.put("size", favoritePage.getSize());
            result.put("pages", favoritePage.getPages());

            logger.info("获取收藏列表成功 - 用户ID: {}, 总数: {}", userId, favoritePage.getTotal());
            return R.ok(result);
        } catch (Exception e) {
            logger.error("获取收藏列表失败 - 用户ID: {}", userId, e);
            return R.fail("获取收藏列表失败");
        }
    }

    @Override
    public R<Boolean> isFavorited(Long userId, Long productId) {
        logger.debug("检查收藏状态 - 用户ID: {}, 商品ID: {}", userId, productId);

        try {
            boolean favorited = userFavoriteMapper.existsByUserIdAndProductId(userId, productId);
            return R.ok(favorited);
        } catch (Exception e) {
            logger.error("检查收藏状态失败 - 用户ID: {}, 商品ID: {}", userId, productId, e);
            return R.fail("检查收藏状态失败");
        }
    }

    @Override
    public R<Long> getFavoriteCount(Long userId) {
        logger.debug("获取收藏数量 - 用户ID: {}", userId);

        try {
            long count = userFavoriteMapper.countByUserId(userId);
            return R.ok(count);
        } catch (Exception e) {
            logger.error("获取收藏数量失败 - 用户ID: {}", userId, e);
            return R.fail("获取收藏数量失败");
        }
    }

    /**
     * 将实体转换为VO
     */
    private UserFavoriteVO convertToVO(UserFavorite favorite) {
        UserFavoriteVO vo = new UserFavoriteVO();
        BeanUtils.copyProperties(favorite, vo);
        return vo;
    }
}
