package com.blocksy.server.modules.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record PostMediaItem(
        @Schema(description = "MinIO 对象键", example = "f7f64f0a-demo.png")
        @NotBlank(message = "objectKey 不能为空") String objectKey,
        @Schema(description = "媒体访问 URL", example = "http://localhost:9000/blocksy-media/f7f64f0a-demo.png")
        @NotBlank(message = "url 不能为空") String url,
        @Schema(description = "媒体大小（字节）", example = "24567")
        @PositiveOrZero(message = "size 必须大于等于 0") Long size
) {
}
