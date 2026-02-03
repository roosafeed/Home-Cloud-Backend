package com.roosafeed.home_cloud.media.repository;

import com.roosafeed.home_cloud.media.entity.MediaMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MediaMetadataRepository extends JpaRepository<MediaMetadata, UUID> {
    Optional<MediaMetadata> findByMediaId(UUID mediaId);
}
