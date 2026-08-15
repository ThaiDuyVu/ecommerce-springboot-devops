package com.project.ecommerce.Controller;

import com.project.ecommerce.Model.Product;
import com.project.ecommerce.Model.User;
import com.project.ecommerce.Request.AddCartItemRequest;
import com.project.ecommerce.Request.UpdateCartItemRequest;
import com.project.ecommerce.Response.ApiResponse;
import com.project.ecommerce.Response.CartResponse;
import com.project.ecommerce.Service.CartService;
import com.project.ecommerce.Service.ProductService;
import com.project.ecommerce.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    private final ProductService productService;

    private final UserService userService;


    @GetMapping
    public ResponseEntity<CartResponse> getCart(
            @RequestHeader("Authorization") String jwt
    ) throws Exception {


        User user =
                userService.findUserByJwtToken(jwt);


        CartResponse cart =
                cartService.findUserCart(user);


        return ResponseEntity.ok(cart);

    }


    @PostMapping("/items")
    public ResponseEntity<CartResponse> addCartItem(
            @RequestHeader("Authorization") String jwt,
            @RequestBody AddCartItemRequest request
    ) throws Exception {


        User user =
                userService.findUserByJwtToken(jwt);


        Product product =
                productService.findProductEntityById(
                        request.getProductId()
                );


        CartResponse cart =
                cartService.addCartItem(
                        user,
                        product,
                        request.getSize(),
                        request.getQuantity()
                );


        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(cart);

    }


    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<CartResponse> updateCartItem(
            @PathVariable Long cartItemId,
            @RequestHeader("Authorization") String jwt,
            @RequestBody UpdateCartItemRequest request
    ) throws Exception {


        User user =
                userService.findUserByJwtToken(jwt);


        CartResponse cart =
                cartService.updateCartItem(
                        cartItemId,
                        request.getQuantity(),
                        user
                );


        return ResponseEntity.ok(cart);

    }


    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse> removeCartItem(
            @PathVariable Long cartItemId,
            @RequestHeader("Authorization") String jwt
    ) throws Exception {


        User user =
                userService.findUserByJwtToken(jwt);


        cartService.removeCartItem(
                cartItemId,
                user
        );


        ApiResponse response =
                new ApiResponse();

        response.setMessage(
                "Cart item removed successfully"
        );


        return ResponseEntity.ok(response);

    }


    @DeleteMapping
    public ResponseEntity<ApiResponse> clearCart(
            @RequestHeader("Authorization") String jwt
    ) throws Exception {


        User user =
                userService.findUserByJwtToken(jwt);


        cartService.clearCart(user);


        ApiResponse response =
                new ApiResponse();

        response.setMessage(
                "Cart cleared successfully"
        );


        return ResponseEntity.ok(response);

    }

}