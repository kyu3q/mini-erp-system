package com.minierpapp.service;

import com.minierpapp.exception.ResourceNotFoundException;
import com.minierpapp.model.order.Order;
import com.minierpapp.model.order.OrderStatus;
import com.minierpapp.model.order.dto.OrderDto;
import com.minierpapp.model.order.dto.OrderRequest;
import com.minierpapp.model.order.dto.OrderResponse;
import com.minierpapp.model.order.mapper.OrderMapper;
import com.minierpapp.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Transactional(readOnly = true)
    public List<OrderDto> findAll() {
        return orderMapper.toDtoList(orderRepository.findAllActive());
    }

    @Transactional(readOnly = true)
    public OrderDto findById(Long id) {
        return orderMapper.toDto(orderRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id)));
    }

    @Transactional
    public OrderResponse create(OrderRequest request) {
        if (request.getOrderNumber() == null || request.getOrderNumber().isEmpty()) {
            request.setOrderNumber(generateOrderNumber());
        }
        Order order = orderMapper.toEntity(request);
        order.setStatus(OrderStatus.DRAFT);
        return orderMapper.toResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse update(Long id, OrderRequest request) {
        Order order = orderRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        
        Order updatedOrder = orderMapper.toEntity(request);
        updatedOrder.setId(order.getId());
        updatedOrder.setCreatedAt(order.getCreatedAt());
        updatedOrder.setCreatedBy(order.getCreatedBy());
        
        return orderMapper.toResponse(orderRepository.save(updatedOrder));
    }

    @Transactional
    public void delete(Long id) {
        Order order = orderRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        order.setDeleted(true);
        orderRepository.save(order);
    }

    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatus status) {
        Order order = orderRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        order.setStatus(status);
        return orderMapper.toResponse(orderRepository.save(order));
    }

    private String generateOrderNumber() {
        String prefix = "ORD" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyMM"));
        Integer maxNumber = orderRepository.findMaxOrderNumberByPrefix(prefix);
        return String.format("%s%04d", prefix, maxNumber + 1);
    }
}