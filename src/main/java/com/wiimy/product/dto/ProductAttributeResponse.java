package com.wiimy.product.dto;

import com.wiimy.product.entity.ProductAttribute;
import java.util.UUID;

public record ProductAttributeResponse(
        UUID id,
        String name,
        String value,
        int sortOrder
) {
    public static ProductAttributeResponse from(ProductAttribute attr) {
        return new ProductAttributeResponse(
                attr.getId(),
                attr.getName(),
                attr.getValue(),
                attr.getSortOrder()
        );
    }
}
