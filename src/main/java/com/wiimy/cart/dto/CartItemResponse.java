package com.wiimy.cart.dto;

import com.wiimy.cart.entity.CartItem;
import com.wiimy.product.dto.CategoryResponse;

import java.math.BigDecimal;
import java.util.UUID;

public record CartItemResponse(
        UUID             itemId,
        UUID             productId,
        String           productName,
        String           productImageUrl,
        CategoryResponse category,
        Integer          quantity,
        BigDecimal       unitPrice,
        BigDecimal       subtotal,
        Integer          availableStock
) {
    public static CartItemResponse from(CartItem item) {
        return new CartItemResponse(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getProduct().getImageUrl(),
                CategoryResponse.from(item.getProduct().getCategory()),
                item.getQuantity(),
                item.getUnitPrice(),
                item.subtotal(),
                item.getProduct().getStock()
        );
    }
}
