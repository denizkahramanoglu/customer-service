package com.example.customer_service.controller;

import com.example.customer_service.entity.CustomerEntity;
import com.example.customer_service.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public CustomerEntity addCustomer(@RequestBody CustomerEntity customerEntity) {
        return customerService.createCustomer(customerEntity);
    }
    @GetMapping
    public List<CustomerEntity> getCustomers() {
        return customerService.getAllCustomers();
    }
    @GetMapping("/{identityNumber}")
    public CustomerEntity getCustomerByIdentityNumber(@PathVariable String identityNumber) {
        return customerService.getCustomerByIdentityNumber(identityNumber);
    }
}