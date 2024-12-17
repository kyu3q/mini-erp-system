package com.minierpapp.dto.excel;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ImportResult {
    private int totalCount;
    private int successCount;
    private int errorCount;
    private List<ErrorDetail> errors = new ArrayList<>();

    @Data
    public static class ErrorDetail {
        private int rowNum;
        private String message;

        public ErrorDetail(int rowNum, String message) {
            this.rowNum = rowNum;
            this.message = message;
        }
    }

    public void addSuccess() {
        successCount++;
        totalCount++;
    }

    public void addError(int rowNum, String message) {
        errors.add(new ErrorDetail(rowNum, message));
        errorCount++;
        totalCount++;
    }
}