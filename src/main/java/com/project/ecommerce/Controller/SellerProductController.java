package com.project.ecommerce.Controller;


import com.project.ecommerce.Model.Seller;
import com.project.ecommerce.Request.CreateProductRequest;
import com.project.ecommerce.Request.UpdateProductRequest;
import com.project.ecommerce.Response.ProductResponse;
import com.project.ecommerce.Service.ProductService;
import com.project.ecommerce.Service.SellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/seller/products")
public class SellerProductController {

    private final ProductService productService;
    private final SellerService sellerService;

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @RequestHeader("Authorization") String jwt,
            @RequestBody CreateProductRequest request
    ) throws Exception {

        Seller seller =
                sellerService.getSellerProfile(jwt);

        ProductResponse product =
                productService.createProduct(
                        request,
                        seller
                );

        return new ResponseEntity<>(
                product,
                HttpStatus.CREATED
        );

    }

    @PutMapping("/{productId}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long productId,
            @RequestBody UpdateProductRequest request
    ) {

        ProductResponse product =
                productService.updateProduct(
                        productId,
                        request
                );

        return ResponseEntity.ok(product);

    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long productId
    ) {

        productService.deleteProduct(productId);

        return ResponseEntity.noContent().build();

    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProductById(
            @PathVariable Long productId
    ) {

        ProductResponse product =
                productService.getProductById(productId);

        return ResponseEntity.ok(product);

    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getSellerProducts(
            @RequestHeader("Authorization") String jwt
    ) throws Exception {

        Seller seller =
                sellerService.getSellerProfile(jwt);

        List<ProductResponse> products =
                productService.getProductsBySeller(
                        seller.getId()
                );

        return ResponseEntity.ok(products);

    }

}