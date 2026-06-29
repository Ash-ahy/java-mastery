package com.mastery.controller;

import com.mastery.common.Result;
import com.mastery.security.LoginUser;
import com.mastery.service.SeckillService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/seckill")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.features.seckill", name = "enabled", havingValue = "true")
public class SeckillController {
    
    private final SeckillService seckillService;
    private final StringRedisTemplate redisTemplate;
    
    @PostMapping("/{productId}")
    public Result<Map<String, Object>> seckill(Authentication auth, @PathVariable Long productId) {
        Long userId = currentUserId(auth);
        return Result.success(seckillService.seckill(productId, userId));
    }
    
    @GetMapping("/result")
    public Result<Map<String, Object>> result(Authentication auth) {
        Long userId = currentUserId(auth);
        return Result.success(seckillService.getResult(userId));
    }
    
    @PreAuthorize("hasAuthority('sys:seckill:init')")
    @PostMapping("/init/{productId}")
    public Result<Void> initStock(@PathVariable Long productId, @RequestParam(defaultValue = "100") int stock) {
        redisTemplate.opsForValue().set("seckill:stock:" + productId, String.valueOf(stock));
        return Result.success();
    }

    private Long currentUserId(Authentication auth) {
        Object principal = auth.getPrincipal();
        if (principal instanceof LoginUser loginUser) {
            return loginUser.getUserId();
        }
        throw new IllegalStateException("Unsupported authentication principal: " + principal);
    }
}
