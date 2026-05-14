package com.wiimy.order.dto;

import com.wiimy.order.entity.Order;
import com.wiimy.order.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        String orderNumber,
        String customerName,
        String customerEmail,
        String customerPhone,
        OrderStatus status,
        List<OrderItemResponse> items,
        BigDecimal subtotal,
        BigDecimal shippingCost,
        BigDecimal total,
        // Shipping address fields (flattened for easy consumption)
        String shippingStreet,
        String shippingCity,
        String shippingState,
        String shippingZipCode,
        String shippingCountry,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static OrderResponse from(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(OrderItemResponse::from)
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getCustomerName(),
                order.getCustomerEmail(),
                order.getCustomerPhone(),
                order.getStatus(),
                items,
                order.getSubtotal(),
                order.getShippingCost(),
                order.getTotal(),
                order.getShippingAddress().getStreet(),
                order.getShippingAddress().getCity(),
                order.getShippingAddress().getState(),
                order.getShippingAddress().getZipCode(),
                order.getShippingAddress().getCountry(),
                order.getNotes(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
