package com.minierpapp.controller;

import com.minierpapp.controller.base.BaseRestController;
import com.minierpapp.model.supplier.Supplier;
import com.minierpapp.model.supplier.dto.SupplierDto;
import com.minierpapp.model.supplier.dto.SupplierRequest;
import com.minierpapp.model.supplier.dto.SupplierResponse;
import com.minierpapp.model.supplier.mapper.SupplierMapper;
import com.minierpapp.service.SupplierService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.minierpapp.model.common.Status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController extends BaseRestController<Supplier, SupplierDto, SupplierRequest, SupplierResponse> {

    private final SupplierService supplierService;

    public SupplierController(SupplierMapper mapper, SupplierService supplierService) {
        super(mapper);
        this.supplierService = supplierService;
    }

    @GetMapping("/code/{supplierCode}")
    public ResponseEntity<SupplierResponse> findBySupplierCode(@PathVariable String supplierCode) {
        return ResponseEntity.ok(supplierService.findBySupplierCode(supplierCode));
    }

    @Override
    protected List<SupplierResponse> findAllEntities() {
        return supplierService.findAll(null, null);
    }

    @Override
    protected SupplierResponse findEntityById(Long id) {
        return supplierService.findById(id);
    }

    @Override
    protected SupplierResponse createEntity(SupplierRequest request) {
        return supplierService.create(request);
    }

    @Override
    protected SupplierResponse updateEntity(Long id, SupplierRequest request) {
        return supplierService.update(id, request);
    }

    @Override
    protected void deleteEntity(Long id) {
        supplierService.delete(id);
    }

    @Override
    protected List<SupplierResponse> searchEntities(String keyword) {
        return supplierService.searchSuppliers(keyword);
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
            List<SupplierRequest> suppliersToCreate = new ArrayList<>();
            List<SupplierRequest> suppliersToUpdate = new ArrayList<>();

            try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
                Sheet sheet = workbook.getSheetAt(0);

                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    try {
                        Row row = sheet.getRow(i);
                        if (row == null || isEmptyRow(row)) continue;

                        SupplierRequest supplier = new SupplierRequest();
                        String supplierCode = getStringCellValue(row.getCell(0));
                        
                        // 必須項目のバリデーション
                        if (supplierCode == null || supplierCode.isEmpty()) {
                            throw new IllegalArgumentException("仕入先コードは必須です (行: " + (i + 1) + ")");
                        }

                        supplier.setSupplierCode(supplierCode);
                        supplier.setName(getStringCellValue(row.getCell(1)));
                        supplier.setNameKana(getStringCellValue(row.getCell(2)));
                        supplier.setPostalCode(getStringCellValue(row.getCell(3)));
                        supplier.setAddress(getStringCellValue(row.getCell(4)));
                        supplier.setPhone(getStringCellValue(row.getCell(5)));
                        supplier.setFax(getStringCellValue(row.getCell(6)));
                        supplier.setEmail(getStringCellValue(row.getCell(7)));
                        supplier.setContactPerson(getStringCellValue(row.getCell(8)));
                        supplier.setPaymentTerms(getStringCellValue(row.getCell(9)));

                        // ステータスの処理
                        String status = getStringCellValue(row.getCell(10));
                        if (status != null && !status.isEmpty()) {
                            try {
                                switch (status.trim()) {
                                    case "有効":
                                        supplier.setStatus(Status.ACTIVE);
                                        break;
                                    case "無効":
                                        supplier.setStatus(Status.INACTIVE);
                                        break;
                                    default:
                                        try {
                                            supplier.setStatus(Status.valueOf(status.toUpperCase()));
                                        } catch (IllegalArgumentException e) {
                                            throw new IllegalArgumentException("無効なステータス値です。'有効' または '無効' を入力してください。: " + status + " (行: " + (i + 1) + ")");
                                        }
                                }
                            } catch (IllegalArgumentException e) {
                                throw new IllegalArgumentException("無効なステータス値です。'有効' または '無効' を入力してください。: " + status + " (行: " + (i + 1) + ")");
                            }
                        } else {
                            throw new IllegalArgumentException("ステータスは必須です (行: " + (i + 1) + ")");
                        }

                        supplier.setNotes(getStringCellValue(row.getCell(11)));

                        // その他の必須項目のバリデーション
                        if (supplier.getName() == null || supplier.getName().isEmpty()) {
                            throw new IllegalArgumentException("仕入先名は必須です (行: " + (i + 1) + ")");
                        }

                        // 既存データの確認
                        try {
                            SupplierResponse existingSupplier = supplierService.findBySupplierCode(supplierCode);
                            if (existingSupplier != null) {
                                supplier.setId(existingSupplier.getId());
                                suppliersToUpdate.add(supplier);
                            } else {
                                suppliersToCreate.add(supplier);
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

            // 一括更新と作成の実行
            if (!suppliersToUpdate.isEmpty()) {
                for (SupplierRequest supplier : suppliersToUpdate) {
                    supplierService.update(supplier.getId(), supplier);
                    updateCount++;
                }
            }

            if (!suppliersToCreate.isEmpty()) {
                supplierService.bulkCreate(suppliersToCreate);
                createCount = suppliersToCreate.size();
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

    // ユーティリティメソッド
    private boolean isEmptyRow(Row row) {
        if (row == null) return true;
        for (int i = 0; i < 12; i++) {
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
                return String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }
}