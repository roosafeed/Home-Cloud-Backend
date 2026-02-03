package com.roosafeed.home_cloud.common.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatusCode;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    private T data;
    private Boolean success;

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(data, true);
    }
}
