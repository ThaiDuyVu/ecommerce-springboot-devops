package com.project.ecommerce.Service.impl;

import com.project.ecommerce.Exceptions.ResourceNotFoundException;
import com.project.ecommerce.Mapper.TransactionMapper;
import com.project.ecommerce.Model.Order;
import com.project.ecommerce.Model.PaymentOrder;
import com.project.ecommerce.Model.Seller;
import com.project.ecommerce.Model.Transaction;
import com.project.ecommerce.Repository.SellerRepository;
import com.project.ecommerce.Repository.TransactionRepository;
import com.project.ecommerce.Response.TransactionResponse;
import com.project.ecommerce.Service.TransactionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final SellerRepository sellerRepository;
    private final TransactionMapper transactionMapper;

    @Override
    @Transactional
    public TransactionResponse createTransaction(
            PaymentOrder paymentOrder,
            Order order
    ) {

        Transaction transaction =
                Transaction.builder()

                        .customer(
                                paymentOrder.getUser()
                        )

                        .seller(
                                order.getOrderItems()
                                        .get(0)
                                        .getProduct()
                                        .getSeller()
                        )

                        .order(
                                order
                        )

                        .paymentOrder(
                                paymentOrder
                        )

                        .amount(
                                order.getTotalSellingPrice().longValue()
                        )

                        .paymentMethod(
                                paymentOrder.getPaymentMethod()
                        )

                        .paymentStatus(
                                order.getPaymentDetails().getPaymentStatus()
                        )

                        .build();

        Transaction savedTransaction =
                transactionRepository.save(
                        transaction
                );

        return transactionMapper.toResponse(
                savedTransaction
        );

    }

    @Override
    public TransactionResponse findTransactionById(
            Long transactionId
    ) {

        Transaction transaction =
                transactionRepository.findById(transactionId)
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Transaction not found with id: "
                                                + transactionId
                                )
                        );

        return transactionMapper.toResponse(
                transaction
        );

    }

    @Override
    public Transaction findTransactionEntityById(
            Long transactionId
    ) {

        return transactionRepository.findById(transactionId)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Transaction not found with id: "
                                        + transactionId
                        )
                );

    }

    @Override
    public List<TransactionResponse> getCustomerTransactions(
            Long customerId
    ) {

        return transactionRepository
                .findByCustomerId(customerId)
                .stream()
                .map(transactionMapper::toResponse)
                .toList();

    }

    @Override
    public List<TransactionResponse> getSellerTransactions(
            Long sellerId
    ) {

        return transactionRepository
                .findBySellerId(sellerId)
                .stream()
                .map(transactionMapper::toResponse)
                .toList();

    }
}
