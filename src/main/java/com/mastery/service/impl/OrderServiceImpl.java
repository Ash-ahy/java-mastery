package com.mastery.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mastery.common.PageResult;
import com.mastery.dto.OrderCreateDTO;
import com.mastery.entity.MallOrder;
import com.mastery.entity.MallOrderItem;
import com.mastery.entity.Product;
import com.mastery.exception.BusinessException;
import com.mastery.mapper.MallOrderItemMapper;
import com.mastery.mapper.MallOrderMapper;
import com.mastery.service.OrderService;
import com.mastery.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<MallOrderMapper, MallOrder> implements OrderService {

    private final MallOrderMapper orderMapper;
    private final MallOrderItemMapper orderItemMapper;
    private final ProductService productService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MallOrder createOrder(Long userId, OrderCreateDTO dto) {
        String orderNo = generateOrderNo();
        BigDecimal totalAmount = BigDecimal.ZERO;

        // 批量加载商品，避免N+1
        List<Long> productIds = dto.getItems().stream()
                .map(OrderCreateDTO.OrderItemDTO::getProductId).toList();
        var productMap = productService.listByIds(productIds).stream()
                .collect(java.util.stream.Collectors.toMap(Product::getId, p -> p));

        // 校验 + 扣库存 + 计算总价
        for (OrderCreateDTO.OrderItemDTO item : dto.getItems()) {
            var product = productMap.get(item.getProductId());
            if (product == null || product.getStatus() != 1) {
                throw new BusinessException(400, "product not found: " + item.getProductId());
            }
            if (!productService.deductStock(item.getProductId(), item.getQuantity())) {
                throw new BusinessException(400, "insufficient stock: " + product.getName());
            }
            totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        MallOrder order = new MallOrder();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setPayAmount(totalAmount);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setStatus("PENDING");
        order.setReceiverName(dto.getReceiverName());
        order.setReceiverPhone(dto.getReceiverPhone());
        order.setReceiverAddress(dto.getReceiverAddress());
        order.setRemark(dto.getRemark());
        orderMapper.insert(order);

        // 保存订单明细（复用已加载的商品）
        for (OrderCreateDTO.OrderItemDTO item : dto.getItems()) {
            var product = productMap.get(item.getProductId());
            MallOrderItem oi = new MallOrderItem();
            oi.setOrderId(order.getId());
            oi.setProductId(item.getProductId());
            oi.setProductName(product.getName());
            oi.setProductImage(product.getImages());
            oi.setPrice(product.getPrice());
            oi.setQuantity(item.getQuantity());
            oi.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            orderItemMapper.insert(oi);
        }

        log.info("Order created: orderNo={}, amount={}", orderNo, totalAmount);
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payOrder(Long orderId) {
        MallOrder order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException(404, "order not found");
        if (!"PENDING".equals(order.getStatus())) throw new BusinessException(400, "invalid status");
        order.setStatus("PAID");
        order.setPayTime(LocalDateTime.now());
        orderMapper.updateById(order);
        log.info("Order paid: orderNo={}", order.getOrderNo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId) {
        MallOrder order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException(404, "order not found");
        if (!"PENDING".equals(order.getStatus())) throw new BusinessException(400, "only pending orders can be cancelled");
        order.setStatus("CANCELLED");
        orderMapper.updateById(order);
        log.info("Order cancelled: orderNo={}", order.getOrderNo());
    }

    @Override
    public Map<String, Object> getStatistics() {
        return Map.of(
            "statusCount", orderMapper.countByStatus(),
            "todayTurnover", orderMapper.todayTurnover()
        );
    }

    @Override
    public List<MallOrderItem> getOrderItems(Long orderId) {
        return orderItemMapper.selectByOrderId(orderId);
    }

    @Override
    public PageResult<MallOrder> queryPage(int page, int size, String status) {
        Page<MallOrder> p = new Page<>(page, size);
        LambdaQueryWrapper<MallOrder> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status)) w.eq(MallOrder::getStatus, status);
        w.orderByDesc(MallOrder::getCreateTime);
        Page<MallOrder> result = orderMapper.selectPage(p, w);
        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords());
    }

    private String generateOrderNo() {
        return "OM" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}
