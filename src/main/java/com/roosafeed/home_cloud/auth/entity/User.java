package com.roosafeed.home_cloud.auth.entity;

import com.roosafeed.home_cloud.common.entity.BaseEntity;
import com.roosafeed.home_cloud.common.enums.UserRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_user_email", columnList = "email", unique = true)
        }
)
public class User extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String email;


    @Column(nullable = false)
    private String passwordHash;


    @Column(nullable = false)
    private String displayName;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.USER;


    @Column(nullable = false)
    private boolean active = true;
}
