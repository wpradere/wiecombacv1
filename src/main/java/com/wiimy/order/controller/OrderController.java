package com.wiimy.order.controller;

import com.wiimy.order.dto.CreateOrderRequest;
import com.wiimy.order.dto.OrderResponse;
import com.wiimy.order.entity.OrderStatus;
import com.wiimy.order.service.OrderService;
import java.math.BigDecimal;
import java.util.UUID;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Orders", description = "Order placement and lifecycle management")
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // ── Customer endpoints ────────────────────────────────────────────────────

    @Operation(summary = "Place an order from the current cart")
    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createFromCart(request));
    }

    @Operation(summary = "Look up an order by its human-readable order number")
    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<OrderResponse> getByOrderNumber(
            @Parameter(description = "e.g. WIM-2026-4F3A2B1C") @PathVariable String orderNumber
    ) {
        return ResponseEntity.ok(orderService.findByOrderNumber(orderNumber));
    }

    @Operation(summary = "Look up orders placed from a specific browser session")
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<OrderResponse>> getBySession(@PathVariable String sessionId) {
        return ResponseEntity.ok(orderService.findBySession(sessionId));
    }

    @Operation(summary = "Look up orders by customer email")
    @GetMapping("/customer/{email}")
    public ResponseEntity<List<OrderResponse>> getByEmail(@PathVariable String email) {
        return ResponseEntity.ok(orderService.findByEmail(email));
    }

    // ── Admin endpoints ───────────────────────────────────────────────────────

    @Operation(summary = "List all orders, newest first (admin)")
    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(orderService.findAll(page, size));
    }

    @Operation(summary = "List orders filtered by status (admin)")
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<OrderResponse>> getByStatus(
            @PathVariable OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(orderService.findByStatus(status, page, size));
    }

    @Operation(summary = "Get order by internal ID (admin)")
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.findById(id));
    }

    @Operation(summary = "Advance order to the next status (admin)")
    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable UUID id,
            @Parameter(description = "Target status: SHIPPED, DELIVERED, CANCELLED, etc.")
            @RequestParam OrderStatus status,
            @Parameter(description = "Shipping cost in COP (required when dispatching)")
            @RequestParam(required = false) BigDecimal shippingCost
    ) {
        return ResponseEntity.ok(orderService.updateStatus(id, status, shippingCost));
    }
}
