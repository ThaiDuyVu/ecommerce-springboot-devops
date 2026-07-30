package com.project.ecommerce.Service.impl;

import com.project.ecommerce.Mapper.WishlistMapper;
import com.project.ecommerce.Model.Product;
import com.project.ecommerce.Model.User;
import com.project.ecommerce.Model.Wishlist;
import com.project.ecommerce.Repository.WishListRepository;
import com.project.ecommerce.Response.WishlistResponse;
import com.project.ecommerce.Service.WishListService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WishListServiceImpl implements WishListService {

    private final WishListRepository wishListRepository;
    private final WishlistMapper wishlistMapper;

    @Override
    public WishlistResponse createWishList(
            User user
    ) {

        Wishlist wishlist = new Wishlist();

        wishlist.setUser(user);

        Wishlist saved =
                wishListRepository.save(wishlist);

        return wishlistMapper.toResponse(saved);

    }

    @Override
    public WishlistResponse getWishListByUser(
            User user
    ) {

        Wishlist wishlist =
                wishListRepository.findByUserId(
                        user.getId()
                );

        if (wishlist == null) {

            wishlist = new Wishlist();

            wishlist.setUser(user);

            wishlist =
                    wishListRepository.save(
                            wishlist
                    );

        }

        return wishlistMapper.toResponse(
                wishlist
        );

    }

    @Override
    public WishlistResponse toggleProduct(
            User user,
            Product product
    ) {

        Wishlist wishlist =
                wishListRepository.findByUserId(
                        user.getId()
                );

        if (wishlist == null) {

            wishlist = new Wishlist();

            wishlist.setUser(user);

            wishlist =
                    wishListRepository.save(
                            wishlist
                    );

        }

        if (wishlist.getProducts().contains(product)) {

            wishlist.getProducts().remove(product);

        } else {

            wishlist.getProducts().add(product);

        }

        Wishlist updated =
                wishListRepository.save(
                        wishlist
                );

        return wishlistMapper.toResponse(
                updated
        );

    }
}
