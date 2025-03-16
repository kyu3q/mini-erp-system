package com.minierpapp.controller;

import com.minierpapp.controller.base.BaseWebController;
import com.minierpapp.model.customer.Customer;
import com.minierpapp.model.customer.dto.CustomerDto;
import com.minierpapp.model.customer.dto.CustomerRequest;
import com.minierpapp.model.customer.dto.CustomerResponse;
import com.minierpapp.model.customer.mapper.CustomerMapper;
import com.minierpapp.service.CustomerService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    protected CustomerResponse findById(Long id) {
        return customerService.findById(id);
    }

    @Override
    @PutMapping("/{id}")
    public String update(@PathVariable Long id,
                        @Valid @ModelAttribute(binding = true) CustomerRequest request,
                        BindingResult result,
                        Model model,
                        RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return handleValidationError(request, result, model);
        }

        try {
            updateEntity(id, request);
            addSuccessMessage(redirectAttributes, getUpdateSuccessMessage());
            return getRedirectToList();
        } catch (Exception e) {
            handleError(result, e);
            return handleValidationError(request, result, model);
        }
    }

    @Override
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            deleteEntity(id);
            addSuccessMessage(redirectAttributes, getDeleteSuccessMessage());
        } catch (Exception e) {
            addErrorMessage(redirectAttributes, e.getMessage());
        }
        return getRedirectToList();
    }

    @Override
    protected Long createEntityAndGetId(CustomerRequest request) {
        CustomerResponse createdEntity = customerService.create(request);
        return createdEntity.getId();
    }

    @Override
    protected void createEntity(CustomerRequest request) {
        // IDの設定処理を削除
        createEntityAndGetId(request);
    }

    @Override
    protected void updateEntity(Long id, CustomerRequest request) {
        customerService.update(id, request);
    }

    @Override
    protected void deleteEntity(Long id) {
        customerService.delete(id);
    }

    @Override
    protected void setRequestId(CustomerRequest request, Long id) {
        request.setId(id);
    }

    @GetMapping("/export")
    public void exportToExcel(HttpServletResponse response) throws IOException {
        List<CustomerResponse> customers = findAll();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("得意先マスタ");

        // ヘッダー行の作成
        String[] headers = {"得意先コード", "得意先名", "フリガナ", "郵便番号", "住所", "電話番号", "FAX", "メールアドレス", "担当者", "支払条件", "状態", "備考"};
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, 256 * 15);  // 15文字分の幅
        }

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

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=customers.xlsx");
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    @GetMapping("/import/template")
    public void generateTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=customer_template.xlsx");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("得意先データ");

            // ヘッダー行の作成
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "得意先コード*", "得意先名*", "フリガナ", "郵便番号", "住所",
                "電話番号", "FAX", "メールアドレス", "担当者", "支払条件",
                "ステータス*", "備考"
            };

            // ヘッダースタイルの設定
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // ヘッダーの作成
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 256 * 20);  // 20文字分の幅
            }

            // サンプルデータ行のスタイル
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            // サンプルデータ行の追加
            Row sampleRow = sheet.createRow(1);
            String[] sampleData = {
                "CUST001", "株式会社サンプル", "カブシキガイシャサンプル", "123-4567",
                "東京都千代田区...", "03-1234-5678", "03-1234-5679", "contact@example.com",
                "山田太郎", "月末締め翌月末払い", "有効", "備考欄"
            };

            for (int i = 0; i < sampleData.length; i++) {
                Cell cell = sampleRow.createCell(i);
                cell.setCellValue(sampleData[i]);
                cell.setCellStyle(dataStyle);
            }

            // 注意書き行の追加
            Row noteRow = sheet.createRow(3);
            Cell noteCell = noteRow.createCell(0);
            noteCell.setCellValue("注意: *は必須項目です。ステータスは「有効」または「無効」を入力してください。");
            CellStyle noteStyle = workbook.createCellStyle();
            Font noteFont = workbook.createFont();
            noteFont.setColor(IndexedColors.RED.getIndex());
            noteStyle.setFont(noteFont);
            noteCell.setCellStyle(noteStyle);

            // セルの結合（注意書き用）
            sheet.addMergedRegion(new CellRangeAddress(3, 3, 0, headers.length - 1));

            workbook.write(response.getOutputStream());
        }
    }
}