package com.minierpapp.controller;

import com.minierpapp.model.warehouse.dto.WarehouseDto;
import com.minierpapp.model.warehouse.dto.WarehouseRequest;
import com.minierpapp.service.ExcelExportService;
import com.minierpapp.service.ExcelImportService;
import com.minierpapp.service.WarehouseService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.minierpapp.model.warehouse.mapper.WarehouseMapper;
import com.minierpapp.model.warehouse.Warehouse;

@Controller
@RequestMapping("/warehouses")
public class WarehouseWebController {

    private final WarehouseService warehouseService;
    private final WarehouseMapper warehouseMapper;
    private final ExcelExportService excelExportService;
    private final ExcelImportService excelImportService;

    public WarehouseWebController(WarehouseService warehouseService, 
                                 WarehouseMapper warehouseMapper,
                                 ExcelExportService excelExportService,
                                 ExcelImportService excelImportService) {
        this.warehouseService = warehouseService;
        this.warehouseMapper = warehouseMapper;
        this.excelExportService = excelExportService;
        this.excelImportService = excelImportService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String warehouseCode,
                      @RequestParam(required = false) String name,
                      @PageableDefault Pageable pageable,
                      Model model) {
        Page<WarehouseDto> warehouses;
        if (warehouseCode != null || name != null) {
            warehouses = warehouseService.search(warehouseCode, name, pageable);
        } else {
            warehouses = warehouseService.findAll(pageable);
        }
        model.addAttribute("warehouses", warehouses);
        return "warehouse/list";
    }

    @GetMapping("/new")
    public String newWarehouse(Model model) {
        model.addAttribute("warehouse", new WarehouseRequest());
        return "warehouse/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("warehouse") WarehouseRequest request,
                        BindingResult result,
                        RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "warehouse/form";
        }

        try {
            warehouseService.create(request);
            redirectAttributes.addFlashAttribute("message", "倉庫を登録しました。");
            return "redirect:/warehouses";
        } catch (IllegalArgumentException e) {
            result.rejectValue("warehouseCode", "error.warehouseCode", e.getMessage());
            return "warehouse/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        Warehouse warehouse = warehouseService.findById(id);
        WarehouseDto warehouseDto = warehouseMapper.toDto(warehouse);
        WarehouseRequest request = new WarehouseRequest();
        request.setId(id);
        request.setWarehouseCode(warehouseDto.getWarehouseCode());
        request.setName(warehouseDto.getName());
        request.setAddress(warehouseDto.getAddress());
        request.setCapacity(warehouseDto.getCapacity());
        request.setStatus(warehouseDto.getStatus());
        request.setDescription(warehouseDto.getDescription());
        model.addAttribute("warehouse", request);
        return "warehouse/form";
    }

    @PutMapping("/{id}")
    public String update(@PathVariable Long id,
                        @Valid @ModelAttribute("warehouse") WarehouseRequest request,
                        BindingResult result,
                        RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "warehouse/form";
        }

        try {
            warehouseService.update(id, request);
            redirectAttributes.addFlashAttribute("message", "倉庫を更新しました。");
            return "redirect:/warehouses";
        } catch (IllegalArgumentException e) {
            result.rejectValue("warehouseCode", "error.warehouseCode", e.getMessage());
            return "warehouse/form";
        }
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        warehouseService.delete(id);
        redirectAttributes.addFlashAttribute("message", "倉庫を削除しました。");
        return "redirect:/warehouses";
    }

    @GetMapping("/export")
    public ResponseEntity<ByteArrayResource> export() throws IOException {
        byte[] data = excelExportService.exportWarehouses();
        ByteArrayResource resource = new ByteArrayResource(data);
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String filename = String.format("warehouses_%s.xlsx", timestamp);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(data.length)
                .body(resource);
    }

    @GetMapping("/import/template")
    public void generateTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=warehouse_template.xlsx");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("倉庫データ");

            // ヘッダー行の作成
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "倉庫コード*", "倉庫名*", "フリガナ", "郵便番号", "住所",
                "電話番号", "FAX", "管理者", "ステータス*", "備考"
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
                "WH001", "第一倉庫", "ダイイチソウコ", "123-4567",
                "東京都千代田区...", "03-1234-5678", "03-1234-5679",
                "山田太郎", "有効", "備考欄"
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
            CellRangeAddressList statusRange = new CellRangeAddressList(1, 1000, 8, 8);
            DataValidationConstraint statusConstraint = validationHelper.createExplicitListConstraint(new String[]{"有効", "無効"});
            DataValidation statusValidation = validationHelper.createValidation(statusConstraint, statusRange);
            statusValidation.setShowErrorBox(true);
            statusValidation.setErrorStyle(DataValidation.ErrorStyle.STOP);
            statusValidation.createErrorBox("入力エラー", "「有効」または「無効」を選択してください。");
            sheet.addValidationData(statusValidation);

            workbook.write(response.getOutputStream());
        }
    }

    @PostMapping("/import")
    public String importFile(@RequestParam("file") MultipartFile file,
                           RedirectAttributes redirectAttributes) {
        try {
            List<String> errors = excelImportService.importWarehouses(file);
            if (errors.isEmpty()) {
                redirectAttributes.addFlashAttribute("message", "倉庫データを取り込みました。");
            } else {
                redirectAttributes.addFlashAttribute("error",
                        String.format("取り込み時にエラーが発生しました：<br>%s",
                                String.join("<br>", errors)));
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "ファイルの取り込みに失敗しました：" + e.getMessage());
        }
        return "redirect:/warehouses";
    }
}