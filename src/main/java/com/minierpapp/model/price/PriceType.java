package com.minierpapp.model.price;

public enum PriceType {
    SALES("販売単価"),
    PURCHASE("購買単価");

    private final String displayName;

    PriceType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}