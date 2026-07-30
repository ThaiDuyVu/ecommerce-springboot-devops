package com.project.ecommerce.Controller;

import com.project.ecommerce.Request.*;
import com.project.ecommerce.Response.ApiResponse;
import com.project.ecommerce.Response.AuthResponse;
import com.project.ecommerce.Request.SignUpRequest;
import com.project.ecommerce.Service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup/send-otp")
    public ResponseEntity<ApiResponse> sendSignupOtp(
            @RequestBody SignUpOtpRequest request) throws Exception {

        authService.sendSignupOtp(request.getEmail());

        ApiResponse res = new ApiResponse();
        res.setMessage("OTP sent successfully");

        return ResponseEntity.ok(res);
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> createUserHandle(
            @RequestBody SignUpRequest request) throws Exception {

        AuthResponse response = authService.createUser(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginHandle(
            @RequestBody LoginRequest request) throws Exception {

        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/forgot-password/send-otp")
    public ResponseEntity<ApiResponse> sendForgotPasswordOtp(
            @RequestBody ForgotPasswordOtpRequest request) throws Exception {

        authService.sendForgotPasswordOtp(request.getEmail());

        ApiResponse res = new ApiResponse();
        res.setMessage("OTP sent successfully");

        return ResponseEntity.ok(res);
    }

    @PostMapping("/forgot-password/reset")
    public ResponseEntity<ApiResponse> resetPassword(
            @RequestBody ResetPasswordRequest request) throws Exception {

        authService.resetPassword(request);

        ApiResponse res = new ApiResponse();
        res.setMessage("Password reset successfully");

        return ResponseEntity.ok(res);
    }
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @RequestBody RefreshTokenRequest request
    ) {

        AuthResponse response =
                authService.refreshToken(request.getRefreshToken());

        return ResponseEntity.ok(response);
    }
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(
            @RequestBody RefreshTokenRequest request
    ){

        authService.logout(
                request.getRefreshToken()
        );


        ApiResponse response = new ApiResponse();

        response.setMessage(
                "Logout success"
        );


        return ResponseEntity.ok(response);
    }
}