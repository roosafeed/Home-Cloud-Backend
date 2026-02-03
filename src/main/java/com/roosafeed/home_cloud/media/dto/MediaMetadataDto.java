package com.roosafeed.home_cloud.media.dto;

import com.roosafeed.home_cloud.common.dto.BaseDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
public class MediaMetadataDto extends BaseDto {
    private Map<String, Object> metadata;
}
