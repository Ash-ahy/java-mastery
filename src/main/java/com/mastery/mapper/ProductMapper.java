package com.mastery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mastery.dto.ProductQueryDTO;
import com.mastery.entity.Product;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 商品Mapper
 */
public interface ProductMapper extends BaseMapper<Product> {
    
    /**
     * 条件分页查询
     */
    Page<Product> selectPage(Page<Product> page, @Param("query") ProductQueryDTO query);
    
    /**
     * 扣减库存（原子操作）
     */
    @Update("UPDATE product SET stock = stock - #{quantity}, sales = sales + #{quantity} " +
            "WHERE id = #{productId} AND stock >= #{quantity}")
    int deductStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);
    
    /**
     * 恢复库存
     */
    @Update("UPDATE product SET stock = stock + #{quantity} WHERE id = #{productId}")
    int restoreStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);
}
