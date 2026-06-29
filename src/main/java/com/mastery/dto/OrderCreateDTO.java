package com.mastery.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 创建订单请求
 */
@Data
public class OrderCreateDTO {
    
    @NotEmpty(message = "订单明细不能为空")
    private List<OrderItemDTO> items;
    
    private String payType;
    
    private String receiverName;
    
    private String receiverPhone;
    
    private String receiverAddress;
    
    private String remark;
    
    @Data
    public static class OrderItemDTO {
        @NotNull(message = "商品ID不能为空")
        private Long productId;
        
        @NotNull(message = "数量不能为空")
        private Integer quantity;
    }
}
