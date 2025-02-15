package com.minierpapp.service;

import com.minierpapp.exception.ResourceNotFoundException;
import com.minierpapp.model.warehouse.Warehouse;
import com.minierpapp.model.warehouse.dto.WarehouseDto;
import com.minierpapp.model.warehouse.dto.WarehouseRequest;
import com.minierpapp.model.warehouse.dto.WarehouseResponse;
import com.minierpapp.model.warehouse.mapper.WarehouseMapper;
import com.minierpapp.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DuplicateKeyException;
import com.minierpapp.model.common.Status;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final WarehouseMapper warehouseMapper;

    public List<Warehouse> findAll() {
        return warehouseRepository.findAll();
    }

    public Page<WarehouseDto> findAll(Pageable pageable) {
        return warehouseRepository.findAll(pageable)
                .map(warehouseMapper::toDto);
    }

    public Warehouse findById(Long id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", id));
    }

    public WarehouseResponse findByWarehouseCode(String warehouseCode) {
        Warehouse warehouse = warehouseRepository.findByWarehouseCode(warehouseCode)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "warehouseCode", warehouseCode));
        return warehouseMapper.entityToResponse(warehouse);
    }

    public List<Warehouse> search(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return warehouseRepository.findAll();
        }
        return warehouseRepository.findByWarehouseCodeContainingIgnoreCaseOrNameContainingIgnoreCase(keyword, keyword);
    }

    @Transactional
    public WarehouseDto create(WarehouseRequest request) {
        validateWarehouseCode(request.getWarehouseCode(), null);
        Warehouse warehouse = warehouseMapper.requestToEntity(request);
        warehouse = warehouseRepository.save(warehouse);
        return warehouseMapper.toDto(warehouse);
    }

    @Transactional
    public WarehouseDto update(Long id, WarehouseRequest request) {
        Warehouse warehouse = findById(id);
        validateWarehouseCode(request.getWarehouseCode(), id);
        warehouseMapper.updateEntityFromRequest(request, warehouse);
        warehouse = warehouseRepository.save(warehouse);
        return warehouseMapper.toDto(warehouse);
    }

    public void delete(Long id) {
        Warehouse warehouse = findById(id);
        warehouseRepository.delete(warehouse);
    }

    @Transactional
    public Warehouse create(Warehouse warehouse) {
        validateWarehouseCode(warehouse.getWarehouseCode(), null);
        return warehouseRepository.save(warehouse);
    }

    @Transactional
    public Warehouse update(Warehouse warehouse) {
        validateWarehouseCode(warehouse.getWarehouseCode(), warehouse.getId());
        return warehouseRepository.save(warehouse);
    }

    @Transactional
    public void bulkCreate(List<WarehouseRequest> warehouses) {
        warehouses.forEach(this::create);
    }

    private void validateWarehouseCode(String warehouseCode, Long excludeId) {
        warehouseRepository.findByWarehouseCode(warehouseCode)
                .ifPresent(existingWarehouse -> {
                    if (excludeId == null || !existingWarehouse.getId().equals(excludeId)) {
                        throw new DuplicateKeyException("倉庫コード " + warehouseCode + " は既に使用されています");
                    }
                });
    }

    public Page<WarehouseDto> search(String warehouseCode, String name, Pageable pageable) {
        return warehouseRepository.search(warehouseCode, name, pageable)
                .map(warehouseMapper::toDto);
    }

    public List<Warehouse> findAllActive() {
        return warehouseRepository.findByStatusAndDeletedFalse(Status.ACTIVE);
    }
}