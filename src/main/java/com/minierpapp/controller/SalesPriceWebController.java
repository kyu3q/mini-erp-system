package com.minierpapp.controller;

import com.minierpapp.controller.base.BaseWebController;
import com.minierpapp.dto.excel.ImportResult;
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
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import com.minierpapp.model.price.entity.SalesPrice;
import com.minierpapp.service.ItemService;
import com.minierpapp.service.CustomerService;
import com.minierpapp.model.item.dto.ItemResponse;
import com.minierpapp.model.customer.dto.CustomerResponse;

@Controller
@RequestMapping("/prices/sales")
public class SalesPriceWebController extends BaseWebController<SalesPrice, SalesPriceDto, SalesPriceRequest, SalesPriceResponse> {

    private final SalesPriceService salesPriceService;
    private final PriceExcelService priceExcelService;
    private final SalesPriceMapper salesPriceMapper;
    private final ItemService itemService;
    private final CustomerService customerService;

    public SalesPriceWebController(
            SalesPriceService salesPriceService,
            SalesPriceMapper mapper,
            MessageSource messageSource,
            PriceExcelService priceExcelService,
            SalesPriceMapper salesPriceMapper,
            ItemService itemService,
            CustomerService customerService) {
        super(mapper, messageSource, "price/sales", "SalesPrice");
        this.salesPriceService = salesPriceService;
        this.priceExcelService = priceExcelService;
        this.salesPriceMapper = salesPriceMapper;
        this.itemService = itemService;
        this.customerService = customerService;
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
        validateAndSetIds(request);
        
        System.out.println("Controller: After validation - itemId=" + request.getItemId() 
            + ", itemCode=" + request.getItemCode()
            + ", customerId=" + request.getCustomerId()
            + ", customerCode=" + request.getCustomerCode());
        
        salesPriceService.create(request);
    }

    @Override
    protected void updateEntity(Long id, SalesPriceRequest request) {
        validateAndSetIds(request);
        System.out.println("Updating entity with itemId: " + request.getItemId() + ", customerId: " + request.getCustomerId());
        salesPriceService.update(id, request);
    }

    @Override
    protected void deleteEntity(Long id) {
        salesPriceService.delete(id);
    }

    @GetMapping("/excel/export")
    public ResponseEntity<ByteArrayResource> exportSalesPrices() {
        try {
            List<SalesPrice> salesPrices = salesPriceMapper.responsesToEntities(
                salesPriceService.findAll());

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            priceExcelService.exportSalesPrices(salesPrices, out);

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

    /**
     * リクエストのコード値からIDを設定する
     */
    private void validateAndSetIds(SalesPriceRequest request) {
        // 品目IDの設定
        if (request.getItemId() == null && request.getItemCode() != null && !request.getItemCode().isEmpty()) {
            ItemResponse item = itemService.findByItemCode(request.getItemCode());
            if (item != null) {
                request.setItemId(item.getId());
            } else {
                throw new IllegalArgumentException("品目コード " + request.getItemCode() + " が見つかりません");
            }
        }
        
        // 必須フィールドの検証
        if (request.getItemId() == null) {
            throw new IllegalArgumentException("品目IDは必須です");
        }
        
        // 得意先IDの設定
        if (request.getCustomerId() == null && request.getCustomerCode() != null && !request.getCustomerCode().isEmpty()) {
            CustomerResponse customer = customerService.findByCustomerCode(request.getCustomerCode());
            if (customer != null) {
                request.setCustomerId(customer.getId());
            } else {
                throw new IllegalArgumentException("得意先コード " + request.getCustomerCode() + " が見つかりません");
            }
        }
        
        // 必須フィールドの検証
        if (request.getCustomerId() == null) {
            throw new IllegalArgumentException("得意先IDは必須です");
        }
    }

    @Override
    protected String getListAttributeName() {
        return "prices";
    }

    @Override
    @GetMapping
    public String list(
            @RequestParam(required = false) String searchParam1,
            @RequestParam(required = false) String searchParam2,
            Model model) {
        List<SalesPriceResponse> prices = findAll();
        model.addAttribute("prices", prices);
        return getListTemplate();
    }
}