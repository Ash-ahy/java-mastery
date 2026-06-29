package com.mastery.controller;

import com.mastery.common.PageResult;
import com.mastery.common.Result;
import com.mastery.dto.OrderCreateDTO;
import com.mastery.entity.MallOrder;
import com.mastery.entity.MallOrderItem;
import com.mastery.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public Result<PageResult<MallOrder>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        return Result.success(orderService.queryPage(page, size, status));
    }

    @PostMapping
    public Result<MallOrder> create(Authentication auth, @Valid @RequestBody OrderCreateDTO dto) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(orderService.createOrder(userId, dto));
    }

    @PostMapping("/{id}/pay")
    public Result<Void> pay(@PathVariable Long id) { orderService.payOrder(id); return Result.success(); }

    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) { orderService.cancelOrder(id); return Result.success(); }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> statistics() {
        return Result.success(orderService.getStatistics());
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        MallOrder order = orderService.getById(id);
        if (order == null) return Result.notFound("订单不存在");
        List<MallOrderItem> items = orderService.getOrderItems(id);
        return Result.success(Map.of("order", order, "items", items));
    }
}
