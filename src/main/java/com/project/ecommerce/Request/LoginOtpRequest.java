package com.project.ecommerce.Request;

import com.project.ecommerce.Enums.USER_ROLE;
import lombok.Data;

@Data
public class LoginOtpRequest {
    private String email;
    private String otp;
    USER_ROLE role;
}
