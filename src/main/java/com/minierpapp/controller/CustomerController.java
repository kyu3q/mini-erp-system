package com.minierpapp.controller;

import com.minierpapp.controller.base.BaseRestController;
import com.minierpapp.model.customer.Customer;
import com.minierpapp.model.customer.dto.CustomerDto;
import com.minierpapp.model.customer.dto.CustomerRequest;
import com.minierpapp.model.customer.dto.CustomerResponse;
import com.minierpapp.model.customer.mapper.CustomerMapper;
import com.minierpapp.service.CustomerService;

import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import com.minierpapp.model.common.Status;

@RestController
@RequestMapping("/api/customers")
public class CustomerController extends BaseRestController<Customer, CustomerDto, CustomerRequest, CustomerResponse> {

    private final CustomerService customerService;

    public CustomerController(CustomerMapper mapper, CustomerService customerService) {
        super(mapper);
        this.customerService = customerService;
    }

     @PostMapping("/import")
    public Map<String, Object> importExcel(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> errors = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;
        int updateCount = 0;
        int createCount = 0;

        try {
            List<CustomerRequest> customersToCreate = new ArrayList<>();
            List<CustomerRequest> customersToUpdate = new ArrayList<>();

            try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
                Sheet sheet = workbook.getSheetAt(0);

                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    try {
                        Row row = sheet.getRow(i);
                        if (row == null || isEmptyRow(row)) continue;

                        CustomerRequest customer = new CustomerRequest();
                        String customerCode = getStringCellValue(row.getCell(0));
                        
                        // 必須項目のバリデーション
                        if (customerCode == null || customerCode.isEmpty()) {
                            throw new IllegalArgumentException("得意先コードは必須です (行: " + (i + 1) + ")");
                        }

                        customer.setCustomerCode(customerCode);
                        customer.setName(getStringCellValue(row.getCell(1)));
                        customer.setNameKana(getStringCellValue(row.getCell(2)));
                        customer.setPostalCode(getStringCellValue(row.getCell(3)));
                        customer.setAddress(getStringCellValue(row.getCell(4)));
                        customer.setPhone(getStringCellValue(row.getCell(5)));
                        customer.setFax(getStringCellValue(row.getCell(6)));
                        customer.setEmail(getStringCellValue(row.getCell(7)));
                        customer.setContactPerson(getStringCellValue(row.getCell(8)));
                        customer.setPaymentTerms(getStringCellValue(row.getCell(9)));

                        // ステータスの処理
                        String status = getStringCellValue(row.getCell(10));
                        if (status != null && !status.isEmpty()) {
                            try {
                                switch (status.trim()) {
                                    case "有効":
                                        customer.setStatus(Status.ACTIVE);
                                        break;
                                    case "無効":
                                        customer.setStatus(Status.INACTIVE);
                                        break;
                                    default:
                                        try {
                                            customer.setStatus(Status.valueOf(status.toUpperCase()));
                                        } catch (IllegalArgumentException e) {
                                            throw new IllegalArgumentException("無効なステータス値です。'有効' または '無効' を入力してください。: " + status + " (行: " + (i + 1) + ")");
                                        }
                            }
                        } catch (IllegalArgumentException e) {
                            throw new IllegalArgumentException("無効なステータス値です。'有効' または '無効' を入力してください。: " + status + " (行: " + (i + 1) + ")");
                        }
                    } else {
                        throw new IllegalArgumentException("ステータスは必須です (行: " + (i + 1) + ")");
                    }

                    customer.setNotes(getStringCellValue(row.getCell(11)));

                    // その他の必須項目のバリデーション
                    if (customer.getName() == null || customer.getName().isEmpty()) {
                        throw new IllegalArgumentException("得意先名は必須です (行: " + (i + 1) + ")");
                    }

                    // 既存データの確認
                    try {
                        CustomerResponse existingCustomer = customerService.findByCustomerCode(customerCode);
                        if (existingCustomer != null) {
                            // 既存データがある場合は更新リストに追加
                            customer.setId(existingCustomer.getId());
                            customersToUpdate.add(customer);
                        } else {
                            // 新規データの場合は作成リストに追加
                            customersToCreate.add(customer);
                        }
                        successCount++;
                    } catch (Exception e) {
                        throw new IllegalArgumentException("データの検証中にエラーが発生しました (行: " + (i + 1) + "): " + e.getMessage());
                    }

                } catch (Exception e) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("rowNum", i + 1);
                    error.put("message", e.getMessage());
                    errors.add(error);
                    errorCount++;
                }
            }
        }

        // 一括更新と作成の実行
        if (!customersToUpdate.isEmpty()) {
            for (CustomerRequest customer : customersToUpdate) {
                customerService.update(customer.getId(), customer);
                updateCount++;
            }
        }

        if (!customersToCreate.isEmpty()) {
            customerService.bulkCreate(customersToCreate);
            createCount = customersToCreate.size();
        }

        result.put("totalCount", successCount + errorCount);
        result.put("successCount", successCount);
        result.put("createCount", createCount);
        result.put("updateCount", updateCount);
        result.put("errorCount", errorCount);
        result.put("errors", errors);

        return result;

    } catch (Exception e) {
        throw new RuntimeException("ファイルの処理中にエラーが発生しました: " + e.getMessage());
    }
}

    // ユーティリティメソッドも移動
    private boolean isEmptyRow(Row row) {
        if (row == null) return true;
        for (int i = 0; i < 12; i++) {
            if (getStringCellValue(row.getCell(i)) != null && !getStringCellValue(row.getCell(i)).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private String getStringCellValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toString();
                }
                return String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }

    @GetMapping("/code/{customerCode}")
    public ResponseEntity<CustomerResponse> findByCustomerCode(@PathVariable String customerCode) {
        return ResponseEntity.ok(customerService.findByCustomerCode(customerCode));
    }

    @Override
    protected List<CustomerResponse> findAllEntities() {
        return customerService.findAll(null, null);
    }

    @Override
    protected CustomerResponse findEntityById(Long id) {
        return customerService.findById(id);
    }

    @Override
    protected CustomerResponse createEntity(CustomerRequest request) {
        return customerService.create(request);
    }

    @Override
    protected CustomerResponse updateEntity(Long id, CustomerRequest request) {
        return customerService.update(id, request);
    }

    @Override
    protected void deleteEntity(Long id) {
        customerService.delete(id);
    }

    @Override
    protected List<CustomerResponse> searchEntities(String keyword) {
        return customerService.searchCustomers(keyword);
    }

    @GetMapping("/search/page")
    public ResponseEntity<Page<CustomerResponse>> searchCustomersWithPagination(
            @RequestParam(required = false) String keyword,
            Pageable pageable) {
        return ResponseEntity.ok(customerService.searchCustomersWithPagination(keyword, pageable));
    }
}