-- 评价点赞记录表
-- 用于防止用户重复点赞同一评价
-- @author lingbai
-- @since 2025-12-28

-- 创建点赞记录表
CREATE TABLE IF NOT EXISTS `review_like` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `review_id` bigint NOT NULL COMMENT '评价ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_review_user` (`review_id`, `user_id`) COMMENT '防止重复点赞',
  KEY `idx_review_id` (`review_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价点赞记录表';
