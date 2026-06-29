package com.mastery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mastery.entity.MallOrder;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 订单Mapper
 */
public interface MallOrderMapper extends BaseMapper<MallOrder> {
    
    /**
     * 统计订单数量（按状态）
     */
    @Select("""
        SELECT status, COUNT(*) as count FROM mall_order
        WHERE deleted = 0
        GROUP BY status
    """)
    List<Map<String, Object>> countByStatus();
    
    /**
     * 统计今日交易额
     */
    @Select("""
        SELECT COALESCE(SUM(pay_amount), 0) FROM mall_order
        WHERE status IN ('PAID', 'SHIPPING', 'DELIVERED')
        AND DATE(pay_time) = CURDATE()
    """)
    java.math.BigDecimal todayTurnover();
}
