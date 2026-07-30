package com.project.ecommerce.Request;

import com.project.ecommerce.Model.Address;
import com.project.ecommerce.Model.BankDetails;
import com.project.ecommerce.Model.BusinessDetails;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateSellerRequest {

    private String sellerName;

    private String phone;

    private String email;

    private String password;

    private String gstin;

    private BusinessDetails businessDetails;

    private BankDetails bankDetails;

    private Address pickupAddress;

    private String otp;
}