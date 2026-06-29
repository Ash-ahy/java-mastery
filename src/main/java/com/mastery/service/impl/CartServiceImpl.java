package com.mastery.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mastery.entity.CartItem;
import com.mastery.exception.BusinessException;
import com.mastery.mapper.CartItemMapper;
import com.mastery.service.CartService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CartServiceImpl extends ServiceImpl<CartItemMapper, CartItem> implements CartService {
    
    @Override
    @Transactional
    public void add(Long userId, Long productId, Integer quantity) {
        CartItem exist = getOne(new LambdaQueryWrapper<CartItem>()
            .eq(CartItem::getUserId, userId).eq(CartItem::getProductId, productId));
        if (exist != null) {
            exist.setQuantity(exist.getQuantity() + quantity);
            updateById(exist);
        } else {
            CartItem item = new CartItem();
            item.setUserId(userId); item.setProductId(productId);
            item.setQuantity(quantity); item.setChecked(1);
            save(item);
        }
    }
    
    @Override public void updateQuantity(Long id, Integer quantity) {
        CartItem item = getById(id);
        if (item == null) throw new BusinessException(404, "cart item not found");
        item.setQuantity(quantity); updateById(item);
    }
    
    @Override public void remove(Long id) { removeById(id); }
    
    @Override public List<CartItem> listByUser(Long userId) {
        return list(new LambdaQueryWrapper<CartItem>().eq(CartItem::getUserId, userId));
    }
    
    @Override public void check(Long id, Boolean checked) {
        CartItem item = getById(id);
        if (item != null) { item.setChecked(checked ? 1 : 0); updateById(item); }
    }
    
    @Override public void clearChecked(Long userId) {
        getBaseMapper().deleteChecked(userId);
    }
}
