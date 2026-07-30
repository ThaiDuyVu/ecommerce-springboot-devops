package com.project.ecommerce.Controller;

import com.project.ecommerce.Model.Product;
import com.project.ecommerce.Model.User;
import com.project.ecommerce.Response.WishlistResponse;
import com.project.ecommerce.Service.ProductService;
import com.project.ecommerce.Service.UserService;
import com.project.ecommerce.Service.WishListService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequiredArgsConstructor
@RequestMapping("/wishlist")
public class WishListController {

    private final WishListService wishListService;

    private final UserService userService;

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<WishlistResponse> getWishlist(
            @RequestHeader("Authorization") String jwt
    ) throws Exception {

        User user =
                userService.findUserByJwtToken(jwt);

        return ResponseEntity.ok(
                wishListService.getWishListByUser(user)
        );

    }

    @PostMapping("/products/{productId}")
    public ResponseEntity<WishlistResponse> toggleProduct(
            @PathVariable Long productId,
            @RequestHeader("Authorization") String jwt
    ) throws Exception {

        User user =
                userService.findUserByJwtToken(jwt);

        Product product =
                productService.findProductEntityById(productId);

        WishlistResponse response =
                wishListService.toggleProduct(
                        user,
                        product
                );

        return ResponseEntity.ok(response);

    }

}
