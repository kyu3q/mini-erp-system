package com.minierpapp.controller;

import com.minierpapp.controller.base.BaseWebController;
import com.minierpapp.dto.excel.ImportResult;
import com.minierpapp.model.price.dto.SalesPriceDto;
import com.minierpapp.model.price.dto.SalesPriceRequest;
import com.minierpapp.model.price.dto.SalesPriceResponse;
import com.minierpapp.model.price.dto.SalesPriceSearchCriteria;
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
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;

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
import com.minierpapp.service.ExcelExportService;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/prices/sales")
public class SalesPriceWebController extends BaseWebController<SalesPrice, SalesPriceDto, SalesPriceRequest, SalesPriceResponse> {

    private final SalesPriceService salesPriceService;
    private final ExcelExportService excelExportService;
    private final PriceExcelService priceExcelService;
    private final ItemService itemService;
    private final CustomerService customerService;

    public SalesPriceWebController(
            SalesPriceService salesPriceService,
            SalesPriceMapper mapper,
            MessageSource messageSource,
            ExcelExportService excelExportService,
            PriceExcelService priceExcelService,
            SalesPriceMapper salesPriceMapper,
            ItemService itemService,
            CustomerService customerService) {
        super(mapper, messageSource, "price/sales", "SalesPrice");
        this.salesPriceService = salesPriceService;
        this.excelExportService = excelExportService;
        this.priceExcelService = priceExcelService;
        this.itemService = itemService;
        this.customerService = customerService;
    }

