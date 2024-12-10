package com.minierpapp.model.customer.mapper;

import com.minierpapp.model.customer.Customer;
import com.minierpapp.model.customer.dto.CustomerDto;
import com.minierpapp.model.customer.dto.CustomerRequest;
import com.minierpapp.model.customer.dto.CustomerResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    CustomerDto toDto(Customer customer);
    CustomerResponse toResponse(Customer customer);
    Customer toEntity(CustomerRequest request);
    void updateEntity(CustomerRequest request, @MappingTarget Customer customer);
}