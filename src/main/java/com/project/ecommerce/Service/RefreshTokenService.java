package com.project.ecommerce.Service;

import com.project.ecommerce.Model.RefreshToken;
import com.project.ecommerce.Model.Seller;
import com.project.ecommerce.Model.User;

public interface RefreshTokenService {

    RefreshToken createForUser(User user);

    RefreshToken createForSeller(Seller seller);

    RefreshToken verify(String token);

    void delete(String token);

    void deleteByUser(User user);

    void deleteBySeller(Seller seller);

}