package com.minierpapp.controller;

import com.minierpapp.dto.excel.ImportResult;
import com.minierpapp.model.price.PriceCondition;
import com.minierpapp.service.PriceService;
import com.minierpapp.service.excel.PriceExcelService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/prices")
@RequiredArgsConstructor
public class PriceController {
    private final PriceService priceService;
    private final PriceExcelService priceExcelService;

    // 販売単価一覧
    @GetMapping("/sales")
    public String listSalesPrices(Model model) {
        List<PriceCondition> prices = priceService.findAllSalesPrices();
        model.addAttribute("prices", prices);
        return "price/sales/list";
    }

    // 購買単価一覧
    @GetMapping("/purchase")
    public String listPurchasePrices(Model model) {
        List<PriceCondition> prices = priceService.findAllPurchasePrices();
        model.addAttribute("prices", prices);
        return "price/purchase/list";
    }

    // 販売単価Excel出力
    @GetMapping("/sales/export")
    public ResponseEntity<ByteArrayResource> exportSalesPrices() {
        try {
            List<PriceCondition> prices = priceService.findAllSalesPrices();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            priceExcelService.exportSalesPrices(prices, out);

            String filename = "sales_price_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";

            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new ByteArrayResource(out.toByteArray()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 購買単価Excel出力
    @GetMapping("/purchase/export")
    public ResponseEntity<ByteArrayResource> exportPurchasePrices() {
        try {
            List<PriceCondition> prices = priceService.findAllPurchasePrices();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            priceExcelService.exportPurchasePrices(prices, out);

            String filename = "purchase_price_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";

            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new ByteArrayResource(out.toByteArray()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 販売単価Excel取込
    @PostMapping("/sales/import")
    @ResponseBody
    public ImportResult importSalesPrices(@RequestParam("file") MultipartFile file) {
        return priceExcelService.importSalesPrices(file);
    }

    // 購買単価Excel取込
    @PostMapping("/purchase/import")
    @ResponseBody
    public ImportResult importPurchasePrices(@RequestParam("file") MultipartFile file) {
        return priceExcelService.importPurchasePrices(file);
    }

    // 販売単価テンプレート
    @GetMapping("/sales/template")
    public ResponseEntity<ByteArrayResource> downloadSalesTemplate() {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            priceExcelService.exportSalesPrices(List.of(), out);

            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=sales_price_template.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new ByteArrayResource(out.toByteArray()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 購買単価テンプレート
    @GetMapping("/purchase/template")
    public ResponseEntity<ByteArrayResource> downloadPurchaseTemplate() {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            priceExcelService.exportPurchasePrices(List.of(), out);

            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=purchase_price_template.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new ByteArrayResource(out.toByteArray()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}