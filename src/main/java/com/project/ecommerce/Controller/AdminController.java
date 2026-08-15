package com.project.ecommerce.Controller;

import com.project.ecommerce.Enums.AccountStatus;
import com.project.ecommerce.Model.Seller;
import com.project.ecommerce.Service.SellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AdminController {
    private final SellerService sellerService;
    @PatchMapping("/seller/{id}/status/{status}")
    public ResponseEntity<Seller> updateSellerStatus(
            @PathVariable Long id ,
            @PathVariable AccountStatus status
            ){
                Seller updatedSeller = sellerService.updateSellerAccountStatus(id,status);
                return ResponseEntity.ok(updatedSeller);
    }

}
