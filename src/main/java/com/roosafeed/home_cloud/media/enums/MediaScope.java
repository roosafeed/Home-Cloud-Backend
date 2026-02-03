package com.roosafeed.home_cloud.media.enums;

public enum MediaScope {
    OWN("OWN"),
    SHARED_WITH_ME("SHARED"),
    ALL("ALL"),
    DELETED("DELETED");

    private final String value;
    MediaScope(String value) {
        this.value = value;
    }
}
