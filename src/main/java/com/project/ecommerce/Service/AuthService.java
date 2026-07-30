package com.project.ecommerce.Service;

import com.project.ecommerce.Request.LoginRequest;
import com.project.ecommerce.Request.ResetPasswordRequest;
import com.project.ecommerce.Response.AuthResponse;
import com.project.ecommerce.Request.SignUpRequest;

public interface AuthService {

    void sendSignupOtp(String email) throws Exception;

    AuthResponse createUser(SignUpRequest request) throws Exception;

    AuthResponse login(LoginRequest request) throws Exception;

    void sendForgotPasswordOtp(String email) throws Exception;

    void resetPassword(ResetPasswordRequest request) throws Exception;

    AuthResponse refreshToken(String refreshToken);

    void logout(String refreshToken);
}