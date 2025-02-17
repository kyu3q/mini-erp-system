package com.minierpapp.controller;

import com.minierpapp.controller.base.BaseWebController;
import com.minierpapp.model.item.Item;
import com.minierpapp.model.item.dto.ItemDto;
import com.minierpapp.model.item.dto.ItemRequest;
import com.minierpapp.model.item.dto.ItemResponse;
import com.minierpapp.model.item.mapper.ItemMapper;
import com.minierpapp.service.ItemService;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import com.minierpapp.service.ExcelExportService;
import com.minierpapp.service.ExcelImportService;

@Controller
@RequestMapping("/items")
public class ItemWebController extends BaseWebController<Item, ItemDto, ItemRequest, ItemResponse> {

    private final ItemService itemService;
    private final ExcelExportService excelExportService;
    private final ExcelImportService excelImportService;

    public ItemWebController(
            ItemService itemService,
            ExcelExportService excelExportService,
            ExcelImportService excelImportService,
            ItemMapper mapper,
            MessageSource messageSource) {
        super(mapper, messageSource, "item", "Item");
        this.itemService = itemService;
        this.excelExportService = excelExportService;
        this.excelImportService = excelImportService;
    }

    @Override
    @GetMapping
    public String list(
            @RequestParam(required = false) String searchParam1,
            @RequestParam(required = false) String searchParam2,
            Model model) {
        String itemCode = searchParam1;
        String itemName = searchParam2;
        if ((itemCode != null && !itemCode.trim().isEmpty()) ||
            (itemName != null && !itemName.trim().isEmpty())) {
            model.addAttribute("items", itemService.search(itemCode, itemName));
            model.addAttribute("itemCode", itemCode);
            model.addAttribute("itemName", itemName);
        } else {
            model.addAttribute("items", findAll());
        }
        return getListTemplate();
    }

    @Override
    protected List<ItemResponse> findAll() {
        return itemService.findAll().stream()
                .map(mapper::entityToResponse)
                .toList();
    }

    @Override
    protected ItemRequest createNewRequest() {
        return new ItemRequest();
    }

    @Override
    protected ItemResponse findById(Long id) {
        return itemService.findById(id);
    }

    @Override
    protected void createEntity(ItemRequest request) {
        itemService.create(request);
    }

    @Override
    protected void updateEntity(Long id, ItemRequest request) {
        itemService.update(id, request);
    }

    @Override
    protected void deleteEntity(Long id) {
        itemService.delete(id);
    }

    @GetMapping("/excel/export")
    public void exportToExcel(HttpServletResponse response, 
                            @RequestParam(required = false) String searchParam1,
                            @RequestParam(required = false) String searchParam2) throws IOException {
        excelExportService.exportItems(response, searchParam1, searchParam2);
    }

    @GetMapping("/excel/template")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        excelExportService.downloadItemTemplate(response);
    }

    @PostMapping("/import")
    public String importFile(@RequestParam("file") MultipartFile file,
                           RedirectAttributes redirectAttributes) {
        try {
            List<String> errors = excelImportService.importItems(file);
            if (errors.isEmpty()) {
                addSuccessMessage(redirectAttributes, "common.imported");
            } else {
                addErrorMessage(redirectAttributes, "error.import", String.join(", ", errors));
            }
        } catch (Exception e) {
            addErrorMessage(redirectAttributes, "error.import.failed", e.getMessage());
        }
        return getRedirectToList();
    }

    @Override
    protected void handleError(BindingResult result, Exception e) {
        if (e.getMessage().contains("品目コード")) {
            result.rejectValue("itemCode", "error.itemCode", e.getMessage());
        } else if (e.getMessage().contains("最小在庫数")) {
            result.rejectValue("minimumStock", "error.minimumStock", e.getMessage());
        } else if (e.getMessage().contains("最大在庫数")) {
            result.rejectValue("maximumStock", "error.maximumStock", e.getMessage());
        } else if (e.getMessage().contains("発注点")) {
            result.rejectValue("reorderPoint", "error.reorderPoint", e.getMessage());
        } else {
            super.handleError(result, e);
        }
    }

    @GetMapping("/import/template")
    public void generateTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=item_template.xlsx");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("品目データ");

            // ヘッダー行の作成
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "品目コード*", "品目名*", "フリガナ", "型番", "単位",
                "単価", "ステータス*", "備考"
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
                "ITEM001", "サンプル品目", "サンプルヒンモク", "ABC-123", "個",
                "1000", "有効", "備考欄"
            };

            for (int i = 0; i < sampleData.length; i++) {
                Cell cell = sampleRow.createCell(i);
                cell.setCellValue(sampleData[i]);
                cell.setCellStyle(dataStyle);
            }

            // 注意書き行の追加
            Row noteRow = sheet.createRow(3);
            Cell noteCell = noteRow.createCell(0);
            noteCell.setCellValue("注意: *は必須項目です。ステータスは「有効」または「無効」を入力してください。単価は数値で入力してください。");
            CellStyle noteStyle = workbook.createCellStyle();
            Font noteFont = workbook.createFont();
            noteFont.setColor(IndexedColors.RED.getIndex());
            noteStyle.setFont(noteFont);
            noteCell.setCellStyle(noteStyle);

            // セルの結合（注意書き用）
            sheet.addMergedRegion(new CellRangeAddress(3, 3, 0, headers.length - 1));

            // 入力規則の設定（ステータス列）
            DataValidationHelper validationHelper = sheet.getDataValidationHelper();
            CellRangeAddressList statusRange = new CellRangeAddressList(1, 1000, 6, 6);
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