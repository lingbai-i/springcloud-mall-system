package com.mall.user.service;

import com.mall.user.domain.entity.Address;

import java.util.List;

/**
 * 地址Service接口
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
public interface AddressService {

  /**
   * 获取用户地址列表
   */
  List<Address> getUserAddresses(Long userId);

  /**
   * 获取地址详情
   */
  Address getAddressById(Long id, Long userId);

  /**
   * 添加地址
   */
  Address addAddress(Address address);

  /**
   * 更新地址
   */
  Address updateAddress(Long id, Address address, Long userId);

  /**
   * 删除地址
   */
  boolean deleteAddress(Long id, Long userId);

  /**
   * 设置默认地址
   */
  boolean setDefaultAddress(Long id, Long userId);
}
