package com.minierpapp.service;

import com.minierpapp.exception.ResourceNotFoundException;
import com.minierpapp.model.item.Item;
import com.minierpapp.model.stock.Stock;
import com.minierpapp.model.stock.dto.StockDto;
import com.minierpapp.model.stock.dto.StockRequest;
import com.minierpapp.model.stock.mapper.StockMapper;
import com.minierpapp.model.warehouse.Warehouse;
import com.minierpapp.repository.ItemRepository;
import com.minierpapp.repository.StockRepository;
import com.minierpapp.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final WarehouseRepository warehouseRepository;
    private final ItemRepository itemRepository;
    private final StockMapper stockMapper;

    @Transactional(readOnly = true)
    public Page<StockDto> findAll(Pageable pageable) {
        return stockRepository.findAll(pageable)
                .map(stockMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<StockDto> findByWarehouse(Long warehouseId, Pageable pageable) {
        return stockRepository.findByWarehouseId(warehouseId, pageable)
                .map(stockMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<StockDto> findByItem(Long itemId, Pageable pageable) {
        return stockRepository.findByItemId(itemId, pageable)
                .map(stockMapper::toDto);
    }

    @Transactional(readOnly = true)
    public StockDto findById(Long id) {
        return stockRepository.findById(id)
                .map(stockMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found with id: " + id));
    }

    @Transactional
    public StockDto create(StockRequest request) {
        if (stockRepository.existsByWarehouseIdAndItemId(request.getWarehouseId(), request.getItemId())) {
            throw new IllegalArgumentException("Stock already exists for this warehouse and item combination");
        }

        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + request.getWarehouseId()));

        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + request.getItemId()));

        Stock stock = stockMapper.toEntity(request);
        stock.setWarehouse(warehouse);
        stock.setItem(item);

        return stockMapper.toDto(stockRepository.save(stock));
    }

    @Transactional
    public StockDto update(Long id, StockRequest request) {
        Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found with id: " + id));

        if (!stock.getWarehouse().getId().equals(request.getWarehouseId()) ||
            !stock.getItem().getId().equals(request.getItemId())) {
            
            if (stockRepository.existsByWarehouseIdAndItemId(request.getWarehouseId(), request.getItemId())) {
                throw new IllegalArgumentException("Stock already exists for this warehouse and item combination");
            }

            Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + request.getWarehouseId()));

            Item item = itemRepository.findById(request.getItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + request.getItemId()));

            stock.setWarehouse(warehouse);
            stock.setItem(item);
        }

        stock.setQuantity(request.getQuantity());
        stock.setMinimumQuantity(request.getMinimumQuantity());
        stock.setMaximumQuantity(request.getMaximumQuantity());
        stock.setLocation(request.getLocation());
        stock.setNotes(request.getNotes());

        return stockMapper.toDto(stockRepository.save(stock));
    }

    @Transactional
    public void delete(Long id) {
        if (!stockRepository.existsById(id)) {
            throw new ResourceNotFoundException("Stock not found with id: " + id);
        }
        stockRepository.deleteById(id);
    }

    @Transactional
    public StockDto adjustQuantity(Long id, int adjustment) {
        Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found with id: " + id));

        int newQuantity = stock.getQuantity() + adjustment;
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }

        stock.setQuantity(newQuantity);
        return stockMapper.toDto(stockRepository.save(stock));
    }
}