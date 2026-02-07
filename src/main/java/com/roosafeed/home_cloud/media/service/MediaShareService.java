package com.roosafeed.home_cloud.media.service;

import com.roosafeed.home_cloud.auth.dto.UserDto;
import com.roosafeed.home_cloud.auth.entity.User;
import com.roosafeed.home_cloud.auth.repository.UserRepository;
import com.roosafeed.home_cloud.common.auth.context.CurrentUserProvider;
import com.roosafeed.home_cloud.common.enums.ErrorCode;
import com.roosafeed.home_cloud.common.enums.SharePermission;
import com.roosafeed.home_cloud.common.exception.ApiException;
import com.roosafeed.home_cloud.media.dto.MediaShareDto;
import com.roosafeed.home_cloud.media.dto.request.MediaShareRequest;
import com.roosafeed.home_cloud.media.entity.MediaFile;
import com.roosafeed.home_cloud.media.entity.MediaShare;
import com.roosafeed.home_cloud.media.repository.MediaShareRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaShareService {
    private final MediaShareRepository mediaShareRepository;
    private final UserRepository userRepository;

    private final MediaPermissionService mediaPermissionService;

    private final CurrentUserProvider currentUserProvider;

    private final EntityManager entityManager;

    public List<MediaShareDto> shareMediaWithUserList(UUID mediaId, List<MediaShareRequest> shareRequests) {
        // current user should have full permission
        UserDto currentUser = currentUserProvider.getCurrentUser();
        boolean hasPermission = mediaPermissionService
                .hasOwnerOrSharePermission(mediaId, currentUser.getId(), SharePermission.FULL);

        if (!hasPermission) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    ErrorCode.FORBIDDEN,
                    "Not enough permission to share"
            );
        }

        if (shareRequests.isEmpty()) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    ErrorCode.VALIDATION_ERROR,
                    "At least one user must be provided"
            );
        }

        // extract all the user IDs
        List<UUID> userIds = shareRequests.stream()
                .map(MediaShareRequest::getSharedWithUserId)
                .toList();

        // TODO: revisit: is this the best way to do this?
        List<User> users = userRepository.findAllById(userIds);
        if (users.size() != userIds.size()) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    ErrorCode.VALIDATION_ERROR,
                    "One or more users do not exist"
            );
        }

        Map<UUID, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        // fetch existing shares (if any)
        List<MediaShare> existingShares =
                mediaShareRepository.findByMediaIdAndSharedWithIdIn(mediaId, userIds);

        Map<UUID, MediaShare> existingMap =
                existingShares.stream()
                        .collect(Collectors.toMap(
                                s -> s.getSharedWith().getId(),
                                Function.identity()
                        ));

        MediaFile mediaRef = entityManager.getReference(MediaFile.class, mediaId);

        List<MediaShare> toSave = new ArrayList<>();

        for (var req : shareRequests) {

            MediaShare share = existingMap.get(req.getSharedWithUserId());

            if (share == null) {
                share = new MediaShare();
                share.setMedia(mediaRef);
                share.setSharedWith(userMap.get(req.getSharedWithUserId()));
            }

            // update or set permission
            share.setPermission(req.getPermission());

            toSave.add(share);
        }

        List<MediaShare> saved = mediaShareRepository.saveAll(toSave);

        return saved.stream()
                .map(this::toMediaShareDto)
                .toList();
    }

    public MediaShareDto getShareByMediaAndUser(UUID mediaId, UUID sharedWithUserId) {
        // DESIGN: for now each user and media pair has only one share entry since the highest permission is recorded
        // checks for at least read permission
        MediaShare mediaShare = getSharingWithoutChecks(mediaId, sharedWithUserId);

        if (mediaShare != null) {
            return toMediaShareDto(mediaShare);
        }

        return null;
    }

    public List<MediaShareDto> getSharesByMediaId(UUID mediaId) {
        // user should have full permission to see who the media is shared with
        UserDto currentUser = currentUserProvider.getCurrentUser();
        boolean hasPermission = mediaPermissionService
                .hasOwnerOrSharePermission(mediaId, currentUser.getId(), SharePermission.FULL);

        if (!hasPermission) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    ErrorCode.FORBIDDEN,
                    "Not enough permission"
            );
        }

        return mediaShareRepository.findByMediaId(mediaId)
                .stream().map(this::toMediaShareDto)
                .toList();
    }

    public void deleteShare(UUID mediaId, UUID sharedWithUserId) {
        // current user should have full permission
        UserDto currentUser = currentUserProvider.getCurrentUser();
        boolean hasPermission = mediaPermissionService
                .hasOwnerOrSharePermission(mediaId, currentUser.getId(), SharePermission.FULL);

        if (!hasPermission) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    ErrorCode.FORBIDDEN,
                    "Not enough permission to share"
            );
        }

        // make sure the share exists
        MediaShare share = getSharingWithoutChecks(mediaId, sharedWithUserId);

        if (share == null) {
            throw new ApiException(
                    HttpStatus.NOT_FOUND,
                    ErrorCode.NOT_FOUND,
                    "Media is not shared with the user"
            );
        }

        mediaShareRepository.delete(share);
    }

    private MediaShare getSharingWithoutChecks(UUID mediaId, UUID sharedWithUserId) {
        return mediaShareRepository
                .findByMediaIdAndSharedWithId(mediaId, sharedWithUserId)
                .orElse(null);
    }

    private MediaShareDto toMediaShareDto(MediaShare entity) {
        MediaShareDto dto = new MediaShareDto();

        dto.setId(entity.getId());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setPermission(entity.getPermission());

        UserDto userDto = new UserDto();
        userDto.setActive(entity.getSharedWith().isActive());
        userDto.setId(entity.getSharedWith().getId());
        userDto.setDisplayName(entity.getSharedWith().getDisplayName());
        userDto.setEmail(entity.getSharedWith().getEmail());

        dto.setSharedWith(userDto);

        return dto;
    }
}
