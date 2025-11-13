package com.mall.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.user.domain.entity.Address;
import org.apache.ibatis.annotations.Mapper;

/**
 * 地址Mapper接口
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
@Mapper
public interface AddressMapper extends BaseMapper<Address> {
}
