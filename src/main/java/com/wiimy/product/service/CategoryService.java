package com.wiimy.product.service;

import com.wiimy.product.dto.CategoryRequest;
import com.wiimy.product.dto.CategoryResponse;
import com.wiimy.product.entity.Category;
import com.wiimy.product.repository.CategoryRepository;
import com.wiimy.shared.exception.BadRequestException;
import com.wiimy.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll().stream().map(CategoryResponse::from).toList();
    }

    public List<CategoryResponse> findActive() {
        return categoryRepository.findByActiveTrue().stream().map(CategoryResponse::from).toList();
    }

    public CategoryResponse findById(UUID id) {
        return CategoryResponse.from(
                categoryRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Category", id)));
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        String name = request.name().toUpperCase();
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new BadRequestException("Category '" + name + "' already exists");
        }
        Category saved = categoryRepository.save(
                Category.builder()
                        .name(name)
                        .label(request.label())
                        .active(request.active() != null ? request.active() : true)
                        .build());
        return CategoryResponse.from(saved);
    }

    @Transactional
    public CategoryResponse update(UUID id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        category.setName(request.name().toUpperCase());
        category.setLabel(request.label());
        if (request.active() != null) category.setActive(request.active());
        return CategoryResponse.from(categoryRepository.save(category));
    }

    @Transactional
    public void deactivate(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        category.setActive(false);
        categoryRepository.save(category);
    }
}
