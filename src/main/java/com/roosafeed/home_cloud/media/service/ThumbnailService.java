package com.roosafeed.home_cloud.media.service;

import com.roosafeed.home_cloud.common.enums.ErrorCode;
import com.roosafeed.home_cloud.common.exception.ApiException;
import com.roosafeed.home_cloud.media.dto.MediaFileDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

@Slf4j
@Service
@RequiredArgsConstructor
// Separate class from the generator - separate responsibility and easier async calls
public class ThumbnailService {
    private final ThumbnailGeneratorService thumbnailGeneratorService;
    private final FileSystemService fileSystemService;

    public byte[] getThumbnailByMediaFileId(MediaFileDto fileDto) {
        Path thumbnailPath = thumbnailGeneratorService.resolveThumbnailPath(fileDto.getId());

        if (fileSystemService.exists(thumbnailPath, false)) {
            try (InputStream in = fileSystemService.readFile(thumbnailPath)) {
                return in.readAllBytes();
            } catch (IOException ex) {
                log.error("Internal server error while trying to fetch the thumbnail for media {}", fileDto.getId(), ex);
                throw new ApiException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        ErrorCode.INTERNAL_ERROR,
                        "Internal server error while trying to fetch the thumbnail"
                );
            }
        }

        // try to generate the thumbnail for later use
        thumbnailGeneratorService.generateThumbnailAsync(fileDto);

        // error for now
        throw new ApiException(
                HttpStatus.NOT_FOUND,
                ErrorCode.NOT_FOUND,
                "Thumbnail not found"
        );
    }
}
