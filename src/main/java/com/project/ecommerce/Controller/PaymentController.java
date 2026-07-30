package com.project.ecommerce.Controller;

import com.project.ecommerce.Model.*;
import com.project.ecommerce.Request.CreatePaymentRequest;
import com.project.ecommerce.Response.PaymentOrderResponse;
import com.project.ecommerce.Service.*;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    private final UserService userService;

    private final OrderService orderService;
    @PostMapping
    public ResponseEntity<PaymentOrderResponse> createPaymentOrder(
            @RequestHeader("Authorization") String jwt,
            @RequestBody CreatePaymentRequest request
    ) throws Exception {

        User user =
                userService.findUserByJwtToken(jwt);

        Set<Order> orders =
                request.getOrderIds()
                        .stream()
                        .map(orderService::findOrderEntityById)
                        .collect(Collectors.toSet());

        PaymentOrderResponse response =
                paymentService.createPaymentOrder(
                        user,
                        orders,
                        request.getPaymentMethod()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);

    }
    @GetMapping("/{paymentOrderId}")
    public ResponseEntity<PaymentOrderResponse> getPaymentOrder(
            @PathVariable Long paymentOrderId
    ) {

        return ResponseEntity.ok(
                paymentService.getPaymentOrderById(
                        paymentOrderId
                )
        );

    }
    @PostMapping("/{paymentOrderId}/razorpay-link")
    public ResponseEntity<String> createRazorpayPaymentLink(
            @RequestHeader("Authorization") String jwt,
            @PathVariable Long paymentOrderId
    ) throws Exception {

        User user =
                userService.findUserByJwtToken(jwt);

        PaymentOrder paymentOrder =
                paymentService.getPaymentOrderEntityById(
                        paymentOrderId
                );

        String paymentUrl =
                paymentService.createRazorpayPaymentLink(
                        user,
                        paymentOrder.getAmount(),
                        paymentOrderId
                );

        return ResponseEntity.ok(paymentUrl);

    }
    @PostMapping("/{paymentOrderId}/stripe-link")
    public ResponseEntity<String> createStripePaymentLink(
            @RequestHeader("Authorization") String jwt,
            @PathVariable Long paymentOrderId
    ) throws Exception {

        User user =
                userService.findUserByJwtToken(jwt);

        PaymentOrder paymentOrder =
                paymentService.getPaymentOrderEntityById(
                        paymentOrderId
                );

        String paymentUrl =
                paymentService.createStripePaymentLink(
                        user,
                        paymentOrder.getAmount(),
                        paymentOrderId
                );

        return ResponseEntity.ok(paymentUrl);

    }
    @PostMapping("/{paymentOrderId}/process")
    public ResponseEntity<PaymentOrderResponse> processPayment(
            @PathVariable Long paymentOrderId,
            @RequestParam String paymentId,
            @RequestParam String paymentLinkId
    ) throws RazorpayException {

        return ResponseEntity.ok(
                paymentService.processPaymentOrder(
                        paymentOrderId,
                        paymentId,
                        paymentLinkId
                )
        );
    }
    @PostMapping("/{paymentOrderId}/vnpay")
    public ResponseEntity<String> createVNPayPayment(
            @RequestHeader("Authorization") String jwt,
            @PathVariable Long paymentOrderId
    ) throws Exception {


        User user =
                userService.findUserByJwtToken(jwt);


        PaymentOrder paymentOrder =
                paymentService.getPaymentOrderEntityById(
                        paymentOrderId
                );


        String paymentUrl =
                paymentService.createVNPayPaymentUrl(
                        user,
                        paymentOrder.getAmount(),
                        paymentOrderId
                );


        return ResponseEntity.ok(paymentUrl);

    }
    @GetMapping("/vnpay-return")
    public ResponseEntity<PaymentOrderResponse> vnpayReturn(
            @RequestParam Map<String,String> params
    ) {

        return ResponseEntity.ok(
                paymentService.processVNPayPayment(
                        params
                )
        );

    }
}