package com.mall.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.admin.domain.entity.Admin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 管理员Mapper
 */
@Mapper
public interface AdminMapper extends BaseMapper<Admin> {
    
    /**
     * 根据管理员ID查询权限列表
     */
    List<String> selectPermissionsByAdminId(@Param("adminId") Long adminId);
}
