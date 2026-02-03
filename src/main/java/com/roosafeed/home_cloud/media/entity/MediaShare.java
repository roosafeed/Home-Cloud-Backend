package com.roosafeed.home_cloud.media.entity;

import com.roosafeed.home_cloud.auth.entity.User;
import com.roosafeed.home_cloud.common.entity.BaseEntity;
import com.roosafeed.home_cloud.common.enums.SharePermission;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "media_shares",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_media_user",
                        columnNames = {"media_id", "shared_with_id"}
                )
        }
)
public class MediaShare extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "media_id", nullable = false)
    private MediaFile media;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shared_with_id", nullable = false)
    private User sharedWith;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SharePermission permission = SharePermission.READ;
}
