package com.roosafeed.home_cloud.auth.dto;

import com.roosafeed.home_cloud.common.dto.BaseDto;
import com.roosafeed.home_cloud.common.enums.UserRole;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserDto extends BaseDto {
    private String email;
    private String displayName;
    private UserRole role;
    private boolean active;
}
