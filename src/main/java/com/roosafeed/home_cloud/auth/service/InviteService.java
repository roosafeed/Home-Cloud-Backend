package com.roosafeed.home_cloud.auth.service;

import com.roosafeed.home_cloud.auth.dto.InviteDto;
import com.roosafeed.home_cloud.auth.dto.UserDto;
import com.roosafeed.home_cloud.auth.dto.request.CreateUserRequest;
import com.roosafeed.home_cloud.auth.dto.request.InviteUserRequest;
import com.roosafeed.home_cloud.auth.entity.Invite;
import com.roosafeed.home_cloud.auth.entity.User;
import com.roosafeed.home_cloud.auth.repository.InviteRepository;
import com.roosafeed.home_cloud.common.auth.context.CurrentUserProvider;
import com.roosafeed.home_cloud.common.enums.ErrorCode;
import com.roosafeed.home_cloud.common.enums.UserRole;
import com.roosafeed.home_cloud.common.exception.ApiException;
import com.roosafeed.home_cloud.config.AppProperties;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class InviteService {
    private final InviteRepository inviteRepository;
    private final UserService userService;
    private final CurrentUserProvider currentUserProvider;
    private final AppProperties appProperties;
    private final EntityManager entityManager;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int TOKEN_BYTES = 32; // 256 bits

    public InviteDto invite(InviteUserRequest request) {
        // We do not want to throw error if the invited user already exists

        UserDto currentUser = currentUserProvider.getCurrentUser();

        User user = entityManager.getReference(User.class, currentUser.getId());

        int expireHours = appProperties.getSecurity().getInviteTokenTtlHours();
        Instant now = Instant.now();
        Instant expireAt = now.plus(Duration.ofHours(expireHours));

        Invite invite = new Invite();
        invite.setInvitedBy(user);
        invite.setEmail(request.getEmail());
        invite.setExpiresAt(expireAt);
        invite.setToken(generateUniqueToken());

        invite = inviteRepository.save(invite);

        return toInviteDto(invite);
    }

    public UserDto acceptInvite(String inviteToken, CreateUserRequest request) {
        // validate request
        Invite invite = inviteRepository.findByToken(inviteToken)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.BAD_REQUEST,
                        ErrorCode.VALIDATION_ERROR,
                        "Invalid invite token"
                ));

        Instant now = Instant.now();
        boolean isExpiredAt = invite.getExpiresAt() != null && now.isAfter(invite.getExpiresAt());
        boolean isUsed = invite.getUsedAt() != null && now.isAfter(invite.getUsedAt());

        if (isExpiredAt || isUsed) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    ErrorCode.VALIDATION_ERROR,
                    "The invite token has been expired"
            );
        }

        if (userService.userExists(request.getEmail())) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    ErrorCode.VALIDATION_ERROR,
                    "User with the email already exists"
            );
        }

        return userService.saveUser(request, UserRole.USER);
    }

    private String generate() {
        byte[] bytes = new byte[TOKEN_BYTES];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String generateUniqueToken() {
        for (int i = 0; i < 5; i++) {
            String token = generate();
            if (!inviteRepository.existsByToken(token)) {
                return token;
            }
        }

        throw new IllegalStateException("Failed to generate unique invite token");
    }


    private InviteDto toInviteDto(Invite entity) {
        InviteDto dto = new InviteDto();
        UserDto userDto = new UserDto();
        userDto.setActive(entity.getInvitedBy().isActive());
        userDto.setDisplayName(entity.getInvitedBy().getDisplayName());
        userDto.setId(entity.getInvitedBy().getId());
        userDto.setEmail(entity.getInvitedBy().getEmail());

        dto.setId(entity.getId());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setInvitedBy(userDto);
        dto.setEmail(entity.getEmail());
        dto.setToken(entity.getToken());
        dto.setExpiresAt(entity.getExpiresAt());
        dto.setUsedAt(entity.getUsedAt());

        return dto;
    }
}
