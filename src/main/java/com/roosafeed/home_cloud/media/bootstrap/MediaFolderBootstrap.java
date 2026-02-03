package com.roosafeed.home_cloud.media.bootstrap;

import com.roosafeed.home_cloud.media.service.FileSystemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MediaFolderBootstrap implements ApplicationRunner {
    private final FileSystemService fileSystemService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Attempting to initialise the media root directory");
        fileSystemService.initializeMediaRoot();
    }
}
