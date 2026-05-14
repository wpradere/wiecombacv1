package com.wiimy.cart.controller;

import com.wiimy.cart.dto.AddToCartRequest;
import com.wiimy.cart.dto.CartResponse;
import com.wiimy.cart.dto.UpdateCartItemRequest;
import com.wiimy.cart.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Arrays;
import java.util.UUID;

@Tag(name = "Cart", description = "Shopping cart — guest sessions identified via HTTP cookie")
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private static final String SESSION_COOKIE = "wiimy_session";
    private static final Duration COOKIE_TTL = Duration.ofDays(30);

    private final CartService cartService;

    // Set to true in production (requires HTTPS)
    @Value("${app.cookie.secure:false}")
    private boolean secureCookie;

    @Operation(summary = "Get current cart (creates one if none exists)")
    @GetMapping
    public ResponseEntity<CartResponse> getCart(HttpServletRequest req, HttpServletResponse res) {
        String sessionId = resolveSessionId(req, res);
        return ResponseEntity.ok(cartService.getCart(sessionId));
    }

    @Operation(summary = "Add a product to the cart")
    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(
            @Valid @RequestBody AddToCartRequest body,
            HttpServletRequest req,
            HttpServletResponse res
    ) {
        String sessionId = resolveSessionId(req, res);
        return ResponseEntity.ok(cartService.addItem(sessionId, body));
    }

    @Operation(summary = "Update cart item quantity (send 0 to remove the item)")
    @PatchMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> updateItem(
            @Parameter(description = "Cart item ID") @PathVariable UUID itemId,
            @Valid @RequestBody UpdateCartItemRequest body,
            HttpServletRequest req,
            HttpServletResponse res
    ) {
        String sessionId = resolveSessionId(req, res);
        return ResponseEntity.ok(cartService.updateItem(sessionId, itemId, body));
    }

    @Operation(summary = "Remove a specific item from the cart")
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> removeItem(
            @PathVariable UUID itemId,
            HttpServletRequest req,
            HttpServletResponse res
    ) {
        String sessionId = resolveSessionId(req, res);
        return ResponseEntity.ok(cartService.removeItem(sessionId, itemId));
    }

    @Operation(summary = "Remove all items from the cart")
    @DeleteMapping
    public ResponseEntity<Void> clearCart(HttpServletRequest req, HttpServletResponse res) {
        String sessionId = resolveSessionId(req, res);
        cartService.clearCart(sessionId);
        return ResponseEntity.noContent().build();
    }

    // ── Cookie helpers ────────────────────────────────────────────────────────

    private String resolveSessionId(HttpServletRequest req, HttpServletResponse res) {
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            return Arrays.stream(cookies)
                    .filter(c -> SESSION_COOKIE.equals(c.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElseGet(() -> createAndSetSession(res));
        }
        return createAndSetSession(res);
    }

    private String createAndSetSession(HttpServletResponse res) {
        String sessionId = cartService.generateSessionId();

        // ResponseCookie supports SameSite (javax.servlet.http.Cookie does not)
        ResponseCookie cookie = ResponseCookie.from(SESSION_COOKIE, sessionId)
                .maxAge(COOKIE_TTL)
                .path("/")
                .httpOnly(true)
                .secure(secureCookie)   // true in production (HTTPS)
                .sameSite("Lax")        // blocks cross-site POST — primary CSRF mitigation
                .build();

        res.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return sessionId;
    }
}
