package com.presta.domain.model.valueobject;

import java.util.Objects;

public record Address(
        String street,
        String city,
        String postalCode,
        String country
) {

    public Address {
        street = validateField(street, "Street", 200);
        city = validateField(city, "City", 100);
        postalCode = validateField(postalCode, "Postal code", 20);
        country = validateField(country, "Country", 100);
    }

    public static Address of(String street, String city, String postalCode, String country) {
        return new Address(street, city, postalCode, country);
    }

    private static String validateField(String value, String fieldName, int maxLength) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty");
        }
        String trimmed = value.trim();
        if (trimmed.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " cannot exceed " + maxLength + " characters");
        }
        return trimmed;
    }

    public String getFormattedAddress() {
        return String.format("%s, %s %s, %s", street, city, postalCode, country);
    }

    @Override
    public String toString() {
        return getFormattedAddress();
    }
}
