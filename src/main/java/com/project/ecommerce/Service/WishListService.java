package com.project.ecommerce.Service;

import com.project.ecommerce.Model.Product;
import com.project.ecommerce.Model.User;
import com.project.ecommerce.Model.Wishlist;
import com.project.ecommerce.Response.WishlistResponse;

public interface WishListService {
    WishlistResponse createWishList(User user);

    WishlistResponse getWishListByUser(User user);

    WishlistResponse toggleProduct(
            User user,
            Product product
    );
}
