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

/**
 * Müşterilerin kredi kartı ekleme ve kayıtlı kartlarını listeleme
 * işlemlerinden sorumlu servis sınıfı.
 *
 * @author deniz
 */
@Service
@RequiredArgsConstructor
public class CustomerCardService {

    private final CustomerRepository customerRepository;
    private final CustomerCardRepository customerCardRepository;
    private final CustomerCardMapper customerCardMapper;
    private final Clock clock;

    /**
     * Müşteriye yeni bir kredi kartı ekler.
     * Eklenmeden önce kart numarasının formatı ve son kullanma tarihinin geçerliliği kontrol edilir.
     *
     * @param request Eklenecek kart bilgilerini ve müşteri ID'sini içeren {@link CreateCustomerCardRequestDTO} nesnesi
     * @throws BusinessException Verilen ID ile eşleşen bir müşteri bulunamazsa veya kart bilgileri geçersizse fırlatılır
     */
    @Transactional
    public void addCardToCustomer(@Valid CreateCustomerCardRequestDTO request) {

        CustomerEntity customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new BusinessException("Müşteri bulunamadı. ID: " + request.getCustomerId(), HttpStatus.NOT_FOUND));

        Integer expireMonth = Integer.valueOf(request.getExpireMonth());
        Integer expireYear = Integer.valueOf(request.getExpireYear());
        CreditCardValidationUtil.validateCreditCard(request.getCardNumber(), expireMonth, expireYear, clock);
        CustomerCardEntity newCard = CustomerCardEntity.builder()
                .customer(customer)
                .cardAlias(request.getCardAlias() != null ? request.getCardAlias() : "Yeni Kart")
                .cardNumber(request.getCardNumber())
                .expireMonth(expireMonth)
                .expireYear(expireYear)
                .build();

        customerCardRepository.save(newCard);
    }

    /**
     * Belirli bir müşteriye ait sisteme kaydedilmiş tüm kredi kartlarını getirir.
     *
     * @param customerId Kartları getirilecek müşterinin benzersiz ID'si
     * @return Müşteriye ait kredi kartlarının detaylarını içeren {@link CustomerCardResponseDTO} nesnelerinin listesi
     * @throws BusinessException Müşteri sistemde (veritabanında) bulunamazsa fırlatılır
     */
    public List<CustomerCardResponseDTO> getCustomerCards(Long customerId) {

        ExceptionUtil.businessExceptionCheckerAndThrowException(
                !customerRepository.existsById(customerId),
                "Müşteri bulunamadı. ID: " + customerId,
                HttpStatus.NOT_FOUND
        );
        List<CustomerCardEntity> cards = customerCardRepository.findByCustomerId(customerId);

        return cards.stream()
                .map(customerCardMapper::toResponseDTO)
                .toList();
    }
}