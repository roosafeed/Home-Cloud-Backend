package com.roosafeed.home_cloud.media.dto;

import com.roosafeed.home_cloud.auth.dto.UserDto;
import com.roosafeed.home_cloud.common.dto.BaseDto;
import com.roosafeed.home_cloud.common.enums.SharePermission;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class MediaShareDto extends BaseDto {
    private UserDto sharedWith;
    private SharePermission permission;
}
