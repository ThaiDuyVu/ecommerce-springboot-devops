package com.project.ecommerce.Request;

import com.project.ecommerce.Enums.PaymentMethod;
import lombok.Data;

import java.util.Set;

@Data
public class CreatePaymentRequest {

    private Set<Long> orderIds;

    private PaymentMethod paymentMethod;

}