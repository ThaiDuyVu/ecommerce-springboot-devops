package com.project.ecommerce.Request;

import lombok.Data;

import java.util.List;

@Data
public class UpdateProductRequest {

    private String title;

    private String description;

    private Integer mrpPrice;

    private Integer sellingPrice;

    private Integer quantity;

    private String color;

    private List<String> sizes;

    private List<String> images;

    /**
     * Cho phép chuyển sang category khác
     */
    private Long categoryId;

    /**
     * Hiển thị / Ẩn sản phẩm
     */
    private Boolean active;

}