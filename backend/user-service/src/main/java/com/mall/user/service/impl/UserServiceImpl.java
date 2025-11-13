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

        // 设置userId字段（从 id 复制）
        response.setUserId(user.getId());

        // 设置是否已设置密码标识（password_set_time不为空表示已设置）
        response.setHasSetPassword(user.getPasswordSetTime() != null);

        return response;
    }

    @Override
    public UserInfoResponse getUserInfo(Long userId) {
        // 临时返回null，等待数据库连接恢复
        return null;
    }

    @Override
    public boolean updateUserInfo(UpdateUserRequest request) {
        try {
            // 根据用户名查找用户
            User user = findByUsername(request.getUsername());
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }

            // 更新用户信息（只更新非空字段）
            if (StrUtil.isNotBlank(request.getNickname())) {
                user.setNickname(request.getNickname());
            }
            if (StrUtil.isNotBlank(request.getEmail())) {
                user.setEmail(request.getEmail());
            }
            if (StrUtil.isNotBlank(request.getPhone())) {
                user.setPhone(request.getPhone());
            }
            if (request.getGender() != null) {
                user.setGender(request.getGender());
            }
            if (StrUtil.isNotBlank(request.getBirthday())) {
                user.setBirthday(request.getBirthday());
            }
            if (StrUtil.isNotBlank(request.getAvatar())) {
                user.setAvatar(request.getAvatar());
            }
            if (StrUtil.isNotBlank(request.getBio())) {
                user.setBio(request.getBio());
            }

            // 使用MyBatis-Plus更新
            return updateById(user);
        } catch (Exception e) {
            System.err.println("更新用户信息异常: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("更新用户信息失败: " + e.getMessage());
        }
    }

    @Override
    public boolean changePassword(ChangePasswordRequest request) {
        try {
            // 根据用户名查找用户
            User user = findByUsername(request.getUsername());
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }

            // 验证旧密码
            if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
                throw new RuntimeException("旧密码错误");
            }

            // 设置新密码
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));

            // 更新密码设置时间
            user.setPasswordSetTime(java.time.LocalDateTime.now());

            // 更新密码
            return updateById(user);
        } catch (Exception e) {
            System.err.println("修改密码异常: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("修改密码失败: " + e.getMessage());
        }
    }

    /**
     * 首次设置密码（不需要旧密码）
     * 用于SMS登录的用户首次设置自己的密码
     * 
     * @param username    用户名
     * @param newPassword 新密码
     * @return 设置结果
     */
    public boolean setPassword(String username, String newPassword) {
        try {
            // 根据用户名查找用户
            User user = findByUsername(username);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }

            // 检查是否已经设置过密码
            if (user.getPasswordSetTime() != null) {
                throw new RuntimeException("密码已设置，请使用修改密码功能");
            }

            // 设置新密码
            user.setPassword(passwordEncoder.encode(newPassword));

            // 设置密码设置时间
            user.setPasswordSetTime(java.time.LocalDateTime.now());

            // 更新密码
            return updateById(user);
        } catch (Exception e) {
            System.err.println("设置密码异常: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("设置密码失败: " + e.getMessage());
        }
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

    @Override
    public Map<String, Object> getUserStatistics() {
        Map<String, Object> stats = new HashMap<>();

        try {
            // 统计总用户数
            Long total = userMapper.selectCount(null);
            stats.put("total", total != null ? total : 0);

            // 统计活跃用户数（状态为1）
            LambdaQueryWrapper<User> activeWrapper = new LambdaQueryWrapper<>();
            activeWrapper.eq(User::getStatus, 1);
            Long active = userMapper.selectCount(activeWrapper);
            stats.put("active", active != null ? active : 0);

            // 统计禁用用户数（状态为0）
            LambdaQueryWrapper<User> disabledWrapper = new LambdaQueryWrapper<>();
            disabledWrapper.eq(User::getStatus, 0);
            Long disabled = userMapper.selectCount(disabledWrapper);
            stats.put("disabled", disabled != null ? disabled : 0);

            // 统计待验证用户数（暂时设置为0，后续根据实际业务调整）
            stats.put("pending", 0);

            return stats;

        } catch (Exception e) {
            System.err.println("获取用户统计数据异常: " + e.getMessage());
            e.printStackTrace();
            // 返回默认值
            stats.put("total", 0);
            stats.put("active", 0);
            stats.put("disabled", 0);
            stats.put("pending", 0);
            return stats;
        }
    }

    /**
     * 获取用户列表（分页查询）
     *
     * @param page    页码
     * @param size    每页大小
     * @param keyword 关键词（搜索用户名、手机号、邮箱）
     * @param status  状态（0-禁用, 1-正常）
     * @return 分页结果
     */
    public com.mall.common.core.domain.PageResult<User> getUserList(Integer page, Integer size, String keyword,
            Integer status) {
        try {
            // 构建查询条件
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();

            // 关键词搜索：用户名、手机号、邮箱
            if (StrUtil.isNotBlank(keyword)) {
                queryWrapper.and(wrapper -> wrapper
                        .like(User::getUsername, keyword)
                        .or().like(User::getPhone, keyword)
                        .or().like(User::getEmail, keyword)
                        .or().like(User::getNickname, keyword));
            }

            // 状态筛选
            if (status != null) {
                queryWrapper.eq(User::getStatus, status);
            }

            // 按创建时间降序
            queryWrapper.orderByDesc(User::getCreateTime);

            // 分页查询
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<User> mybatisPlusPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                    page, size);

            com.baomidou.mybatisplus.extension.plugins.pagination.Page<User> resultPage = userMapper
                    .selectPage(mybatisPlusPage, queryWrapper);

            // 转换为通用 PageResult
            com.mall.common.core.domain.PageResult<User> pageResult = new com.mall.common.core.domain.PageResult<>();
            pageResult.setRecords(resultPage.getRecords());
            pageResult.setTotal(resultPage.getTotal());
            pageResult.setCurrent(resultPage.getCurrent());
            pageResult.setSize(resultPage.getSize());
            pageResult.setPages(resultPage.getPages());

            return pageResult;

        } catch (Exception e) {
            System.err.println("获取用户列表异常: " + e.getMessage());
            e.printStackTrace();
            throw new BusinessException("获取用户列表失败: " + e.getMessage());
        }
    }
}