package com.minierpapp.service;

import com.minierpapp.exception.ResourceNotFoundException;
import com.minierpapp.model.supplier.Supplier;
import com.minierpapp.model.supplier.dto.SupplierRequest;
import com.minierpapp.model.supplier.dto.SupplierResponse;
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
    private final SupplierMapper supplierMapper;

    public List<SupplierResponse> findAll(String supplierCode, String name) {
        return supplierRepository.findBySupplierCodeAndName(supplierCode, name)
                .stream()
                .map(supplierMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SupplierResponse> findAllActive() {
        return supplierRepository.findByDeletedFalseOrderBySupplierCodeAsc()
                .stream()
                .map(supplierMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SupplierResponse> searchSuppliers(String keyword) {
        keyword = keyword != null ? keyword.trim() : "";
        List<Supplier> suppliers = supplierRepository.findBySupplierCodeContainingOrNameContainingAndDeletedFalse(keyword, keyword);
        return suppliers.stream()
            .map(supplierMapper::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public SupplierResponse findBySupplierCode(String supplierCode) {
        return supplierRepository.findBySupplierCodeAndDeletedFalse(supplierCode)
                .map(supplierMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "supplierCode", supplierCode));
    }

    @Transactional(readOnly = true)
    public SupplierResponse findById(Long id) {
        return supplierRepository.findById(id)
                .map(supplierMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", id));
    }

    @Transactional
    public SupplierResponse create(SupplierRequest request) {
        if (supplierRepository.existsBySupplierCodeAndDeletedFalse(request.getSupplierCode())) {
            throw new IllegalArgumentException("仕入先コード " + request.getSupplierCode() + " は既に使用されています");
        }

        Supplier supplier = supplierMapper.toEntity(request);
        return supplierMapper.toResponse(supplierRepository.save(supplier));
    }

    @Transactional
    public SupplierResponse update(Long id, SupplierRequest request) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", id));

        supplierRepository.findBySupplierCodeAndDeletedFalse(request.getSupplierCode())
                .ifPresent(existingSupplier -> {
                    if (!existingSupplier.getId().equals(id)) {
                        throw new IllegalArgumentException("仕入先コード " + request.getSupplierCode() + " は既に使用されています");
                    }
                });

        supplierMapper.updateEntity(request, supplier);
        return supplierMapper.toResponse(supplierRepository.save(supplier));
    }

    @Transactional
    public void delete(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", id));
        supplier.setDeleted(true);
        supplierRepository.save(supplier);
    }

        @Transactional(readOnly = true)
    public boolean existsByCode(String supplierCode) {
        return supplierRepository.existsBySupplierCodeAndDeletedFalse(supplierCode);
    }

    @Transactional(readOnly = true)
    public Supplier findByCode(String supplierCode) {
        return supplierRepository.findBySupplierCodeAndDeletedFalse(supplierCode)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "supplierCode", supplierCode));
    }

    public void bulkCreate(List<SupplierRequest> suppliers) {
        suppliers.forEach(this::create);
    }
}