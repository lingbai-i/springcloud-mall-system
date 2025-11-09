package com.mall.admin.domain.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * \u7528\u6237DTO\uff08\u7528\u4e8e\u66ff\u4ee3\u76f4\u63a5\u4f9d\u8d56user-service\u7684User\u5b9e\u4f53\uff09
 */
@Data
public class UserDTO {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String phone;
    private String avatar;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
