package com.project.ecommerce.Response;

import com.project.ecommerce.Enums.USER_ROLE;
import lombok.Data;

@Data
public class AuthResponse {
    private String jwt;
    private String refreshToken;
    private String message;
    private USER_ROLE role;
}
