package com.roosafeed.home_cloud.common.enums;

import lombok.Getter;

@Getter
public enum MediaVisibility {
    PRIVATE("Private"),
    SHARED("Shared"),
    PUBLIC("Public");

    private final String value;

    MediaVisibility(String value) {
        this.value = value;
    }
}
