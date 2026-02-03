package com.roosafeed.home_cloud.auth.controller;

import com.roosafeed.home_cloud.auth.dto.request.UserLoginRequest;
import com.roosafeed.home_cloud.auth.dto.response.UserLoginResponse;
import com.roosafeed.home_cloud.auth.service.AuthService;
import com.roosafeed.home_cloud.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<UserLoginResponse> login(@Valid @RequestBody UserLoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }
}
