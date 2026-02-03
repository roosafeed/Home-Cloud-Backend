package com.roosafeed.home_cloud.common.response;

import com.roosafeed.home_cloud.common.enums.ErrorCode;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ErrorResponse {
    private Instant timestamp;
    private ErrorCode errorCode;
    private String message;
    private String path;
}
