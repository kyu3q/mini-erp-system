package com.minierpapp.controller;

import com.minierpapp.controller.base.BaseWebController;
import com.minierpapp.dto.excel.ImportResult;
import com.minierpapp.model.price.entity.PriceCondition;
import com.minierpapp.model.price.dto.PurchasePriceDto;
import com.minierpapp.model.price.dto.PurchasePriceRequest;
import com.minierpapp.model.price.dto.PurchasePriceResponse;
import com.minierpapp.service.CustomerService;
import com.minierpapp.service.ItemService;
import com.minierpapp.service.PriceService;
import com.minierpapp.service.PurchasePriceService;
import com.minierpapp.service.SupplierService;
import com.minierpapp.service.excel.PriceExcelService;
import com.minierpapp.model.price.mapper.PriceConditionMapper;
import com.minierpapp.model.price.mapper.PurchasePriceMapper;

import org.springframework.context.MessageSource;
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
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/prices/purchase")
public class PurchasePriceController extends BaseWebController<PriceCondition, PurchasePriceDto, PurchasePriceRequest, PurchasePriceResponse> {
    private final PurchasePriceService purchasePriceService;
    private final PriceService priceService;
    private final ItemService itemService;
    private final SupplierService supplierService;
    private final CustomerService customerService;
    private final PriceExcelService priceExcelService;
    private final PriceConditionMapper priceConditionMapper;

    public PurchasePriceController(
            PurchasePriceService purchasePriceService,
            PriceService priceService,
            ItemService itemService,
            SupplierService supplierService,
            CustomerService customerService,
            PurchasePriceMapper mapper,
            MessageSource messageSource,
            PriceExcelService priceExcelService,
            PriceConditionMapper priceConditionMapper) {
        super(mapper, messageSource, "prices/purchase", "PurchasePrice");
        this.purchasePriceService = purchasePriceService;
        this.priceService = priceService;
        this.itemService = itemService;
        this.supplierService = supplierService;
        this.customerService = customerService;
        this.priceExcelService = priceExcelService;
        this.priceConditionMapper = priceConditionMapper;
    }

    @Override
    protected List<PurchasePriceResponse> findAll() {
        return purchasePriceService.findAll();
    }

    @Override
    protected PurchasePriceRequest createNewRequest() {
        PurchasePriceRequest request = new PurchasePriceRequest();
        request.setPriceScales(new ArrayList<>());
        return request;
    }

    @Override
    protected PurchasePriceResponse findById(Long id) {
        return purchasePriceService.findById(id);
    }

    @Override
    protected void createEntity(PurchasePriceRequest request) {
        purchasePriceService.create(request);
    }

    @Override
    protected void updateEntity(Long id, PurchasePriceRequest request) {
        purchasePriceService.update(id, request);
    }

    @Override
    protected void deleteEntity(Long id) {
        purchasePriceService.delete(id);
    }

    @Override
    protected void prepareForm(Model model, PurchasePriceRequest request) {
        super.prepareForm(model, request);
        
        // フォーム用の追加データを準備
        model.addAttribute("items", itemService.findAllActive());
        model.addAttribute("suppliers", supplierService.findAllActive());
        
        // 編集時に関連エンティティの情報を追加
        if (request.getId() != null) {
            if (request.getItemId() != null) {
                model.addAttribute("item", itemService.findById(request.getItemId()));
            }
            if (request.getSupplierId() != null) {
                model.addAttribute("supplier", supplierService.findById(request.getSupplierId()));
            }
        }
    }

    @GetMapping("/search")
    public String search(
            @RequestParam(required = false) Long itemId,
            @RequestParam(required = false) Long supplierId,
            Model model) {
        model.addAttribute("prices", purchasePriceService.search(itemId, supplierId));
        model.addAttribute("items", itemService.findAllActive());
        model.addAttribute("suppliers", supplierService.findAllActive());
        model.addAttribute("selectedItemId", itemId);
        model.addAttribute("selectedSupplierId", supplierId);
        return getListTemplate();
    }

    @GetMapping("/excel/export")
    public ResponseEntity<ByteArrayResource> exportPurchasePrices() {
        try {
            List<PriceCondition> prices = priceConditionMapper.responsesToEntities(
                priceService.findAllPurchasePrices());

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

    @PostMapping("/excel/import")
    @ResponseBody
    public ImportResult importPurchasePrices(@RequestParam("file") MultipartFile file) {
        return priceExcelService.importPurchasePrices(file);
    }

    @GetMapping("/excel/template")
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