package com.project.ecommerce.Response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderItemResponse {


    private Long id;


    private Long productId;


    private String productTitle;


    private String productImage;


    private String size;


    private Integer quantity;


    private Integer mrpPrice;


    private Integer sellingPrice;

}