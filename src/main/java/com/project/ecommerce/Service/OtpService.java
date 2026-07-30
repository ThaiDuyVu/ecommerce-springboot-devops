package com.project.ecommerce.Service;


import com.project.ecommerce.Model.VerificationCode;

public interface OtpService {

    void generateAndSendOtp(String email, String subject, String messagePrefix );

    void validateOtp( VerificationCode verificationCode, String otp );
    VerificationCode getByEmail(String email);

    VerificationCode getByOtp(String otp);

    void deleteOtp(VerificationCode verificationCode);
}