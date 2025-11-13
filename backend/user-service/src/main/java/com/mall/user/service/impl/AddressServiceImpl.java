package com.mall.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.mall.user.domain.entity.Address;
import com.mall.user.mapper.AddressMapper;
import com.mall.user.service.AddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 地址Service实现类
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

  private final AddressMapper addressMapper;

  @Override
  public List<Address> getUserAddresses(Long userId) {
    LambdaQueryWrapper<Address> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(Address::getUserId, userId)
        .orderByDesc(Address::getIsDefault)
        .orderByDesc(Address::getUpdatedTime);
    return addressMapper.selectList(wrapper);
  }

  @Override
  public Address getAddressById(Long id, Long userId) {
    LambdaQueryWrapper<Address> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(Address::getId, id)
        .eq(Address::getUserId, userId);
    return addressMapper.selectOne(wrapper);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Address addAddress(Address address) {
    // 如果设置为默认地址,先取消其他默认地址
    if (Boolean.TRUE.equals(address.getIsDefault())) {
      cancelOtherDefaultAddress(address.getUserId());
    }

    addressMapper.insert(address);
    return address;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Address updateAddress(Long id, Address address, Long userId) {
    // 验证地址是否属于当前用户
    Address existAddress = getAddressById(id, userId);
    if (existAddress == null) {
      throw new RuntimeException("地址不存在或无权限修改");
    }

    // 如果设置为默认地址,先取消其他默认地址
    if (Boolean.TRUE.equals(address.getIsDefault())) {
      cancelOtherDefaultAddress(userId);
    }

    address.setId(id);
    address.setUserId(userId);
    addressMapper.updateById(address);
    return address;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public boolean deleteAddress(Long id, Long userId) {
    LambdaQueryWrapper<Address> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(Address::getId, id)
        .eq(Address::getUserId, userId);
    return addressMapper.delete(wrapper) > 0;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public boolean setDefaultAddress(Long id, Long userId) {
    // 验证地址是否属于当前用户
    Address address = getAddressById(id, userId);
    if (address == null) {
      throw new RuntimeException("地址不存在或无权限修改");
    }

    // 取消其他默认地址
    cancelOtherDefaultAddress(userId);

    // 设置为默认地址
    address.setIsDefault(true);
    addressMapper.updateById(address);
    return true;
  }

  /**
   * 取消用户的其他默认地址
   */
  private void cancelOtherDefaultAddress(Long userId) {
    LambdaUpdateWrapper<Address> wrapper = new LambdaUpdateWrapper<>();
    wrapper.eq(Address::getUserId, userId)
        .eq(Address::getIsDefault, true)
        .set(Address::getIsDefault, false);
    addressMapper.update(null, wrapper);
  }
}
