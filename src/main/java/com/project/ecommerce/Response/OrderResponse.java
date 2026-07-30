package com.project.ecommerce.Response;

import com.project.ecommerce.Enums.OrderStatus;
import com.project.ecommerce.Enums.PaymentStatus;
import com.project.ecommerce.Model.Address;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponse {


    private Long id;


    private Long sellerId;


    private List<OrderItemResponse> orderItems;


    private Long userId;


    private String userName;


    private Address shippingAddress;


    private Integer totalMrpPrice;


    private Integer totalSellingPrice;


    private Integer discount;


    private Integer totalItem;


    private OrderStatus orderStatus;


    private PaymentStatus paymentStatus;


    private LocalDateTime orderDate;


    private LocalDateTime deliverDate;


}