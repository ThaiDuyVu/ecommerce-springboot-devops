package com.project.ecommerce.Service.impl;

import com.project.ecommerce.Enums.OrderStatus;
import com.project.ecommerce.Enums.PaymentStatus;
import com.project.ecommerce.Exceptions.InvalidOperationException;
import com.project.ecommerce.Exceptions.ResourceNotFoundException;
import com.project.ecommerce.Mapper.OrderMapper;
import com.project.ecommerce.Model.*;
import com.project.ecommerce.Repository.CartRepository;
import com.project.ecommerce.Repository.OrderItemRepository;
import com.project.ecommerce.Repository.OrderRepository;
import com.project.ecommerce.Response.OrderItemResponse;
import com.project.ecommerce.Response.OrderResponse;
import com.project.ecommerce.Service.OrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final OrderMapper orderMapper;
    @Override
    @Transactional
    public Set<OrderResponse> createOrder(
            User user,
            Address address,
            Cart cart
    ) {


        if(cart.getCartItems().isEmpty()){

            throw new IllegalStateException(
                    "Cart is empty"
            );

        }

        Set<OrderResponse> responses =
                new HashSet<>();


        Map<Long, List<CartItem>> sellerItems =
                cart.getCartItems()
                        .stream()
                        .collect(
                                Collectors.groupingBy(
                                        item ->
                                                item.getProduct()
                                                        .getSeller()
                                                        .getId()
                                )
                        );



        for(Map.Entry<Long,List<CartItem>> entry
                : sellerItems.entrySet()){


            Long sellerId =
                    entry.getKey();



            List<CartItem> cartItems =
                    entry.getValue();



            Order order =
                    new Order();



            order.setUser(user);


            order.setSellerId(
                    sellerId
            );


            order.setShippingAddress(
                    address
            );


            order.setOrderStatus(
                    OrderStatus.PENDING
            );

            order.getPaymentDetails()
                    .setPaymentStatus(
                            PaymentStatus.PENDING
                    );

            int totalMrp = 0;

            int totalSelling = 0;

            int totalQuantity = 0;



            List<OrderItem> orderItems =
                    new ArrayList<>();

            for(CartItem cartItem : cartItems){

                OrderItem orderItem =
                        new OrderItem();



                orderItem.setOrder(
                        order
                );


                orderItem.setProduct(
                        cartItem.getProduct()
                );


                orderItem.setSize(
                        cartItem.getSize()
                );


                orderItem.setQuantity(
                        cartItem.getQuantity()
                );


                orderItem.setMrpPrice(
                        cartItem.getMrpPrice()
                );


                orderItem.setSellingPrice(
                        cartItem.getSellingPrice()
                );


                orderItem.setUserId(
                        user.getId()
                );



                orderItems.add(
                        orderItem
                );



                totalMrp +=
                        cartItem.getMrpPrice()
                                *
                                cartItem.getQuantity();



                totalSelling +=
                        cartItem.getSellingPrice()
                                *
                                cartItem.getQuantity();



                totalQuantity +=
                        cartItem.getQuantity();


            }

            order.setOrderItems(
                    orderItems
            );

            order.setTotalMrpPrice(
                    totalMrp
            );


            order.setTotalSellingPrice(
                    totalSelling
            );


            order.setTotalItem(
                    totalQuantity
            );
            order.setDiscount(
                    calculateDiscountPercentage(
                            totalMrp,
                            totalSelling
                    )
            );

            Order savedOrder =
                    orderRepository.save(order);



            responses.add(
                    orderMapper.toResponse(savedOrder)
            );


        }

        cart.getCartItems()
                .clear();

        cart.setTotalItem(0);

        cart.setTotalMrpPrice(0);

        cart.setTotalSellingPrice(0);

        cart.setDiscount(0);



        cartRepository.save(cart);



        return responses;

    }
    @Override
    public OrderResponse findOrderById(
            Long orderId
    ) {

        Order order =
                orderRepository.findById(orderId)
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Order not found with id: "
                                                + orderId
                                )
                        );

        return orderMapper.toResponse(order);

    }

    @Override
    public List<OrderResponse> usersOrderHistory(
            Long userId
    ) {

        return orderRepository
                .findByUserId(userId)
                .stream()
                .map(orderMapper::toResponse)
                .toList();

    }

    @Override
    public List<OrderResponse> sellersOrder(
            Long sellerId
    ) {

        return orderRepository
                .findBySellerId(sellerId)
                .stream()
                .map(orderMapper::toResponse)
                .toList();

    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(
            Long orderId,
            OrderStatus orderStatus
    ) {

        Order order =
                orderRepository.findById(orderId)
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Order not found with id: " + orderId
                                )
                        );

        order.setOrderStatus(orderStatus);

        /*
         * Đồng bộ payment status nếu cần.
         * Có thể mở rộng sau.
         */
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

    @Override
    @Transactional
    public OrderResponse cancelOrder(
            Long orderId,
            User user
    ) {

        Order order =
                orderRepository.findById(orderId)
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Order not found with id: " + orderId
                                )
                        );

        if (!order.getUser().getId().equals(user.getId())) {

            throw new InvalidOperationException(
                    "You cannot cancel this order"
            );

        }

        if (order.getOrderStatus() == OrderStatus.DELIVERED) {

            throw new InvalidOperationException(
                    "Delivered order cannot be cancelled"
            );

        }

        if (order.getOrderStatus() == OrderStatus.CANCELLED) {

            throw new InvalidOperationException(
                    "Order already cancelled"
            );

        }

        order.setOrderStatus(
                OrderStatus.CANCELLED
        );

        order.getPaymentDetails()
                .setPaymentStatus(
                        PaymentStatus.FAILED
                );

        Order savedOrder =
                orderRepository.save(order);

        return orderMapper.toResponse(savedOrder);

    }

    @Override
    public OrderItemResponse getOrderItemById(
            Long id
    ) {

        OrderItem item =
                orderItemRepository.findById(id)
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Order item not found with id: " + id
                                )
                        );

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
    @Override
    public Order findOrderEntityById(
            Long orderId
    ) {

        return orderRepository.findById(orderId)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Order not found with id: "
                                        + orderId
                        )
                );

    }

    private Integer calculateDiscountPercentage(
            Integer mrp,
            Integer selling
    ){

        if(mrp == null || mrp <= 0){
            return 0;
        }


        double discount =
                mrp - selling;


        return (int)
                ((discount / mrp) * 100);

    }
}
