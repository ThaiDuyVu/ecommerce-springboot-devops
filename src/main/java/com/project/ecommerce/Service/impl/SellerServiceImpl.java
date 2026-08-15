package com.project.ecommerce.Service.impl;


import com.project.ecommerce.Config.JwtProvider;
import com.project.ecommerce.Enums.AccountStatus;
import com.project.ecommerce.Enums.USER_ROLE;
import com.project.ecommerce.Exceptions.DuplicateResourceException;
import com.project.ecommerce.Exceptions.ResourceNotFoundException;
import com.project.ecommerce.Model.Seller;
import com.project.ecommerce.Model.User;
import com.project.ecommerce.Model.VerificationCode;
import com.project.ecommerce.Repository.SellerRepository;
import com.project.ecommerce.Repository.UserRepository;
import com.project.ecommerce.Repository.VerifcationCodeRepository;
import com.project.ecommerce.Request.CreateSellerRequest;
import com.project.ecommerce.Service.OtpService;
import com.project.ecommerce.Service.SellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SellerServiceImpl implements SellerService {

    private final SellerRepository sellerRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final VerifcationCodeRepository verifcationCodeRepository;
    private final UserRepository userRepository;
    private final OtpService otpService;


    @Override
    public Seller getSellerProfile(String jwt) {
        String email   = jwtProvider.getEmailFromJwtToken(jwt);
        return this.getSellerByEmail(email);
    }

    @Override
    public Seller createSeller(CreateSellerRequest request) {

        Seller existingSeller = sellerRepository.findByEmail(request.getEmail());

        if (existingSeller != null) {
            throw new DuplicateResourceException("Email already exists");
        }
        VerificationCode verificationCode =
                otpService.getByEmail(request.getEmail());

        otpService.validateOtp(
                verificationCode,
                request.getOtp()
        );
        Seller newSeller = new Seller();

        newSeller.setEmail(request.getEmail());

        newSeller.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        newSeller.setSellerName(request.getSellerName());

        newSeller.setPickupAddress(request.getPickupAddress());

        newSeller.setGSTIN(request.getGstin());

        newSeller.setCreatedAt(LocalDateTime.now());

        newSeller.setRole(USER_ROLE.ROLE_SELLER);

        newSeller.setPhone(request.getPhone());

        newSeller.setBankDetails(request.getBankDetails());

        newSeller.setBusinessDetails(request.getBusinessDetails());
        newSeller.setEmailVerified(true);
        // Lưu Seller
        Seller savedSeller = sellerRepository.save(newSeller);
        otpService.deleteOtp(
                verificationCode
        );

        return savedSeller;
    }

    @Override
    public Seller getSellerById(Long id){

        return sellerRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Seller not found with id :" + id
                        )
                );
    }

    @Override
    public Seller getSellerByEmail(String email) {

        return Optional.ofNullable(
                        sellerRepository.findByEmail(email)
                )
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Seller not found with email: " + email
                        )
                );
    }

    @Override
    public List<Seller> getAllSeller(AccountStatus status) {

        if (status == null) {
            return sellerRepository.findAll();
        }

        return sellerRepository.findByAccountStatus(status);
    }

    @Override
    public Seller updateSeller(Long id, Seller seller) {
        Seller existingSeller = this.getSellerById(id);
        if(seller.getSellerName() != null){
            existingSeller.setSellerName(seller.getSellerName());
        }
        if(seller.getPhone()!= null){
            existingSeller.setPhone(seller.getPhone());
        }
        if(seller.getBusinessDetails() != null && seller.getBusinessDetails().getBusinessName() != null){
            existingSeller.getBusinessDetails().setBusinessName(seller.getBusinessDetails().getBusinessName());
        }
        if(seller.getBankDetails()!= null
                && seller.getBankDetails().getIfscCode() != null
                && seller.getBankDetails().getAccountHolderName() != null
                && seller.getBankDetails().getAccountNumber() != null){

            existingSeller.getBankDetails().setAccountHolderName(seller.getBankDetails().getAccountHolderName());
            existingSeller.getBankDetails().setAccountNumber(seller.getBankDetails().getAccountNumber());
            existingSeller.getBankDetails().setIfscCode(seller.getBankDetails().getIfscCode());
        }
        if (    seller.getPickupAddress() != null
                && seller.getPickupAddress().getAddress() != null
                && seller.getPickupAddress().getPhone() != null
                && seller.getPickupAddress().getCity() != null
                && seller.getPickupAddress().getState() != null) {

            existingSeller.getPickupAddress().setAddress(seller.getPickupAddress().getAddress());
            existingSeller.getPickupAddress().setPhone(seller.getPickupAddress().getPhone());
            existingSeller.getPickupAddress().setCity(seller.getPickupAddress().getCity());
            existingSeller.getPickupAddress().setState(seller.getPickupAddress().getState());
            existingSeller.getPickupAddress().setPinCode(seller.getPickupAddress().getPinCode());

        }
        if(seller.getGSTIN() != null){
            existingSeller.setGSTIN(seller.getGSTIN());
        }
        return sellerRepository.save(existingSeller);
    }

    @Override
    public void deleteSeller(Long id) {
        Seller seller = getSellerById(id);
        VerificationCode code =
                otpService.getByEmail(
                        seller.getEmail()
                );

        if(code != null){
            otpService.deleteOtp(code);
        }

        sellerRepository.delete(seller);
    }


    @Override
    public Seller updateSellerAccountStatus(Long id, AccountStatus status){
        Seller seller = getSellerById(id);
        seller.setAccountStatus(status);
        return sellerRepository.save(seller);
    }

    @Override
    public void deleteExpiredUnverifiedSellers() {

        LocalDateTime expiredTime = LocalDateTime.now().minusHours(24);

        List<Seller> expiredSellers =
                sellerRepository.findByIsEmailVerifiedFalseAndCreatedAtBefore(expiredTime);

        for (Seller seller : expiredSellers) {

            VerificationCode verificationCode =
                    otpService.getByEmail(
                            seller.getEmail()
                    );

            if (verificationCode != null) {
                otpService.deleteOtp(verificationCode);
            }
            sellerRepository.delete(seller);
        }
    }

    @Override
    public void sendSignupOtp(String email) {

        User existingUser = userRepository.findByEmail(email);

        if (existingUser != null) {
            throw new DuplicateResourceException("Email already exists");
        }

        Seller existingSeller = sellerRepository.findByEmail(email);

        if (existingSeller != null) {
            throw new DuplicateResourceException("Email already exists");
        }

        otpService.generateAndSendOtp(
                email,
                "Seller Email Verification",
                "Your verification OTP is: "
        );
    }
}
