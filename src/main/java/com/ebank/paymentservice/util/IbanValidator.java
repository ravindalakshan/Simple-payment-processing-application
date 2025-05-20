package com.ebank.paymentservice.util;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class IbanValidator {
    private static final Pattern IBAN_PATTERN = Pattern.compile("^[A-Z]{2}[0-9]{2}[A-Z0-9]{4,}$");
    private static final List<String> SEPA_COUNTRIES = Arrays.asList("LV", "LT", "EE");

    public boolean isInvalid(String iban) {
        if (iban == null || iban.length() < 5) {
            return true;
        }
        return !IBAN_PATTERN.matcher(iban).matches();
    }

    public boolean isSEPACountryIban(String iban) {
        if (iban == null || iban.length() < 2) {
            return false;
        }
        String countryCode = iban.substring(0, 2);
        return SEPA_COUNTRIES.contains(countryCode);
    }
}
