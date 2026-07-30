package com.project.ecommerce.Service;

import com.project.ecommerce.Model.Cart;
import com.project.ecommerce.Model.CartItem;
import com.project.ecommerce.Model.Product;
import com.project.ecommerce.Model.User;
import com.project.ecommerce.Response.CartResponse;

public interface CartService {


    CartResponse addCartItem(
            User user,
            Product product,
            String size,
            int quantity
    );


    CartResponse findUserCart(
            User user
    );

    CartResponse updateCartItem(
            Long cartItemId,
            Integer quantity,
            User user
    );

    void removeCartItem(
            Long cartItemId,
            User user
    );


    void clearCart(
            User user
    );
    Cart findUserCartEntity(User user);
}