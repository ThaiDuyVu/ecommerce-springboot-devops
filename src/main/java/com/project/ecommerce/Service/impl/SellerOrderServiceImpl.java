package com.project.ecommerce.Service.impl;

import com.project.ecommerce.Enums.OrderStatus;
import com.project.ecommerce.Enums.PaymentStatus;
import com.project.ecommerce.Exceptions.InvalidOperationException;
import com.project.ecommerce.Exceptions.ResourceNotFoundException;
import com.project.ecommerce.Mapper.OrderMapper;
import com.project.ecommerce.Model.Order;
import com.project.ecommerce.Model.Seller;
import com.project.ecommerce.Repository.OrderRepository;
import com.project.ecommerce.Response.OrderResponse;
import com.project.ecommerce.Service.SellerOrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class SellerOrderServiceImpl implements SellerOrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;


    @Override
    public List<OrderResponse> getSellerOrders(
            Seller seller
    ) {


        List<Order> orders =
                orderRepository.findBySellerId(
                        seller.getId()
                );


        return orders
                .stream()
                .map(orderMapper::toResponse)
                .toList();

    }

    @Override
    public OrderResponse getSellerOrderById(
            Long orderId,
            Seller seller
    ) {

        Order order =
                orderRepository.findByIdAndSellerId(
                                orderId,
                                seller.getId()
                        )
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Order not found"
                                )
                        );

        return orderMapper.toResponse(order);

    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(
            Long orderId,
            OrderStatus orderStatus,
            Seller seller
    ) {

        Order order =
                orderRepository
                        .findByIdAndSellerId(
                                orderId,
                                seller.getId()
                        )
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Order not found"
                                )
                        );

        if (order.getOrderStatus() == OrderStatus.CANCELLED) {

            throw new InvalidOperationException(
                    "Cancelled order cannot be updated"
            );

        }

        if (order.getOrderStatus() == OrderStatus.DELIVERED) {

            throw new InvalidOperationException(
                    "Delivered order cannot be updated"
            );

        }

        if (!isValidTransition(
                order.getOrderStatus(),
                orderStatus
        )) {

            throw new InvalidOperationException(
                    "Invalid order status transition"
            );

        }

        order.setOrderStatus(orderStatus);

        if (orderStatus == OrderStatus.DELIVERED) {

            order.getPaymentDetails()
                    .setPaymentStatus(
                            PaymentStatus.COMPLETED
                    );

        }

        Order savedOrder =
                orderRepository.save(order);

        return orderMapper.toResponse(savedOrder);

    }
    private boolean isValidTransition(
            OrderStatus current,
            OrderStatus next
    ) {

        return switch (current) {

            case PENDING ->
                    next == OrderStatus.CONFIRMED
                            || next == OrderStatus.CANCELLED;

            case CONFIRMED ->
                    next == OrderStatus.SHIPPED
                            || next == OrderStatus.CANCELLED;

            case SHIPPED ->
                    next == OrderStatus.DELIVERED;

            default -> false;

        };

    }
}
