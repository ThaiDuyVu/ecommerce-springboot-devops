package com.project.ecommerce.Request;

import lombok.Data;

@Data
public class SignUpOtpRequest {
    private String email;
    private String fullName;
    private String otp;
    private String password;
}
