package com.project.ecommerce.Mapper;

import com.project.ecommerce.Model.Product;
import com.project.ecommerce.Model.Wishlist;
import com.project.ecommerce.Response.WishlistProductResponse;
import com.project.ecommerce.Response.WishlistResponse;
import org.springframework.stereotype.Component;

@Component
public class WishlistMapper {

    public WishlistResponse toResponse(
            Wishlist wishlist
    ) {

        return WishlistResponse.builder()

                .id(
                        wishlist.getId()
                )

                .userId(
                        wishlist.getUser().getId()
                )

                .userName(
                        wishlist.getUser().getFullName()
                )

                .products(
                        wishlist.getProducts()
                                .stream()
                                .map(this::toWishlistProductResponse)
                                .toList()
                )

                .build();

    }

    private WishlistProductResponse toWishlistProductResponse(
            Product product
    ) {

        return WishlistProductResponse.builder()

                .id(
                        product.getId()
                )

                .title(
                        product.getTitle()
                )

                .sellingPrice(
                        product.getSellingPrice()
                )

                .mrpPrice(
                        product.getMrpPrice()
                )

                .image(
                        product.getImages().isEmpty()
                                ? null
                                : product.getImages().get(0)
                )

                .averageRating(
                        product.getAverageRating()
                )

                .build();

    }

}