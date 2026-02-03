package com.roosafeed.home_cloud.media.entity;

import com.roosafeed.home_cloud.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Getter
@Setter
@Entity
@Table(
        name = "media_metadata",
        indexes = {
                @Index(name = "idx_metadata_media", columnList = "media_id")
        }
)
public class MediaMetadata extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "media_id", nullable = false, unique = true)
    private MediaFile media;

    /**
     * EXIF, GPS, AI tags, etc.
     * Stored as jsonb
    */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> metadata;
}
