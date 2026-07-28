package com.example.customer_service.exception;

import org.springframework.http.HttpStatus;

public class BusinessException extends RuntimeException {

    private final HttpStatus status;

    // Sadece mesaj gönderilirse varsayılan olarak 400 Bad Request döner
    public BusinessException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }

    // Hem mesaj hem de statü kodu gönderilirse senin verdiğin kodu döner
    public BusinessException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    // Eğer önceki hatayı (Exception) da zincire eklemek istersen (SonarQube için)
    public BusinessException(String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}