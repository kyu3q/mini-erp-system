package com.minierpapp.model.price;

public enum ConditionType {
    ITEM_ONLY("品目のみ"),
    CUSTOMER_ITEM("得意先・品目"),
    SUPPLIER_ONLY("仕入先のみ"),
    SUPPLIER_ITEM("仕入先・品目"),
    SUPPLIER_CUSTOMER_ITEM("仕入先・得意先・品目");

    private final String description;

    ConditionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}