package com.roosafeed.home_cloud.media.service;

import com.roosafeed.home_cloud.common.enums.ErrorCode;
import com.roosafeed.home_cloud.common.enums.SharePermission;
import com.roosafeed.home_cloud.common.exception.ApiException;
import com.roosafeed.home_cloud.media.entity.MediaFile;
import com.roosafeed.home_cloud.media.entity.MediaShare;
import com.roosafeed.home_cloud.media.repository.MediaFileRepository;
import com.roosafeed.home_cloud.media.repository.MediaShareRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
// TODO: check for hasPermission (based on permission hierarchy)
public class MediaPermissionService {
    private final MediaShareRepository mediaShareRepository;
    private final MediaFileRepository mediaFileRepository;

    public boolean hasOwnerOrSharePermission(
            @NotNull UUID mediaId,
            @NotNull UUID userId,
            @NotNull SharePermission permission) {
        // fetch the media from the repo
        MediaFile media = mediaFileRepository.findById(mediaId)
                .orElseThrow(() ->
                        new ApiException(
                                HttpStatus.NOT_FOUND,
                                ErrorCode.NOT_FOUND,
                                "Media not found"
                        )
                );

        boolean isOwner = media.getOwner().getId().equals(userId);
        boolean isShared = hasSharePermission(mediaId, userId, permission);

        return isOwner || isShared;
    }

    public boolean hasSharePermission(
            @NotNull UUID mediaId,
            @NotNull UUID sharedWithUserId,
            @NotNull SharePermission permission) {
        MediaShare share = mediaShareRepository
                .findByMediaIdAndSharedWithId(mediaId, sharedWithUserId)
                .orElse(null);

        return share != null && permission.allows(share.getPermission());
    }
}
