package com.roosafeed.home_cloud.media.service;

import com.roosafeed.home_cloud.config.AppProperties;
import com.roosafeed.home_cloud.media.dto.MediaFileDto;
import com.roosafeed.home_cloud.media.entity.MediaFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ThumbnailGeneratorService {
    private final AppProperties appProperties;

    private final FileSystemService fileSystemService;

    @Async("thumbnailExecutor")
    public void generateThumbnailAsync(MediaFile media) {
        doGenerate(media);
    }

    @Async("thumbnailExecutor")
    public void generateThumbnailAsync(MediaFileDto mediaFileDto) {
        MediaFile media = new MediaFile();
        media.setId(mediaFileDto.getId());
        media.setMediaType(mediaFileDto.getMediaType());
        media.setPath(mediaFileDto.getPath());

        doGenerate(media);
    }

    private void doGenerate(MediaFile media) {
        Path thumbnailPath = this.resolveThumbnailPath(media.getId());

        if (fileSystemService.exists(thumbnailPath, false)) {
            return; // already generated -> but would we want to overwrite? will the source change?
        }

        try {
            fileSystemService.createDirectories(thumbnailPath.getParent());

            switch (media.getMediaType()) {
                case IMAGE -> generateImageThumbnail(media, thumbnailPath);
                case VIDEO -> generateVideoThumbnail(media, thumbnailPath);
                default -> {
                    // no thumbnail for now
                    // TODO: support thumbnails for other formats
                }
            }
        } catch (IOException | InterruptedException ex) {
            // no errors thrown - fail silently
            // TODO: future: implement retry
            log.warn(
                    "Thumbnail generation failed for media {}: {}",
                    media.getId(),
                    ex.getMessage()
            );
        }
    }

    private void generateImageThumbnail(MediaFile media, Path thumbnailPath) throws IOException {
        Path source = fileSystemService.resolveRelativePath(media.getPath());

        try (InputStream in = fileSystemService.readFile(source)) {
            BufferedImage ogImage = ImageIO.read(in);

            if (ogImage == null) {
                // nothing to do here, return
                return;
            }

            BufferedImage resized = this.resize(ogImage, appProperties.getMedia().getThumbnail().getMaxSizePx());
            ImageIO.write(resized, "jpg", thumbnailPath.toFile());

        }
    }

    private void generateVideoThumbnail(MediaFile media, Path thumbnailPath) throws IOException, InterruptedException {
        Path source = fileSystemService.resolveRelativePath(media.getPath());
        String absoluteVidPath = source.toAbsolutePath().toString();
        String absoluteThumbPath = thumbnailPath.toAbsolutePath().toString();

        // try to extract the frame at 2 sec
        if (!runFfmpeg(absoluteVidPath, absoluteThumbPath,
                "-ss", "2")) {

            // Fallback: last frame (works even for very short videos)
            if (!runFfmpeg(absoluteVidPath, absoluteThumbPath,
                    "-sseof", "-0.1")) {

                // Final fallback: first frame
                if (!runFfmpeg(absoluteVidPath, absoluteThumbPath)) {
                    throw new RuntimeException("Failed to generate video thumbnail");
                }
            }
        }

        if (!fileSystemService.exists(thumbnailPath, false)) {
            throw new RuntimeException("Video thumbnail file not created");
        }
    }

    private boolean runFfmpeg(
            String absoluteVideoPath,
            String absoluteThumbnailPath,
            String... seekArgs
    ) throws IOException, InterruptedException {

        List<String> command = new ArrayList<>();
        command.add("ffmpeg");
        command.add("-y");

        command.addAll(List.of(seekArgs));

        command.add("-i");
        command.add(absoluteVideoPath);

        command.add("-frames:v");
        command.add("1");

        command.add("-vf");
        command.add("scale=256:-1");

        command.add(absoluteThumbnailPath);

        ProcessBuilder pb = new ProcessBuilder(command);

        // ffmpeg is very noisy -> merge stdout & stderr
        pb.redirectErrorStream(true);

        Process process = pb.start();

        // Consume output to avoid buffer deadlock
        try (var reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            while (reader.readLine() != null) {
                // discard output
            }
        }

        // exit code 0 = happy
        return process.waitFor() == 0;
    }

    private BufferedImage resize(BufferedImage src, int maxSize) {
        int width = src.getWidth();
        int height = src.getHeight();

        float scale = Math.min(
                (float) maxSize / width,
                (float) maxSize / height
        );

        int newW = Math.round(width * scale);
        int newH = Math.round(height * scale);

        BufferedImage out = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();

        g.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR
        );
        g.drawImage(src, 0, 0, newW, newH, null);
        g.dispose();

        return out;
    }

    public Path resolveThumbnailPath(UUID mediaId) {
        return fileSystemService.resolveRelativePath(appProperties.getMedia().getThumbnail().getDirectory())
                .resolve(mediaId.toString() + ".jpg")
                .normalize();
    }
}
