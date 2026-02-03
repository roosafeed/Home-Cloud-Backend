package com.roosafeed.home_cloud.media.service;

import com.roosafeed.home_cloud.auth.dto.UserDto;
import com.roosafeed.home_cloud.auth.entity.User;
import com.roosafeed.home_cloud.common.auth.context.CurrentUserProvider;
import com.roosafeed.home_cloud.common.enums.ErrorCode;
import com.roosafeed.home_cloud.common.enums.MediaVisibility;
import com.roosafeed.home_cloud.common.enums.SharePermission;
import com.roosafeed.home_cloud.common.exception.ApiException;
import com.roosafeed.home_cloud.config.AppProperties;
import com.roosafeed.home_cloud.media.dto.MediaFileDto;
import com.roosafeed.home_cloud.media.dto.request.MediaUploadRequest;
import com.roosafeed.home_cloud.media.dto.response.MediaSummaryDto;
import com.roosafeed.home_cloud.media.entity.MediaFile;
import com.roosafeed.home_cloud.media.enums.MediaScope;
import com.roosafeed.home_cloud.media.enums.MediaType;
import com.roosafeed.home_cloud.media.filter.MediaSearchQuery;
import com.roosafeed.home_cloud.media.filter.MediaSort;
import com.roosafeed.home_cloud.media.filter.MediaSpecifications;
import com.roosafeed.home_cloud.media.repository.MediaFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaService {
    private final MediaFileRepository mediaFileRepository;

    private final MediaPermissionService mediaPermissionService;
    private final FileSystemService fileSystemService;
    private final ThumbnailGeneratorService thumbnailGeneratorService;

    private final CurrentUserProvider currentUserProvider;

    private final AppProperties appProperties;

    public MediaFileDto upload(MultipartFile file, MediaUploadRequest request) {
        /* TODO:
        * 1. check for existing filename (same path + user combo) -> if a different user has a file with the same
        *   name, then append something to the end of the filename in the filesystem
        * 2. metadata extraction -> it will be an async job. decide if the job will simply scan for missing metadata
        *   or we have to setup a messaging board of sorts
        * 3. limit filename length (max 255 including extension)
        * 4. FUTURE: Chunked uploads for really big files
        */
        if (file == null || file.isEmpty()) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    ErrorCode.VALIDATION_ERROR,
                    "File is required"
            );
        }

        if (!StringUtils.hasText(request.getFilename())) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    ErrorCode.VALIDATION_ERROR,
                    "Invalid filename"
            );
        }

        UserDto owner = currentUserProvider.getCurrentUser();
        // TODO: change this to something unique later to prevent collisions
        String fsFilename = request.getFilename();

        try {
            // Save file to filesystem
            Path savedPath = fileSystemService.saveFile(
                    request.getPath(),
                   fsFilename,
                    file.getInputStream()
            );

            User user = new User();
            user.setId(owner.getId());
            user.setEmail(owner.getEmail());

            // Persist DB record
            MediaFile media = new MediaFile();
            media.setFilename(request.getFilename());
            media.setFsFilename(fsFilename);
            media.setExtension(extractExtension(request.getFilename()));
            media.setMimeType(file.getContentType());
            media.setSizeBytes(file.getSize());
            media.setMediaType(detectMediaType(file.getContentType()));
            media.setVisibility(MediaVisibility.PRIVATE);
            media.setOwner(user);
            media.setPath(fileSystemService.getRelativePath(savedPath));

            MediaFile saved = mediaFileRepository.save(media);

            MediaFileDto dto = toMediaFileDto(saved);
            dto.setOwnerDisplayName(owner.getDisplayName());

            // asynchronously generate the thumbnail
            thumbnailGeneratorService.generateThumbnailAsync(media);

            return dto;
        } catch (IOException ex) {
            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorCode.INTERNAL_ERROR,
                    "Failed to upload file"
            );
        }
    }

    public Page<MediaSummaryDto> list(MediaSearchQuery query) {
        UserDto currentUserDto = currentUserProvider.getCurrentUser();
        User currentUser = new User();
        currentUser.setId(currentUserDto.getId());
        currentUser.setEmail(currentUserDto.getEmail());

        // Validate sort field
        MediaSort.validate(query.getSortBy());

        Pageable pageable = PageRequest.of(
                query.getPage(),
                query.getSize(),
                Sort.by(query.getDirection(), query.getSortBy())
        );

        Specification<MediaFile> spec = Specification
                .where(MediaSpecifications.isDeleted(MediaScope.DELETED.equals(query.getScope())))
                .and(MediaSpecifications.ownershipScope(query.getScope(), currentUser))
                .and(MediaSpecifications.onPath(correctPathSlashes(query.getPath())));

        Page<MediaFile> page = mediaFileRepository.findAll(spec, pageable);

        return page.map(this::toSummaryDto);
    }

    // candidate for caching
    public MediaFileDto getById(UUID mediaId) {
        UserDto currentUser = currentUserProvider.getCurrentUser();

        MediaFile media = mediaFileRepository.findById(mediaId)
                .orElseThrow(() ->
                        new ApiException(
                                HttpStatus.NOT_FOUND,
                                ErrorCode.NOT_FOUND,
                                "Media not found"
                        )
                );

        if (Boolean.TRUE.equals(media.getMarkedAsDeleted())) {
            throw new ApiException(
                    HttpStatus.NOT_FOUND,
                    ErrorCode.NOT_FOUND,
                    "Media not found"
            );
        }

        // Owner always has access
        if (media.getOwner().getId().equals(currentUser.getId())) {
            return toMediaFileDto(media);
        }

        // Check sharing
        boolean shared = mediaPermissionService
                .hasSharePermission(mediaId, currentUser.getId(), SharePermission.READ);

        if (!shared) {
            throw new ApiException(
                    HttpStatus.NOT_FOUND,
                    ErrorCode.NOT_FOUND,
                    "Media not found"
            );
        }

        return toMediaFileDto(media);
    }

    public void markAsDeleted(UUID mediaId) {
        markMediaDeletedStatus(mediaId, true);
    }

    public void restoreMedia(UUID mediaId) {
        markMediaDeletedStatus(mediaId, false);
    }

    private void markMediaDeletedStatus(UUID mediaId, boolean isDeleted) {
        UserDto currentUser = currentUserProvider.getCurrentUser();

        MediaFile media = mediaFileRepository.findById(mediaId)
                .orElseThrow(() ->
                        new ApiException(
                                HttpStatus.NOT_FOUND,
                                ErrorCode.NOT_FOUND,
                                "Media not found"
                        )
                );

        if (Boolean.TRUE.equals(media.getMarkedAsDeleted())) {
            throw new ApiException(
                    HttpStatus.NOT_FOUND,
                    ErrorCode.NOT_FOUND,
                    "Media not found"
            );
        }

        // Check sharing permission
        boolean hasDeletePermission = mediaPermissionService
                .hasSharePermission(mediaId, currentUser.getId(), SharePermission.FULL);

        boolean isOwner = media.getOwner().getId().equals(currentUser.getId());

        if (isOwner || hasDeletePermission) {
            // mark as deleted
            media.setMarkedAsDeleted(isDeleted);
            mediaFileRepository.save(media);
            return;
        }

        throw new ApiException(
                HttpStatus.FORBIDDEN,
                ErrorCode.FORBIDDEN,
                "Not enough permission to delete"
        );
    }

    private String extractExtension(String filename) {
        int idx = filename.lastIndexOf('.');
        return idx > 0 ? filename.substring(idx + 1) : "";
    }

    private MediaType detectMediaType(String mimeType) {
        if (mimeType == null) return MediaType.OTHER;
        if (mimeType.startsWith("image/")) return MediaType.IMAGE;
        if (mimeType.startsWith("video/")) return MediaType.VIDEO;
        return MediaType.OTHER;
    }

    private MediaFileDto toMediaFileDto(MediaFile media) {
        MediaFileDto dto = new MediaFileDto();
        dto.setId(media.getId());
        dto.setCreatedAt(media.getCreatedAt());
        dto.setUpdatedAt(media.getUpdatedAt());
        dto.setFilename(media.getFilename());
        dto.setPath(media.getPath());
        dto.setMimeType(media.getMimeType());
        dto.setSizeBytes(media.getSizeBytes());
        dto.setMediaType(media.getMediaType());
        dto.setVisibility(media.getVisibility());
        dto.setOwnerDisplayName(media.getOwner().getDisplayName());
        return dto;
    }

    private MediaSummaryDto toSummaryDto(MediaFile media) {
        MediaSummaryDto dto = new MediaSummaryDto();
        dto.setId(media.getId());
        dto.setPath(media.getPath());
        dto.setFilename(media.getFilename());
        dto.setMediaType(media.getMediaType());
        dto.setSizeBytes(media.getSizeBytes());
        dto.setCreatedAt(media.getCreatedAt());
        return dto;
    }

    private String correctPathSlashes(String path) {
        if (path == null) {
            return null;
        }
        return path.replace("/", "\\");
    }
}
