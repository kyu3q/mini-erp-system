package com.minierpapp.service;

import com.minierpapp.exception.ResourceNotFoundException;
import com.minierpapp.model.supplier.Supplier;
import com.minierpapp.model.supplier.dto.SupplierDto;
import com.minierpapp.model.supplier.dto.SupplierRequest;
import com.minierpapp.model.supplier.mapper.SupplierMapper;
import com.minierpapp.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupplierService {
    private final SupplierRepository supplierRepository;

    public List<SupplierDto> findAll(String supplierCode, String name) {
        return supplierRepository.findBySupplierCodeAndName(supplierCode, name)
                .stream()
                .map(SupplierMapper.INSTANCE::toDto)
                .collect(Collectors.toList());
    }

    public SupplierDto findById(Long id) {
        return supplierRepository.findById(id)
                .map(SupplierMapper.INSTANCE::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", id));
    }

    @Transactional
    public SupplierDto create(SupplierRequest request) {
        if (supplierRepository.existsBySupplierCodeAndDeletedFalse(request.getSupplierCode())) {
            throw new IllegalArgumentException("仕入先コード " + request.getSupplierCode() + " は既に使用されています");
        }

        Supplier supplier = SupplierMapper.INSTANCE.toEntity(request);
        return SupplierMapper.INSTANCE.toDto(supplierRepository.save(supplier));
    }

    @Transactional
    public SupplierDto update(Long id, SupplierRequest request) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", id));

        supplierRepository.findBySupplierCodeAndDeletedFalse(request.getSupplierCode())
                .ifPresent(existingSupplier -> {
                    if (!existingSupplier.getId().equals(id)) {
                        throw new IllegalArgumentException("仕入先コード " + request.getSupplierCode() + " は既に使用されています");
                    }
                });

        SupplierMapper.INSTANCE.updateEntity(request, supplier);
        return SupplierMapper.INSTANCE.toDto(supplierRepository.save(supplier));
    }

    @Transactional
    public void delete(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", id));
        supplier.setDeleted(true);
        supplierRepository.save(supplier);
    }
}