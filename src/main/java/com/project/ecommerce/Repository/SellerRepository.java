package com.project.ecommerce.Repository;

import com.project.ecommerce.Enums.AccountStatus;
import com.project.ecommerce.Model.Seller;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SellerRepository extends JpaRepository<Seller,Long> {
    Seller findByEmail(String email);
    List<Seller> findByAccountStatus(AccountStatus status);

    List<Seller> findByIsEmailVerifiedFalseAndCreatedAtBefore(LocalDateTime time);
}
