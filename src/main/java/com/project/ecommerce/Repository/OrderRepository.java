package com.project.ecommerce.Repository;

import com.project.ecommerce.Model.Order;
import com.project.ecommerce.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order,Long> {

    List<Order> findByUserId(Long userId);
    List<Order>  findBySellerId(Long sellerId);
    Optional<Order> findByIdAndSellerId(
            Long orderId,
            Long sellerId
    );
}
