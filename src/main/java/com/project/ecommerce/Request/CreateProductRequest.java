package com.project.ecommerce.Request;

import lombok.Data;

import java.util.List;

@Data
public class CreateProductRequest {

    private String title;

    private String description;

    private Integer mrpPrice;

    private Integer sellingPrice;

    private Integer quantity;

    private String color;

    private List<String> sizes;

    private List<String> images;

    private Long categoryId;

}
