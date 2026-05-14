package com.wiimy.order.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingAddress {

    @Column(name = "shipping_street", nullable = false, length = 300)
    private String street;

    @Column(name = "shipping_city", nullable = false, length = 100)
    private String city;

    @Column(name = "shipping_state", length = 100)
    private String state;

    @Column(name = "shipping_zip", length = 20)
    private String zipCode;

    /** ISO 3166-1 alpha-2 country code, e.g. "AR", "US". */
    @Column(name = "shipping_country", nullable = false, length = 2)
    private String country;
}
