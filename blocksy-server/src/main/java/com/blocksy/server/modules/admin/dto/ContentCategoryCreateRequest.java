package com.blocksy.server.modules.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContentCategoryCreateRequest(
        @NotBlank(message = "module 不能为空")
        @Size(max = 32, message = "module 最长 32 字符")
        String module,
        @NotBlank(message = "code 不能为空")
        @Size(max = 64, message = "code 最长 64 字符")
        String code,
        @NotBlank(message = "name 不能为空")
        @Size(max = 120, message = "name 最长 120 字符")
        String name,
        Integer sortNo
) {
}
