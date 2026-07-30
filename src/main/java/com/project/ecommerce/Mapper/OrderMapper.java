package com.project.ecommerce.Mapper;

import com.project.ecommerce.Model.Order;
import com.project.ecommerce.Model.OrderItem;
import com.project.ecommerce.Response.OrderItemResponse;
import com.project.ecommerce.Response.OrderResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderMapper {

    public OrderResponse toResponse(
            Order order
    ) {

        List<OrderItemResponse> items =
                order.getOrderItems()
                        .stream()
                        .map(this::toOrderItemResponse)
                        .toList();

        return OrderResponse.builder()

                .id(order.getId())

                .sellerId(
                        order.getSellerId()
                )

                .userId(
                        order.getUser().getId()
                )

                .userName(
                        order.getUser().getFullName()
                )

                .shippingAddress(
                        order.getShippingAddress()
                )

                .orderItems(items)

                .totalMrpPrice(
                        order.getTotalMrpPrice()
                )

                .totalSellingPrice(
                        order.getTotalSellingPrice()
                )

                .discount(
                        order.getDiscount()
                )

                .totalItem(
                        order.getTotalItem()
                )

                .orderStatus(
                        order.getOrderStatus()
                )

                .paymentStatus(
                        order.getPaymentDetails().getPaymentStatus()
                )

                .orderDate(
                        order.getOrderDate()
                )

                .deliverDate(
                        order.getDeliverDate()
                )

                .build();

    }

    private OrderItemResponse toOrderItemResponse(
            OrderItem item
    ) {

        return OrderItemResponse.builder()

                .id(item.getId())

                .productId(
                        item.getProduct().getId()
                )

                .productTitle(
                        item.getProduct().getTitle()
                )

                .productImage(
                        item.getProduct().getImages().isEmpty()
                                ? null
                                : item.getProduct().getImages().get(0)
                )

                .size(
                        item.getSize()
                )

                .quantity(
                        item.getQuantity()
                )

                .mrpPrice(
                        item.getMrpPrice()
                )

                .sellingPrice(
                        item.getSellingPrice()
                )

                .build();

    }

}