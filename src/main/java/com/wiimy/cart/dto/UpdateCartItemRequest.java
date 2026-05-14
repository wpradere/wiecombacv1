package com.wiimy.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateCartItemRequest(

        /** Set to 0 to remove the item from the cart entirely. */
        @NotNull(message = "Quantity is required")
        @Min(value = 0, message = "Quantity cannot be negative")
        Integer quantity
) {}
