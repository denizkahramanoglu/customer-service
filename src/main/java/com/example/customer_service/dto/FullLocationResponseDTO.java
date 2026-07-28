package com.example.customer_service.dto;

import lombok.Data;

@Data
public class FullLocationResponseDTO {
    private String countryName;
    private String cityName;
    private String districtName;
}
