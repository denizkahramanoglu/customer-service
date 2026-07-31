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
import com.example.customer_service.util.ExceptionUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerCardService {

    private final CustomerRepository customerRepository;
    private final CustomerCardRepository customerCardRepository;
    private final CustomerCardMapper customerCardMapper;
    private final Clock clock;

    // --- KART EKLEME (POST) ---
    @Transactional
    public void addCardToCustomer(@Valid CreateCustomerCardRequestDTO request) {

        CustomerEntity customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new BusinessException("Müşteri bulunamadı. ID: " + request.getCustomerId(), HttpStatus.NOT_FOUND));

        Integer expireMonth = Integer.valueOf(request.getExpireMonth());
        Integer expireYear = Integer.valueOf(request.getExpireYear());
        CreditCardValidationUtil.validateCreditCard(request.getCardNumber(), expireMonth, expireYear, clock);

        // 4. Yeni kart Entity'sini oluştur
        CustomerCardEntity newCard = CustomerCardEntity.builder()
                .customer(customer)
                .cardAlias(request.getCardAlias() != null ? request.getCardAlias() : "Yeni Kart")
                .cardNumber(request.getCardNumber())
                .expireMonth(expireMonth)
                .expireYear(expireYear)
                .build();

        // 5. Veritabanına kaydet
        customerCardRepository.save(newCard);
    }

    // --- KARTLARI GETİRME (GET) ---
    public List<CustomerCardResponseDTO> getCustomerCards(Long customerId) {

        // 1. Müşteri gerçekten var mı kontrolü
        ExceptionUtil.businessExceptionCheckerAndThrowException(
                !customerRepository.existsById(customerId),
                "Müşteri bulunamadı. ID: " + customerId,
                HttpStatus.NOT_FOUND
        );

        // 2. Doğrudan kartları çek (Müşteriyi komple çekmiyoruz!)
        List<CustomerCardEntity> cards = customerCardRepository.findByCustomerId(customerId);

        // 3. Stream kullanarak elindeki Entity listesini DTO listesine dönüştür
        return cards.stream()
                .map(customerCardMapper::toResponseDTO)
                .toList();
    }
}