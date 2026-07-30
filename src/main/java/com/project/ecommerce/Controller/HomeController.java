package com.project.ecommerce.Controller;

import com.project.ecommerce.Response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {
    @GetMapping
    public ApiResponse HomeControllerHandle(){
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage("Hello world");
        return apiResponse;
    }

}
