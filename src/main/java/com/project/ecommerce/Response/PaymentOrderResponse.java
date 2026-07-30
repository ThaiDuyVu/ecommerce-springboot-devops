package com.project.ecommerce.Response;

import com.project.ecommerce.Enums.PaymentMethod;
import com.project.ecommerce.Enums.PaymentOrderStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
public class PaymentOrderResponse {

    private Long id;

    private Long amount;

    private PaymentOrderStatus status;

    private PaymentMethod paymentMethod;

    private String paymentLinkId;

    private Long userId;

    private Set<Long> orderIds;

    private LocalDateTime createdAt;

}