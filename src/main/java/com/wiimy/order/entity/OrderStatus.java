package com.wiimy.order.entity;

/**
 * Lifecycle of a WIIMY order.
 *
 * Allowed transitions:
 *   PENDING  → CONFIRMED | CANCELLED
 *   CONFIRMED → PROCESSING | CANCELLED
 *   PROCESSING → SHIPPED | CANCELLED
 *   SHIPPED → DELIVERED
 *   DELIVERED → REFUNDED
 *   CANCELLED / REFUNDED → (terminal)
 */
public enum OrderStatus {
    PENDING_PAYMENT,
    PENDING,
    CONFIRMED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    REFUNDED
}
