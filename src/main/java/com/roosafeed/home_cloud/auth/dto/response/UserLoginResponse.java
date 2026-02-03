package com.roosafeed.home_cloud.auth.dto.response;

import com.roosafeed.home_cloud.auth.dto.UserDto;
import lombok.Data;

@Data
public class UserLoginResponse {
    private String token;
    private UserDto user;
}
