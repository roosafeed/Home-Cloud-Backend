package com.roosafeed.home_cloud.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InviteUserRequest {
    @Email
    @NotBlank
    private String email;
}
