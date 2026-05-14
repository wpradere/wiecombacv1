package com.wiimy.product.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Entity
@Table(name = "products")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Section section = Section.SUBLIMACION;

    @Column(nullable = false)
    private Integer stock;

    /** Edition label, e.g. "01/20" */
    @Column(length = 20)
    private String edition;

    @Column(nullable = false)
    @Builder.Default
    private Boolean featured = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL,
               fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    @Builder.Default
    private List<ProductImage> images = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
