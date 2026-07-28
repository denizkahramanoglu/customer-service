package com.example.customer_service.util;

public class TcknValidator {

    private TcknValidator() {
        throw new IllegalStateException("Utility sınıfı nesne olarak üretilemez!");
    }

        public static boolean isValid (String tckn){
            if (tckn == null || tckn.length() != 11 || !tckn.matches("\\d+")) {
                return false;
            }

            if (tckn.startsWith("0")) {
                return false;
            }

            int[] digits = new int[11];
            for (int i = 0; i < 11; i++) {
                digits[i] = Character.getNumericValue(tckn.charAt(i));
            }

            int sumOdd = digits[0] + digits[2] + digits[4] + digits[6] + digits[8];
            int sumEven = digits[1] + digits[3] + digits[5] + digits[7];

            int tenthDigit = (((sumOdd * 7) - sumEven) % 10 + 10) % 10;

            if (tenthDigit != digits[9]) {
                return false;
            }

            int sumFirstTen = 0;
            for (int i = 0; i < 10; i++) {
                sumFirstTen += digits[i];
            }

            return (sumFirstTen % 10) == digits[10];
        }
    }
