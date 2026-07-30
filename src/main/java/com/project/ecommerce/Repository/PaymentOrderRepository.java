package com.project.ecommerce.Repository;

import com.project.ecommerce.Model.PaymentOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentOrderRepository
        extends JpaRepository<PaymentOrder, Long> {

    Optional<PaymentOrder> findByPaymentLinkId(
            String paymentLinkId
    );

}