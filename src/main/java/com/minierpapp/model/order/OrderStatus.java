package com.minierpapp.model.order;

public enum OrderStatus {
    DRAFT("下書き"),
    CONFIRMED("確定"),
    PROCESSING("処理中"),
    SHIPPED("出荷済"),
    DELIVERED("配送済"),
    CANCELLED("キャンセル");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}