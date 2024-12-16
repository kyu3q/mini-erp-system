package com.minierpapp.model.common;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Status {
    ACTIVE("有効"),
    INACTIVE("無効");

    private final String displayName;

    Status(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }
}