package com.roosafeed.home_cloud.media.enums;

import lombok.Getter;

@Getter
public enum MediaType {
    IMAGE("Image"),
    VIDEO("Video"),
    OTHER("Other");

    private final String value;

    MediaType(String value) {
        this.value = value;
    }
}
