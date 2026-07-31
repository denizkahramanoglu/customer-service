package com.example.customer_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "customer_cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE customer_cards SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted = false")
public class CustomerCardEntity extends BaseEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "card_alias", length = 50)
        private String cardAlias; // Örn: "İş Bankası Kredi Kartım"

        @Column(name = "card_number", length = 16, nullable = false)
        private String cardNumber;

        @Column(name = "expire_month", nullable = false)
        private Integer expireMonth;

        @Column(name = "expire_year", nullable = false)
        private Integer expireYear;

        // Foreign Key Bağlantısı
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "customer_id", nullable = false)
        private CustomerEntity customer;
    }