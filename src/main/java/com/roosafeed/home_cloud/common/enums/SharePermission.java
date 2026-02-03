package com.roosafeed.home_cloud.common.enums;

import lombok.Getter;

@Getter
public enum SharePermission {
    READ(1, "Read"),
    FULL(2, "Full");

    private final int level;
    private final String value;

    SharePermission(int level, String value) {
        this.level = level;
        this.value = value;
    }

    public boolean allows(SharePermission required) {
        return this.level >= required.level;
    }
}
