package com.minierpapp.model.price.dto;

import com.minierpapp.model.price.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class PriceDetailsResponse {
    private List<PriceItemDetail> priceItems;
    private List<PriceCustomerItemDetail> priceCustomerItems;
    private List<PriceSupplierItemDetail> priceSupplierItems;
    private List<PriceSupplierCustomerItemDetail> priceSupplierCustomerItems;

    public static PriceDetailsResponse from(Price price) {
        PriceDetailsResponse response = new PriceDetailsResponse();
        response.setPriceItems(price.getPriceItems().stream()
                .map(PriceItemDetail::from)
                .collect(Collectors.toList()));
        response.setPriceCustomerItems(price.getPriceCustomerItems().stream()
                .map(PriceCustomerItemDetail::from)
                .collect(Collectors.toList()));
        response.setPriceSupplierItems(price.getPriceSupplierItems().stream()
                .map(PriceSupplierItemDetail::from)
                .collect(Collectors.toList()));
        response.setPriceSupplierCustomerItems(price.getPriceSupplierCustomerItems().stream()
                .map(PriceSupplierCustomerItemDetail::from)
                .collect(Collectors.toList()));
        return response;
    }

    @Data
    public static class PriceItemDetail {
        private String itemCode;
        private BigDecimal basePrice;
        private String currencyCode;
        private List<PriceScaleDetail> scales;

        public static PriceItemDetail from(PriceItem item) {
            PriceItemDetail detail = new PriceItemDetail();
            detail.setItemCode(item.getItemCode());
            detail.setBasePrice(item.getBasePrice());
            detail.setCurrencyCode(item.getCurrencyCode());
            detail.setScales(item.getPriceScales().stream()
                    .map(PriceScaleDetail::from)
                    .collect(Collectors.toList()));
            return detail;
        }
    }

    @Data
    public static class PriceCustomerItemDetail {
        private String customerCode;
        private String itemCode;
        private BigDecimal basePrice;
        private String currencyCode;
        private List<PriceScaleDetail> scales;

        public static PriceCustomerItemDetail from(PriceCustomerItem item) {
            PriceCustomerItemDetail detail = new PriceCustomerItemDetail();
            detail.setCustomerCode(item.getCustomerCode());
            detail.setItemCode(item.getItemCode());
            detail.setBasePrice(item.getBasePrice());
            detail.setCurrencyCode(item.getCurrencyCode());
            detail.setScales(item.getPriceScales().stream()
                    .map(PriceScaleDetail::from)
                    .collect(Collectors.toList()));
            return detail;
        }
    }

    @Data
    public static class PriceSupplierItemDetail {
        private String supplierCode;
        private String itemCode;
        private BigDecimal basePrice;
        private String currencyCode;
        private List<PriceScaleDetail> scales;

        public static PriceSupplierItemDetail from(PriceSupplierItem item) {
            PriceSupplierItemDetail detail = new PriceSupplierItemDetail();
            detail.setSupplierCode(item.getSupplierCode());
            detail.setItemCode(item.getItemCode());
            detail.setBasePrice(item.getBasePrice());
            detail.setCurrencyCode(item.getCurrencyCode());
            detail.setScales(item.getPriceScales().stream()
                    .map(PriceScaleDetail::from)
                    .collect(Collectors.toList()));
            return detail;
        }
    }

    @Data
    public static class PriceSupplierCustomerItemDetail {
        private String supplierCode;
        private String customerCode;
        private String itemCode;
        private BigDecimal basePrice;
        private String currencyCode;
        private List<PriceScaleDetail> scales;

        public static PriceSupplierCustomerItemDetail from(PriceSupplierCustomerItem item) {
            PriceSupplierCustomerItemDetail detail = new PriceSupplierCustomerItemDetail();
            detail.setSupplierCode(item.getSupplierCode());
            detail.setCustomerCode(item.getCustomerCode());
            detail.setItemCode(item.getItemCode());
            detail.setBasePrice(item.getBasePrice());
            detail.setCurrencyCode(item.getCurrencyCode());
            detail.setScales(item.getPriceScales().stream()
                    .map(PriceScaleDetail::from)
                    .collect(Collectors.toList()));
            return detail;
        }
    }

    @Data
    public static class PriceScaleDetail {
        private BigDecimal fromQuantity;
        private BigDecimal toQuantity;
        private BigDecimal scalePrice;

        public static PriceScaleDetail from(PriceScale scale) {
            PriceScaleDetail detail = new PriceScaleDetail();
            detail.setFromQuantity(scale.getFromQuantity());
            detail.setToQuantity(scale.getToQuantity());
            detail.setScalePrice(scale.getScalePrice());
            return detail;
        }
    }
}