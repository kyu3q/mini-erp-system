package com.minierpapp.model.order.mapper;

import com.minierpapp.model.base.BaseMapper;
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
import java.util.stream.Collectors;

@Mapper(
    componentModel = "spring", 
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface OrderMapper extends BaseMapper<Order, OrderDto, OrderRequest, OrderResponse> {

    @Override
    @Mapping(target = "customer", source = "customerId")
    @Mapping(target = "orderDetails", source = "orderDetails")
    Order requestToEntity(OrderRequest request);

    @Override
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", source = "customer.name")
    OrderDto toDto(Order order);

    @Override
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", source = "customer.name")
    OrderResponse entityToResponse(Order order);

    @Mapping(target = "itemId", source = "item.id")
    @Mapping(target = "itemName", source = "item.itemName")
    @Mapping(target = "itemCode", source = "item.itemCode")
    @Mapping(target = "warehouseId", source = "warehouse.id")
    @Mapping(target = "warehouseName", source = "warehouse.name")
    OrderDto.OrderDetailDto toDto(OrderDetail orderDetail);

    @Mapping(target = "itemId", source = "item.id")
    @Mapping(target = "itemName", source = "item.itemName")
    @Mapping(target = "itemCode", source = "item.itemCode")
    @Mapping(target = "warehouseId", source = "warehouse.id")
    @Mapping(target = "warehouseName", source = "warehouse.name")
    OrderResponse.OrderDetailResponse toResponse(OrderDetail orderDetail);

    default List<OrderResponse> toResponseList(List<Order> entities) {
        return entities.stream()
            .map(this::entityToResponse)
            .collect(Collectors.toList());
    }

    default List<OrderDto> toDtoList(List<Order> entities) {
        return entities.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    default Customer customerFromId(Long id) {
        if (id == null) {
            return null;
        }
        Customer customer = new Customer();
        customer.setId(id);
        return customer;
    }

    @Named("itemFromId")
    default Item itemFromId(Long id) {
        if (id == null) {
            return null;
        }
        Item item = new Item();
        item.setId(id);
        return item;
    }

    @Named("warehouseFromId")
    default Warehouse warehouseFromId(Long id) {
        if (id == null) {
            return null;
        }
        Warehouse warehouse = new Warehouse();
        warehouse.setId(id);
        return warehouse;
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    OrderDetail toOrderDetail(OrderRequest.OrderDetailRequest request);

    List<OrderDetail> toOrderDetails(List<OrderRequest.OrderDetailRequest> requests);
}