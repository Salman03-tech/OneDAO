package com.onedao.service;

import com.onedao.payload.LoginRequest;
import com.onedao.payload.OtpVerificationRequest;
import com.onedao.payload.RegisterRequest;
import com.onedao.exception.CustomException;
import com.onedao.exception.InvalidCountryException;
import com.onedao.entity.User;
import com.onedao.repository.UserRepository;
import com.onedao.security.JwtTokenProvider;
import com.onedao.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final CountryValidationService countryValidationService;
    private final OtpService otpService;

    @Transactional
    public Map<String, Object> register(RegisterRequest registerRequest) {
        // Validate passwords match
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new CustomException("Passwords don't match");
        }

        // Validate country
        if (!countryValidationService.isCountryAllowed(registerRequest.getCountry())) {
            throw new InvalidCountryException("Registration from your country is not allowed");
        }

        // Check if user already exists
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new CustomException("Email already in use");
        }

        // Create new user
        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setCountry(registerRequest.getCountry());
        user.setEnabled(false); // User will be enabled after OTP verification
        userRepository.save(user);

        // Generate and send OTP
        String otp = otpService.generateOtp();
        String otpToken = otpService.generateOtpToken(user.getEmail(), otp);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "OTP sent to email");
        response.put("otpToken", otpToken);
        return response;
    }

    public Map<String, Object> login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        if (!userPrincipal.isEnabled()) {
            throw new CustomException("Account not verified. Please complete OTP verification.");
        }

        String jwt = jwtTokenProvider.generateToken(authentication);

        Map<String, Object> response = new HashMap<>();
        response.put("token", jwt);
        response.put("user", userPrincipal.getUser());
        return response;
    }

    @Transactional
    public Map<String, Object> verifyOtp(OtpVerificationRequest otpRequest) {
        // Step 1: Validate OTP
        if (!otpService.validateOtp(otpRequest.getEmail(), otpRequest.getOtp(), otpRequest.getOtpToken())) {
            throw new CustomException("Invalid OTP or token");
        }

        // Step 2: Enable user in DB
        User user = userRepository.findByEmail(otpRequest.getEmail())
                .orElseThrow(() -> new CustomException("User not found"));
        user.setEnabled(true);
        userRepository.save(user);

        // ✅ Step 3: Load UserPrincipal properly
        UserPrincipal userPrincipal = new UserPrincipal(user);

        // ✅ Step 4: Create auth with UserPrincipal
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal,
                null,
                userPrincipal.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // ✅ Step 5: Generate JWT from Authentication
        String jwt = jwtTokenProvider.generateToken(authentication);

        // Step 6: Return response
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Account verified successfully");
        response.put("token", jwt);
        response.put("user", user);
        return response;
    }
}
