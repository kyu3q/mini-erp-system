package com.minierpapp.controller;

import com.minierpapp.model.product.Product;
import com.minierpapp.model.product.dto.ProductRequest;
import com.minierpapp.model.product.mapper.ProductMapper;
import com.minierpapp.service.ExcelExportService;
import com.minierpapp.service.ExcelImportService;
import com.minierpapp.service.ProductService;
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
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductWebController {

    private final ProductService productService;
    private final ProductMapper productMapper;
    private final ExcelExportService excelExportService;
    private final ExcelImportService excelImportService;

    @GetMapping
    public String list(
            @RequestParam(required = false) String productCode,
            @RequestParam(required = false) String productName,
            Model model) {
        if ((productCode != null && !productCode.trim().isEmpty()) ||
            (productName != null && !productName.trim().isEmpty())) {
            model.addAttribute("products", productService.search(productCode, productName));
            model.addAttribute("productCode", productCode);
            model.addAttribute("productName", productName);
        } else {
            model.addAttribute("products", productService.findAll());
        }
        return "products/list";
    }

    @GetMapping("/new")
    public String newProduct(Model model) {
        model.addAttribute("product", new ProductRequest());
        return "products/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("product") ProductRequest request,
                        BindingResult result,
                        RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "products/form";
        }

        try {
            productService.create(request);
            redirectAttributes.addFlashAttribute("message", "商品を登録しました。");
            return "redirect:/products";
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("商品コード")) {
                result.rejectValue("productCode", "error.productCode", e.getMessage());
            } else if (e.getMessage().contains("最小在庫数")) {
                result.rejectValue("minimumStock", "error.minimumStock", e.getMessage());
            } else if (e.getMessage().contains("最大在庫数")) {
                result.rejectValue("maximumStock", "error.maximumStock", e.getMessage());
            } else if (e.getMessage().contains("発注点")) {
                result.rejectValue("reorderPoint", "error.reorderPoint", e.getMessage());
            } else {
                result.reject("error.global", e.getMessage());
            }
            return "products/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        Product product = productService.findById(id);
        ProductRequest request = new ProductRequest();
        request.setId(id);
        request.setProductCode(product.getProductCode());
        request.setProductName(product.getProductName());
        request.setDescription(product.getDescription());
        request.setUnit(product.getUnit());
        request.setStatus(product.getStatus());
        request.setMinimumStock(product.getMinimumStock());
        request.setMaximumStock(product.getMaximumStock());
        request.setReorderPoint(product.getReorderPoint());
        model.addAttribute("product", request);
        return "products/form";
    }

    @PutMapping("/{id}")
    public String update(@PathVariable Long id,
                        @Valid @ModelAttribute("product") ProductRequest request,
                        BindingResult result,
                        RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "products/form";
        }

        try {
            productService.update(id, request);
            redirectAttributes.addFlashAttribute("message", "商品を更新しました。");
            return "redirect:/products";
        } catch (IllegalArgumentException | IllegalStateException e) {
            if (e.getMessage().contains("商品コード")) {
                result.rejectValue("productCode", "error.productCode", e.getMessage());
            } else if (e.getMessage().contains("最小在庫数")) {
                result.rejectValue("minimumStock", "error.minimumStock", e.getMessage());
            } else if (e.getMessage().contains("最大在庫数")) {
                result.rejectValue("maximumStock", "error.maximumStock", e.getMessage());
            } else if (e.getMessage().contains("発注点")) {
                result.rejectValue("reorderPoint", "error.reorderPoint", e.getMessage());
            } else {
                result.reject("error.global", e.getMessage());
            }
            return "products/form";
        }
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productService.delete(id);
        redirectAttributes.addFlashAttribute("message", "商品を削除しました。");
        return "redirect:/products";
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportToExcel(
            @RequestParam(required = false) String productCode,
            @RequestParam(required = false) String productName) throws IOException {
        
        List<Product> products;
        if ((productCode != null && !productCode.trim().isEmpty()) ||
            (productName != null && !productName.trim().isEmpty())) {
            products = productService.search(productCode, productName);
        } else {
            products = productService.findAll();
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String filename = String.format("products_%s.xlsx", timestamp);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", filename);

        byte[] excelBytes = excelExportService.exportProductsToExcel(products);
        return ResponseEntity.ok()
                .headers(headers)
                .body(excelBytes);
    }

    @PostMapping("/import")
    public String importFromExcel(@RequestParam("file") MultipartFile file,
                                RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "ファイルを選択してください。");
            return "redirect:/products";
        }

        if (!file.getOriginalFilename().toLowerCase().endsWith(".xlsx")) {
            redirectAttributes.addFlashAttribute("error", "Excelファイル(.xlsx)を選択してください。");
            return "redirect:/products";
        }

        try {
            List<String> errors = excelImportService.importProducts(file);
            
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
        
        return "redirect:/products";
    }

    @GetMapping("/import/template")
    public ResponseEntity<byte[]> downloadImportTemplate() throws IOException {
        String filename = "product_import_template.xlsx";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", filename);

        byte[] templateBytes = excelImportService.createImportTemplate();
        return ResponseEntity.ok()
                .headers(headers)
                .body(templateBytes);
    }
}