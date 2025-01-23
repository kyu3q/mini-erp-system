package com.minierpapp.model.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Status {
    ACTIVE("有効"),
    INACTIVE("無効");

    private final String displayName;

    Status(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @JsonValue
    public String getValue() {
        return this.name();
    }

    @JsonCreator
    public static Status fromString(String value) {
        try {
            return Status.valueOf(value);
        } catch (IllegalArgumentException e) {
            // 表示名からの変換を試みる
            for (Status status : Status.values()) {
                if (status.displayName.equals(value)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Invalid status value: " + value);
        }
    }
}