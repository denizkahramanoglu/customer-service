package com.example.customer_service.service;


import com.example.customer_service.client.ParameterClient;
import com.example.customer_service.dto.FullLocationResponseDTO;
import com.example.customer_service.entity.CustomerCardEntity;
import com.example.customer_service.exception.BusinessException;
import com.example.customer_service.mapper.CustomerCardMapper;
import com.example.customer_service.mapper.CustomerMapper;
import com.example.customer_service.dto.CustomerRequestDTO;
import com.example.customer_service.dto.CustomerResponseDTO;
import com.example.customer_service.entity.CustomerEntity;
import com.example.customer_service.repository.CustomerCardRepository;
import com.example.customer_service.repository.CustomerRepository;
import com.example.customer_service.util.ExceptionUtil;
import com.example.customer_service.util.PhoneNumberValidator;
import com.example.customer_service.util.TcknValidator;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final ParameterClient parameterClient;
    private final CustomerCardRepository customerCardRepository;
    private final CustomerCardMapper customerCardMapper;

    @Transactional
    public CustomerResponseDTO createCustomer(CustomerRequestDTO requestDTO) {

        ExceptionUtil.businessExceptionCheckerAndThrowException(!TcknValidator.isValid(requestDTO.getIdentityNumber()), "Geçersiz TC Kimlik Numarası!", HttpStatus.BAD_REQUEST);
        String validPhoneNumber = PhoneNumberValidator.cleanAndValidate(requestDTO.getPhoneNumber());
        ExceptionUtil.businessExceptionCheckerAndThrowException(customerRepository.existsByIdentityNumber(requestDTO.getIdentityNumber()), "Bu TC Kimlik Numarası sistemde zaten kayıtlı!", HttpStatus.CONFLICT);
        ExceptionUtil.businessExceptionCheckerAndThrowException(customerRepository.existsByPhoneNumber(validPhoneNumber), "Bu telefon numarası sistemde zaten kayıtlı!", HttpStatus.CONFLICT);
        CustomerEntity entity = customerMapper.toEntity(requestDTO);
        CustomerEntity savedEntity = customerRepository.save(entity);

        return mapToResponseDTO(savedEntity);
    }

    public List<CustomerResponseDTO> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public CustomerResponseDTO getCustomerById(Long id) {

        CustomerEntity entity = customerRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Müşteri bulunamadı! Geçersiz ID: " + id, HttpStatus.NOT_FOUND));

        return mapToResponseDTO(entity);
    }

    private CustomerResponseDTO mapToResponseDTO(CustomerEntity entity) {
        // 1. Temel bilgileri çevir
        CustomerResponseDTO dto = customerMapper.toResponseDTO(entity);

        // 2. KARTLARI DOLDUR
        List<CustomerCardEntity> cards = customerCardRepository.findByCustomerId(entity.getId());
        dto.setCards(cards.stream().map(customerCardMapper::toResponseDTO).toList());

        // 3. ADRESİ DOLDUR (Senin yazdığın Feign Client mantığı)
        try {
            FullLocationResponseDTO location = parameterClient.getFullLocation(entity.getDistrictId());
            dto.setAddress(location);
        } catch (FeignException.NotFound e) {
            log.warn("Parametre servisinde bölge bulunamadı. İlçe ID: {}", entity.getDistrictId());
        } catch (FeignException e) {
            log.error("Parametre servisine ulaşılamadı: {}", e.getMessage());
        }

        return dto;
    }

    public CustomerResponseDTO updateCustomer(Long id, CustomerRequestDTO requestDTO) {
        CustomerEntity existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Müşteri bulunamadı. ID: " + id, HttpStatus.NOT_FOUND));

        existingCustomer.setFirstName(requestDTO.getFirstName());
        existingCustomer.setLastName(requestDTO.getLastName());
        existingCustomer.setPhoneNumber(requestDTO.getPhoneNumber());
        existingCustomer.setEmail(requestDTO.getEmail());
        CustomerEntity updatedCustomer = customerRepository.save(existingCustomer);
        return customerMapper.toResponseDTO(updatedCustomer);
    }

    @Transactional
    public void deleteCustomer(Long id) {

        CustomerEntity existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Müşteri bulunamadı. ID: " + id, HttpStatus.NOT_FOUND));

        ExceptionUtil.businessExceptionCheckerAndThrowException(existingCustomer.isDeleted(), "Bu müşteri zaten sistemden silinmiş.", HttpStatus.BAD_REQUEST);
        existingCustomer.setDeleted(true);
        customerRepository.save(existingCustomer);
    }
}