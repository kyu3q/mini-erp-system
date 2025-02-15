package com.minierpapp.controller;

import com.minierpapp.model.warehouse.dto.WarehouseDto;
import com.minierpapp.model.warehouse.Warehouse;
import com.minierpapp.controller.base.BaseRestController;
import com.minierpapp.model.warehouse.dto.WarehouseRequest;
import com.minierpapp.model.warehouse.dto.WarehouseResponse;
import com.minierpapp.model.warehouse.mapper.WarehouseMapper;
import com.minierpapp.service.WarehouseService;
import com.minierpapp.model.common.Status;
import jakarta.validation.Valid;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/warehouses")
public class WarehouseController extends BaseRestController<Warehouse, WarehouseDto, WarehouseRequest, WarehouseResponse> {

    private final WarehouseService warehouseService;
    private final WarehouseMapper mapper;

    public WarehouseController(WarehouseMapper mapper, WarehouseService warehouseService) {
        super(mapper);
        this.mapper = mapper;
        this.warehouseService = warehouseService;
    }

    @Override
    protected List<Warehouse> findAllEntities() {
        return warehouseService.findAll();
    }

    @Override
    protected Page<Warehouse> findAllEntities(Pageable pageable) {
        return warehouseService.findAll(pageable)
                .map(dto -> warehouseService.findById(dto.getId()));
    }

    @Override
    protected Warehouse findEntityById(Long id) {
        return warehouseService.findById(id);
    }

    @Override
    protected Warehouse createEntity(Warehouse entity) {
        return warehouseService.create(entity);
    }

    @Override
    protected Warehouse updateEntity(Warehouse entity) {
        return warehouseService.update(entity);
    }

    @Override
    protected void deleteEntity(Long id) {
        warehouseService.delete(id);
    }

    @GetMapping("/search")
    public ResponseEntity<List<WarehouseResponse>> search(@RequestParam(required = false) String keyword) {
        List<Warehouse> warehouses = warehouseService.search(keyword);
        List<WarehouseResponse> responses = warehouses.stream()
                .map(mapper::entityToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/code/{warehouseCode}")
    public ResponseEntity<WarehouseResponse> findByWarehouseCode(@PathVariable String warehouseCode) {
        WarehouseResponse response = warehouseService.findByWarehouseCode(warehouseCode);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<WarehouseResponse> create(@Valid @RequestBody WarehouseRequest request) {
        WarehouseDto dto = warehouseService.create(request);
        return ResponseEntity.ok(mapper.entityToResponse(warehouseService.findById(dto.getId())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WarehouseResponse> update(@PathVariable Long id, @Valid @RequestBody WarehouseRequest request) {
        WarehouseDto dto = warehouseService.update(id, request);
        return ResponseEntity.ok(mapper.entityToResponse(warehouseService.findById(dto.getId())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        warehouseService.delete(id);
        return ResponseEntity.noContent().build();
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
            List<WarehouseRequest> warehousesToCreate = new ArrayList<>();
            List<WarehouseRequest> warehousesToUpdate = new ArrayList<>();

            try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
                Sheet sheet = workbook.getSheetAt(0);

                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    try {
                        Row row = sheet.getRow(i);
                        if (row == null || isEmptyRow(row)) continue;

                        WarehouseRequest warehouse = new WarehouseRequest();
                        String warehouseCode = getStringCellValue(row.getCell(0));
                        
                        if (warehouseCode == null || warehouseCode.isEmpty()) {
                            throw new IllegalArgumentException("倉庫コードは必須です (行: " + (i + 1) + ")");
                        }

                        warehouse.setWarehouseCode(warehouseCode);
                        warehouse.setName(getStringCellValue(row.getCell(1)));
                        warehouse.setNameKana(getStringCellValue(row.getCell(2)));
                        warehouse.setPostalCode(getStringCellValue(row.getCell(3)));
                        warehouse.setAddress(getStringCellValue(row.getCell(4)));
                        warehouse.setPhone(getStringCellValue(row.getCell(5)));
                        warehouse.setFax(getStringCellValue(row.getCell(6)));
                        warehouse.setManager(getStringCellValue(row.getCell(7)));

                        // ステータスの処理
                        String status = getStringCellValue(row.getCell(8));
                        if (status != null && !status.isEmpty()) {
                            try {
                                switch (status.trim()) {
                                    case "有効":
                                        warehouse.setStatus(Status.ACTIVE);
                                        break;
                                    case "無効":
                                        warehouse.setStatus(Status.INACTIVE);
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

                        warehouse.setDescription(getStringCellValue(row.getCell(9)));

                        if (warehouse.getName() == null || warehouse.getName().isEmpty()) {
                            throw new IllegalArgumentException("倉庫名は必須です (行: " + (i + 1) + ")");
                        }

                        // 既存データの確認
                        try {
                            WarehouseResponse existingWarehouse = warehouseService.findByWarehouseCode(warehouseCode);
                            if (existingWarehouse != null) {
                                warehouse.setId(existingWarehouse.getId());
                                warehousesToUpdate.add(warehouse);
                            } else {
                                warehousesToCreate.add(warehouse);
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

            if (!warehousesToUpdate.isEmpty()) {
                for (WarehouseRequest warehouse : warehousesToUpdate) {
                    warehouseService.update(warehouse.getId(), warehouse);
                    updateCount++;
                }
            }

            if (!warehousesToCreate.isEmpty()) {
                warehouseService.bulkCreate(warehousesToCreate);
                createCount = warehousesToCreate.size();
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
        for (int i = 0; i < 10; i++) {
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

    @Override
    protected List<WarehouseResponse> searchEntities(String keyword) {
        return warehouseService.search(keyword).stream()
                .map(mapper::entityToResponse)
                .collect(Collectors.toList());
    }
}