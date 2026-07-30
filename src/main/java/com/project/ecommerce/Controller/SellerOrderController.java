package com.project.ecommerce.Controller;

import com.project.ecommerce.Enums.OrderStatus;
import com.project.ecommerce.Model.Seller;
import com.project.ecommerce.Response.OrderResponse;
import com.project.ecommerce.Service.SellerOrderService;
import com.project.ecommerce.Service.SellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/seller/orders")
public class SellerOrderController {
    private final SellerService sellerService;
    private final SellerOrderService sellerOrderService;
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getSellerOrders(
            @RequestHeader("Authorization") String jwt
    ) throws Exception {

        Seller seller =
                sellerService.getSellerProfile(jwt);

        return ResponseEntity.ok(
                sellerOrderService.getSellerOrders(seller)
        );

    }
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getSellerOrder(
            @RequestHeader("Authorization") String jwt,
            @PathVariable Long orderId
    ) throws Exception {

        Seller seller =
                sellerService.getSellerProfile(jwt);

        return ResponseEntity.ok(
                sellerOrderService.getSellerOrderById(
                        orderId,
                        seller
                )
        );

    }
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @RequestHeader("Authorization") String jwt,
            @PathVariable Long orderId,
            @RequestParam OrderStatus orderStatus
    ) throws Exception {

        Seller seller =
                sellerService.getSellerProfile(jwt);

        return ResponseEntity.ok(
                sellerOrderService.updateOrderStatus(
                        orderId,
                        orderStatus,
                        seller
                )
        );

    }
}
