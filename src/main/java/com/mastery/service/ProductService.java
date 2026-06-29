package com.mastery.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mastery.common.PageResult;
import com.mastery.dto.ProductQueryDTO;
import com.mastery.entity.Product;

/**
 * 商品服务接口
 */
public interface ProductService extends IService<Product> {
    
    /** 分页搜索 */
    PageResult<Product> queryPage(ProductQueryDTO query);
    
    /** 上架/下架 */
    void updateStatus(Long id, Integer status);
    
    /** 扣减库存（带Redis预扣） */
    boolean deductStock(Long productId, Integer quantity);
    
    /** 恢复库存 */
    void restoreStock(Long productId, Integer quantity);
}
