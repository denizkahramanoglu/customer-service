package com.example.customer_service.client;

import com.example.customer_service.dto.FullLocationResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "parameter-client", url = "http://localhost:8083") // Parametre servisinin portu
public interface ParameterClient {
    @GetMapping("/api/parameters/locations/full-address/{districtId}")
        FullLocationResponseDTO getFullLocation(@PathVariable("districtId") Long districtId);

}