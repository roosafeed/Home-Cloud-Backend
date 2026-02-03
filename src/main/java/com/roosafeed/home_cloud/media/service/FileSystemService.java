package com.roosafeed.home_cloud.media.service;

import com.roosafeed.home_cloud.common.enums.ErrorCode;
import com.roosafeed.home_cloud.common.exception.ApiException;
import com.roosafeed.home_cloud.config.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileSystemService {
    private final AppProperties appProperties;
    private Path mediaRoot;

    /*
        Invoked on startup by an application runner
     */
    public void initializeMediaRoot() {
        String rootPath = appProperties.getMedia().getRootPath();

        if (rootPath == null || rootPath.isBlank()) {
            log.error("Media root is not configured. Configure one in the properties");
            throw new IllegalStateException("Media root is not configured");
        }

        try {
            mediaRoot = Paths.get(rootPath).toAbsolutePath().normalize();

            // if root doesn't exist, create it
            if (Files.notExists(mediaRoot)) {
                Files.createDirectories(mediaRoot);
                log.info("Media root folder created at {}", mediaRoot);
            }

            // root exists, but is not a directory
            if (!Files.isDirectory(mediaRoot)) {
                log.error("Media root is not a directory. Set the root path as a directory path");
                throw new IllegalStateException("Media root is not a directory");
            }

            // we don't have enough permissions on the root directory
            if (!Files.isReadable(mediaRoot) || !Files.isWritable(mediaRoot)) {
                log.error("Not enough permissions on the root directory.");
                throw new IllegalStateException("Not enough permissions on the root directory");
            }

            log.info("Media root initialized at {}", mediaRoot);

        } catch (IOException ex) {
            log.error("Failed to initialize media root folder", ex);
            throw new IllegalStateException("Failed to initialize media root folder", ex);
        }
    }

    // Resolves a relative path safely against the media root.
    // Prevents path traversal.
    public Path resolveRelativePath(String relativePath) {
        try {
            Path resolved = mediaRoot
                    .resolve(relativePath == null ? "" : relativePath)
                    .normalize();

            if (!resolved.startsWith(mediaRoot)) {
                throw new IllegalArgumentException("Invalid relative path");
            }

            return resolved;
        } catch (InvalidPathException ex) {
            throw new IllegalArgumentException("Invalid path format");
        }
    }

    public Path resolveRelativePath(Path relativePath) {
        return this.resolveRelativePath(relativePath.toString());
    }

    public Path saveFile(String relativePath, String filename, InputStream data) {
        Path directory = resolveRelativePath(relativePath);
        Path targetFile = getTargetFile(relativePath, filename);

        try {
            Files.createDirectories(directory);

            Files.copy(
                    data,
                    targetFile,
                    StandardCopyOption.REPLACE_EXISTING
            );

            log.debug("File saved: {}", targetFile);

            return targetFile;

        } catch (IOException ex) {
            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorCode.INTERNAL_ERROR,
                    "Failed to save file"
            );
        }
    }

    public void createDirectories(Path path) throws IOException {
        if (!path.startsWith(mediaRoot)) {
            throw new IllegalArgumentException("Invalid file path");
        }

        if (this.exists(path)) {
            return;
        }

        Files.createDirectories(path);
    }

    public void deleteFile(Path targetFile) {
        if (!targetFile.startsWith(mediaRoot)) {
            throw new IllegalArgumentException("Invalid file path");
        }

        try {
            if (Files.exists(targetFile)) {
                Files.delete(targetFile);
                log.debug("File deleted: {}", targetFile);
            }

        } catch (IOException ex) {
            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorCode.INTERNAL_ERROR,
                    "Failed to delete file"
            );
        }
    }

    private Boolean exists(Path path) {
        if (!path.startsWith(mediaRoot)) {
            throw new IllegalArgumentException("Invalid file path");
        }

        return Files.exists(path);
    }

    public Boolean exists(Path path, Boolean isRelativePath) {
        if (isRelativePath) {
            path = this.resolveRelativePath(path);
        }

        return this.exists(path);
    }

    public InputStream readFile(Path targetFile) {
        if (!targetFile.startsWith(mediaRoot)) {
            throw new IllegalArgumentException("Invalid file path");
        }

        if (!Files.exists(targetFile) || !Files.isRegularFile(targetFile)) {
            throw new ApiException(
                    HttpStatus.NOT_FOUND,
                    ErrorCode.NOT_FOUND,
                    "File not found"
            );
        }

        try {
            return Files.newInputStream(targetFile, StandardOpenOption.READ);

        } catch (IOException ex) {
            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorCode.INTERNAL_ERROR,
                    "Failed to read file"
            );
        }
    }

    public String getRelativePath(Path fullPath) {
        return mediaRoot.relativize(fullPath).toString();
    }

    private Path getTargetFile(String relativePath, String filename) {
        Path directory = resolveRelativePath(relativePath);
        Path targetFile = directory.resolve(filename).normalize();

        if (!targetFile.startsWith(mediaRoot)) {
            throw new IllegalArgumentException("Invalid file path");
        }
        return targetFile;
    }

}
