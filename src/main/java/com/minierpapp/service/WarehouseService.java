package com.minierpapp.service;

import com.minierpapp.exception.ResourceNotFoundException;
import com.minierpapp.model.common.Status;
import com.minierpapp.model.warehouse.Warehouse;
import com.minierpapp.model.warehouse.dto.WarehouseDto;
import com.minierpapp.model.warehouse.dto.WarehouseRequest;
import com.minierpapp.model.warehouse.mapper.WarehouseMapper;
import com.minierpapp.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final WarehouseMapper warehouseMapper;

    @Transactional(readOnly = true)
    public Page<WarehouseDto> findAll(Pageable pageable) {
        return warehouseRepository.findAll(pageable)
                .map(warehouseMapper::toDto);
    }

    @Transactional(readOnly = true)
    public WarehouseDto findById(Long id) {
        return warehouseRepository.findById(id)
                .map(warehouseMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public WarehouseDto findByWarehouseCode(String warehouseCode) {
        return warehouseRepository.findByWarehouseCode(warehouseCode)
                .map(warehouseMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with code: " + warehouseCode));
    }

    @Transactional
    public WarehouseDto create(WarehouseRequest request) {
        if (warehouseRepository.existsByWarehouseCode(request.getWarehouseCode())) {
            throw new IllegalArgumentException("Warehouse code already exists: " + request.getWarehouseCode());
        }

        Warehouse warehouse = warehouseMapper.toEntity(request);
        warehouse.setStatus(Status.ACTIVE);
        return warehouseMapper.toDto(warehouseRepository.save(warehouse));
    }

    @Transactional
    public WarehouseDto update(Long id, WarehouseRequest request) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + id));

        if (!warehouse.getWarehouseCode().equals(request.getWarehouseCode()) &&
            warehouseRepository.existsByWarehouseCode(request.getWarehouseCode())) {
            throw new IllegalArgumentException("Warehouse code already exists: " + request.getWarehouseCode());
        }

        warehouse.setWarehouseCode(request.getWarehouseCode());
        warehouse.setName(request.getName());
        warehouse.setAddress(request.getAddress());
        warehouse.setCapacity(request.getCapacity());
        warehouse.setDescription(request.getDescription());

        return warehouseMapper.toDto(warehouseRepository.save(warehouse));
    }

    @Transactional
    public void delete(Long id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + id));
        warehouse.setStatus(Status.INACTIVE);
        warehouseRepository.save(warehouse);
    }
}