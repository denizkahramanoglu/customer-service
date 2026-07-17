package com.example.customer_service.service;

import java.util.logging.Logger;
import com.example.customer_service.client.ParameterClient;
import com.example.customer_service.dto.FullLocationResponseDTO;
import com.example.customer_service.mapper.CustomerMapper;
import com.example.customer_service.dto.CustomerRequestDTO;
import com.example.customer_service.dto.CustomerResponseDTO;
import com.example.customer_service.entity.CustomerEntity;
import com.example.customer_service.repository.CustomerRepository;
import com.example.customer_service.util.PhoneNumberValidator;
import com.example.customer_service.util.TcknValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final ParameterClient parameterClient;

    @Transactional
    public CustomerResponseDTO createCustomer(CustomerRequestDTO requestDTO) {
        // İş Kuralları
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

        CustomerEntity entity = customerMapper.toEntity(requestDTO);
        CustomerEntity savedEntity = customerRepository.save(entity);

        // Kaydedilen veriyi zenginleştirerek dönüyoruz
        return mapToResponseDTO(savedEntity);
    }

    public List<CustomerResponseDTO> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    public CustomerResponseDTO getCustomerById(Long id) {
        CustomerEntity entity = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Müşteri bulunamadı!"));
        return mapToResponseDTO(entity);
    }

    // Yardımcı metot: Feign Client ile veriyi birleştirir
    private CustomerResponseDTO mapToResponseDTO(CustomerEntity entity) {
        CustomerResponseDTO dto = customerMapper.toResponseDTO(entity);

        // Feign Client üzerinden ilçe bilgisini çek
        try {
            FullLocationResponseDTO location = parameterClient.getFullLocation(entity.getDistrictId());
            dto.setAddress(location);
        } catch (Exception e) {
            Logger.getLogger(CustomerService.class.getName()).severe("Parametre servisine ulaşılamadı: " + e.getMessage());
        }

        return dto;
    }
}