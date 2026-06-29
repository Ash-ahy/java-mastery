package com.mastery.controller;

import com.mastery.common.PageResult;
import com.mastery.common.Result;
import com.mastery.dto.ProductQueryDTO;
import com.mastery.dto.ProductSaveDTO;
import com.mastery.entity.Product;
import com.mastery.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public Result<PageResult<Product>> list(ProductQueryDTO query) {
        return Result.success(productService.queryPage(query));
    }

    @GetMapping("/{id}")
    public Result<Product> detail(@PathVariable Long id) {
        return Result.success(productService.getById(id));
    }

    @PostMapping
    public Result<Product> create(@Valid @RequestBody ProductSaveDTO dto) {
        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setCategoryId(dto.getCategoryId());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setImages(dto.getImages());
        product.setStatus(dto.getStatus() != null ? dto.getStatus() : 0);
        productService.save(product);
        return Result.success(product);
    }

    @PutMapping("/{id}")
    public Result<Product> update(@PathVariable Long id, @Valid @RequestBody ProductSaveDTO dto) {
        Product p = productService.getById(id);
        if (p == null) return Result.notFound("商品不存在");
        if (dto.getName() != null) p.setName(dto.getName());
        if (dto.getDescription() != null) p.setDescription(dto.getDescription());
        if (dto.getCategoryId() != null) p.setCategoryId(dto.getCategoryId());
        if (dto.getPrice() != null) p.setPrice(dto.getPrice());
        if (dto.getStock() != null) p.setStock(dto.getStock());
        if (dto.getImages() != null) p.setImages(dto.getImages());
        if (dto.getStatus() != null) p.setStatus(dto.getStatus());
        productService.updateById(p);
        return Result.success(p);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productService.removeById(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        productService.updateStatus(id, status);
        return Result.success();
    }
}
