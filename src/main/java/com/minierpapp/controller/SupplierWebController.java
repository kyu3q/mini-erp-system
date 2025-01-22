package com.minierpapp.controller;

import com.minierpapp.controller.base.BaseWebController;
import com.minierpapp.model.supplier.Supplier;
import com.minierpapp.model.supplier.dto.SupplierDto;
import com.minierpapp.model.supplier.dto.SupplierRequest;
import com.minierpapp.model.supplier.dto.SupplierResponse;
import com.minierpapp.model.supplier.mapper.SupplierMapper;
import com.minierpapp.service.SupplierService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
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

    @GetMapping("/export")
    public void exportToExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=suppliers.xlsx");

        List<SupplierResponse> suppliers = findAll();
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("仕入先一覧");

            // ヘッダー行の作成
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("仕入先コード");
            headerRow.createCell(1).setCellValue("仕入先名");
            headerRow.createCell(2).setCellValue("仕入先名（カナ）");
            headerRow.createCell(3).setCellValue("郵便番号");
            headerRow.createCell(4).setCellValue("住所");
            headerRow.createCell(5).setCellValue("電話番号");
            headerRow.createCell(6).setCellValue("FAX");
            headerRow.createCell(7).setCellValue("メールアドレス");
            headerRow.createCell(8).setCellValue("担当者");
            headerRow.createCell(9).setCellValue("支払条件");
            headerRow.createCell(10).setCellValue("状態");
            headerRow.createCell(11).setCellValue("備考");

            // データ行の作成
            int rowNum = 1;
            for (SupplierResponse supplier : suppliers) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(supplier.getSupplierCode());
                row.createCell(1).setCellValue(supplier.getName());
                row.createCell(2).setCellValue(supplier.getNameKana());
                row.createCell(3).setCellValue(supplier.getPostalCode());
                row.createCell(4).setCellValue(supplier.getAddress());
                row.createCell(5).setCellValue(supplier.getPhone());
                row.createCell(6).setCellValue(supplier.getFax());
                row.createCell(7).setCellValue(supplier.getEmail());
                row.createCell(8).setCellValue(supplier.getContactPerson());
                row.createCell(9).setCellValue(supplier.getPaymentTerms());
                row.createCell(10).setCellValue(supplier.getStatus().getDisplayName());
                row.createCell(11).setCellValue(supplier.getNotes());
            }

            // 列幅の自動調整
            for (int i = 0; i < 12; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(response.getOutputStream());
        }
    }
}