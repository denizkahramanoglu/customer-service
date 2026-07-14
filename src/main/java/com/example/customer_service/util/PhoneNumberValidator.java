package com.example.customer_service.util;

public class PhoneNumberValidator {
    private PhoneNumberValidator() {}
    public static String cleanAndValidate(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Telefon numarası boş olamaz!");
        }

        String cleaned = phoneNumber.replaceAll("[\\s\\-\\(\\)\\+]", "");


        if (cleaned.startsWith("90") && cleaned.length() == 12) {
            cleaned = cleaned.substring(2);
        }

        else if (cleaned.startsWith("0") && cleaned.length() == 11) {
            cleaned = cleaned.substring(1);
        }

        if (cleaned.length() != 10 || !cleaned.startsWith("5") || !cleaned.matches("\\d+")) {
            throw new IllegalArgumentException("Geçersiz cep telefonu formatı! Lütfen doğru bir numara giriniz.");
        }

        return cleaned;
    }
}