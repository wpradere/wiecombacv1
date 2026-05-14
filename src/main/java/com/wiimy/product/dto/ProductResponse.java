package com.wiimy.product.dto;

import com.wiimy.product.entity.Product;
import com.wiimy.product.entity.Section;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ProductResponse(
        UUID              id,
        String            name,
        String            description,
        BigDecimal        price,
        String            imageUrl,
        CategoryResponse  category,
        Integer           stock,
        String            edition,
        Boolean           featured,
        Boolean           active,
        Section           section,
        LocalDateTime     createdAt,
        List<ProductImageResponse>     images,
        List<ProductAttributeResponse> attributes
) {
    public static ProductResponse from(Product p) {
        return new ProductResponse(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getPrice(),
                p.getImageUrl(),
                CategoryResponse.from(p.getCategory()),
                p.getStock(),
                p.getEdition(),
                p.getFeatured(),
                p.getActive(),
                p.getSection(),
                p.getCreatedAt(),
                List.of(),
                List.of()
        );
    }

    public static ProductResponse fromWithDetails(
            Product p,
            List<ProductImageResponse> images,
            List<ProductAttributeResponse> attributes
    ) {
        return new ProductResponse(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getPrice(),
                p.getImageUrl(),
                CategoryResponse.from(p.getCategory()),
                p.getStock(),
                p.getEdition(),
                p.getFeatured(),
                p.getActive(),
                p.getSection(),
                p.getCreatedAt(),
                images,
                attributes
        );
    }
}
