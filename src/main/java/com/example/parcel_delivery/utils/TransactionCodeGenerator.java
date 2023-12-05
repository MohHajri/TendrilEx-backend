package com.example.parcel_delivery.utils;

import org.springframework.stereotype.Component;
import java.security.SecureRandom;

@Component
public class TransactionCodeGenerator {

    private static final SecureRandom random = new SecureRandom();
    private static final int MIN = 100000;
    private static final int MAX = 999999;

    public int generateTransactionCode() {
        return random.nextInt((MAX - MIN) + 1) + MIN;
    }
}
