package com.example.customer_service.controller;

import com.example.customer_service.dto.CreateCustomerCardRequestDTO;
import com.example.customer_service.dto.CustomerCardResponseDTO;
import com.example.customer_service.dto.CustomerRequestDTO;
import com.example.customer_service.dto.CustomerResponseDTO;
import com.example.customer_service.service.CustomerCardService;
import com.example.customer_service.service.CustomerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {

    @Mock
    private CustomerService customerService;

    @Mock
    private CustomerCardService customerCardService;

    @InjectMocks
    private CustomerController customerController;

    @Test
    @DisplayName("addCustomer - Servis çağrılmalı ve oluşturulan müşteri nesnesi dönülmeli")
    void addCustomer_shouldReturnCreatedCustomer() {
        CustomerResponseDTO mockResponse = new CustomerResponseDTO();
        when(customerService.createCustomer(any(CustomerRequestDTO.class))).thenReturn(mockResponse);

        assertEquals(mockResponse, customerController.addCustomer(new CustomerRequestDTO()));
    }

    @Test
    @DisplayName("getAllCustomers - Servis çağrılmalı ve müşteri listesi dönülmeli")
    void getAllCustomers_shouldReturnCustomerList() {
        List<CustomerResponseDTO> mockList = List.of(new CustomerResponseDTO());
        when(customerService.getAllCustomers()).thenReturn(mockList);

        assertEquals(mockList, customerController.getAllCustomers());
    }

    @Test
    @DisplayName("getCustomer - İlgili ID ile servis çağrılmalı ve müşteri ResponseEntity içinde dönülmeli")
    void getCustomer_shouldReturnResponseEntityWithCustomer() {
        CustomerResponseDTO mockResponse = new CustomerResponseDTO();
        when(customerService.getCustomerById(1L)).thenReturn(mockResponse);

        assertEquals(ResponseEntity.ok(mockResponse), customerController.getCustomer(1L));
    }

    @Test
    @DisplayName("updateCustomer - İlgili ID ile servis çağrılmalı ve güncellenen müşteri dönülmeli")
    void updateCustomer_shouldReturnUpdatedCustomer() {
        CustomerResponseDTO mockResponse = new CustomerResponseDTO();
        when(customerService.updateCustomer(eq(1L), any(CustomerRequestDTO.class))).thenReturn(mockResponse);

        assertEquals(mockResponse, customerController.updateCustomer(1L, new CustomerRequestDTO()));
    }

    @Test
    @DisplayName("deleteCustomer - İlgili ID ile servisin silme metodu doğrudan tetiklenmeli")
    void deleteCustomer_shouldCallServiceDeleteMethod() {
        customerController.deleteCustomer(1L);

        verify(customerService).deleteCustomer(1L);
    }
    @Test
    @DisplayName("addCard - Servis çağrılmalı ve CREATED response dönülmeli")
    void addCard_shouldCallServiceAndReturnCreatedResponse() {

        CreateCustomerCardRequestDTO request = new CreateCustomerCardRequestDTO();

        ResponseEntity<String> response = customerController.addCard(request);

        verify(customerCardService).addCardToCustomer(request);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Kart başarıyla eklendi.", response.getBody());
    }

    @Test
    @DisplayName("getCustomerCards - Servis çağrılmalı ve kart listesi ResponseEntity içinde dönülmeli")
    void getCustomerCards_shouldReturnCardList() {

        List<CustomerCardResponseDTO> mockCards = List.of(new CustomerCardResponseDTO());
        when(customerCardService.getCustomerCards(1L)).thenReturn(mockCards);

        ResponseEntity<List<CustomerCardResponseDTO>> response =
                customerController.getCustomerCards(1L);

        verify(customerCardService).getCustomerCards(1L);
        assertEquals(ResponseEntity.ok(mockCards), response);
    }
}