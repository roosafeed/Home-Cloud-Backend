package com.roosafeed.home_cloud.auth.service;

import com.roosafeed.home_cloud.auth.dto.InviteDto;
import com.roosafeed.home_cloud.auth.dto.UserDto;
import com.roosafeed.home_cloud.auth.dto.request.InviteUserRequest;
import com.roosafeed.home_cloud.auth.entity.Invite;
import com.roosafeed.home_cloud.auth.entity.User;
import com.roosafeed.home_cloud.auth.repository.InviteRepository;
import com.roosafeed.home_cloud.common.auth.context.CurrentUserProvider;
import com.roosafeed.home_cloud.config.AppProperties;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
