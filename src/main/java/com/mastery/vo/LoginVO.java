package com.mastery.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 登录响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO {
    
    private String token;
    
    private String tokenType;
    
    private Long userId;
    
    private String username;
    
    private String nickname;
    
    private String avatar;
    
    private List<String> roles;
    
    private List<String> permissions;
}
