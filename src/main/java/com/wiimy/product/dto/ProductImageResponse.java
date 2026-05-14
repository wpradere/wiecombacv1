package com.wiimy.product.dto;

import com.wiimy.product.entity.ProductImage;
import java.util.UUID;

public record ProductImageResponse(
        UUID id,
        String imageUrl,
        String altText,
        String description,
        int sortOrder
) {
    public static ProductImageResponse from(ProductImage img) {
        return new ProductImageResponse(
                img.getId(),
                img.getImageUrl(),
                img.getAltText(),
                img.getDescription(),
                img.getSortOrder()
        );
    }
}
