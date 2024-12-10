package com.minierpapp.controller;

import com.minierpapp.model.item.Item;
import com.minierpapp.model.item.dto.ItemRequest;
import com.minierpapp.service.ExcelExportService;
import com.minierpapp.service.ExcelImportService;
import com.minierpapp.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

@Controller
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemWebController {

    private final ItemService itemService;
    private final ExcelExportService excelExportService;
    private final ExcelImportService excelImportService;

    @GetMapping
    public String list(
            @RequestParam(required = false) String itemCode,
            @RequestParam(required = false) String itemName,
            Model model) {
        if ((itemCode != null && !itemCode.trim().isEmpty()) ||
            (itemName != null && !itemName.trim().isEmpty())) {
            model.addAttribute("items", itemService.search(itemCode, itemName));
            model.addAttribute("itemCode", itemCode);
            model.addAttribute("itemName", itemName);
        } else {
            model.addAttribute("items", itemService.findAll());
        }
        return "items/list";
    }

    @GetMapping("/new")
    public String newItem(Model model) {
        model.addAttribute("item", new ItemRequest());
        return "items/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("item") ItemRequest request,
                        BindingResult result,
                        RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "items/form";
        }

        try {
            itemService.create(request);
            redirectAttributes.addFlashAttribute("message", "商品を登録しました。");
            return "redirect:/items";
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("商品コード")) {
                result.rejectValue("itemCode", "error.itemCode", e.getMessage());
            } else if (e.getMessage().contains("最小在庫数")) {
                result.rejectValue("minimumStock", "error.minimumStock", e.getMessage());
            } else if (e.getMessage().contains("最大在庫数")) {
                result.rejectValue("maximumStock", "error.maximumStock", e.getMessage());
            } else if (e.getMessage().contains("発注点")) {
                result.rejectValue("reorderPoint", "error.reorderPoint", e.getMessage());
            } else {
                result.reject("error.global", e.getMessage());
            }
            return "items/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        Item item = itemService.findById(id);
        ItemRequest request = new ItemRequest();
        request.setId(id);
        request.setItemCode(item.getItemCode());
        request.setItemName(item.getItemName());
        request.setDescription(item.getDescription());
        request.setUnit(item.getUnit());
        request.setStatus(item.getStatus());
        request.setMinimumStock(item.getMinimumStock());
        request.setMaximumStock(item.getMaximumStock());
        request.setReorderPoint(item.getReorderPoint());
        model.addAttribute("item", request);
        return "items/form";
    }

    @PutMapping("/{id}")
    public String update(@PathVariable Long id,
                        @Valid @ModelAttribute("item") ItemRequest request,
                        BindingResult result,
                        RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "items/form";
        }

        try {
            itemService.update(id, request);
            redirectAttributes.addFlashAttribute("message", "商品を更新しました。");
            return "redirect:/items";
        } catch (IllegalArgumentException | IllegalStateException e) {
            if (e.getMessage().contains("商品コード")) {
                result.rejectValue("itemCode", "error.itemCode", e.getMessage());
            } else if (e.getMessage().contains("最小在庫数")) {
                result.rejectValue("minimumStock", "error.minimumStock", e.getMessage());
            } else if (e.getMessage().contains("最大在庫数")) {
                result.rejectValue("maximumStock", "error.maximumStock", e.getMessage());
            } else if (e.getMessage().contains("発注点")) {
                result.rejectValue("reorderPoint", "error.reorderPoint", e.getMessage());
            } else {
                result.reject("error.global", e.getMessage());
            }
            return "items/form";
        }
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        itemService.delete(id);
        redirectAttributes.addFlashAttribute("message", "商品を削除しました。");
        return "redirect:/items";
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportToExcel(
            @RequestParam(required = false) String itemCode,
            @RequestParam(required = false) String itemName) throws IOException {
        
        List<Item> items;
        if ((itemCode != null && !itemCode.trim().isEmpty()) ||
            (itemName != null && !itemName.trim().isEmpty())) {
            items = itemService.search(itemCode, itemName);
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
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "ファイルを選択してください。");
            return "redirect:/items";
        }

        if (!file.getOriginalFilename().toLowerCase().endsWith(".xlsx")) {
            redirectAttributes.addFlashAttribute("error", "Excelファイル(.xlsx)を選択してください。");
            return "redirect:/items";
        }

        try {
            List<String> errors = excelImportService.importItems(file);
            
            if (errors.isEmpty()) {
                redirectAttributes.addFlashAttribute("message", "商品データを取り込みました。");
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