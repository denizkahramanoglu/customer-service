package com.example.customer_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "customers")
@Data
public class CustomerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 11)
    private String identityNumber;

    @Column(nullable = false)
    private String firstName; // Ad

    @Column(nullable = false)
    private String lastName; // Soyad

    @Column(length = 500)
    private String address;

    private String placeOfBirth; // Doğum Yeri

    private LocalDate dateOfBirth;

    @Column(unique = true, length = 15)
    private String phoneNumber;
}