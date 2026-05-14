package com.wiimy.product.controller;

import com.wiimy.product.dto.*;
import com.wiimy.product.entity.Section;
import com.wiimy.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Tag(name = "Products", description = "Product catalog management")
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // ── Read ─────────────────────────────────────────────────────────────────

    @Operation(summary = "List all active products (no pagination)")
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAll() {
        return ResponseEntity.ok(productService.findAll());
    }

    @Operation(summary = "Search products with optional filters and pagination")
    @GetMapping("/search")
    public ResponseEntity<Page<ProductResponse>> search(
            @Parameter(description = "Filter by category UUID")
            @RequestParam(required = false) UUID categoryId,
            @Parameter(description = "Filter by section: SUBLIMACION, ZONA_GEEK")
            @RequestParam(required = false) Section section,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean featured,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        var filter = new ProductFilterRequest(categoryId, section, minPrice, maxPrice, featured, page, size, sortBy, sortDir);
        return ResponseEntity.ok(productService.findFiltered(filter));
    }

    @Operation(summary = "Returns active categories per section (derived from products in DB)")
    @GetMapping("/meta")
    public ResponseEntity<Map<String, List<CategoryResponse>>> getMeta() {
        Map<String, List<CategoryResponse>> meta = new LinkedHashMap<>();
        for (Section section : Section.values()) {
            List<CategoryResponse> cats = productService.findCategoriesBySection(section)
                    .stream().map(CategoryResponse::from).toList();
            meta.put(section.name(), cats);
        }
        return ResponseEntity.ok(meta);
    }

    @Operation(summary = "List featured products")
    @GetMapping("/featured")
    public ResponseEntity<List<ProductResponse>> getFeatured() {
        return ResponseEntity.ok(productService.findFeatured());
    }

    @Operation(summary = "Get product by ID (includes images and attributes)")
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    @Operation(summary = "List products by category UUID")
    @GetMapping("/category/{id}")
    public ResponseEntity<List<ProductResponse>> getByCategory(@PathVariable UUID id) {
        return ResponseEntity.ok(productService.findByCategoryId(id));
    }

    // ── Product write ─────────────────────────────────────────────────────────

    @Operation(summary = "Create a new product")
    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(request));
    }

    @Operation(summary = "Update an existing product")
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody ProductRequest request
    ) {
        return ResponseEntity.ok(productService.update(id, request));
    }

    @Operation(summary = "Soft-delete a product (marks it inactive)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        productService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    // ── Images ────────────────────────────────────────────────────────────────

    @Operation(summary = "Add an image to a product",
               description = "imageUrl must be a path relative to /public, e.g. /images/products/remera-back.jpg")
    @PostMapping("/{id}/images")
    public ResponseEntity<ProductImageResponse> addImage(
            @PathVariable UUID id,
            @Valid @RequestBody ProductImageRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.addImage(id, request));
    }

    @Operation(summary = "Update an existing product image")
    @PatchMapping("/{id}/images/{imageId}")
    public ResponseEntity<ProductImageResponse> updateImage(
            @PathVariable UUID id,
            @PathVariable UUID imageId,
            @Valid @RequestBody ProductImageRequest request
    ) {
        return ResponseEntity.ok(productService.updateImage(id, imageId, request));
    }

    @Operation(summary = "Delete a product image")
    @DeleteMapping("/{id}/images/{imageId}")
    public ResponseEntity<Void> deleteImage(
            @PathVariable UUID id,
            @PathVariable UUID imageId
    ) {
        productService.deleteImage(id, imageId);
        return ResponseEntity.noContent().build();
    }

    // ── Attributes ────────────────────────────────────────────────────────────

    @Operation(summary = "Add an attribute to a product (e.g. Talle: M/L/XL)")
    @PostMapping("/{id}/attributes")
    public ResponseEntity<ProductAttributeResponse> addAttribute(
            @PathVariable UUID id,
            @Valid @RequestBody ProductAttributeRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.addAttribute(id, request));
    }

    @Operation(summary = "Update an existing product attribute")
    @PatchMapping("/{id}/attributes/{attrId}")
    public ResponseEntity<ProductAttributeResponse> updateAttribute(
            @PathVariable UUID id,
            @PathVariable UUID attrId,
            @Valid @RequestBody ProductAttributeRequest request
    ) {
        return ResponseEntity.ok(productService.updateAttribute(id, attrId, request));
    }

    @Operation(summary = "Delete a product attribute")
    @DeleteMapping("/{id}/attributes/{attrId}")
    public ResponseEntity<Void> deleteAttribute(
            @PathVariable UUID id,
            @PathVariable UUID attrId
    ) {
        productService.deleteAttribute(id, attrId);
        return ResponseEntity.noContent().build();
    }
}
