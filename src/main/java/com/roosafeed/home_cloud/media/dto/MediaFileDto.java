package com.roosafeed.home_cloud.media.dto;

import com.roosafeed.home_cloud.common.dto.BaseDto;
import com.roosafeed.home_cloud.media.enums.MediaType;
import com.roosafeed.home_cloud.common.enums.MediaVisibility;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class MediaFileDto extends BaseDto {
    private String filename;
    private String mimeType;
    private String path;
    private long sizeBytes;

    private MediaType mediaType;
    private MediaVisibility visibility;

    private String ownerDisplayName;
}
