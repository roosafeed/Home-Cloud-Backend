package com.roosafeed.home_cloud.auth.dto;

import com.roosafeed.home_cloud.common.dto.BaseDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

@EqualsAndHashCode(callSuper = true)
@Data
public class InviteDto extends BaseDto {
    private String email;
    private String token;
    private UserDto invitedBy;
    private Instant expiresAt;
    private Instant usedAt;
}
