package com.blocksy.server.modules.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PushTaskCreateRequest(
        @NotBlank(message = "title 不能为空")
        @Size(max = 120, message = "title 不能超过 120 字符")
        String title,
        @NotBlank(message = "content 不能为空")
        @Size(max = 500, message = "content 不能超过 500 字符")
        String content,
        @Size(max = 32, message = "targetType 不能超过 32 字符")
        String targetType
) {
}
