package com.project.ecommerce.Response;

import com.project.ecommerce.Enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentDetailsResponse {


    private String paymentId;


    private String razorpayPaymentLinkId;


    private String razorpayPaymentLinkStatus;


    private PaymentStatus status;

}