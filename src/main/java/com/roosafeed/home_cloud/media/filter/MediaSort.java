package com.roosafeed.home_cloud.media.filter;

import com.roosafeed.home_cloud.common.enums.ErrorCode;
import com.roosafeed.home_cloud.common.exception.ApiException;
import org.springframework.http.HttpStatus;

import java.util.Set;

public final class MediaSort {
    // prevent initializations elsewhere
    private MediaSort() {}

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "createdAt",
            "updatedAt",
            "filename",
            "sizeBytes"
    );

    public static void validate(String sortBy) {
        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    ErrorCode.VALIDATION_ERROR,
                    "Invalid sort field: " + sortBy
            );
        }
    }
}
