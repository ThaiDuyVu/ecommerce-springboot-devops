package com.project.ecommerce.Repository;

import com.project.ecommerce.Model.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {

}
