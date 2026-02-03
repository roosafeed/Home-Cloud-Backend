package com.roosafeed.home_cloud.media.filter;

import com.roosafeed.home_cloud.auth.entity.User;
import com.roosafeed.home_cloud.media.entity.MediaFile;
import com.roosafeed.home_cloud.media.entity.MediaShare;
import com.roosafeed.home_cloud.media.enums.MediaScope;
import org.springframework.data.jpa.domain.Specification;

public class MediaSpecifications {
    private MediaSpecifications() {}

    public static Specification<MediaFile> ownedBy(User user) {
        return (root, query, cb) ->
                cb.equal(root.get("owner"), user);
    }

    public static Specification<MediaFile> sharedWith(User user) {
        return (root, query, cb) -> {

            // Avoid duplicate rows when combined with OR
            query.distinct(true);

            var subquery = query.subquery(Long.class);
            var shareRoot = subquery.from(MediaShare.class);

            subquery.select(cb.literal(1L))
                    .where(
                            cb.equal(shareRoot.get("media"), root),
                            cb.equal(shareRoot.get("sharedWith"), user)
                    );

            return cb.exists(subquery);
        };
    }

    public static Specification<MediaFile> ownershipScope(MediaScope scope, User user) {
        if (user == null) {
            throw new IllegalArgumentException("User is required");
        }

        if (scope == null) {
            return ownedBy(user);
        }

        // The new way
        return switch (scope) {
            case SHARED_WITH_ME ->
                    sharedWith(user)
                    .and(
                            Specification.not(ownedBy(user))
                    );

            case ALL ->
                    ownedBy(user)
                            .or(sharedWith(user));

            case DELETED ->
                    (root, query, cb) -> cb.conjunction(); // handled elsewhere

            case OWN ->
                    ownedBy(user);
        };
    }

    public static Specification<MediaFile> onPath(String path) {
        return  (root, query, cb) -> {
            if (path == null) {
                return cb.conjunction(); // no filtering
            }

            String pathStartsWith = path + "%";
            return cb.like(root.get("path"), pathStartsWith);
        };
    }


    public static Specification<MediaFile> isDeleted(Boolean isMarkedAsDeleted) {
        return (root, query, cb) -> {

            if (isMarkedAsDeleted == null) {
                return cb.conjunction(); // no filtering
            }

            if (Boolean.FALSE.equals(isMarkedAsDeleted)) {
                // markedAsDeleted = false OR markedAsDeleted IS NULL
                return cb.or(
                        cb.isFalse(root.get("markedAsDeleted")),
                        cb.isNull(root.get("markedAsDeleted"))
                );
            }

            return cb.isTrue(root.get("markedAsDeleted"));
        };
    }
}
