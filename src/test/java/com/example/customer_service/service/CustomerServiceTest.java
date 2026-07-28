package com.example.customer_service.service;


import com.example.customer_service.client.ParameterClient;
import com.example.customer_service.dto.CustomerRequestDTO;
import com.example.customer_service.dto.CustomerResponseDTO;
import com.example.customer_service.dto.FullLocationResponseDTO;
import com.example.customer_service.entity.CustomerEntity;
import com.example.customer_service.exception.BusinessException;
import com.example.customer_service.mapper.CustomerMapper;
import com.example.customer_service.repository.CustomerRepository;
import com.example.customer_service.util.PhoneNumberValidator;
import com.example.customer_service.util.TcknValidator;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @Mock
    private ParameterClient parameterClient;

    @InjectMocks
    private CustomerService customerService;

    // --- createCustomer TESTLERİ ---

    @Test
    void createCustomer_InvalidTckn_ThrowsException() {
        CustomerRequestDTO request = new CustomerRequestDTO();
        request.setIdentityNumber("123"); // Geçersiz TCKN simülasyonu

        try (MockedStatic<TcknValidator> tcknMock = mockStatic(TcknValidator.class)) {
            tcknMock.when(() -> TcknValidator.isValid(anyString())).thenReturn(false);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> customerService.createCustomer(request));

            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
            assertEquals("Geçersiz TC Kimlik Numarası!", exception.getMessage());
        }
    }

    @Test
    void createCustomer_TcknAlreadyExists_ThrowsException() {
        CustomerRequestDTO request = new CustomerRequestDTO();
        request.setIdentityNumber("11111111110");
        request.setPhoneNumber("5551234567");

        try (MockedStatic<TcknValidator> tcknMock = mockStatic(TcknValidator.class);
             MockedStatic<PhoneNumberValidator> phoneMock = mockStatic(PhoneNumberValidator.class)) {

            tcknMock.when(() -> TcknValidator.isValid(anyString())).thenReturn(true);
            phoneMock.when(() -> PhoneNumberValidator.cleanAndValidate(anyString())).thenReturn("5551234567");

            when(customerRepository.existsByIdentityNumber("11111111110")).thenReturn(true);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> customerService.createCustomer(request));

            assertEquals(HttpStatus.CONFLICT, exception.getStatus());
            assertEquals("Bu TC Kimlik Numarası sistemde zaten kayıtlı!", exception.getMessage());
        }
    }

    @Test
    void createCustomer_PhoneAlreadyExists_ThrowsException() {
        CustomerRequestDTO request = new CustomerRequestDTO();
        request.setIdentityNumber("11111111110");
        request.setPhoneNumber("5551234567");

        try (MockedStatic<TcknValidator> tcknMock = mockStatic(TcknValidator.class);
             MockedStatic<PhoneNumberValidator> phoneMock = mockStatic(PhoneNumberValidator.class)) {

            tcknMock.when(() -> TcknValidator.isValid(anyString())).thenReturn(true);
            phoneMock.when(() -> PhoneNumberValidator.cleanAndValidate(anyString())).thenReturn("5551234567");

            when(customerRepository.existsByIdentityNumber(anyString())).thenReturn(false);
            when(customerRepository.existsByPhoneNumber("5551234567")).thenReturn(true);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> customerService.createCustomer(request));

            assertEquals(HttpStatus.CONFLICT, exception.getStatus());
            assertEquals("Bu telefon numarası sistemde zaten kayıtlı!", exception.getMessage());
        }
    }
    @Test
    void createCustomer_Success() {
        CustomerRequestDTO request = new CustomerRequestDTO();
        // EKSİK OLAN SATIRLAR BURASI
        request.setIdentityNumber("11111111110"); // İlk if'i geçmesi için gerekli
        request.setPhoneNumber("5551234567");     // İkinci if'i geçmesi için gerekli

        request.setDateOfBirth(LocalDate.now().minusYears(25)); // 25 Yaş (Geçerli)

        CustomerEntity entity = new CustomerEntity();
        CustomerResponseDTO responseDTO = new CustomerResponseDTO();

        try (MockedStatic<TcknValidator> tcknMock = mockStatic(TcknValidator.class);
             MockedStatic<PhoneNumberValidator> phoneMock = mockStatic(PhoneNumberValidator.class)) {

            tcknMock.when(() -> TcknValidator.isValid(anyString())).thenReturn(true);
            phoneMock.when(() -> PhoneNumberValidator.cleanAndValidate(anyString())).thenReturn("5551234567");

            when(customerRepository.existsByIdentityNumber(any())).thenReturn(false);
            when(customerRepository.existsByPhoneNumber(any())).thenReturn(false);

            when(customerMapper.toEntity(request)).thenReturn(entity);
            when(customerRepository.save(entity)).thenReturn(entity);
            when(customerMapper.toResponseDTO(entity)).thenReturn(responseDTO);

            CustomerResponseDTO result = customerService.createCustomer(request);

            assertNotNull(result);
            verify(customerRepository).save(entity);
            verify(parameterClient).getFullLocation(any());
        }
    }

    // --- getAllCustomers TESTLERİ ---

    @Test
    void getAllCustomers_Success() {
        CustomerEntity entity = new CustomerEntity();
        CustomerResponseDTO responseDTO = new CustomerResponseDTO();

        when(customerRepository.findAll()).thenReturn(List.of(entity));
        when(customerMapper.toResponseDTO(entity)).thenReturn(responseDTO);

        List<CustomerResponseDTO> result = customerService.getAllCustomers();

        assertEquals(1, result.size());
        verify(parameterClient, times(1)).getFullLocation(any());
    }

    // --- getCustomerById & mapToResponseDTO TESTLERİ ---

    @Test
    void getCustomerById_NotFound_ThrowsException() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> customerService.getCustomerById(1L));
    }

    @Test
    void getCustomerById_Success_WithLocation() {
        CustomerEntity entity = new CustomerEntity();
        entity.setDistrictId(34L);
        CustomerResponseDTO responseDTO = new CustomerResponseDTO();
        FullLocationResponseDTO location = new FullLocationResponseDTO();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(customerMapper.toResponseDTO(entity)).thenReturn(responseDTO);
        when(parameterClient.getFullLocation(34L)).thenReturn(location);

        CustomerResponseDTO result = customerService.getCustomerById(1L);

        assertEquals(location, result.getAddress());
    }

    @Test
    void getCustomerById_FeignNotFound_HandledGracefully() {
        CustomerEntity entity = new CustomerEntity();
        entity.setDistrictId(34L);
        CustomerResponseDTO responseDTO = new CustomerResponseDTO();

        FeignException.NotFound mockNotFound = mock(FeignException.NotFound.class);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(customerMapper.toResponseDTO(entity)).thenReturn(responseDTO);
        when(parameterClient.getFullLocation(34L)).thenThrow(mockNotFound); // İlk catch bloğu

        CustomerResponseDTO result = customerService.getCustomerById(1L);

        assertNull(result.getAddress()); // Hata fırlatmadı, adress null kaldı
    }

    @Test
    void getCustomerById_FeignGenericException_HandledGracefully() {
        CustomerEntity entity = new CustomerEntity();
        entity.setDistrictId(34L);
        CustomerResponseDTO responseDTO = new CustomerResponseDTO();

        FeignException mockFeignException = mock(FeignException.class);
        when(mockFeignException.getMessage()).thenReturn("Connection Refused");

        when(customerRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(customerMapper.toResponseDTO(entity)).thenReturn(responseDTO);
        when(parameterClient.getFullLocation(34L)).thenThrow(mockFeignException); // İkinci catch bloğu

        CustomerResponseDTO result = customerService.getCustomerById(1L);

        assertNull(result.getAddress()); // Hata fırlatmadı, adress null kaldı
    }

    // --- updateCustomer TESTLERİ ---

    @Test
    void updateCustomer_NotFound_ThrowsException() {
        CustomerRequestDTO request = new CustomerRequestDTO();
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> customerService.updateCustomer(1L, request));
    }

    @Test
    void updateCustomer_Success() {
        CustomerRequestDTO request = new CustomerRequestDTO();
        request.setFirstName("Ahmet");
        request.setLastName("Yılmaz");
        request.setPhoneNumber("5559998877");

        CustomerEntity existing = new CustomerEntity();
        CustomerResponseDTO responseDTO = new CustomerResponseDTO();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(customerRepository.save(existing)).thenReturn(existing);
        when(customerMapper.toResponseDTO(existing)).thenReturn(responseDTO);

        CustomerResponseDTO result = customerService.updateCustomer(1L, request);

        assertEquals("Ahmet", existing.getFirstName());
        assertEquals("Yılmaz", existing.getLastName());
        assertEquals("5559998877", existing.getPhoneNumber());
        assertNotNull(result);
    }

    // --- deleteCustomer TESTLERİ ---

    @Test
    void deleteCustomer_NotFound_ThrowsException() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> customerService.deleteCustomer(1L));
    }

    @Test
    void deleteCustomer_AlreadyDeleted_ThrowsException() {
        CustomerEntity existing = new CustomerEntity();
        existing.setDeleted(true);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(existing));

        BusinessException exception = assertThrows(BusinessException.class, () -> customerService.deleteCustomer(1L));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void deleteCustomer_Success() {
        CustomerEntity existing = new CustomerEntity();
        existing.setDeleted(false);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(existing));

        customerService.deleteCustomer(1L);

        assertTrue(existing.isDeleted());
        verify(customerRepository).save(existing);
    }
}