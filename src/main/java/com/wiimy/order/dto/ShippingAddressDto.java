package com.wiimy.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ShippingAddressDto(

        @NotBlank(message = "Street is required")
        @Size(max = 300)
        String street,

        @NotBlank(message = "City is required")
        @Size(max = 100)
        String city,

        @Size(max = 100)
        String state,

        @Size(max = 20)
        String zipCode,

        @NotBlank(message = "Country is required")
        @Size(min = 2, max = 2, message = "Country must be an ISO 3166-1 alpha-2 code (e.g. AR, US)")
        String country
) {}
