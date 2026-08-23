package com.InfoLink.utils;

import java.util.List;

import com.InfoLink.dto.PagedResponse;

public class PaginationUtil {

    public static <T> PagedResponse<T> buildPagedResponse(
            List<T> content, int page, int size, long totalElements) {
        return new PagedResponse<>(content, page, size, totalElements);
    }
}
