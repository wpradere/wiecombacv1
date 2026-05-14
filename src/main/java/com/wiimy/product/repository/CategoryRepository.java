package com.wiimy.product.repository;

import com.wiimy.product.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    List<Category> findByActiveTrue();

    Optional<Category> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
