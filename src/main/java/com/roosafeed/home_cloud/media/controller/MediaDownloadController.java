package com.roosafeed.home_cloud.media.controller;

import com.roosafeed.home_cloud.media.dto.MediaFileDto;
import com.roosafeed.home_cloud.media.service.FileSystemService;
import com.roosafeed.home_cloud.media.service.MediaService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
public class MediaDownloadController {
    private final MediaService mediaService;
    private final FileSystemService fileSystemService;

    @GetMapping("/{id}/download")
    public void download(
            @PathVariable UUID id,
            HttpServletResponse response
    ) throws Exception {
        MediaFileDto fileDto = mediaService.getById(id);
        Path filePath = fileSystemService.resolveRelativePath(fileDto.getPath());

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(fileDto.getMimeType());
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=\"" + fileDto.getFilename() + "\""
        );
        response.setHeader(
                "Content-Length",
                String.valueOf(fileDto.getSizeBytes())
        );

        try (InputStream in = fileSystemService.readFile(filePath)) {
            in.transferTo(response.getOutputStream());
        }
    }
}
