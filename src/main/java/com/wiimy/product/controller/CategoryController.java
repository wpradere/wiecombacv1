package com.wiimy.product.controller;

import com.wiimy.product.dto.CategoryRequest;
import com.wiimy.product.dto.CategoryResponse;
import com.wiimy.product.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Categories", description = "Product category management")
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "List all categories")
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAll() {
        return ResponseEntity.ok(categoryService.findAll());
    }

    @Operation(summary = "List active categories only")
    @GetMapping("/active")
    public ResponseEntity<List<CategoryResponse>> getActive() {
        return ResponseEntity.ok(categoryService.findActive());
    }

    @Operation(summary = "Get category by ID")
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(categoryService.findById(id));
    }

    @Operation(summary = "Create a new category")
    @PostMapping
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(request));
    }

    @Operation(summary = "Update a category")
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody CategoryRequest request
    ) {
        return ResponseEntity.ok(categoryService.update(id, request));
    }

    @Operation(summary = "Deactivate a category")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        categoryService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
