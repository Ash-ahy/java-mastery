package com.mastery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mastery.entity.CartItem;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

public interface CartItemMapper extends BaseMapper<CartItem> {
    @Delete("DELETE FROM cart_item WHERE user_id = #{userId} AND checked = 1")
    int deleteChecked(@Param("userId") Long userId);
}
