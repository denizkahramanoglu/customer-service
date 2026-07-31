package com.example.customer_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import java.time.LocalDate;

@Entity
@Table(name = "customers")
@SQLDelete(sql = "UPDATE customers SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted = false")
@Data
public class CustomerEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 11)
    private String identityNumber;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String gender;

    @Column(name = "district_id", nullable = false)
    private Long districtId;

    @Column(name = "place_of_birth_city_id")
    private Long placeOfBirthCityId;

    @Column(nullable = false, unique = true, length = 15)
    private String phoneNumber;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;
}


