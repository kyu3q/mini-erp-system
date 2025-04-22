package com.minierpapp.service;

import com.minierpapp.exception.ResourceNotFoundException;
import com.minierpapp.model.customer.Customer;
import com.minierpapp.model.item.Item;
import com.minierpapp.model.price.dto.PriceScaleRequest;
import com.minierpapp.model.price.dto.SalesPriceRequest;
import com.minierpapp.model.price.dto.SalesPriceResponse;
import com.minierpapp.model.price.dto.SalesPriceSearchCriteria;
import com.minierpapp.model.price.entity.PriceScale;
import com.minierpapp.model.price.entity.SalesPrice;
import com.minierpapp.model.price.mapper.SalesPriceMapper;
import com.minierpapp.repository.CustomerRepository;
import com.minierpapp.repository.ItemRepository;
import com.minierpapp.repository.PriceScaleRepository;
import com.minierpapp.repository.SalesPriceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import jakarta.persistence.EntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalesPriceService {
    private final SalesPriceRepository salesPriceRepository;
    private final PriceScaleRepository priceScaleRepository;
    private final ItemRepository itemRepository;
    private final CustomerRepository customerRepository;
    private final SalesPriceMapper salesPriceMapper;
    private final EntityManager entityManager;

    @Transactional(readOnly = true)
    public List<SalesPriceResponse> findAll() {
        List<SalesPrice> entities = salesPriceRepository.findAll();
        entities.forEach(entity -> {
            List<PriceScale> scales = priceScaleRepository.findByPriceIdAndDeletedFalse(entity.getId());
            entity.setPriceScales(scales);
        });
        List<SalesPriceResponse> responses = salesPriceMapper.toResponseList(entities);
        responses.forEach(this::setRelatedNames);
        return responses;
    }

    @Transactional(readOnly = true)
    public SalesPriceResponse findById(Long id) {
        SalesPrice entity = salesPriceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("販売価格", "ID", id));
        List<PriceScale> scales = priceScaleRepository.findByPriceIdAndDeletedFalse(entity.getId());
        entity.setPriceScales(scales);
        return salesPriceMapper.entityToResponse(entity);
    }

    @Transactional(readOnly = true)
    public List<SalesPriceResponse> search(String itemCode, String customerCode, LocalDate date) {
        List<SalesPrice> results;
        if (date != null) {
            results = salesPriceRepository.findByItemCodeContainingAndCustomerCodeContainingAndValidFromDateLessThanEqualAndValidToDateGreaterThanEqualAndDeletedFalse(
                itemCode != null ? itemCode : "", 
                customerCode != null ? customerCode : "", 
                date, date);
        } else {
            results = salesPriceRepository.findByItemCodeContainingAndCustomerCodeContainingAndDeletedFalse(
                itemCode != null ? itemCode : "", 
                customerCode != null ? customerCode : "");
        }
        results.forEach(entity -> {
            List<PriceScale> scales = priceScaleRepository.findByPriceIdAndDeletedFalse(entity.getId());
            entity.setPriceScales(scales);
        });
        return salesPriceMapper.toResponseList(results);
    }

    @Transactional
    public SalesPriceResponse create(SalesPriceRequest request) {
        validateRequest(request);
        
        log.debug("リクエストの処理を開始: {}", request);
        log.debug("リクエストのスケール数: {}", 
            (request.getPriceScales() != null ? request.getPriceScales().size() : 0));
        
        // エンティティへの変換
        final SalesPrice entity = salesPriceMapper.requestToEntity(request);
        log.debug("エンティティに変換完了: {}", entity);
        
        // 関連エンティティの設定
        if (request.getItemId() != null) {
            log.debug("品目IDの設定: {}", request.getItemId());
            try {
                Item item = itemRepository.findById(request.getItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("品目", request.getItemId()));
                entity.setItem(item);
                entity.setItemCode(item.getItemCode());
                log.debug("品目情報の設定完了: {}, コード: {}", item.getItemName(), item.getItemCode());
            } catch (Exception e) {
                log.error("品目情報の取得中にエラー発生: {}", e.getMessage(), e);
                throw e;
            }
        }
        
        if (request.getCustomerId() != null) {
            log.debug("得意先IDの設定: {}", request.getCustomerId());
            try {
                Customer customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("得意先", request.getCustomerId()));
                entity.setCustomer(customer);
                entity.setCustomerCode(customer.getCustomerCode());
                log.debug("得意先情報の設定完了: {}, コード: {}", customer.getName(), customer.getCustomerCode());
            } catch (Exception e) {
                log.error("得意先情報の取得中にエラー発生: {}", e.getMessage(), e);
                throw e;
            }
        }
        
        // 先に価格エンティティを保存
        log.debug("販売価格エンティティを保存します");
        final SalesPrice savedEntity;
        try {
            savedEntity = salesPriceRepository.save(entity);
            log.debug("販売価格エンティティの保存完了: ID={}", savedEntity.getId());
        } catch (Exception e) {
            log.error("販売価格エンティティの保存中にエラー発生: {}", e.getMessage(), e);
            throw e;
        }
        
        // スケール価格の設定
        log.debug("スケール価格の設定を開始します");
        try {
            savePriceScales(request.getPriceScales(), savedEntity);
            log.debug("スケール価格の設定完了");
        } catch (Exception e) {
            log.error("スケール価格の設定中にエラー発生: {}", e.getMessage(), e);
            throw e;
        }
        
        return salesPriceMapper.entityToResponse(savedEntity);
    }

    private void savePriceScales(List<PriceScaleRequest> scales, SalesPrice price) {
        log.debug("savePriceScales開始: scales={}", scales);
        if (scales == null) {
            log.debug("スケール情報がnullです");
            return;
        }
        
        if (scales.isEmpty()) {
            log.debug("スケール情報が空です");
            return;
        }
        
        log.debug("スケール数: {}", scales.size());
        
        for (int i = 0; i < scales.size(); i++) {
            PriceScaleRequest scale = scales.get(i);
            log.debug("スケール[{}]の処理: fromQuantity={}, toQuantity={}, price={}", 
                i, scale.getFromQuantity(), scale.getToQuantity(), scale.getScalePrice());
            
            PriceScale priceScale = new PriceScale();
            priceScale.setFromQuantity(scale.getFromQuantity());
            priceScale.setToQuantity(scale.getToQuantity());
            priceScale.setScalePrice(scale.getScalePrice());
            priceScale.setPrice(price);
            
            try {
                price.getPriceScales().add(priceScale);
                PriceScale savedScale = priceScaleRepository.save(priceScale);
                log.debug("スケール[{}]の保存完了: ID={}", i, savedScale.getId());
            } catch (Exception e) {
                log.error("スケール[{}]の保存中にエラー発生: {}", i, e.getMessage(), e);
                throw e;
            }
        }
    }

    @Transactional
    public SalesPriceResponse update(Long id, SalesPriceRequest request) {
        validateRequest(request);
        
        SalesPrice salesPrice = salesPriceRepository.findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new ResourceNotFoundException("販売価格", id));
        
        // 基本情報の更新
        salesPriceMapper.updateEntityFromRequest(request, salesPrice);
        
        // 関連エンティティの設定
        if (request.getItemId() != null) {
            Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("品目", request.getItemId()));
            salesPrice.setItem(item);
            salesPrice.setItemCode(item.getItemCode());
        }
        
        // 得意先情報の設定
        if (request.getCustomerId() != null) {
            Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("得意先", request.getCustomerId()));
            salesPrice.setCustomer(customer);
            salesPrice.setCustomerCode(customer.getCustomerCode());
        }
        
        // 既存のスケール情報を物理削除する
        deleteExistingPriceScales(id);
        
        // 販売単価を保存
        SalesPrice savedPrice = salesPriceRepository.save(salesPrice);
        
        // 新しいスケール情報を保存
        if (request.getPriceScales() != null && !request.getPriceScales().isEmpty()) {
            createNewPriceScales(request.getPriceScales(), savedPrice);
        }
        
        // 更新後のエンティティを再取得
        SalesPrice updatedPrice = salesPriceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("販売価格", id));
        
        // スケール情報を明示的に取得
        List<PriceScale> updatedScales = priceScaleRepository.findByPriceIdAndDeletedFalse(id);
        updatedPrice.setPriceScales(updatedScales);
        
        // レスポンスを返す
        SalesPriceResponse response = salesPriceMapper.entityToResponse(updatedPrice);
        setRelatedNames(response);
        return response;
    }

    // 既存のスケール情報を物理削除するメソッド
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteExistingPriceScales(Long priceId) {
        // ネイティブSQLを使用して物理削除を実行
        priceScaleRepository.deleteByPriceIdNative(priceId);
        
        // 念のため、エンティティマネージャのキャッシュをクリア
        entityManager.flush();
        entityManager.clear();
    }
    
    // 新しいスケール情報を保存するメソッド
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createNewPriceScales(List<PriceScaleRequest> scaleRequests, SalesPrice price) {
        log.debug("createNewPriceScales開始: scaleRequests={}, priceId={}", 
            scaleRequests != null ? scaleRequests.size() : "null", price.getId());
        
        if (scaleRequests == null) {
            log.debug("スケールリクエストがnullです");
            return;
        }
        
        for (int i = 0; i < scaleRequests.size(); i++) {
            PriceScaleRequest scaleRequest = scaleRequests.get(i);
            log.debug("スケール[{}]の作成: fromQuantity={}, toQuantity={}, price={}", 
                i, scaleRequest.getFromQuantity(), scaleRequest.getToQuantity(), scaleRequest.getScalePrice());
            
            try {
                PriceScale scale = new PriceScale();
                scale.setPrice(price);
                scale.setFromQuantity(scaleRequest.getFromQuantity());
                scale.setToQuantity(scaleRequest.getToQuantity());
                scale.setScalePrice(scaleRequest.getScalePrice());
                PriceScale savedScale = priceScaleRepository.save(scale);
                log.debug("スケール[{}]の保存完了: ID={}", i, savedScale.getId());
            } catch (Exception e) {
                log.error("スケール[{}]の保存中にエラー発生: {}", i, e.getMessage(), e);
                throw e;
            }
        }
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
        log.debug("calculatePriceWithScales開始: priceId={}, quantity={}", price.getId(), quantity);
        
        // priceScalesがnullかどうかチェック
        if (price.getPriceScales() == null) {
            log.debug("priceScalesがnullです。基本価格を返します: {}", price.getBasePrice());
            return price.getBasePrice();
        }
        
        log.debug("スケール数: {}", price.getPriceScales().size());
        
        // 数量スケールに基づいて価格を計算
        for (PriceScale scale : price.getPriceScales()) {
            log.debug("スケールチェック: fromQuantity={}, toQuantity={}, scalePrice={}", 
                scale.getFromQuantity(), scale.getToQuantity(), scale.getScalePrice());
                
            if (quantity.compareTo(scale.getFromQuantity()) >= 0 && 
                (scale.getToQuantity() == null || quantity.compareTo(scale.getToQuantity()) <= 0)) {
                log.debug("適合するスケールが見つかりました。スケール価格を返します: {}", scale.getScalePrice());
                return scale.getScalePrice();
            }
        }
        
        // スケールに該当しない場合は基本価格を返す
        log.debug("適合するスケールが見つかりませんでした。基本価格を返します: {}", price.getBasePrice());
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

    private void setRelatedNames(SalesPriceResponse response) {
        // 品目名の設定
        if (response.getItemId() != null) {
            try {
                Item item = itemRepository.findById(response.getItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("品目", response.getItemId()));
                if (item != null) {
                    response.setItemName(item.getItemName());
                }
            } catch (Exception e) {
                // エラーが発生しても処理を続行
                System.out.println("品目名の取得に失敗: " + e.getMessage());
            }
        }
        
        // 得意先名の設定
        if (response.getCustomerId() != null) {
            try {
                Customer customer = customerRepository.findById(response.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("得意先", response.getCustomerId()));
                if (customer != null) {
                    response.setCustomerName(customer.getName());
                }
            } catch (Exception e) {
                // エラーが発生しても処理を続行
                System.out.println("得意先名の取得に失敗: " + e.getMessage());
            }
        }
    }

    /**
     * 重複する販売単価を検索する
     */
    public List<SalesPrice> findOverlappingPrices(Long itemId, Long customerId, LocalDate fromDate, LocalDate toDate) {
        return salesPriceRepository.findOverlappingPrices(itemId, customerId, fromDate, toDate);
    }

    /**
     * 検索条件に基づいて販売単価を検索する
     */
    public List<SalesPrice> searchSalesPrices(SalesPriceSearchCriteria criteria) {
        LocalDate validDate = criteria.getValidDate();
        String validDateStr = validDate != null ? validDate.toString() : null;
        
        return salesPriceRepository.findAllWithItemAndCustomer(
            criteria.getItemCode(),
            criteria.getItemName(),
            criteria.getCustomerCode(),
            criteria.getCustomerName(),
            validDateStr,
            criteria.getStatus()
        );
    }

    /**
     * エクスポート用に販売単価を検索し、関連エンティティの情報を設定する
     */
    @Transactional(readOnly = true)
    public List<SalesPrice> searchSalesPricesForExport(SalesPriceSearchCriteria criteria) {
        LocalDate validDate = criteria.getValidDate();
        String validDateStr = validDate != null ? validDate.toString() : null;
        
        List<SalesPrice> salesPrices = salesPriceRepository.findAllWithItemAndCustomer(
            criteria.getItemCode(),
            criteria.getItemName(),
            criteria.getCustomerCode(),
            criteria.getCustomerName(),
            validDateStr,
            criteria.getStatus()
        );
        
        // 関連エンティティの情報を明示的に設定
        for (SalesPrice price : salesPrices) {
            if (price.getItem() == null && price.getItemId() != null) {
                itemRepository.findById(price.getItemId()).ifPresent(price::setItem);
            }
            
            if (price.getCustomer() == null && price.getCustomerId() != null) {
                customerRepository.findById(price.getCustomerId()).ifPresent(price::setCustomer);
            }
            
            // スケール情報も取得
            List<PriceScale> scales = priceScaleRepository.findByPriceIdAndDeletedFalse(price.getId());
            price.setPriceScales(scales);
        }
        
        return salesPrices;
    }

    /**
     * 検索条件に基づいて販売単価を検索し、レスポンスに変換する
     */
    @Transactional(readOnly = true, noRollbackFor = Exception.class)
    public List<SalesPriceResponse> searchWithCriteria(SalesPriceSearchCriteria criteria) {
        try {
            // validDateがnullの場合は現在の日付を使用
            LocalDate searchDate = criteria.getValidDate();
            if (searchDate == null) {
                searchDate = LocalDate.now();
            }
            
            // 日付を文字列に変換
            String validDateStr = searchDate.toString(); // YYYY-MM-DD形式
            
            // 検索条件のログ出力
            System.out.println("検索条件: " + criteria);
            System.out.println("日付文字列: " + validDateStr);
            
            List<SalesPrice> entities = salesPriceRepository.findAllWithItemAndCustomer(
                criteria.getItemCode(),
                criteria.getItemName(),
                criteria.getCustomerCode(),
                criteria.getCustomerName(),
                validDateStr,
                criteria.getStatus()
            );
            
            // 検索結果のログ出力
            System.out.println("検索結果件数: " + entities.size());
            
            entities.forEach(entity -> {
                try {
                    List<PriceScale> scales = priceScaleRepository.findByPriceIdAndDeletedFalse(entity.getId());
                    entity.setPriceScales(scales);
                } catch (Exception e) {
                    log.warn("スケール情報の取得に失敗しました: {}", e.getMessage());
                    entity.setPriceScales(List.of());
                }
            });
            
            List<SalesPriceResponse> responses = salesPriceMapper.toResponseList(entities);
            
            responses.forEach(response -> {
                try {
                    setRelatedNames(response);
                } catch (Exception e) {
                    log.warn("関連名称の設定に失敗しました: {}", e.getMessage());
                }
            });
            
            return responses;
        } catch (Exception e) {
            log.error("検索中にエラーが発生しました: {}", e.getMessage(), e);
            return List.of(); // 空のリストを返す
        }
    }
} 