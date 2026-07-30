package com.project.ecommerce.Service;

import com.project.ecommerce.Model.Product;
import com.project.ecommerce.Model.Seller;
import com.project.ecommerce.Request.CreateProductRequest;
import com.project.ecommerce.Request.ProductFilterRequest;
import com.project.ecommerce.Request.UpdateProductRequest;
import com.project.ecommerce.Response.ProductResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductService {

    ProductResponse createProduct(
            CreateProductRequest request,
            Seller seller
    );


    ProductResponse updateProduct(
            Long productId,
            UpdateProductRequest request
    );


    void deleteProduct(Long productId);


    ProductResponse getProductById(Long productId);


    List<ProductResponse> getProductsBySeller(Long sellerId);


    Page<ProductResponse> getProducts(
            ProductFilterRequest filter
    );
    Product findProductEntityById(
            Long productId
    );
}