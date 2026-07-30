package com.project.ecommerce.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private Long id;

    private String title;

    private String slug;

    private String description;

    private Integer mrpPrice;

    private Integer sellingPrice;

    private Integer discountPercent;

    private Integer quantity;

    private String color;

    private List<String> sizes;

    private List<String> images;

    private Double averageRating;

    private Integer numRatings;

    private Boolean active;

    private Long categoryId;

    private String categoryName;

    private Long sellerId;

    private String sellerName;

    private LocalDateTime createdAt;

}
