package com.mastery.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品创建/更新请求
 */
@Data
public class ProductSaveDTO {

    @NotBlank(message = "商品名称不能为空")
    private String name;

    private String description;

    private Long categoryId;

    @NotNull(message = "价格不能为空")
    private BigDecimal price;

    @NotNull(message = "库存不能为空")
    private Integer stock;

    private String images;

    private Integer status;
}
