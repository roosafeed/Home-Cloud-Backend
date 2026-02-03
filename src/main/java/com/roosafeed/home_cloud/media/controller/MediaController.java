package com.roosafeed.home_cloud.media.controller;

import com.roosafeed.home_cloud.common.enums.ErrorCode;
import com.roosafeed.home_cloud.common.exception.ApiException;
import com.roosafeed.home_cloud.common.response.ApiResponse;
import com.roosafeed.home_cloud.common.response.PagedApiResponse;
import com.roosafeed.home_cloud.media.dto.MediaFileDto;
import com.roosafeed.home_cloud.media.dto.request.MediaUploadRequest;
import com.roosafeed.home_cloud.media.dto.response.MediaSummaryDto;
import com.roosafeed.home_cloud.media.filter.MediaSearchQuery;
import com.roosafeed.home_cloud.media.service.MediaService;
import com.roosafeed.home_cloud.media.service.StreamService;
import com.roosafeed.home_cloud.media.service.ThumbnailService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
public class MediaController {
    private final MediaService mediaService;
    private final StreamService streamService;
    private final ThumbnailService thumbnailService;

    @PostMapping
    public ApiResponse<MediaFileDto> upload(
            @RequestPart("file") MultipartFile file,
            @ModelAttribute MediaUploadRequest request
    ) {
        return ApiResponse.ok(mediaService.upload(file, request));
    }

    @GetMapping
    public PagedApiResponse<MediaSummaryDto> list(
            @ModelAttribute MediaSearchQuery query
    ) {
        return PagedApiResponse.ok(mediaService.list(query));
    }

    @GetMapping("/{id}")
    public ApiResponse<MediaFileDto> getById(@PathVariable UUID id) {
        return ApiResponse.ok(mediaService.getById(id));
    }

    @GetMapping("/{id}/thumbnail")
    public ResponseEntity<byte[]> getThumbnailByMediaId(@PathVariable UUID id) {
        try {
            MediaFileDto fileDto = mediaService.getById(id);
            byte[] imageBytes = thumbnailService.getThumbnailByMediaFileId(fileDto);

            return ResponseEntity
                    .ok()
                    .contentType(MediaType.IMAGE_JPEG)      // for now all thumbnails should be JPEG
                    .contentLength(imageBytes.length)
                    .body(imageBytes);
        } catch (ApiException ex) {
            if (ErrorCode.NOT_FOUND.equals(ex.getErrorCode())) {
                return ResponseEntity.notFound().build();
            }

            throw ex;
        }
    }

    @GetMapping("/{id}/stream")
    public void stream(
            @PathVariable UUID id,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        // <video src="/api/v1/media/{id}/stream">
        MediaFileDto fileDto = mediaService.getById(id);
        streamService.stream(
                fileDto.getPath(),
                fileDto.getMimeType(),
                request,
                response
        );
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> deleteById(@PathVariable UUID id) {
        mediaService.markAsDeleted(id);
        return ApiResponse.ok(true);
    }

    @PatchMapping("/{id}/restore")
    public ApiResponse<Boolean> restoreById(@PathVariable UUID id) {
        mediaService.restoreMedia(id);
        return ApiResponse.ok(true);
    }
}
