package com.project.ecommerce.Response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CartResponse {

    private Long id;

    private List<CartItemResponse> cartItems;


    private Integer totalMrpPrice;

    private Double totalSellingPrice;

    private Integer totalItem;
    private String couponCode;

    private Integer discount;

}