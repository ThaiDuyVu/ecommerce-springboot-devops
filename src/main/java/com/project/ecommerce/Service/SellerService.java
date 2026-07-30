package com.project.ecommerce.Service;

import com.project.ecommerce.Enums.AccountStatus;
import com.project.ecommerce.Model.Seller;
import com.project.ecommerce.Request.CreateSellerRequest;

import java.util.List;

public interface SellerService {
    Seller getSellerProfile(String jwt ) throws Exception;
    Seller createSeller(CreateSellerRequest request);
    Seller getSellerById(Long id);
    Seller getSellerByEmail(String email);
    List<Seller> getAllSeller(AccountStatus status);
    Seller updateSeller(Long id , Seller seller) throws Exception;
    void deleteSeller(Long id);
    Seller updateSellerAccountStatus(Long id, AccountStatus status);
    void deleteExpiredUnverifiedSellers();
    void sendSignupOtp(String email);
}
