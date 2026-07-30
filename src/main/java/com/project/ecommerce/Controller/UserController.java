package com.project.ecommerce.Controller;

import com.project.ecommerce.Request.UpdateProfileRequest;
import com.project.ecommerce.Response.UserProfileResponse;
import com.project.ecommerce.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    @GetMapping("/users/profile")
    public ResponseEntity<UserProfileResponse> getProfile(
            @RequestHeader("Authorization") String jwt
    ) throws Exception {

        return ResponseEntity.ok(
                userService.getProfile(jwt)
        );
    }
    @PatchMapping("/users/profile")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @RequestHeader("Authorization") String jwt,
            @RequestBody UpdateProfileRequest request
    ) throws Exception {

        return ResponseEntity.ok(
                userService.updateProfile(jwt, request)
        );
    }
}
