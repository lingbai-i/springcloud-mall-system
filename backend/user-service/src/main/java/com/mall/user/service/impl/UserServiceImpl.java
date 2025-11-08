package com.mall.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.core.exception.BusinessException;
import com.mall.user.domain.dto.ChangePasswordRequest;
import com.mall.user.domain.dto.UpdateUserRequest;
import com.mall.user.domain.entity.User;
import com.mall.user.domain.vo.UserInfoResponse;
import com.mall.user.mapper.UserMapper;
import com.mall.user.service.UserService;
import cn.hutool.core.util.StrUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public User findByUsername(String username) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        return userMapper.selectOne(queryWrapper);
    }

    @Override
    public User findByEmail(String email) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getEmail, email);
        return userMapper.selectOne(queryWrapper);
    }

    @Override
    public User findByPhone(String phone) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getPhone, phone);
        return userMapper.selectOne(queryWrapper);
    }

    @Override
    public boolean isUsernameUnique(String username) {
        User user = findByUsername(username);
        return user == null;
    }

    @Override
    public boolean isPhoneUnique(String phone) {
        User user = findByPhone(phone);
        return user == null;
    }

    @Override
    public boolean isEmailUnique(String email) {
        User user = findByEmail(email);
        return user == null;
    }

    @Override
    public boolean isUsernameAvailable(String username) {
        return isUsernameUnique(username);
    }

    @Override
    public boolean isEmailAvailable(String email) {
        return isEmailUnique(email);
    }

    @Override
    public boolean isPhoneAvailable(String phone) {
        return isPhoneUnique(phone);
    }

    @Override
    public UserInfoResponse getUserInfo(String username) {
        User user = findByUsername(username);
        if (user == null) {
            return null;
        }
        UserInfoResponse response = new UserInfoResponse();
        BeanUtils.copyProperties(user, response);
        return response;
    }

    @Override
    public UserInfoResponse getUserInfo(Long userId) {
        // 临时返回null，等待数据库连接恢复
        return null;
    }

    @Override
    public boolean updateUserInfo(UpdateUserRequest request) {
        // 临时返回false，等待数据库连接恢复
        return false;
    }

    @Override
    public boolean changePassword(ChangePasswordRequest request) {
        // 临时返回false，等待数据库连接恢复
        return false;
    }

    @Override
    public boolean insertUser(User user) {
        try {
            // 设置创建时间和更新时间
            user.setCreateTime(java.time.LocalDateTime.now());
            user.setUpdateTime(java.time.LocalDateTime.now());

            // 使用MyBatis-Plus保存到数据库
            int result = userMapper.insert(user);

            if (result > 0) {
                System.out.println("用户保存到数据库成功: " + user.getUsername() + ", ID: " + user.getId());
                return true;
            } else {
                System.err.println("用户保存失败: " + user.getUsername());
                return false;
            }
        } catch (Exception e) {
            System.err.println("用户保存异常: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean updateById(User user) {
        try {
            // 设置更新时间
            user.setUpdateTime(java.time.LocalDateTime.now());
            
            // 使用MyBatis-Plus更新数据库
            int result = userMapper.updateById(user);
            
            return result > 0;
        } catch (Exception e) {
            System.err.println("用户更新异常: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}