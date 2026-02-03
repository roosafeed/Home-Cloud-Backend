package com.roosafeed.home_cloud.auth.service;

import com.roosafeed.home_cloud.auth.dto.request.UserLoginRequest;
import com.roosafeed.home_cloud.auth.dto.response.UserLoginResponse;
import com.roosafeed.home_cloud.auth.entity.User;
import com.roosafeed.home_cloud.common.enums.ErrorCode;
import com.roosafeed.home_cloud.common.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final PasswordEncoder passwordEncoder;

    private final UserService userService;
    private final JwtService jwtService;

    public UserLoginResponse login(UserLoginRequest request) {
        if (!StringUtils.hasText(request.getEmail())
                || !StringUtils.hasText(request.getPassword())
        ) {
            throw new IllegalArgumentException("Email and password are required.");
        }

        User user = userService.getUserEntityByEmail(request.getEmail());

        if (user == null) {
            throw new ApiException(
                    HttpStatus.UNAUTHORIZED,
                    ErrorCode.UNAUTHORIZED,
                    "Invalid credentials"
            );
        }

        if (!user.isActive() ||
                !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ApiException(
                    HttpStatus.UNAUTHORIZED,
                    ErrorCode.UNAUTHORIZED,
                    "Invalid credentials"
            );
        }

        String token = jwtService.generateToken(user);

        UserLoginResponse response = new UserLoginResponse();
        response.setToken(token);
        response.setUser(userService.userEntityToUserDto(user));

        return response;
    }
}
