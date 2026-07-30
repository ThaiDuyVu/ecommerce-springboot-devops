package com.project.ecommerce.Controller;

import com.project.ecommerce.Request.ProductFilterRequest;
import com.project.ecommerce.Response.ProductResponse;
import com.project.ecommerce.Service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(
            @PathVariable Long id
    ) {

        ProductResponse product =
                productService.getProductById(id);

        return ResponseEntity.ok(product);

    }

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getProducts(
            ProductFilterRequest filter
    ) {

        Page<ProductResponse> products =
                productService.getProducts(filter);

        return ResponseEntity.ok(products);

    }

}