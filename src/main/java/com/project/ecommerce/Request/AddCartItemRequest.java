package com.project.ecommerce.Request;

import lombok.Data;


@Data
public class AddCartItemRequest {


    private Long productId;


    private String size;


    private Integer quantity;

}