package com.project.ecommerce.Service.impl;

import com.project.ecommerce.Exceptions.AuthenticationException;
import com.project.ecommerce.Model.RefreshToken;
import com.project.ecommerce.Model.Seller;
import com.project.ecommerce.Model.User;
import com.project.ecommerce.Repository.RefreshTokenRepository;
import com.project.ecommerce.Service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    private static final long REFRESH_EXPIRE_DAYS = 30;


    @Override
    public RefreshToken createForUser(User user) {

        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setToken(UUID.randomUUID().toString());

        refreshToken.setCreatedAt(LocalDateTime.now());

        refreshToken.setExpiryDate(
                LocalDateTime.now().plusDays(REFRESH_EXPIRE_DAYS)
        );

        refreshToken.setUser(user);

        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public RefreshToken createForSeller(Seller seller) {

        refreshTokenRepository.deleteBySeller(seller);

        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setToken(UUID.randomUUID().toString());

        refreshToken.setCreatedAt(LocalDateTime.now());

        refreshToken.setExpiryDate(
                LocalDateTime.now().plusDays(REFRESH_EXPIRE_DAYS)
        );

        refreshToken.setSeller(seller);

        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public RefreshToken verify(String token) {

        RefreshToken refreshToken =
                refreshTokenRepository.findByToken(token)
                        .orElseThrow(
                                () -> new AuthenticationException(
                                        "Refresh token not found"
                                )
                        );

        if(LocalDateTime.now().isAfter(refreshToken.getExpiryDate())){

            refreshTokenRepository.delete(refreshToken);

            throw new AuthenticationException(
                    "Refresh token expired"
            );
        }

        return refreshToken;
    }

    @Override
    public void delete(String token) {

        RefreshToken refreshToken =
                refreshTokenRepository.findByToken(token)
                        .orElseThrow(
                                () -> new AuthenticationException(
                                        "Refresh token not found"
                                )
                        );


        refreshTokenRepository.delete(refreshToken);

    }

    @Override
    public void deleteByUser(User user) {

        refreshTokenRepository.deleteByUser(user);

    }

    @Override
    public void deleteBySeller(Seller seller) {

        refreshTokenRepository.deleteBySeller(seller);

    }
}
