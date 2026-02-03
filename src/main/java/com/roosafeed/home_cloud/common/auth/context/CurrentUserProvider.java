package com.roosafeed.home_cloud.common.auth.context;

import com.roosafeed.home_cloud.auth.dto.UserDto;
import com.roosafeed.home_cloud.auth.service.UserService;
import com.roosafeed.home_cloud.common.enums.ErrorCode;
import com.roosafeed.home_cloud.common.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CurrentUserProvider {
    private final UserService userService;

    public UserDto getCurrentUser() {
        Object principal = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        if (!(principal instanceof UUID userId)) {
            throw new ApiException(
                    HttpStatus.UNAUTHORIZED,
                    ErrorCode.UNAUTHORIZED,
                    "Unauthenticated"
            );
        }

        return userService.getUserById(userId);
    }
}
