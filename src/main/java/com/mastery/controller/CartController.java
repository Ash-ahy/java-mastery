package com.mastery.controller;

import com.mastery.common.Result;
import com.mastery.entity.CartItem;
import com.mastery.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    
    private final CartService cartService;
    
    @GetMapping
    public Result<List<CartItem>> list(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(cartService.listByUser(userId));
    }
    
    @PostMapping
    public Result<Void> add(Authentication auth, @RequestBody Map<String, Object> body) {
        Long userId = (Long) auth.getPrincipal();
        cartService.add(userId, Long.valueOf(body.get("productId").toString()), Integer.valueOf(body.get("quantity").toString()));
        return Result.success();
    }
    
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        cartService.updateQuantity(id, Integer.valueOf(body.get("quantity").toString()));
        return Result.success();
    }
    
    @PutMapping("/{id}/check")
    public Result<Void> check(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        cartService.check(id, body.get("checked"));
        return Result.success();
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) { cartService.remove(id); return Result.success(); }
}
