package com.project.ecommerce.Service;

import com.project.ecommerce.Response.OrderResponse;
import com.project.ecommerce.Model.Seller;
import com.project.ecommerce.Enums.OrderStatus;

import java.util.List;

public interface SellerOrderService {


    List<OrderResponse> getSellerOrders(
            Seller seller
    );


    OrderResponse getSellerOrderById(
            Long orderId,
            Seller seller
    );


    OrderResponse updateOrderStatus(
            Long orderId,
            OrderStatus orderStatus,
            Seller seller
    );


}