package com.example.customer_service.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PhoneNumberValidatorTest {

    @Test
    @DisplayName("Utility sınıfın private constructor'ı initialize edilebilmeli (Coverage için)")
    void constructor_shouldBeInvoked() throws Exception {
        Constructor<PhoneNumberValidator> constructor = PhoneNumberValidator.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertNotNull(constructor.newInstance());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "+90 555 123 45 67",
            "05551234567",
            "905551234567",
            "555-123-45-67",
            "(555) 123 4567",
            " +90 (555) 123-45-67 "
    })
    @DisplayName("Farklı formatlardaki geçerli numaralar 10 haneli standart formata dönüştürülmeli")
    void cleanAndValidate_shouldReturnCleaned10DigitNumber(String input) {
        assertEquals("5551234567", PhoneNumberValidator.cleanAndValidate(input));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    @DisplayName("Null, boş veya sadece boşluk içeren girişlerde hata fırlatılmalı")
    void cleanAndValidate_shouldThrowExceptionForNullOrEmptyInput(String input) {
        assertEquals("Telefon numarası boş olamaz!",
                assertThrows(IllegalArgumentException.class, () -> PhoneNumberValidator.cleanAndValidate(input)).getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "4551234567",   // 5 ile başlamıyor
            "0555123456",   // Eksik hane (10 karakter)
            "055512345678", // Fazla hane (12 karakter)
            "555123456A",   // Harf içeriyor
            "904551234567"  // 90 ile başlıyor ama devamı 5 ile başlamıyor
    })
    @DisplayName("Geçersiz format, uzunluk veya karakter içeren girişlerde hata fırlatılmalı")
    void cleanAndValidate_shouldThrowExceptionForInvalidFormat(String input) {
        assertEquals("Geçersiz cep telefonu formatı! Lütfen doğru bir numara giriniz.",
                assertThrows(IllegalArgumentException.class, () -> PhoneNumberValidator.cleanAndValidate(input)).getMessage());
    }
}