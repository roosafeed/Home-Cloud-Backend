package com.roosafeed.home_cloud.common.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PagedApiResponse<T> extends ApiResponse<List<T>> {
    private Integer page;
    private Integer size;
    private Integer totalItems;

    public static <T> PagedApiResponse<T> ok(List<T> data, Integer page, Integer totalItems) {
        PagedApiResponse<T> response = new PagedApiResponse<T>();
        response.setData(data);
        response.setPage(page);
        response.setSize(data != null ? data.size() : 0);
        response.setTotalItems(totalItems);
        response.setSuccess(true);

        return response;
    }

    public static <T> PagedApiResponse<T> ok(Page<T> page) {
        PagedApiResponse<T> response = new PagedApiResponse<T>();
        response.setData(page.toList());
        response.setPage(page.getNumber());
        response.setSize(page.getSize());
        response.setTotalItems(page.getNumberOfElements());
        response.setSuccess(true);

        return response;
    }
}
