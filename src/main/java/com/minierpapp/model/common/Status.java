package com.minierpapp.model.common;

public enum Status {
    ACTIVE("有効"),
    INACTIVE("無効");

    private final String description;

    Status(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}