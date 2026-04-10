package com.blocksy.server.modules.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SystemAnnouncementRequest(
        @Schema(description = "公告标题")
        @NotBlank(message = "title 不能为空")
        @Size(max = 120, message = "title 长度不能超过 120")
        String title,
        @Schema(description = "公告内容")
        @NotBlank(message = "content 不能为空")
        @Size(max = 500, message = "content 长度不能超过 500")
        String content
) {
}
