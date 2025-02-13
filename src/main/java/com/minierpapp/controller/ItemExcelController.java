package com.minierpapp.controller;

import com.minierpapp.model.item.Item;
import com.minierpapp.service.ExcelExportService;
import com.minierpapp.service.ExcelImportService;
import com.minierpapp.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/items/excel")
@RequiredArgsConstructor
public class ItemExcelController {

    private final ItemService itemService;
    private final ExcelExportService excelExportService;
    private final ExcelImportService excelImportService;

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportToExcel(
            @RequestParam(required = false) String searchParam1,
            @RequestParam(required = false) String searchParam2) throws IOException {
        
        List<Item> items;
        if ((searchParam1 != null && !searchParam1.trim().isEmpty()) ||
            (searchParam2 != null && !searchParam2.trim().isEmpty())) {
            items = itemService.search(searchParam1, searchParam2);
        } else {
            items = itemService.findAll();
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String filename = String.format("items_%s.xlsx", timestamp);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", filename);

        byte[] excelBytes = excelExportService.exportItemsToExcel(items);
        return ResponseEntity.ok()
                .headers(headers)
                .body(excelBytes);
    }

    @PostMapping("/import")
    public String importFromExcel(@RequestParam("file") MultipartFile file,
                              RedirectAttributes redirectAttributes) {
        String filename = file.getOriginalFilename();
        if (file.isEmpty() || filename == null) {
            redirectAttributes.addFlashAttribute("error", "ファイルを選択してください。");
            return "redirect:/items";
        }

        if (!filename.toLowerCase().endsWith(".xlsx")) {
            redirectAttributes.addFlashAttribute("error", "Excelファイル(.xlsx)を選択してください。");
            return "redirect:/items";
        }

        try {
            List<String> errors = excelImportService.importItems(file);
            
            if (errors.isEmpty()) {
                redirectAttributes.addFlashAttribute("message", "品目データを取り込みました。");
            } else {
                StringBuilder errorMessage = new StringBuilder("取り込み時にエラーが発生しました：<br>");
                errors.forEach(error -> errorMessage.append(error).append("<br>"));
                redirectAttributes.addFlashAttribute("error", errorMessage.toString());
            }
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "ファイルの読み込みに失敗しました。");
        }
        
        return "redirect:/items";
    }

    @GetMapping("/import/template")
    public ResponseEntity<byte[]> downloadImportTemplate() throws IOException {
        String filename = "item_import_template.xlsx";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", filename);

        byte[] templateBytes = excelImportService.createImportTemplate();
        return ResponseEntity.ok()
                .headers(headers)
                .body(templateBytes);
    }
}