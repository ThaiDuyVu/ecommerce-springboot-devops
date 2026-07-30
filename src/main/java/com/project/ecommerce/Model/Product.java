package com.project.ecommerce.Model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_product_slug_seller",
                        columnNames = {
                                "slug",
                                "seller_id"
                        }
                )
        }
)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    /**
     * SEO URL
     */
    @Column(nullable = false, length = 250)
    private String slug;

    /**
     * Mô tả chi tiết
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Giá niêm yết
     */
    @Column(nullable = false)
    private Integer mrpPrice;

    /**
     * Giá bán
     */
    @Column(nullable = false)
    private Integer sellingPrice;

    /**
     * Tạm giữ để không ảnh hưởng business hiện tại.
     * Sau này có thể bỏ và tính động.
     */
    @Column(nullable = false)
    private Integer discountPercent = 0;

    /**
     * Tồn kho
     */
    @Column(nullable = false)
    private Integer quantity = 0;

    /**
     * Màu sắc chính
     */
    @Column(length = 50)
    private String color;

    /**
     * Danh sách ảnh
     */
    @ElementCollection
    @CollectionTable(
            name = "product_images",
            joinColumns = @JoinColumn(name = "product_id")
    )
    @Column(name = "image_url")
    private List<String> images = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
            name = "product_sizes",
            joinColumns = @JoinColumn(name = "product_id")
    )
    @Column(name = "size")
    private List<String> sizes = new ArrayList<>();

    /**
     * Điểm đánh giá trung bình
     */
    @Column(nullable = false)
    private Double averageRating = 0.0;

    /**
     * Tổng số lượt đánh giá
     */
    @Column(nullable = false)
    private Integer numRatings = 0;

    /**
     * Trạng thái hiển thị
     */
    @Column(nullable = false)
    private Boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private Seller seller;

    /**
     * Giữ nguyên để không ảnh hưởng module Review hiện tại.
     */
    @OneToMany(
            mappedBy = "product",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Review> reviews = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
