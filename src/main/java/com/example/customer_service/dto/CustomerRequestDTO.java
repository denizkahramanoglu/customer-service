package com.example.customer_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRequestDTO {
    private String identityNumber;
    private String firstName;
    private String lastName;
    private Long districtId;
    private Long placeOfBirthCityId;
    private LocalDate dateOfBirth;
    private String phoneNumber;
}