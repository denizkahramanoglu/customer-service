package com.example.customer_service.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCustomerCardRequestDTO {

    @NotNull(message = "Müşteri ID boş bırakılamaz")
    private Long customerId;

    // Kullanıcı kartına bir isim vermek isteyebilir (Örn: "Maaş Kartım"). Zorunlu değil.
    @Size(max = 50, message = "Kart adı en fazla 50 karakter olabilir")
    private String cardAlias;

    // Sadece rakamlardan oluşan tam 16 haneli bir değer bekliyoruz
    @NotBlank(message = "Kart numarası boş bırakılamaz")
    private String cardNumber;

    @NotBlank(message = "Ay bilgisi boş bırakılamaz")
    private String expireMonth;

    // 2026 ve sonrası yılları kabul eden Regex (2026-2099 arası)
    @NotBlank(message = "Yıl bilgisi boş bırakılamaz")
    private String expireYear;
}