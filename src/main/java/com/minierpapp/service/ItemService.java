package com.minierpapp.service;

import com.minierpapp.exception.ResourceNotFoundException;
import com.minierpapp.model.item.Item;
import com.minierpapp.model.item.dto.ItemRequest;
import com.minierpapp.model.item.dto.ItemResponse;
import com.minierpapp.model.item.dto.ItemDto;
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
            .map(itemMapper::entityToResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public Page<Item> findAll(Pageable pageable) {
        return itemRepository.findByDeletedFalse(pageable);
    }

    @Transactional(readOnly = true)
    public ItemResponse findById(Long id) {
       return itemRepository.findById(id)
                .map(itemMapper::entityToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", id));
    }

    @Transactional(readOnly = true)
    public ItemResponse findByItemCode(String itemCode) {
        return itemRepository.findByItemCodeAndDeletedFalse(itemCode)
                .map(itemMapper::entityToResponse)
                .orElseThrow(() -> new EntityNotFoundException("Item not found with code: " + itemCode));
    }

    @Transactional
    public ItemResponse create(ItemRequest request) {
        // 品目コードの重複チェック
        if (itemRepository.existsByItemCodeAndDeletedFalse(request.getItemCode())) {
            throw new IllegalArgumentException("品目コード「" + request.getItemCode() + "」は既に使用されています");
        }

        // 在庫レベルのバリデーション
        validateStockLevels(request);

        // 新規品目の作成
        Item item = itemMapper.requestToEntity(request);
        item.setCreatedAt(LocalDateTime.now());
        Item savedItem = itemRepository.save(item);
        return itemMapper.entityToResponse(savedItem);
    }

    @Transactional
    public ItemDto update(Long id, ItemRequest request) {
        Item existingItem = itemRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Item", "id", id));

        // 品目コードの変更は不可
        if (!existingItem.getItemCode().equals(request.getItemCode())) {
            throw new IllegalArgumentException("品目コードは変更できません");
        }

        // 在庫レベルのバリデーション
        validateStockLevels(request);

        // 品目情報の更新
        itemMapper.updateEntityFromRequest(request, existingItem);
        existingItem.setUpdatedAt(LocalDateTime.now());

        Item updatedItem = itemRepository.save(existingItem);
        return itemMapper.toDto(updatedItem);
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

    @Transactional(readOnly = true)
    public boolean existsByCode(String itemCode) {
        return itemRepository.existsByItemCodeAndDeletedFalse(itemCode);
    }

    @Transactional(readOnly = true)
    public Item findByCode(String itemCode) {
        return itemRepository.findByItemCodeAndDeletedFalse(itemCode)
                .orElseThrow(() -> new EntityNotFoundException("Item not found with code: " + itemCode));
    }

    @Transactional
    public void delete(Long id) {
        Item item = itemRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Item", "id", id));

        item.setDeleted(true);
        item.setUpdatedAt(LocalDateTime.now());
        itemRepository.save(item);
    }

    @Transactional
    public void bulkCreate(List<Item> items) {
        items.forEach(this::create);
    }

    @Transactional
    public Item create(Item item) {
        return itemRepository.save(item);
    }

    @Transactional
    public Item update(Item item) {
        return itemRepository.save(item);
    }
}