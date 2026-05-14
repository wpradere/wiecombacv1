package com.wiimy.product.repository;

import com.wiimy.product.entity.ProductAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductAttributeRepository extends JpaRepository<ProductAttribute, UUID> {

    List<ProductAttribute> findByProductIdOrderBySortOrderAsc(UUID productId);

    Optional<ProductAttribute> findByIdAndProductId(UUID id, UUID productId);
}
