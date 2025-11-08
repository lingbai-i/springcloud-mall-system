package com.mall.user.service;

// import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.user.domain.dto.ChangePasswordRequest;
import com.mall.user.domain.dto.UpdateUserRequest;
import com.mall.user.domain.entity.User;
import com.mall.user.domain.vo.UserInfoResponse;

/**
 * 用户服务接口
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-27
 */
public interface UserService /* extends IService<User> */ {
    
    /**
     * 根据用户名查询用户
     * 
     * @param username 用户名
     * @return 用户信息
     */
    User findByUsername(String username);
    
    /**
     * 根据邮箱查询用户
     * 
     * @param email 邮箱
     * @return 用户信息
     */
    User findByEmail(String email);
    
    /**
     * 根据手机号查询用户
     * 
     * @param phone 手机号
     * @return 用户信息
     */
    User findByPhone(String phone);
    
    /**
     * 获取用户信息
     * 
     * @param userId 用户ID
     * @return 用户信息响应
     */
    UserInfoResponse getUserInfo(Long userId);
    
    /**
     * 根据用户名获取用户信息
     * 
     * @param username 用户名
     * @return 用户信息响应
     */
    UserInfoResponse getUserInfo(String username);
    
    /**
     * 更新用户信息
     * 
     * @param updateRequest 更新请求
     * @return 更新结果
     */
    boolean updateUserInfo(UpdateUserRequest updateRequest);
    
    /**
     * 修改密码
     * 
     * @param changePasswordRequest 修改密码请求
     * @return 修改结果
     */
    boolean changePassword(ChangePasswordRequest changePasswordRequest);
    
    /**
     * 检查用户名是否唯一
     * 
     * @param username 用户名
     * @return 是否唯一
     */
    boolean isUsernameUnique(String username);
    
    /**
     * 检查邮箱是否唯一
     * 
     * @param email 邮箱
     * @return 是否唯一
     */
    boolean isEmailUnique(String email);
    
    /**
     * 检查手机号是否唯一
     * 
     * @param phone 手机号
     * @return 是否唯一
     */
    boolean isPhoneUnique(String phone);
    
    /**
     * 检查用户名是否可用
     * 
     * @param username 用户名
     * @return true-可用，false-不可用
     */
    boolean isUsernameAvailable(String username);
    
    /**
     * 检查邮箱是否可用
     * 
     * @param email 邮箱
     * @return true-可用，false-不可用
     */
    boolean isEmailAvailable(String email);
    
    /**
     * 检查手机号是否可用
     * 
     * @param phone 手机号
     * @return true-可用，false-不可用
     */
    boolean isPhoneAvailable(String phone);
    
    /**
     * 插入用户
     * 
     * @param user 用户信息
     * @return true-成功，false-失败
     */
    boolean insertUser(User user);
    
    /**
     * 更新用户信息
     * 
     * @param user 用户信息
     * @return true-成功，false-失败
     */
    boolean updateById(User user);
}