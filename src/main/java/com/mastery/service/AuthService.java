package com.mastery.service;

import com.mastery.dto.LoginDTO;
import com.mastery.vo.LoginVO;

/**
 * 认证服务接口
 */
public interface AuthService {
    
    /** 登录 */
    LoginVO login(LoginDTO dto);
    
    /** 刷新Token */
    LoginVO refreshToken(String token);
}
