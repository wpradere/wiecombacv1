package com.wiimy.product.dto;

import com.wiimy.product.entity.Section;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductFilterRequest(
        UUID categoryId,
        Section section,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Boolean featured,
        int page,
        int size,
        String sortBy,
        String sortDir
) {
    public ProductFilterRequest {
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 20;
        if (sortBy == null || sortBy.isBlank()) sortBy = "createdAt";
        if (sortDir == null || sortDir.isBlank()) sortDir = "desc";
    }
}
