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
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
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
    protected SupplierResponse findById(Long id) {
        return supplierService.findById(id);
    }

    @Override
    protected Long createEntityAndGetId(SupplierRequest request) {
        SupplierResponse createdEntity = supplierService.create(request);
        return createdEntity.getId();
    }

    @Override
    protected void createEntity(SupplierRequest request) {
        createEntityAndGetId(request);
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
    protected void setRequestId(SupplierRequest request, Long id) {
        request.setId(id);
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

    @GetMapping("/import/template")
    public void generateTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=supplier_template.xlsx");

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("仕入先データ");

            // ヘッダー行の作成
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "仕入先コード*", "仕入先名*", "フリガナ", "郵便番号", "住所",
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
                "SUP001", "株式会社サプライヤー", "カブシキガイシャサプライヤー", "123-4567",
                "東京都千代田区...", "03-1234-5678", "03-1234-5679", "contact@supplier.com",
                "鈴木一郎", "月末締め翌月末払い", "有効", "備考欄"
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

            // 入力規則の設定（ステータス列）
            DataValidationHelper validationHelper = sheet.getDataValidationHelper();
            CellRangeAddressList statusRange = new CellRangeAddressList(1, 1000, 10, 10); // B2:B1000
            DataValidationConstraint statusConstraint = validationHelper.createExplicitListConstraint(new String[]{"有効", "無効"});
            DataValidation statusValidation = validationHelper.createValidation(statusConstraint, statusRange);
            statusValidation.setShowErrorBox(true);
            statusValidation.setErrorStyle(DataValidation.ErrorStyle.STOP);
            statusValidation.createErrorBox("入力エラー", "「有効」または「無効」を選択してください。");
            sheet.addValidationData(statusValidation);

            workbook.write(response.getOutputStream());
        }
    }
}