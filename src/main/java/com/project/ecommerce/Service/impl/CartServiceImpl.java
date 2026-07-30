package com.project.ecommerce.Service.impl;

import com.project.ecommerce.Exceptions.InvalidOperationException;
import com.project.ecommerce.Exceptions.ResourceNotFoundException;
import com.project.ecommerce.Model.Cart;
import com.project.ecommerce.Model.CartItem;
import com.project.ecommerce.Model.Product;
import com.project.ecommerce.Model.User;
import com.project.ecommerce.Repository.CartItemRepository;
import com.project.ecommerce.Repository.CartRepository;
import com.project.ecommerce.Response.CartItemResponse;
import com.project.ecommerce.Response.CartResponse;
import com.project.ecommerce.Service.CartService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    @Override
    @Transactional
    public CartResponse addCartItem(
            User user,
            Product product,
            String size,
            int quantity
    ) {


        if(quantity <= 0){

            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );

        }



        if(product.getQuantity() < quantity){

            throw new IllegalArgumentException(
                    "Product stock is not enough"
            );

        }



        Cart cart =
                cartRepository
                        .findByUser_Id(
                                user.getId()
                        )
                        .orElseGet(() -> {


                            Cart newCart =
                                    new Cart();


                            newCart.setUser(
                                    user
                            );


                            return cartRepository.save(
                                    newCart
                            );

                        });

        Optional<CartItem> existingItem =
                cartItemRepository
                        .findByCartAndProductAndSize(
                                cart,
                                product,
                                size
                        );

        if(existingItem.isPresent()){
            CartItem cartItem =
                    existingItem.get();


            int newQuantity =
                    cartItem.getQuantity()
                            +
                            quantity;

            if(newQuantity > product.getQuantity()){

                throw new IllegalArgumentException(
                        "Product stock is not enough"
                );

            }
            cartItem.setQuantity(
                    newQuantity
            );

            updatePriceSnapshot(
                    cartItem
            );
            cartItemRepository.save(
                    cartItem
            );


        }else{


            CartItem cartItem =
                    new CartItem();


            cartItem.setCart(
                    cart
            );


            cartItem.setProduct(
                    product
            );


            cartItem.setQuantity(
                    quantity
            );


            cartItem.setSize(
                    size
            );


            cartItem.setUserId(
                    user.getId()
            );


            updatePriceSnapshot(
                    cartItem
            );


            cart.getCartItems()
                    .add(cartItem);


            cartItemRepository.save(
                    cartItem
            );

        }

        calculateCartTotals(
                cart
        );

        Cart savedCart =
                cartRepository.save(
                        cart
                );

        return mapToResponse(
                savedCart
        );

    }

    @Override
    @Transactional
    public CartResponse findUserCart(
            User user
    ) {
        Cart cart =
                cartRepository
                        .findByUser_Id(
                                user.getId()
                        )
                        .orElseGet(() -> {
                            Cart newCart =
                                    new Cart();
                            newCart.setUser(
                                    user
                            );
                            return cartRepository.save(
                                    newCart
                            );

                        });

        calculateCartTotals(
                cart
        );
        Cart savedCart =
                cartRepository.save(
                        cart
                );

        return mapToResponse(
                savedCart
        );

    }

    @Override
    @Transactional
    public CartResponse updateCartItem(
            Long cartItemId,
            Integer quantity,
            User user
    ) {


        if(quantity == null || quantity <= 0){

            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );

        }



        CartItem cartItem =
                cartItemRepository
                        .findById(cartItemId)
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Cart item not found with id: "
                                                + cartItemId
                                )
                        );



        if(cartItem.getUserId() == null
                ||
                !cartItem.getUserId()
                        .equals(user.getId())){


            throw new InvalidOperationException(
                    "You cannot update this cart item"
            );

        }



        Product product =
                cartItem.getProduct();



        if(quantity > product.getQuantity()){


            throw new IllegalArgumentException(
                    "Product stock is not enough"
            );

        }



        cartItem.setQuantity(
                quantity
        );



        updatePriceSnapshot(
                cartItem
        );

        cartItemRepository.save(
                cartItem
        );

        Cart cart =
                cartItem.getCart();

        calculateCartTotals(
                cart
        );

        Cart savedCart =
                cartRepository.save(
                        cart
                );
        return mapToResponse(
                savedCart
        );

    }

    @Override
    @Transactional
    public void removeCartItem(
            Long cartItemId,
            User user
    ) {


        CartItem cartItem =
                cartItemRepository
                        .findById(cartItemId)
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Cart item not found with id: "
                                                + cartItemId
                                )
                        );



        if(cartItem.getUserId() == null
                ||
                !cartItem.getUserId()
                        .equals(user.getId())){


            throw new InvalidOperationException(
                    "You cannot delete this cart item"
            );

        }



        Cart cart =
                cartItem.getCart();



        cart.getCartItems()
                .remove(cartItem);



        cartItemRepository.delete(
                cartItem
        );



        calculateCartTotals(
                cart
        );



        cartRepository.save(
                cart
        );

    }

    @Override
    @Transactional
    public void clearCart(
            User user
    ) {
        Cart cart =
                cartRepository
                        .findByUser_Id(user.getId())
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Cart not found"
                                )
                        );

        cart.getCartItems()
                .clear();

        cart.setTotalMrpPrice(0);

        cart.setTotalSellingPrice(0);

        cart.setTotalItem(0);

        cart.setDiscount(0);

        cart.setCouponCode(null);

        cartRepository.save(
                cart
        );

    }

    @Override
    public Cart findUserCartEntity(
            User user
    ) {

        return cartRepository
                .findByUser_Id(user.getId())
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Cart not found"
                        )
                );

    }


    private void calculateCartTotals(
            Cart cart
    ) {


        int totalMrp = 0;

        int totalSelling = 0;

        int totalItem = 0;


        for(CartItem item : cart.getCartItems()){


            totalMrp +=
                    item.getMrpPrice()
                            *
                            item.getQuantity();


            totalSelling +=
                    item.getSellingPrice()
                            *
                            item.getQuantity();


            totalItem +=
                    item.getQuantity();

        }


        cart.setTotalMrpPrice(totalMrp);

        cart.setTotalSellingPrice(totalSelling);

        cart.setTotalItem(totalItem);


        cart.setDiscount(
                calculateDiscountPercentage(
                        totalMrp,
                        totalSelling
                )
        );

    }
    private void updatePriceSnapshot(
            CartItem cartItem
    ) {


        Product product =
                cartItem.getProduct();


        cartItem.setSellingPrice(
                product.getSellingPrice()
        );


        cartItem.setMrpPrice(
                product.getMrpPrice()
        );

    }
    private int calculateDiscountPercentage(
            int mrpPrice,
            int sellingPrice
    ) {


        if(mrpPrice <= 0){

            return 0;

        }


        double discount =
                mrpPrice - sellingPrice;


        double percentage =
                (discount / mrpPrice) * 100;


        return (int) percentage;

    }

    private CartResponse mapToResponse(
            Cart cart
    ){

        List<CartItemResponse> items =
                cart.getCartItems()
                        .stream()
                        .map(item ->

                                CartItemResponse.builder()

                                        .id(
                                                item.getId()
                                        )

                                        .productId(
                                                item.getProduct()
                                                        .getId()
                                        )

                                        .productTitle(
                                                item.getProduct()
                                                        .getTitle()
                                        )

                                        .productImage(
                                                item.getProduct()
                                                        .getImages()
                                                        .isEmpty()
                                                        ?
                                                        null
                                                        :
                                                        item.getProduct()
                                                                .getImages()
                                                                .get(0)
                                        )
                                        .size(
                                                item.getSize()
                                        )

                                        .quantity(
                                                item.getQuantity()
                                        )

                                        .mrpPrice(
                                                item.getMrpPrice()
                                        )

                                        .sellingPrice(
                                                item.getSellingPrice()
                                        )

                                        .build()

                        )
                        .toList();


        return CartResponse.builder()

                .id(
                        cart.getId()
                )

                .cartItems(items)

                .totalMrpPrice(
                        cart.getTotalMrpPrice()
                )

                .totalSellingPrice(
                        cart.getTotalSellingPrice()
                )

                .totalItem(
                        cart.getTotalItem()
                )

                .discount(
                        cart.getDiscount()
                )

                .couponCode(
                        cart.getCouponCode()
                )

                .build();

    }
}
