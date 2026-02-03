package com.roosafeed.home_cloud.auth.repository;

import com.roosafeed.home_cloud.auth.entity.Invite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InviteRepository extends JpaRepository<Invite, UUID> {
    Optional<Invite> findByToken(String token);
    Boolean existsByToken(String token);
}
