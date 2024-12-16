package com.minierpapp.controller;

import com.minierpapp.controller.base.BaseRestController;
import com.minierpapp.model.supplier.Supplier;
import com.minierpapp.model.supplier.dto.SupplierDto;
import com.minierpapp.model.supplier.dto.SupplierRequest;
import com.minierpapp.model.supplier.dto.SupplierResponse;
import com.minierpapp.model.supplier.mapper.SupplierMapper;
import com.minierpapp.service.SupplierService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController extends BaseRestController<Supplier, SupplierDto, SupplierRequest, SupplierResponse> {

    private final SupplierService supplierService;

    public SupplierController(SupplierMapper mapper, SupplierService supplierService) {
        super(mapper);
        this.supplierService = supplierService;
    }

    @GetMapping("/code/{supplierCode}")
    public ResponseEntity<SupplierResponse> findBySupplierCode(@PathVariable String supplierCode) {
        return ResponseEntity.ok(supplierService.findBySupplierCode(supplierCode));
    }

    @Override
    protected List<SupplierResponse> findAllEntities() {
        return supplierService.findAll(null, null);
    }

    @Override
    protected SupplierResponse findEntityById(Long id) {
        return supplierService.findById(id);
    }

    @Override
    protected SupplierResponse createEntity(SupplierRequest request) {
        return supplierService.create(request);
    }

    @Override
    protected SupplierResponse updateEntity(Long id, SupplierRequest request) {
        return supplierService.update(id, request);
    }

    @Override
    protected void deleteEntity(Long id) {
        supplierService.delete(id);
    }

    @Override
    protected List<SupplierResponse> searchEntities(String keyword) {
        return supplierService.searchSuppliers(keyword);
    }
}