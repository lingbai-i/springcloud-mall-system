package com.mall.cart.service.impl;

import com.mall.cart.client.ProductClient;
import com.mall.cart.domain.dto.ProductDTO;
import com.mall.cart.domain.entity.CartItem;
import com.mall.cart.service.CartService;
import com.mall.common.core.domain.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 购物车服务实现类
 * 基于Redis实现购物车功能
 * 
 * @author lingbai
 * @version 1.1
 * @since 2025-01-21
 * 
 * V1.1 2025-11-01：启用Redis缓存功能，完善库存验证和异常处理
 */
@Service
public class CartServiceImpl implements CartService {
    
    private static final Logger log = LoggerFactory.getLogger(CartServiceImpl.class);
    private static final String CART_KEY_PREFIX = "cart:";
    private static final long CART_EXPIRE_TIME = 30; // 30天过期
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private ProductClient productClient;
    
    /**
     * 添加商品到购物车
     * 
     * @param userId 用户ID
     * @param productId 商品ID
     * @param quantity 数量
     * @param specifications 规格
     * @return 操作结果
     */
    @Override
    public R<Void> addToCart(Long userId, Long productId, Integer quantity, String specifications) {
        try {
            log.info("添加商品到购物车开始: userId={}, productId={}, quantity={}, specifications={}", 
                userId, productId, quantity, specifications);
            
            // 参数验证
            if (userId == null || userId <= 0) {
                log.warn("用户ID无效: userId={}", userId);
                return R.fail("用户ID无效");
            }
            if (productId == null || productId <= 0) {
                log.warn("商品ID无效: productId={}", productId);
                return R.fail("商品ID无效");
            }
            if (quantity == null || quantity <= 0) {
                log.warn("商品数量无效: quantity={}", quantity);
                return R.fail("商品数量必须大于0");
            }
            
            String cartKey = CART_KEY_PREFIX + userId;
            String itemKey = productId + ":" + (StringUtils.hasText(specifications) ? specifications : "default");
            
            // 检查购物车中是否已存在该商品
            CartItem existingItem = (CartItem) redisTemplate.opsForHash().get(cartKey, itemKey);
            
            if (existingItem != null) {
                log.info("购物车中已存在该商品，更新数量: 原数量={}, 新增数量={}", existingItem.getQuantity(), quantity);
                
                // 验证库存
                R<ProductDTO> productResult = productClient.getProductById(productId);
                if (!isValidProductResult(productResult)) {
                    log.error("获取商品信息失败: productId={}", productId);
                    return R.fail("商品信息不存在");
                }
                
                ProductDTO product = productResult.getData();
                int newQuantity = existingItem.getQuantity() + quantity;
                
                // 检查库存是否充足
                if (product.getStock() < newQuantity) {
                    log.warn("库存不足: 商品ID={}, 需要数量={}, 可用库存={}", productId, newQuantity, product.getStock());
                    return R.fail("库存不足，当前可用库存：" + product.getStock());
                }
                
                // 更新数量和价格（价格可能有变化）
                existingItem.setQuantity(newQuantity);
                existingItem.setPrice(product.getPrice());
                existingItem.setProductName(product.getName());
                existingItem.setProductImage(product.getImage());
                
                redisTemplate.opsForHash().put(cartKey, itemKey, existingItem);
                log.info("购物车商品数量更新成功: 新数量={}", newQuantity);
            } else {
                log.info("购物车中不存在该商品，创建新的购物车项");
                
                // 调用商品服务获取商品信息
                R<ProductDTO> productResult = productClient.getProductById(productId);
                if (!isValidProductResult(productResult)) {
                    log.error("获取商品信息失败: productId={}", productId);
                    return R.fail("商品信息不存在");
                }
                
                ProductDTO product = productResult.getData();
                
                // 检查商品状态和库存
                if (product.getStatus() != 1) {
                    log.warn("商品已下架: productId={}, status={}", productId, product.getStatus());
                    return R.fail("商品已下架");
                }
                if (product.getStock() < quantity) {
                    log.warn("库存不足: 商品ID={}, 需要数量={}, 可用库存={}", productId, quantity, product.getStock());
                    return R.fail("库存不足，当前可用库存：" + product.getStock());
                }
                
                // 创建新的购物车项
                CartItem newItem = new CartItem();
                newItem.setUserId(userId);
                newItem.setProductId(productId);
                newItem.setQuantity(quantity);
                newItem.setSelected(true);
                newItem.setSpecifications(specifications);
                newItem.setProductName(product.getName());
                newItem.setPrice(product.getPrice());
                newItem.setProductImage(product.getImage());
                
                redisTemplate.opsForHash().put(cartKey, itemKey, newItem);
                log.info("新购物车项创建成功: productName={}, price={}", product.getName(), product.getPrice());
            }
            
            // 设置过期时间
            redisTemplate.expire(cartKey, CART_EXPIRE_TIME, TimeUnit.DAYS);
            
            log.info("商品添加到购物车成功: userId={}, productId={}", userId, productId);
            return R.ok();
            
        } catch (Exception e) {
            log.error("添加商品到购物车失败: userId={}, productId={}", userId, productId, e);
            return R.fail("添加商品到购物车失败：" + e.getMessage());
        }
    }

