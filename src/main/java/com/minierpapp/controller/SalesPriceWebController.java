package com.minierpapp.controller;

import com.minierpapp.controller.base.BaseWebController;
import com.minierpapp.dto.excel.ImportResult;
import com.minierpapp.model.price.entity.PriceCondition;
import com.minierpapp.model.price.dto.SalesPriceDto;
import com.minierpapp.model.price.dto.SalesPriceRequest;
import com.minierpapp.model.price.dto.SalesPriceResponse;
import com.minierpapp.service.excel.PriceExcelService;
import com.minierpapp.service.SalesPriceService;
import com.minierpapp.model.price.mapper.SalesPriceMapper;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import com.minierpapp.service.PriceService;
import com.minierpapp.model.price.dto.PriceConditionResponse;
import com.minierpapp.model.price.mapper.PriceConditionMapper;

@Controller
@RequestMapping("/prices/sales")
public class SalesPriceWebController extends BaseWebController<PriceCondition, SalesPriceDto, SalesPriceRequest, SalesPriceResponse> {

    private final SalesPriceService salesPriceService;
    private final PriceService priceService;
    private final PriceExcelService priceExcelService;
    private final PriceConditionMapper priceConditionMapper;

    public SalesPriceWebController(
            SalesPriceService salesPriceService,
            PriceService priceService,
            SalesPriceMapper mapper,
            MessageSource messageSource,
            PriceExcelService priceExcelService,
            PriceConditionMapper priceConditionMapper) {
        super(mapper, messageSource, "price/sales", "SalesPrice");
        this.salesPriceService = salesPriceService;
        this.priceService = priceService;
        this.priceExcelService = priceExcelService;
        this.priceConditionMapper = priceConditionMapper;
    }

    @Override
    protected List<SalesPriceResponse> findAll() {
        return salesPriceService.findAll();
    }

    @Override
    protected SalesPriceRequest createNewRequest() {
        SalesPriceRequest request = new SalesPriceRequest();
        request.setPriceScales(new ArrayList<>());
        return request;
    }

    @Override
    protected SalesPriceResponse findById(Long id) {
        return salesPriceService.findById(id);
    }

    @Override
    protected void createEntity(SalesPriceRequest request) {
        salesPriceService.create(request);
    }

    @Override
    protected void updateEntity(Long id, SalesPriceRequest request) {
        salesPriceService.update(id, request);
    }

    @Override
    protected void deleteEntity(Long id) {
        salesPriceService.delete(id);
    }

    @GetMapping("/excel/export")
    public ResponseEntity<ByteArrayResource> exportSalesPrices() {
        try {
            List<PriceConditionResponse> responses = priceService.findAllSalesPrices();
            List<PriceCondition> prices = priceConditionMapper.responsesToEntities(responses);

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

    @PostMapping("/excel/import")
    @ResponseBody
    public ImportResult importSalesPrices(@RequestParam("file") MultipartFile file) {
        return priceExcelService.importSalesPrices(file);
    }

    @GetMapping("/excel/template")
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
}