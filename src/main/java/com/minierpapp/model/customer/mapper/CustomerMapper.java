package com.minierpapp.model.customer.mapper;

import com.minierpapp.model.base.BaseMapper;
import com.minierpapp.model.customer.Customer;
import com.minierpapp.model.customer.dto.CustomerDto;
import com.minierpapp.model.customer.dto.CustomerRequest;
import com.minierpapp.model.customer.dto.CustomerResponse;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface CustomerMapper extends BaseMapper<Customer, CustomerDto, CustomerRequest, CustomerResponse> {
    @Override
    Customer toEntity(CustomerDto dto);

    @Override
    CustomerDto toDto(Customer entity);

    @Override
    Customer requestToEntity(CustomerRequest request);

    @Override
    CustomerResponse entityToResponse(Customer entity);

    @Override
    void updateEntityFromRequest(CustomerRequest request, @MappingTarget Customer entity);

    @Override
    void updateEntity(CustomerDto dto, @MappingTarget Customer entity);

    @Override
    CustomerRequest responseToRequest(CustomerResponse response);

    default List<CustomerResponse> toResponseList(List<Customer> entities) {
        return entities.stream()
            .map(this::entityToResponse)
            .collect(Collectors.toList());
    }

    default List<CustomerDto> toDtoList(List<Customer> entities) {
        return entities.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }
}