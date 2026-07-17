package com.example.customer_service.mapper;

import com.example.customer_service.dto.CustomerRequestDTO;
import com.example.customer_service.dto.CustomerResponseDTO;
import com.example.customer_service.entity.CustomerEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    @Mapping(target = "firstName", source = "firstName", qualifiedByName = "trimString")
    @Mapping(target = "lastName", source = "lastName", qualifiedByName = "trimString")
    @Mapping(target = "districtId", source = "districtId")


    CustomerEntity toEntity(CustomerRequestDTO dto);
    CustomerResponseDTO toResponseDTO(CustomerEntity entity);

    @Named("trimString")
    default String trimString(String value) {
        if (value == null) {
            return null;
        }
        return value.trim();
    }
}