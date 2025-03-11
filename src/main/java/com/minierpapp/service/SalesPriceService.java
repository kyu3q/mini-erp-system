package com.minierpapp.service;

import com.minierpapp.exception.ResourceNotFoundException;
import com.minierpapp.model.customer.Customer;
import com.minierpapp.model.item.Item;
import com.minierpapp.model.price.dto.SalesPriceRequest;
import com.minierpapp.model.price.dto.SalesPriceResponse;
import com.minierpapp.model.price.entity.PriceScale;
import com.minierpapp.model.price.entity.SalesPrice;
import com.minierpapp.model.price.mapper.SalesPriceMapper;
import com.minierpapp.repository.CustomerRepository;
import com.minierpapp.repository.ItemRepository;
import com.minierpapp.repository.PriceScaleRepository;
import com.minierpapp.repository.SalesPriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalesPriceService {
    private final SalesPriceRepository salesPriceRepository;
    private final PriceScaleRepository priceScaleRepository;
    private final ItemRepository itemRepository;
    private final CustomerRepository customerRepository;
    private final SalesPriceMapper salesPriceMapper;

    @Transactional(readOnly = true)
    public List<SalesPriceResponse> findAll() {
        List<SalesPrice> entities = salesPriceRepository.findAllWithRelations();
        return salesPriceMapper.toResponseList(entities);
    }

    @Transactional(readOnly = true)
    public SalesPriceResponse findById(Long id) {
        SalesPrice entity = salesPriceRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new ResourceNotFoundException("販売価格", "ID", id));
        return salesPriceMapper.entityToResponse(entity);
    }

    @Transactional(readOnly = true)
    public List<SalesPriceResponse> search(String itemCode, String customerCode, LocalDate date) {
        // 検索条件に応じたクエリを実行
        List<SalesPrice> results;
        if (date != null) {
            // 日付指定がある場合は有効期間内の価格を検索
            results = salesPriceRepository.findByItemCodeContainingAndCustomerCodeContainingAndValidFromDateLessThanEqualAndValidToDateGreaterThanEqualAndDeletedFalse(
                itemCode != null ? itemCode : "", 
                customerCode != null ? customerCode : "", 
                date, date);
        } else {
            // 日付指定がない場合は全ての価格を検索
            results = salesPriceRepository.findByItemCodeContainingAndCustomerCodeContainingAndDeletedFalse(
                itemCode != null ? itemCode : "", 
                customerCode != null ? customerCode : "");
        }
        return salesPriceMapper.toResponseList(results);
    }

    @Transactional
    public SalesPriceResponse create(SalesPriceRequest request) {
        validateRequest(request);
        
        // マッパーを使用してエンティティに変換
        SalesPrice salesPrice = salesPriceMapper.requestToEntity(request);
        
        // 関連エンティティの設定
        if (request.getItemId() != null) {
            Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("品目", request.getItemId()));
            salesPrice.setItem(item);
            salesPrice.setItemCode(item.getItemCode());
        }
        
        if (request.getCustomerId() != null) {
            Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("得意先", request.getCustomerId()));
            salesPrice.setCustomer(customer);
            salesPrice.setCustomerCode(customer.getCustomerCode());
        }
        
        salesPriceRepository.save(salesPrice);
        
        // 数量スケールの保存
        if (request.getPriceScales() != null && !request.getPriceScales().isEmpty()) {
            List<PriceScale> scales = request.getPriceScales().stream()
                .map(scaleRequest -> {
                    PriceScale scale = new PriceScale();
                    scale.setPrice(salesPrice);
                    scale.setFromQuantity(scaleRequest.getFromQuantity());
                    scale.setToQuantity(scaleRequest.getToQuantity());
                    scale.setScalePrice(scaleRequest.getScalePrice());
                    return scale;
                })
                .collect(Collectors.toList());
            
            priceScaleRepository.saveAll(scales);
        }
        
        return salesPriceMapper.entityToResponse(salesPrice);
    }

    @Transactional
    public SalesPriceResponse update(Long id, SalesPriceRequest request) {
        validateRequest(request);
        
        SalesPrice salesPrice = salesPriceRepository.findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new ResourceNotFoundException("販売価格", id));
        
        salesPriceMapper.updateEntityFromRequest(request, salesPrice);
        
        // 関連エンティティの更新
        if (request.getItemId() != null) {
            Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("品目", request.getItemId()));
            salesPrice.setItem(item);
            salesPrice.setItemCode(item.getItemCode());
        }
        
        if (request.getCustomerId() != null) {
            Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("得意先", request.getCustomerId()));
            salesPrice.setCustomer(customer);
            salesPrice.setCustomerCode(customer.getCustomerCode());
        } else {
            salesPrice.setCustomer(null);
            salesPrice.setCustomerCode(null);
        }
        
        salesPriceRepository.save(salesPrice);
        
        // 既存の数量スケールを削除
        priceScaleRepository.deleteByPriceId(id);
        
        // 新しい数量スケールを保存
        if (request.getPriceScales() != null && !request.getPriceScales().isEmpty()) {
            List<PriceScale> scales = request.getPriceScales().stream()
                .map(scaleRequest -> {
                    PriceScale scale = new PriceScale();
                    scale.setPrice(salesPrice);
                    scale.setFromQuantity(scaleRequest.getFromQuantity());
                    scale.setToQuantity(scaleRequest.getToQuantity());
                    scale.setScalePrice(scaleRequest.getScalePrice());
                    return scale;
                })
                .collect(Collectors.toList());
            
            priceScaleRepository.saveAll(scales);
        }
        
        return salesPriceMapper.entityToResponse(salesPrice);
    }

    @Transactional
    public void delete(Long id) {
        SalesPrice salesPrice = salesPriceRepository.findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new ResourceNotFoundException("販売価格", "ID", id));
        
        salesPrice.setDeleted(true);
        salesPriceRepository.save(salesPrice);
    }

    @Transactional(readOnly = true)
    public BigDecimal calculatePrice(String itemCode, String customerCode, BigDecimal quantity) {
        LocalDate today = LocalDate.now();
        
        // 顧客別価格を検索
        Optional<SalesPrice> priceOpt = salesPriceRepository.findByItemCodeAndCustomerCodeAndValidFromDateLessThanEqualAndValidToDateGreaterThanEqualAndStatusAndDeletedFalse(
            itemCode, customerCode, today, today, "ACTIVE");
        
        if (priceOpt.isPresent()) {
            return calculatePriceWithScales(priceOpt.get(), quantity);
        }
        
        // 顧客別価格がない場合は標準価格を検索
        return salesPriceRepository.findByItemCodeAndCustomerCodeIsNullAndValidFromDateLessThanEqualAndValidToDateGreaterThanEqualAndStatusAndDeletedFalse(
                itemCode, today, today, "ACTIVE")
            .map(price -> calculatePriceWithScales(price, quantity))
            .orElseThrow(() -> new ResourceNotFoundException("販売価格", "商品コード", itemCode));
    }
    
    private BigDecimal calculatePriceWithScales(SalesPrice price, BigDecimal quantity) {
        // 数量スケールに基づいて価格を計算
        for (PriceScale scale : price.getPriceScales()) {
            if (quantity.compareTo(scale.getFromQuantity()) >= 0 && 
                (scale.getToQuantity() == null || quantity.compareTo(scale.getToQuantity()) <= 0)) {
                return scale.getScalePrice();
            }
        }
        // スケールに該当しない場合は基本価格を返す
        return price.getBasePrice();
    }
    
    @Transactional(readOnly = true)
    public List<SalesPriceResponse> findExpiredPrices() {
        return salesPriceMapper.toResponseList(
            salesPriceRepository.findByValidToDateLessThanAndDeletedFalse(LocalDate.now())
        );
    }
    
    @Transactional(readOnly = true)
    public List<SalesPriceResponse> findSoonExpiringPrices(int daysThreshold) {
        LocalDate today = LocalDate.now();
        LocalDate thresholdDate = today.plusDays(daysThreshold);
        return salesPriceMapper.toResponseList(
            salesPriceRepository.findByValidToDateGreaterThanEqualAndValidToDateLessThanEqualAndDeletedFalse(today, thresholdDate)
        );
    }

    @Transactional(readOnly = true)
    public List<SalesPriceResponse> findAllForDisplay() {
        List<SalesPrice> entities = salesPriceRepository.findAllWithRelations();
        return salesPriceMapper.toResponseList(entities);
    }

    @Transactional(readOnly = true)
    public List<SalesPriceResponse> findWithFilters(Long itemId, Long supplierId, Long customerId) {
        List<SalesPrice> entities = salesPriceRepository.findWithFilters(itemId, supplierId, customerId);
        return salesPriceMapper.toResponseList(entities);
    }

    @Transactional(readOnly = true)
    public SalesPriceResponse findByIdForDisplay(Long id) {
        SalesPrice entity = salesPriceRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new ResourceNotFoundException("販売価格が見つかりません: " + id));
        return salesPriceMapper.entityToResponse(entity);
    }

    private void validateRequest(SalesPriceRequest request) {
        if (request.getItemId() == null) {
            throw new IllegalArgumentException("品目IDは必須です。");
        }
        
        if (request.getBasePrice() == null || request.getBasePrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("基本価格は0以上の値を指定してください。");
        }
        
        if (request.getValidFromDate() == null || request.getValidToDate() == null) {
            throw new IllegalArgumentException("有効期間の開始日と終了日は必須です。");
        }
        
        if (request.getValidFromDate().isAfter(request.getValidToDate())) {
            throw new IllegalArgumentException("有効開始日は有効終了日より前の日付を指定してください。");
        }
        
        // 数量スケールのバリデーション
        if (request.getPriceScales() != null) {
            for (var scale : request.getPriceScales()) {
                if (scale.getFromQuantity() == null || scale.getFromQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("数量スケールの開始数量は0より大きい値を指定してください。");
                }
                
                if (scale.getScalePrice() == null || scale.getScalePrice().compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalArgumentException("数量スケールの価格は0以上の値を指定してください。");
                }
            }
        }
    }
} 