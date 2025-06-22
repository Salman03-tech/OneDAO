package com.onedao.service;

import com.onedao.exception.InvalidCountryException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class CountryValidationService {
    @Value("${app.allowed-countries}")
    private String allowedCountries;

    @Value("${app.blocked-countries}")
    private String blockedCountries;

    public boolean isCountryAllowed(String countryCode) {
        List<String> allowed = Arrays.asList(allowedCountries.split(","));
        List<String> blocked = Arrays.asList(blockedCountries.split(","));

        if (blocked.contains(countryCode)) {
            return false;
        }

        return allowed.contains(countryCode);
    }
}

