package com.roosafeed.home_cloud.auth.service;

import com.roosafeed.home_cloud.auth.dto.UserDto;
import com.roosafeed.home_cloud.auth.dto.request.CreateUserRequest;
import com.roosafeed.home_cloud.auth.entity.User;
import com.roosafeed.home_cloud.auth.repository.UserRepository;
import com.roosafeed.home_cloud.common.enums.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public boolean userExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean userExists(UUID userId) {
        return userRepository.existsById(userId);
    }

    public UserDto getUserById(UUID userId) {
        User user = userRepository.findById(userId).orElse(null);

        return userEntityToUserDto(user);
    }

    public UserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email).orElse(null);

        return userEntityToUserDto(user);
    }

    public User getUserEntityByEmail(String email) {
        // for auth mostly
        return userRepository.findByEmail(email).orElse(null);
    }

    public UserDto saveUser(CreateUserRequest userRequest, UserRole role) {
        if (!StringUtils.hasText(userRequest.getEmail())
                || !StringUtils.hasText(userRequest.getPassword())
        ) {
            throw new IllegalArgumentException("Email and password are required to create a new user");
        }

        User user = new User();
        user.setActive(true);
        user.setEmail(userRequest.getEmail());
        user.setDisplayName(userRequest.getDisplayName());
        user.setPasswordHash(passwordEncoder.encode(userRequest.getPassword()));
        user.setRole(role);

        user = userRepository.save(user);

        return userEntityToUserDto(user);
    }

    public UserDto userEntityToUserDto(User user) {
        if (user == null) {
            return null;
        }

        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setEmail(user.getEmail());
        userDto.setActive(user.isActive());
        userDto.setRole(user.getRole());
        userDto.setDisplayName(user.getDisplayName());
        userDto.setCreatedAt(user.getCreatedAt());
        userDto.setUpdatedAt(user.getUpdatedAt());

        return userDto;
    }
}
