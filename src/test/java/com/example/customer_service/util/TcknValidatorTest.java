package com.example.customer_service.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

class TcknValidatorTest {

    @Test
    @DisplayName("Girdi boş string olduğunda false dönmelidir")
    void validateTckn_ShouldReturnFalse_WhenTcknIsEmptyString() {
        Assertions.assertFalse(TcknValidator.isValid(""));
    }

    @Test
    @DisplayName("Girdi null olduğunda false dönmelidir")
    void validateTckn_ShouldReturnFalse_WhenTcknIsNull() {
        assertFalse(TcknValidator.isValid(null));
    }

    @Test
    @DisplayName("Girdi eksik haneli (10 basamaklı) olduğunda false dönmelidir")
    void validateTckn_ShouldReturnFalse_WhenTcknIsTenDigits() {
        assertFalse(TcknValidator.isValid("1234567890"));
    }

    @Test
    @DisplayName("Girdi fazla haneli (12 basamaklı) olduğunda false dönmelidir")
    void validateTckn_ShouldReturnFalse_WhenTcknIsTwelveDigits() {
        assertFalse(TcknValidator.isValid("123456789012"));
    }

    @Test
    @DisplayName("Girdi harf içerdiğinde false dönmelidir")
    void validateTckn_ShouldReturnFalse_WhenTcknContainsLetters() {
        assertFalse(TcknValidator.isValid("12O45ABC901"));
    }

    @Test
    @DisplayName("Girdi sıfır ile başladığında false dönmelidir")
    void validateTckn_ShouldReturnFalse_WhenTcknStartsWithZero() {
        assertFalse(TcknValidator.isValid("01234567890"));
    }

    @Test
    @DisplayName("Matematiksel algoritması hatalı olan TCKN için false dönmelidir")
    void validateTckn_ShouldReturnFalse_WhenTcknChecksumIsInvalid() {
        assertFalse(TcknValidator.isValid("12345678901"));
    }

    @Test
    @DisplayName("Geçerli 11 haneli TCKN girildiğinde true dönmelidir")
    void validateTckn_ShouldReturnTrue_WhenTcknIsValid() {
        assertTrue(TcknValidator.isValid("10000000146"));
    }
    @Test
    @DisplayName("Matematiksel fark negatif çıktığında mod hesabını doğru yapıp true dönmelidir")
    void validateTckn_ShouldReturnTrue_WhenTcknAlgorithmResultIsNegative() {
        assertTrue(TcknValidator.isValid("18181818062"));
    }
    @Test
    @DisplayName("10. hanesi doğru ama 11. hanesi yanlış olan TCKN için false dönmelidir")
    void validateTckn_ShouldReturnFalse_WhenEleventhDigitIsInvalid() {
        assertFalse(TcknValidator.isValid("10000000145"));
    }
    @Test
    @DisplayName("Utility sınıfının gizli constructor'ı nesne üretimini engellemelidir")
    void constructor_ShouldThrowException() throws NoSuchMethodException {
        Constructor<TcknValidator> constructor = TcknValidator.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        InvocationTargetException exception = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertTrue(exception.getCause() instanceof IllegalStateException);
        assertEquals("Utility sınıfı nesne olarak üretilemez!", exception.getCause().getMessage());
    }
}