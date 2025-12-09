package com.mall.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.product.domain.entity.StockLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 库存操作日志数据访问层接口
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-12-01
 */
@Mapper
public interface StockLogMapper extends BaseMapper<StockLog> {
}