    @Override
    protected List<SalesPriceResponse> findAll() {
        // 現在の日付で検索条件を作成
        SalesPriceSearchCriteria criteria = new SalesPriceSearchCriteria(
            null, null, null, null, LocalDate.now(), null);
        
        return salesPriceService.searchWithCriteria(criteria);
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
        SalesPriceResponse createdEntity = salesPriceService.create(request);
        request.setId(createdEntity.getId());
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

    @Override
    protected void prepareForm(Model model, SalesPriceRequest request) {
        super.prepareForm(model, request);
        
        // フォーム用の追加データを準備
        model.addAttribute("items", itemService.findAllActive());
        model.addAttribute("customers", customerService.findAllActive());
        
        // 編集時に関連エンティティの情報を追加
        if (request.getId() != null) {
            if (request.getItemId() != null) {
                model.addAttribute("item", itemService.findById(request.getItemId()));
            }
            if (request.getCustomerId() != null) {
                model.addAttribute("customer", customerService.findById(request.getCustomerId()));
            }
        }
    }

    @Override
    protected void prepareSearchCriteria(Model model, HttpServletRequest request) {
        try {
            // リクエストパラメータをログに出力
            System.out.println("===== リクエストパラメータ =====");
            request.getParameterMap().forEach((key, values) -> {
                System.out.println(key + ": " + String.join(", ", values));
            });
            
            // モデルから既に作成された検索条件を取得
            SalesPriceSearchCriteria criteria = (SalesPriceSearchCriteria) model.getAttribute("searchCriteria");
            
            // 検索条件がない場合は新規作成
            if (criteria == null) {
                // パラメータを直接取得
                String itemCode = request.getParameter("itemCode");
                String itemName = request.getParameter("itemName");
                String customerCode = request.getParameter("customerCode");
                String customerName = request.getParameter("customerName");
                String validDateStr = request.getParameter("validDate");
                String status = request.getParameter("status");
                
                System.out.println("===== 取得したパラメータ =====");
                System.out.println("itemCode: " + itemCode);
                System.out.println("itemName: " + itemName);
                System.out.println("customerCode: " + customerCode);
                System.out.println("customerName: " + customerName);
                System.out.println("validDate: " + validDateStr);
                System.out.println("status: " + status);
                
                // 空文字列をnullに変換
                itemCode = (itemCode != null && !itemCode.isEmpty()) ? itemCode : null;
                itemName = (itemName != null && !itemName.isEmpty()) ? itemName : null;
                customerCode = (customerCode != null && !customerCode.isEmpty()) ? customerCode : null;
                customerName = (customerName != null && !customerName.isEmpty()) ? customerName : null;
                status = (status != null && !status.isEmpty()) ? status : null;
                
                // 日付の処理
                LocalDate validDate = LocalDate.now();
                if (validDateStr != null && !validDateStr.isEmpty()) {
                    try {
                        validDate = LocalDate.parse(validDateStr);
                    } catch (Exception e) {
                        System.err.println("日付の解析エラー: " + validDateStr + ", " + e.getMessage());
                    }
                }
                
                // 検索条件を作成
                criteria = new SalesPriceSearchCriteria(
                    itemCode, itemName, customerCode, customerName, validDate, status
                );
                
                // 検索条件をモデルに追加
                model.addAttribute("searchCriteria", criteria);
            }
            
            System.out.println("===== 使用する検索条件 =====");
            System.out.println(criteria);
            
            // 検索条件に基づいて販売単価を検索
            List<SalesPriceResponse> prices = salesPriceService.searchWithCriteria(criteria);
            System.out.println("===== 検索結果 =====");
            System.out.println("件数: " + prices.size());
            
            // 検索結果をそのまま表示（0件の場合は0件）
            model.addAttribute("prices", prices);
            
            // 検索パラメータをモデルに追加（フォームの初期値用）
            model.addAttribute("itemCode", criteria.getItemCode());
            model.addAttribute("itemName", criteria.getItemName());
            model.addAttribute("customerCode", criteria.getCustomerCode());
            model.addAttribute("customerName", criteria.getCustomerName());
            model.addAttribute("validDate", criteria.getValidDate() != null ? criteria.getValidDate().toString() : null);
            model.addAttribute("status", criteria.getStatus());
            
            // モデルの内容をログに出力
            System.out.println("===== モデルの内容 =====");
            model.asMap().forEach((key, value) -> {
                System.out.println(key + ": " + value);
            });
        } catch (Exception e) {
            // エラーをログに記録
            System.err.println("検索処理でエラーが発生しました: " + e.getMessage());
            e.printStackTrace();
            
            // 空のリストをセット
            model.addAttribute("prices", List.of());
            model.addAttribute("error", "検索処理でエラーが発生しました。システム管理者に連絡してください。");
        }
    }

    @GetMapping("/excel/export")
    public void exportSalesPrices(HttpServletResponse response, 
                                 @ModelAttribute SalesPriceSearchCriteria criteria) throws IOException {
        // 検索条件が指定されていない場合は、現在の日付を設定
        if (criteria.getValidDate() == null) {
            criteria.setValidDate(LocalDate.now());
        }
        
        // 現在の日付をフォーマットしてファイル名に追加
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String filename = "sales_prices_" + dateStr + ".xlsx";
        
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + filename);
        
        byte[] excelData = excelExportService.exportSalesPrices(criteria);
        response.getOutputStream().write(excelData);
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

    /**
     * 検索条件オブジェクトの初期化のみを行う
     * 標準的なSpring MVCパターンに従い、検索処理は行わない
     */
    @ModelAttribute("searchCriteria")
    public SalesPriceSearchCriteria initSearchCriteria(
            @RequestParam(required = false) String itemCode,
            @RequestParam(required = false) String itemName,
            @RequestParam(required = false) String customerCode,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String validDateStr,
            @RequestParam(required = false) String status) {
        
        System.out.println("initSearchCriteria メソッド呼び出し: " + 
                          "itemCode=" + itemCode + ", " +
                          "itemName=" + itemName + ", " +
                          "customerCode=" + customerCode + ", " +
                          "customerName=" + customerName + ", " +
                          "validDateStr=" + validDateStr + ", " +
                          "status=" + status);
        
        // 空文字列をnullに変換（空白文字も考慮）
        itemCode = (itemCode != null && !itemCode.trim().isEmpty()) ? itemCode.trim() : null;
        itemName = (itemName != null && !itemName.trim().isEmpty()) ? itemName.trim() : null;
        customerCode = (customerCode != null && !customerCode.trim().isEmpty()) ? customerCode.trim() : null;
        customerName = (customerName != null && !customerName.trim().isEmpty()) ? customerName.trim() : null;
        status = (status != null && !status.trim().isEmpty()) ? status.trim() : null;
        
        // 日付の処理
        LocalDate validDate = LocalDate.now();
        if (validDateStr != null && !validDateStr.trim().isEmpty()) {
            try {
                validDate = LocalDate.parse(validDateStr.trim());
            } catch (Exception e) {
                System.err.println("日付の解析エラー: " + validDateStr + ", " + e.getMessage());
            }
        }
        
        // 検索条件オブジェクトを作成して返す（検索は行わない）
        return new SalesPriceSearchCriteria(
            itemCode, itemName, customerCode, customerName, validDate, status);
    }
}