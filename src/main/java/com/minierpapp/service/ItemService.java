package com.minierpapp.service;

import com.minierpapp.model.item.Item;
import com.minierpapp.model.item.dto.ItemRequest;
import com.minierpapp.model.item.dto.ItemResponse;
import com.minierpapp.model.item.mapper.ItemMapper;
import com.minierpapp.repository.ItemRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;

    @Transactional(readOnly = true)
    public List<Item> findAll() {
        return itemRepository.findByDeletedFalse();
    }

    @Transactional(readOnly = true)
    public List<Item> findAllActive() {
        return itemRepository.findByDeletedFalse();
    }

    @Transactional(readOnly = true)
    public List<Item> search(String itemCode, String itemName) {
        itemCode = itemCode != null ? itemCode.trim() : "";
        itemName = itemName != null ? itemName.trim() : "";
        return itemRepository.findByItemCodeContainingAndItemNameContainingAndDeletedFalse(itemCode, itemName);
    }

    @Transactional(readOnly = true)
    public List<ItemResponse> searchItems(String keyword) {
        keyword = keyword != null ? keyword.trim() : "";
        List<Item> items = itemRepository.findByItemCodeContainingOrItemNameContainingAndDeletedFalse(keyword, keyword);
        return items.stream()
            .map(itemMapper::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public Page<Item> findAll(Pageable pageable) {
        return itemRepository.findByDeletedFalse(pageable);
    }

    @Transactional(readOnly = true)
    public Item findById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Item findByItemCode(String itemCode) {
        return itemRepository.findByItemCodeAndDeletedFalse(itemCode)
                .orElseThrow(() -> new EntityNotFoundException("Item not found with code: " + itemCode));
    }

    @Transactional
    public Item create(ItemRequest request) {
        // 品目コードの重複チェック
        if (itemRepository.existsByItemCodeAndDeletedFalse(request.getItemCode())) {
            throw new IllegalArgumentException("品目コード「" + request.getItemCode() + "」は既に使用されています");
        }

        // 在庫レベルのバリデーション
        validateStockLevels(request);

        // 新規品目の作成
        Item item = itemMapper.toEntity(request);
        item.setCreatedAt(LocalDateTime.now());
        return itemRepository.save(item);
    }

    @Transactional
    public Item update(Long id, ItemRequest request) {
        Item existingItem = findById(id);

        // 品目コードの変更は不可
        if (!existingItem.getItemCode().equals(request.getItemCode())) {
            throw new IllegalArgumentException("品目コードは変更できません");
        }

        // 在庫レベルのバリデーション
        validateStockLevels(request);

        // 品目情報の更新
        itemMapper.updateEntity(request, existingItem);
        existingItem.setUpdatedAt(LocalDateTime.now());

        return itemRepository.save(existingItem);
    }

    private void validateStockLevels(ItemRequest request) {
        Integer minimumStock = request.getMinimumStock();
        Integer maximumStock = request.getMaximumStock();
        Integer reorderPoint = request.getReorderPoint();

        if (minimumStock != null && maximumStock != null && minimumStock > maximumStock) {
            throw new IllegalArgumentException("最小在庫数は最大在庫数以下である必要があります");
        }

        if (reorderPoint != null) {
            if (minimumStock != null && reorderPoint < minimumStock) {
                throw new IllegalArgumentException("発注点は最小在庫数以上である必要があります");
            }
            if (maximumStock != null && reorderPoint > maximumStock) {
                throw new IllegalArgumentException("発注点は最大在庫数以下である必要があります");
            }
        }
    }

    @Transactional
    public void delete(Long id) {
        Item item = findById(id);
        item.setDeleted(true);
        item.setUpdatedAt(LocalDateTime.now());
        itemRepository.save(item);
    }
}