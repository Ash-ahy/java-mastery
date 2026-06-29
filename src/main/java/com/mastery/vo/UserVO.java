package com.mastery.vo;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class UserVO {
    private Long id;
    private String username;
    private String nickname;
    private String phone;
    private String email;
    private String avatar;
    private Integer gender;
    private Integer age;
    private Integer status;
    private LocalDateTime lastLoginTime;
    private LocalDateTime createTime;
    private List<String> roles;
    private List<String> permissions;
}
