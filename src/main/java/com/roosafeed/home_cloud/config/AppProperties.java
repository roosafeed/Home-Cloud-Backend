package com.roosafeed.home_cloud.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private Media media = new Media();
    private Security security = new Security();
    private Streaming streaming = new Streaming();

    @Data
    public static class Media {
        // Example (Windows): C:/home-cloud/media
        private String rootPath;
        private Boolean scanOnStartup = true;
        private Boolean watchEnabled = true;
        private List<String> ignoredExtensions = new ArrayList<>();
        private Thumbnail thumbnail = new Thumbnail();
    }

    @Data
    public static class Security {
        private int inviteTokenTtlHours = 48;
        private Admin admin = new Admin();
        private Jwt jwt = new Jwt();
    }

    @Data
    public static class Streaming {
        private int chunkSizeKb = 1024;
    }

    @Data
    public static class Admin {
        private String email;
        private String password;
    }

    @Data
    public static class Jwt {
        private String secret;
        private int tokenTtlHours = 24;
        private String issuer = "home-cloud";
    }

    @Data
    public static class Thumbnail {
        private int maxSizePx;
        private String directory;
    }
}
