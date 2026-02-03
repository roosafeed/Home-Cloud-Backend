package com.roosafeed.home_cloud.media.entity;

import com.roosafeed.home_cloud.auth.entity.User;
import com.roosafeed.home_cloud.common.entity.BaseEntity;
import com.roosafeed.home_cloud.media.enums.MediaType;
import com.roosafeed.home_cloud.common.enums.MediaVisibility;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "media_files",
        indexes = {
                @Index(name = "idx_media_owner", columnList = "owner_id"),
                @Index(name = "idx_media_type", columnList = "media_type"),
                @Index(name = "idx_media_visibility", columnList = "visibility")
        }
)
public class MediaFile extends BaseEntity {
    @Column(nullable = false)
    private String path;


    @Column(nullable = false)
    private String filename; // filename displayed to the user

    @Column(nullable = false)
    private String fsFilename; // the secret filename stored in the file system


    @Column(nullable = false)
    private String extension;


    @Column(nullable = false)
    private String mimeType;


    @Column(nullable = false)
    private long sizeBytes;


    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false)
    private MediaType mediaType;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaVisibility visibility = MediaVisibility.PRIVATE;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false)
    private Boolean markedAsDeleted = false;
}
