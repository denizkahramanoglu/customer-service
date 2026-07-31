package com.example.customer_service.mapper;

import com.example.customer_service.dto.CustomerCardResponseDTO;
import com.example.customer_service.entity.CustomerCardEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CustomerCardMapper {

    // CustomerCardEntity içindeki alanları isim benzerliğine göre DTO'ya eşler
    CustomerCardResponseDTO toResponseDTO(CustomerCardEntity entity);

}