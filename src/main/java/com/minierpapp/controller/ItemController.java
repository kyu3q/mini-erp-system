package com.minierpapp.controller;

import com.minierpapp.controller.base.BaseRestController;
import com.minierpapp.model.item.Item;
import com.minierpapp.model.item.dto.ItemDto;
import com.minierpapp.model.item.dto.ItemRequest;
import com.minierpapp.model.item.dto.ItemResponse;
import com.minierpapp.model.item.mapper.ItemMapper;
import com.minierpapp.service.ItemService;
import com.minierpapp.model.common.Status;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/items")
public class ItemController extends BaseRestController<Item, ItemDto, ItemRequest, ItemResponse> {
    private final ItemService itemService;

    public ItemController(ItemMapper mapper, ItemService itemService) {
        super(mapper);
        this.itemService = itemService;
    }

    @GetMapping("/code/{itemCode}")
    public ResponseEntity<ItemResponse> findByItemCode(@PathVariable String itemCode) {
        return ResponseEntity.ok(itemService.findByItemCode(itemCode));
    }

    @Override
    protected List<Item> findAllEntities() {
        return itemService.findAll();
    }

    @Override
    protected Page<Item> findAllEntities(Pageable pageable) {
        return itemService.findAll(pageable);
    }

    @Override
    protected Item findEntityById(Long id) {
        return itemService.findById(id);
    }

    @Override
    protected Item createEntity(Item entity) {
        return itemService.create(entity);
    }

    @Override
    protected Item updateEntity(Item entity) {
        return itemService.update(entity);
    }

    @Override
    protected void deleteEntity(Long id) {
        itemService.delete(id);
    }

    @Override
    protected List<ItemResponse> searchEntities(String keyword) {
        return itemService.searchItems(keyword);
    }

    @PostMapping("/import")
    public Map<String, Object> importExcel(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> errors = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;
        int updateCount = 0;
        int createCount = 0;

        try {
            List<Item> itemsToCreate = new ArrayList<>();
            List<Item> itemsToUpdate = new ArrayList<>();

            try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
                Sheet sheet = workbook.getSheetAt(0);

                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    try {
                        Row row = sheet.getRow(i);
                        if (row == null || isEmptyRow(row)) continue;

                        Item item = new Item();
                        String itemCode = getStringCellValue(row.getCell(0));
                        
                        if (itemCode == null || itemCode.isEmpty()) {
                            throw new IllegalArgumentException("品目コードは必須です (行: " + (i + 1) + ")");
                        }

                        item.setItemCode(itemCode);
                        item.setItemName(getStringCellValue(row.getCell(1)));
                        item.setUnit(getStringCellValue(row.getCell(2)));
                        

                        // ステータスの処理
                        String status = getStringCellValue(row.getCell(3));
                        if (status != null && !status.isEmpty()) {
                            try {
                                switch (status.trim()) {
                                    case "有効":
                                        item.setStatus(Status.ACTIVE);
                                        break;
                                    case "無効":
                                        item.setStatus(Status.INACTIVE);
                                        break;
                                    default:
                                        throw new IllegalArgumentException("無効なステータス値です。'有効' または '無効' を入力してください。");
                                }
                            } catch (IllegalArgumentException e) {
                                throw new IllegalArgumentException("無効なステータス値です (行: " + (i + 1) + "): " + e.getMessage());
                            }
                        } else {
                            throw new IllegalArgumentException("ステータスは必須です (行: " + (i + 1) + ")");
                        }

                        item.setDescription(getStringCellValue(row.getCell(4)));

                        if (item.getItemName() == null || item.getItemName().isEmpty()) {
                            throw new IllegalArgumentException("品目名は必須です (行: " + (i + 1) + ")");
                        }

                        // 既存データの確認
                        try {
                            ItemResponse existingItem = itemService.findByItemCode(itemCode);
                            if (existingItem != null) {
                                item.setId(existingItem.getId());
                                itemsToUpdate.add(item);
                            } else {
                                itemsToCreate.add(item);
                            }
                            successCount++;
                        } catch (Exception e) {
                            throw new IllegalArgumentException("データの検証中にエラーが発生しました (行: " + (i + 1) + "): " + e.getMessage());
                        }

                    } catch (Exception e) {
                        Map<String, Object> error = new HashMap<>();
                        error.put("rowNum", i + 1);
                        error.put("message", e.getMessage());
                        errors.add(error);
                        errorCount++;
                    }
                }
            }

            if (!itemsToUpdate.isEmpty()) {
                for (Item item : itemsToUpdate) {
                    itemService.update(item);
                    updateCount++;
                }
            }

            if (!itemsToCreate.isEmpty()) {
                itemService.bulkCreate(itemsToCreate);
                createCount = itemsToCreate.size();
            }

            result.put("totalCount", successCount + errorCount);
            result.put("successCount", successCount);
            result.put("createCount", createCount);
            result.put("updateCount", updateCount);
            result.put("errorCount", errorCount);
            result.put("errors", errors);

            return result;

        } catch (Exception e) {
            throw new RuntimeException("ファイルの処理中にエラーが発生しました: " + e.getMessage());
        }
    }

    private boolean isEmptyRow(Row row) {
        if (row == null) return true;
        for (int i = 0; i < 8; i++) {
            if (getStringCellValue(row.getCell(i)) != null && !getStringCellValue(row.getCell(i)).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private String getStringCellValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toString();
                }
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }
}