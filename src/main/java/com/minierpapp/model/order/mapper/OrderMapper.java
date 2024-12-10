package com.minierpapp.model.order.mapper;

import com.minierpapp.model.customer.Customer;
import com.minierpapp.model.item.Item;
import com.minierpapp.model.order.Order;
import com.minierpapp.model.order.OrderDetail;
import com.minierpapp.model.order.dto.OrderDto;
import com.minierpapp.model.order.dto.OrderRequest;
import com.minierpapp.model.order.dto.OrderResponse;
import com.minierpapp.model.warehouse.Warehouse;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "customer", source = "customerId")
    @Mapping(target = "orderDetails", source = "orderDetails")
    Order toEntity(OrderRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "item", source = "itemId")
    @Mapping(target = "warehouse", source = "warehouseId")
    OrderDetail toEntity(OrderRequest.OrderDetailRequest request);

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", source = "customer.name")
    OrderDto toDto(Order order);

    @Mapping(target = "itemId", source = "item.id")
    @Mapping(target = "itemName", source = "item.itemName")
    @Mapping(target = "itemCode", source = "item.itemCode")
    @Mapping(target = "warehouseId", source = "warehouse.id")
    @Mapping(target = "warehouseName", source = "warehouse.name")
    OrderDto.OrderDetailDto toDto(OrderDetail orderDetail);

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", source = "customer.name")
    OrderResponse toResponse(Order order);

    @Mapping(target = "itemId", source = "item.id")
    @Mapping(target = "itemName", source = "item.itemName")
    @Mapping(target = "itemCode", source = "item.itemCode")
    @Mapping(target = "warehouseId", source = "warehouse.id")
    @Mapping(target = "warehouseName", source = "warehouse.name")
    OrderResponse.OrderDetailResponse toResponse(OrderDetail orderDetail);

    List<OrderDto> toDtoList(List<Order> orders);
    List<OrderResponse> toResponseList(List<Order> orders);

    default Customer customerFromId(Long id) {
        if (id == null) {
            return null;
        }
        Customer customer = new Customer();
        customer.setId(id);
        return customer;
    }

    default Item itemFromId(Long id) {
        if (id == null) {
            return null;
        }
        Item item = new Item();
        item.setId(id);
        return item;
    }

    default Warehouse warehouseFromId(Long id) {
        if (id == null) {
            return null;
        }
        Warehouse warehouse = new Warehouse();
        warehouse.setId(id);
        return warehouse;
    }
}