package com.example.customer_service.service;

import com.example.customer_service.dto.CreateCustomerCardRequestDTO;
import com.example.customer_service.dto.CustomerCardResponseDTO;
import com.example.customer_service.entity.CustomerCardEntity;
import com.example.customer_service.entity.CustomerEntity;
import com.example.customer_service.exception.BusinessException;
import com.example.customer_service.mapper.CustomerCardMapper;
import com.example.customer_service.repository.CustomerCardRepository;
import com.example.customer_service.repository.CustomerRepository;
import com.example.customer_service.util.CreditCardValidationUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerCardServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerCardRepository customerCardRepository;

    @Mock
    private CustomerCardMapper customerCardMapper;

    @Mock
    private Clock clock;

    @InjectMocks
    private CustomerCardService customerCardService;

    @Captor
    private ArgumentCaptor<CustomerCardEntity> cardEntityCaptor;

    // --- addCardToCustomer TESTLERİ ---

    @Test
    void addCardToCustomer_CustomerNotFound_ThrowsException() {
        CreateCustomerCardRequestDTO request = new CreateCustomerCardRequestDTO();
        request.setCustomerId(1L);

        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> customerCardService.addCardToCustomer(request));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Müşteri bulunamadı. ID: 1", exception.getMessage());
    }

    @Test
    void addCardToCustomer_ValidationFails_ThrowsException() {
        CreateCustomerCardRequestDTO request = new CreateCustomerCardRequestDTO();
        request.setCustomerId(1L);
        request.setCardNumber("1234");
        request.setExpireMonth("13"); // Hatalı ay örneği
        request.setExpireYear("2020"); // Geçmiş yıl örneği

        CustomerEntity customer = new CustomerEntity();
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        try (MockedStatic<CreditCardValidationUtil> validationMock = mockStatic(CreditCardValidationUtil.class)) {

            // Validasyon metodunun BusinessException fırlattığını simüle ediyoruz
            validationMock.when(() -> CreditCardValidationUtil.validateCreditCard("1234", 13, 2020, clock))
                    .thenThrow(new BusinessException("Geçersiz kredi kartı", HttpStatus.BAD_REQUEST));

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> customerCardService.addCardToCustomer(request));

            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
            assertEquals("Geçersiz kredi kartı", exception.getMessage());
        }
    }

    @Test
    void addCardToCustomer_Success_WithDefaultAlias() {
        // Alias null gönderildiğinde "Yeni Kart" atanıp atanmadığını test ediyoruz
        CreateCustomerCardRequestDTO request = new CreateCustomerCardRequestDTO();
        request.setCustomerId(1L);
        request.setCardNumber("1111222233334444");
        request.setExpireMonth("12");
        request.setExpireYear("2030");
        request.setCardAlias(null);

        CustomerEntity customer = new CustomerEntity();
        customer.setId(1L);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        try (MockedStatic<CreditCardValidationUtil> validationMock = mockStatic(CreditCardValidationUtil.class)) {

            // Validasyonda bir sorun olmadığını (void dönmesini) simüle ediyoruz
            validationMock.when(() -> CreditCardValidationUtil.validateCreditCard(anyString(), anyInt(), anyInt(), any(Clock.class)))
                    .thenAnswer(invocation -> null);

            customerCardService.addCardToCustomer(request);

            verify(customerCardRepository, times(1)).save(cardEntityCaptor.capture());

            CustomerCardEntity savedCard = cardEntityCaptor.getValue();
            assertEquals("Yeni Kart", savedCard.getCardAlias()); // Default değer atanmış mı?
            assertEquals("1111222233334444", savedCard.getCardNumber());
            assertEquals(12, savedCard.getExpireMonth());
            assertEquals(2030, savedCard.getExpireYear());
            assertEquals(customer, savedCard.getCustomer());
        }
    }

    @Test
    void addCardToCustomer_Success_WithProvidedAlias() {
        // Kullanıcının özel bir Alias verdiği senaryo
        CreateCustomerCardRequestDTO request = new CreateCustomerCardRequestDTO();
        request.setCustomerId(1L);
        request.setCardNumber("1111222233334444");
        request.setExpireMonth("10");
        request.setExpireYear("2026");
        request.setCardAlias("Maaş Kartım");

        CustomerEntity customer = new CustomerEntity();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        try (MockedStatic<CreditCardValidationUtil> validationMock = mockStatic(CreditCardValidationUtil.class)) {

            validationMock.when(() -> CreditCardValidationUtil.validateCreditCard(anyString(), anyInt(), anyInt(), any(Clock.class)))
                    .thenAnswer(invocation -> null);

            customerCardService.addCardToCustomer(request);

            verify(customerCardRepository, times(1)).save(cardEntityCaptor.capture());

            CustomerCardEntity savedCard = cardEntityCaptor.getValue();
            assertEquals("Maaş Kartım", savedCard.getCardAlias()); // Kullanıcının girdiği alias gelmiş mi?
            assertEquals("1111222233334444", savedCard.getCardNumber());
        }
    }

    // --- getCustomerCards TESTLERİ ---

    @Test
    void getCustomerCards_CustomerNotFound_ThrowsException() {
        when(customerRepository.existsById(1L)).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> customerCardService.getCustomerCards(1L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Müşteri bulunamadı. ID: 1", exception.getMessage());
    }

    @Test
    void getCustomerCards_Success() {
        Long customerId = 1L;

        CustomerCardEntity cardEntity = new CustomerCardEntity();
        cardEntity.setId(10L);

        CustomerCardResponseDTO cardDTO = new CustomerCardResponseDTO();
        cardDTO.setId(10L);

        when(customerRepository.existsById(customerId)).thenReturn(true);
        when(customerCardRepository.findByCustomerId(customerId)).thenReturn(List.of(cardEntity));
        when(customerCardMapper.toResponseDTO(cardEntity)).thenReturn(cardDTO);

        List<CustomerCardResponseDTO> result = customerCardService.getCustomerCards(customerId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(10L, result.getFirst().getId());

        verify(customerRepository, times(1)).existsById(customerId);
        verify(customerCardRepository, times(1)).findByCustomerId(customerId);
        verify(customerCardMapper, times(1)).toResponseDTO(cardEntity);
    }
}