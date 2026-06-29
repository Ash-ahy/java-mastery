package com.mastery.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mastery.common.PageResult;
import com.mastery.dto.OrderCreateDTO;
import com.mastery.entity.MallOrder;
import com.mastery.entity.MallOrderItem;

import java.util.List;
import java.util.Map;

/**
 * 订单服务接口
 */
public interface OrderService extends IService<MallOrder> {
    
    /** 创建订单（事务+库存扣减+消息队列） */
    MallOrder createOrder(Long userId, OrderCreateDTO dto);
    
    /** 支付订单 */
    void payOrder(Long orderId);
    
    /** 取消订单（恢复库存） */
    void cancelOrder(Long orderId);
    
    /** 订单统计 */
    Map<String, Object> getStatistics();

    /** 获取订单明细 */
    List<MallOrderItem> getOrderItems(Long orderId);

    /** 分页查询订单 */
    PageResult<MallOrder> queryPage(int page, int size, String status);
}
