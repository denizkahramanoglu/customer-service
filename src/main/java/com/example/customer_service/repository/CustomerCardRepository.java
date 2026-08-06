package com.example.customer_service.repository;

import com.example.customer_service.entity.CustomerCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerCardRepository extends JpaRepository<CustomerCardEntity, Long> {

    List<CustomerCardEntity> findAllByCustomerIdAndIsDeletedFalse(Long customerId);
    List<CustomerCardEntity> findByCustomerId(Long customerId);
}