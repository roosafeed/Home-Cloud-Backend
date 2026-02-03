package com.roosafeed.home_cloud.media.dto.request;

import com.roosafeed.home_cloud.common.enums.SharePermission;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class MediaShareRequest {
    @NotBlank
    private UUID sharedWithUserId;

    @NotBlank
    private SharePermission permission;
}
