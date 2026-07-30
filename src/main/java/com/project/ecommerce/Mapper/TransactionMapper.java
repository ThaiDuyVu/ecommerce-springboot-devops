package com.project.ecommerce.Mapper;

import com.project.ecommerce.Model.Transaction;
import com.project.ecommerce.Response.TransactionResponse;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransactionResponse toResponse(
            Transaction transaction
    ) {

        return TransactionResponse.builder()

                .id(transaction.getId())

                .customerId(
                        transaction.getCustomer().getId()
                )

                .customerName(
                        transaction.getCustomer().getFullName()
                )

                .sellerId(
                        transaction.getSeller().getId()
                )

                .sellerName(
                        transaction.getSeller().getSellerName()
                )

                .orderId(
                        transaction.getOrder().getId()
                )

                .paymentOrderId(
                        transaction.getPaymentOrder().getId()
                )

                .amount(
                        transaction.getAmount()
                )

                .paymentMethod(
                        transaction.getPaymentMethod()
                )

                .paymentStatus(
                        transaction.getPaymentStatus()
                )

                .transactionDate(
                        transaction.getTransactionDate()
                )

                .build();

    }

}