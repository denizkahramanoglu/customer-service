package com.example.customer_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerCardResponseDTO {

    private Long id;
    private String cardAlias;
    private String cardNumber;
    private String expireMonth;
    private  String expireYear;
}