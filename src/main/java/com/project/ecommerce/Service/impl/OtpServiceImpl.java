package com.project.ecommerce.Service.impl;

import com.project.ecommerce.Exceptions.InvalidOtpException;
import com.project.ecommerce.Exceptions.OtpExpiredException;
import com.project.ecommerce.Exceptions.TooManyRequestsException;
import com.project.ecommerce.Model.VerificationCode;
import com.project.ecommerce.Repository.VerifcationCodeRepository;
import com.project.ecommerce.Service.EmailService;
import com.project.ecommerce.Service.OtpService;
import com.project.ecommerce.Utils.OtpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {
    private final VerifcationCodeRepository verifcationCodeRepository;
    private final EmailService emailService;

    @Override
    public void generateAndSendOtp(
            String email,
            String subject,
            String messagePrefix
    ){

        VerificationCode existingOtp =
                verifcationCodeRepository.findByEmail(email);

        if (existingOtp != null) {

            LocalDateTime nextAvailableTime =
                    existingOtp.getCreatedAt().plusSeconds(60);

            if (LocalDateTime.now().isBefore(nextAvailableTime)) {

                long remainSeconds =
                        Duration.between(
                                LocalDateTime.now(),
                                nextAvailableTime
                        ).getSeconds();

                System.out.println(
                        "[OTP COOLDOWN] " +
                                email +
                                " còn phải chờ "
                                + remainSeconds +
                                " giây để gửi lại OTP."
                );

                throw new TooManyRequestsException(
                        "Please wait "
                                + remainSeconds
                                + " seconds before requesting another OTP."
                );
            }

            verifcationCodeRepository.delete(existingOtp);
        }

        String otp = OtpUtil.generateOtp();

        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setEmail(email);
        verificationCode.setOtp(otp);
        verificationCode.setCreatedAt(LocalDateTime.now());
        verifcationCodeRepository.save(verificationCode);

        emailService.senVerificationOtpEmail(
                email,
                otp,
                subject,
                messagePrefix + otp
        );
    }

    @Override
    public void validateOtp(
            VerificationCode verificationCode,
            String otp
    ){

        if(verificationCode == null){
            throw new InvalidOtpException("OTP not found");
        }

        if(!verificationCode.getOtp().equals(otp)){
            throw new InvalidOtpException("Wrong OTP");
        }
        if (verificationCode.getCreatedAt() == null) {
            verifcationCodeRepository.delete(verificationCode);
            throw new InvalidOtpException("Invalid OTP");
        }

        LocalDateTime expiredTime =
                verificationCode.getCreatedAt()
                        .plusMinutes(3);

        if(LocalDateTime.now().isAfter(expiredTime)){
            verifcationCodeRepository.delete(verificationCode);

            throw new OtpExpiredException(
                    "OTP expired"
            );
        }
    }

    @Override
    public VerificationCode getByEmail(String email) {

        return verifcationCodeRepository.findByEmail(email);
    }
    @Override
    public VerificationCode getByOtp(String otp) {

        return verifcationCodeRepository.findByOtp(otp);

    }
    @Override
    public void deleteOtp(
            VerificationCode verificationCode
    ) {

        verifcationCodeRepository.delete(verificationCode);

    }

}