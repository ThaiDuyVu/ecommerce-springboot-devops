package com.project.ecommerce.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender javaMailSender ;
    public void senVerificationOtpEmail(String userEmail, String otp, String subject, String text ){
        try{
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage,"utf-8");
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setText(text);
            mimeMessageHelper.setTo(userEmail);
            javaMailSender.send(mimeMessage);
        }
        catch (MailException e){
            System.out.println("Err : "+e);
            throw new MailSendException("failed to send mail");
        }
        catch (MessagingException e){
            throw new RuntimeException(e);
        }
    }
}
