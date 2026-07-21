package com.example.customer_service.mapper;

import com.example.customer_service.dto.CustomerRequestDTO;
import com.example.customer_service.dto.CustomerResponseDTO;
import com.example.customer_service.entity.CustomerEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "firstName", source = "firstName", qualifiedByName = "trimString")
    @Mapping(target = "lastName", source = "lastName", qualifiedByName = "trimString")
    @Mapping(target = "districtId", source = "districtId")

    CustomerEntity toEntity(CustomerRequestDTO dto);

    @Mapping(target = "address", ignore = true)
    CustomerResponseDTO toResponseDTO(CustomerEntity entity);


    @Named("trimString")
    default String trimString(String value) {
        if (value == null) {
            return null;
        }
        return value.trim();
    }
}