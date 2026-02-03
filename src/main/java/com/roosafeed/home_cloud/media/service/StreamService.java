package com.roosafeed.home_cloud.media.service;

import com.roosafeed.home_cloud.config.AppProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Service
@RequiredArgsConstructor
public class StreamService {
    private final AppProperties appProperties;

    private final FileSystemService fileSystemService;

    public void stream(
            String filePath,
            String contentType,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        Path path = fileSystemService.resolveRelativePath(filePath);
        this.stream(
                path,
                contentType,
                request,
                response
        );
    }

    public void stream(
            Path filePath,
            String contentType,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        long fileLength = Files.size(filePath);
        String rangeHeader = request.getHeader("Range");

        long start = 0;
        long end = fileLength - 1;

        if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
            String[] ranges = rangeHeader.substring(6).split("-");
            start = Long.parseLong(ranges[0]);

            if (ranges.length > 1 && !ranges[1].isEmpty()) {
                end = Long.parseLong(ranges[1]);
            }

            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        }
        else {
            response.setStatus(HttpServletResponse.SC_OK);
        }

        long contentLength = end - start + 1;

        response.setHeader("Content-Type", contentType);
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("Content-Length", String.valueOf(contentLength));
        response.setHeader(
                "Content-Range",
                "bytes " + start + "-" + end + "/" + fileLength
        );

        try (
                RandomAccessFile raf = new RandomAccessFile(filePath.toFile(), "r");
                OutputStream outputStream = response.getOutputStream()
        ) {
            raf.seek(start);

            int bufferSizeBytes = appProperties.getStreaming().getChunkSizeKb() * 1024;
            byte[] buffer = new byte[bufferSizeBytes];
            long bytesRemaining = contentLength;
            int bytesRead;

            while (bytesRemaining > 0 &&
                    (bytesRead = raf.read(
                            buffer,
                            0,
                            (int) Math.min(buffer.length, bytesRemaining)
                    )) != -1) {

                outputStream.write(buffer, 0, bytesRead);
                bytesRemaining -= bytesRead;
            }

            outputStream.flush();
        }
    }
}
