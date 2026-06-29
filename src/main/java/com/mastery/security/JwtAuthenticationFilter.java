package com.mastery.security;

import com.mastery.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT认证过滤器
 * 每次请求时从Header中提取Token并验证
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtUtil jwtUtil;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String token = extractToken(request);
        
        if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
            Long userId = jwtUtil.getUserId(token);
            String username = jwtUtil.getUsername(token);
            List<String> roles = jwtUtil.getRoles(token);
            List<String> permissions = jwtUtil.getPermissions(token);
            
            // 构建认证对象 — 角色和权限都作为 authorities
            List<SimpleGrantedAuthority> authorities = new java.util.ArrayList<>();
            if (roles != null) roles.forEach(r -> authorities.add(new SimpleGrantedAuthority(r)));
            if (permissions != null) permissions.forEach(p -> authorities.add(new SimpleGrantedAuthority(p)));
            
            LoginUser loginUser = new LoginUser(userId, username, roles, permissions);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(loginUser, null, authorities);
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            log.debug("JWT认证成功: userId={}, username={}", userId, username);
        }
        
        chain.doFilter(request, response);
    }
    
    /**
     * 从请求头提取Token
     */
    private String extractToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
