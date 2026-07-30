package com.project.ecommerce.Service.impl;


import com.project.ecommerce.Exceptions.TooManyRequestsException;
import com.project.ecommerce.Service.RateLimitService;
import io.github.bucket4j.*;
import org.springframework.stereotype.Service;


import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;


@Service
public class RateLimitServiceImpl
        implements RateLimitService {

    private static final int LOGIN_LIMIT = 5;

    private static final int FORGOT_PASSWORD_LIMIT = 3;

    private static final int REFILL_SECONDS = 60;

    private final ConcurrentHashMap<String, Bucket> buckets =
            new ConcurrentHashMap<>();


    private Bucket createBucket(
            int capacity
    ){

        Bandwidth limit =
                Bandwidth.builder()
                        .capacity(capacity)
                        .refillGreedy(
                                capacity,
                                Duration.ofSeconds(REFILL_SECONDS)
                        )
                        .build();


        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private Bucket getBucket(
            String key,
            int capacity
    ){
        return buckets.computeIfAbsent(
                key,
                k -> createBucket(capacity)
        );
    }

    @Override
    public void checkLoginLimit(String key){
        Bucket bucket =
                getBucket(
                        "LOGIN_" + key,
                        LOGIN_LIMIT
                );


        if(!bucket.tryConsume(1)){

            throw new TooManyRequestsException(
                    "Too many login attempts, try again later"
            );

        }

    }

    @Override
    public void checkForgotPasswordLimit(String key){


        Bucket bucket =
                getBucket(
                        "FORGOT_PASSWORD_" + key,
                        FORGOT_PASSWORD_LIMIT
                );


        if(!bucket.tryConsume(1)){

            throw new TooManyRequestsException(
                    "Too many password reset requests"
            );

        }

    }

}