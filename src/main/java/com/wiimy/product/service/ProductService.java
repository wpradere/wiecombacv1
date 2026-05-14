package com.wiimy.product.service;

import com.wiimy.product.dto.*;
import com.wiimy.product.entity.Category;
import com.wiimy.product.entity.Product;
import com.wiimy.product.entity.ProductAttribute;
import com.wiimy.product.entity.ProductImage;
import com.wiimy.product.entity.Section;
import com.wiimy.product.repository.CategoryRepository;
import com.wiimy.product.repository.ProductAttributeRepository;
import com.wiimy.product.repository.ProductImageRepository;
import com.wiimy.product.repository.ProductRepository;
import com.wiimy.shared.exception.BadRequestException;
import com.wiimy.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository imageRepository;
    private final ProductAttributeRepository attributeRepository;
    private final CategoryRepository categoryRepository;

    // ── Queries ──────────────────────────────────────────────────────────────

    public List<ProductResponse> findAll() {
        return productRepository.findByActiveTrueOrderByCreatedAtDesc()
                .stream()
                .map(ProductResponse::from)
                .toList();
    }

    public Page<ProductResponse> findFiltered(ProductFilterRequest filter) {
        Sort sort = filter.sortDir().equalsIgnoreCase("asc")
                ? Sort.by(filter.sortBy()).ascending()
                : Sort.by(filter.sortBy()).descending();
        Pageable pageable = PageRequest.of(filter.page(), filter.size(), sort);
        return productRepository
                .findWithFilters(filter.categoryId(), filter.section(), filter.minPrice(), filter.maxPrice(), filter.featured(), pageable)
                .map(ProductResponse::from);
    }

    public ProductResponse findById(UUID id) {
        Product product = productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        List<ProductImageResponse> images = imageRepository
                .findByProductIdOrderBySortOrderAsc(id)
                .stream().map(ProductImageResponse::from).toList();

        List<ProductAttributeResponse> attributes = attributeRepository
                .findByProductIdOrderBySortOrderAsc(id)
                .stream().map(ProductAttributeResponse::from).toList();

        return ProductResponse.fromWithDetails(product, images, attributes);
    }

    public List<ProductResponse> findByCategoryId(UUID categoryId) {
        return productRepository.findByCategory_IdAndActiveTrue(categoryId)
                .stream()
                .map(ProductResponse::from)
                .toList();
    }

    public List<Category> findCategoriesBySection(Section section) {
        return productRepository.findDistinctCategoriesBySection(section);
    }

    public List<ProductResponse> findFeatured() {
        return productRepository.findByFeaturedTrueAndActiveTrue()
                .stream()
                .map(ProductResponse::from)
                .toList();
    }

    // ── Product commands ─────────────────────────────────────────────────────

    @Transactional
    public ProductResponse create(ProductRequest request) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.categoryId()));

        Product product = Product.builder()
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .imageUrl(request.imageUrl())
                .category(category)
                .stock(request.stock())
                .edition(request.edition())
                .featured(request.featured() != null ? request.featured() : false)
                .active(request.active()   != null ? request.active()   : true)
                .section(request.section() != null ? request.section()  : Section.SUBLIMACION)
                .build();

        Product saved = productRepository.save(product);
        log.info("Created product id={} name='{}' category={}", saved.getId(), saved.getName(), saved.getCategory().getName());
        return ProductResponse.from(saved);
    }

    @Transactional
    public ProductResponse update(UUID id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.categoryId()));

        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setImageUrl(request.imageUrl());
        product.setCategory(category);
        product.setStock(request.stock());
        product.setEdition(request.edition());
        if (request.featured() != null) product.setFeatured(request.featured());
        if (request.active()   != null) product.setActive(request.active());
        if (request.section()  != null) product.setSection(request.section());

        log.info("Updated product id={}", id);
        return ProductResponse.from(productRepository.save(product));
    }

    @Transactional
    public void deactivate(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        product.setActive(false);
        productRepository.save(product);
        log.info("Deactivated product id={}", id);
    }

    @Transactional
    public void updateStock(UUID id, int delta) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        int newStock = product.getStock() + delta;
        if (newStock < 0) {
            throw new BadRequestException(
                    "Insufficient stock for product id=" + id + ". Available: " + product.getStock());
        }
        product.setStock(newStock);
        productRepository.save(product);
        log.debug("Stock updated product id={} delta={} newStock={}", id, delta, newStock);
    }

    // ── Image commands ───────────────────────────────────────────────────────

    @Transactional
    public ProductImageResponse addImage(UUID productId, ProductImageRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));

        ProductImage image = ProductImage.builder()
                .product(product)
                .imageUrl(request.imageUrl())
                .altText(request.altText())
                .description(request.description())
                .sortOrder(request.sortOrder())
                .build();

        ProductImage saved = imageRepository.save(image);
        log.info("Added image id={} to product id={}", saved.getId(), productId);
        return ProductImageResponse.from(saved);
    }

    @Transactional
    public ProductImageResponse updateImage(UUID productId, UUID imageId, ProductImageRequest request) {
        ProductImage image = imageRepository.findByIdAndProductId(imageId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductImage", imageId));

        image.setImageUrl(request.imageUrl());
        image.setAltText(request.altText());
        image.setDescription(request.description());
        image.setSortOrder(request.sortOrder());

        return ProductImageResponse.from(imageRepository.save(image));
    }

    @Transactional
    public void deleteImage(UUID productId, UUID imageId) {
        ProductImage image = imageRepository.findByIdAndProductId(imageId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductImage", imageId));
        imageRepository.delete(image);
        log.info("Deleted image id={} from product id={}", imageId, productId);
    }

    // ── Attribute commands ───────────────────────────────────────────────────

    @Transactional
    public ProductAttributeResponse addAttribute(UUID productId, ProductAttributeRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));

        ProductAttribute attribute = ProductAttribute.builder()
                .product(product)
                .name(request.name())
                .value(request.value())
                .sortOrder(request.sortOrder())
                .build();

        ProductAttribute saved = attributeRepository.save(attribute);
        log.info("Added attribute id={} to product id={}", saved.getId(), productId);
        return ProductAttributeResponse.from(saved);
    }

    @Transactional
    public ProductAttributeResponse updateAttribute(UUID productId, UUID attrId, ProductAttributeRequest request) {
        ProductAttribute attribute = attributeRepository.findByIdAndProductId(attrId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductAttribute", attrId));

        attribute.setName(request.name());
        attribute.setValue(request.value());
        attribute.setSortOrder(request.sortOrder());

        return ProductAttributeResponse.from(attributeRepository.save(attribute));
    }

    @Transactional
    public void deleteAttribute(UUID productId, UUID attrId) {
        ProductAttribute attribute = attributeRepository.findByIdAndProductId(attrId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductAttribute", attrId));
        attributeRepository.delete(attribute);
        log.info("Deleted attribute id={} from product id={}", attrId, productId);
    }
}
