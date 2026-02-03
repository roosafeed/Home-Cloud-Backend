package com.roosafeed.home_cloud.media.repository;

import com.roosafeed.home_cloud.media.entity.MediaShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MediaShareRepository extends JpaRepository<MediaShare, UUID> {
    List<MediaShare> findBySharedWithId(UUID userId);
    List<MediaShare> findByMediaId(UUID mediaId);
    Optional<MediaShare> findByMediaIdAndSharedWithId(UUID mediaId, UUID userId);
}
