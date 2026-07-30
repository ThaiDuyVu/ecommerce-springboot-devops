package com.project.ecommerce.Service;

import com.project.ecommerce.Enums.OrderStatus;
import com.project.ecommerce.Model.*;
import com.project.ecommerce.Response.OrderItemResponse;
import com.project.ecommerce.Response.OrderResponse;

import java.util.List;
import java.util.Set;

public interface OrderService {


    Set<OrderResponse> createOrder(
            User user,
            Address address,
            Cart cart
    );


    OrderResponse findOrderById(
            Long id
    ) throws Exception;


    List<OrderResponse> usersOrderHistory(
            Long userId
    );


    List<OrderResponse> sellersOrder(
            Long sellerId
    );


    OrderResponse updateOrderStatus(
            Long orderId,
            OrderStatus orderStatus
    ) throws Exception;


    OrderResponse cancelOrder(
            Long orderId,
            User user
    ) throws Exception;


    OrderItemResponse getOrderItemById(
            Long id
    ) throws Exception;
    Order findOrderEntityById(
            Long orderId
    );
}