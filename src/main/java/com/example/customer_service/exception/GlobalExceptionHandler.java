package com.example.customer_service.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Sınıfa özel Logger'ımızı tanımlıyoruz
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 1. Kendi yazdığımız iş kuralı hatalarını yakalar
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(BusinessException ex) {

        // Hatayı arka planda konsola yazdırıyoruz (Loglama)
        logger.error("İş Kuralı Hatası Fırlatıldı: {}", ex.getMessage());

        // Kullanıcıya döneceğimiz JSON kutusunu hazırlıyoruz
        Map<String, Object> errorResponse = new HashMap<>();

        // Jackson patlamasın diye zamanı direkt String (düz metin) yapıp geçiyoruz
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("status", ex.getStatus().value());
        errorResponse.put("message", ex.getMessage());

        return new ResponseEntity<>(errorResponse, ex.getStatus());
    }

    // 2. (Bonus) NullPointerException gibi senin öngöremediğin sürpriz sistem hatalarını yakalar
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex) {

        // Orijinal hatayı (ex) parametre veriyoruz ki çökme sebebi konsolda tam görünsün
        logger.error("Beklenmeyen Sistem Hatası: {}", ex.getMessage(), ex);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value()); // 500
        errorResponse.put("message", "Sistemde beklenmeyen bir hata oluştu. Lütfen daha sonra tekrar deneyin.");

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}