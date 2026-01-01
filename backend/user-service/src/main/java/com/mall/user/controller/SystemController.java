package com.mall.user.controller;

import com.mall.common.core.domain.R;
import com.mall.common.core.utils.TimeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 系统相关接口控制器
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-01
 */
@RestController
@RequestMapping({ "/api/users/system", "/users/system", "/system" })
public class SystemController {
    
    /**
     * 获取服务器时间
     * 公开接口，无需认证
     * 
     * @return 服务器时间信息
     */
    @GetMapping("/time")
    public R<Map<String, Object>> getServerTime() {
        Map<String, Object> timeInfo = new HashMap<>();
        
        // 当前时间戳（毫秒）
        timeInfo.put("timestamp", TimeUtils.currentTimeMillis());
        
        // 当前时间戳（秒）
        timeInfo.put("timestampSeconds", TimeUtils.currentTimeSeconds());
        
        // 格式化时间字符串
        timeInfo.put("datetime", TimeUtils.nowString());
        
        // 日期字符串
        timeInfo.put("date", TimeUtils.nowDateString());
        
        // 星期几
        timeInfo.put("weekday", TimeUtils.getCurrentWeekDay());
        
        // 时区信息
        timeInfo.put("timezone", "Asia/Shanghai");
        timeInfo.put("timezoneOffset", "+08:00");
        
        // ISO 8601 格式
        timeInfo.put("iso8601", TimeUtils.now().toString());
        
        return R.ok(timeInfo);
    }
}

