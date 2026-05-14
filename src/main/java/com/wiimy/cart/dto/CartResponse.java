package com.wiimy.cart.dto;

import com.wiimy.cart.entity.Cart;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CartResponse(
        UUID cartId,
        String sessionId,
        List<CartItemResponse> items,
        int totalItems,
        BigDecimal subtotal
) {
    public static CartResponse from(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(CartItemResponse::from)
                .toList();

        BigDecimal subtotal = items.stream()
                .map(CartItemResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalItems = items.stream()
                .mapToInt(CartItemResponse::quantity)
                .sum();

        return new CartResponse(cart.getId(), cart.getSessionId(), items, totalItems, subtotal);
    }
}
