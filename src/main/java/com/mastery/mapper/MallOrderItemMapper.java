package com.mastery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mastery.entity.MallOrderItem;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface MallOrderItemMapper extends BaseMapper<MallOrderItem> {

    @Select("SELECT * FROM mall_order_item WHERE order_id = #{orderId}")
    List<MallOrderItem> selectByOrderId(@Param("orderId") Long orderId);
}
