package com.project.ecommerce.Service.impl;

import com.project.ecommerce.Config.JwtProvider;
import com.project.ecommerce.Model.User;
import com.project.ecommerce.Repository.UserRepository;
import com.project.ecommerce.Request.UpdateProfileRequest;
import com.project.ecommerce.Response.UserProfileResponse;
import com.project.ecommerce.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository ;
    private final JwtProvider jwtProvider;
    @Override
    public User findUserByJwtToken(String jwt) throws Exception {
        String email = jwtProvider.getEmailFromJwtToken(jwt);
        return this.findUserByEmail(email);
    }

    @Override
    public User findUserByEmail(String email) throws Exception {
        User user = userRepository.findByEmail(email);
        if(user == null){
            throw new Exception("User not found with email -"+ email);
        }
        return user;
    }

    @Override
    public UserProfileResponse getProfile(String jwt) throws Exception {
        User user = findUserByJwtToken(jwt);

        UserProfileResponse response = new UserProfileResponse();

        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setPhone(user.getPhone());
        response.setRole(user.getRole());

        return response;
    }

    @Override
    public UserProfileResponse updateProfile(
            String jwt,
            UpdateProfileRequest request
    ) throws Exception {

        User user = findUserByJwtToken(jwt);

        if (request.getFullName() != null &&
                !request.getFullName().isBlank()) {

            user.setFullName(request.getFullName());
        }

        if (request.getPhone() != null &&
                !request.getPhone().isBlank()) {

            user.setPhone(request.getPhone());
        }

        User updatedUser = userRepository.save(user);

        UserProfileResponse response = new UserProfileResponse();

        response.setId(updatedUser.getId());
        response.setEmail(updatedUser.getEmail());
        response.setFullName(updatedUser.getFullName());
        response.setPhone(updatedUser.getPhone());
        response.setRole(updatedUser.getRole());

        return response;
    }

}
