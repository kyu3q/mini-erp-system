package com.minierpapp.controller;

import com.minierpapp.controller.base.BaseWebController;
import com.minierpapp.model.supplier.Supplier;
import com.minierpapp.model.supplier.dto.SupplierDto;
import com.minierpapp.model.supplier.dto.SupplierRequest;
import com.minierpapp.model.supplier.dto.SupplierResponse;
import com.minierpapp.model.supplier.mapper.SupplierMapper;
import com.minierpapp.service.SupplierService;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/suppliers")
public class SupplierWebController extends BaseWebController<Supplier, SupplierDto, SupplierRequest, SupplierResponse> {

    private final SupplierService supplierService;

    public SupplierWebController(SupplierMapper mapper, MessageSource messageSource, SupplierService supplierService) {
        super(mapper, messageSource, "supplier", "Supplier");
        this.supplierService = supplierService;
    }

    @Override
    @GetMapping
    public String list(
            @RequestParam(required = false) String searchParam1,
            @RequestParam(required = false) String searchParam2,
            Model model) {
        String supplierCode = searchParam1;
        String name = searchParam2;
        model.addAttribute("suppliers", supplierService.findAll(supplierCode, name));
        model.addAttribute("supplierCode", supplierCode);
        model.addAttribute("name", name);
        return getListTemplate();
    }

    @Override
    protected List<SupplierResponse> findAll() {
        return supplierService.findAll(null, null);
    }

    @Override
    protected SupplierRequest createNewRequest() {
        return new SupplierRequest();
    }

    @Override
    protected SupplierRequest findById(Long id) {
        SupplierResponse supplierResponse = supplierService.findById(id);
        SupplierRequest request = new SupplierRequest();
        request.setSupplierCode(supplierResponse.getSupplierCode());
        request.setName(supplierResponse.getName());
        request.setNameKana(supplierResponse.getNameKana());
        request.setPostalCode(supplierResponse.getPostalCode());
        request.setAddress(supplierResponse.getAddress());
        request.setPhone(supplierResponse.getPhone());
        request.setEmail(supplierResponse.getEmail());
        request.setFax(supplierResponse.getFax());
        request.setContactPerson(supplierResponse.getContactPerson());
        request.setPaymentTerms(supplierResponse.getPaymentTerms());
        request.setStatus(supplierResponse.getStatus());
        request.setNotes(supplierResponse.getNotes());
        return request;
    }

    @Override
    protected void createEntity(SupplierRequest request) {
        supplierService.create(request);
    }

    @Override
    protected void updateEntity(Long id, SupplierRequest request) {
        supplierService.update(id, request);
    }

    @Override
    protected void deleteEntity(Long id) {
        supplierService.delete(id);
    }

    @Override
    protected void handleError(BindingResult result, Exception e) {
        if (e.getMessage().contains("仕入先コード")) {
            result.rejectValue("supplierCode", "error.supplierCode", e.getMessage());
        } else {
            super.handleError(result, e);
        }
    }
}