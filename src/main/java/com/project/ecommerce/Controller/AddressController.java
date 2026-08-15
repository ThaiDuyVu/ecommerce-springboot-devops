package com.project.ecommerce.Controller;

import com.project.ecommerce.Model.Address;
import com.project.ecommerce.Model.User;
import com.project.ecommerce.Service.AddressService;
import com.project.ecommerce.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/addresses")
public class AddressController {

    private final AddressService addressService;

    private final UserService userService;


    @GetMapping
    public ResponseEntity<List<Address>> getUserAddresses(
            @RequestHeader("Authorization") String jwt
    ) throws Exception {

        User user =
                userService.findUserByJwtToken(jwt);


        return ResponseEntity.ok(
                addressService.getUserAddresses(user)
        );
    }


    @GetMapping("/{addressId}")
    public ResponseEntity<Address> getAddressById(
            @PathVariable Long addressId,
            @RequestHeader("Authorization") String jwt
    ) throws Exception {

        User user =
                userService.findUserByJwtToken(jwt);


        return ResponseEntity.ok(
                addressService.getAddressById(
                        addressId,
                        user
                )
        );
    }
}