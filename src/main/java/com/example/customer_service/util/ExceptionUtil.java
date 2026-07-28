package com.example.customer_service.util;

import com.example.customer_service.exception.BusinessException;
import org.springframework.http.HttpStatus;

public final class ExceptionUtil {

    private ExceptionUtil() {
    }

    public static void businessExceptionCheckerAndThrowException(
            boolean condition,
            String exceptionMessage,
            HttpStatus httpStatus) {

        if (condition) {
            throw new BusinessException(exceptionMessage, httpStatus);
        }
    }
}