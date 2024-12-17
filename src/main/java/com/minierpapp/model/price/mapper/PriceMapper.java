package com.minierpapp.model.price.mapper;

import com.minierpapp.model.customer.Customer;
import com.minierpapp.model.item.Item;
import com.minierpapp.model.price.PriceCondition;
import com.minierpapp.model.price.PriceScale;
import com.minierpapp.model.price.dto.PriceConditionRequest;
import com.minierpapp.model.price.dto.PriceConditionResponse;
import com.minierpapp.model.price.dto.PriceScaleRequest;
import com.minierpapp.model.price.dto.PriceScaleResponse;
import com.minierpapp.model.supplier.Supplier;
import com.minierpapp.repository.CustomerRepository;
import com.minierpapp.repository.ItemRepository;
import com.minierpapp.repository.SupplierRepository;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", uses = {})
public abstract class PriceMapper {
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private SupplierRepository supplierRepository;

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "item", source = "itemId")
    @Mapping(target = "customer", source = "customerId")
    @Mapping(target = "supplier", source = "supplierId")
    @Mapping(target = "itemCode", expression = "java(request.getItemCode())")
    @Mapping(target = "customerCode", expression = "java(request.getCustomerCode())")
    @Mapping(target = "supplierCode", expression = "java(request.getSupplierCode())")
    @Mapping(target = "priceScales", ignore = true)
    public abstract PriceCondition toEntity(PriceConditionRequest request);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "itemId", source = "item.id")
    @Mapping(target = "itemCode", source = "item.itemCode")
    @Mapping(target = "itemName", source = "item.itemName")
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerCode", source = "customer.customerCode")
    @Mapping(target = "customerName", source = "customer.name")
    @Mapping(target = "supplierId", source = "supplier.id")
    @Mapping(target = "supplierCode", source = "supplier.supplierCode")
    @Mapping(target = "supplierName", source = "supplier.name")
    @Mapping(target = "expired", expression = "java(priceCondition.isExpired())")
    @Mapping(target = "expiringSoon", expression = "java(priceCondition.isExpiringSoon())")
    public abstract PriceConditionResponse toResponse(PriceCondition priceCondition);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "priceCondition", ignore = true)
    public abstract PriceScale toEntity(PriceScaleRequest request);

    public abstract PriceScaleResponse toResponse(PriceScale priceScale);

    @AfterMapping
    protected void setPriceScales(@MappingTarget PriceCondition priceCondition, PriceConditionRequest request) {
        if (request.getPriceScales() != null) {
            request.getPriceScales().forEach(scaleRequest -> {
                PriceScale scale = toEntity(scaleRequest);
                scale.setPriceCondition(priceCondition);
                priceCondition.getPriceScales().add(scale);
            });
        }
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "item", source = "itemId")
    @Mapping(target = "customer", source = "customerId")
    @Mapping(target = "supplier", source = "supplierId")
    public abstract void updateEntity(@MappingTarget PriceCondition priceCondition, PriceConditionRequest request);

    protected Item mapItemId(Long id) {
        if (id == null) return null;
        return itemRepository.findById(id).orElse(null);
    }

    protected Customer mapCustomerId(Long id) {
        if (id == null) return null;
        return customerRepository.findById(id).orElse(null);
    }

    protected Supplier mapSupplierId(Long id) {
        if (id == null) return null;
        return supplierRepository.findById(id).orElse(null);
    }
}