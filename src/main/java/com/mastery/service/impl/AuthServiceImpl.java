package com.mastery.service.impl;

import com.mastery.dto.LoginDTO;
import com.mastery.entity.User;
import com.mastery.exception.BusinessException;
import com.mastery.mapper.UserMapper;
import com.mastery.service.AuthService;
import com.mastery.service.UserService;
import com.mastery.util.JwtUtil;
import com.mastery.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 认证服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    @Override
    public LoginVO login(LoginDTO dto) {
        // 1. 查询用户
        User user = userMapper.selectByUsername(dto.getUsername());
        if (user == null) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        
        // 2. 检查状态
        if (user.getStatus() != 1) {
            throw new BusinessException(403, "账号已被禁用");
        }
        
        // 3. 验证密码
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        
        // 4. 获取角色和权限
        List<String> roles = userMapper.selectRoleCodes(user.getId());
        List<String> permissions = userMapper.selectPermissionCodes(user.getId());
        
        // 5. 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), roles, permissions);
        
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);
        
        log.info("User logged in: username={}", user.getUsername());
        
        return LoginVO.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .roles(roles)
                .permissions(permissions)
                .build();
    }
    
    @Override
    public LoginVO refreshToken(String token) {
        if (!jwtUtil.validateToken(token)) {
            throw new BusinessException(401, "Token已过期");
        }
        
        Long userId = jwtUtil.getUserId(token);
        String username = jwtUtil.getUsername(token);
        List<String> roles = jwtUtil.getRoles(token);
        List<String> permissions = jwtUtil.getPermissions(token);
        
        String newToken = jwtUtil.generateToken(userId, username, roles, permissions);
        
        return LoginVO.builder()
                .token(newToken)
                .tokenType("Bearer")
                .userId(userId)
                .username(username)
                .roles(roles)
                .build();
    }
}
