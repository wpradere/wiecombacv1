package com.wiimy.product.repository;

import com.wiimy.product.entity.Category;
import com.wiimy.product.entity.Product;
import com.wiimy.product.entity.Section;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    List<Product> findByActiveTrueOrderByCreatedAtDesc();

    Page<Product> findByActiveTrue(Pageable pageable);

    List<Product> findByCategory_IdAndActiveTrue(UUID categoryId);

    List<Product> findByFeaturedTrueAndActiveTrue();

    Optional<Product> findByIdAndActiveTrue(UUID id);

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.images WHERE p.id = :id AND p.active = true")
    Optional<Product> findByIdWithImages(@Param("id") UUID id);

    boolean existsByIdAndStockGreaterThan(UUID id, int minStock);

    @Query("SELECT DISTINCT p.category FROM Product p WHERE p.active = true AND p.section = :section ORDER BY p.category.name ASC")
    List<Category> findDistinctCategoriesBySection(@Param("section") Section section);

    @Query("""
            SELECT p FROM Product p
            WHERE p.active = true
              AND (:categoryId IS NULL OR p.category.id = :categoryId)
              AND (:section    IS NULL OR p.section     = :section)
              AND (:minPrice   IS NULL OR p.price       >= :minPrice)
              AND (:maxPrice   IS NULL OR p.price       <= :maxPrice)
              AND (:featured   IS NULL OR p.featured    = :featured)
            """)
    Page<Product> findWithFilters(
            @Param("categoryId") UUID categoryId,
            @Param("section")    Section section,
            @Param("minPrice")   BigDecimal minPrice,
            @Param("maxPrice")   BigDecimal maxPrice,
            @Param("featured")   Boolean featured,
            Pageable pageable
    );
}
