package com.wiimy.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateOrderRequest(

        @NotBlank(message = "Session ID is required")
        @Size(max = 36)
        String sessionId,

        @NotBlank(message = "Customer name is required")
        @Size(max = 200)
        String customerName,

        @NotBlank(message = "Customer email is required")
        @Email(message = "Must be a valid email address")
        String customerEmail,

        @Size(max = 30)
        String customerPhone,

        @NotNull(message = "Shipping address is required")
        @Valid
        ShippingAddressDto shippingAddress,

        @Size(max = 1000)
        String notes
) {}