    /**
     * 更新购物车商品数量
     * 
     * @param userId 用户ID
     * @param productId 商品ID
     * @param quantity 新数量
     * @return 操作结果
     */
    @Override
    public R<Void> updateQuantity(Long userId, Long productId, Integer quantity) {
        try {
            log.info("更新购物车商品数量开始: userId={}, productId={}, quantity={}", userId, productId, quantity);
            
            // 参数验证
            if (userId == null || userId <= 0) {
                return R.fail("用户ID无效");
            }
            if (productId == null || productId <= 0) {
                return R.fail("商品ID无效");
            }
            if (quantity == null || quantity < 0) {
                return R.fail("商品数量不能为负数");
            }
            
            String cartKey = CART_KEY_PREFIX + userId;
            
            // 如果数量为0，删除该商品
            if (quantity == 0) {
                log.info("数量为0，删除购物车商品: userId={}, productId={}", userId, productId);
                return removeFromCart(userId, productId);
            }
            
            // 查找购物车中的商品
            Map<Object, Object> cartItems = redisTemplate.opsForHash().entries(cartKey);
            boolean found = false;
            
            for (Map.Entry<Object, Object> entry : cartItems.entrySet()) {
                String itemKey = (String) entry.getKey();
                CartItem item = (CartItem) entry.getValue();
                
                if (item.getProductId().equals(productId)) {
                    // 验证库存
                    R<ProductDTO> productResult = productClient.getProductById(productId);
                    if (!isValidProductResult(productResult)) {
                        log.error("获取商品信息失败: productId={}", productId);
                        return R.fail("商品信息不存在");
                    }
                    
                    ProductDTO product = productResult.getData();
                    if (product.getStock() < quantity) {
                        log.warn("库存不足: 商品ID={}, 需要数量={}, 可用库存={}", productId, quantity, product.getStock());
                        return R.fail("库存不足，当前可用库存：" + product.getStock());
                    }
                    
                    // 更新数量和价格
                    item.setQuantity(quantity);
                    item.setPrice(product.getPrice());
                    item.setProductName(product.getName());
                    item.setProductImage(product.getImage());
                    
                    redisTemplate.opsForHash().put(cartKey, itemKey, item);
                    found = true;
                    log.info("购物车商品数量更新成功: userId={}, productId={}, newQuantity={}", userId, productId, quantity);
                    break;
                }
            }
            
            if (!found) {
                log.warn("购物车中未找到指定商品: userId={}, productId={}", userId, productId);
                return R.fail("购物车中未找到该商品");
            }
            
            return R.ok();
            
        } catch (Exception e) {
            log.error("更新购物车商品数量失败: userId={}, productId={}", userId, productId, e);
            return R.fail("更新商品数量失败：" + e.getMessage());
        }
    }

