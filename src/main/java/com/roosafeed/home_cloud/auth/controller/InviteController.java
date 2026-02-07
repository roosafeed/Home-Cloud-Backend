package com.roosafeed.home_cloud.auth.controller;

import com.roosafeed.home_cloud.auth.dto.InviteDto;
import com.roosafeed.home_cloud.auth.dto.UserDto;
import com.roosafeed.home_cloud.auth.dto.request.CreateUserRequest;
import com.roosafeed.home_cloud.auth.dto.request.InviteUserRequest;
import com.roosafeed.home_cloud.auth.service.InviteService;
import com.roosafeed.home_cloud.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/invite")
@RequiredArgsConstructor
public class InviteController {
    private final InviteService inviteService;

    @PostMapping
    public ApiResponse<InviteDto> invite(
            @Valid @RequestBody InviteUserRequest request
    ) {
        return ApiResponse.ok(inviteService.invite(request));
    }

    @PostMapping("/{token}")
    public ApiResponse<UserDto> acceptInvite(
            @PathVariable("token") String token,
            @Valid @RequestBody CreateUserRequest request
    ) {
        return ApiResponse.ok(inviteService.acceptInvite(token, request));
    }
}