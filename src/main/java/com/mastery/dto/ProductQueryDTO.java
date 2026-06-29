package com.mastery.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品搜索条件
 */
@Data
public class ProductQueryDTO {
    
    private String keyword;
    
    private Long categoryId;
    
    private BigDecimal minPrice;
    
    private BigDecimal maxPrice;
    
    private Integer status;
    
    private Integer page = 1;
    
    private Integer size = 10;
}
