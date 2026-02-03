package com.roosafeed.home_cloud.media.filter;

import com.roosafeed.home_cloud.common.filter.PageQuery;
import com.roosafeed.home_cloud.media.enums.MediaScope;
import com.roosafeed.home_cloud.media.enums.MediaType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class MediaSearchQuery extends PageQuery {
    private MediaType type;
    private MediaScope scope;
    private String path;
}
