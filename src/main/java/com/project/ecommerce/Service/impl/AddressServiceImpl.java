package com.project.ecommerce.Service.impl;


import com.project.ecommerce.Exceptions.ResourceNotFoundException;
import com.project.ecommerce.Model.Address;
import com.project.ecommerce.Model.User;
import com.project.ecommerce.Repository.AddressRepository;
import com.project.ecommerce.Service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl
        implements AddressService {

    private final AddressRepository addressRepository;


    @Override
    public List<Address> getUserAddresses(
            User user
    ) throws Exception {

        return addressRepository.findByUserId(
                user.getId()
        );
    }


    @Override
    public Address getAddressById(
            Long addressId,
            User user
    ) throws Exception {

        Address address =
                addressRepository.findById(addressId)
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Address not found"
                                )
                        );


        if (!address.getUser().getId()
                .equals(user.getId())) {

            throw new Exception(
                    "Address does not belong to this user"
            );
        }


        return address;
    }
}
