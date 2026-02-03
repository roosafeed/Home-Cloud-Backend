package com.roosafeed.home_cloud.media.service;

import com.roosafeed.home_cloud.auth.dto.UserDto;
import com.roosafeed.home_cloud.auth.entity.User;
import com.roosafeed.home_cloud.common.auth.context.CurrentUserProvider;
import com.roosafeed.home_cloud.common.enums.ErrorCode;
import com.roosafeed.home_cloud.common.enums.SharePermission;
import com.roosafeed.home_cloud.common.exception.ApiException;
import com.roosafeed.home_cloud.media.dto.MediaShareDto;
import com.roosafeed.home_cloud.media.dto.request.MediaShareRequest;
import com.roosafeed.home_cloud.media.entity.MediaFile;
import com.roosafeed.home_cloud.media.entity.MediaShare;
import com.roosafeed.home_cloud.media.repository.MediaShareRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaShareService {
    private final MediaShareRepository mediaShareRepository;

    private final MediaPermissionService mediaPermissionService;

    private final CurrentUserProvider currentUserProvider;

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

        MediaFile media = new MediaFile();
        media.setId(mediaId);

        List<MediaShare> mediaShares = new ArrayList<>();

        for (var request : shareRequests) {
            // TODO: verify the user exists
            User user = new User();
            user.setId(request.getSharedWithUserId());

            MediaShare mediaShare = new MediaShare();
            mediaShare.setMedia(media);
            mediaShare.setSharedWith(user);
            mediaShare.setPermission(request.getPermission());

            mediaShares.add(mediaShare);
        }

        mediaShares = mediaShareRepository.saveAll(mediaShares);

        return mediaShares.stream().map(this::toMediaShareDto).toList();
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
        dto.setSharedWith(entity.getSharedWith());
        dto.setPermission(entity.getPermission());

        return dto;
    }
}
