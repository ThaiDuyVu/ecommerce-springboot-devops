package com.project.ecommerce.Response;

import com.project.ecommerce.Enums.PaymentMethod;
import com.project.ecommerce.Enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TransactionResponse {

    private Long id;

    private Long customerId;

    private String customerName;

    private Long sellerId;

    private String sellerName;

    private Long orderId;

    private Long paymentOrderId;

    private Long amount;

    private PaymentMethod paymentMethod;

    private PaymentStatus paymentStatus;

    private LocalDateTime transactionDate;

}