package com.project.ecommerce.Service;

import com.project.ecommerce.Enums.PaymentMethod;
import com.project.ecommerce.Model.Order;
import com.project.ecommerce.Model.PaymentOrder;
import com.project.ecommerce.Model.User;
import com.project.ecommerce.Response.PaymentOrderResponse;
import com.razorpay.RazorpayException;
import com.stripe.exception.StripeException;

import java.util.Map;
import java.util.Set;

public interface PaymentService {

    PaymentOrderResponse createPaymentOrder(
            User user,
            Set<Order> orders,
            PaymentMethod paymentMethod
    );

    PaymentOrderResponse getPaymentOrderById(
            Long paymentOrderId
    );

    PaymentOrder getPaymentOrderEntityById(
            Long paymentOrderId
    );

    PaymentOrder getPaymentOrderEntityByPaymentLinkId(
            String paymentLinkId
    );

    PaymentOrderResponse processPaymentOrder(
            Long paymentOrderId,
            String paymentId,
            String paymentLinkId
    ) throws RazorpayException;

    String createRazorpayPaymentLink(
            User user,
            Long amount,
            Long paymentOrderId
    ) throws RazorpayException;

    String createStripePaymentLink(
            User user,
            Long amount,
            Long paymentOrderId
    ) throws StripeException;
    String createVNPayPaymentUrl(
            User user,
            Long amount,
            Long paymentOrderId
    );

    PaymentOrderResponse processVNPayCallback(
            Map<String,String> params
    );
    PaymentOrderResponse processVNPayPayment(
            Map<String, String> params
    );
}