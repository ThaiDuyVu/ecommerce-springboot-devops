package com.project.ecommerce.Exceptions;


public class OtpExpiredException extends RuntimeException {

    public OtpExpiredException(String message){
        super(message);
    }
}