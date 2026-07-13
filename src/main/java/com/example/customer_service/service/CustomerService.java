package com.example.customer_service.service;

import com.example.customer_service.mapper.CustomerMapper;
import com.example.customer_service.dto.CustomerRequestDTO;
import com.example.customer_service.dto.CustomerResponseDTO;
import com.example.customer_service.entity.CustomerEntity;
import com.example.customer_service.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public CustomerResponseDTO createCustomer(CustomerRequestDTO requestDTO) {
        CustomerEntity entity = customerMapper.toEntity(requestDTO);
        CustomerEntity savedEntity = customerRepository.save(entity);
        return customerMapper.toResponseDTO(savedEntity);
    }
    public List<CustomerResponseDTO> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(customerMapper::toResponseDTO)
                .toList();
    }
}