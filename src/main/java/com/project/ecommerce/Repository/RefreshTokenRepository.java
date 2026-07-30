package com.project.ecommerce.Repository;

import com.project.ecommerce.Model.RefreshToken;
import com.project.ecommerce.Model.Seller;
import com.project.ecommerce.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository
        extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByUser(User user);

    Optional<RefreshToken> findBySeller(Seller seller);

    void deleteByToken(String token);

    void deleteByUser(User user);

    void deleteBySeller(Seller seller);
}