    /**
     * 从购物车删除商品
     * 
     * @param userId 用户ID
     * @param productId 商品ID
     * @return 操作结果
     */
    @Override
    public R<Void> removeFromCart(Long userId, Long productId) {
        try {
            log.info("从购物车删除商品开始: userId={}, productId={}", userId, productId);
            
            // 参数验证
            if (userId == null || userId <= 0) {
                return R.fail("用户ID无效");
            }
            if (productId == null || productId <= 0) {
                return R.fail("商品ID无效");
            }
            
            String cartKey = CART_KEY_PREFIX + userId;
            Map<Object, Object> cartItems = redisTemplate.opsForHash().entries(cartKey);
            
            boolean found = false;
            for (Object itemKey : cartItems.keySet()) {
                CartItem item = (CartItem) cartItems.get(itemKey);
                if (item.getProductId().equals(productId)) {
                    redisTemplate.opsForHash().delete(cartKey, itemKey);
                    found = true;
                    log.info("商品从购物车删除成功: userId={}, productId={}", userId, productId);
                    break;
                }
            }
            
            if (!found) {
                log.warn("购物车中未找到指定商品: userId={}, productId={}", userId, productId);
                return R.fail("购物车中未找到该商品");
            }
            
            return R.ok();
            
        } catch (Exception e) {
            log.error("从购物车删除商品失败: userId={}, productId={}", userId, productId, e);
            return R.fail("删除商品失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户购物车列表
     * 
     * @param userId 用户ID
     * @return 购物车商品列表
     */
    @Override
    public R<List<CartItem>> getCartItems(Long userId) {
        try {
            log.info("获取购物车商品列表开始: userId={}", userId);
            
            // 参数验证
            if (userId == null || userId <= 0) {
                return R.fail("用户ID无效");
            }
            
            String cartKey = CART_KEY_PREFIX + userId;
            Map<Object, Object> cartItems = redisTemplate.opsForHash().entries(cartKey);
            
            List<CartItem> items = new ArrayList<>();
            for (Object value : cartItems.values()) {
                CartItem item = (CartItem) value;
                
                // 验证商品信息和库存状态
                try {
                    R<ProductDTO> productResult = productClient.getProductById(item.getProductId());
                    if (isValidProductResult(productResult)) {
                        ProductDTO product = productResult.getData();
                        
                        // 更新商品信息（价格、名称、图片可能有变化）
                        item.setPrice(product.getPrice());
                        item.setProductName(product.getName());
                        item.setProductImage(product.getImage());
                        
                        // 检查库存状态
                        if (product.getStatus() != 1) {
                            item.setSelected(false); // 下架商品自动取消选中
                            log.warn("购物车中商品已下架: productId={}", item.getProductId());
                        } else if (product.getStock() < item.getQuantity()) {
                            item.setSelected(false); // 库存不足自动取消选中
                            log.warn("购物车中商品库存不足: productId={}, 需要数量={}, 可用库存={}", 
                                item.getProductId(), item.getQuantity(), product.getStock());
                        }
                    } else {
                        // 商品不存在，标记为无效
                        item.setSelected(false);
                        log.warn("购物车中商品信息获取失败: productId={}", item.getProductId());
                    }
                } catch (Exception e) {
                    log.error("验证购物车商品信息失败: productId={}", item.getProductId(), e);
                    item.setSelected(false);
                }
                
                items.add(item);
            }
            
            log.info("获取购物车商品列表成功: userId={}, 商品数量={}", userId, items.size());
            return R.ok(items);
            
        } catch (Exception e) {
            log.error("获取购物车商品列表失败: userId={}", userId, e);
            return R.fail("获取购物车列表失败：" + e.getMessage());
        }
    }

    /**
     * 清空购物车
     * 
     * @param userId 用户ID
     * @return 操作结果
     */
    @Override
    public R<Void> clearCart(Long userId) {
        try {
            log.info("清空购物车开始: userId={}", userId);
            
            // 参数验证
            if (userId == null || userId <= 0) {
                return R.fail("用户ID无效");
            }
            
            String cartKey = CART_KEY_PREFIX + userId;
            redisTemplate.delete(cartKey);
            
            log.info("购物车清空成功: userId={}", userId);
            return R.ok();
            
        } catch (Exception e) {
            log.error("清空购物车失败: userId={}", userId, e);
            return R.fail("清空购物车失败：" + e.getMessage());
        }
    }

    /**
     * 选中/取消选中购物车商品
     * 
     * @param userId 用户ID
     * @param productId 商品ID
     * @param selected 是否选中
     * @return 操作结果
     */
    @Override
    public R<Void> selectItem(Long userId, Long productId, Boolean selected) {
        try {
            log.info("选中/取消选中购物车商品开始: userId={}, productId={}, selected={}", userId, productId, selected);
            
            // 参数验证
            if (userId == null || userId <= 0) {
                return R.fail("用户ID无效");
            }
            if (productId == null || productId <= 0) {
                return R.fail("商品ID无效");
            }
            if (selected == null) {
                return R.fail("选中状态不能为空");
            }
            
            String cartKey = CART_KEY_PREFIX + userId;
            Map<Object, Object> cartItems = redisTemplate.opsForHash().entries(cartKey);
            
            boolean found = false;
            for (Map.Entry<Object, Object> entry : cartItems.entrySet()) {
                String itemKey = (String) entry.getKey();
                CartItem item = (CartItem) entry.getValue();
                
                if (item.getProductId().equals(productId)) {
                    // 如果要选中商品，需要验证商品状态和库存
                    if (selected) {
                        R<ProductDTO> productResult = productClient.getProductById(productId);
                        if (!isValidProductResult(productResult)) {
                            log.error("获取商品信息失败: productId={}", productId);
                            return R.fail("商品信息不存在，无法选中");
                        }
                        
                        ProductDTO product = productResult.getData();
                        if (product.getStatus() != 1) {
                            log.warn("商品已下架，无法选中: productId={}", productId);
                            return R.fail("商品已下架，无法选中");
                        }
                        if (product.getStock() < item.getQuantity()) {
                            log.warn("库存不足，无法选中: productId={}, 需要数量={}, 可用库存={}", 
                                productId, item.getQuantity(), product.getStock());
                            return R.fail("库存不足，无法选中。当前可用库存：" + product.getStock());
                        }
                    }
                    
                    item.setSelected(selected);
                    redisTemplate.opsForHash().put(cartKey, itemKey, item);
                    found = true;
                    log.info("购物车商品选中状态更新成功: userId={}, productId={}, selected={}", userId, productId, selected);
                    break;
                }
            }
            
            if (!found) {
                log.warn("购物车中未找到指定商品: userId={}, productId={}", userId, productId);
                return R.fail("购物车中未找到该商品");
            }
            
            return R.ok();
            
        } catch (Exception e) {
            log.error("选中/取消选中购物车商品失败: userId={}, productId={}", userId, productId, e);
            return R.fail("操作失败：" + e.getMessage());
        }
    }

    /**
     * 获取购物车商品数量
     * 
     * @param userId 用户ID
     * @return 商品数量
     */
    @Override
    public R<Integer> getCartCount(Long userId) {
        try {
            log.info("获取购物车商品数量开始: userId={}", userId);
            
            // 参数验证
            if (userId == null || userId <= 0) {
                return R.fail("用户ID无效");
            }
            
            String cartKey = CART_KEY_PREFIX + userId;
            Map<Object, Object> cartItems = redisTemplate.opsForHash().entries(cartKey);
            
            int totalCount = 0;
            for (Object value : cartItems.values()) {
                CartItem item = (CartItem) value;
                totalCount += item.getQuantity();
            }
            
            log.info("获取购物车商品数量成功: userId={}, totalCount={}", userId, totalCount);
            return R.ok(totalCount);
            
        } catch (Exception e) {
            log.error("获取购物车商品数量失败: userId={}", userId, e);
            return R.fail("获取购物车数量失败：" + e.getMessage());
        }
    }
    
    /**
     * 验证商品服务返回结果是否有效
     * 
     * @param productResult 商品服务返回结果
     * @return 是否有效
     */
    private boolean isValidProductResult(R<ProductDTO> productResult) {
        return productResult != null && productResult.getCode() == 200 && productResult.getData() != null;
    }
}