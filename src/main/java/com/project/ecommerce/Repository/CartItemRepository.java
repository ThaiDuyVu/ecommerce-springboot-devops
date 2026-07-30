package com.project.ecommerce.Repository;

import com.project.ecommerce.Model.Cart;
import com.project.ecommerce.Model.CartItem;
import com.project.ecommerce.Model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository
        extends JpaRepository<CartItem, Long> {


    Optional<CartItem> findByCartAndProductAndSize(
            Cart cart,
            Product product,
            String size
    );
    Optional<CartItem> findByIdAndUserId(
            Long cartItemId,
            Long userId
    );
}