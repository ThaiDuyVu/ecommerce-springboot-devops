package com.project.ecommerce.Exceptions;

public class JwtException extends RuntimeException {

    public JwtException(String message) {
        super(message);
    }
}