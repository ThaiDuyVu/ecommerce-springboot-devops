package com.project.ecommerce.Controller;

import com.project.ecommerce.Exceptions.ResourceNotFoundException;
import com.project.ecommerce.Model.*;
import com.project.ecommerce.Repository.AddressRepository;
import com.project.ecommerce.Request.CreateOrderRequest;
import com.project.ecommerce.Request.UpdateOrderStatusRequest;
import com.project.ecommerce.Response.OrderResponse;
import com.project.ecommerce.Service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;


@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    private final UserService userService;

    private final CartService cartService;

    private final AddressRepository addressRepository;

    @PostMapping
    public ResponseEntity<Set<OrderResponse>> createOrder(
            @RequestHeader("Authorization") String jwt,
            @RequestBody CreateOrderRequest request
    ) throws Exception {

        User user =
                userService.findUserByJwtToken(jwt);

        Cart cart =
                cartService.findUserCartEntity(user);

        Address address =
                addressRepository.findById(request.getAddressId())
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Address not found"
                                )
                        );

        Set<OrderResponse> orders =
                orderService.createOrder(
                        user,
                        address,
                        cart
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(orders);

    }
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getUserOrders(
            @RequestHeader("Authorization") String jwt
    ) throws Exception {

        User user = userService.findUserByJwtToken(jwt);

        return ResponseEntity.ok(
                orderService.usersOrderHistory(user.getId())
        );
    }
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable Long orderId
    ) throws Exception {

        return ResponseEntity.ok(
                orderService.findOrderById(orderId)
        );

    }

    @GetMapping("/me")
    public ResponseEntity<List<OrderResponse>> userOrders(
            @RequestHeader("Authorization") String jwt
    ) throws Exception {

        User user =
                userService.findUserByJwtToken(jwt);

        return ResponseEntity.ok(
                orderService.usersOrderHistory(
                        user.getId()
                )
        );

    }

    @GetMapping("/seller")
    public ResponseEntity<List<OrderResponse>> sellerOrders(
            @RequestHeader("Authorization") String jwt
    ) throws Exception {

        User seller =
                userService.findUserByJwtToken(jwt);

        return ResponseEntity.ok(
                orderService.sellersOrder(
                        seller.getId()
                )
        );

    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable Long orderId,
            @RequestBody UpdateOrderStatusRequest request
    ) throws Exception {

        return ResponseEntity.ok(
                orderService.updateOrderStatus(
                        orderId,
                        request.getOrderStatus()
                )
        );

    }

    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable Long orderId,
            @RequestHeader("Authorization") String jwt
    ) throws Exception {

        User user =
                userService.findUserByJwtToken(jwt);

        return ResponseEntity.ok(
                orderService.cancelOrder(
                        orderId,
                        user
                )
        );

    }

}