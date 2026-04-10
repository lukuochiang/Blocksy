package com.blocksy.server.modules.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PolicyDocumentSaveRequest(
        @NotBlank(message = "policyType 不能为空")
        @Size(max = 64, message = "policyType 不能超过 64 字符")
        String policyType,
        @NotBlank(message = "version 不能为空")
        @Size(max = 32, message = "version 不能超过 32 字符")
        String version,
        @NotBlank(message = "title 不能为空")
        @Size(max = 120, message = "title 不能超过 120 字符")
        String title,
        @NotBlank(message = "content 不能为空")
        String content
) {
}
