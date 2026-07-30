package com.project.ecommerce.Service;

import com.project.ecommerce.Model.Order;
import com.project.ecommerce.Model.PaymentOrder;
import com.project.ecommerce.Model.Seller;
import com.project.ecommerce.Model.Transaction;
import com.project.ecommerce.Response.TransactionResponse;

import java.util.List;

public interface TransactionService {

    TransactionResponse createTransaction(
            PaymentOrder paymentOrder,
            Order order
    );

    TransactionResponse findTransactionById(
            Long transactionId
    );

    Transaction findTransactionEntityById(
            Long transactionId
    );

    List<TransactionResponse> getCustomerTransactions(
            Long customerId
    );

    List<TransactionResponse> getSellerTransactions(
            Long sellerId
    );

}