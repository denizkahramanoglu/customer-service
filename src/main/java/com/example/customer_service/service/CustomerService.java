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

/**
 * Müşteri oluşturma, okuma, güncelleme ve (mantıksal) silme gibi temel CRUD işlemlerini yöneten servis sınıfı.
 * Ayrıca müşteri detayları getirilirken kart bilgileri ve dış servisten adres hiyerarşisi (İl, İlçe vb.) ile zenginleştirme yapar.
 *
 * @author deniz
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final ParameterClient parameterClient;
    private final CustomerCardRepository customerCardRepository;
    private final CustomerCardMapper customerCardMapper;

    /**
     * Sisteme yeni bir müşteri kaydeder.
     * Kayıt öncesi TC Kimlik No ve Telefon numarası doğrulama (format ve benzersizlik) işlemlerinden geçirilir.
     *
     * @param requestDTO Oluşturulacak müşterinin detaylarını içeren {@link CustomerRequestDTO} nesnesi
     * @return Kaydedilen müşterinin veritabanındaki karşılığını içeren {@link CustomerResponseDTO} nesnesi
     * @throws BusinessException TCKN/Telefon geçerli değilse veya sistemde zaten kayıtlıysa fırlatılır
     */
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

    /**
     * Sistemdeki tüm müşterileri getirir.
     *
     * @return Zenginleştirilmiş müşteri detaylarını barındıran {@link CustomerResponseDTO} nesnelerinin listesi
     */
    public List<CustomerResponseDTO> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    /**
     * Belirtilen ID'ye sahip müşteriyi bulur ve adres, kart bilgileri ile zenginleştirerek döner.
     *
     * @param id Aranacak müşterinin benzersiz ID'si
     * @return Müşterinin tüm detaylarını içeren {@link CustomerResponseDTO} nesnesi
     * @throws BusinessException Belirtilen ID ile eşleşen bir müşteri bulunamazsa fırlatılır
     */
    @Transactional(readOnly = true)
    public CustomerResponseDTO getCustomerById(Long id) {

        CustomerEntity entity = customerRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Müşteri bulunamadı! Geçersiz ID: " + id, HttpStatus.NOT_FOUND));

        return mapToResponseDTO(entity);
    }

    /**
     * Müşteri entity sınıfını DTO'ya çevirirken ekstra bilgileri (kredi kartları ve Parameter servisinden gelen adres) doldurur.
     *
     * @param entity Veritabanından çekilmiş müşteri nesnesi
     * @return İlgili detaylarla zenginleştirilmiş {@link CustomerResponseDTO} nesnesi
     */
    private CustomerResponseDTO mapToResponseDTO(CustomerEntity entity) {

        CustomerResponseDTO dto = customerMapper.toResponseDTO(entity);
        List<CustomerCardEntity> cards = customerCardRepository.findByCustomerId(entity.getId());
        dto.setCards(cards.stream().map(customerCardMapper::toResponseDTO).toList());

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

    /**
     * Mevcut bir müşterinin temel bilgilerini (ad, soyad, e-posta, telefon vb.) günceller.
     *
     * @param id Güncellenecek müşterinin benzersiz ID'si
     * @param requestDTO Müşteriye ait yeni bilgileri içeren {@link CustomerRequestDTO} nesnesi
     * @return Güncellenmiş müşteri detaylarını barındıran {@link CustomerResponseDTO} nesnesi
     * @throws BusinessException Belirtilen ID ile eşleşen bir müşteri bulunamazsa fırlatılır
     */
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

    /**
     * Belirtilen ID'ye sahip müşteriyi mantıksal olarak (soft delete) siler.
     *
     * @param id Silinecek müşterinin benzersiz ID'si
     * @throws BusinessException Müşteri bulunamazsa veya zaten daha önceden silinmiş durumdaysa fırlatılır
     */
    @Transactional
    public void deleteCustomer(Long id) {

        CustomerEntity existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Müşteri bulunamadı. ID: " + id, HttpStatus.NOT_FOUND));

        ExceptionUtil.businessExceptionCheckerAndThrowException(existingCustomer.isDeleted(), "Bu müşteri zaten sistemden silinmiş.", HttpStatus.BAD_REQUEST);
        existingCustomer.setDeleted(true);
        customerRepository.save(existingCustomer);
    }
}