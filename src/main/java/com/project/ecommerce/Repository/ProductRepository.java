package com.project.ecommerce.Repository;

import com.project.ecommerce.Model.Product;
import com.project.ecommerce.Model.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository
        extends JpaRepository<Product, Long>,
        JpaSpecificationExecutor<Product> {


    List<Product> findBySeller_Id(Long sellerId);


    boolean existsBySlugAndSeller(
            String slug,
            Seller seller
    );
    Optional<Product> findBySlugAndSeller(
            String slug,
            Seller seller
    );
}