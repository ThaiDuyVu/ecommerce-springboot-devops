package com.project.ecommerce.Response;

import com.project.ecommerce.Enums.USER_ROLE;
import lombok.Data;

@Data
public class UserProfileResponse {

    private Long id;

    private String email;

    private String fullName;

    private String phone;

    private USER_ROLE role;

}