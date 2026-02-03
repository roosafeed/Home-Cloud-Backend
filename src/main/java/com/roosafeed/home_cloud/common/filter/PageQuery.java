package com.roosafeed.home_cloud.common.filter;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.domain.Sort;

@EqualsAndHashCode(callSuper = true)
@Data
// ?page=0&size=50&sortBy=createdAt&direction=DESC
public class PageQuery extends BaseSearchQuery {
    // 0 based page number
    private int page = 0;

    private int size = 50;

    // column to sort by
    private String sortBy = "createdAt";

    private Sort.Direction direction = Sort.Direction.DESC;
}
