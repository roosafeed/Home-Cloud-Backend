package com.roosafeed.home_cloud.media.dto;

import com.roosafeed.home_cloud.auth.entity.User;
import com.roosafeed.home_cloud.common.dto.BaseDto;
import com.roosafeed.home_cloud.common.enums.SharePermission;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class MediaShareDto extends BaseDto {
    private User sharedWith;
    private SharePermission permission;
}
