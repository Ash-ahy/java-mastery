package com.mastery.controller;

import com.alibaba.excel.EasyExcel;
import com.mastery.common.Result;
import com.mastery.entity.Product;
import com.mastery.entity.User;
import com.mastery.service.ProductService;
import com.mastery.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/excel")
@RequiredArgsConstructor
public class ExcelController {
    
    private final UserService userService;
    private final ProductService productService;
    
    @PreAuthorize("hasAuthority('sys:user:list')")
    @GetMapping("/export/users")
    public void exportUsers(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        String name = URLEncoder.encode("用户列表", StandardCharsets.UTF_8);
        response.setHeader("Content-Disposition", "attachment;filename=" + name + ".xlsx");
        List<User> users = userService.list();
        EasyExcel.write(response.getOutputStream(), User.class).sheet("用户").doWrite(users);
    }
    
    @PostMapping("/import/products")
    public Result<String> importProducts(@RequestParam("file") MultipartFile file) throws IOException {
        List<Product> products = EasyExcel.read(file.getInputStream(), Product.class, null).sheet().doReadSync();
        productService.saveBatch(products);
        return Result.success("imported " + products.size() + " products");
    }
}
