package com.project.ecommerce.Service.impl;

import com.project.ecommerce.Config.JwtProvider;
import com.project.ecommerce.Enums.USER_ROLE;
import com.project.ecommerce.Model.*;
import com.project.ecommerce.Repository.CartRepository;
import com.project.ecommerce.Repository.UserRepository;
import com.project.ecommerce.Request.LoginRequest;
import com.project.ecommerce.Request.ResetPasswordRequest;
import com.project.ecommerce.Response.AuthResponse;
import com.project.ecommerce.Request.SignUpRequest;
import com.project.ecommerce.Service.AuthService;
import com.project.ecommerce.Service.OtpService;
import com.project.ecommerce.Service.RateLimitService;
import com.project.ecommerce.Service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.project.ecommerce.Repository.SellerRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CartRepository cartRepository;
    private final JwtProvider jwtProvider;
    private final AuthenticationManager authenticationManager;
    private final SellerRepository sellerRepository;
    private final OtpService otpService;
    private final RefreshTokenService refreshTokenService;
    private final RateLimitService rateLimitService;
    @Override
    public void sendSignupOtp(String email) throws Exception {

        User existingUser = userRepository.findByEmail(email);
        Seller existingSeller = sellerRepository.findByEmail(email);
        if (existingUser != null|| existingSeller != null) {
            throw new Exception("Email already exists");
        }
        otpService.generateAndSendOtp(
                email,
                "Signup Verification OTP",
                "Your signup verification OTP is: "
        );
    }

    @Override
    public AuthResponse createUser(SignUpRequest request) throws Exception {

        User existingUser = userRepository.findByEmail(request.getEmail());
        Seller existingSeller = sellerRepository.findByEmail(request.getEmail());

        if (existingUser != null || existingSeller != null) {
            throw new Exception("Email already exists");
        }

        VerificationCode verificationCode =
                otpService.getByEmail(request.getEmail());

        otpService.validateOtp(
                verificationCode,
                request.getOtp()
        );

        User createdUser = new User();
        createdUser.setEmail(request.getEmail());
        createdUser.setFullName(request.getFullName());
        createdUser.setRole(USER_ROLE.ROLE_CUSTOMER);
        createdUser.setPhone(null);
        createdUser.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        createdUser = userRepository.save(createdUser);

        Cart cart = new Cart();
        cart.setUser(createdUser);
        cartRepository.save(cart);

        // OTP đã sử dụng -> xóa
        otpService.deleteOtp(verificationCode);

        // Tự động đăng nhập sau khi đăng ký thành công
        List<GrantedAuthority> authorities = new ArrayList<>();

        authorities.add(
                new SimpleGrantedAuthority(
                        USER_ROLE.ROLE_CUSTOMER.toString()
                )
        );

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        createdUser.getEmail(),
                        null,
                        authorities
                );

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);

        String jwt =
                jwtProvider.generateToken(authentication);

        RefreshToken refreshToken =
                refreshTokenService.createForUser(createdUser);

        AuthResponse response = new AuthResponse();

        response.setJwt(jwt);

        response.setRefreshToken(
                refreshToken.getToken()
        );

        response.setMessage("Register success");

        response.setRole(USER_ROLE.ROLE_CUSTOMER);

        return response;
    }

    @Override
    public AuthResponse login(LoginRequest request) throws Exception {
        rateLimitService.checkLoginLimit(
                request.getEmail()
        );
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getEmail(),
                                request.getPassword()
                        )
                );
        String email = authentication.getName();

        Seller seller = sellerRepository.findByEmail(email);
        if (seller != null) {
            if (!seller.isEmailVerified()) {
                throw new Exception("Email is not verified");
            }
            switch (seller.getAccountStatus()) {

                case PENDING_VERIFICATION:
                    throw new Exception("Seller account is waiting for admin approval");

                case SUSPENDED:
                    throw new Exception("Seller account has been suspended");

                case BANNED:
                    throw new Exception("Seller account has been banned");

                case DEACTIVATED:
                    throw new Exception("Seller account has been deactivated");

                case CLOSED:
                    throw new Exception("Seller account has been closed");

                case ACTIVE:
                    break;
            }
        }

        String token = jwtProvider.generateToken(authentication);
        RefreshToken refreshToken;
        if (seller != null) {

            refreshToken =
                    refreshTokenService.createForSeller(seller);

        } else {

            User user = userRepository.findByEmail(email);

            refreshToken =
                    refreshTokenService.createForUser(user);

        }
        AuthResponse authResponse = new AuthResponse();
        authResponse.setJwt(token);
        authResponse.setRefreshToken(
                refreshToken.getToken()
        );
        authResponse.setMessage("Login success");

        Collection<? extends GrantedAuthority> authorities =
                authentication.getAuthorities();

        String roleName = authorities.isEmpty()
                ? null
                : authorities.iterator().next().getAuthority();

        authResponse.setRole(USER_ROLE.valueOf(roleName));

        return authResponse;
    }

    @Override
    public void sendForgotPasswordOtp(String email) throws Exception {
        rateLimitService.checkForgotPasswordLimit(email);
        User user = userRepository.findByEmail(email);

        Seller seller = sellerRepository.findByEmail(email);

        if (user == null && seller == null) {
            throw new Exception("Email not found");
        }

        otpService.generateAndSendOtp(
                email,
                "Reset Password OTP",
                "Your password reset OTP is: "
        );
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) throws Exception {

        VerificationCode verificationCode = otpService.getByEmail(request.getEmail());

        otpService.validateOtp(
                verificationCode,
                request.getOtp()
        );


        User user = userRepository.findByEmail(request.getEmail());

        if (user != null) {

            user.setPassword(
                    passwordEncoder.encode(request.getNewPassword())
            );

            userRepository.save(user);

        } else {

            Seller seller =
                    sellerRepository.findByEmail(request.getEmail());

            if (seller == null) {
                throw new Exception("Account not found");
            }

            seller.setPassword(
                    passwordEncoder.encode(request.getNewPassword())
            );

            sellerRepository.save(seller);
        }


        otpService.deleteOtp(verificationCode);
    }

    @Override
    public AuthResponse refreshToken(String token) {

        RefreshToken refreshToken =
                refreshTokenService.verify(token);


        Authentication authentication;

        USER_ROLE role;

        RefreshToken newRefreshToken;


        if(refreshToken.getUser() != null){

            User user = refreshToken.getUser();

            role = user.getRole();


            authentication =
                    createAuthentication(
                            user.getEmail(),
                            role
                    );


            refreshTokenService.delete(token);


            newRefreshToken =
                    refreshTokenService.createForUser(user);


        }else{


            Seller seller =
                    refreshToken.getSeller();


            role = seller.getRole();


            authentication =
                    createAuthentication(
                            seller.getEmail(),
                            role
                    );


            refreshTokenService.delete(token);


            newRefreshToken =
                    refreshTokenService.createForSeller(seller);

        }


        String jwt =
                jwtProvider.generateToken(authentication);


        AuthResponse response =
                new AuthResponse();

        response.setJwt(jwt);

        response.setRefreshToken(
                newRefreshToken.getToken()
        );

        response.setMessage(
                "Refresh token success"
        );

        response.setRole(role);


        return response;
    }
    private Authentication createAuthentication(
            String email,
            USER_ROLE role
    ){

        List<GrantedAuthority> authorities =
                List.of(
                        new SimpleGrantedAuthority(
                                role.toString()
                        )
                );


        return new UsernamePasswordAuthenticationToken(
                email,
                null,
                authorities
        );
    }
    @Override
    public void logout(String refreshToken){

        refreshTokenService.delete(refreshToken);

    }
}