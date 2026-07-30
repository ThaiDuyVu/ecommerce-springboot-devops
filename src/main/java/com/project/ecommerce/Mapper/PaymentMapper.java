package com.project.ecommerce.Mapper;

import com.project.ecommerce.Model.Order;
import com.project.ecommerce.Model.PaymentOrder;
import com.project.ecommerce.Response.PaymentOrderResponse;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class PaymentMapper {

    public PaymentOrderResponse toResponse(
            PaymentOrder paymentOrder
    ) {

        return PaymentOrderResponse.builder()

                .id(
                        paymentOrder.getId()
                )

                .amount(
                        paymentOrder.getAmount()
                )

                .status(
                        paymentOrder.getStatus()
                )

                .paymentMethod(
                        paymentOrder.getPaymentMethod()
                )

                .paymentLinkId(
                        paymentOrder.getPaymentLinkId()
                )

                .userId(
                        paymentOrder.getUser().getId()
                )

                .orderIds(
                        paymentOrder.getOrders()
                                .stream()
                                .map(Order::getId)
                                .collect(Collectors.toSet())
                )

                .createdAt(
                        paymentOrder.getCreatedAt()
                )

                .build();

    }

}