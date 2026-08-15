package com.project.ecommerce.Service;

import com.project.ecommerce.Model.Address;
import com.project.ecommerce.Model.User;

import java.util.List;

public interface AddressService {

    List<Address> getUserAddresses(User user)
            throws Exception;

    Address getAddressById(
            Long addressId,
            User user
    ) throws Exception;
}