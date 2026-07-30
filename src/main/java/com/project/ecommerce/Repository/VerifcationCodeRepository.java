package com.project.ecommerce.Repository;

import com.project.ecommerce.Model.User;
import com.project.ecommerce.Model.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerifcationCodeRepository extends JpaRepository<VerificationCode,Long> {

    VerificationCode findByEmail(String email);
    VerificationCode findByOtp(String otp);
    String user(User user);
}
