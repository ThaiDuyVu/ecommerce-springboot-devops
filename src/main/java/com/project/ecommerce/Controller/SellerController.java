package com.project.ecommerce.Controller;

import com.project.ecommerce.Enums.AccountStatus;
import com.project.ecommerce.Model.Seller;
import com.project.ecommerce.Model.SellerReport;
import com.project.ecommerce.Request.CreateSellerRequest;
import com.project.ecommerce.Request.LoginRequest;
import com.project.ecommerce.Request.SellerOtpRequest;
import com.project.ecommerce.Response.ApiResponse;
import com.project.ecommerce.Response.AuthResponse;
import com.project.ecommerce.Service.AuthService;
import com.project.ecommerce.Service.SellerReportService;
import com.project.ecommerce.Service.SellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sellers")
public class SellerController {
    private final SellerService sellerService;
    private final AuthService authService ;
    private final SellerReportService sellerReportService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginSeller(@RequestBody LoginRequest request) throws Exception {
        String email = request.getEmail();
        request.setEmail("seller_"+email);
        AuthResponse authResponse =authService.login(request);

         return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse> sendSellerOtp(
            @RequestBody SellerOtpRequest request) {

        sellerService.sendSignupOtp(request.getEmail());

        ApiResponse response = new ApiResponse();
        response.setMessage("OTP sent successfully");

        return ResponseEntity.ok(response);
    }
    @PostMapping
    public ResponseEntity<Seller> createSeller(
            @RequestBody CreateSellerRequest request){

        Seller savedSeller =
                sellerService.createSeller(request);

        return new ResponseEntity<>(savedSeller, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Seller> getSellerById(@PathVariable Long id) {
        Seller seller = sellerService.getSellerById(id);
        return new ResponseEntity<>(seller, HttpStatus.OK);
    }

    @GetMapping("/profile")
    public ResponseEntity<Seller> getSellerByJwt(@RequestHeader("Authorization") String jwt) throws Exception{
        Seller seller = sellerService.getSellerProfile(jwt);
        return new ResponseEntity<>(seller,HttpStatus.OK);
    }

    @GetMapping("/report")
    public ResponseEntity<SellerReport> getSellerReport(@RequestHeader("Authorization") String jwt) throws Exception{
        Seller seller = sellerService.getSellerProfile(jwt);
        SellerReport report = sellerReportService.getSellerReport(seller);
        return new ResponseEntity<>(report,HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<Seller>> getAllSellers(@RequestParam(required = false)AccountStatus status){
        List<Seller> sellers = sellerService.getAllSeller(status);
        return ResponseEntity.ok(sellers);
    }

    @PatchMapping()
    public ResponseEntity<Seller> updateSeller(@RequestHeader("Authorization")String jwt ,@RequestBody Seller seller ) throws Exception{
        Seller profile = sellerService.getSellerProfile(jwt);
        Seller updateSeller = sellerService.updateSeller(profile.getId(), seller);
        return ResponseEntity.ok(updateSeller);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSeller(@PathVariable Long id){
        sellerService.deleteSeller(id);
        return ResponseEntity.noContent().build();
    }
}
