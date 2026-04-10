package com.blocksy.server.common.api;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "分页返回")
public record PageResponse<T>(
        @Schema(description = "当前页", example = "1")
        Integer page,
        @Schema(description = "每页大小", example = "10")
        Integer pageSize,
        @Schema(description = "总数", example = "58")
        Long total,
        @Schema(description = "分页数据")
        List<T> items
) {
}
