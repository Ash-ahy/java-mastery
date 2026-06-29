package com.mastery.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 操作日志拦截器 - 记录每个请求的耗时
 */
@Slf4j
@Component
public class LogInterceptor implements HandlerInterceptor {
    
    private static final ThreadLocal<Long> START_TIME = new ThreadLocal<>();
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        START_TIME.set(System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        try {
            Long start = START_TIME.get();
            if (start != null) {
                long elapsed = System.currentTimeMillis() - start;
                if (elapsed > 1000) {
                    log.warn("慢请求 {} {} → {}ms", request.getMethod(), request.getRequestURI(), elapsed);
                } else {
                    log.info("{} {} → {}ms", request.getMethod(), request.getRequestURI(), elapsed);
                }
            }
        } finally {
            START_TIME.remove();
        }
    }
}
