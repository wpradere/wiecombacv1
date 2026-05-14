package com.wiimy.order.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Human-readable unique code, e.g. WIM-2026-00042 */
    @Column(name = "order_number", nullable = false, unique = true, length = 30)
    private String orderNumber;

    /** Guest session that placed the order */
    @Column(name = "session_id", nullable = false, length = 36)
    private String sessionId;

    // ── Customer snapshot ─────────────────────────────────────────────────────
    @Column(name = "customer_email", nullable = false)
    private String customerEmail;

    @Column(name = "customer_name", nullable = false, length = 200)
    private String customerName;

    @Column(name = "customer_phone", length = 30)
    private String customerPhone;

    // ── Shipping address (embedded columns) ───────────────────────────────────
    @Embedded
    private ShippingAddress shippingAddress;

    // ── Status ────────────────────────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING_PAYMENT;

    // ── Line items ────────────────────────────────────────────────────────────
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    // ── Pricing ───────────────────────────────────────────────────────────────
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "shipping_cost", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal shippingCost = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Column(columnDefinition = "TEXT")
    private String notes;

    // ── Timestamps ────────────────────────────────────────────────────────────
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
