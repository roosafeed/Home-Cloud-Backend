package com.roosafeed.home_cloud.auth.entity;

import com.roosafeed.home_cloud.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(
        name = "invites",
        indexes = {
                @Index(name = "idx_invite_token", columnList = "token", unique = true)
        }
)
public class Invite extends BaseEntity {
    @Column(nullable = false)
    private String email;


    @Column(nullable = false, unique = true)
    private String token;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invited_by")
    private User invitedBy;


    @Column(nullable = false)
    private Instant expiresAt;


    private Instant usedAt;
}
