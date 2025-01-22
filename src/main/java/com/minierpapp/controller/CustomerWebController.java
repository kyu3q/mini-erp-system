package com.minierpapp.controller;

import com.minierpapp.controller.base.BaseWebController;
import com.minierpapp.model.customer.Customer;
import com.minierpapp.model.customer.dto.CustomerDto;
import com.minierpapp.model.customer.dto.CustomerRequest;
import com.minierpapp.model.customer.dto.CustomerResponse;
import com.minierpapp.model.customer.mapper.CustomerMapper;
import com.minierpapp.service.CustomerService;
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
@RequestMapping("/customers")
public class CustomerWebController extends BaseWebController<Customer, CustomerDto, CustomerRequest, CustomerResponse> {

    private final CustomerService customerService;

    public CustomerWebController(CustomerMapper mapper, MessageSource messageSource, CustomerService customerService) {
        super(mapper, messageSource, "customer", "Customer");
        this.customerService = customerService;
    }

    @Override
    @GetMapping
    public String list(
            @RequestParam(required = false) String searchParam1,
            @RequestParam(required = false) String searchParam2,
            Model model) {
        String customerCode = searchParam1;
        String name = searchParam2;
        model.addAttribute("customers", customerService.findAll(customerCode, name));
        model.addAttribute("customerCode", customerCode);
        model.addAttribute("name", name);
        return getListTemplate();
    }

    @Override
    protected List<CustomerResponse> findAll() {
        return customerService.findAll(null, null);
    }

    @Override
    protected CustomerRequest createNewRequest() {
        return new CustomerRequest();
    }

    @Override
    protected CustomerRequest findById(Long id) {
        CustomerResponse customerResponse = customerService.findById(id);
        CustomerRequest request = new CustomerRequest();
        request.setCustomerCode(customerResponse.getCustomerCode());
        request.setName(customerResponse.getName());
        request.setNameKana(customerResponse.getNameKana());
        request.setPostalCode(customerResponse.getPostalCode());
        request.setAddress(customerResponse.getAddress());
        request.setPhone(customerResponse.getPhone());
        request.setEmail(customerResponse.getEmail());
        request.setFax(customerResponse.getFax());
        request.setContactPerson(customerResponse.getContactPerson());
        request.setPaymentTerms(customerResponse.getPaymentTerms());
        request.setStatus(customerResponse.getStatus());
        request.setNotes(customerResponse.getNotes());
        return request;
    }

    @Override
    protected void createEntity(CustomerRequest request) {
        customerService.create(request);
    }

    @Override
    protected void updateEntity(Long id, CustomerRequest request) {
        customerService.update(id, request);
    }

    @Override
    protected void deleteEntity(Long id) {
        customerService.delete(id);
    }

    @GetMapping("/export")
    public void exportToExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=customers.xlsx");

        List<CustomerResponse> customers = findAll();
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("得意先一覧");

            // ヘッダー行の作成
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("得意先コード");
            headerRow.createCell(1).setCellValue("得意先名");
            headerRow.createCell(2).setCellValue("得意先名（カナ）");
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
            for (CustomerResponse customer : customers) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(customer.getCustomerCode());
                row.createCell(1).setCellValue(customer.getName());
                row.createCell(2).setCellValue(customer.getNameKana());
                row.createCell(3).setCellValue(customer.getPostalCode());
                row.createCell(4).setCellValue(customer.getAddress());
                row.createCell(5).setCellValue(customer.getPhone());
                row.createCell(6).setCellValue(customer.getFax());
                row.createCell(7).setCellValue(customer.getEmail());
                row.createCell(8).setCellValue(customer.getContactPerson());
                row.createCell(9).setCellValue(customer.getPaymentTerms());
                row.createCell(10).setCellValue(customer.getStatus().getDisplayName());
                row.createCell(11).setCellValue(customer.getNotes());
            }

            // 列幅の自動調整
            for (int i = 0; i < 12; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(response.getOutputStream());
        }
    }
}