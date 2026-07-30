package com.project.ecommerce.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishlistProductResponse {

    private Long id;

    private String title;

    private Integer sellingPrice;

    private Integer mrpPrice;

    private String image;

    private Double averageRating;

}