package com.roosafeed.home_cloud.media.controller;

import com.roosafeed.home_cloud.common.response.ApiResponse;
import com.roosafeed.home_cloud.media.dto.MediaShareDto;
import com.roosafeed.home_cloud.media.dto.request.MediaShareRequest;
import com.roosafeed.home_cloud.media.service.MediaShareService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/media/{id}/share")
@RequiredArgsConstructor
public class MediaShareController {
    private final MediaShareService mediaShareService;

    @PostMapping
    public ApiResponse<List<MediaShareDto>> share(
            @PathVariable("id") UUID mediaId,
            @ModelAttribute List<MediaShareRequest> requests
    ) {
        return ApiResponse.ok(mediaShareService.shareMediaWithUserList(mediaId, requests));
    }

    @GetMapping
    public ApiResponse<List<MediaShareDto>> getListForMedia(@PathVariable("id") UUID mediaId) {
        return ApiResponse.ok(mediaShareService.getSharesByMediaId(mediaId));
    }

    @DeleteMapping("/user/{uid}")
    public ApiResponse<Boolean> deleteByMediaAndUser(
            @PathVariable("id") UUID mediaId,
            @PathVariable("uid") UUID userId
    ) {
        mediaShareService.deleteShare(mediaId, userId);
        return ApiResponse.ok(true);
    }
}
