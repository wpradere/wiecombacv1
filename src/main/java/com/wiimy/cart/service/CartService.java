package com.wiimy.cart.service;

import com.wiimy.cart.dto.AddToCartRequest;
import com.wiimy.cart.dto.CartResponse;
import com.wiimy.cart.dto.UpdateCartItemRequest;
import com.wiimy.cart.entity.Cart;
import com.wiimy.cart.entity.CartItem;
import com.wiimy.cart.repository.CartItemRepository;
import com.wiimy.cart.repository.CartRepository;
import com.wiimy.product.entity.Product;
import com.wiimy.product.repository.ProductRepository;
import com.wiimy.shared.exception.BadRequestException;
import com.wiimy.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    // ── Queries ───────────────────────────────────────────────────────────────

    public CartResponse getCart(String sessionId) {
        Cart cart = getOrCreateCart(sessionId);
        return CartResponse.from(cart);
    }

    // ── Commands ──────────────────────────────────────────────────────────────

    @Transactional
    public CartResponse addItem(String sessionId, AddToCartRequest request) {
        Cart cart = getOrCreateCart(sessionId);

        Product product = productRepository.findByIdAndActiveTrue(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", request.productId()));

        if (product.getStock() < request.quantity()) {
            throw new BadRequestException(
                    "Insufficient stock. Available: " + product.getStock());
        }

        Optional<CartItem> existing = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), product.getId());

        if (existing.isPresent()) {
            CartItem item = existing.get();
            int newQty = item.getQuantity() + request.quantity();
            if (newQty > product.getStock()) {
                throw new BadRequestException(
                        "Cannot add " + request.quantity() + " more units. "
                                + "Stock: " + product.getStock()
                                + ", already in cart: " + item.getQuantity());
            }
            item.setQuantity(newQty);
            cartItemRepository.save(item);
        } else {
            CartItem newItem = CartItem.builder()
                    .product(product)
                    .quantity(request.quantity())
                    .unitPrice(product.getPrice())
                    .build();
            cart.addItem(newItem);
        }

        Cart saved = cartRepository.save(cart);
        log.info("Added productId={} qty={} to cart session={}", product.getId(), request.quantity(), sessionId);
        return CartResponse.from(saved);
    }

    @Transactional
    public CartResponse updateItem(String sessionId, UUID itemId, UpdateCartItemRequest request) {
        Cart cart = cartRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "sessionId", sessionId));

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", itemId));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new BadRequestException("Cart item does not belong to this session");
        }

        if (request.quantity() == 0) {
            cart.removeItem(item);
            log.info("Removed itemId={} from cart session={}", itemId, sessionId);
        } else {
            if (request.quantity() > item.getProduct().getStock()) {
                throw new BadRequestException(
                        "Insufficient stock. Available: " + item.getProduct().getStock());
            }
            item.setQuantity(request.quantity());
        }

        return CartResponse.from(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse removeItem(String sessionId, UUID itemId) {
        Cart cart = cartRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "sessionId", sessionId));

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", itemId));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new BadRequestException("Cart item does not belong to this session");
        }

        cart.removeItem(item);
        log.info("Removed itemId={} from cart session={}", itemId, sessionId);
        return CartResponse.from(cartRepository.save(cart));
    }

    @Transactional
    public void clearCart(String sessionId) {
        cartRepository.findBySessionId(sessionId).ifPresent(cart -> {
            cart.getItems().clear();
            cartRepository.save(cart);
            log.info("Cleared cart session={}", sessionId);
        });
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    @Transactional
    public Cart getOrCreateCart(String sessionId) {
        return cartRepository.findBySessionId(sessionId)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder().sessionId(sessionId).build();
                    Cart saved = cartRepository.save(newCart);
                    log.info("Created new cart id={} session={}", saved.getId(), sessionId);
                    return saved;
                });
    }

    public String generateSessionId() {
        return UUID.randomUUID().toString();
    }
}
