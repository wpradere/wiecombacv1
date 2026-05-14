package com.wiimy.order.service;

import com.wiimy.cart.entity.Cart;
import com.wiimy.cart.repository.CartRepository;
import com.wiimy.order.dto.CreateOrderRequest;
import com.wiimy.order.dto.OrderResponse;
import com.wiimy.order.entity.*;
import com.wiimy.order.repository.OrderRepository;
import com.wiimy.product.service.ProductService;
import com.wiimy.shared.email.EmailService;
import com.wiimy.shared.exception.BadRequestException;
import com.wiimy.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Year;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductService productService;
    private final EmailService emailService;

    // ── Commands ──────────────────────────────────────────────────────────────

    /**
     * Creates an order from the active cart identified by {@code request.sessionId()}.
     * <p>
     * Steps:
     * <ol>
     *   <li>Validate cart is not empty.</li>
     *   <li>Deduct stock for every item (throws if any product has insufficient stock).</li>
     *   <li>Snapshot prices and product names into {@link OrderItem}s.</li>
     *   <li>Persist the order and clear the cart.</li>
     * </ol>
     */
    @Transactional
    public OrderResponse createFromCart(CreateOrderRequest request) {
        Cart cart = cartRepository.findBySessionId(request.sessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "sessionId", request.sessionId()));

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cannot create order from an empty cart");
        }

        // Build shipping address
        ShippingAddress shippingAddress = ShippingAddress.builder()
                .street(request.shippingAddress().street())
                .city(request.shippingAddress().city())
                .state(request.shippingAddress().state())
                .zipCode(request.shippingAddress().zipCode())
                .country(request.shippingAddress().country())
                .build();

        // Compute totals
        BigDecimal subtotal = cart.getItems().stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Build order (items added below to set back-references)
        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .sessionId(request.sessionId())
                .customerName(request.customerName())
                .customerEmail(request.customerEmail())
                .customerPhone(request.customerPhone())
                .shippingAddress(shippingAddress)
                .status(OrderStatus.PENDING_PAYMENT)
                .subtotal(subtotal)
                .shippingCost(BigDecimal.ZERO)
                .total(subtotal)          // total = subtotal + shippingCost
                .notes(request.notes())
                .build();

        // Add items with bidirectional reference
        cart.getItems().forEach(cartItem -> {
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(cartItem.getProduct())
                    .productName(cartItem.getProduct().getName())
                    .quantity(cartItem.getQuantity())
                    .unitPrice(cartItem.getUnitPrice())
                    .build();
            order.getItems().add(orderItem);
        });

        Order saved = orderRepository.save(order);

        // Clear cart after successful order placement
        cart.getItems().clear();
        cartRepository.save(cart);

        log.info("Order created: {} for session={} total={}",
                saved.getOrderNumber(), request.sessionId(), saved.getTotal());

        emailService.sendOrderConfirmation(saved);

        return OrderResponse.from(saved);
    }

    /**
     * Transitions the order to a new status, enforcing valid lifecycle transitions.
     * If the new status is {@link OrderStatus#CANCELLED}, restores stock for all items.
     */
    @Transactional
    public OrderResponse updateStatus(UUID id, OrderStatus newStatus, BigDecimal shippingCost) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));

        validateStatusTransition(order.getStatus(), newStatus);

        OrderStatus previous = order.getStatus();
        order.setStatus(newStatus);

        if (newStatus == OrderStatus.SHIPPED) {
            if (shippingCost != null && shippingCost.compareTo(BigDecimal.ZERO) > 0) {
                order.setShippingCost(shippingCost);
                order.setTotal(order.getSubtotal().add(shippingCost));
            }
            order.getItems().forEach(item ->
                    productService.updateStock(item.getProduct().getId(), -item.getQuantity())
            );
            log.info("Order {} dispatched — shippingCost={} stock deducted",
                    order.getOrderNumber(), order.getShippingCost());
        }

        log.info("Order {} status: {} → {}", order.getOrderNumber(), previous, newStatus);
        Order saved = orderRepository.save(order);

        if (newStatus == OrderStatus.SHIPPED) {
            emailService.sendDispatchConfirmation(saved);
        }

        return OrderResponse.from(saved);
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    public OrderResponse findByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .map(OrderResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderNumber", orderNumber));
    }

    public OrderResponse findById(UUID id) {
        return orderRepository.findById(id)
                .map(OrderResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
    }

    public List<OrderResponse> findBySession(String sessionId) {
        return orderRepository.findBySessionIdOrderByCreatedAtDesc(sessionId)
                .stream().map(OrderResponse::from).toList();
    }

    public List<OrderResponse> findByEmail(String email) {
        return orderRepository.findByCustomerEmailIgnoreCaseOrderByCreatedAtDesc(email)
                .stream().map(OrderResponse::from).toList();
    }

    public Page<OrderResponse> findAll(int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return orderRepository.findAllByOrderByCreatedAtDesc(pageable).map(OrderResponse::from);
    }

    public Page<OrderResponse> findByStatus(OrderStatus status, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return orderRepository.findByStatus(status, pageable).map(OrderResponse::from);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        boolean valid = switch (current) {
            case PENDING_PAYMENT -> next == OrderStatus.SHIPPED || next == OrderStatus.CANCELLED;
            case PENDING    -> next == OrderStatus.CONFIRMED  || next == OrderStatus.CANCELLED;
            case CONFIRMED  -> next == OrderStatus.PROCESSING || next == OrderStatus.CANCELLED;
            case PROCESSING -> next == OrderStatus.SHIPPED    || next == OrderStatus.CANCELLED;
            case SHIPPED    -> next == OrderStatus.DELIVERED;
            case DELIVERED  -> next == OrderStatus.REFUNDED;
            case CANCELLED, REFUNDED -> false;
        };
        if (!valid) {
            throw new BadRequestException(
                    "Invalid status transition: " + current + " → " + next);
        }
    }

    private String generateOrderNumber() {
        // Format: WIM-2026-<8-char UUID segment>
        String uid = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return String.format("WIM-%d-%s", Year.now().getValue(), uid);
    }
}
