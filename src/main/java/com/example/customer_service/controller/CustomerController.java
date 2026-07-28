package com.example.customer_service.controller;

import com.example.customer_service.dto.CustomerRequestDTO;
import com.example.customer_service.dto.CustomerResponseDTO;
import com.example.customer_service.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @Operation(summary = "Müşteri ekleme")
    @PostMapping
    public CustomerResponseDTO addCustomer(@RequestBody CustomerRequestDTO requestDTO) {
        return customerService.createCustomer(requestDTO);
    }
    @Operation(summary = "Bütün müşterileri getirme")
    @GetMapping
    public List<CustomerResponseDTO> getAllCustomers() {
        return customerService.getAllCustomers();
    }

    @Operation(summary = "Müşteri İD si ile müşteri getirme")
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> getCustomer(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }
    @Operation(summary = "Müşteri güncelleme")
    @PutMapping("/{id}")
    public CustomerResponseDTO updateCustomer(@PathVariable Long id, @RequestBody CustomerRequestDTO requestDTO) {
        return customerService.updateCustomer(id, requestDTO);
    }
    @Operation(summary = "Müşteri silme")
    @DeleteMapping("/{id}")
    public void deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
    }


}