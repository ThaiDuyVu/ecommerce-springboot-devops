package com.project.ecommerce.Service;

import com.project.ecommerce.Model.User;
import com.project.ecommerce.Request.UpdateProfileRequest;
import com.project.ecommerce.Response.UserProfileResponse;

public interface UserService {
    User findUserByJwtToken(String jwt)throws Exception ;

    User findUserByEmail(String email)throws Exception ;

    UserProfileResponse getProfile(String jwt) throws Exception;

    UserProfileResponse updateProfile(
            String jwt,
            UpdateProfileRequest request
    ) throws Exception;
}
