package com.example.customer_service.service;

import com.example.customer_service.mapper.CustomerMapper;
import com.example.customer_service.dto.CustomerRequestDTO;
import com.example.customer_service.dto.CustomerResponseDTO;
import com.example.customer_service.entity.CustomerEntity;
import com.example.customer_service.repository.CustomerRepository;
import com.example.customer_service.util.PhoneNumberValidator;
import com.example.customer_service.util.TcknValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public CustomerResponseDTO createCustomer(CustomerRequestDTO requestDTO) {

        if (!TcknValidator.isValid(requestDTO.getIdentityNumber())) {
            throw new IllegalArgumentException("Geçersiz TC Kimlik Numarası!");
        }
        String validPhoneNumber = PhoneNumberValidator.cleanAndValidate(requestDTO.getPhoneNumber());
        if (customerRepository.existsByIdentityNumber(requestDTO.getIdentityNumber())) {
            throw new IllegalArgumentException("Bu TC Kimlik Numarası sistemde zaten kayıtlı!");
        }
        if (customerRepository.existsByPhoneNumber(validPhoneNumber)) {
            throw new IllegalArgumentException("Bu telefon numarası sistemde zaten kayıtlı!");
        }
        int age = Period.between(requestDTO.getDateOfBirth(), LocalDate.now()).getYears();
        if (age < 18) {
            throw new IllegalArgumentException("18 yaşından küçükler sisteme müşteri olarak eklenemez!");
        }

        requestDTO.setFirstName(requestDTO.getFirstName().trim());

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