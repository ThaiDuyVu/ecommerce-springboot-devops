package com.project.ecommerce.Request;

import lombok.Data;

@Data
public class ProductFilterRequest {

    private String keyword;

    private Long categoryId;

    private Integer minPrice;

    private Integer maxPrice;

    private String color;

    private String size;

    private Integer minDiscount;

    private Boolean inStock;

    private String sort;

    private Integer pageNumber = 0;

    private Integer pageSize = 20;

}