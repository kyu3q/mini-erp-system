package com.minierpapp.controller;

import com.minierpapp.model.warehouse.dto.WarehouseDto;
import com.minierpapp.model.warehouse.dto.WarehouseRequest;
import com.minierpapp.service.ExcelExportService;
import com.minierpapp.service.ExcelImportService;
import com.minierpapp.service.WarehouseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

@Controller
@RequestMapping("/warehouses")
@RequiredArgsConstructor
public class WarehouseWebController {

    private final WarehouseService warehouseService;
    private final ExcelExportService excelExportService;
    private final ExcelImportService excelImportService;

    @GetMapping
    public String list(@PageableDefault Pageable pageable, Model model) {
        Page<WarehouseDto> warehouses = warehouseService.findAll(pageable);
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
        WarehouseDto warehouse = warehouseService.findById(id);
        WarehouseRequest request = new WarehouseRequest();
        request.setId(id);  // IDを設定
        request.setWarehouseCode(warehouse.getWarehouseCode());
        request.setName(warehouse.getName());
        request.setAddress(warehouse.getAddress());
        request.setCapacity(warehouse.getCapacity());
        request.setStatus(warehouse.getStatus());
        request.setDescription(warehouse.getDescription());
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
    public ResponseEntity<ByteArrayResource> downloadTemplate() throws IOException {
        byte[] data = excelImportService.createImportTemplate();
        ByteArrayResource resource = new ByteArrayResource(data);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=warehouse_import_template.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(data.length)
                .body(resource);
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