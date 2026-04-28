package com.example.secure_login.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    //separate buckets for each endpoint type, keyed by IP address
    private final Map<String, Bucket> loginBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> otpBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> registerBuckets = new ConcurrentHashMap<>();

    /**
     * LOGIN: 5 attempts per 15 minutes per IP
     * After 5 failed logins, block for 15 minutes
     */
    public Bucket getLoginBucket(String ipAddress){
        return loginBuckets.computeIfAbsent(ipAddress,key ->{
            Bandwidth limit = Bandwidth.classic(
                    5, // 5 token max
                    Refill.intervally(5, Duration.ofMinutes(15)) // refill every 15 min
            );
            return Bucket.builder().addLimit(limit).build();
        });
    }

    /**
     * OTP VERIFY: 3 attempts per 10 minutes per IP
     * Tighter limit since OTP is 6 digits (1,000,000 combos — we don't want brute force)
     */
    public Bucket getOtpBucket(String ipAddress){
        return otpBuckets.computeIfAbsent(ipAddress,key ->{
            Bandwidth limit = Bandwidth.classic(
                    3,
                    Refill.intervally(3, Duration.ofMinutes(10))
            );
            return Bucket.builder().addLimit(limit).build();
        });
    }

    /**
     * REGISTER: 3 registrations per hour per IP
     * Prevents spam account creation
     */
    public Bucket getRegisterBucket(String ipAddress){
        return registerBuckets.computeIfAbsent(ipAddress , key ->{
            Bandwidth limit = Bandwidth.classic(
                    3,
                    Refill.intervally(3, Duration.ofHours(1))
            );
            return Bucket.builder().addLimit(limit).build();
        });
    }

}
