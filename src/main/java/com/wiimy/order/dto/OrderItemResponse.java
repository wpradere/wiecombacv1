package com.wiimy.order.dto;

import com.wiimy.order.entity.OrderItem;
import com.wiimy.product.dto.CategoryResponse;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(
        UUID             itemId,
        UUID             productId,
        String           productName,
        String           productImageUrl,
        CategoryResponse productCategory,
        Integer          quantity,
        BigDecimal       unitPrice,
        BigDecimal       subtotal
) {
    public static OrderItemResponse from(OrderItem item) {
        return new OrderItemResponse(
                item.getId(),
                item.getProduct().getId(),
                item.getProductName(),
                item.getProduct().getImageUrl(),
                CategoryResponse.from(item.getProduct().getCategory()),
                item.getQuantity(),
                item.getUnitPrice(),
                item.subtotal()
        );
    }
}
