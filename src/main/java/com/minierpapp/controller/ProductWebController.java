package com.minierpapp.controller;

import com.minierpapp.model.product.Product;
import com.minierpapp.model.product.dto.ProductRequest;
import com.minierpapp.model.product.mapper.ProductMapper;
import com.minierpapp.service.ExcelExportService;
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
        // バリデーションエラーがある場合は、そのままエラーを表示
        if (result.hasErrors()) {
            return "products/form";
        }

        try {
            productService.create(request);
            redirectAttributes.addFlashAttribute("message", "商品を登録しました。");
            return "redirect:/products";
        } catch (IllegalArgumentException e) {
            // ビジネスロジックのバリデーションエラーを追加
            if (e.getMessage().contains("商品コード")) {
                result.rejectValue("productCode", "error.productCode", e.getMessage());
            } else if (e.getMessage().contains("最小在庫数")) {
                result.rejectValue("minimumStock", "error.minimumStock", e.getMessage());
            } else if (e.getMessage().contains("最大在庫数")) {
                result.rejectValue("maximumStock", "error.maximumStock", e.getMessage());
            } else if (e.getMessage().contains("発注点")) {
                result.rejectValue("reorderPoint", "error.reorderPoint", e.getMessage());
            } else {
                // その他のエラー
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
        request.setVersion(product.getVersion());
        model.addAttribute("product", request);
        return "products/form";
    }

    @PutMapping("/{id}")
    public String update(@PathVariable Long id,
                        @Valid @ModelAttribute("product") ProductRequest request,
                        BindingResult result,
                        RedirectAttributes redirectAttributes) {
        // バリデーションエラーがある場合は、そのままエラーを表示
        if (result.hasErrors()) {
            return "products/form";
        }

        try {
            productService.update(id, request);
            redirectAttributes.addFlashAttribute("message", "商品を更新しました。");
            return "redirect:/products";
        } catch (IllegalArgumentException e) {
            // ビジネスロジックのバリデーションエラーを追加
            if (e.getMessage().contains("商品コード")) {
                result.rejectValue("productCode", "error.productCode", e.getMessage());
            } else if (e.getMessage().contains("最小在庫数")) {
                result.rejectValue("minimumStock", "error.minimumStock", e.getMessage());
            } else if (e.getMessage().contains("最大在庫数")) {
                result.rejectValue("maximumStock", "error.maximumStock", e.getMessage());
            } else if (e.getMessage().contains("発注点")) {
                result.rejectValue("reorderPoint", "error.reorderPoint", e.getMessage());
            } else {
                // その他のエラー
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
}