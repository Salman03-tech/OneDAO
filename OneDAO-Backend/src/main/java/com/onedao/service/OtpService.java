package com.onedao.service;

import com.onedao.security.JwtTokenProvider;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OtpService {

    @Value("${app.otp.length}")
    private int otpLength;

    @Value("${app.otp.expiration-minutes}")
    private long otpExpirationMinutes;

    private final JwtTokenProvider jwtTokenProvider;

    public OtpService(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public String generateOtp() {
        return RandomStringUtils.randomNumeric(otpLength);
    }

    public String generateOtpToken(String email, String otp) {
        long expirationMillis = otpExpirationMinutes * 60 * 1000;
        System.out.println("✅ Sending OTP " + otp + " to " + email);
        return jwtTokenProvider.generateOtpToken(email, otp, expirationMillis);
    }

    public boolean validateOtp(String email, String otp, String token) {
        if (!jwtTokenProvider.validateToken(token)) return false;

        String tokenEmail = jwtTokenProvider.getEmailFromOtpToken(token);
        String tokenOtp = jwtTokenProvider.getOtpFromToken(token);

        return email.equals(tokenEmail) && otp.equals(tokenOtp);
    }
}
