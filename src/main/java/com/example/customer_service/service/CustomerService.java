package com.example.customer_service.service;

import java.util.logging.Logger;
import com.example.customer_service.client.ParameterClient;
import com.example.customer_service.dto.FullLocationResponseDTO;
import com.example.customer_service.exception.BusinessException;
import com.example.customer_service.mapper.CustomerMapper;
import com.example.customer_service.dto.CustomerRequestDTO;
import com.example.customer_service.dto.CustomerResponseDTO;
import com.example.customer_service.entity.CustomerEntity;
import com.example.customer_service.repository.CustomerRepository;
import com.example.customer_service.util.PhoneNumberValidator;
import com.example.customer_service.util.TcknValidator;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final ParameterClient parameterClient;

    @Transactional
    public CustomerResponseDTO createCustomer(CustomerRequestDTO requestDTO) {

        if (!TcknValidator.isValid(requestDTO.getIdentityNumber())) {
            throw new BusinessException("Geçersiz TC Kimlik Numarası!", HttpStatus.BAD_REQUEST);
        }

        String validPhoneNumber = PhoneNumberValidator.cleanAndValidate(requestDTO.getPhoneNumber());

        // Zaten var olan kayıtlar için 409 Conflict dönmek en doğru mimari karardır
        if (customerRepository.existsByIdentityNumber(requestDTO.getIdentityNumber())) {
            throw new BusinessException("Bu TC Kimlik Numarası sistemde zaten kayıtlı!", HttpStatus.CONFLICT);
        }

        if (customerRepository.existsByPhoneNumber(validPhoneNumber)) {
            throw new BusinessException("Bu telefon numarası sistemde zaten kayıtlı!", HttpStatus.CONFLICT);
        }

        int age = Period.between(requestDTO.getDateOfBirth(), LocalDate.now()).getYears();
        if (age < 18) {
            throw new BusinessException("18 yaşından küçükler sisteme müşteri olarak eklenemez!", HttpStatus.BAD_REQUEST);
        }

        CustomerEntity entity = customerMapper.toEntity(requestDTO);
        CustomerEntity savedEntity = customerRepository.save(entity);

        return mapToResponseDTO(savedEntity);
    }

    public List<CustomerResponseDTO> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    public CustomerResponseDTO getCustomerById(Long id) {
        CustomerEntity entity = customerRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Müşteri bulunamadı! Geçersiz ID: " + id,
                        HttpStatus.NOT_FOUND
                ));
        return mapToResponseDTO(entity);
    }

    private CustomerResponseDTO mapToResponseDTO(CustomerEntity entity) {
        CustomerResponseDTO dto = customerMapper.toResponseDTO(entity);

        try {
            // Feign Client otomatik olarak JSON'u DTO'ya dönüştürüyor
            FullLocationResponseDTO location = parameterClient.getFullLocation(entity.getDistrictId());
            dto.setAddress(location);

        } catch (FeignException.NotFound e) {
            log.warn("Parametre servisinde bölge bulunamadı. İlçe ID: {}", entity.getDistrictId());

        } catch (FeignException e) {
            log.error("Parametre servisine ulaşılamadı: {}", e.getMessage());
        }

        return dto;
    }
}