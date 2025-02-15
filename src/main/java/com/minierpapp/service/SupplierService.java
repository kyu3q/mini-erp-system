package com.minierpapp.service;

import com.minierpapp.exception.ResourceNotFoundException;
import com.minierpapp.model.supplier.Supplier;
import com.minierpapp.model.supplier.dto.SupplierRequest;
import com.minierpapp.model.supplier.dto.SupplierResponse;
import com.minierpapp.model.supplier.mapper.SupplierMapper;
import com.minierpapp.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
                .map(supplierMapper::entityToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SupplierResponse> findAllActive() {
        return supplierRepository.findByDeletedFalseOrderBySupplierCodeAsc()
                .stream()
                .map(supplierMapper::entityToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SupplierResponse> searchSuppliers(String keyword) {
        keyword = keyword != null ? keyword.trim() : "";
        List<Supplier> suppliers = supplierRepository.findBySupplierCodeContainingOrNameContainingAndDeletedFalse(keyword, keyword);
        return suppliers.stream()
            .map(supplierMapper::entityToResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public SupplierResponse findBySupplierCode(String supplierCode) {
        return supplierRepository.findBySupplierCodeAndDeletedFalse(supplierCode)
                .map(supplierMapper::entityToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "supplierCode", supplierCode));
    }

    @Transactional(readOnly = true)
    public SupplierResponse findById(Long id) {
        return supplierRepository.findById(id)
                .map(supplierMapper::entityToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", id));
    }

    @Transactional
    public SupplierResponse create(SupplierRequest request) {
        if (supplierRepository.existsBySupplierCodeAndDeletedFalse(request.getSupplierCode())) {
            throw new IllegalArgumentException("仕入先コード " + request.getSupplierCode() + " は既に使用されています");
        }

        Supplier supplier = supplierMapper.requestToEntity(request);
        supplier = supplierRepository.save(supplier);
        return supplierMapper.entityToResponse(supplier);
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

        supplierMapper.updateEntityFromRequest(request, supplier);
        supplier = supplierRepository.save(supplier);
        return supplierMapper.entityToResponse(supplier);
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

    public Page<Supplier> findAll(Pageable pageable) {
        return supplierRepository.findByDeletedFalse(pageable);
    }

    public Supplier save(Supplier supplier) {
        return supplierRepository.save(supplier);
    }

    public List<Supplier> findAllEntities() {
        return supplierRepository.findByDeletedFalse();
    }

    public Supplier findByCode(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", id));
    }
}