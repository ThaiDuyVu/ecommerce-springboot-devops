package com.project.ecommerce.Service;

public interface RateLimitService {

    void checkLoginLimit(String key);


    void checkForgotPasswordLimit(String key);
}
