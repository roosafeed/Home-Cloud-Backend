package com.roosafeed.home_cloud.media.dto.response;

import com.roosafeed.home_cloud.media.enums.MediaType;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class MediaSummaryDto {
    private UUID id;
    private String path;
    private String filename;
    private MediaType mediaType;
    private long sizeBytes;
    private Instant createdAt;
}
