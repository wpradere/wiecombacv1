package com.wiimy.product.dto;

import com.wiimy.product.entity.Category;

import java.util.UUID;

public record CategoryResponse(
        UUID   id,
        String name,
        String label,
        Boolean active
) {
    public static CategoryResponse from(Category c) {
        return new CategoryResponse(c.getId(), c.getName(), c.getLabel(), c.getActive());
    }
}
