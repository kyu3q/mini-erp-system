package com.minierpapp.service;

import com.minierpapp.exception.ResourceNotFoundException;
import com.minierpapp.model.customer.Customer;
import com.minierpapp.model.customer.dto.CustomerRequest;
import com.minierpapp.model.customer.dto.CustomerResponse;
import com.minierpapp.model.customer.mapper.CustomerMapper;
import com.minierpapp.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public List<CustomerResponse> findAll(String customerCode, String name) {
        return customerRepository.findByCustomerCodeAndName(customerCode, name)
                .stream()
                .map(customerMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> searchCustomers(String keyword) {
        keyword = keyword != null ? keyword.trim() : "";
        List<Customer> customers = customerRepository.findByCustomerCodeContainingOrNameContainingAndDeletedFalse(keyword, keyword);
        return customers.stream()
            .map(customerMapper::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public Page<CustomerResponse> searchCustomersWithPagination(String keyword, Pageable pageable) {
        keyword = keyword != null ? keyword.trim() : "";
        Page<Customer> customers = customerRepository.findByCustomerCodeContainingOrNameContainingAndDeletedFalse(keyword, keyword, pageable);
        return customers.map(customerMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<Customer> findAllActive() {
        return customerRepository.findByDeletedFalse();
    }

    @Transactional(readOnly = true)
    public CustomerResponse findByCustomerCode(String customerCode) {
        Customer customer = customerRepository.findByCustomerCodeAndDeletedFalse(customerCode)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "customerCode", customerCode));

        if (!hasAccessToCustomer(customer)) {
            throw new AccessDeniedException("Access denied");
        }

        return customerMapper.toResponse(customer);
    }

    @Transactional(readOnly = true)
    public CustomerResponse findById(Long id) {
        return customerRepository.findById(id)
                .map(customerMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
    }

    @Transactional(readOnly = true)
    public boolean existsByCode(String customerCode) {
        return customerRepository.existsByCustomerCodeAndDeletedFalse(customerCode);
    }

    @Transactional(readOnly = true)
    public Customer findByCode(String customerCode) {
        return customerRepository.findByCustomerCodeAndDeletedFalse(customerCode)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "customerCode", customerCode));
    }

    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        if (request.getCustomerCode() == null || request.getCustomerCode().trim().isEmpty()) {
            throw new IllegalArgumentException("得意先コードを入力してください");
        }

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("得意先名は必須です");
        }

        if (customerRepository.existsByCustomerCodeAndDeletedFalse(request.getCustomerCode())) {
            throw new IllegalArgumentException("得意先コード " + request.getCustomerCode() + " は既に使用されています");
        }

        Customer customer = customerMapper.toEntity(request);
        return customerMapper.toResponse(customerRepository.save(customer));
    }

    @Transactional
    public CustomerResponse update(Long id, CustomerRequest request) {
        if (request.getCustomerCode() == null || request.getCustomerCode().trim().isEmpty()) {
            throw new IllegalArgumentException("得意先コードを入力してください");
        }

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("得意先名は必須です");
        }

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));

        customerRepository.findByCustomerCodeAndDeletedFalse(request.getCustomerCode())
                .ifPresent(existingCustomer -> {
                    if (!existingCustomer.getId().equals(id)) {
                        throw new IllegalArgumentException("得意先コード " + request.getCustomerCode() + " は既に使用されています");
                    }
                });

        customerMapper.updateEntity(request, customer);
        return customerMapper.toResponse(customerRepository.save(customer));
    }

    @Transactional
    public void delete(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
        customer.setDeleted(true);
        customerRepository.save(customer);
    }

    @Transactional(readOnly = true)
    public Page<CustomerResponse> search(String keyword, Pageable pageable) {
        Page<Customer> customerPage = customerRepository.findByNameContainingAndDeletedFalse(keyword, pageable);
        return customerPage.map(customerMapper::toResponse);
    }

    @Transactional
    public List<CustomerResponse> bulkCreate(List<CustomerRequest> requests) {
        return requests.stream()
            .map(request -> {
                if (request.getCustomerCode() == null || request.getCustomerCode().trim().isEmpty()) {
                    throw new IllegalArgumentException("得意先コードを入力してください");
                }
                if (request.getName() == null || request.getName().trim().isEmpty()) {
                    throw new IllegalArgumentException("得意先名は必須です");
                }
                if (customerRepository.existsByCustomerCodeAndDeletedFalse(request.getCustomerCode())) {
                    throw new IllegalArgumentException("得意先コード " + request.getCustomerCode() + " は既に使用されています");
                }
                Customer customer = customerMapper.toEntity(request);
                return customerMapper.toResponse(customerRepository.save(customer));
            })
            .collect(Collectors.toList());
    }

    public boolean hasAccessToCustomer(Customer customer) {
        // TODO: 実際のセキュリティロジックを実装
        // 例: 現在のユーザーが顧客にアクセスする権限があるかどうかをチェック
        return true;
    }

    /**
     * Excelファイルにデータを書き出す
     */
    public void writeToExcel(Workbook workbook, List<CustomerResponse> customers) {
        Sheet sheet = workbook.createSheet("得意先");
        
        // ヘッダー行
        Row headerRow = sheet.createRow(0);
        String[] headers = {"顧客コード", "名前", "カナ名", "郵便番号", "住所", "電話番号", "メールアドレス"};
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }
        
        // データ行
        int rowNum = 1;
        for (CustomerResponse customer : customers) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(customer.getCustomerCode());
            row.createCell(1).setCellValue(customer.getName());
            row.createCell(2).setCellValue(customer.getNameKana());
            row.createCell(3).setCellValue(customer.getPostalCode());
            row.createCell(4).setCellValue(customer.getAddress());
            row.createCell(5).setCellValue(customer.getPhone());
            row.createCell(6).setCellValue(customer.getEmail());
        }
        
        // 列幅の自動調整
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * 取込テンプレートを作成
     */
    public void createTemplate(Workbook workbook) {
        Sheet sheet = workbook.createSheet("得意先");
        
        // ヘッダー行
        Row headerRow = sheet.createRow(0);
        String[] headers = {"顧客コード", "名前", "カナ名", "郵便番号", "住所", "電話番号", "メールアドレス"};
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }
        
        // 列幅の自動調整
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * Excelファイルからデータを取り込む
     */
    public void importFromExcel(InputStream inputStream) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            
            // ヘッダー行の検証
            Row headerRow = sheet.getRow(0);
            if (headerRow == null || !isValidHeader(headerRow)) {
                throw new IllegalArgumentException("テンプレートの形式が不正です");
            }
            
            // データ行の処理
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                CustomerRequest request = new CustomerRequest();
                request.setCustomerCode(getCellValue(row.getCell(0)));
                request.setName(getCellValue(row.getCell(1)));
                request.setNameKana(getCellValue(row.getCell(2)));
                request.setPostalCode(getCellValue(row.getCell(3)));
                request.setAddress(getCellValue(row.getCell(4)));
                request.setPhone(getCellValue(row.getCell(5)));
                request.setEmail(getCellValue(row.getCell(6)));
                
                try {
                    create(request);
                } catch (Exception e) {
                    throw new IllegalArgumentException(String.format("行 %d: %s", i + 1, e.getMessage()));
                }
            }
        }
    }

    private boolean isValidHeader(Row headerRow) {
        String[] expectedHeaders = {"顧客コード", "名前", "カナ名", "郵便番号", "住所", "電話番号", "メールアドレス"};
        for (int i = 0; i < expectedHeaders.length; i++) {
            Cell cell = headerRow.getCell(i);
            if (cell == null || !expectedHeaders[i].equals(cell.getStringCellValue())) {
                return false;
            }
        }
        return true;
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
            default -> "";
        };
    }

    @Transactional
    public void deleteAll() {
        customerRepository.deleteAll();
    }
}