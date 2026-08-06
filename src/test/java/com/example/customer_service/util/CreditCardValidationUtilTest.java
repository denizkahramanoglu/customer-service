package com.example.customer_service.util;


import com.example.customer_service.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CreditCardValidationUtilTest {

    private final Clock fixedClock = Clock.fixed(Instant.parse("2026-08-01T00:00:00Z"), ZoneId.systemDefault());

    @Test
    @DisplayName("Geçerli kart bilgileri için exception fırlatılmamalı")
    void validateCreditCard_shouldPassForValidCard() {

        assertDoesNotThrow(() -> CreditCardValidationUtil.validateCreditCard("4111111111111111", 12, 2027, fixedClock));
    }

    @Test
    @DisplayName("Kart bilgileri eksik olduğunda BusinessException fırlatılmalı")
    void validateCreditCard_shouldThrowExceptionWhenCardInfoMissing() {

        assertThrows(BusinessException.class, () ->
                CreditCardValidationUtil.validateCreditCard(null, 12, 2027, fixedClock));
    }

    @Test
    @DisplayName("Son kullanma tarihi geçmiş kart için BusinessException fırlatılmalı")
    void validateCreditCard_shouldThrowExceptionWhenCardExpired() {

        assertThrows(BusinessException.class, () ->
                CreditCardValidationUtil.validateCreditCard("4111111111111111", 7, 2026, fixedClock));
    }

    @Test
    @DisplayName("Geçersiz kart numarası için BusinessException fırlatılmalı")
    void validateCreditCard_shouldThrowExceptionWhenCardNumberInvalid() {

        assertThrows(BusinessException.class, () ->
                CreditCardValidationUtil.validateCreditCard("1234567890123456", 12, 2027, fixedClock));
    }

    @Test
    @DisplayName("Sadece harflerden oluşan kart numarası için BusinessException fırlatılmalı")
    void validateCreditCard_shouldThrowExceptionWhenCardNumberContainsNoDigits() {

        assertThrows(BusinessException.class, () -> CreditCardValidationUtil.validateCreditCard("abcdef", 12, 2027, fixedClock)
        );
    }

    @Test
    @DisplayName("Boş kart numarası için BusinessException fırlatılmalı")
    void validateCreditCard_shouldThrowExceptionWhenCardNumberEmpty() {

        assertThrows(BusinessException.class, () ->
                CreditCardValidationUtil.validateCreditCard("", 12, 2027, fixedClock));
    }
}