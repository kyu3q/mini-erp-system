package com.minierpapp.controller;

import com.minierpapp.controller.base.BaseWebController;
import com.minierpapp.dto.excel.ImportResult;
import com.minierpapp.model.price.entity.PurchasePrice;
import com.minierpapp.model.customer.dto.CustomerResponse;
import com.minierpapp.model.item.dto.ItemResponse;
import com.minierpapp.model.price.dto.PurchasePriceDto;
import com.minierpapp.model.price.dto.PurchasePriceRequest;
import com.minierpapp.model.price.dto.PurchasePriceResponse;
import com.minierpapp.service.CustomerService;
import com.minierpapp.service.ItemService;
import com.minierpapp.service.PurchasePriceService;
import com.minierpapp.service.SupplierService;
import com.minierpapp.service.excel.PriceExcelService;
import com.minierpapp.model.price.mapper.PurchasePriceMapper;
import com.minierpapp.model.supplier.dto.SupplierResponse;

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
public class PurchasePriceWebController extends BaseWebController<PurchasePrice, PurchasePriceDto, PurchasePriceRequest, PurchasePriceResponse> {
    private final PurchasePriceService purchasePriceService;
    private final ItemService itemService;
    private final SupplierService supplierService;
    private final CustomerService customerService;
    private final PriceExcelService priceExcelService;
    private final PurchasePriceMapper purchasePriceMapper;

    public PurchasePriceWebController(
            PurchasePriceService purchasePriceService,
            ItemService itemService,
            SupplierService supplierService,
            CustomerService customerService,
            PurchasePriceMapper mapper,
            MessageSource messageSource,
            PriceExcelService priceExcelService) {
        super(mapper, messageSource, "price/purchase", "PurchasePrice");
        this.purchasePriceService = purchasePriceService;
        this.itemService = itemService;
        this.supplierService = supplierService;
        this.customerService = customerService;
        this.priceExcelService = priceExcelService;
        this.purchasePriceMapper = mapper;
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
        System.out.println("===== 購買単価登録処理開始 =====");
        System.out.println("登録前: itemId=" + request.getItemId() + ", itemCode=" + request.getItemCode());
        System.out.println("登録前: supplierId=" + request.getSupplierId() + ", supplierCode=" + request.getSupplierCode());
        System.out.println("登録前: customerId=" + request.getCustomerId() + ", customerCode=" + request.getCustomerCode());
        
        try {
            // コード値からIDへの変換を確認
            validateAndSetIds(request);
            
            System.out.println("変換後: itemId=" + request.getItemId() + ", itemCode=" + request.getItemCode());
            System.out.println("変換後: supplierId=" + request.getSupplierId() + ", supplierCode=" + request.getSupplierCode());
            System.out.println("変換後: customerId=" + request.getCustomerId() + ", customerCode=" + request.getCustomerCode());
            
            purchasePriceService.create(request);
            System.out.println("===== 購買単価登録処理完了 =====");
        } catch (Exception e) {
            System.out.println("購買単価登録処理でエラー発生: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    protected void updateEntity(Long id, PurchasePriceRequest request) {
        validateAndSetIds(request);
        System.out.println("Updating entity with itemId: " + request.getItemId() + ", supplierId: " + request.getSupplierId());
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
            List<PurchasePrice> purchasePrices = purchasePriceMapper.responsesToEntities(
                purchasePriceService.findAll());

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            priceExcelService.exportPurchasePrices(purchasePrices, out);

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

    /**
     * リクエストのコード値からIDを設定する
     */
    private void validateAndSetIds(PurchasePriceRequest request) {
        System.out.println("validateAndSetIds開始");
        
        // 品目IDの設定
        if (request.getItemId() == null && request.getItemCode() != null && !request.getItemCode().isEmpty()) {
            System.out.println("品目コード検索: " + request.getItemCode());
            ItemResponse item = itemService.findByItemCode(request.getItemCode());
            if (item != null) {
                System.out.println("品目が見つかりました: id=" + item.getId() + ", name=" + item.getItemName());
                request.setItemId(item.getId());
            } else {
                System.out.println("品目が見つかりません: code=" + request.getItemCode());
                throw new IllegalArgumentException("品目コード " + request.getItemCode() + " が見つかりません");
            }
        } else {
            System.out.println("品目コード検索をスキップ: itemId=" + request.getItemId() + ", itemCode=" + request.getItemCode());
        }
        
        // 必須フィールドの検証
        if (request.getItemId() == null) {
            System.out.println("品目IDが設定されていません");
            throw new IllegalArgumentException("品目IDは必須です");
        }
        
        // 仕入先IDの設定
        if (request.getSupplierId() == null && request.getSupplierCode() != null && !request.getSupplierCode().isEmpty()) {
            System.out.println("仕入先コード検索: " + request.getSupplierCode());
            SupplierResponse supplier = supplierService.findBySupplierCode(request.getSupplierCode());
            if (supplier != null) {
                System.out.println("仕入先が見つかりました: id=" + supplier.getId() + ", name=" + supplier.getName());
                request.setSupplierId(supplier.getId());
            } else {
                System.out.println("仕入先が見つかりません: code=" + request.getSupplierCode());
                throw new IllegalArgumentException("仕入先コード " + request.getSupplierCode() + " が見つかりません");
            }
        } else {
            System.out.println("仕入先コード検索をスキップ: supplierId=" + request.getSupplierId() + ", supplierCode=" + request.getSupplierCode());
        }
        
        // 必須フィールドの検証
        if (request.getSupplierId() == null) {
            System.out.println("仕入先IDが設定されていません");
            throw new IllegalArgumentException("仕入先IDは必須です");
        }
        
        // 得意先IDの設定（必要な場合）
        if (request.getCustomerId() == null && request.getCustomerCode() != null && !request.getCustomerCode().isEmpty()) {
            System.out.println("得意先コード検索: " + request.getCustomerCode());
            CustomerResponse customer = customerService.findByCustomerCode(request.getCustomerCode());
            if (customer != null) {
                System.out.println("得意先が見つかりました: id=" + customer.getId() + ", name=" + customer.getName());
                request.setCustomerId(customer.getId());
            } else {
                System.out.println("得意先が見つかりません: code=" + request.getCustomerCode());
                throw new IllegalArgumentException("得意先コード " + request.getCustomerCode() + " が見つかりません");
            }
        } else {
            System.out.println("得意先コード検索をスキップ: customerId=" + request.getCustomerId() + ", customerCode=" + request.getCustomerCode());
        }
        
        System.out.println("validateAndSetIds完了");
    }
}