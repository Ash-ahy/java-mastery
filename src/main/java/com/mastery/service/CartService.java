package com.mastery.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mastery.entity.CartItem;
import java.util.List;

public interface CartService extends IService<CartItem> {
    void add(Long userId, Long productId, Integer quantity);
    void updateQuantity(Long id, Integer quantity);
    void remove(Long id);
    List<CartItem> listByUser(Long userId);
    void check(Long id, Boolean checked);
    void clearChecked(Long userId);
}
