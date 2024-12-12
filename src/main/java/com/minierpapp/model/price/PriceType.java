package com.minierpapp.model.price;

public enum PriceType {
    SALES("販売単価"),
    PURCHASE("購買単価");

    private final String description;

    PriceType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}